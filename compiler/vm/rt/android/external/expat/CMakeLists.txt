project(android-external-expat)

include_directories(. lib)

add_definitions(-DHAVE_EXPAT_CONFIG_H -UWIN32_LEAN_AND_MEAN)
add_compile_options(-Wall)
add_compile_options(-Werror)
add_compile_options(-Wmissing-prototypes)
add_compile_options(-Wstrict-prototypes)
add_compile_options(-Wno-unused-function)
add_compile_options(-Wno-unused-parameter)
add_compile_options(-Wno-missing-field-initializers)
add_compile_options(-fexceptions)

if(DARWIN)
  add_compile_options(-fno-common)
endif(DARWIN)

set(SRC
  lib/xmlparse.c
  lib/xmlrole.c
  lib/xmltok.c
)

add_library(android-external-expat OBJECT ${SRC})
set(EXTERNAL_EXPAT_OBJECTS $<TARGET_OBJECTS:android-external-expat> PARENT_SCOPE)