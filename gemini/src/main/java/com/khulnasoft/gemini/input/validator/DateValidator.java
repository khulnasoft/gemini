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
package com.khulnasoft.gemini.input.validator;

import java.text.*;
import java.util.*;

import com.khulnasoft.gemini.input.*;
import com.khulnasoft.util.*;

/**
 * Validates that the user-provided String is a valid date, using a provided
 * SimpleDateFormat.
 */
public class DateValidator
  extends    ElementValidator
{

  //
  // Variables.
  //
  
  private final SimpleDateFormat formatter;
  private final LongRange permittedTimes;
  
  //
  // Methods.
  //
  
  /**
   * Constructor.
   */
  public DateValidator(
      String elementName, 
      SimpleDateFormat formatter,
      LongRange permittedTimes)
  {
    super(elementName);
    this.formatter = formatter;
    this.permittedTimes = permittedTimes;
    message(elementName + " is not a properly formatted date.");
  }
  
  /**
   * Constructor, allowing any time to be specified.
   */
  public DateValidator(
      String elementName, 
      SimpleDateFormat formatter)
  {
    this(elementName, formatter, null);
  }
  
  @Override
  public void process(final Input input)
  {
    final String userValue = getUserValue(input);
    if (userValue.length() > 0)
    {
      try
      {
        final Date temp = formatter.parse(userValue);
        
        if (permittedTimes != null)
        {
          if (!permittedTimes.contains(temp.getTime()))
          {
            input.addError(getElementName(), getElementName() 
                + " is not an acceptable date.");
          }
        }
      }
      catch (ParseException pexc)
      { 
        input.addError(getElementName(), message);
      }
    }
  }
  
}
