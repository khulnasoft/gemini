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

package com.khulnasoft;

import java.util.*;

import com.khulnasoft.asynchronous.*;
import com.khulnasoft.helper.*;
import com.khulnasoft.scheduler.*;
import com.khulnasoft.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base for applications' main classes.  Typically, an application that uses
 * this framework will subclass either KhulnaSoftApplication (for console
 * applications) or GeminiApplication (for web applications).  The resulting 
 * subclasses act as a central hub of references to application modules, such
 * as the log, scheduler, request dispatcher, templater, and so on.
 */
public class KhulnaSoftApplication
{
  //
  // Member variables.
  //

  private final Version                  version;
  private final Logger                   log = LoggerFactory.getLogger(getClass());
  private final Scheduler                scheduler;
  private final List<Asynchronous>       asynchronousRscs;
  private final List<DeferredStartAsynchronous> deferredRscs;

  //
  // Member methods.
  //

  /**
   * Constructor.  Overload to provide custom references.
   */
  public KhulnaSoftApplication()
  {
    // Create a Collection for the asynchronous resources.
    this.asynchronousRscs = new ArrayList<>();
    this.deferredRscs     = new ArrayList<>();
    this.version          = constructVersion();
    this.scheduler        = constructScheduler();
  }

  /**
   * Constructs a Version reference.  Overload to return a custom object.
   */
  protected Version constructVersion()
  {
    return new Version();
  }
  
  /**
   * Construct a Scheduler.  Schedule events at construction time by 
   * overloading this method.
   */
  protected Scheduler constructScheduler()
  {
    return new Scheduler(this);
  }

  /**
   * Gets a Version object for this application.  The Version holds items
   * such as the name and version number of the application.
   */
  public Version getVersion()
  {
    return version;
  }
  
  /**
   * Gets the main application scheduler.  If none exists it will be created.
   * If no scheduler is desired for the application, do not call this method.
   */
  public Scheduler getScheduler()
  {
    return scheduler;
  }

  /**
   * Adds a DeferredStartAsynchronous resource for this application to
   * manage.  When the application is started or stopped, the resource will
   * be started and stopped as well.  Deferred resources are started 
   * approximately 2 seconds after the application starts and will <b>not</b>
   * block the start of the application.
   */
  public void addAsynchronous(DeferredStartAsynchronous async)
  {
    // Only add if we don't already have it in the list.
    if (!deferredRscs.contains(async))
    {
      deferredRscs.add(async);
    }
  }
  
  /**
   * Adds an Asynchronous resource for this application to manage.  When the
   * application is started or stopped, the resource will be started and 
   * stopped as well.
   *   <p>
   * When designing an Asynchronous resource, keep in mind that the 
   * application's start-up will be blocked until the call to all resources'
   * Asynchronous.begin() methods return.
   */
  public void addAsynchronous(Asynchronous async)
  {
    // Only add if we don't already have it in the list.
    if (!asynchronousRscs.contains(async))
    {
      asynchronousRscs.add(async);
    }
  }

  /**
   * Removes an Asynchronous resource.
   */
  public void removeAsynchronous(Asynchronous async)
  {
    asynchronousRscs.remove(async);
  }

  /**
   * Starts the Asynchronous resources.
   */
  public void startAsynchronousResources()
  {
    // Start the async resources in the collection.
    for (Asynchronous async : asynchronousRscs)
    {
      // Begin within this thread.
      async.begin();
    }
    
    // Start a deferred asynchronous resources on separate threads.
    if (!deferredRscs.isEmpty())
    {
      final DeferredAsynchronousStarter thread = 
          new DeferredAsynchronousStarter(deferredRscs);
      thread.start();
    }
  }
  
  /**
   * Thread that runs the deferred Asynchronous resources.
   */
  static final class DeferredAsynchronousStarter
    extends Thread
  {
    private final List<DeferredStartAsynchronous> deferred; 
    
    private DeferredAsynchronousStarter(List<DeferredStartAsynchronous> deferred)
    {
      super("Deferred Asynchronous Object Starter");
      this.deferred = deferred;
    }
    
    @Override
    public void run()
    {
      // Sleep for 2 seconds.
      ThreadHelper.sleep(2 * UtilityConstants.SECOND);
      
      // Start the asynchronous resources.
      for (Asynchronous async : deferred)
      {
        async.begin();
      }
    }
  }
  
  /**
   * Stops the Asynchronous resources.
   */
  public void stopAsynchronousResources()
  {
    // Stop the asynchronous resources.
    for (Asynchronous async : asynchronousRscs)
    {
      async.end();
    }
    
    // Stop the deferred asynchronous resources.
    for (DeferredStartAsynchronous async : deferredRscs)
    {
      async.end();
    }
  }

}   // End KhulnaSoftApplication.
