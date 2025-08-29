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
 *         &lt;element name="ConsultarCNPJP1Result" type="{nsProxyRFBCNJ}ArrayOfCNPJPerfil1" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "consultarCNPJP1Result" })
@XmlRootElement(name = "ConsultarCNPJP1Response")
public class ConsultarCNPJP1Response {

	@XmlElement(name = "ConsultarCNPJP1Result")
	protected ArrayOfCNPJPerfil1 consultarCNPJP1Result;

	/**
	 * Gets the value of the consultarCNPJP1Result property.
	 * 
	 * @return possible object is {@link ArrayOfCNPJPerfil1 }
	 * 
	 */
	public ArrayOfCNPJPerfil1 getConsultarCNPJP1Result() {
		return consultarCNPJP1Result;
	}

	/**
	 * Sets the value of the consultarCNPJP1Result property.
	 * 
	 * @param value
	 *            allowed object is {@link ArrayOfCNPJPerfil1 }
	 * 
	 */
	public void setConsultarCNPJP1Result(ArrayOfCNPJPerfil1 value) {
		this.consultarCNPJP1Result = value;
	}

}
