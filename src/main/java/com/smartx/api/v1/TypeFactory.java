/**
 Copyright (c) 2017-2018 The SmartX Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.api.v1;

import java.math.BigInteger;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.smartx.Kernel;
import com.smartx.api.v1.model.*;
import com.smartx.block.Block;
import com.smartx.block.Field;
import com.smartx.core.blockchain.BlockDAG;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.TraverBlock;
import com.smartx.core.consensus.SatException;
import com.smartx.core.coordinate.RuleSign;
import com.smartx.core.state.Account;
import com.smartx.crypto.Hex;

public class TypeFactory {
    public static InfoType infoType(Kernel kernel) {
        //TODO:获取待处理交易的个数   kernel.getPendingManager().getPendingTransactions().size()
        BlockDAG blockDAG = SATObjFactory.GetBlockDAG();
        String latestBlockHash = "";
        Block MC = blockDAG.GetLastestMC();
        if (null != MC) latestBlockHash = MC.header.hash;
        int activePeerNum = kernel.getChannelManager().getActivePeers().size();
        return new InfoType().network(kernel.getConfig().network()).capabilities(kernel.getConfig().getClientCapabilities().toList()).clientId(kernel.getConfig().getClientId()).coinbase(Hex.encode0x(kernel.getCoinbase().toAddress())).latestBlockNumber(Long.toString(blockDAG.GetLatestHeight())).latestBlockHash(latestBlockHash).activePeers(activePeerNum).pendingTransactions(10);
    }
    public static AccountType accountType(Account account, BigInteger available, BigInteger locked, int transactionCount, int internalTransactionCount, int pendingTransactionCount) {
        //TODO: encodeAmount(account.getAvailable())
        //TODO: encodeAmount(account.getLocked())
        return new AccountType().address(Hex.encode0x(account.getAddress())).available(available.toString()).locked(locked.toString()).nonce(String.valueOf(account.getNonce())).transactionCount(transactionCount).internalTransactionCount(internalTransactionCount).pendingTransactionCount(pendingTransactionCount);
    }
    public static BalanceType balanceType(String address, BigInteger amount) {
        //TODO: encodeAmount(account.getLocked())
        return new BalanceType().address(address).available(amount.toString());
    }
    public static JsonType jsonType(String json) {
        //TODO: encodeAmount(account.getLocked())
        return new JsonType().json(json);
    }
    public static TransactionType transactionType(String hash, String from, String to, long timestamp, BigInteger fee, BigInteger amount) {
        return new TransactionType().hash(hash).from(from).to(to).fee(fee.toString()).value(amount.toString()).timestamp(Long.toString(timestamp));
    }
    public static BlockType blockType(Block block) {
        int txCount = 0;
        TraverBlock traverBlock = SATObjFactory.GetTraveBlock();
        //height fee amount difficulty
        BlockType blockType = new BlockType().height(Long.toString(block.height)).hash(block.header.hash).headtype(block.header.headtype).btype(block.header.btype.toString()).difficulty(block.diff).address(block.header.address).timestamp(Long.toString(block.header.timestamp)).nonce(block.header.nonce).random(block.header.random);
        switch (block.header.btype) {
            case SMARTX_MAIN:
                //如果是MC块则返回裁决层签名和VIEW，还有难度，交易数，出块奖励
                //blockType.difficulty(block.diff);
                blockType.ruleSignCount(block.ruleSigns.size());
                List<RuleSign> ruleSignList = block.ruleSigns;
                List<SignType> signTypeList = new ArrayList<>();
                for (RuleSign ruleSign : ruleSignList) {
                    SignType sign = new SignType().sign(ruleSign.sign).address(ruleSign.signer);
                    signTypeList.add(sign);
                }
                blockType.setRuleSignList(signTypeList);
                txCount = traverBlock.GetBlockRef(block).size();
                blockType.transactionCount(txCount);
                blockType.reward(block.rewards.toString());
                blockType.nodename(block.nodename);
                break;
            case SMARTX_MAINREF:
                //如果是普通主块则返回难度，交易数，出块奖励
                //blockType.difficulty(block.diff);
                txCount = traverBlock.GetBlockRef(block).size();
                blockType.transactionCount(txCount);
                blockType.reward(block.rewards.toString());
                blockType.nodename(block.nodename);
                break;
            case SMARTX_TXS:
                //如果是交易块则返回from to amount fee
                Field inField = block.GetInField();
                Field outField = block.GetOutField();
                blockType.from(inField.hash);
                blockType.to(outField.hash);
                blockType.amount(outField.amount.toString());
                blockType.fee(outField.fee.toString());
                blockType.nodename(block.nodename);
                break;
        }
        return blockType;
    }
    public static TransferNonceType transferNonceType(String nonce, long timestamp) {
        return new TransferNonceType().nonce(nonce).timestamp(Long.toString(timestamp));
    }
}


