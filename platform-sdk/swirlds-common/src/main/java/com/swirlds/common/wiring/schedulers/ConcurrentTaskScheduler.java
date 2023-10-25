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

package com.swirlds.common.wiring.schedulers;

import com.swirlds.common.wiring.TaskScheduler;
import com.swirlds.common.wiring.WiringModel;
import com.swirlds.common.wiring.counters.ObjectCounter;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

/**
 * A {@link TaskScheduler} that permits parallel execution of tasks. Similar to {@link ConcurrentTaskScheduler} but with
 * extra metering.
 *
 * @param <O> the output time of the wire (use {@link Void}) for a task scheduler with no output type)
 */
public class ConcurrentTaskScheduler<O> extends TaskScheduler<O> {

    private final ObjectCounter onRamp;
    private final ObjectCounter offRamp;
    private final UncaughtExceptionHandler uncaughtExceptionHandler;
    private final ForkJoinPool pool;

    /**
     * Constructor.
     *
     * @param model                    the wiring model containing this wire
     * @param name                     the name of the wire
     * @param pool                     the fork join pool that will execute tasks on this wire
     * @param uncaughtExceptionHandler the handler for uncaught exceptions
     * @param onRamp                   an object counter that is incremented when data is added to the wire, ignored if
     *                                 null
     * @param offRamp                  an object counter that is decremented when data is removed from the wire, ignored
     *                                 if null
     * @param flushEnabled             if true, then {@link #flush() will be enabled, otherwise it will throw.
     * @param insertionIsBlocking      when data is inserted into this wire, will it block until capacity is available?
     */
    public ConcurrentTaskScheduler(
            @NonNull final WiringModel model,
            @NonNull final String name,
            @NonNull ForkJoinPool pool,
            @NonNull UncaughtExceptionHandler uncaughtExceptionHandler,
            @NonNull final ObjectCounter onRamp,
            @NonNull final ObjectCounter offRamp,
            final boolean flushEnabled,
            final boolean insertionIsBlocking) {

        super(model, name, flushEnabled, insertionIsBlocking);

        this.pool = Objects.requireNonNull(pool);
        this.uncaughtExceptionHandler = Objects.requireNonNull(uncaughtExceptionHandler);
        this.onRamp = Objects.requireNonNull(onRamp);
        this.offRamp = Objects.requireNonNull(offRamp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void put(@NonNull final Consumer<Object> handler, @Nullable final Object data) {
        onRamp.onRamp();
        new ConcurrentTask(pool, offRamp, uncaughtExceptionHandler, handler, data).send();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean offer(@NonNull final Consumer<Object> handler, @Nullable final Object data) {
        boolean accepted = onRamp.attemptOnRamp();
        if (accepted) {
            new ConcurrentTask(pool, offRamp, uncaughtExceptionHandler, handler, data).send();
        }
        return accepted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void inject(@NonNull final Consumer<Object> handler, @Nullable final Object data) {
        onRamp.forceOnRamp();
        new ConcurrentTask(pool, offRamp, uncaughtExceptionHandler, handler, data).send();
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
        throwIfFlushDisabled();
        onRamp.waitUntilEmpty();
    }
}