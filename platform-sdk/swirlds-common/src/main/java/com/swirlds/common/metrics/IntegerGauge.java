/*
 * Copyright (C) 2023 Hedera Hashgraph, LLC
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

package com.swirlds.common.metrics;

import static com.swirlds.common.metrics.Metric.ValueType.VALUE;

import com.swirlds.base.utility.ToStringBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.EnumSet;
import java.util.Objects;

/**
 * An {@code IntegerGauge} stores a single {@code int} value.
 * <p>
 * Only the current value is stored, no history or distribution is kept.
 */
public interface IntegerGauge extends Metric {

    /**
     * {@inheritDoc}
     */
    @Override
    default MetricType getMetricType() {
        return MetricType.GAUGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default DataType getDataType() {
        return DataType.INT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default EnumSet<ValueType> getValueTypes() {
        return EnumSet.of(VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Integer get(@NonNull final ValueType valueType) {
        Objects.requireNonNull(valueType, "valueType");
        if (valueType == VALUE) {
            return get();
        }
        throw new IllegalArgumentException("Unsupported ValueType: " + valueType);
    }

    /**
     * Get the current value
     *
     * @return the current value
     */
    int get();

    /**
     * Set the current value
     *
     * @param newValue
     * 		the new value
     */
    void set(final int newValue);

    /**
     * Configuration of a {@link IntegerGauge}
     */
    final class Config extends MetricConfig<IntegerGauge, IntegerGauge.Config> {

        private final int initialValue;

        /**
         * Constructor of {@code IntegerGauge.Config}
         *
         * The {@link #getInitialValue() initialValue} is by default set to {@code 0},
         * the {@link #getFormat() format} is set to "%d".
         *
         * @param category
         * 		the kind of metric (metrics are grouped or filtered by this)
         * @param name
         * 		a short name for the metric
         * @throws IllegalArgumentException
         * 		if one of the parameters is {@code null} or consists only of whitespaces
         */
        public Config(@NonNull final String category, @NonNull final String name) {

            super(category, name, "%d");
            this.initialValue = 0;
        }

        private Config(
                @NonNull final String category,
                @NonNull final String name,
                @NonNull final String description,
                @NonNull final String unit,
                @NonNull final String format,
                final int initialValue) {

            super(category, name, description, unit, format);
            this.initialValue = initialValue;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public IntegerGauge.Config withDescription(@NonNull final String description) {
            return new IntegerGauge.Config(
                    getCategory(), getName(), description, getUnit(), getFormat(), getInitialValue());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public IntegerGauge.Config withUnit(@NonNull final String unit) {
            return new IntegerGauge.Config(
                    getCategory(), getName(), getDescription(), unit, getFormat(), getInitialValue());
        }

        /**
         * Sets the {@link Metric#getFormat() Metric.format} in fluent style.
         *
         * @param format
         * 		the format-string
         * @return a new configuration-object with updated {@code format}
         * @throws IllegalArgumentException
         * 		if {@code format} is {@code null} or consists only of whitespaces
         */
        @NonNull
        public IntegerGauge.Config withFormat(@NonNull final String format) {
            return new IntegerGauge.Config(
                    getCategory(), getName(), getDescription(), getUnit(), format, getInitialValue());
        }

        /**
         * Getter of the {@code initialValue}
         *
         * @return the {@code initialValue}
         */
        public int getInitialValue() {
            return initialValue;
        }

        /**
         * Fluent-style setter of the initial value.
         *
         * @param initialValue
         * 		the initial value
         * @return a new configuration-object with updated {@code initialValue}
         */
        @NonNull
        public IntegerGauge.Config withInitialValue(final int initialValue) {
            return new IntegerGauge.Config(
                    getCategory(), getName(), getDescription(), getUnit(), getFormat(), initialValue);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<IntegerGauge> getResultClass() {
            return IntegerGauge.class;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @NonNull
        public IntegerGauge create(@NonNull final MetricsFactory factory) {
            return factory.createIntegerGauge(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .appendSuper(super.toString())
                    .append("initialValue", initialValue)
                    .toString();
        }
    }
}
