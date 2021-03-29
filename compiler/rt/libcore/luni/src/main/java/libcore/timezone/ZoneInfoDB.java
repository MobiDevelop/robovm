/*
 * Copyright (C) 2007 The Android Open Source Project
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

package libcore.timezone;

import android.system.ErrnoException;
import dalvik.annotation.optimization.ReachabilitySensitive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import libcore.io.BufferIterator;
import libcore.io.MemoryMappedFile;
import libcore.util.BasicLruCache;
import libcore.util.ZoneInfo;

/**
 * A class used to initialize the time zone database. This implementation uses the
 * Olson tzdata as the source of time zone information. However, to conserve
 * disk space (inodes) and reduce I/O, all the data is concatenated into a single file,
 * with an index to indicate the starting position of each time zone record.
 *
 * @hide - used to implement TimeZone
 */
@libcore.api.CorePlatformApi
public final class ZoneInfoDB {

  // VisibleForTesting
  public static final String TZDATA_FILE = "tzdata";

  private static final TzData DATA =
          TzData.loadTzDataWithFallback(TimeZoneDataFiles.getTimeZoneFilePaths(TZDATA_FILE));

  /** @hide */
  @libcore.api.CorePlatformApi
  public static class TzData implements AutoCloseable {

    // The database reserves 40 bytes for each id.
    private static final int SIZEOF_TZNAME = 40;

    // The database uses 32-bit (4 byte) integers.
    private static final int SIZEOF_TZINT = 4;

    // Each index entry takes up this number of bytes.
    public static final int SIZEOF_INDEX_ENTRY = SIZEOF_TZNAME + 3 * SIZEOF_TZINT;

    /**
     * {@code true} if {@link #close()} has been called meaning the instance cannot provide any
     * data.
     */
    private boolean closed;

    /**
     * Rather than open, read, and close the big data file each time we look up a time zone,
     * we map the big data file during startup, and then just use the MemoryMappedFile.
     *
     * At the moment, this "big" data file is about 500 KiB. At some point, that will be small
     * enough that we could just keep the byte[] in memory, but using mmap(2) like this has the
     * nice property that even if someone replaces the file under us (because multiple gservices
     * updates have gone out, say), we still get a consistent (if outdated) view of the world.
     */
    // Android-added: @ReachabilitySensitive
    @ReachabilitySensitive
    private MemoryMappedFile mappedFile;

    private String version;
    private String zoneTab;

    /**
     * The 'ids' array contains time zone ids sorted alphabetically, for binary searching.
     * The other two arrays are in the same order. 'byteOffsets' gives the byte offset
     * of each time zone, and 'rawUtcOffsetsCache' gives the time zone's raw UTC offset.
     */
    private String[] ids;
    private int[] byteOffsets;
    private int[] rawUtcOffsetsCache; // Access this via getRawUtcOffsets instead.

    /**
     * ZoneInfo objects are worth caching because they are expensive to create.
     * See http://b/8270865 for context.
     */
    private final static int CACHE_SIZE = 1;
    private final BasicLruCache<String, ZoneInfo> cache =
        new BasicLruCache<String, ZoneInfo>(CACHE_SIZE) {
      @Override
      protected ZoneInfo create(String id) {
        try {
          return makeTimeZoneUncached(id);
        } catch (IOException e) {
          throw new IllegalStateException("Unable to load timezone for ID=" + id, e);
        }
      }
    };

    /**
     * Loads the data at the specified paths in order, returning the first valid one as a
     * {@link TzData} object. If there is no valid one found a basic fallback instance is created
     * containing just GMT.
     */
    public static TzData loadTzDataWithFallback(String... paths) {
      for (String path : paths) {
        TzData tzData = new TzData();
        if (tzData.loadData(path)) {
          return tzData;
        }
      }

      // We didn't find any usable tzdata on disk, so let's just hard-code knowledge of "GMT".
      // This is actually implemented in TimeZone itself, so if this is the only time zone
      // we report, we won't be asked any more questions.
      System.logE("Couldn't find any " + TZDATA_FILE + " file!");
      return TzData.createFallback();
    }

    /**
     * Loads the data at the specified path and returns the {@link TzData} object if it is valid,
     * otherwise {@code null}.
     */
    @libcore.api.CorePlatformApi
    public static TzData loadTzData(String path) {
      TzData tzData = new TzData();
      if (tzData.loadData(path)) {
        return tzData;
      }
      return null;
    }

