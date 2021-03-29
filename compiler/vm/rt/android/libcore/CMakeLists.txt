project(android-libcore)

include_directories(
  ../libnativehelper
)

add_subdirectory(luni/src/main/native)
add_subdirectory(ojluni/src/main/native)

set(LIBCORE_OBJECTS
  ${LIBCORE_LUNI_OBJECTS}
  ${LIBCORE_OJLUNI_OBJECTS}
  PARENT_SCOPE
)