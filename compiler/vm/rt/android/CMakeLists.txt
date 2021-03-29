project(android)

include_directories(
  # system/core/liblog
  system/core/liblog/include
  system/core/base/include

  # native helper
  libnativehelper/include/
  libnativehelper/include_jni/
  libnativehelper/platform_include/
  libnativehelper/header_only_include/
)

if(DARWIN)
  # this will enables BSD path of code instead of linux one that will allow lot of sources
  # to be compilable without changing sources
  add_definitions(-D_ALLBSD_SOURCE)
endif()

add_subdirectory(system/core/base)
add_subdirectory(libnativehelper)
add_subdirectory(libcore)
add_subdirectory(external/expat)
add_subdirectory(external/fdlibm)
add_subdirectory(external/icu4c)
add_subdirectory(external/javasqlite)
if(NOT DARWIN)
  # Mac OS X and iOS ships with sqlite so we'll just use that
  add_subdirectory(external/sqlite)
endif()
add_subdirectory(external/zlib)
add_subdirectory(external/boringssl)
add_subdirectory(art)
add_subdirectory(conscrypt)

set(ANDROID_OBJECTS
  ${SYSTEM_CORE_LIBLOG_OBJECTS}
  ${SYSTEM_CORE_BASE_OBJECTS}
  ${LIBNATIVEHELPER_OBJECTS}
  ${EXTERNAL_EXPAT_OBJECTS}
  ${EXTERNAL_FDLIBM_OBJECTS}
  ${EXTERNAL_ICU4C_OBJECTS}
  ${EXTERNAL_JAVASQLITE_OBJECTS}
  ${EXTERNAL_SQLITE_OBJECTS}
  ${EXTERNAL_ZLIB_OBJECTS}
  ${EXTERNAL_BORINGSSL_OBJECTS}
  ${LIBCORE_OBJECTS}
  ${ART_OBJECTS}
  ${DALVIK_OBJECTS}
  ${LIBCONSCRYPT_OBJECTS}

  PARENT_SCOPE
)
