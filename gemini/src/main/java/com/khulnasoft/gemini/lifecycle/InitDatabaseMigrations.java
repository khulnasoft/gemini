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
package com.khulnasoft.gemini.lifecycle;

import java.sql.*;
import java.util.*;

import org.slf4j.*;

import com.khulnasoft.data.*;
import com.khulnasoft.gemini.*;
import com.khulnasoft.gemini.data.*;
import com.khulnasoft.helper.*;
import com.khulnasoft.util.*;

/**
 * Applies any pending database migrations.
 */
public class InitDatabaseMigrations implements InitializationTask, Configurable
{
  private Logger  log     = LoggerFactory.getLogger(getClass());
  private boolean enabled = false;
  private boolean applyMigrations = false;
  private boolean abortStartupIfPending = true;
  private GeminiApplication app;

  /**
   * Constructor.
   */
  public InitDatabaseMigrations(GeminiApplication app)
  {
    this.app = app;
    app.getConfigurator().addConfigurable(this);
  }

  @Override
  public void taskInitialize(GeminiApplication app)
  {
    if (!enabled)
    {
      log.info("Database migrations disabled.");
      return;
    }

    final ConnectorFactory cf = app.getConnectorFactory();
    if (!cf.isEnabled())
    {
      log.info("ConnectorFactor not enabled. Skipping database migrations.");
      return;
    }

    final DatabaseMigrator migrator = app.getDatabaseMigrator();
    if (migrator == null)
    {
      log.info("DatabaseMigrator unavailable. Skipping database migrations.");
      return;
    }

    if (applyMigrations)
    {
      log.info("Applying database migrations.");

      try (ConnectionMonitor monitor = cf.getConnectionMonitor())
      {
        // Start the migration
        int migrationsApplied = migrator.migrate(monitor);
        log.info("Database migrations applied: {}", migrationsApplied);
      }
      catch (SQLException e)
      {
        log.error("Database migrations caught exception ", e);
      }
    }

    if (abortStartupIfPending)
    {
      log.info("Checking for pending migrations...");

      try (ConnectionMonitor monitor = cf.getConnectionMonitor())
      {
        // Start the migration
        List<String> pendingMigrations = migrator.listPendingMigrations(monitor);
        if (CollectionHelper.isNonEmpty(pendingMigrations))
        {
          log.error("Aborting application startup because of these pending migrations:");
          for (String s : pendingMigrations)
          {
            log.error("Pending migration: {}", s);
          }
          throw new GeminiInitializationError("There are pending database migrations; cannot start.");
        }
        else
        {
          log.info("There are no pending migrations.");
        }
      }
      catch (SQLException e)
      {
        log.error("Database migrations caught exception ", e);
      }
    }
  }

  @Override
  public void configure(EnhancedProperties props)
  {
    enabled = props.getBoolean("Initialization.DbMigrations.Enabled", false);
    // Default to applying migrations everywhere except production.
    applyMigrations = props.getBoolean("Initialization.DbMigrations.ApplyMigrations", !app.getVersion().isProduction());
    abortStartupIfPending = props.getBoolean("Initialization.DbMigrations.AbortStartupIfPending", true);
  }

}
