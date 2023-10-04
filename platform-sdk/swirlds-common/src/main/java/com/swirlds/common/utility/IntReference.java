/*
 * Copyright (C) 2016-2023 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.swirlds.common.utility;

import com.swirlds.base.utility.ToStringBuilder;
import java.util.Objects;

/**
 * An object wrapper for an int that allows it to mutate, unlike {@link Integer}. Similar to {@link ValueReference} but
 * for a primitive int with increment decrement functions included.
 */
public class IntReference {
    /** the int value */
    private int value;

    /**
     * @param value the initial value of the int
     */
    public IntReference(final int value) {
        this.value = value;
    }

    /**
     * @return the int value
     */
    public int get() {
        return value;
    }

    /**
     * Set the value of the int
     *
     * @param value the value to set to
     */
    public void set(final int value) {
        this.value = value;
    }

    /** Increment the value by 1 */
    public void increment() {
        this.value++;
    }

    /** Decrement the value by 1 */
    public void decrement() {
        this.value--;
    }

    /**
     * Check if the supplied value is equal to the stored one.
     *
     * @param value the value to check
     * @return true if the values are equal
     */
    public boolean equalsInt(final int value) {
        return this.value == value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final IntReference that = (IntReference) o;
        return equalsInt(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("value", value).toString();
    }
}