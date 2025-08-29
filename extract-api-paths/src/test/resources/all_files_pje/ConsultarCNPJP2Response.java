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
 *         &lt;element name="ConsultarCNPJP2Result" type="{nsProxyRFBCNJ}ArrayOfCNPJPerfil2" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "consultarCNPJP2Result" })
@XmlRootElement(name = "ConsultarCNPJP2Response")
public class ConsultarCNPJP2Response {

	@XmlElement(name = "ConsultarCNPJP2Result")
	protected ArrayOfCNPJPerfil2 consultarCNPJP2Result;

	/**
	 * Gets the value of the consultarCNPJP2Result property.
	 * 
	 * @return possible object is {@link ArrayOfCNPJPerfil2 }
	 * 
	 */
	public ArrayOfCNPJPerfil2 getConsultarCNPJP2Result() {
		return consultarCNPJP2Result;
	}

	/**
	 * Sets the value of the consultarCNPJP2Result property.
	 * 
	 * @param value
	 *            allowed object is {@link ArrayOfCNPJPerfil2 }
	 * 
	 */
	public void setConsultarCNPJP2Result(ArrayOfCNPJPerfil2 value) {
		this.consultarCNPJP2Result = value;
	}

}
