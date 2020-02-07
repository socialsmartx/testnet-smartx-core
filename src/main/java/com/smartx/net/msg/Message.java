/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg;

import java.util.UUID;

import com.smartx.util.Bytes;

/**
 * Abstract message class for all messages on the Smartx network
 *
 */
public abstract class Message {
    /**
     * Message code.
     */
    protected final MessageCode code;
    /**
     * Message uuid used for sync message, length 36.
     */
    protected byte[] uuid;
    /**
     * Response message class.
     */
    protected final Class<?> responseMessageClass;
    /**
     * Message body.
     */
    protected byte[] body;
    /**
     * Create a message instance.
     *
     * @param code
     * @param responseMessageClass
     */
    public Message(MessageCode code, Class<?> responseMessageClass) {
        this.code = code;
        this.uuid = UUID.randomUUID().toString().getBytes();
        this.responseMessageClass = responseMessageClass;
        this.body = Bytes.EMPTY_BYTES;
    }
    /**
     * Create a message instance.
     *
     * @param code
     * @param responseMessageClass
     */
    public Message(MessageCode code, byte[] uuid, Class<?> responseMessageClass) {
        this.code = code;
        this.uuid = uuid;
        this.responseMessageClass = responseMessageClass;
        this.body = Bytes.EMPTY_BYTES;
    }
    /**
     * Get the body of this message
     *
     * @return
     */
    public byte[] getBody() {
        return body;
    }
    /**
     * Get the message code
     *
     * @return
     */
    public MessageCode getCode() {
        return code;
    }
    /**
     * Get the message code
     *
     * @return
     */
    public byte[] getUUID() {
        return uuid;
    }
    public void setUUID(byte[] uu) {
        uuid = uu;
    }
    /**
     * Get the response message class of this message.
     *
     * @return the response message, or null if this message requires no response.
     */
    public Class<?> getResponseMessageClass() {
        return responseMessageClass;
    }
    /**
     * Return the message name.
     */
    public String toString() {
        return getClass().getName();
    }
}
