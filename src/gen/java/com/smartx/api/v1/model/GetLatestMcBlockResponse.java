package com.smartx.api.v1.model;

import com.smartx.api.v1.model.ApiHandlerResponse;
import com.smartx.api.v1.model.BlockType;
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

public class GetLatestMcBlockResponse extends ApiHandlerResponse {
  
  @ApiModelProperty(value = "The Mc Block count")
 /**
   * The Mc Block count  
  **/
  private String mcBlockCount = null;

  @ApiModelProperty(value = "The Latest Mc Block List")
 /**
   * The Latest Mc Block List  
  **/
  private List<BlockType> mcBlockList = null;
 /**
   * The Mc Block count
   * @return mcBlockCount
  **/
  @JsonProperty("mcBlockCount")
 @Pattern(regexp="^\\d+$")  public String getMcBlockCount() {
    return mcBlockCount;
  }

  public void setMcBlockCount(String mcBlockCount) {
    this.mcBlockCount = mcBlockCount;
  }

  public GetLatestMcBlockResponse mcBlockCount(String mcBlockCount) {
    this.mcBlockCount = mcBlockCount;
    return this;
  }

 /**
   * The Latest Mc Block List
   * @return mcBlockList
  **/
  @JsonProperty("mcBlockList")
  public List<BlockType> getMcBlockList() {
    return mcBlockList;
  }

  public void setMcBlockList(List<BlockType> mcBlockList) {
    this.mcBlockList = mcBlockList;
  }

  public GetLatestMcBlockResponse mcBlockList(List<BlockType> mcBlockList) {
    this.mcBlockList = mcBlockList;
    return this;
  }

  public GetLatestMcBlockResponse addMcBlockListItem(BlockType mcBlockListItem) {
    this.mcBlockList.add(mcBlockListItem);
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetLatestMcBlockResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    mcBlockCount: ").append(toIndentedString(mcBlockCount)).append("\n");
    sb.append("    mcBlockList: ").append(toIndentedString(mcBlockList)).append("\n");
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

