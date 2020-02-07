/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.p2p;

import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;

public class TransactionMessage extends Message {
    /**
     * Create a message instance.
     *
     * @param code
     * @param responseMessageClass
     */
    public TransactionMessage(MessageCode code, Class<?> responseMessageClass) {
        super(code, responseMessageClass);
    }
    //
    //    private final Transaction transaction;
    //
    //    /**
    //     * Create a TRANSACTION message.
    //     *
    //     */
    //    public TransactionMessage(Transaction transaction) {
    //        super(MessageCode.TRANSACTION, null);
    //
    //        this.transaction = transaction;
    //
    //        // TODO: consider wrapping by simple codec
    //        this.body = transaction.toBytes();
    //    }
    //
    //    /**
    //     * Parse a TRANSACTION message from byte array.
    //     *
    //     * @param body
    //     */
    //    public TransactionMessage(byte[] body) {
    //        super(MessageCode.TRANSACTION, null);
    //
    //        this.transaction = Transaction.fromBase58(body);
    //
    //        this.body = body;
    //    }
    //
    //    public Transaction getTransaction() {
    //        return transaction;
    //    }
    //
    //    @Override
    //    public String toString() {
    //        return "TransactionMessage [tx=" + transaction + "]";
    //    }
}
