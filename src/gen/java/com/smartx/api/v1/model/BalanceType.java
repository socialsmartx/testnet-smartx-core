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

public class BalanceType  {
  
  @ApiModelProperty(value = "The address of this address")
 /**
   * The address of this address  
  **/
  private String address = null;

  @ApiModelProperty(value = "The available balance of this address")
 /**
   * The available balance of this address  
  **/
  private String available = null;
 /**
   * The address of this address
   * @return address
  **/
  @JsonProperty("address")
 @Pattern(regexp="^(0x)?[0-9a-fA-F]{40}$")  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public BalanceType address(String address) {
    this.address = address;
    return this;
  }

 /**
   * The available balance of this address
   * @return available
  **/
  @JsonProperty("available")
 @Pattern(regexp="^\\d+$")  public String getAvailable() {
    return available;
  }

  public void setAvailable(String available) {
    this.available = available;
  }

  public BalanceType available(String available) {
    this.available = available;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BalanceType {\n");
    
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    available: ").append(toIndentedString(available)).append("\n");
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

