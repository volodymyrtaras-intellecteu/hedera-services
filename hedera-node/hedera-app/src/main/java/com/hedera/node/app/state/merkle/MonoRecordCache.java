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

package com.hedera.node.app.state.merkle;

import com.hedera.hapi.node.base.AccountID;
import com.hedera.hapi.node.base.TransactionID;
import com.hedera.hapi.node.transaction.TransactionReceipt;
import com.hedera.node.app.state.RecordCache;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class MonoRecordCache implements RecordCache {

    @Override
    public void put(@NonNull TransactionID transactionID, @NonNull AccountID nodeAccountID) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nullable
    @Override
    public CacheItem get(@NonNull TransactionID transactionID) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(@NonNull TransactionID transactionID, @NonNull TransactionReceipt receipt) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}