cmake_minimum_required(VERSION 3.20.0)

include(ExternalProject)

project(robovm-llvm)

find_package(JNI REQUIRED)

message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")

if(OS STREQUAL "linux")
  set(LINUX YES)
  if (ARCH STREQUAL "x86_64")
    set(X86_64 YES)
    set(C_CXX_FLAGS "${C_CXX_FLAGS} -m64")
    set(LD_FLAGS "${LD_FLAGS} -m64")
  else()
    message(FATAL_ERROR "Unsupported ARCH: ${ARCH}")
  endif()
elseif(OS STREQUAL "macosx")
  set(MACOSX YES)
  if (ARCH STREQUAL "x86_64")
    set(X86_64 YES)
    set(C_CXX_FLAGS "${C_CXX_FLAGS} --target=x86_64-apple-macos10.9")
    set(LD_FLAGS "${LD_FLAGS} --target=x86_64-apple-macos10.9")
  elseif (ARCH STREQUAL "arm64")
    set(ARM64 YES)
    set(C_CXX_FLAGS "${C_CXX_FLAGS} --target=arm64-apple-macos11.0")
    set(LD_FLAGS "${LD_FLAGS} --target=arm64-apple-macos11.0")
  else()
    message(FATAL_ERROR "Unsupported ARCH: ${ARCH}")
  endif()

  exec_program(xcode-select ARGS --print-path OUTPUT_VARIABLE XCODE_PATH)
  set(SYSROOT "${XCODE_PATH}/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk")
  if(NOT IS_DIRECTORY ${SYSROOT})
    message(FATAL_ERROR "No supported MacOSX SDK found")
  endif()
  set(CMAKE_OSX_SYSROOT "${SYSROOT}")
else()
  message(FATAL_ERROR "Unsupported OS: ${OS}")
endif()

set(C_CXX_FLAGS "${C_CXX_FLAGS} -fvisibility=hidden -fdata-sections -ffunction-sections")

ExternalProject_Add(extclang 
  GIT_REPOSITORY git://github.com/mobivm/clang.git
  GIT_TAG 7b389564897bc6c0431c42f8f1825f04624ff4e3
  UPDATE_COMMAND ""
  CONFIGURE_COMMAND "" 
  BUILD_COMMAND ""
  INSTALL_COMMAND ""
  BUILD_IN_SOURCE 1
)

