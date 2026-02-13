package br.org.oab.www5.consultanacionalws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CPFResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "cpfResult" })
@XmlRootElement(name = "CPFResponse")
public class CPFResponse {

	@XmlElement(name = "CPFResult")
	protected String cpfResult;

	/**
	 * Gets the value of the cpfResult property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCPFResult() {
		return cpfResult;
	}

	/**
	 * Sets the value of the cpfResult property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCPFResult(String value) {
		this.cpfResult = value;
	}

}