    private static TzData createFallback() {
      TzData tzData = new TzData();
      tzData.populateFallback();
      return tzData;
    }

    private TzData() {
    }

    /**
     * Visible for testing.
     */
    public BufferIterator getBufferIterator(String id) {
      checkNotClosed();

      // Work out where in the big data file this time zone is.
      int index = Arrays.binarySearch(ids, id);
      if (index < 0) {
        return null;
      }

      int byteOffset = byteOffsets[index];
      BufferIterator it = mappedFile.bigEndianIterator();
      it.skip(byteOffset);
      return it;
    }

    private void populateFallback() {
      version = "missing";
      zoneTab = "# Emergency fallback data.\n";
      ids = new String[] { "GMT" };
      byteOffsets = rawUtcOffsetsCache = new int[1];
    }

    /**
     * Loads the data file at the specified path. If the data is valid {@code true} will be
     * returned and the {@link TzData} instance can be used. If {@code false} is returned then the
     * TzData instance is left in a closed state and must be discarded.
     */
    private boolean loadData(String path) {
      try {
        mappedFile = MemoryMappedFile.mmapRO(path);
      } catch (ErrnoException errnoException) {
        return false;
      }
      try {
        readHeader();
        return true;
      } catch (Exception ex) {
        close();

        // Something's wrong with the file.
        // Log the problem and return false so we try the next choice.
        System.logE(TZDATA_FILE + " file \"" + path + "\" was present but invalid!", ex);
        return false;
      }
    }

    private void readHeader() throws IOException {
      // byte[12] tzdata_version  -- "tzdata2012f\0"
      // int index_offset
      // int data_offset
      // int zonetab_offset
      BufferIterator it = mappedFile.bigEndianIterator();

      try {
        byte[] tzdata_version = new byte[12];
        it.readByteArray(tzdata_version, 0, tzdata_version.length);
        String magic = new String(tzdata_version, 0, 6, StandardCharsets.US_ASCII);
        if (!magic.equals("tzdata") || tzdata_version[11] != 0) {
          throw new IOException("bad tzdata magic: " + Arrays.toString(tzdata_version));
        }
        version = new String(tzdata_version, 6, 5, StandardCharsets.US_ASCII);

        final int fileSize = mappedFile.size();
        int index_offset = it.readInt();
        validateOffset(index_offset, fileSize);
        int data_offset = it.readInt();
        validateOffset(data_offset, fileSize);
        int zonetab_offset = it.readInt();
        validateOffset(zonetab_offset, fileSize);

        if (index_offset >= data_offset || data_offset >= zonetab_offset) {
          throw new IOException("Invalid offset: index_offset=" + index_offset
                  + ", data_offset=" + data_offset + ", zonetab_offset=" + zonetab_offset
                  + ", fileSize=" + fileSize);
        }

        readIndex(it, index_offset, data_offset);
        readZoneTab(it, zonetab_offset, fileSize - zonetab_offset);
      } catch (IndexOutOfBoundsException e) {
        throw new IOException("Invalid read from data file", e);
      }
    }

    private static void validateOffset(int offset, int size) throws IOException {
      if (offset < 0 || offset >= size) {
        throw new IOException("Invalid offset=" + offset + ", size=" + size);
      }
    }

