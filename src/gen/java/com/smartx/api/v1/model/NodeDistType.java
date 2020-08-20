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

public class NodeDistType  {
  
  @ApiModelProperty(value = "The Country Name")
 /**
   * The Country Name  
  **/
  private String country = null;

  @ApiModelProperty(value = "The Node Count In The Country ")
 /**
   * The Node Count In The Country   
  **/
  private String nodeCount = null;
 /**
   * The Country Name
   * @return country
  **/
  @JsonProperty("country")
  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public NodeDistType country(String country) {
    this.country = country;
    return this;
  }

 /**
   * The Node Count In The Country 
   * @return nodeCount
  **/
  @JsonProperty("nodeCount")
 @Pattern(regexp="^\\d+$")  public String getNodeCount() {
    return nodeCount;
  }

  public void setNodeCount(String nodeCount) {
    this.nodeCount = nodeCount;
  }

  public NodeDistType nodeCount(String nodeCount) {
    this.nodeCount = nodeCount;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NodeDistType {\n");
    
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
    sb.append("    nodeCount: ").append(toIndentedString(nodeCount)).append("\n");
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

