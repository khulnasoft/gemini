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
package com.khulnasoft.data.annotation;

import java.lang.annotation.*;

/**
 * This annotation signals an entity class that will be registered with
 * the EntityStore. Using this annotation is equivalent to calling
 * <p><pre>
 *   register(EntityGroup.of(Foo.class)
 *     .table("Foo")
 *     .id("FooId")
 *     .comparable("getName"));
 * </pre>
 * <p>
 * Not all of the Builder options are available through this annotation because 
 * we are limited in what types of data we can capture. If you need to use 
 * maker, constructorArgs, where, or comparator (with a Comparator object) methods 
 * of Builder. Then do not use this annotation, and instead register the EntityGroup 
 * yourself in the EntityStore.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity
{
  /**
   * The database table that stores these entities.
   *
   * @return the database table that stores these entities
   * @see com.khulnasoft.cache.CacheGroup.Builder#table(String)
   */
  String table() default "";

  /**
   * The name of the auto-incrementing identity column in the database table for
   * these entities.
   *
   * @return the name of the auto-incrementing identity column in the database table for
   *         these entities
   * @see com.khulnasoft.cache.CacheGroup.Builder#id(String)
   */
  String id() default "";

  /**
   * The name of the method to invoke for comparison between these entities.
   *
   * @return the name of the method to invoke for comparison between these
   *         entities
   * @see com.khulnasoft.cache.CacheGroup.Builder#comparator(String)
   */
  String comparator() default "";
}
