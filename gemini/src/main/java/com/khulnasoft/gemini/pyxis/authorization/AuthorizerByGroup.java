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
package com.khulnasoft.gemini.pyxis.authorization;

import com.khulnasoft.gemini.*;
import com.khulnasoft.gemini.pyxis.*;

/**
 * Authorize a user by matching membership in a user group specified at
 * construction time.
 */
public class AuthorizerByGroup
  implements Authorizer 
{
  
  private final int groupId;
  private final boolean allowAdministrators;
  
  /**
   * Constructor.  By default, administrators are not permitted.
   */
  public AuthorizerByGroup(int groupId, boolean allowAdministrators)
  {
    this.groupId = groupId;
    this.allowAdministrators = allowAdministrators;
  }
  
  /**
   * Constructor.  By default, administrators are not permitted.
   */
  public AuthorizerByGroup(int groupId)
  {
    this(groupId, false);
  }

  @Override
  public boolean isAuthorized(PyxisUser user, Context context) 
  {
    return (  (  (allowAdministrators)
              && (user.isAdministrator())
              )
           || (user.isMember(groupId))
           );
  }

}
