/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.io;

import dalvik.system.BlockGuard;

/**
 * Used to detect unbuffered I/O.
 * @hide
 */
public final class IoTracker {
    private int opCount;
    private int totalByteCount;
    private boolean isOpen = true;
    private Mode mode = Mode.READ;

    public void trackIo(int byteCount) {
        ++opCount;
        totalByteCount += byteCount;
        if (isOpen && opCount > 10 && totalByteCount < 10*512) {
            BlockGuard.getThreadPolicy().onUnbufferedIO();
            isOpen = false;
        }
    }

    public void trackIo(int byteCount, Mode mode) {
        if (this.mode != mode) {
            reset();
            this.mode = mode;
        }
        trackIo(byteCount);
    }

    /**
     * Resets the state of the IoTracker, except {@link #isOpen} as it is not required to notify
     * again and again about the same stream.
     * This is primarily used by RandomAccessFile to consider a case when {@link
     * java.io.RandomAccessFile#seek seek} is called.
     */
    public void reset() {
        opCount = 0;
        totalByteCount = 0;
    }

    public enum Mode {
        READ,
        WRITE
    }
}
