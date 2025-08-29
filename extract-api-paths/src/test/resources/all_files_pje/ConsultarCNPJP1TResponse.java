package br.jus.cnj.pje.webservice.client.consultacnpj;

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
 *         &lt;element name="ConsultarCNPJP1TResult" type="{nsProxyRFBCNJ}ArrayOfCNPJPerfil1" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "consultarCNPJP1TResult" })
@XmlRootElement(name = "ConsultarCNPJP1TResponse")
public class ConsultarCNPJP1TResponse {

	@XmlElement(name = "ConsultarCNPJP1TResult")
	protected ArrayOfCNPJPerfil1 consultarCNPJP1TResult;

	/**
	 * Gets the value of the consultarCNPJP1TResult property.
	 * 
	 * @return possible object is {@link ArrayOfCNPJPerfil1 }
	 * 
	 */
	public ArrayOfCNPJPerfil1 getConsultarCNPJP1TResult() {
		return consultarCNPJP1TResult;
	}

	/**
	 * Sets the value of the consultarCNPJP1TResult property.
	 * 
	 * @param value
	 *            allowed object is {@link ArrayOfCNPJPerfil1 }
	 * 
	 */
	public void setConsultarCNPJP1TResult(ArrayOfCNPJPerfil1 value) {
		this.consultarCNPJP1TResult = value;
	}

}
