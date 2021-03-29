/*
 * Copyright (C) 2010 The Android Open Source Project
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

#ifndef ASYNCHRONOUS_CLOSE_MONITOR_H_included
#define ASYNCHRONOUS_CLOSE_MONITOR_H_included

#include <pthread.h>

// Public API for library function.
extern "C" {
void async_close_monitor_destroy(void* instance);
void async_close_monitor_static_init();
void async_close_monitor_signal_blocked_threads(int fd);
int async_close_monitor_was_signalled(const void* instance);
void* async_close_monitor_create(int fd);
}

/**
 * AsynchronousCloseMonitor helps implement Java's asynchronous close semantics.
 *
 * AsynchronousCloseMonitor::init must be called before anything else.
 *
 * Every blocking I/O operation must be surrounded by an AsynchronousCloseMonitor
 * instance. For example:
 *
 *   {
 *     AsynchronousCloseMonitor monitor(fd);
 *     byteCount = ::read(fd, buf, sizeof(buf));
 *   }
 *
 * To interrupt all threads currently blocked on file descriptor 'fd', call signalBlockedThreads:
 *
 *   AsynchronousCloseMonitor::signalBlockedThreads(fd);
 *
 * To test to see if the interruption was due to the signalBlockedThreads call:
 *
 *   monitor.wasSignaled();
 */
class AsynchronousCloseMonitor {
public:
    explicit AsynchronousCloseMonitor(int fd) {
        instance_ = async_close_monitor_create(fd);
    }
    ~AsynchronousCloseMonitor() {
        async_close_monitor_destroy(instance_);
    }
    bool wasSignaled() const {
        return async_close_monitor_was_signalled(instance_) != 0;
    }

    static void init() {
        async_close_monitor_static_init();
    }

    static void signalBlockedThreads(int fd) {
        async_close_monitor_signal_blocked_threads(fd);
    }

private:
    AsynchronousCloseMonitor(const AsynchronousCloseMonitor&) = delete;
    AsynchronousCloseMonitor& operator=(const AsynchronousCloseMonitor&) = delete;

    void* instance_;
};

#endif  // ASYNCHRONOUS_CLOSE_MONITOR_H_included
