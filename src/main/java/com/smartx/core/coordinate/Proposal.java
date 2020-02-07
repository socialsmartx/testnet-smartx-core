/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.core.coordinate;

import java.util.List;

import com.smartx.block.Block;
import com.smartx.block.BlockHeader;
import com.smartx.config.Network;
import com.smartx.crypto.Key;
import com.smartx.crypto.Key.Signature;
import com.smartx.util.SimpleDecoder;
import com.smartx.util.SimpleEncoder;

public class Proposal {
    private final Proof proof;
    private final BlockHeader blockHeader;
    //private final List<Transaction> transactions;
    private final byte[] encoded;
    private Signature signature;
    public Proposal(Proof proof, BlockHeader blockHeader) {
        this.proof = proof;
        this.blockHeader = blockHeader;
        //this.transactions = transactions;
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(proof.toBytes());
        enc.writeBytes(blockHeader.toBytes());
        //        enc.writeInt(transactions.size());
        //        for (Transaction tx : transactions) {
        //            enc.writeBytes(tx.toBytes());
        //        }
        this.encoded = enc.toBytes();
    }
    public Proposal(byte[] encoded, byte[] signature) {
        SimpleDecoder dec = new SimpleDecoder(encoded);
        this.proof = Proof.fromBytes(dec.readBytes());
        this.blockHeader = BlockHeader.fromBytes(dec.readBytes());
        //        this.transactions = new ArrayList<>();
        //        int n = dec.readInt();
        //        for (int i = 0; i < n; i++) {
        //            transactions.add(Transaction.fromBase58(dec.readBytes()));
        //        }
        this.encoded = encoded;
        this.signature = Signature.fromBytes(signature);
    }
    /**
     * Sign this proposal.
     *
     * @param key
     * @return
     */
    public Proposal sign(Key key) {
        this.signature = key.sign(encoded);
        return this;
    }
    /**
     * <p>
     * Validate proposal format and signature.
     * </p>
     *
     * <p>
     * NOTE: this method will NOT validate the proposed block, nor the proof, nor
     * the transactions inside the block. Use
     * {@link Block#validateHeader(BlockHeader, BlockHeader)} and
     * {@link Block#validateTransactions(BlockHeader, List, Network)} for that
     * purpose.
     * </p>
     *
     * @return true if success, otherwise false
     */
    public boolean validate() {
        //        return getHeight() > 0
        //                && getView() >= 0
        //                && proof != null
        //                && blockHeader != null
        //                && transactions != null
        //                && proof.getHeight() == blockHeader.getNumber()
        //                && encoded != null
        //                && signature != null && Key.verify(encoded, signature);
        //TODO:包含交易
        return getHeight() > 0 && getView() >= 0 && proof != null && blockHeader != null && encoded != null && signature != null && Key.verify(encoded, signature);
    }
    public Proof getProof() {
        return proof;
    }
    public long getHeight() {
        return proof.getHeight();
    }
    public int getView() {
        return proof.getView();
    }
    public BlockHeader getBlockHeader() {
        return blockHeader;
    }
    //    public List<Transaction> getTransactions() {
    //        return transactions;
    //    }
    public Signature getSignature() {
        return signature;
    }
    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(encoded);
        enc.writeBytes(signature.toBytes());
        return enc.toBytes();
    }
    public static Proposal fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        byte[] encoded = dec.readBytes();
        byte[] signature = dec.readBytes();
        return new Proposal(encoded, signature);
    }
    @Override
    public String toString() {
        //        return "Proposal [height=" + getHeight() + ", view = " + getView() + ", # proof votes = "
        //                + proof.getVotes().size() + ", # txs = " + transactions.size() + "]";
        //TODO:包含交易
        return "Proposal [height=" + getHeight() + ", view = " + getView() + ", # proof votes = " + proof.getVotes().size() + "]";
    }
}
