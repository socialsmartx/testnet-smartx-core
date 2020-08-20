package com.smartx.api.v1.model;

import com.smartx.api.v1.model.ApiHandlerResponse;
import com.smartx.api.v1.model.NodeDistType;
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

public class GetGlobalNodeDistInfoResponse extends ApiHandlerResponse {
  
  @ApiModelProperty(value = "The node dist info count")
 /**
   * The node dist info count  
  **/
  private String nodeDistInfoCount = null;

  @ApiModelProperty(value = "The node dist info list")
 /**
   * The node dist info list  
  **/
  private List<NodeDistType> nodeDistInfoList = null;
 /**
   * The node dist info count
   * @return nodeDistInfoCount
  **/
  @JsonProperty("nodeDistInfoCount")
 @Pattern(regexp="^\\d+$")  public String getNodeDistInfoCount() {
    return nodeDistInfoCount;
  }

  public void setNodeDistInfoCount(String nodeDistInfoCount) {
    this.nodeDistInfoCount = nodeDistInfoCount;
  }

  public GetGlobalNodeDistInfoResponse nodeDistInfoCount(String nodeDistInfoCount) {
    this.nodeDistInfoCount = nodeDistInfoCount;
    return this;
  }

 /**
   * The node dist info list
   * @return nodeDistInfoList
  **/
  @JsonProperty("nodeDistInfoList")
  public List<NodeDistType> getNodeDistInfoList() {
    return nodeDistInfoList;
  }

  public void setNodeDistInfoList(List<NodeDistType> nodeDistInfoList) {
    this.nodeDistInfoList = nodeDistInfoList;
  }

  public GetGlobalNodeDistInfoResponse nodeDistInfoList(List<NodeDistType> nodeDistInfoList) {
    this.nodeDistInfoList = nodeDistInfoList;
    return this;
  }

  public GetGlobalNodeDistInfoResponse addNodeDistInfoListItem(NodeDistType nodeDistInfoListItem) {
    this.nodeDistInfoList.add(nodeDistInfoListItem);
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetGlobalNodeDistInfoResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    nodeDistInfoCount: ").append(toIndentedString(nodeDistInfoCount)).append("\n");
    sb.append("    nodeDistInfoList: ").append(toIndentedString(nodeDistInfoList)).append("\n");
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

