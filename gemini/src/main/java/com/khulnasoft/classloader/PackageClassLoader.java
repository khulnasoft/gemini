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
package com.khulnasoft.classloader;

import java.net.*;
import java.util.*;

import org.reflections.*;
import org.reflections.scanners.*;
import org.reflections.util.*;

import com.khulnasoft.*;
import com.khulnasoft.gemini.*;

/**
 * Loads classes from a given package.
 */
public final class PackageClassLoader 
{

  /**
   * Placeholder comment.
   */
  public static Reflections getReflectionClassLoader(String packageName)
  {
    final ConfigurationBuilder configBuild = new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage(packageName))
        .setScanners(
            new TypeAnnotationsScanner(), 
            new SubTypesScanner(), 
            new MethodAnnotationsScanner()
        );
    
    return new Reflections(configBuild);
  }
  
  /**
   * Placeholder comment.
   */
  public static Reflections getReflectionClassLoader(KhulnaSoftApplication app)
  {
    final Collection<URL> urls = new ArrayList<>();
    
    // Add the gemini jars
    for (URL url : ClasspathHelper.forClassLoader(
        GeminiApplication.class.getClassLoader()))
    {
      if (url.toString().contains(GeminiConstants.GEMINI_NAME))
      {
        urls.add(url);
      }
    }
    
    // Add the app class files (could be in a jar)
    for (URL url : ClasspathHelper
        .forPackage(app.getClass().getPackage().getName()))
    {
      urls.add(url);
    }
    
    final ConfigurationBuilder configBuild = new ConfigurationBuilder()
      .setUrls(urls)
      .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner());
    
    return new Reflections(configBuild);
  }
  
  /**
   * Hide the constructor.
   */
  private PackageClassLoader()
  {
    // Does nothing.
  }
  
}