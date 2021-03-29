project(android-external-icu4c-icui18n)

include_directories(. ../common)

add_definitions(-D_REENTRANT)
# add_definitions(-DUDATA_DEBUG)
add_definitions(-DU_I18N_IMPLEMENTATION)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -fvisibility=hidden -Wall -Werror -Wno-unused-parameter")

file(GLOB SRC *.cpp)

add_library(android-external-icu4c-icui18n OBJECT ${SRC})
set(EXTERNAL_ICU4C_I18N_OBJECTS $<TARGET_OBJECTS:android-external-icu4c-icui18n> PARENT_SCOPE)