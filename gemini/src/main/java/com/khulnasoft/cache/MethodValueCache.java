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

import gnu.trove.map.*;
import gnu.trove.map.hash.*;
import gnu.trove.set.*;
import gnu.trove.set.hash.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.locks.*;

import com.khulnasoft.util.*;

/**
 * Caches the return values of methods for one type of cached object.  Allows 
 * you to quickly retrieve objects by any field value.  
 * <p>
 * It is expected that an application using this will notify it when a cached 
 * object is modified.  EntityStore, if configured to use method value 
 * caches, will take care of this assuming every modification of an object in 
 * the cache is persisted to the database.
 * <p>
 * The cache is populated using lazy loading on a per method basis.  If the
 * {@code getObjects} method is called on method {@code getFoo}, the cache
 * for only {@code getFoo} gets populated.  If {@code getObjects} is never
 * called on method {@code getBar}, the cache for {@code getBar} is never
 * populated.
 * 
 * @param <T> The type of objects whose values are being cached.
 */
public class MethodValueCache<T extends Identifiable>
{
  // Utility objects.
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Map<String, Map<Object, TLongSet>> mapMethodNameToValueToIds
      = new HashMap<>();
  private final Map<String, TLongObjectMap<Object>> mapMethodNameToIdToValue
      = new HashMap<>();
  private final Map<String, Method> mapMethodNameToMethod 
      = new HashMap<>();
  private boolean loaded = false;
  
  // Assigned in the constructor.
  private final EntityStore cache;
  private final Class<T> type;
  
  /**
   * Creates a new method value cache.
   * 
   * @param cache The cache that stores the objects.
   * @param type The type of objects whose values are being cached.
   */
  public MethodValueCache(EntityStore cache, Class<T> type)
  {
    this.cache = cache;
    this.type = type;
  }
  
  /**
   * Removes the values for the given entity from this cache.
   * 
   * @param id The id of the entity to be removed.
   */
  public void delete(long id)
  {
    // If the cache hasn't been loaded, there is nothing to delete.
    if (!this.loaded)
    {
      return;
    }

    this.lock.writeLock().lock();
    try
    {
      for (String methodName : this.mapMethodNameToMethod.keySet())
      {
        TLongObjectMap<Object> mapIdToValue = this.mapMethodNameToIdToValue.get(methodName);
        Map<Object, TLongSet> mapValueToIds = this.mapMethodNameToValueToIds.get(methodName);
        
        Object value = mapIdToValue.get(id);

        TLongSet ids = mapValueToIds.get(value);
        if (ids != null)
        {
          ids.remove(id);
          if (ids.isEmpty())
          {
            mapValueToIds.remove(value);
          }
        }
        mapIdToValue.remove(id);
      }
    }
    finally
    {
      this.lock.writeLock().unlock();
    }
  }
  
  /**
   * Returns the entity who have the given value for the given method.  For 
   * example, if methodName is "getName" and value is "Foo", then the following 
   * is true about the returned entity:  entity.getName().equals("Foo").  If no 
   * entities have that value, {@code null} is returned.  If more than one 
   * entity has that value, the first one encountered is returned.  
   * 
   * @param methodName The name of the method to call.
   * @param value The desired value of the method.
   * @return The first entity that has the given value.
   */
  public T getObject(String methodName, Object value)
  {
    if (!this.loaded)
    {
      load();
    }

    this.lock.readLock().lock();
    try
    {
      Map<Object, TLongSet> mapValueToIds = this.mapMethodNameToValueToIds.get(methodName);
      
      if (mapValueToIds != null)
      {
        // If we're here, then this method has been called before.
        TLongSet ids = mapValueToIds.get(value);
        
        if (ids == null || ids.isEmpty())
        {
          return null;
        }
        
        long id = ids.iterator().next();
        return this.cache.get(this.type, id);
      }
    }
    finally
    {
      this.lock.readLock().unlock();
    }
    
    // If we're here, then this is the first time this method has been called.
    this.lock.writeLock().lock();
    try
    {
      addMethod(methodName);
    }
    finally
    {
      this.lock.writeLock().unlock();
    }
    
    return getObject(methodName, value);
  }
  
  /**
   * Returns the entities who have the given value for the given method.  For 
   * example, if methodName is "getName" and value is "Foo", then the following 
   * is true about the returned entities:  entity.getName().equals("Foo").
   * 
   * @param methodName The name of the method to call.
   * @param value The desired value of the method.
   * @return The entities that have the given value.
   */
  public List<T> getObjects(String methodName, Object value)
  {
    if (!this.loaded)
    {
      load();
    }

    this.lock.readLock().lock();
    try
    {
      Map<Object, TLongSet> mapValueToIds = this.mapMethodNameToValueToIds.get(methodName);
      
      if (mapValueToIds != null)
      {
        // If we're here, then this method has been called before.
        TLongSet ids = mapValueToIds.get(value);
        
        if (ids == null || ids.isEmpty())
        {
          return new ArrayList<>(0);
        }

        // Provide the list of desired IDs to map() so that, if this is an EntityGroup, we can
        // efficiently build all of them from a single query.
        return new ArrayList<>(this.cache.map(this.type, ids.toArray()).valueCollection());
      }
    }
    finally
    {
      this.lock.readLock().unlock();
    }
    
    // If we're here, then this is the first time this method has been called.
    this.lock.writeLock().lock();
    try
    {
      addMethod(methodName);
    }
    finally
    {
      this.lock.writeLock().unlock();
    }
    
    return getObjects(methodName, value);
  }
  
