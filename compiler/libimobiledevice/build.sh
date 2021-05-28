#!/bin/bash

SELF=$(basename $0)
BASE=$(cd $(dirname $0); pwd -P)
CLEAN=0

function usage {
  cat <<EOF
Usage: $SELF [options]
Options:
  --clean                 Cleans the build dir before starting the build.
  --target=...            Specifies the target(s) to build for. Supported
                          targets are macosx-x86_64, macosx-arm64, linux-x86_64,
                          linux-x86. Enclose multiple targets in quotes and
                          separate with spaces or specify --target multiple
                          times. If not set the current host OS determines the
                          targets. macosx-x86_64, macosx-arm64 on MacOSX and
                          linux-x86_64 and linux-x86 on Linux.
  --help                  Displays this information and exits.
EOF
  exit $1
}

while [ "${1:0:2}" = '--' ]; do
  NAME=${1%%=*}
  VALUE=${1#*=}
  case $NAME in
    '--target') TARGETS="$TARGETS $VALUE" ;;
    '--clean') CLEAN=1 ;;
    '--help')
      usage 0
      ;;
    *)
      echo "Unrecognized option or syntax error in option '$1'"
      usage 1
      ;;
  esac
  shift
done

OS=$(uname)
if [ "x$TARGETS" = 'x' ]; then
  case $OS in
  Darwin)
    TARGETS="macosx-x86_64 macosx-arm64"
    ;;
  Linux)
    TARGETS="linux-x86_64 linux-x86"
    ;;
  *)
    echo "Unsupported OS: $OS"
    exit 1
    ;;
  esac
fi

# Validate targets
for T in $TARGETS; do
  if ! [[ $T =~ (macosx-(x86_64|arm64))|(linux-(x86_64)) ]] ; then
    echo "Unsupported target: $T"
    exit 1
  fi
done

mkdir -p "$BASE/target-libimobiledevice/build"
if [ "$CLEAN" = '1' ]; then
  for T in $TARGETS; do
    rm -rf "$BASE/target-libimobiledevice/build/$T"
  done
fi

if [ $(uname) = 'Darwin' ]; then
  if xcrun -f clang &> /dev/null; then
    CC=$(xcrun -f clang)
  else
    CC=$(which clang)
  fi
  if xcrun -f clang++ &> /dev/null; then
    CXX=$(xcrun -f clang++)
  else
    CXX=$(which clang++)
  fi
else
  CC=$(which gcc)
  CXX=$(which g++)
fi

for T in $TARGETS; do
  OS=${T%%-*}
  ARCH=${T#*-}
  BUILD_TYPE=Release
  # find out cross-compilation parameters, required to controll CMake on Apple Silicon 
  case $OS in
  macosx)
    SYSTEM_NAME_PARAM="-DCMAKE_SYSTEM_NAME=Darwin"
    ;;
  *)
    SYSTEM_NAME_PARAM=""
    ;;
  esac
  mkdir -p "$BASE/target-libimobiledevice/build/$T"
  rm -rf "$BASE/binaries/$OS/$ARCH"
  bash -c "cd '$BASE/target-libimobiledevice/build/$T'; cmake $SYSTEM_NAME_PARAM -DCMAKE_C_COMPILER=$CC -DCMAKE_CXX_COMPILER=$CXX -DCMAKE_BUILD_TYPE=$BUILD_TYPE -DOS=$OS -DARCH=$ARCH '$BASE'; make install/strip"
  R=$?
  if [[ $R != 0 ]]; then
    echo "$T build failed"
    exit $R
  fi
done
