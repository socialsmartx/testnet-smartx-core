package com.smartx.api.v1.model;

import javax.validation.constraints.*;

import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionType  {
  
  @ApiModelProperty(value = "The transaction hash")
 /**
   * The transaction hash  
  **/
  private String hash = null;


@XmlType(name="TypeEnum")
@XmlEnum(String.class)
public enum TypeEnum {

@XmlEnumValue("COINBASE") COINBASE(String.valueOf("COINBASE")), @XmlEnumValue("TRANSFER") TRANSFER(String.valueOf("TRANSFER")), @XmlEnumValue("DELEGATE") DELEGATE(String.valueOf("DELEGATE")), @XmlEnumValue("VOTE") VOTE(String.valueOf("VOTE")), @XmlEnumValue("UNVOTE") UNVOTE(String.valueOf("UNVOTE")), @XmlEnumValue("CREATE") CREATE(String.valueOf("CREATE")), @XmlEnumValue("CALL") CALL(String.valueOf("CALL"));


    private String value;

    TypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TypeEnum fromValue(String v) {
        for (TypeEnum b : TypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

  @ApiModelProperty(value = "The transaction type")
 /**
   * The transaction type  
  **/
  private TypeEnum type = null;

  @ApiModelProperty(value = "Sender's address")
 /**
   * Sender's address  
  **/
  private String from = null;

  @ApiModelProperty(value = "Recipient's address")
 /**
   * Recipient's address  
  **/
  private String to = null;

  @ApiModelProperty(value = "Transaction value in nano SEM")
 /**
   * Transaction value in nano SEM  
  **/
  private String value = null;

  @ApiModelProperty(value = "Transaction fee in nano SEM. For CREATE/CALL, this field is zero; use gas instead")
 /**
   * Transaction fee in nano SEM. For CREATE/CALL, this field is zero; use gas instead  
  **/
  private String fee = null;

  @ApiModelProperty(value = "The nonce of the sender")
 /**
   * The nonce of the sender  
  **/
  private String nonce = null;

  @ApiModelProperty(value = "Transaction timestamp in milliseconds specified by the sender. There can be a time drift up to 2 hours.")
 /**
   * Transaction timestamp in milliseconds specified by the sender. There can be a time drift up to 2 hours.  
  **/
  private String timestamp = null;

  @ApiModelProperty(value = "Transaction data encoded in hexadecimal string")
 /**
   * Transaction data encoded in hexadecimal string  
  **/
  private String data = null;

  @ApiModelProperty(value = "The gas limit set by the sender")
 /**
   * The gas limit set by the sender  
  **/
  private String gas = null;

  @ApiModelProperty(value = "The gas Price set by the sender")
 /**
   * The gas Price set by the sender  
  **/
  private String gasPrice = null;
 /**
   * The transaction hash
   * @return hash
  **/
  @JsonProperty("hash")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{64}$")  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public TransactionType hash(String hash) {
    this.hash = hash;
    return this;
  }

 /**
   * The transaction type
   * @return type
  **/
  @JsonProperty("type")
  public String getType() {
    if (type == null) {
      return null;
    }
    return type.value();
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public TransactionType type(TypeEnum type) {
    this.type = type;
    return this;
  }

 /**
   * Sender&#39;s address
   * @return from
  **/
  @JsonProperty("from")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{40}$")  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public TransactionType from(String from) {
    this.from = from;
    return this;
  }

 /**
   * Recipient&#39;s address
   * @return to
  **/
  @JsonProperty("to")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{40}$")  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public TransactionType to(String to) {
    this.to = to;
    return this;
  }

 /**
   * Transaction value in nano SEM
   * @return value
  **/
  @JsonProperty("value")
 @Pattern(regexp="^\\d+$")  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public TransactionType value(String value) {
    this.value = value;
    return this;
  }

 /**
   * Transaction fee in nano SEM. For CREATE/CALL, this field is zero; use gas instead
   * @return fee
  **/
  @JsonProperty("fee")
 @Pattern(regexp="^\\d+$")  public String getFee() {
    return fee;
  }

  public void setFee(String fee) {
    this.fee = fee;
  }

  public TransactionType fee(String fee) {
    this.fee = fee;
    return this;
  }

 /**
   * The nonce of the sender
   * @return nonce
  **/
  @JsonProperty("nonce")
 @Pattern(regexp="^\\d+$")  public String getNonce() {
    return nonce;
  }

  public void setNonce(String nonce) {
    this.nonce = nonce;
  }

  public TransactionType nonce(String nonce) {
    this.nonce = nonce;
    return this;
  }

 /**
   * Transaction timestamp in milliseconds specified by the sender. There can be a time drift up to 2 hours.
   * @return timestamp
  **/
  @JsonProperty("timestamp")
 @Pattern(regexp="^\\d+$")  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public TransactionType timestamp(String timestamp) {
    this.timestamp = timestamp;
    return this;
  }

 /**
   * Transaction data encoded in hexadecimal string
   * @return data
  **/
  @JsonProperty("data")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]*$")  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public TransactionType data(String data) {
    this.data = data;
    return this;
  }

 /**
   * The gas limit set by the sender
   * @return gas
  **/
  @JsonProperty("gas")
 @Pattern(regexp="^\\d+$")  public String getGas() {
    return gas;
  }

  public void setGas(String gas) {
    this.gas = gas;
  }

  public TransactionType gas(String gas) {
    this.gas = gas;
    return this;
  }

 /**
   * The gas Price set by the sender
   * @return gasPrice
  **/
  @JsonProperty("gasPrice")
 @Pattern(regexp="^\\d+$")  public String getGasPrice() {
    return gasPrice;
  }

  public void setGasPrice(String gasPrice) {
    this.gasPrice = gasPrice;
  }

  public TransactionType gasPrice(String gasPrice) {
    this.gasPrice = gasPrice;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionType {\n");
    
    sb.append("    hash: ").append(toIndentedString(hash)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    from: ").append(toIndentedString(from)).append("\n");
    sb.append("    to: ").append(toIndentedString(to)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    fee: ").append(toIndentedString(fee)).append("\n");
    sb.append("    nonce: ").append(toIndentedString(nonce)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    gas: ").append(toIndentedString(gas)).append("\n");
    sb.append("    gasPrice: ").append(toIndentedString(gasPrice)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private static String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

