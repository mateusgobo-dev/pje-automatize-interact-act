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
 *         &lt;element name="ConsultarCNPJP3TResult" type="{nsProxyRFBCNJ}ArrayOfCNPJPerfil3" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "consultarCNPJP3TResult" })
@XmlRootElement(name = "ConsultarCNPJP3TResponse")
public class ConsultarCNPJP3TResponse {

	@XmlElement(name = "ConsultarCNPJP3TResult")
	protected ArrayOfCNPJPerfil3 consultarCNPJP3TResult;

	/**
	 * Gets the value of the consultarCNPJP3TResult property.
	 * 
	 * @return possible object is {@link ArrayOfCNPJPerfil3 }
	 * 
	 */
	public ArrayOfCNPJPerfil3 getConsultarCNPJP3TResult() {
		return consultarCNPJP3TResult;
	}

	/**
	 * Sets the value of the consultarCNPJP3TResult property.
	 * 
	 * @param value
	 *            allowed object is {@link ArrayOfCNPJPerfil3 }
	 * 
	 */
	public void setConsultarCNPJP3TResult(ArrayOfCNPJPerfil3 value) {
		this.consultarCNPJP3TResult = value;
	}

}
