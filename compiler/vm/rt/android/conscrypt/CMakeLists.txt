project(android-conscrypt)

include_directories(
  include
  ../external/boringssl/include
)

add_definitions(-DANDROID -DJNI_JARJAR_PREFIX=com/android/)


set(SRC
  cpp/conscrypt/compatibility_close_monitor.cc
  cpp/conscrypt/jniload.cc
  cpp/conscrypt/jniutil.cc
  cpp/conscrypt/native_crypto.cc
  cpp/conscrypt/netutil.cc
  cpp/conscrypt/trace.cc
)

add_library(android-conscrypt OBJECT ${SRC})
set(LIBCONSCRYPT_OBJECTS $<TARGET_OBJECTS:android-conscrypt> PARENT_SCOPE)
