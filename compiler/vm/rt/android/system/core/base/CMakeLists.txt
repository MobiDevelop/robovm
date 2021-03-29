project(android-system-core-base)

include_directories(
)

set(SRC
        logging.cpp
        threads.cpp
        stringprintf.cpp 
# FIXME: keep it empty for now as we don't need base code from Android core in robovm. Probably printf would be enough
# FIXME: fix on final linkage stage
)

add_library(android-system-core-base OBJECT ${SRC})
set(SYSTEM_CORE_BASE_OBJECTS $<TARGET_OBJECTS:android-system-core-base> PARENT_SCOPE)