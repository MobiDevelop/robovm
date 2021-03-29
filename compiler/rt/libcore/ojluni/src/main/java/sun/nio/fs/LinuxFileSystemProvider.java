/*
 * Copyright (c) 2008, 2012, Oracle and/or its affiliates. All rights reserved.
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

package sun.nio.fs;

import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.file.spi.FileTypeDetector;
import java.io.IOException;

/**
 * Linux implementation of FileSystemProvider
 */

public class LinuxFileSystemProvider extends UnixFileSystemProvider {
    public LinuxFileSystemProvider() {
        super();
    }

    @Override
    LinuxFileSystem newFileSystem(String dir) {
        return new LinuxFileSystem(this, dir);
    }

    @Override
    LinuxFileStore getFileStore(UnixPath path) throws IOException {
        // Android-changed: Complete information about file systems is neither available to regular
        // apps nor the system server due to SELinux policies.
        // return new LinuxFileStore(path);

        throw new SecurityException("getFileStore");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends FileAttributeView> V getFileAttributeView(Path obj,
                                                                Class<V> type,
                                                                LinkOption... options)
    {
        // Android-changed: Delegate to UnixFileSystemProvider, remove support for "dos" and
        // "user" file attribute views which are not supported.
        /*
        if (type == DosFileAttributeView.class) {
            return (V) new LinuxDosFileAttributeView(UnixPath.toUnixPath(obj),
                                                     Util.followLinks(options));
        }
        if (type == UserDefinedFileAttributeView.class) {
            return (V) new LinuxUserDefinedFileAttributeView(UnixPath.toUnixPath(obj),
                                                             Util.followLinks(options));
        }
        */
        return super.getFileAttributeView(obj, type, options);
    }

    @Override
    public DynamicFileAttributeView getFileAttributeView(Path obj,
                                                         String name,
                                                         LinkOption... options)
    {
        // Android-changed: Delegate to UnixFileSystemProvider, remove support for "dos" and
        // "user" file attribute views which are not supported.
        /*
        if (name.equals("dos")) {
            return new LinuxDosFileAttributeView(UnixPath.toUnixPath(obj),
                                                 Util.followLinks(options));
        }
        if (name.equals("user")) {
            return new LinuxUserDefinedFileAttributeView(UnixPath.toUnixPath(obj),
                                                         Util.followLinks(options));
        }
        */
        return super.getFileAttributeView(obj, name, options);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends BasicFileAttributes> A readAttributes(Path file,
                                                            Class<A> type,
                                                            LinkOption... options)
        throws IOException
    {
        // Android-changed: Delegate to UnixFileSystemProvider, remove support for "dos" and
        // "user" file attribute views which are not supported.
        /*
        if (type == DosFileAttributes.class) {
            DosFileAttributeView view =
                getFileAttributeView(file, DosFileAttributeView.class, options);
            return (A) view.readAttributes();
        } else {
            return super.readAttributes(file, type, options);
        }
        */
        return super.readAttributes(file, type, options);
    }

    @Override
    FileTypeDetector getFileTypeDetector() {
        // Android-changed: As libgio & libmagic is not available, GnomeFileTypeDetector and
        // MagicFileTypeDetector have been removed. MimeTypeFileDetector detects file type based
        // on the file extension, which may give false results.
        /*
        Path userMimeTypes = Paths.get(AccessController.doPrivileged(
            new GetPropertyAction("user.home")), ".mime.types");
        Path etcMimeTypes = Paths.get("/etc/mime.types");

        return chain(new GnomeFileTypeDetector(),
                     new MimeTypesFileTypeDetector(userMimeTypes),
                     new MimeTypesFileTypeDetector(etcMimeTypes),
                     new MagicFileTypeDetector());
        */
        return new MimeTypesFileTypeDetector();
    }
}
