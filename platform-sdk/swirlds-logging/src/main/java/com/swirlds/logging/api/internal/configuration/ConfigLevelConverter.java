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

package com.swirlds.logging.api.internal.configuration;

import com.swirlds.config.api.converter.ConfigConverter;
import com.swirlds.logging.api.internal.level.ConfigLevel;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A class that implements the {@link ConfigConverter} interface for converting configuration values to {@link ConfigLevel}.
 * It is used to convert strings to {@link ConfigLevel} enum values based on the string representation.
 *
 * @see ConfigConverter
 * @see ConfigLevel
 */
public class ConfigLevelConverter implements ConfigConverter<ConfigLevel> {

    /**
     * Converts a string representation of a logging level to a {@link ConfigLevel} enum value.
     *
     * @param value The string value to convert.
     * @return The {@link ConfigLevel} enum value corresponding to the provided string.
     * @throws IllegalArgumentException If the provided value cannot be converted to a valid {@link ConfigLevel}.
     * @throws NullPointerException     If the provided value is {@code null}.
     */
    @Nullable
    @Override
    public ConfigLevel convert(@NonNull final String value) throws IllegalArgumentException, NullPointerException {
        if (value == null) {
            throw new NullPointerException("null can not be converted");
        }

        try {
            return ConfigLevel.valueOf(value.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "The given value '%s' can not be converted to a logging level.".formatted(value));
        }
    }
}