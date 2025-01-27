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

package com.hedera.node.app.service.contract.impl.exec.systemcontracts.hts.grantapproval;

import com.hedera.hapi.node.base.AccountID;
import com.hedera.hapi.node.base.TokenID;
import com.hedera.hapi.node.base.TokenType;
import com.hedera.hapi.node.token.CryptoApproveAllowanceTransactionBody;
import com.hedera.hapi.node.token.NftAllowance;
import com.hedera.hapi.node.token.TokenAllowance;
import com.hedera.hapi.node.transaction.TransactionBody;
import com.hedera.node.app.service.contract.impl.exec.gas.SystemContractGasCalculator;
import com.hedera.node.app.service.contract.impl.exec.scope.VerificationStrategy;
import com.hedera.node.app.service.contract.impl.exec.systemcontracts.hts.AbstractHtsCall;
import com.hedera.node.app.service.contract.impl.hevm.HederaWorldUpdater.Enhancement;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.math.BigInteger;

public abstract class AbstractGrantApprovalCall extends AbstractHtsCall {
    protected final VerificationStrategy verificationStrategy;
    protected final AccountID senderId;
    protected final TokenID token;
    protected final AccountID spender;
    protected final BigInteger amount;
    protected final TokenType tokenType;

    // too many parameters
    @SuppressWarnings("java:S107")
    protected AbstractGrantApprovalCall(
            @NonNull final SystemContractGasCalculator gasCalculator,
            @NonNull final Enhancement enhancement,
            @NonNull final VerificationStrategy verificationStrategy,
            @NonNull final AccountID senderId,
            @NonNull final TokenID token,
            @NonNull final AccountID spender,
            @NonNull final BigInteger amount,
            @NonNull final TokenType tokenType,
            @NonNull final boolean isViewCall) {
        super(gasCalculator, enhancement, isViewCall);
        this.verificationStrategy = verificationStrategy;
        this.senderId = senderId;
        this.token = token;
        this.spender = spender;
        this.amount = amount;
        this.tokenType = tokenType;
    }

    public TransactionBody callGrantApproval() {
        return TransactionBody.newBuilder()
                .cryptoApproveAllowance(approve(token, spender, amount, tokenType))
                .build();
    }

    private CryptoApproveAllowanceTransactionBody approve(
            @NonNull final TokenID token,
            @NonNull final AccountID spender,
            @NonNull final BigInteger amount,
            @NonNull final TokenType tokenType) {
        return tokenType.equals(TokenType.FUNGIBLE_COMMON)
                ? CryptoApproveAllowanceTransactionBody.newBuilder()
                        .tokenAllowances(TokenAllowance.newBuilder()
                                .tokenId(token)
                                .spender(spender)
                                .owner(senderId)
                                .amount(amount.longValue())
                                .build())
                        .build()
                : CryptoApproveAllowanceTransactionBody.newBuilder()
                        .nftAllowances(NftAllowance.newBuilder()
                                .tokenId(token)
                                .spender(spender)
                                .owner(senderId)
                                .serialNumbers(amount.longValue())
                                .build())
                        .build();
    }
}