    private void readZoneTab(BufferIterator it, int zoneTabOffset, int zoneTabSize) {
      byte[] bytes = new byte[zoneTabSize];
      it.seek(zoneTabOffset);
      it.readByteArray(bytes, 0, bytes.length);
      zoneTab = new String(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
    }

    private void readIndex(BufferIterator it, int indexOffset, int dataOffset) throws IOException {
      it.seek(indexOffset);

      byte[] idBytes = new byte[SIZEOF_TZNAME];
      int indexSize = (dataOffset - indexOffset);
      if (indexSize % SIZEOF_INDEX_ENTRY != 0) {
        throw new IOException("Index size is not divisible by " + SIZEOF_INDEX_ENTRY
                + ", indexSize=" + indexSize);
      }
      int entryCount = indexSize / SIZEOF_INDEX_ENTRY;

      byteOffsets = new int[entryCount];
      ids = new String[entryCount];

      for (int i = 0; i < entryCount; i++) {
        // Read the fixed length timezone ID.
        it.readByteArray(idBytes, 0, idBytes.length);

        // Read the offset into the file where the data for ID can be found.
        byteOffsets[i] = it.readInt();
        byteOffsets[i] += dataOffset;

        int length = it.readInt();
        if (length < 44) {
          throw new IOException("length in index file < sizeof(tzhead)");
        }
        it.skip(4); // Skip the unused 4 bytes that used to be the raw offset.

        // Calculate the true length of the ID.
        int len = 0;
        while (idBytes[len] != 0 && len < idBytes.length) {
          len++;
        }
        if (len == 0) {
          throw new IOException("Invalid ID at index=" + i);
        }
        ids[i] = new String(idBytes, 0, len, StandardCharsets.US_ASCII);
        if (i > 0) {
          if (ids[i].compareTo(ids[i - 1]) <= 0) {
            throw new IOException("Index not sorted or contains multiple entries with the same ID"
                    + ", index=" + i + ", ids[i]=" + ids[i] + ", ids[i - 1]=" + ids[i - 1]);
          }
        }
      }
    }

    @libcore.api.CorePlatformApi
    public void validate() throws IOException {
      checkNotClosed();
      // Validate the data in the tzdata file by loading each and every zone.
      for (String id : getAvailableIDs()) {
        ZoneInfo zoneInfo = makeTimeZoneUncached(id);
        if (zoneInfo == null) {
          throw new IOException("Unable to find data for ID=" + id);
        }
      }
    }

    ZoneInfo makeTimeZoneUncached(String id) throws IOException {
      BufferIterator it = getBufferIterator(id);
      if (it == null) {
        return null;
      }

      return ZoneInfo.readTimeZone(id, it, System.currentTimeMillis());
    }

    public String[] getAvailableIDs() {
      checkNotClosed();
      return ids.clone();
    }

    public String[] getAvailableIDs(int rawUtcOffset) {
      checkNotClosed();
      List<String> matches = new ArrayList<String>();
      int[] rawUtcOffsets = getRawUtcOffsets();
      for (int i = 0; i < rawUtcOffsets.length; ++i) {
        if (rawUtcOffsets[i] == rawUtcOffset) {
          matches.add(ids[i]);
        }
      }
      return matches.toArray(new String[matches.size()]);
    }

    private synchronized int[] getRawUtcOffsets() {
      if (rawUtcOffsetsCache != null) {
        return rawUtcOffsetsCache;
      }
      rawUtcOffsetsCache = new int[ids.length];
      for (int i = 0; i < ids.length; ++i) {
        // This creates a TimeZone, which is quite expensive. Hence the cache.
        // Note that icu4c does the same (without the cache), so if you're
        // switching this code over to icu4j you should check its performance.
        // Telephony shouldn't care, but someone converting a bunch of calendar
        // events might.
        rawUtcOffsetsCache[i] = cache.get(ids[i]).getRawOffset();
      }
      return rawUtcOffsetsCache;
    }

    @libcore.api.CorePlatformApi
    public String getVersion() {
      checkNotClosed();
      return version;
    }

    public String getZoneTab() {
      checkNotClosed();
      return zoneTab;
    }

    @libcore.api.CorePlatformApi
    public ZoneInfo makeTimeZone(String id) throws IOException {
      checkNotClosed();
      ZoneInfo zoneInfo = cache.get(id);
      // The object from the cache is cloned because TimeZone / ZoneInfo are mutable.
      return zoneInfo == null ? null : (ZoneInfo) zoneInfo.clone();
    }

    @libcore.api.CorePlatformApi
    public boolean hasTimeZone(String id) throws IOException {
      checkNotClosed();
      return cache.get(id) != null;
    }

    public void close() {
      if (!closed) {
        closed = true;

        // Clear state that takes up appreciable heap.
        ids = null;
        byteOffsets = null;
        rawUtcOffsetsCache = null;
        cache.evictAll();

        // Remove the mapped file (if needed).
        if (mappedFile != null) {
          try {
            mappedFile.close();
          } catch (ErrnoException ignored) {
          }
          mappedFile = null;
        }
      }
    }

    private void checkNotClosed() throws IllegalStateException {
      if (closed) {
        throw new IllegalStateException("TzData is closed");
      }
    }

    @Override protected void finalize() throws Throwable {
      try {
        close();
      } finally {
        super.finalize();
      }
    }
  }

  private ZoneInfoDB() {
  }

  @libcore.api.CorePlatformApi
  public static TzData getInstance() {
    return DATA;
  }
}
