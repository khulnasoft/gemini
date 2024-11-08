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

package com.khulnasoft.collection.relation;

import gnu.trove.set.*;

import java.io.*;
import java.util.*;

/**
 * A binary relation between longs.  It can be thought of as a set of
 * unique long pairs.  For example, <tt>{(1,2),(1,3),(2,7)}</tt> could
 * be the string representation of an LongRelation containing three
 * pairs.
 * <p>
 * For the pair <tt>(1,2)</tt>, this class refers to 1 as the "left value"
 * and to 2 as the "right value".  Calling <tt>rightValues(1)</tt> would
 * return <tt>[2,3]</tt>.  Calling <tt>leftValues(2)</tt> would return
 * <tt>[1]</tt>.
 * <p>
 * TODO: Add a retainAll method.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Binary_relation">http://en.wikipedia.org/wiki/Binary_relation</a>
 */
public interface LongRelation extends Serializable
{
  /**
   * Relates the two values.  Returns <tt>true</tt> unless the values
   * were previously related.
   */
  boolean add(long left, long right);

  /**
   * Relates all values that are related in the given relation.  Returns
   * <tt>true</tt> unless the relation is <tt>null</tt> or all of the
   * values were previously related.
   */
  boolean addAll(LongRelation relation);

  /**
   * Clears the relation.
   */
  void clear();

  /**
   * Returns a deep copy of this relation.  The returned <tt>Object</tt>
   * is an instance of <tt>LongRelation</tt> but it is cast to an
   * <tt>Object</tt> for Java 1.5 compatibility.
   */
  Object clone();

  /**
   * Returns <tt>true</tt> if the values are related.
   */
  boolean contains(long left, long right);

  /**
   * Returns <tt>true</tt> if all the values are related.  Also
   * returns <tt>true</tt> if the given relation is <tt>null</tt>.
   */
  boolean containsAll(LongRelation relation);

  /**
   * Returns <tt>true</tt> if the value is related to any right
   * values.
   */
  boolean containsLeftValue(long left);

  /**
   * Returns <tt>true</tt> if the value is related to any left
   * values.
   */
  boolean containsRightValue(long right);

  /**
   * Returns an iterator over the pairs in this relation.
   */
  LongRelationIterator iterator();

  /**
   * Returns the number of left values related to the given
   * value.
   */
  int leftSize(long right);

  /**
   * Returns an array of the left values related to the given
   * value.  The returned values are not necessarily in a
   * meaningful order.
   */
  long[] leftValues(long right);

  /**
   * Unrelates the two values.  Returns <tt>true</tt> unless the
   * values were already unrelated.
   */
  boolean remove(long left, long right);

  /**
   * Unrelates all values that are related in the given relation.  Returns
   * <tt>true</tt> unless the relation is <tt>null</tt> or none of the
   * values were previously related.
   */
  boolean removeAll(LongRelation relation);

  /**
   * Unrelates the given value from all right values.
   */
  boolean removeLeftValue(long left);

  /**
   * Unrelates the given value from all left values.
   */
  boolean removeRightValue(long right);

  /**
   * Returns the number of right values related to the given
   * value.
   */
  int rightSize(long right);

  /**
   * Returns the number of right values related to the given value, counting
   * only IDs included in filterRightIds. Counts all if filterRightIds is null
   * or empty.
   */
  int rightSize(long right, Collection<Long> filterRightIds);

  /**
   * Returns the number of right values related to the given value, counting
   * only IDs included in filterRightIds. Counts all if filterRightIds is null
   * or empty.
   */
  int rightSize(long right, TLongSet filterRightIds);

  /**
   * Returns an array of the right values related to the given
   * value.  The returned values are not necessarily in a
   * meaningful order.
   */
  long[] rightValues(long left);

  /**
   * Returns an array of the right values related to the given
   * value.  The returned values are not necessarily in a
   * meaningful order.
   */
  TLongSet rightValuesLongSet(long left);

  /**
   * Returns the number of pairs in the relation.
   */
  int size();
}
