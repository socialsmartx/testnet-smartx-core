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

public class SignType  {
  
  @ApiModelProperty(value = "signer address.")
 /**
   * signer address.  
  **/
  private String address = null;

  @ApiModelProperty(value = "ecdsa sign.")
 /**
   * ecdsa sign.  
  **/
  private String sign = null;
 /**
   * signer address.
   * @return address
  **/
  @JsonProperty("address")
 @Pattern(regexp="^\\d+$")  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public SignType address(String address) {
    this.address = address;
    return this;
  }

 /**
   * ecdsa sign.
   * @return sign
  **/
  @JsonProperty("sign")
 @Pattern(regexp="^\\d+$")  public String getSign() {
    return sign;
  }

  public void setSign(String sign) {
    this.sign = sign;
  }

  public SignType sign(String sign) {
    this.sign = sign;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SignType {\n");
    
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    sign: ").append(toIndentedString(sign)).append("\n");
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

