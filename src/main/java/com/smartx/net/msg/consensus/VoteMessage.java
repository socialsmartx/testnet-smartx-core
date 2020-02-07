/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.consensus;

import com.smartx.core.coordinate.Vote;
import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;

public class VoteMessage extends Message {
    private final Vote vote;
    public VoteMessage(Vote vote) {
        super(MessageCode.BFT_VOTE, null);
        this.vote = vote;
        // TODO: consider wrapping by simple codec
        this.body = vote.toBytes();
    }
    public VoteMessage(byte[] uuid, byte[] body) {
        super(MessageCode.BFT_VOTE, uuid, null);
        this.vote = Vote.fromBytes(body);
        this.body = body;
    }
    public Vote getVote() {
        return vote;
    }
    @Override
    public String toString() {
        return "BFTVoteMessage: " + vote;
    }
}
