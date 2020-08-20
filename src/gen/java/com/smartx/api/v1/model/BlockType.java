package com.smartx.api.v1.model;

import com.smartx.api.v1.model.SignType;
import java.util.ArrayList;
import java.util.List;
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

public class BlockType  {
  
  @ApiModelProperty(value = "Block height.")
 /**
   * Block height.  
  **/
  private String height = null;

  @ApiModelProperty(value = "Mc Block Reward.")
 /**
   * Mc Block Reward.  
  **/
  private String reward = null;

  @ApiModelProperty(value = "Transaction Count In Block")
 /**
   * Transaction Count In Block  
  **/
  private Integer transactionCount = null;

  @ApiModelProperty(value = "Time cost to generate the block")
 /**
   * Time cost to generate the block  
  **/
  private String timeCost = null;

  @ApiModelProperty(value = "The block hash")
 /**
   * The block hash  
  **/
  private String hash = null;

  @ApiModelProperty(value = "Main Block difficulty.")
 /**
   * Main Block difficulty.  
  **/
  private String difficulty = null;

  @ApiModelProperty(value = "The block header type")
 /**
   * The block header type  
  **/
  private Integer headtype = null;

  @ApiModelProperty(value = "the block type")
 /**
   * the block type  
  **/
  private String btype = null;

  @ApiModelProperty(value = "the amount of the block")
 /**
   * the amount of the block  
  **/
  private String amount = null;

  @ApiModelProperty(value = "the fee of the transaction")
 /**
   * the fee of the transaction  
  **/
  private String fee = null;

  @ApiModelProperty(value = "Block timestamp in milliseconds specified by the block producer.")
 /**
   * Block timestamp in milliseconds specified by the block producer.  
  **/
  private String timestamp = null;

  @ApiModelProperty(value = "The block producer's address")
 /**
   * The block producer's address  
  **/
  private String address = null;

  @ApiModelProperty(value = "The transaction sender's address")
 /**
   * The transaction sender's address  
  **/
  private String from = null;

  @ApiModelProperty(value = "The transaction receiver's address")
 /**
   * The transaction receiver's address  
  **/
  private String to = null;

  @ApiModelProperty(value = "The block nonce")
 /**
   * The block nonce  
  **/
  private String nonce = null;

  @ApiModelProperty(value = "The random value of the block")
 /**
   * The random value of the block  
  **/
  private String random = null;

  @ApiModelProperty(value = "The number of rule sign")
 /**
   * The number of rule sign  
  **/
  private Integer ruleSignCount = null;

  @ApiModelProperty(value = "The rule sign list")
 /**
   * The rule sign list  
  **/
  private List<SignType> ruleSignList = null;

  @ApiModelProperty(value = "The nodename that package the blocks")
 /**
   * The nodename that package the blocks  
  **/
  private String nodename = null;
 /**
   * Block height.
   * @return height
  **/
  @JsonProperty("height")
 @Pattern(regexp="^\\d+$")  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public BlockType height(String height) {
    this.height = height;
    return this;
  }

 /**
   * Mc Block Reward.
   * @return reward
  **/
  @JsonProperty("reward")
 @Pattern(regexp="^\\d+$")  public String getReward() {
    return reward;
  }

  public void setReward(String reward) {
    this.reward = reward;
  }

  public BlockType reward(String reward) {
    this.reward = reward;
    return this;
  }

 /**
   * Transaction Count In Block
   * @return transactionCount
  **/
  @JsonProperty("transactionCount")
  public Integer getTransactionCount() {
    return transactionCount;
  }

  public void setTransactionCount(Integer transactionCount) {
    this.transactionCount = transactionCount;
  }

  public BlockType transactionCount(Integer transactionCount) {
    this.transactionCount = transactionCount;
    return this;
  }

 /**
   * Time cost to generate the block
   * @return timeCost
  **/
  @JsonProperty("timeCost")
 @Pattern(regexp="^\\d+$")  public String getTimeCost() {
    return timeCost;
  }

  public void setTimeCost(String timeCost) {
    this.timeCost = timeCost;
  }

  public BlockType timeCost(String timeCost) {
    this.timeCost = timeCost;
    return this;
  }

