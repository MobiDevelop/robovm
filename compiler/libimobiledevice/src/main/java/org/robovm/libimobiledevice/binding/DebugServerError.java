/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.4
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.robovm.libimobiledevice.binding;

public enum DebugServerError {
  DEBUGSERVER_E_SUCCESS(0),
  DEBUGSERVER_E_INVALID_ARG(-1),
  DEBUGSERVER_E_MUX_ERROR(-2),
  DEBUGSERVER_E_SSL_ERROR(-3),
  DEBUGSERVER_E_RESPONSE_ERROR(-4),
  DEBUGSERVER_E_UNKNOWN_ERROR(-256);

  public final int swigValue() {
    return swigValue;
  }

  public static DebugServerError swigToEnum(int swigValue) {
    DebugServerError[] swigValues = DebugServerError.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (DebugServerError swigEnum : swigValues)
      if (swigEnum.swigValue == swigValue)
        return swigEnum;
    throw new IllegalArgumentException("No enum " + DebugServerError.class + " with value " + swigValue);
  }

  @SuppressWarnings("unused")
  private DebugServerError() {
    this.swigValue = SwigNext.next++;
  }

  @SuppressWarnings("unused")
  private DebugServerError(int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue+1;
  }

  @SuppressWarnings("unused")
  private DebugServerError(DebugServerError swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue+1;
  }

  private final int swigValue;

  private static class SwigNext {
    private static int next = 0;
  }
}

