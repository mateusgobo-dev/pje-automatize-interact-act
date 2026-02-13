package br.com.infox.pje.webservices.consultaoab;

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
 *         &lt;element name="ConsultaAdvogadoResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "consultaAdvogadoResult" })
@XmlRootElement(name = "ConsultaAdvogadoResponse")
public class ConsultaAdvogadoResponse {

	@XmlElement(name = "ConsultaAdvogadoResult")
	protected String consultaAdvogadoResult;

	/**
	 * Gets the value of the consultaAdvogadoResult property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getConsultaAdvogadoResult() {
		return consultaAdvogadoResult;
	}

	/**
	 * Sets the value of the consultaAdvogadoResult property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setConsultaAdvogadoResult(String value) {
		this.consultaAdvogadoResult = value;
	}

}
