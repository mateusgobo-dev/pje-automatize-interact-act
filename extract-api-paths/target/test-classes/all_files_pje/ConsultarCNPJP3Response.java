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
 *         &lt;element name="ConsultarCNPJP3Result" type="{nsProxyRFBCNJ}ArrayOfCNPJPerfil3" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "consultarCNPJP3Result" })
@XmlRootElement(name = "ConsultarCNPJP3Response")
public class ConsultarCNPJP3Response {

	@XmlElement(name = "ConsultarCNPJP3Result")
	protected ArrayOfCNPJPerfil3 consultarCNPJP3Result;

	/**
	 * Gets the value of the consultarCNPJP3Result property.
	 * 
	 * @return possible object is {@link ArrayOfCNPJPerfil3 }
	 * 
	 */
	public ArrayOfCNPJPerfil3 getConsultarCNPJP3Result() {
		return consultarCNPJP3Result;
	}

	/**
	 * Sets the value of the consultarCNPJP3Result property.
	 * 
	 * @param value
	 *            allowed object is {@link ArrayOfCNPJPerfil3 }
	 * 
	 */
	public void setConsultarCNPJP3Result(ArrayOfCNPJPerfil3 value) {
		this.consultarCNPJP3Result = value;
	}

}
