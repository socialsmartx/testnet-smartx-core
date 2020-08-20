package com.smartx.api.v1.model;

import com.smartx.api.v1.model.ApiHandlerResponse;
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

public class GetGlobalInfoResponse extends ApiHandlerResponse {
  
  @ApiModelProperty(value = "The global hash rate")
 /**
   * The global hash rate  
  **/
  private String globalHashRate = null;

  @ApiModelProperty(value = "The global hash rate change percent")
 /**
   * The global hash rate change percent  
  **/
  private String globalHashRatePer = null;

  @ApiModelProperty(value = "The global block time cost")
 /**
   * The global block time cost  
  **/
  private String globalBlockTimeCost = null;

  @ApiModelProperty(value = "The global block time cost change percent")
 /**
   * The global block time cost change percent  
  **/
  private String globalBlockTimeCostPer = null;

  @ApiModelProperty(value = "The global block height")
 /**
   * The global block height  
  **/
  private String currentBlockHeight = null;

  @ApiModelProperty(value = "The global block height change percent")
 /**
   * The global block height change percent  
  **/
  private String currentBlockHeightPer = null;

  @ApiModelProperty(value = "The global difficulty")
 /**
   * The global difficulty  
  **/
  private String globalDifficulty = null;

  @ApiModelProperty(value = "The global difficulty  change percent")
 /**
   * The global difficulty  change percent  
  **/
  private String globalDifficultyPer = null;

  @ApiModelProperty(value = "The global transaction count")
 /**
   * The global transaction count  
  **/
  private String globalTransactionCount = null;

  @ApiModelProperty(value = "The global transaction count change percent")
 /**
   * The global transaction count change percent  
  **/
  private String globalTransactionCountPer = null;
 /**
   * The global hash rate
   * @return globalHashRate
  **/
  @JsonProperty("globalHashRate")
  public String getGlobalHashRate() {
    return globalHashRate;
  }

  public void setGlobalHashRate(String globalHashRate) {
    this.globalHashRate = globalHashRate;
  }

  public GetGlobalInfoResponse globalHashRate(String globalHashRate) {
    this.globalHashRate = globalHashRate;
    return this;
  }

 /**
   * The global hash rate change percent
   * @return globalHashRatePer
  **/
  @JsonProperty("globalHashRatePer")
  public String getGlobalHashRatePer() {
    return globalHashRatePer;
  }

  public void setGlobalHashRatePer(String globalHashRatePer) {
    this.globalHashRatePer = globalHashRatePer;
  }

  public GetGlobalInfoResponse globalHashRatePer(String globalHashRatePer) {
    this.globalHashRatePer = globalHashRatePer;
    return this;
  }

 /**
   * The global block time cost
   * @return globalBlockTimeCost
  **/
  @JsonProperty("globalBlockTimeCost")
  public String getGlobalBlockTimeCost() {
    return globalBlockTimeCost;
  }

  public void setGlobalBlockTimeCost(String globalBlockTimeCost) {
    this.globalBlockTimeCost = globalBlockTimeCost;
  }

  public GetGlobalInfoResponse globalBlockTimeCost(String globalBlockTimeCost) {
    this.globalBlockTimeCost = globalBlockTimeCost;
    return this;
  }

 /**
   * The global block time cost change percent
   * @return globalBlockTimeCostPer
  **/
  @JsonProperty("globalBlockTimeCostPer")
  public String getGlobalBlockTimeCostPer() {
    return globalBlockTimeCostPer;
  }

  public void setGlobalBlockTimeCostPer(String globalBlockTimeCostPer) {
    this.globalBlockTimeCostPer = globalBlockTimeCostPer;
  }

  public GetGlobalInfoResponse globalBlockTimeCostPer(String globalBlockTimeCostPer) {
    this.globalBlockTimeCostPer = globalBlockTimeCostPer;
    return this;
  }

 /**
   * The global block height
   * @return currentBlockHeight
  **/
  @JsonProperty("currentBlockHeight")
  public String getCurrentBlockHeight() {
    return currentBlockHeight;
  }

