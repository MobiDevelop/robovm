project(android-external-sqlite-android)

include_directories(
  ../../icu4c/common
  ../../icu4c/i18n
)

add_definitions(
  -DSQLITE_ENABLE_ICU
)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Werror -Wno-unused-function -Wno-unused-variable")

set(SRC
  PhoneNumberUtils.cpp
  OldPhoneNumberUtils.cpp
  sqlite3_android.cpp
)

add_library(android-external-sqlite-android OBJECT ${SRC})
set(EXTERNAL_SQLITE_ANDROID_OBJECTS $<TARGET_OBJECTS:android-external-sqlite-android> PARENT_SCOPE)