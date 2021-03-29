project(android-libcore-luni)

include_directories(
  ../../../../../external/icu4c/common
  ../../../../../external/icu4c/i18n
  ../../../../../external/boringssl/include
)

add_definitions(-DBORINGSSL_IMPLEMENTATION)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra -Werror")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DU_USING_ICU_NAMESPACE=0")

set(SRC
  ExecStrings.cpp
  IcuUtilities.cpp
  JniException.cpp
  NetworkUtilities.cpp
  Register.cpp
  ZipUtilities.cpp
  android_system_OsConstants.cpp
  cbigint.cpp
  java_lang_StringToReal.cpp
  java_lang_invoke_MethodHandle.cpp
  java_lang_invoke_VarHandle.cpp
  java_math_NativeBN.cpp
  java_util_regex_Matcher.cpp
  java_util_regex_Pattern.cpp
  libcore_icu_ICU.cpp
  libcore_icu_NativeConverter.cpp
  libcore_icu_TimeZoneNames.cpp
  libcore_io_AsynchronousCloseMonitor.cpp
  libcore_io_Memory.cpp
  libcore_util_NativeAllocationRegistry.cpp
  org_apache_harmony_xml_ExpatParser.cpp
  valueOf.cpp
  AsynchronousCloseMonitor.cpp
  # files from luni4
  libcore_io_Posix.cpp
  java_nio_charset_Charsets.cpp
)
if(LINUX)
  set(SRC
    ${SRC}
    libcore_io_Linux.cpp
  )
endif()

add_library(android-libcore-luni OBJECT ${SRC})
set(LIBCORE_LUNI_OBJECTS $<TARGET_OBJECTS:android-libcore-luni> PARENT_SCOPE)
