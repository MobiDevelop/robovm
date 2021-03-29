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

package dalvik.system;

/**
 * An exception only used by the compiler to abort a transaction.
 *
 * @hide
 */
final class TransactionAbortError extends InternalError {
  /**
   * Constructs a new {@code TransactionAbortError} with its stack trace filled in.
   */
  private TransactionAbortError() {
  }

  /**
   * Constructs a new {@code TransactionAbortError} with its stack trace and detail
   * message filled in.
   *
   * @param detailMessage the detail message for the exception.
   */
  private TransactionAbortError(String detailMessage) {
      super(detailMessage);
  }

  /**
   * Constructs a new {@code TransactionAbortError} with detail message and cause
   * filled in.
   *
   * @param message the detail message for the exception.
   * @param cause the detail cause for the exception.
   */
  private TransactionAbortError(String message, Throwable cause) {
      super(message);
      initCause(cause);
  }

  /**
   * Constructs a new {@code TransactionAbortError} with its detail cause filled in.
   *
   * @param cause the detail cause for the exception.
   */
  private TransactionAbortError(Throwable cause) {
      this(cause == null ? null : cause.toString(), cause);
  }

}
