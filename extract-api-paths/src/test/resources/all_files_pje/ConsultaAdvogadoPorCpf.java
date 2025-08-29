package br.com.infox.pje.webservices.consultaoab;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="cpf" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "cpf" })
@XmlRootElement(name = "ConsultaAdvogadoPorCpf")
public class ConsultaAdvogadoPorCpf {

	protected String cpf;

	/**
	 * Gets the value of the cpf property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCpf() {
		return cpf;
	}

	/**
	 * Sets the value of the cpf property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCpf(String value) {
		this.cpf = value;
	}

}
