package com.smartx.api.v1.model;

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

public class InfoType  {
  
  @ApiModelProperty(value = "The connected network")
 /**
   * The connected network  
  **/
  private String network = null;

  @ApiModelProperty(value = "The features supported")
 /**
   * The features supported  
  **/
  private List<String> capabilities = null;

  @ApiModelProperty(value = "The client identifier string")
 /**
   * The client identifier string  
  **/
  private String clientId = null;

  @ApiModelProperty(value = "The address used for establishing connections to the network")
 /**
   * The address used for establishing connections to the network  
  **/
  private String coinbase = null;

  @ApiModelProperty(value = "The number of the last block")
 /**
   * The number of the last block  
  **/
  private String latestBlockNumber = null;

  @ApiModelProperty(value = "The hash of the last block")
 /**
   * The hash of the last block  
  **/
  private String latestBlockHash = null;

  @ApiModelProperty(value = "The number of actively connected peers")
 /**
   * The number of actively connected peers  
  **/
  private Integer activePeers = null;

  @ApiModelProperty(value = "The number of transactions in pending pool")
 /**
   * The number of transactions in pending pool  
  **/
  private Integer pendingTransactions = null;
 /**
   * The connected network
   * @return network
  **/
  @JsonProperty("network")
  public String getNetwork() {
    return network;
  }

  public void setNetwork(String network) {
    this.network = network;
  }

  public InfoType network(String network) {
    this.network = network;
    return this;
  }

 /**
   * The features supported
   * @return capabilities
  **/
  @JsonProperty("capabilities")
  public List<String> getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(List<String> capabilities) {
    this.capabilities = capabilities;
  }

  public InfoType capabilities(List<String> capabilities) {
    this.capabilities = capabilities;
    return this;
  }

  public InfoType addCapabilitiesItem(String capabilitiesItem) {
    this.capabilities.add(capabilitiesItem);
    return this;
  }

 /**
   * The client identifier string
   * @return clientId
  **/
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public InfoType clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

 /**
   * The address used for establishing connections to the network
   * @return coinbase
  **/
  @JsonProperty("coinbase")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{40}$")  public String getCoinbase() {
    return coinbase;
  }

  public void setCoinbase(String coinbase) {
    this.coinbase = coinbase;
  }

  public InfoType coinbase(String coinbase) {
    this.coinbase = coinbase;
    return this;
  }

 /**
   * The number of the last block
   * @return latestBlockNumber
  **/
  @JsonProperty("latestBlockNumber")
 @Pattern(regexp="^\\d+$")  public String getLatestBlockNumber() {
    return latestBlockNumber;
  }

  public void setLatestBlockNumber(String latestBlockNumber) {
    this.latestBlockNumber = latestBlockNumber;
  }

  public InfoType latestBlockNumber(String latestBlockNumber) {
    this.latestBlockNumber = latestBlockNumber;
    return this;
  }

 /**
   * The hash of the last block
   * @return latestBlockHash
  **/
  @JsonProperty("latestBlockHash")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{64}$")  public String getLatestBlockHash() {
    return latestBlockHash;
  }

  public void setLatestBlockHash(String latestBlockHash) {
    this.latestBlockHash = latestBlockHash;
  }

  public InfoType latestBlockHash(String latestBlockHash) {
    this.latestBlockHash = latestBlockHash;
    return this;
  }

 /**
   * The number of actively connected peers
   * @return activePeers
  **/
  @JsonProperty("activePeers")
  public Integer getActivePeers() {
    return activePeers;
  }

  public void setActivePeers(Integer activePeers) {
    this.activePeers = activePeers;
  }

  public InfoType activePeers(Integer activePeers) {
    this.activePeers = activePeers;
    return this;
  }

 /**
   * The number of transactions in pending pool
   * @return pendingTransactions
  **/
  @JsonProperty("pendingTransactions")
  public Integer getPendingTransactions() {
    return pendingTransactions;
  }

  public void setPendingTransactions(Integer pendingTransactions) {
    this.pendingTransactions = pendingTransactions;
  }

  public InfoType pendingTransactions(Integer pendingTransactions) {
    this.pendingTransactions = pendingTransactions;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InfoType {\n");
    
    sb.append("    network: ").append(toIndentedString(network)).append("\n");
    sb.append("    capabilities: ").append(toIndentedString(capabilities)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    coinbase: ").append(toIndentedString(coinbase)).append("\n");
    sb.append("    latestBlockNumber: ").append(toIndentedString(latestBlockNumber)).append("\n");
    sb.append("    latestBlockHash: ").append(toIndentedString(latestBlockHash)).append("\n");
    sb.append("    activePeers: ").append(toIndentedString(activePeers)).append("\n");
    sb.append("    pendingTransactions: ").append(toIndentedString(pendingTransactions)).append("\n");
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

