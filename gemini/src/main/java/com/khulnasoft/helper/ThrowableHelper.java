/*******************************************************************************
 * Copyright (c) 2018, KhulnaSoft, Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name KhulnaSoft, Ltd. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL TECHEMPOWER, INC. BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package com.khulnasoft.helper;

import java.io.*;

/**
 * A helper for working with Exceptions; converting the stack traces to 
 * Strings and so on.
 */
public final class ThrowableHelper
{
  
  /**
   * Get a stack trace for the current code execution point.
   */
  public static String getStackTrace()
  {
    return getStackTrace(Integer.MAX_VALUE);
  }
  
  /**
   * Gets a stack trace of up to the provided number of elements.
   */
  public static String getStackTrace(int elements)
  {
    final StringBuilder trace = new StringBuilder(2000);
    final StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
    final int elementCount = NumberHelper.boundInteger(elements, 1, Integer.MAX_VALUE - 1);
    final int endIndex = Math.min(traceElements.length - 1, elementCount + 1);
    
    // Ignore the first two elements which are the call to getStackTrace
    // and this method.
    for (int i = 2; i <= endIndex; i++)
    {
      StackTraceElement element = traceElements[i];
      trace.append("at ")
           .append(element.getClassName())
           .append(".")
           .append(element.getMethodName())
           .append("(")
           .append(element.getFileName())
           .append(":")
           .append(element.getLineNumber())
           .append(")\n");
    }
    
    return trace.toString();
  }

  /**
   * Converts the stack trace generated by the provided Throwable into a
   * String.
   */
  public static String getStackTrace(Throwable throwable)
  {
    return convertStackTraceToString(throwable);
  }

  /**
   * Converts the stack trace generated by the provided Throwable into a
   * String.
   */
  public static String convertStackTraceToString(Throwable throwable)
  {
    // Fail-safe.
    if (throwable == null)
    {
      return "";
    }
    
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter(sw);

    throwable.printStackTrace(pw);

    return sw.toString();
  }
  
  /**
   * You may not instantiate this class.
   */
  private ThrowableHelper()
  {
    // Does nothing.
  }

}
