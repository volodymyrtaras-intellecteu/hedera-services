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

package com.swirlds.platform.state.signed;

import static org.junit.jupiter.api.Assertions.*;

import com.swirlds.platform.consensus.ConsensusConstants;
import com.swirlds.platform.state.RandomSignedStateGenerator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SignedStateNexusTest {
    @Test
    void basicUsage() {
        final int round = 123;
        final ReservedSignedState original =
                new RandomSignedStateGenerator().setRound(round).build().reserve("test");
        final SignedStateNexus nexus = new SignedStateNexus();

        assertNull(nexus.getState("reason"), "Should be null when initialized");
        assertEquals(ConsensusConstants.ROUND_UNDEFINED, nexus.getRound(), "Should be undefined when initialized");
        nexus.setState(original);

        try (final ReservedSignedState get = nexus.getState("reason")) {
            assertNotNull(get, "Should not be null");
            assertNotSame(original, get, "Should be a different instance");
        }
        assertEquals(round, nexus.getRound(), "Should be set to the round of the state");

        assertFalse(original.isClosed(), "Should not be closed since its still held by the nexus");
        nexus.setState(realState());
        assertTrue(original.isClosed(), "Should be closed once its replaced");

        nexus.clear();
        assertNull(nexus.getState("reason"));
        assertEquals(ConsensusConstants.ROUND_UNDEFINED, nexus.getRound(), "Should be undefined when cleared");
    }

    @Test
    void closedStateTest() {
        final SignedStateNexus nexus = new SignedStateNexus();
        final ReservedSignedState reservedSignedState = realState();
        nexus.setState(reservedSignedState);
        reservedSignedState.close();
        assertNull(
                nexus.getState("reason"), "The nexus has a state, but it cannot be reserved, so it should return null");
    }

    /**
     * Tests a race condition where the state is replaced while a thread trying to reserve it. In this case, the nexus
     * should reserve the replacement state instead.
     */
    @Test
    void raceConditionTest() throws InterruptedException {
        final SignedStateNexus nexus = new SignedStateNexus();

        final ReservedSignedState state1 = mockState();
        final CountDownLatch unblockThread = new CountDownLatch(1);
        final CountDownLatch threadWaiting = new CountDownLatch(1);
        Mockito.when(state1.tryGetAndReserve(Mockito.any())).then(i -> {
            threadWaiting.countDown();
            unblockThread.await();
            return null;
        });
        final ReservedSignedState state2 = mockState();
        final ReservedSignedState state2child = mockState();
        Mockito.when(state2.tryGetAndReserve(Mockito.any())).thenReturn(state2child);

        final CountDownLatch threadDone = new CountDownLatch(1);
        final AtomicReference<ReservedSignedState> threadGetResult = new AtomicReference<>();

        // the nexus should have state1 initially
        nexus.setState(state1);
        // the background thread will try to reserve state1, but will be blocked the unblockThread latch
        new Thread(() -> {
                    threadGetResult.set(nexus.getState("reason"));
                    threadDone.countDown();
                })
                .start();

        // wait for the thread to start trying to reserve the state, fail if it takes too long
        assertTrue(threadWaiting.await(5, TimeUnit.SECONDS), "The thread should be waiting for the state");
        // while the thread is waiting to reserve state1, replace it with state2
        nexus.setState(state2);
        // unblock the thread
        // the nexus should then see that it cannot reserve state1, and that state1 is no longer the current state
        // it should then reserve state2 and set the threadGetResult variable
        unblockThread.countDown();
        // wait for the thread to finish, fail if it takes too long
        assertTrue(threadDone.await(5, TimeUnit.SECONDS), "The thread should have finished");
        // check that the nexus returned the child of state2
        assertSame(state2child, threadGetResult.get(), "The nexus should have returned the child of state2");
    }

    private static ReservedSignedState mockState() {
        final ReservedSignedState state = Mockito.mock(ReservedSignedState.class);
        final SignedState ss = new RandomSignedStateGenerator().build();
        Mockito.when(state.get()).thenReturn(ss);
        return state;
    }

    private static ReservedSignedState realState() {
        return new RandomSignedStateGenerator().build().reserve("test");
    }
}