  /**
   * Resets this cache so that it will be rebuilt the next time it is used.
   */
  public void reset()
  {
    // If the cache hasn't been loaded, there is nothing to reset.
    if (!this.loaded)
    {
      return;
    }

    this.lock.writeLock().lock();
    try
    {
      this.loaded = false;
    }
    finally
    {
      this.lock.writeLock().unlock();
    }
  }
  
  /**
   * Updates the values for the given entity in this cache.
   * 
   * @param id The id of the entity to be updated.
   */
  public void update(long id)
  {
    // If the cache hasn't been loaded, there is nothing to update.
    if (!this.loaded)
    {
      return;
    }

    this.lock.writeLock().lock();
    try
    {
      T object = this.cache.get(this.type, id);
      
      // Clear out the previous id/value mappings.
      for (String methodName : this.mapMethodNameToMethod.keySet())
      {
        TLongObjectMap<Object> mapIdToValue = this.mapMethodNameToIdToValue.get(methodName);
        Map<Object, TLongSet> mapValueToIds = this.mapMethodNameToValueToIds.get(methodName);
        
        Object oldValue = mapIdToValue.get(id);
        TLongSet oldIds = mapValueToIds.get(oldValue);
        if (oldIds != null)
        {
          oldIds.remove(id);
          if (oldIds.isEmpty())
          {
            mapValueToIds.remove(oldValue);
          }
        }
        mapIdToValue.remove(id);
        
        if (object != null)
        {
          // Add in the new id/value mapping.
          Object newValue = invokeMethod(object, methodName);
          mapIdToValue.put(id, newValue);
          TLongSet ids = mapValueToIds.get(newValue);
          if (ids == null)
          {
            ids = new TLongHashSet();
            mapValueToIds.put(newValue, ids);
          }
          ids.add(id);
        }
      } 
    }
    finally
    {
      this.lock.writeLock().unlock();
    }
  }
  
  /**
   * Stores the given method and the values of that method for all entities in 
   * this cache.
   * 
   * @param methodName The name of the method to be stored.
   */
  protected void addMethod(String methodName)
  {
    try
    {
      Method method = this.type.getMethod(methodName);
      this.mapMethodNameToMethod.put(methodName, method);
      this.mapMethodNameToValueToIds.put(methodName, new HashMap<Object, TLongSet>());
      this.mapMethodNameToIdToValue.put(methodName, new TLongObjectHashMap<>());
      
      indexMethod(methodName);
    }
    catch (NoSuchMethodException e)
    {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Stores the values of the given method for all entities in this cache.
   * 
   * @param methodName The name of the method to be stored.
   */
  protected void indexMethod(String methodName)
  {
    TLongObjectMap<Object> mapIdToValue = this.mapMethodNameToIdToValue.get(methodName);
    Map<Object, TLongSet> mapValueToIds = this.mapMethodNameToValueToIds.get(methodName);
    
    if (mapIdToValue == null)
    {
      mapIdToValue = new TLongObjectHashMap<>();
      this.mapMethodNameToIdToValue.put(methodName, mapIdToValue);
    }
    else
    {
      mapIdToValue.clear();
    }
    
    if (mapValueToIds == null)
    {
      mapValueToIds = new HashMap<>();
      this.mapMethodNameToValueToIds.put(methodName, mapValueToIds);
    }
    else
    {
      mapValueToIds.clear();
    }
    
    for (T object : this.cache.list(this.type))
    {
      Object value = invokeMethod(object, methodName);
      long id = object.getId();
      mapIdToValue.put(id, value);

      TLongSet ids = mapValueToIds.get(value);
      if (ids == null)
      {
        ids = new TLongHashSet();
        mapValueToIds.put(value, ids);
      }
      ids.add(id);
    }
  }
  
  /**
   * Invokes the given method of the given object.
   * 
   * @param object The object.
   * @param methodName The method to be invoked.
   * @return The return value of the invoked method.
   */
  protected Object invokeMethod(T object, String methodName)
  {
    try
    {
      return this.mapMethodNameToMethod.get(methodName).invoke(object);
    }
    catch (IllegalAccessException e)
    {
      return null;
    }
    catch (InvocationTargetException e)
    {
      return null;
    }
  }
  
  /**
   * Initializes this cache.  Queries all entities on each method that is 
   * currently known by this cache.
   */
  protected void load()
  {
    this.lock.writeLock().lock();
    try
    {
      if (this.loaded)
      {
        return;
      }
      
      for (String methodName : this.mapMethodNameToMethod.keySet())
      {
        indexMethod(methodName);
      }
      
      this.loaded = true;
    }
    finally
    {
      this.lock.writeLock().unlock();
    }
  }
}
