#!/bin/bash

set -e
gcc -I../../../vm/rt/android/external/boringssl/include/ generate_constants.cc  -o generate_constants
./generate_constants com.android.org.conscrypt > ../src/main/java/com/android/org/conscrypt/NativeConstants.java