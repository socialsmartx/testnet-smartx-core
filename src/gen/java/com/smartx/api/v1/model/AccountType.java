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

public class AccountType  {
  
  @ApiModelProperty(value = "The address of this account")
 /**
   * The address of this account  
  **/
  private String address = null;

  @ApiModelProperty(value = "The available balance of this account")
 /**
   * The available balance of this account  
  **/
  private String available = null;

  @ApiModelProperty(value = "The locked balance of this account")
 /**
   * The locked balance of this account  
  **/
  private String locked = null;

  @ApiModelProperty(value = "The nonce of this account")
 /**
   * The nonce of this account  
  **/
  private String nonce = null;

  @ApiModelProperty(value = "The number of transactions received/sent")
 /**
   * The number of transactions received/sent  
  **/
  private Integer transactionCount = null;

  @ApiModelProperty(value = "The number of internal transactions received/sent")
 /**
   * The number of internal transactions received/sent  
  **/
  private Integer internalTransactionCount = null;

  @ApiModelProperty(value = "The number of pending transaction from/to this account")
 /**
   * The number of pending transaction from/to this account  
  **/
  private Integer pendingTransactionCount = null;
 /**
   * The address of this account
   * @return address
  **/
  @JsonProperty("address")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{40}$")  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public AccountType address(String address) {
    this.address = address;
    return this;
  }

 /**
   * The available balance of this account
   * @return available
  **/
  @JsonProperty("available")
 @Pattern(regexp="^\\d+$")  public String getAvailable() {
    return available;
  }

  public void setAvailable(String available) {
    this.available = available;
  }

  public AccountType available(String available) {
    this.available = available;
    return this;
  }

 /**
   * The locked balance of this account
   * @return locked
  **/
  @JsonProperty("locked")
 @Pattern(regexp="^\\d+$")  public String getLocked() {
    return locked;
  }

  public void setLocked(String locked) {
    this.locked = locked;
  }

  public AccountType locked(String locked) {
    this.locked = locked;
    return this;
  }

 /**
   * The nonce of this account
   * @return nonce
  **/
  @JsonProperty("nonce")
 @Pattern(regexp="^\\d+$")  public String getNonce() {
    return nonce;
  }

  public void setNonce(String nonce) {
    this.nonce = nonce;
  }

  public AccountType nonce(String nonce) {
    this.nonce = nonce;
    return this;
  }

 /**
   * The number of transactions received/sent
   * @return transactionCount
  **/
  @JsonProperty("transactionCount")
  public Integer getTransactionCount() {
    return transactionCount;
  }

  public void setTransactionCount(Integer transactionCount) {
    this.transactionCount = transactionCount;
  }

  public AccountType transactionCount(Integer transactionCount) {
    this.transactionCount = transactionCount;
    return this;
  }

 /**
   * The number of internal transactions received/sent
   * @return internalTransactionCount
  **/
  @JsonProperty("internalTransactionCount")
  public Integer getInternalTransactionCount() {
    return internalTransactionCount;
  }

  public void setInternalTransactionCount(Integer internalTransactionCount) {
    this.internalTransactionCount = internalTransactionCount;
  }

  public AccountType internalTransactionCount(Integer internalTransactionCount) {
    this.internalTransactionCount = internalTransactionCount;
    return this;
  }

 /**
   * The number of pending transaction from/to this account
   * @return pendingTransactionCount
  **/
  @JsonProperty("pendingTransactionCount")
  public Integer getPendingTransactionCount() {
    return pendingTransactionCount;
  }

  public void setPendingTransactionCount(Integer pendingTransactionCount) {
    this.pendingTransactionCount = pendingTransactionCount;
  }

  public AccountType pendingTransactionCount(Integer pendingTransactionCount) {
    this.pendingTransactionCount = pendingTransactionCount;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountType {\n");
    
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    available: ").append(toIndentedString(available)).append("\n");
    sb.append("    locked: ").append(toIndentedString(locked)).append("\n");
    sb.append("    nonce: ").append(toIndentedString(nonce)).append("\n");
    sb.append("    transactionCount: ").append(toIndentedString(transactionCount)).append("\n");
    sb.append("    internalTransactionCount: ").append(toIndentedString(internalTransactionCount)).append("\n");
    sb.append("    pendingTransactionCount: ").append(toIndentedString(pendingTransactionCount)).append("\n");
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

