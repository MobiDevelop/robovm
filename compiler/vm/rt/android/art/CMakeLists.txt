project(android-art)

include_directories(
  ../libnativehelper
)

set(SRC
  openjdkjvm/OpenjdkJvm.cc
  src/dalvik_system_VMRuntime.cc
)

add_library(android-art OBJECT ${SRC})
set(ART_OBJECTS $<TARGET_OBJECTS:android-art> PARENT_SCOPE)