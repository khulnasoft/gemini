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
package com.khulnasoft.cache;

import java.sql.*;
import java.util.*;
import com.google.common.cache.*;
import com.google.common.collect.*;
import com.google.common.primitives.*;
import com.khulnasoft.data.*;
import com.khulnasoft.helper.*;
import com.khulnasoft.util.*;
import gnu.trove.iterator.*;
import gnu.trove.map.*;
import gnu.trove.map.hash.*;

/**
 * A least-recently-used style caching EntityGroup based on the Guava
 * library's LoadingCache.  The guts are provided by LoadingCache but the
 * usage semantics are similar to CacheGroup.  Note that methods that rely
 * on knowing the full set of entities such as list() and lowest() will
 * bypass the cache.  If your use-case calls for an LRU-style cache, you are
 * not likely to call these full-set methods anyway.
 */
public class LruCacheGroup<T extends Identifiable>
     extends EntityGroup<T> 
{

  private final LoadingCache<Long, T> objects;
  
  /**
   * Constructor.
   */
  protected LruCacheGroup(EntityStore controller, 
      Class<T> type,
      String table, 
      String id, 
      EntityMaker<T> maker,
      Comparator<? super T> comparator, 
      String where, 
      String[] whereArguments, 
      int size,
      boolean readOnly,
      boolean distribute) 
  {
    super(controller, type, table, id, maker, comparator, where, 
        whereArguments, readOnly, distribute);
    
    objects = CacheBuilder.newBuilder()
        .maximumSize(size)
        .build(new CacheLoader<Long, T>() {
          @Override public T load(Long identity) throws Exception {
            return getViaEntityGroup(identity);
          }
        });
  }

  /**
   * Creates a new {@link Builder}, which is used to construct an
   * {@link LruCacheGroup}.  Example usage:
   * 
   * <pre>
   * LruCacheGroup&lt;Foo&gt; = LruCacheGroup.of(Foo.class) // new Builder
   *     .table("foos") // modified Builder
   *     .id("fooID") // modified Builder
   *     .build(controller); // new LruCacheGroup
   * </pre>
   * 
   * <p>Note that a {@link EntityStore#register(EntityGroup.Builder)} method
   * exists, so in the common case where you only want to register the group and
   * don't care to retain your own reference to it, calling
   * {@code .build(controller)} is unnecessary.  For example:
   * 
   * <pre>
   * register(LruCacheGroup.of(Foo.class) // new Builder
   *     .table("foos") // modified Builder
   *     .id("fooID") // modified Builder
   * ); // the register method calls .build(controller) for us
   * </pre>
   * 
   * @param type The type of the entities.
   * @return A new {@link Builder}.
   */
  public static <T extends Identifiable> Builder<T> of(Class<T> type)
  {
    return new Builder<>(type);
  }
  
  @Override
  public T get(long id)
  {
    // Guava's design pattern is irksome: rather than allowing the 
    // underlying retrieval to return an indicator that no such object exists
    // (which is truly unexceptional), the implementation must capture a
    // stack trace and throw an exception to indicate that condition.  This
    // means a zero-value-added try/catch block is necessary here.
    try
    {
      return objects.get(id);
    }
    catch (Exception eexc)
    {
      return null;
    }
  }
  
  @Override
  public void reset()
  {
    objects.invalidateAll();
  }

  /**
   * Tries to satisfy first from the LRU cache, only querying the database if required.
   */
  @Override
  public TLongObjectMap<T> map(Collection<Long> ids) {
    if (CollectionHelper.isEmpty(ids)) {
      return new TLongObjectHashMap<>(0);
    }
    TLongObjectMap<T> toReturn = new TLongObjectHashMap<>(ids.size());
    // Fetch all the objects that are already cached.
    ImmutableMap<Long, T> present = this.objects.getAllPresent(ids);
    toReturn.putAll(present);
    if (ids.size() > present.size()) {
      // We need to fetch more to get all the requested IDs. This could be implemented with
      // this.objects.getAll(ids) instead, but that would result in a separate individual query for
      // each missing value. For performance reasons, we use EntityGroup.map() to get all missing
      // values in a single query.
      Set<Long> neededIds = new HashSet<>(ids);
      neededIds.removeAll(present.keySet());
      for (TLongObjectIterator<T> it = super.map(neededIds).iterator(); it.hasNext();) {
        it.advance();
        long key = it.key();
        T value = it.value();
        toReturn.put(key, value);
        // Cache our fetched items for use next time.
        this.objects.put(key, value);
      }
    }
    return toReturn;
  }

  @Override
  public T querySingle(String query, Object... arguments) throws SQLException {
    T object = super.querySingle(query, arguments);
    if (object != null) {
      // Store in the LRU cache.
      objects.put(object.getId(), object);
    }
    return object;
  }
  
  @Override
  public List<T> list()
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public TLongObjectMap<T> map()
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public int put(T object)
  {
    int rowsUpdated = super.put(object);
    objects.put(object.getId(), object);
    return rowsUpdated;
  }
  
  @Override
  public int putAll(Collection<T> objectsToPut)
  {
    int rowsUpdated = 0;
    for (T object : objectsToPut) 
    {
      int c = put(object);
      if (c > 0)
      {
        // Only accumulate positive values;
        rowsUpdated += c;
      }
    }
    return rowsUpdated;
  }
  
  @Override
  public void remove(long id)
  {
    super.remove(id);
    objects.invalidate(id);
  }
  
  @Override
  public void removeAll(Collection<Long> ids)
  {
    for (Long identity : ids)
    {
      remove(identity);
    }
  }
  
  /**
   * Gets the object using the underlying EntityGroup.
   */
  protected T getViaEntityGroup(long id)
    throws NoSuchEntityException
  {
    final T result = super.get(id);
    if (result == null)
    {
      throw new NoSuchEntityException(getType().getSimpleName() 
          + " id " + id + " not found.");
    }
    return result;
  }
  
  @SuppressWarnings("serial")
  public static class NoSuchEntityException extends Exception
  {
    public NoSuchEntityException(String message) 
    { 
      super(message); 
    }
  }
  
  @Override
  public void refresh(long... ids)
  {
    objects.invalidateAll(Longs.asList(ids));
  }
  
  @Override
  public String toString()
  {
    return "LruCacheGroup [" + name() + "; ro: " + this.readOnly() + "; distribute: " + this.distribute() + "]";
  }

  // 
  // Inner classes.
  // 

  /**
   * Creates new instances of {@code LruCacheGroup}.
   */
  public static class Builder<T extends Identifiable>
      extends EntityGroup.Builder<T>
  {
    /**
     * The default size limit is 10,000.
     */
    public static final int DEFAULT_SIZE = 10000;
    private int size = DEFAULT_SIZE;
    
    protected Builder(Class<T> type)
    {
      super(type);
      // LruCacheGroups default to true since they maintain a partial cache and need to both send
      // and receive cache updates.
      super.distribute = true;
    }
    
    @Override
    public LruCacheGroup<T> build(EntityStore controller)
    {
      if (controller == null)
      {
        throw new NullPointerException();
      }
      
      return new LruCacheGroup<>(
          controller,
          this.type,
          this.table,
          this.id,
          this.maker,
          this.comparator,
          this.where,
          this.whereArguments,
          this.size,
          this.readOnly,
          this.distribute);
    }

    @Override
    public Builder<T> table(String tableName)
    {
      super.table(tableName);
      return this;
    }

    @Override
    public Builder<T> id(String idFieldName)
    {
      super.id(idFieldName);
      return this;
    }
    
    @Override
    public Builder<T> readOnly()
    {
      super.readOnly();
      return this;
    }

    @Override
    public Builder<T> maker(EntityMaker<T> entityMaker)
    {
      super.maker(entityMaker);
      return this;
    }

    @Override
    public Builder<T> comparator(Comparator<? super T> entityComparator)
    {
      super.comparator(entityComparator);
      return this;
    }

    @Override
    public Builder<T> comparator(String methodName)
    {
      super.comparator(methodName); 
      return this;
    }

    @Override
    public Builder<T> where(String whereClause, String... arguments)
    {
      super.where(whereClause, arguments);
      return this;
    }

    @Override
    public Builder<T> constructorArgs(Object... arguments)
    {
      super.constructorArgs(arguments);
      return this;
    }
    
    public Builder<T> maximumSize(int newSize)
    {
      this.size = newSize;
      return this;
    }

  } // End Builder.

}