 /**
   * The block hash
   * @return hash
  **/
  @JsonProperty("hash")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{64}$")  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public BlockType hash(String hash) {
    this.hash = hash;
    return this;
  }

 /**
   * Main Block difficulty.
   * @return difficulty
  **/
  @JsonProperty("difficulty")
 @Pattern(regexp="^\\d+$")  public String getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(String difficulty) {
    this.difficulty = difficulty;
  }

  public BlockType difficulty(String difficulty) {
    this.difficulty = difficulty;
    return this;
  }

 /**
   * The block header type
   * @return headtype
  **/
  @JsonProperty("headtype")
  public Integer getHeadtype() {
    return headtype;
  }

  public void setHeadtype(Integer headtype) {
    this.headtype = headtype;
  }

  public BlockType headtype(Integer headtype) {
    this.headtype = headtype;
    return this;
  }

 /**
   * the block type
   * @return btype
  **/
  @JsonProperty("btype")
  public String getBtype() {
    return btype;
  }

  public void setBtype(String btype) {
    this.btype = btype;
  }

  public BlockType btype(String btype) {
    this.btype = btype;
    return this;
  }

 /**
   * the amount of the block
   * @return amount
  **/
  @JsonProperty("amount")
 @Pattern(regexp="^\\d+$")  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public BlockType amount(String amount) {
    this.amount = amount;
    return this;
  }

 /**
   * the fee of the transaction
   * @return fee
  **/
  @JsonProperty("fee")
 @Pattern(regexp="^\\d+$")  public String getFee() {
    return fee;
  }

  public void setFee(String fee) {
    this.fee = fee;
  }

  public BlockType fee(String fee) {
    this.fee = fee;
    return this;
  }

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

  public BlockType timestamp(String timestamp) {
    this.timestamp = timestamp;
    return this;
  }

 /**
   * The block producer&#39;s address
   * @return address
  **/
  @JsonProperty("address")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{40}$")  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public BlockType address(String address) {
    this.address = address;
    return this;
  }

 /**
   * The transaction sender&#39;s address
   * @return from
  **/
  @JsonProperty("from")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{40}$")  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public BlockType from(String from) {
    this.from = from;
    return this;
  }

 /**
   * The transaction receiver&#39;s address
   * @return to
  **/
  @JsonProperty("to")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{40}$")  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public BlockType to(String to) {
    this.to = to;
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

  public BlockType nonce(String nonce) {
    this.nonce = nonce;
    return this;
  }

 /**
   * The random value of the block
   * @return random
  **/
  @JsonProperty("random")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{64}$")  public String getRandom() {
    return random;
  }

  public void setRandom(String random) {
    this.random = random;
  }

  public BlockType random(String random) {
    this.random = random;
    return this;
  }

 /**
   * The number of rule sign
   * @return ruleSignCount
  **/
  @JsonProperty("ruleSignCount")
  public Integer getRuleSignCount() {
    return ruleSignCount;
  }

  public void setRuleSignCount(Integer ruleSignCount) {
    this.ruleSignCount = ruleSignCount;
  }

  public BlockType ruleSignCount(Integer ruleSignCount) {
    this.ruleSignCount = ruleSignCount;
    return this;
  }

 /**
   * The rule sign list
   * @return ruleSignList
  **/
  @JsonProperty("ruleSignList")
  public List<SignType> getRuleSignList() {
    return ruleSignList;
  }

  public void setRuleSignList(List<SignType> ruleSignList) {
    this.ruleSignList = ruleSignList;
  }

  public BlockType ruleSignList(List<SignType> ruleSignList) {
    this.ruleSignList = ruleSignList;
    return this;
  }

  public BlockType addRuleSignListItem(SignType ruleSignListItem) {
    this.ruleSignList.add(ruleSignListItem);
    return this;
  }

 /**
   * The nodename that package the blocks
   * @return nodename
  **/
  @JsonProperty("nodename")
  public String getNodename() {
    return nodename;
  }

  public void setNodename(String nodename) {
    this.nodename = nodename;
  }

  public BlockType nodename(String nodename) {
    this.nodename = nodename;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockType {\n");
    
    sb.append("    height: ").append(toIndentedString(height)).append("\n");
    sb.append("    reward: ").append(toIndentedString(reward)).append("\n");
    sb.append("    transactionCount: ").append(toIndentedString(transactionCount)).append("\n");
    sb.append("    timeCost: ").append(toIndentedString(timeCost)).append("\n");
    sb.append("    hash: ").append(toIndentedString(hash)).append("\n");
    sb.append("    difficulty: ").append(toIndentedString(difficulty)).append("\n");
    sb.append("    headtype: ").append(toIndentedString(headtype)).append("\n");
    sb.append("    btype: ").append(toIndentedString(btype)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    fee: ").append(toIndentedString(fee)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    from: ").append(toIndentedString(from)).append("\n");
    sb.append("    to: ").append(toIndentedString(to)).append("\n");
    sb.append("    nonce: ").append(toIndentedString(nonce)).append("\n");
    sb.append("    random: ").append(toIndentedString(random)).append("\n");
    sb.append("    ruleSignCount: ").append(toIndentedString(ruleSignCount)).append("\n");
    sb.append("    ruleSignList: ").append(toIndentedString(ruleSignList)).append("\n");
    sb.append("    nodename: ").append(toIndentedString(nodename)).append("\n");
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

