project(android-external-icu4c)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Werror")

add_subdirectory(common)
add_subdirectory(i18n)

set(EXTERNAL_ICU4C_OBJECTS
  ${EXTERNAL_ICU4C_COMMON_OBJECTS}
  ${EXTERNAL_ICU4C_I18N_OBJECTS}
  PARENT_SCOPE
)