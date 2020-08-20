package com.smartx.api.v1.model;

import com.smartx.api.v1.model.ApiHandlerResponse;
import com.smartx.api.v1.model.TransactionType;
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

public class GetAccountTransactionsResponse extends ApiHandlerResponse {
  
  @ApiModelProperty(value = "The transaction count")
 /**
   * The transaction count  
  **/
  private Integer transactionCount = null;

  @ApiModelProperty(value = "The transaction list")
 /**
   * The transaction list  
  **/
  private List<TransactionType> transactionList = null;
 /**
   * The transaction count
   * @return transactionCount
  **/
  @JsonProperty("transactionCount")
  public Integer getTransactionCount() {
    return transactionCount;
  }

  public void setTransactionCount(Integer transactionCount) {
    this.transactionCount = transactionCount;
  }

  public GetAccountTransactionsResponse transactionCount(Integer transactionCount) {
    this.transactionCount = transactionCount;
    return this;
  }

 /**
   * The transaction list
   * @return transactionList
  **/
  @JsonProperty("transactionList")
  public List<TransactionType> getTransactionList() {
    return transactionList;
  }

  public void setTransactionList(List<TransactionType> transactionList) {
    this.transactionList = transactionList;
  }

  public GetAccountTransactionsResponse transactionList(List<TransactionType> transactionList) {
    this.transactionList = transactionList;
    return this;
  }

  public GetAccountTransactionsResponse addTransactionListItem(TransactionType transactionListItem) {
    this.transactionList.add(transactionListItem);
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetAccountTransactionsResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    transactionCount: ").append(toIndentedString(transactionCount)).append("\n");
    sb.append("    transactionList: ").append(toIndentedString(transactionList)).append("\n");
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

