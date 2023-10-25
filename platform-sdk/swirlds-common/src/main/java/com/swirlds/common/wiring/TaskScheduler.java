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

package com.swirlds.common.wiring;

import com.swirlds.common.wiring.counters.ObjectCounter;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.function.Consumer;

/**
 * Schedules tasks for a component.
 *
 * @param <O> the output type of the primary output wire (use {@link Void}) if no output is needed)
 */
public abstract class TaskScheduler<O> extends OutputWire<O> {

    private final boolean flushEnabled;

    /**
     * Constructor.
     *
     * @param model               the wiring model containing this task scheduler
     * @param name                the name of the task scheduler
     * @param flushEnabled        if true, then {@link #flush()} will be enabled,
     *                            otherwise it will throw.
     * @param insertionIsBlocking when data is inserted into this task scheduler, will it block until capacity is
     *                            available?
     */
    protected TaskScheduler(
            @NonNull final WiringModel model,
            @NonNull final String name,
            final boolean flushEnabled,
            final boolean insertionIsBlocking) {
        super(model, name, true, insertionIsBlocking);
        this.flushEnabled = flushEnabled;
    }

    /**
     * Cast this scheduler into whatever a variable is expecting. Sometimes the compiler gets confused with generics,
     * and path of least resistance is to just cast to the proper data type.
     *
     * <p>
     * Warning: this will appease the compiler, but it is possible to cast a scheduler into a data type that will cause
     * runtime exceptions. Use with appropriate caution.
     *
     * @param <X> the type to cast to
     * @return this, cast into whatever type is requested
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public final <X> TaskScheduler<X> cast() {
        return (TaskScheduler<X>) this;
    }

    /**
     * Get an input wire for passing data to this task scheduler. In order to use this wire, a handler must be bound via
     * {@link InputWire#bind(Consumer)}.
     *
     * @param name the name of the input wire
     * @param <I>  the type of data that is inserted via this input wire
     * @return the input wire
     */
    @NonNull
    public final <I> InputWire<I, O> buildInputWire(@NonNull final String name) {
        return new InputWire<>(this, name);
    }

    /**
     * Get the number of unprocessed tasks. Returns -1 if this task scheduler is not monitoring the number of
     * unprocessed tasks. Schedulers do not track the number of unprocessed tasks by default. This method will always
     * return -1 unless one of the following is true:
     * <ul>
     * <li>{@link TaskSchedulerMetricsBuilder#withUnhandledTaskMetricEnabled(boolean)} is called with the value true</li>
     * <li>{@link TaskSchedulerBuilder#withUnhandledTaskCapacity(long)} is passed a positive value</li>
     * <li>{@link TaskSchedulerBuilder#withOnRamp(ObjectCounter)} is passed a counter that is not a no op counter</li>
     * </ul>
     */
    public abstract long getUnprocessedTaskCount();

    /**
     * Flush all data in the task scheduler. Blocks until all data currently in flight has been processed.
     *
     * <p>
     * Note: must be enabled by passing true to {@link TaskSchedulerBuilder#withFlushingEnabled(boolean)}.
     *
     * <p>
     * Warning: some implementations of flush may block indefinitely if new work is continuously added to the scheduler
     * while flushing. Such implementations are guaranteed to finish flushing once new work is no longer being added.
     * Some implementations do not have this restriction, and will return as soon as all of the in flight work has been
     * processed, regardless of whether or not new work is being added.
     *
     * @throws UnsupportedOperationException if {@link TaskSchedulerBuilder#withFlushingEnabled(boolean)} was set to false (or
     *                                       was unset, default is false)
     */
    public abstract void flush();

    /**
     * By default a component has a single output wire (i.e. the primary output wire). This method allows additional
     * output wires to be created.
     *
     * <p>
     * Unlike primary wires, secondary output wires need to be passed to a component's constructor. It is considered a
     * violation of convention to push data into a secondary output wire from any code that is not executing within this
     * task scheduler.
     *
     * @param <T> the type of data that is transmitted over this output wire
     * @return the secondary output wire
     */
    @NonNull
    public <T> SecondaryOutputWire<T> buildSecondaryOutputWire() {
        return new SecondaryOutputWire<>(getModel(), getName(), true);
    }

    /**
     * Add a task to the scheduler. May block if back pressure is enabled.
     *
     * @param handler handles the provided data
     * @param data    the data to be processed by the task scheduler
     */
    protected abstract void put(@NonNull Consumer<Object> handler, @Nullable Object data);

    /**
     * Add a task to the scheduler. If backpressure is enabled and there is not immediately capacity available, this
     * method will not accept the data.
     *
     * @param data the data to be processed by the scheduler
     * @return true if the data was accepted, false otherwise
     */
    protected abstract boolean offer(@NonNull Consumer<Object> handler, @Nullable Object data);

    /**
     * Inject data into the scheduler, doing so even if it causes the number of unprocessed tasks to exceed the capacity
     * specified by configured back pressure. If backpressure is disabled, this operation is logically equivalent to
     * {@link #put(Consumer, Object)}.
     *
     * @param data the data to be processed by the scheduler
     */
    protected abstract void inject(@NonNull Consumer<Object> handler, @Nullable Object data);

    /**
     * Throw an {@link UnsupportedOperationException} if flushing is not enabled.
     */
    protected final void throwIfFlushDisabled() {
        if (!flushEnabled) {
            throw new UnsupportedOperationException("Flushing is not enabled for the task scheduler " + getName());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method is implemented here to allow classes in this package to call forward(), which otherwise would not be
     * visible.
     */
    @Override
    protected final void forward(@NonNull O data) {
        super.forward(data);
    }
}