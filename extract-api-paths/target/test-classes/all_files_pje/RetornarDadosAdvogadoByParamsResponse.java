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
 *         &lt;element name="RetornarDadosAdvogadoByParamsResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "retornarDadosAdvogadoByParamsResult" })
@XmlRootElement(name = "RetornarDadosAdvogadoByParamsResponse")
public class RetornarDadosAdvogadoByParamsResponse {

	@XmlElement(name = "RetornarDadosAdvogadoByParamsResult")
	protected String retornarDadosAdvogadoByParamsResult;

	/**
	 * Gets the value of the retornarDadosAdvogadoByParamsResult property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getRetornarDadosAdvogadoByParamsResult() {
		return retornarDadosAdvogadoByParamsResult;
	}

	/**
	 * Sets the value of the retornarDadosAdvogadoByParamsResult property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setRetornarDadosAdvogadoByParamsResult(String value) {
		this.retornarDadosAdvogadoByParamsResult = value;
	}

}
