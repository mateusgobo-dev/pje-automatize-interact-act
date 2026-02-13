package br.jus.cnj.pje.webservice.client.consultacpf;

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
 *         &lt;element name="ConsultarCPFP1Result" type="{nsProxyRFBCNJ}ArrayOfPessoaPerfil1" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "consultarCPFP1Result" })
@XmlRootElement(name = "ConsultarCPFP1Response")
public class ConsultarCPFP1Response {

	@XmlElement(name = "ConsultarCPFP1Result")
	protected ArrayOfPessoaPerfil1 consultarCPFP1Result;

	/**
	 * Gets the value of the consultarCPFP1Result property.
	 * 
	 * @return possible object is {@link ArrayOfPessoaPerfil1 }
	 * 
	 */
	public ArrayOfPessoaPerfil1 getConsultarCPFP1Result() {
		return consultarCPFP1Result;
	}

	/**
	 * Sets the value of the consultarCPFP1Result property.
	 * 
	 * @param value
	 *            allowed object is {@link ArrayOfPessoaPerfil1 }
	 * 
	 */
	public void setConsultarCPFP1Result(ArrayOfPessoaPerfil1 value) {
		this.consultarCPFP1Result = value;
	}

}
