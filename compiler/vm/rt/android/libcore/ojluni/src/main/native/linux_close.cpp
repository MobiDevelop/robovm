/*
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/uio.h>
#include <unistd.h>
#include <errno.h>

// Android-changed: Fuchsia: Fix poll.h include location
// #include <sys/poll.h>
#if !defined(__Fuchsia__)
#include <sys/poll.h>
#else
#include <poll.h>
#endif

#include <AsynchronousCloseMonitor.h>

extern "C" {

/*
 * Signal to unblock thread
 */
#if defined(__APPLE__)
// RoboVM Note: changes start
static const int sigWakeup = SIGUSR2;
// RoboVM Note: end of changes
#else
// Android-changed: Bionic (and AsynchronousCloseMonitor) expects libcore to use
// __SIGRTMIN + 2, not __SIGRTMAX - 2
static int sigWakeup = (__SIGRTMIN + 2);
#endif

/*
 * Close or dup2 a file descriptor ensuring that all threads blocked on
 * the file descriptor are notified via a wakeup signal.
 *
 *      fd1 < 0    => close(fd2)
 *      fd1 >= 0   => dup2(fd1, fd2)
 *
 * Returns -1 with errno set if operation fails.
 */
static int closefd(int fd1, int fd2) {
    int rv, orig_errno;

    AsynchronousCloseMonitor::signalBlockedThreads(fd2);

    /*
     * And close/dup the file descriptor
     * (restart if interrupted by signal)
     */
    do {
      if (fd1 < 0) {
        rv = close(fd2);
      } else {
        rv = dup2(fd1, fd2);
      }
    } while (rv == -1 && errno == EINTR);
    return rv;
}

/*
 * Wrapper for dup2 - same semantics as dup2 system call except
 * that any threads blocked in an I/O system call on fd2 will be
 * preempted and return -1/EBADF;
 */
int NET_Dup2(int fd, int fd2) {
    if (fd < 0) {
        errno = EBADF;
        return -1;
    }
    return closefd(fd, fd2);
}

/*
 * Wrapper for close - same semantics as close system call
 * except that any threads blocked in an I/O on fd will be
 * preempted and the I/O system call will return -1/EBADF.
 */
int NET_SocketClose(int fd) {
    return closefd(-1, fd);
}

/************** Basic I/O operations here ***************/

/*
 * Macro to perform a blocking IO operation. Restarts
 * automatically if interrupted by signal (other than
 * our wakeup signal)
 */
#define BLOCKING_IO_RETURN_INT(FD, FUNC) {      \
    int ret;                                    \
    int _syscallErrno; \
    do {                                        \
        bool _wasSignaled; \
        {                                       \
            AsynchronousCloseMonitor _monitor(FD); \
            ret = FUNC;                            \
            _syscallErrno = errno; \
            _wasSignaled = _monitor.wasSignaled(); \
        } \
        errno = _wasSignaled ? EBADF : _syscallErrno; \
    } while (ret == -1 && errno == EINTR);      \
    return ret;                                 \
}


int NET_Read(int s, void* buf, size_t len) {
    BLOCKING_IO_RETURN_INT( s, recv(s, buf, len, 0) );
}

int NET_ReadV(int s, const struct iovec * vector, int count) {
    BLOCKING_IO_RETURN_INT( s, readv(s, vector, count) );
}

int NET_RecvFrom(int s, void *buf, int len, unsigned int flags,
       struct sockaddr *from, int *fromlen) {
    socklen_t socklen = *fromlen;
    BLOCKING_IO_RETURN_INT( s, recvfrom(s, buf, len, flags, from, &socklen) );
    *fromlen = socklen;
}

int NET_Send(int s, void *msg, int len, unsigned int flags) {
    BLOCKING_IO_RETURN_INT( s, send(s, msg, len, flags) );
}

int NET_WriteV(int s, const struct iovec * vector, int count) {
    BLOCKING_IO_RETURN_INT( s, writev(s, vector, count) );
}

int NET_SendTo(int s, const void *msg, int len,  unsigned  int
       flags, const struct sockaddr *to, int tolen) {
    BLOCKING_IO_RETURN_INT( s, sendto(s, msg, len, flags, to, tolen) );
}

int NET_Accept(int s, struct sockaddr *addr, int *addrlen) {
    socklen_t socklen = *addrlen;
    BLOCKING_IO_RETURN_INT( s, accept(s, addr, &socklen) );
    *addrlen = socklen;
}

int NET_Connect(int s, struct sockaddr *addr, int addrlen) {
    BLOCKING_IO_RETURN_INT( s, connect(s, addr, addrlen) );
}

#ifndef USE_SELECT
int NET_Poll(struct pollfd *ufds, unsigned int nfds, int timeout) {
    BLOCKING_IO_RETURN_INT( ufds[0].fd, poll(ufds, nfds, timeout) );
}
#else
int NET_Select(int s, fd_set *readfds, fd_set *writefds,
               fd_set *exceptfds, struct timeval *timeout) {
    BLOCKING_IO_RETURN_INT( s-1,
                            select(s, readfds, writefds, exceptfds, timeout) );
}
#endif

/*
 * Wrapper for poll(s, timeout).
 * Auto restarts with adjusted timeout if interrupted by
 * signal other than our wakeup signal.
 *
 * If s < 0, exits early rather than delegating to poll().
 * TODO: Investigate whether it'd be better to handle this
 * case at the caller so that this function is never called
 * for s < 0.
 */
int NET_Timeout(int s, long timeout) {
    long prevtime = 0, newtime;
    struct timeval t;

    /*
     * b/27763633
     * Avoid blocking calls to poll() for invalid sockets, e.g. when
     * called from PlainSocketImpl_socketAccept with fd == -1.
     */
    if (s < 0) {
        errno = EBADF;
        return -1;
    }

    /*
     * Pick up current time as may need to adjust timeout
     */
    if (timeout > 0) {
        gettimeofday(&t, NULL);
        prevtime = t.tv_sec * 1000  +  t.tv_usec / 1000;
    }

    for(;;) {
        struct pollfd pfd;
        int rv;

        /*
         * Poll the fd. If interrupted by our wakeup signal
         * errno will be set to EBADF.
         */
        pfd.fd = s;
        pfd.events = POLLIN | POLLERR;

        bool wasSignaled;
        int syscallErrno;
        {
          AsynchronousCloseMonitor monitor(s);
          rv = poll(&pfd, 1, timeout);
          syscallErrno = errno;
          wasSignaled = monitor.wasSignaled();
        }
        errno = wasSignaled ? EBADF : syscallErrno;

        /*
         * If interrupted then adjust timeout. If timeout
         * has expired return 0 (indicating timeout expired).
         */
        if (rv < 0 && errno == EINTR) {
            if (timeout > 0) {
                gettimeofday(&t, NULL);
                newtime = t.tv_sec * 1000  +  t.tv_usec / 1000;
                timeout -= newtime - prevtime;
                if (timeout <= 0) {
                    return 0;
                }
                prevtime = newtime;
            }
        } else {
            return rv;
        }

    }
}

}
