project(android-external-zlib)

add_definitions(-DUSE_MMAP -DZLIB_CONST)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -Wall -Werror")

set(SRC
  src/adler32.c  
  src/compress.c  
  src/crc32.c  
  src/deflate.c  
  src/gzclose.c  
  src/gzlib.c  
  src/gzread.c  
  src/gzwrite.c  
  src/infback.c  
  src/inflate.c  
  src/inftrees.c  
  src/inffast.c  
  src/trees.c  
  src/uncompr.c  
  src/zutil.c  
)


add_library(android-external-zlib OBJECT ${SRC})
set(EXTERNAL_ZLIB_OBJECTS $<TARGET_OBJECTS:android-external-zlib> PARENT_SCOPE)