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

package com.swirlds.common.wiring.internal;

import static com.swirlds.logging.LogMarker.EXCEPTION;

import com.swirlds.common.utility.StackTrace;
import com.swirlds.common.wiring.Wire;
import com.swirlds.common.wiring.counters.NoOpObjectCounter;
import com.swirlds.common.wiring.counters.ObjectCounter;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A {@link Wire} that permits parallel execution of tasks. Similar to {@link ConcurrentWire} but with extra metering.
 */
public class ConcurrentWire extends Wire {

    private static final Logger logger = LogManager.getLogger(ConcurrentWire.class);

    private final ObjectCounter onRamp;
    private final ObjectCounter offRamp;
    private final String name;

    private final ForkJoinPool pool;

    /**
     * Constructor.
     *
     * @param pool    the fork join pool that will execute tasks on this wire
     * @param name    the name of the wire
     * @param onRamp  an object counter that is incremented when data is added to the wire, ignored if null
     * @param offRamp an object counter that is decremented when data is removed from the wire, ignored if null
     */
    public ConcurrentWire(
            @NonNull ForkJoinPool pool,
            @NonNull final String name,
            @Nullable final ObjectCounter onRamp,
            @Nullable final ObjectCounter offRamp) {
        this.pool = Objects.requireNonNull(pool);
        this.name = Objects.requireNonNull(name);
        this.onRamp = onRamp == null ? NoOpObjectCounter.getInstance() : onRamp;
        this.offRamp = offRamp == null ? NoOpObjectCounter.getInstance() : offRamp;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void put(@NonNull final Consumer<Object> handler, @NonNull final Object data) {
        onRamp.onRamp();
        new AbstractTask(pool, 0) {
            @Override
            protected boolean exec() {
                offRamp.offRamp();
                handler.accept(data);
                return true;
            }
        }.send();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void interruptablePut(@NonNull final Consumer<Object> handler, @NonNull final Object data)
            throws InterruptedException {
        onRamp.interruptableOnRamp();
        new AbstractTask(pool, 0) {
            @Override
            protected boolean exec() {
                offRamp.offRamp();
                handler.accept(data);
                return true;
            }
        }.send();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean offer(@NonNull final Consumer<Object> handler, @NonNull final Object data) {
        boolean accepted = onRamp.attemptOnRamp();
        if (!accepted) {
            return false;
        }
        new AbstractTask(pool, 0) {
            @Override
            protected boolean exec() {
                offRamp.offRamp();
                handler.accept(data);
                return true;
            }
        }.send();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void inject(@NonNull final Consumer<Object> handler, @NonNull final Object data) {
        onRamp.forceOnRamp();
        new AbstractTask(pool, 0) {
            @Override
            protected boolean exec() {
                offRamp.offRamp();
                handler.accept(data);
                return true;
            }
        }.send();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getUnprocessedTaskCount() {
        return onRamp.getCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() {
        logger.error(
                EXCEPTION.getMarker(),
                "flush() called on concurrent wire {}. Concurrent wires do not implement flush, "
                        + "and so this operation is a no op.\n{}",
                name,
                StackTrace.getStackTrace());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void interruptableFlush() {
        logger.error(
                EXCEPTION.getMarker(),
                "flush() called on concurrent wire {}. Concurrent wires do not implement flush, "
                        + "and so this operation is a no op.\n{}",
                name,
                StackTrace.getStackTrace());
    }
}