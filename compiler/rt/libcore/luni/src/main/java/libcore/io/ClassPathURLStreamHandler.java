/*
 * Copyright (C) 2015 The Android Open Source Project
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

package libcore.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import sun.net.www.ParseUtil;
import sun.net.www.protocol.jar.Handler;

/**
 * A {@link URLStreamHandler} for a specific class path {@link JarFile}. This class avoids the need
 * to open a jar file multiple times to read resources if the jar file can be held open. The
 * {@link URLConnection} objects created are a subclass of {@link JarURLConnection}.
 *
 * <p>Use {@link #getEntryUrlOrNull(String)} to obtain a URL backed by this stream handler.
 */
public class ClassPathURLStreamHandler extends Handler {
  private final String fileUri;
  private final JarFile jarFile;

  public ClassPathURLStreamHandler(String jarFileName) throws IOException {
    jarFile = new JarFile(jarFileName);

    // File.toURI() is compliant with RFC 1738 in always creating absolute path names. If we
    // construct the URL by concatenating strings, we might end up with illegal URLs for relative
    // names.
    this.fileUri = new File(jarFileName).toURI().toString();
  }

  /**
   * Returns a URL backed by this stream handler for the named resource, or {@code null} if the
   * entry cannot be found under the exact name presented.
   */
  public URL getEntryUrlOrNull(String entryName) {
    if (jarFile.getEntry(entryName) != null) {
      try {
        // Encode the path to ensure that any special characters like # survive their trip through
        // the URL. Entry names must use / as the path separator.
        String encodedName = ParseUtil.encodePath(entryName, false);
        return new URL("jar", null, -1, fileUri + "!/" + encodedName, this);
      } catch (MalformedURLException e) {
        throw new RuntimeException("Invalid entry name", e);
      }
    }
    return null;
  }

  /**
   * Returns true if an entry with the specified name exists and is stored (not compressed),
   * and false otherwise.
   */
  public boolean isEntryStored(String entryName) {
    ZipEntry entry = jarFile.getEntry(entryName);
    return entry != null && entry.getMethod() == ZipEntry.STORED;
  }

  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    return new ClassPathURLConnection(url);
  }

  /** Used from tests to indicate this stream handler is finished with. */
  public void close() throws IOException {
    jarFile.close();
  }

  private class ClassPathURLConnection extends JarURLConnection {
    // The JarFile instance can be shared across URLConnections and should not be closed when it is:
    //
    // Sharing occurs if getUseCaches() is true when connect() is called (which can take place
    // implicitly). useCachedJarFile records the state of sharing at connect() time.
    // useCachedJarFile == true is the common case. If developers call getJarFile().close() when
    // sharing is enabled then it will affect other users (current and future) of the shared
    // JarFile.
    //
    // Developers could call ClassLoader.findResource().openConnection() to get a URLConnection and
    // then call setUseCaches(false) before connect() to prevent sharing. The developer must then
    // call getJarFile().close() or close() on the inputStream from getInputStream() will do it
    // automatically. This is likely to be an extremely rare case.
    //
    // Most developers are not expecting to deal with the lifecycle of the underlying JarFile object
    // at all. The presence of the getJarFile() method and setUseCaches() forces us to consider /
    // handle it.
    private JarFile connectionJarFile;

    private ZipEntry jarEntry;
    private InputStream jarInput;
    private boolean closed;

    /**
     * Indicates the behavior of the {@link #jarFile}. If true, the reference is shared and should
     * not be closed. If false, it must be closed.
     */
    private boolean useCachedJarFile;


    public ClassPathURLConnection(URL url) throws MalformedURLException {
      super(url);
    }

    @Override
    public void connect() throws IOException {
      if (!connected) {
        this.jarEntry = jarFile.getEntry(getEntryName());
        if (jarEntry == null) {
          throw new FileNotFoundException(
              "URL does not correspond to an entry in the zip file. URL=" + url
              + ", zipfile=" + jarFile.getName());
        }
        useCachedJarFile = getUseCaches();
        connected = true;
      }
    }

    @Override
    public JarFile getJarFile() throws IOException {
      connect();

      // We do cache in the surrounding class if useCachedJarFile is true to
      // preserve garbage collection semantics and to avoid leak warnings.
      if (useCachedJarFile) {
        connectionJarFile = jarFile;
      } else {
        connectionJarFile = new JarFile(jarFile.getName());
      }
      return connectionJarFile;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      if (closed) {
        throw new IllegalStateException("JarURLConnection InputStream has been closed");
      }
      connect();
      if (jarInput != null) {
        return jarInput;
      }
      return jarInput = new FilterInputStream(jarFile.getInputStream(jarEntry)) {
        @Override
        public void close() throws IOException {
          super.close();
          // If the jar file is not cached then closing the input stream will close the
          // URLConnection and any JarFile returned from getJarFile(). If the jar file is cached
          // we must not close it because it will affect other URLConnections.
          if (connectionJarFile != null && !useCachedJarFile) {
            connectionJarFile.close();
            closed = true;
          }
        }
      };
    }

    /**
     * Returns the content type of the entry based on the name of the entry. Returns
     * non-null results ("content/unknown" for unknown types).
     *
     * @return the content type
     */
    @Override
    public String getContentType() {
      String cType = guessContentTypeFromName(getEntryName());
      if (cType == null) {
        cType = "content/unknown";
      }
      return cType;
    }

    @Override
    public int getContentLength() {
      try {
        connect();
        return (int) getJarEntry().getSize();
      } catch (IOException e) {
        // Ignored
      }
      return -1;
    }
  }
}
