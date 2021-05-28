cmake_minimum_required(VERSION 3.20.0)

project(robovm-libhfscompressor)
include(ExternalProject)

find_package(JNI REQUIRED)


set(MACOSX YES)
if (ARCH STREQUAL "x86_64")
  set(X86_64 YES)
  set(C_CXX_FLAGS "${C_CXX_FLAGS} --target=x86_64-apple-macos10.6")
  set(LD_FLAGS "${LD_FLAGS} --target=x86_64-apple-macos10.6")
  set(TENTATIVE_CROSS_COMPILE_HOST "--host=x86_64-apple-darwin")
elseif (ARCH STREQUAL "arm64")
  set(ARM64 YES)
  set(C_CXX_FLAGS "${C_CXX_FLAGS} --target=arm64-apple-macos11.0")
  set(LD_FLAGS "${LD_FLAGS} --target=arm64-apple-macos11.0")
  set(TENTATIVE_CROSS_COMPILE_HOST "--host=aarch64-apple-darwin")
else()
  message(FATAL_ERROR "Unsupported ARCH: ${ARCH}")
endif()

set(EXTPREFIX "${CMAKE_BINARY_DIR}/ext")
set(C_CXX_FLAGS "${C_CXX_FLAGS} -I${EXTPREFIX}/include -I/usr/local/include -Wno-nullability-completeness")
set(EXTCFLAGS "${C_CXX_FLAGS}")
set(LDFLAGS "-L${EXTPREFIX}/lib")

exec_program(xcode-select ARGS --print-path OUTPUT_VARIABLE XCODE_PATH)
set(SYSROOT "${XCODE_PATH}/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk")
if(NOT IS_DIRECTORY ${SYSROOT})
  message(FATAL_ERROR "No supported MacOSX SDK found")
endif()
set(CMAKE_OSX_SYSROOT "${SYSROOT}")
set(EXTCFLAGS "${EXTCFLAGS} -isysroot ${SYSROOT}")

ExternalProject_Add(extlibarchive
  URL http://www.libarchive.org/downloads/libarchive-3.1.2.tar.gz
  CONFIGURE_COMMAND bash -c "CC=${CMAKE_C_COMPILER} CFLAGS='${EXTCFLAGS}' ./configure ${TENTATIVE_CROSS_COMPILE_HOST} --enable-shared=no --prefix=${EXTPREFIX}"
  BUILD_IN_SOURCE 1
)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${C_CXX_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${C_CXX_FLAGS} -fno-rtti")
set(CMAKE_INSTALL_DO_STRIP YES)

include_directories(${JAVA_INCLUDE_PATH} ${JAVA_INCLUDE_PATH2} ${EXTPREFIX}/include)

set(LIBS -lz -larchive)

if(MACOSX)
  set(LIBS ${LIBS} -liconv)
endif()

set(SRC
  src/main/native/hfscompressor.c
)

add_library(robovm-libhfscompressor SHARED ${SRC})
add_dependencies(robovm-libhfscompressor extlibarchive)
target_link_libraries(robovm-libhfscompressor ${LIBS} ${LDFLAGS})
set_target_properties(robovm-libhfscompressor PROPERTIES LINK_FLAGS "-Wl,-dead_strip -Wl,-exported_symbols_list -Wl,${CMAKE_SOURCE_DIR}/src/main/native/exports_macosx.sym")
set_target_properties(robovm-libhfscompressor PROPERTIES OUTPUT_NAME "hfscompressor-${ARCH}")
set(INSTALL_DIR ${CMAKE_SOURCE_DIR}/../bin/)
install(TARGETS robovm-libhfscompressor DESTINATION ${INSTALL_DIR})
