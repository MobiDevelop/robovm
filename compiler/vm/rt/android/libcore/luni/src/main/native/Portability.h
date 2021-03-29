/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef PORTABILITY_H_included
#define PORTABILITY_H_included

// RoboVM note: Start change
#if defined(__APPLE__)

// Mac OS.
//#include <AvailabilityMacros.h> // For MAC_OS_X_VERSION_MAX_ALLOWED

#include <libkern/OSByteOrder.h>
#define bswap_16 OSSwapInt16
#define bswap_32 OSSwapInt32
#define bswap_64 OSSwapInt64

// Mac OS has a 64-bit off_t and no 32-bit compatibility cruft.
#define flock64 flock
#define ftruncate64 ftruncate
#define isnanf __inline_isnanf
#define lseek64 lseek
#define pread64 pread
#define pwrite64 pwrite

#define F_GETLK64 F_GETLK
#define F_SETLK64 F_SETLK
#define F_SETLKW64 F_SETLKW

// TODO: Darwin appears to have an fdatasync syscall.
static inline int fdatasync(int fd) { return fsync(fd); }

#define _SO_CUSTOM_BASE 0x10000000
#define SO_PASSCRED (_SO_CUSTOM_BASE + 0)
#define SO_PEERCRED (_SO_CUSTOM_BASE + 1)

// For Linux-compatible sendfile(3).
#include <sys/socket.h>
#include <sys/types.h>
static inline ssize_t sendfile(int out_fd, int in_fd, off_t* offset, size_t count) {
  off_t in_out_count = count;
  int result = sendfile(in_fd, out_fd, *offset, &in_out_count, NULL, 0);
  if (result == -1) {
    return -1;
  }
  return in_out_count;
}

// For mincore(3).
#define _DARWIN_C_SOURCE
#include <sys/mman.h>
#undef _DARWIN_C_SOURCE
static inline int mincore(void* addr, size_t length, unsigned char* vec) {
  return mincore(addr, length, reinterpret_cast<char*>(vec));
}

#else

// Bionic or glibc.

#include <byteswap.h>
#include <sys/sendfile.h>
#include <sys/statvfs.h>

#include <netdb.h>
#if defined(__BIONIC__)
extern "C" int android_getaddrinfofornet(const char*, const char*, const struct addrinfo*, unsigned, unsigned, struct addrinfo**);
#else
static inline int android_getaddrinfofornet(const char* hostname, const char* servname,
    const struct addrinfo* hints, unsigned /*netid*/, unsigned /*mark*/, struct addrinfo** res) {
  return getaddrinfo(hostname, servname, hints, res);
}
#endif

#if defined(__GLIBC__) && !defined(__LP64__)

#include <unistd.h>

// 32 bit GLIBC hardcodes a "long int" as the return type for
// TEMP_FAILURE_RETRY so the return value here gets truncated for
// functions that return 64 bit types.
#undef TEMP_FAILURE_RETRY
#define TEMP_FAILURE_RETRY(exp) ({         \
    __typeof__(exp) _rc;                   \
    do {                                   \
        _rc = (exp);                       \
    } while (_rc == -1 && errno == EINTR); \
    _rc; })

#endif  // __GLIBC__ && !__LP64__

#endif  // __APPLE__

#endif  // PORTABILITY_H_included
