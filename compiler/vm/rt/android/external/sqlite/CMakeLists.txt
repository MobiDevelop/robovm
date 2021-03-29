project(android-external-sqlite)

add_subdirectory(android)
add_subdirectory(dist)


set(EXTERNAL_SQLITE_OBJECTS
  ${EXTERNAL_SQLITE_ANDROID_OBJECTS}
  ${EXTERNAL_SQLITE_DIST_OBJECTS}
  PARENT_SCOPE
)