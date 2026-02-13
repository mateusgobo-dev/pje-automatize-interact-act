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
 *         &lt;element name="RetornaImgsAdvogadoResult" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "retornaImgsAdvogadoResult" })
@XmlRootElement(name = "RetornaImgsAdvogadoResponse")
public class RetornaImgsAdvogadoResponse {

	@XmlElement(name = "RetornaImgsAdvogadoResult")
	protected byte[] retornaImgsAdvogadoResult;

	/**
	 * Gets the value of the retornaImgsAdvogadoResult property.
	 * 
	 * @return possible object is byte[]
	 */
	public byte[] getRetornaImgsAdvogadoResult() {
		return retornaImgsAdvogadoResult;
	}

	/**
	 * Sets the value of the retornaImgsAdvogadoResult property.
	 * 
	 * @param value
	 *            allowed object is byte[]
	 */
	public void setRetornaImgsAdvogadoResult(byte[] value) {
		this.retornaImgsAdvogadoResult = value;
	}

}
