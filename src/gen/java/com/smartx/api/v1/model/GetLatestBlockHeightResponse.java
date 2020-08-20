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

public class GetLatestBlockHeightResponse extends ApiHandlerResponse {
  
  @ApiModelProperty(value = "The latest block height")
 /**
   * The latest block height  
  **/
  private String height = null;
 /**
   * The latest block height
   * @return height
  **/
  @JsonProperty("height")
 @Pattern(regexp="^\\d+$")  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public GetLatestBlockHeightResponse height(String height) {
    this.height = height;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetLatestBlockHeightResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    height: ").append(toIndentedString(height)).append("\n");
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

