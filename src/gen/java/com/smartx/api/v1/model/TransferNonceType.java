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

public class TransferNonceType  {
  
  @ApiModelProperty(value = "Block timestamp in milliseconds specified by the block producer.")
 /**
   * Block timestamp in milliseconds specified by the block producer.  
  **/
  private String timestamp = null;

  @ApiModelProperty(value = "The block nonce")
 /**
   * The block nonce  
  **/
  private String nonce = null;
 /**
   * Block timestamp in milliseconds specified by the block producer.
   * @return timestamp
  **/
  @JsonProperty("timestamp")
 @Pattern(regexp="^\\d+$")  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public TransferNonceType timestamp(String timestamp) {
    this.timestamp = timestamp;
    return this;
  }

 /**
   * The block nonce
   * @return nonce
  **/
  @JsonProperty("nonce")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{40}$")  public String getNonce() {
    return nonce;
  }

  public void setNonce(String nonce) {
    this.nonce = nonce;
  }

  public TransferNonceType nonce(String nonce) {
    this.nonce = nonce;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransferNonceType {\n");
    
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    nonce: ").append(toIndentedString(nonce)).append("\n");
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

