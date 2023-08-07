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

package com.hedera.node.app.service.contract.impl.utils;

import static com.hedera.node.app.spi.key.KeyUtils.isEmpty;
import static java.util.Objects.requireNonNull;

import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.node.base.Duration;
import com.hedera.hapi.node.base.Key;
import com.hedera.hapi.node.base.KeyList;
import com.hedera.hapi.node.contract.ContractCreateTransactionBody;
import com.hedera.hapi.node.token.CryptoCreateTransactionBody;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Some utilities related to synthetic transaction bodies.
 */
public class SynthTxnUtils {
    public static final Key IMMUTABILITY_SENTINEL_KEY =
            Key.newBuilder().keyList(KeyList.DEFAULT).build();

    public static final long THREE_MONTHS_IN_SECONDS = 7776000L;
    public static final Duration DEFAULT_AUTO_RENEW_PERIOD =
            Duration.newBuilder().seconds(THREE_MONTHS_IN_SECONDS).build();
    public static final String LAZY_CREATION_MEMO = "lazy-created account";

    private SynthTxnUtils() {
        throw new UnsupportedOperationException("Utility Class");
    }

    /**
     * Given a validated {@link ContractCreateTransactionBody} and its pending id, returns the
     * corresponding {@link CryptoCreateTransactionBody} to dispatch.
     *
     * @param pendingId the pending id
     * @param body the {@link ContractCreateTransactionBody}
     * @return the corresponding {@link CryptoCreateTransactionBody}
     */
    public static CryptoCreateTransactionBody synthAccountCreationForContract(
            @NonNull final ContractID pendingId,
            @Nullable final com.hedera.pbj.runtime.io.buffer.Bytes evmAddress,
            @NonNull final ContractCreateTransactionBody body) {
        requireNonNull(body);
        requireNonNull(pendingId);
        final var builder = CryptoCreateTransactionBody.newBuilder()
                .maxAutomaticTokenAssociations(body.maxAutomaticTokenAssociations())
                .declineReward(body.declineReward())
                .memo(body.memo());
        if (body.hasAutoRenewPeriod()) {
            builder.autoRenewPeriod(body.autoRenewPeriodOrThrow());
        }
        if (body.hasStakedNodeId()) {
            builder.stakedNodeId(body.stakedNodeIdOrThrow());
        } else if (body.hasStakedAccountId()) {
            builder.stakedAccountId(body.stakedAccountIdOrThrow());
        }
        if (body.hasAdminKey() && !isEmpty(body.adminKeyOrThrow())) {
            builder.key(body.adminKeyOrThrow());
        } else {
            builder.key(Key.newBuilder().contractID(pendingId));
        }
        if (evmAddress != null) {
            builder.alias(evmAddress);
        }
        return builder.build();
    }

    /**
     * Given an EVM address being lazy-created, returns the corresponding {@link CryptoCreateTransactionBody}
     * to dispatch.
     *
     * @param evmAddress the EVM address
     * @return the corresponding {@link CryptoCreateTransactionBody}
     */
    public static CryptoCreateTransactionBody synthHollowAccountCreation(@NonNull final Bytes evmAddress) {
        requireNonNull(evmAddress);
        // TODO - for mono-service equivalence, need to set the initial balance here
        return CryptoCreateTransactionBody.newBuilder()
                .initialBalance(0L)
                .alias(evmAddress)
                .key(IMMUTABILITY_SENTINEL_KEY)
                .memo(LAZY_CREATION_MEMO)
                .autoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD)
                .build();
    }
}