set(LLVM_ARCHS "X86^^ARM^^AArch64")
set(LLVM_C_FLAGS "${C_CXX_FLAGS} -fvisibility=hidden -fdata-sections -ffunction-sections")
set(LLVM_CXX_FLAGS "${C_CXX_FLAGS} -fvisibility=hidden -fdata-sections -ffunction-sections")
ExternalProject_Add(extllvm
  DEPENDS extclang
  GIT_REPOSITORY git://github.com/mobivm/llvm.git
  GIT_TAG aceb47b80838681fe65a1d01aac7aae7b863ac5a
  PATCH_COMMAND bash -c "rm -rf tools/clang && ln -s ../../../../extclang-prefix/src/extclang tools/clang && ${CMAKE_SOURCE_DIR}/patches/apply-patches"
  LIST_SEPARATOR ^^
  CONFIGURE_COMMAND cmake -DCMAKE_SYSTEM_NAME=${CMAKE_SYSTEM_NAME} -DCMAKE_OSX_SYSROOT=${CMAKE_OSX_SYSROOT} -DCMAKE_CXX_COMPILER=${CMAKE_CXX_COMPILER} -DCMAKE_C_COMPILER=${CMAKE_C_COMPILER} -DCMAKE_RC_COMPILER=${CMAKE_RC_COMPILER} -DCMAKE_SYSTEM_NAME=${CMAKE_SYSTEM_NAME} -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE} -DBUILD_SHARED_LIBS=OFF "-DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/llvm" "-DLLVM_TARGETS_TO_BUILD=${LLVM_ARCHS}" "-DCMAKE_C_FLAGS=${LLVM_C_FLAGS}" "-DCMAKE_CXX_FLAGS=${LLVM_CXX_FLAGS}" -DLLVM_ENABLE_LIBCXX=YES -DLLVM_ENABLE_CXX1Y=YES -DLLVM_ENABLE_THREADS=YES -DLIBCLANG_BUILD_STATIC=YES ../extllvm/
)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${C_CXX_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${C_CXX_FLAGS} -fno-rtti -std=c++0x")
set(CMAKE_INSTALL_DO_STRIP YES)
if(MACOSX)
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++")
endif()

add_definitions(-D__STDC_LIMIT_MACROS -D__STDC_CONSTANT_MACROS)
include_directories(${JAVA_INCLUDE_PATH} ${JAVA_INCLUDE_PATH2} ${CMAKE_BINARY_DIR}/llvm/include)

# This was generated using 'llvm-config --libs core bitreader bitwriter irreader ipo arm x86 aarch64 debuginfo linker option instrumentation objcarcopts'
set(LLVM_LIBS
  -lLLVMObjCARCOpts -lLLVMInstrumentation -lLLVMOption -lLLVMLinker
  -lLLVMDebugInfo -lLLVMAArch64Disassembler -lLLVMAArch64CodeGen
  -lLLVMAArch64AsmParser -lLLVMAArch64Desc -lLLVMAArch64Info
  -lLLVMAArch64AsmPrinter -lLLVMAArch64Utils -lLLVMX86Disassembler
  -lLLVMX86AsmParser -lLLVMX86CodeGen -lLLVMX86Desc -lLLVMX86Info
  -lLLVMX86AsmPrinter -lLLVMX86Utils -lLLVMARMDisassembler -lLLVMARMCodeGen
  -lLLVMSelectionDAG -lLLVMAsmPrinter -lLLVMCodeGen -lLLVMARMAsmParser
  -lLLVMARMDesc -lLLVMMCDisassembler -lLLVMARMInfo -lLLVMARMAsmPrinter
  -lLLVMipo -lLLVMVectorize -lLLVMScalarOpts -lLLVMProfileData -lLLVMObject
  -lLLVMMCParser -lLLVMInstCombine -lLLVMTransformUtils -lLLVMipa
  -lLLVMAnalysis -lLLVMTarget -lLLVMMC -lLLVMIRReader -lLLVMAsmParser
  -lLLVMBitWriter -lLLVMBitReader -lLLVMCore -lLLVMSupport
)
# Required clang libs
if(LINUX)
  # With the GNU linker library order is important and some libraries have
  # to be repeated in order to resolve all symbols properly.
  set(LLVM_LIBS
    -lclangBasic -lclangDriver -lclangFrontend -lclangCodeGen
    -lclangFrontend -lclangDriver -lclangParse -lclangSerialization
    -lclangSema -lclangEdit -lclangAnalysis -lclangAST -lclangLex
    -lclangBasic ${LLVM_LIBS}
  )
elseif(MACOSX)
  set(LLVM_LIBS ${LLVM_LIBS}
    -lclangFrontend -lclangAST -lclangBasic -lclangLex -lclangSerialization
    -lclangSema -lclangEdit -lclangCodeGen -lclangParse -lclangAnalysis
    -lclangDriver
  )
endif()

# This was generated using 'llvm-config --ldflags'
if(LINUX)
  set(LLVM_LDFLAGS
    -L"${CMAKE_BINARY_DIR}/llvm/lib"
    -lrt -ldl -lpthread -lz
  )
elseif(MACOSX)
  set(LLVM_LDFLAGS
    -L"${CMAKE_BINARY_DIR}/llvm/lib"
    -lcurses -lpthread -lz
  )
endif()

set(SRC
  src/main/native/LLVM_wrap.c
  src/main/native/LLVMExtra.cpp
  src/main/native/ClangExtra.cpp
)

add_library(robovm-llvm SHARED ${SRC})
add_dependencies(robovm-llvm extllvm)
target_link_libraries(robovm-llvm ${LLVM_LIBS} ${LLVM_LDFLAGS})
set_target_properties(robovm-llvm PROPERTIES LINKER_LANGUAGE CXX)
if(LINUX)
  set_target_properties(robovm-llvm PROPERTIES LINK_FLAGS "-Wl,--gc-sections -Wl,--version-script=${CMAKE_SOURCE_DIR}/src/main/native/exports_linux.sym")
elseif(MACOSX)
  set_target_properties(robovm-llvm PROPERTIES LINK_FLAGS "-Wl,-dead_strip -Wl,-exported_symbols_list -Wl,${CMAKE_SOURCE_DIR}/src/main/native/exports_macosx.sym")
endif()
set(INSTALL_DIR ${CMAKE_SOURCE_DIR}/src/main/resources/org/robovm/llvm/binding/${OS}/${ARCH})
install(TARGETS robovm-llvm LIBRARY DESTINATION ${INSTALL_DIR})
