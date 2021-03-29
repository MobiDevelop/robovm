project(android-external-icu4c-icuuc)

include_directories(.)

add_definitions(-D_REENTRANT)
add_definitions(-DU_COMMON_IMPLEMENTATION)
# add_definitions(-DUDATA_DEBUG)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -fvisibility=hidden -Wall -Werror -Wno-unused-parameter -Wno-missing-field-initializers")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-sign-compare -Wno-deprecated-declarations")

file(GLOB SRC *.cpp)
set(SRC
  ${SRC}
  ../stubdata/stubdata.cpp
)


add_library(android-external-icu4c-icuuc OBJECT ${SRC})
set(EXTERNAL_ICU4C_COMMON_OBJECTS $<TARGET_OBJECTS:android-external-icu4c-icuuc> PARENT_SCOPE)