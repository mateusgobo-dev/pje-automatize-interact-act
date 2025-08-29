package br.gov.cjf.pj.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for getObjectCNPJSecurity complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="getObjectCNPJSecurity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pNumCNPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pNomeOrgao" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pLoginUsuario" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pNomeAplicacao" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getObjectCNPJSecurity", propOrder = { "pNumCNPJ", "pNomeOrgao", "pLoginUsuario", "pNomeAplicacao" })
public class GetObjectCNPJSecurity {

	@XmlElement(required = true, nillable = true)
	protected String pNumCNPJ;
	@XmlElement(required = true, nillable = true)
	protected String pNomeOrgao;
	@XmlElement(required = true, nillable = true)
	protected String pLoginUsuario;
	@XmlElement(required = true, nillable = true)
	protected String pNomeAplicacao;

	/**
	 * Gets the value of the pNumCNPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPNumCNPJ() {
		return pNumCNPJ;
	}

	/**
	 * Sets the value of the pNumCNPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPNumCNPJ(String value) {
		this.pNumCNPJ = value;
	}

	/**
	 * Gets the value of the pNomeOrgao property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPNomeOrgao() {
		return pNomeOrgao;
	}

	/**
	 * Sets the value of the pNomeOrgao property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPNomeOrgao(String value) {
		this.pNomeOrgao = value;
	}

	/**
	 * Gets the value of the pLoginUsuario property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPLoginUsuario() {
		return pLoginUsuario;
	}

	/**
	 * Sets the value of the pLoginUsuario property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPLoginUsuario(String value) {
		this.pLoginUsuario = value;
	}

	/**
	 * Gets the value of the pNomeAplicacao property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPNomeAplicacao() {
		return pNomeAplicacao;
	}

	/**
	 * Sets the value of the pNomeAplicacao property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPNomeAplicacao(String value) {
		this.pNomeAplicacao = value;
	}

}
