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

package com.khulnasoft.gemini.email.inbound;

import com.khulnasoft.gemini.email.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DisplayEmailHandler simply displays an e-mail to the console when it is
 * handled.
 */
public class DisplayEmailHandler
  implements EmailHandler
{
  
  //
  // Member variables.
  //

  private final Logger log;

  //
  // Member methods.
  //

  /**
   * Constructor.
   */
  public DisplayEmailHandler(Logger log)
  {
    this.log = log;
  }

  public DisplayEmailHandler()
  {
    this.log = LoggerFactory.getLogger(getClass());
  }
  
  /**
   * Display the mail to the debug log.
   */
  @Override
  public boolean handleEmail(EmailPackage email)
  {
    this.log.info("From: {}", email.getAuthor());
    this.log.info("To  : {}", email.getRecipient());
    this.log.info("Subj: {}", email.getSubject());
    if (email.getAttachments() != null)
    {
      for (EmailAttachment attach : email.getAttachments())
      {
        this.log.info("Attc: {}", attach.getName());
      }
    }
    this.log.info("Body: {}", email.getTextBody());
    this.log.info("Html: {}", email.getHtmlBody());
    
    // Don't delete on our behalf.
    return false;
  }
  
  /**
   * toString.
   */
  @Override
  public String toString()
  {
    return "DisplayEmailHandler - displays inbound emails to the debug log";
  }

}
