/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.crypto.ed25519.bip32;

import com.smartx.crypto.ed25519.bip32.key.HdPrivateKey;
import com.smartx.crypto.ed25519.bip32.key.HdPublicKey;

/**
 * A HD pub/private key
 */
public class HdKeyPair {
    private final HdPrivateKey privateKey;
    private final HdPublicKey publicKey;
    private final CoinType coinType;
    private final String path;
    public HdKeyPair(HdPrivateKey privateKey, HdPublicKey publicKey, CoinType coinType, String path) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.coinType = coinType;
        this.path = path;
    }
    public HdPrivateKey getPrivateKey() {
        return privateKey;
    }
    public HdPublicKey getPublicKey() {
        return publicKey;
    }
    public CoinType getCoinType() {
        return coinType;
    }
    public String getPath() {
        return path;
    }
}
