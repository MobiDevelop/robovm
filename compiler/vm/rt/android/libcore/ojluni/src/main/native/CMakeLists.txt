project(android-libcore-ojluni)

include_directories(
  ../../../../../external/icu4c/common
  ../../../../../external/icu4c/i18n
  ../../../../../external/boringssl/include
  ../../../../luni/src/main/native
)

add_definitions(-DBORINGSSL_IMPLEMENTATION)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra -Wno-unused-parameter")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DU_USING_ICU_NAMESPACE=0")

set(SRC
  ZipFile.c
  Inflater.c
  Deflater.c
  CRC32.c
  Adler32.c
  zip_util.c
  jni_util.c
  jni_util_md.c
  io_util.c
  canonicalize_md.c
  FileDescriptor_md.c
  DatagramChannelImpl.c
  DatagramDispatcher.c
  Console_md.c
  IOUtil.c
  PollArrayWrapper.c
  SocketChannelImpl.c
  FileChannelImpl.c
  FileDispatcherImpl.c
  FileOutputStream_md.c
  FileInputStream.c
  FileSystemPreferences.c
  UnixAsynchronousServerSocketChannelImpl.c
  UnixAsynchronousSocketChannelImpl.c
  io_util_md.c
  NativeThread.c
  FileKey.c
  UnixFileSystem_md.c
  ObjectStreamClass.c
  ObjectOutputStream.c
  ObjectInputStream.c
  UnixCopyFile.c
  UnixNativeDispatcher.c
  InetAddress.c
  net_util.c
  net_util_md.c
  Net.c
  MappedByteBuffer.c
  Inet6Address.c
  Inet4Address.c
  linux_close.cpp
  ServerSocketChannelImpl.c
  SocketInputStream.c
  SocketOutputStream.c
  Float.c
  Double.c
  StrictMath.c
  Math.c
  ProcessEnvironment_md.c
  System.c
  Runtime.c
  UNIXProcess_md.c
  Character.cpp
  socket_tagger_util.cpp
  OnLoad.cpp
)

if(LINUX)
  set(SRC
    ${SRC}
    EPoll.c
    EPollPort.c
    LinuxNativeDispatcher.c
    LinuxWatchService.c
  )
endif()


if(DARWIN)
  set(SRC
    ${SRC}
    KQueue.c
    KQueueArrayWrapper.c
    KQueuePort.c
  )
endif()

add_library(android-libcore-ojluni OBJECT ${SRC})
set(LIBCORE_OJLUNI_OBJECTS $<TARGET_OBJECTS:android-libcore-ojluni> PARENT_SCOPE)