  public void setCurrentBlockHeight(String currentBlockHeight) {
    this.currentBlockHeight = currentBlockHeight;
  }

  public GetGlobalInfoResponse currentBlockHeight(String currentBlockHeight) {
    this.currentBlockHeight = currentBlockHeight;
    return this;
  }

 /**
   * The global block height change percent
   * @return currentBlockHeightPer
  **/
  @JsonProperty("currentBlockHeightPer")
  public String getCurrentBlockHeightPer() {
    return currentBlockHeightPer;
  }

  public void setCurrentBlockHeightPer(String currentBlockHeightPer) {
    this.currentBlockHeightPer = currentBlockHeightPer;
  }

  public GetGlobalInfoResponse currentBlockHeightPer(String currentBlockHeightPer) {
    this.currentBlockHeightPer = currentBlockHeightPer;
    return this;
  }

 /**
   * The global difficulty
   * @return globalDifficulty
  **/
  @JsonProperty("globalDifficulty")
  public String getGlobalDifficulty() {
    return globalDifficulty;
  }

  public void setGlobalDifficulty(String globalDifficulty) {
    this.globalDifficulty = globalDifficulty;
  }

  public GetGlobalInfoResponse globalDifficulty(String globalDifficulty) {
    this.globalDifficulty = globalDifficulty;
    return this;
  }

 /**
   * The global difficulty  change percent
   * @return globalDifficultyPer
  **/
  @JsonProperty("globalDifficultyPer")
  public String getGlobalDifficultyPer() {
    return globalDifficultyPer;
  }

  public void setGlobalDifficultyPer(String globalDifficultyPer) {
    this.globalDifficultyPer = globalDifficultyPer;
  }

  public GetGlobalInfoResponse globalDifficultyPer(String globalDifficultyPer) {
    this.globalDifficultyPer = globalDifficultyPer;
    return this;
  }

 /**
   * The global transaction count
   * @return globalTransactionCount
  **/
  @JsonProperty("globalTransactionCount")
  public String getGlobalTransactionCount() {
    return globalTransactionCount;
  }

  public void setGlobalTransactionCount(String globalTransactionCount) {
    this.globalTransactionCount = globalTransactionCount;
  }

  public GetGlobalInfoResponse globalTransactionCount(String globalTransactionCount) {
    this.globalTransactionCount = globalTransactionCount;
    return this;
  }

 /**
   * The global transaction count change percent
   * @return globalTransactionCountPer
  **/
  @JsonProperty("globalTransactionCountPer")
  public String getGlobalTransactionCountPer() {
    return globalTransactionCountPer;
  }

  public void setGlobalTransactionCountPer(String globalTransactionCountPer) {
    this.globalTransactionCountPer = globalTransactionCountPer;
  }

  public GetGlobalInfoResponse globalTransactionCountPer(String globalTransactionCountPer) {
    this.globalTransactionCountPer = globalTransactionCountPer;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetGlobalInfoResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    globalHashRate: ").append(toIndentedString(globalHashRate)).append("\n");
    sb.append("    globalHashRatePer: ").append(toIndentedString(globalHashRatePer)).append("\n");
    sb.append("    globalBlockTimeCost: ").append(toIndentedString(globalBlockTimeCost)).append("\n");
    sb.append("    globalBlockTimeCostPer: ").append(toIndentedString(globalBlockTimeCostPer)).append("\n");
    sb.append("    currentBlockHeight: ").append(toIndentedString(currentBlockHeight)).append("\n");
    sb.append("    currentBlockHeightPer: ").append(toIndentedString(currentBlockHeightPer)).append("\n");
    sb.append("    globalDifficulty: ").append(toIndentedString(globalDifficulty)).append("\n");
    sb.append("    globalDifficultyPer: ").append(toIndentedString(globalDifficultyPer)).append("\n");
    sb.append("    globalTransactionCount: ").append(toIndentedString(globalTransactionCount)).append("\n");
    sb.append("    globalTransactionCountPer: ").append(toIndentedString(globalTransactionCountPer)).append("\n");
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

