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

package com.khulnasoft.data;

import java.sql.*;

import com.khulnasoft.data.jdbc.*;
import com.khulnasoft.util.*;

/**
 * A listener interface for monitoring the activity of JdbcConnector
 * objects.  Note that this is a simple interface presently used to look
 * for exceptions.
 *   <p>
 * For consideration in the future: We may at some point reconcile
 * DatabaseConnector and JdbcConnector (DatabaseConnector at a remote time
 * in history had multiple implementations).  But for now, we'll leave this
 * as-is.
 */
public interface DatabaseConnectionListener
  extends Configurable
{

  /**
   * Instructions.
   */
  int      INSTRUCT_DO_NOTHING     = 0;       // Listener instructs to retry query.
  int      INSTRUCT_RETRY          = 5000;    // Listener instructs to do nothing.

  /**
   * Exception occurred during runQuery() in non-safe mode.
   */
  int exceptionInRunQuery(SQLException exc, JdbcConnector conn);

  /**
   * Exception occurred during runUpdateQuery() in non-safe mode.
   */
  int exceptionInRunUpdateQuery(SQLException exc, JdbcConnector conn);

  /**
   * Exception occurred during executeBatch() in non-safe mode.
   */
  int exceptionInExecuteBatch(SQLException exc, JdbcConnector conn);
  
  /**
   * A query is starting.
   */
  void queryStarting();
 
  /**
   * A query is completing.
   */
  void queryCompleting();
  
}