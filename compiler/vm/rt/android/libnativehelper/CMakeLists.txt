project(android-libnativehelper)

include_directories(
  include
  header_only_include
)

set(SRC
  JNIHelp.cpp
  JniConstants.cpp
  toStringArray.cpp
  JniInvocation.cpp
)

add_library(android-libnativehelper OBJECT ${SRC})
set(LIBNATIVEHELPER_OBJECTS $<TARGET_OBJECTS:android-libnativehelper> PARENT_SCOPE)