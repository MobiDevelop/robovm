/*
 * Copyright (C) 2015 RoboVM AB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 */
#include "jni.h"
#include "archive.h"
#include "archive_entry.h"

jboolean compressFile(const char* fileName, void* data, jint len) {
    struct archive* archive = NULL;
    struct archive_entry* entry = NULL;
    
    // initialize archive disk writer
    archive = archive_write_disk_new();
    if(archive == NULL) return JNI_FALSE;
    if(archive_write_disk_set_standard_lookup(archive) != ARCHIVE_OK) goto error;
    if(archive_write_disk_set_options(archive, ARCHIVE_EXTRACT_SECURE_SYMLINKS | ARCHIVE_EXTRACT_SECURE_NODOTDOT | ARCHIVE_EXTRACT_HFS_COMPRESSION_FORCED) != ARCHIVE_OK)
        goto error;
    
    // create an entry to be written to disk
    entry = archive_entry_new();
    if(entry == NULL) goto error;
        
    archive_entry_set_pathname(entry, fileName);
    archive_entry_set_filetype(entry, AE_IFREG); // Note 1
    archive_entry_set_size(entry, len);  // Note 2
    archive_entry_set_mode(entry, AE_IFREG | 0644);
    if(archive_write_header(archive, entry) != ARCHIVE_OK) goto error;
    if(archive_write_data(archive, data, len) != len) goto error;
    if(archive_write_finish_entry(archive) != ARCHIVE_OK) goto error;
    archive_write_free(archive);
    archive_entry_free(entry);
    return JNI_TRUE;
error:
    if(archive) archive_write_free(archive);
    if(entry) archive_entry_free(entry);
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_org_robovm_compiler_util_io_HfsCompressor_compressNative
(JNIEnv *env, jclass clazz, jstring fileNameJava, jbyteArray dataJava, jint len) {
    const char *fileName = (*env)->GetStringUTFChars(env, fileNameJava, 0);
    void* data = (*env)->GetPrimitiveArrayCritical(env, dataJava, 0);
    
    jboolean result = compressFile(fileName, data, len);
    
    (*env)->ReleasePrimitiveArrayCritical(env, dataJava, data, 0);
    (*env)->ReleaseStringUTFChars(env, fileNameJava, fileName);
    return result;
}
