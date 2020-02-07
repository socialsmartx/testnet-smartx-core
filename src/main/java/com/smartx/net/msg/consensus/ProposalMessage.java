/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg.consensus;

import com.smartx.core.coordinate.Proposal;
import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;

public class ProposalMessage extends Message {
    private final Proposal proposal;
    public ProposalMessage(Proposal proposal) {
        super(MessageCode.BFT_PROPOSAL, null);
        this.proposal = proposal;
        // TODO: consider wrapping by simple codec
        this.body = proposal.toBytes();
    }
    public ProposalMessage(byte[] uuid, byte[] body) {
        super(MessageCode.BFT_PROPOSAL, uuid, null);
        this.proposal = Proposal.fromBytes(body);
        this.body = body;
    }
    public Proposal getProposal() {
        return proposal;
    }
    @Override
    public String toString() {
        return "BFTProposalMessage: " + proposal;
    }
}
