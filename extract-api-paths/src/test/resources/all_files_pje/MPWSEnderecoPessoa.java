package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for enderecoPessoa complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="enderecoPessoa">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bairro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cep" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="cidade" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="complemento" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="logradouro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numero" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="pais" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="uf" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "enderecoPessoa", propOrder = { "bairro", "cep", "cidade", "complemento", "logradouro", "numero",
		"pais", "uf" })
public class MPWSEnderecoPessoa {

	protected String bairro;
	protected Long cep;
	protected String cidade;
	protected String complemento;
	protected String logradouro;
	protected Long numero;
	protected String pais;
	protected String uf;

	/**
	 * Gets the value of the bairro property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBairro() {
		return bairro;
	}

	/**
	 * Sets the value of the bairro property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBairro(String value) {
		this.bairro = value;
	}

	/**
	 * Gets the value of the cep property.
	 * 
	 * @return possible object is {@link Long }
	 * 
	 */
	public Long getCep() {
		return cep;
	}

	/**
	 * Sets the value of the cep property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setCep(Long value) {
		this.cep = value;
	}

	/**
	 * Gets the value of the cidade property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCidade() {
		return cidade;
	}

	/**
	 * Sets the value of the cidade property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCidade(String value) {
		this.cidade = value;
	}

	/**
	 * Gets the value of the complemento property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getComplemento() {
		return complemento;
	}

	/**
	 * Sets the value of the complemento property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setComplemento(String value) {
		this.complemento = value;
	}

	/**
	 * Gets the value of the logradouro property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLogradouro() {
		return logradouro;
	}

	/**
	 * Sets the value of the logradouro property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLogradouro(String value) {
		this.logradouro = value;
	}

	/**
	 * Gets the value of the numero property.
	 * 
	 * @return possible object is {@link Long }
	 * 
	 */
	public Long getNumero() {
		return numero;
	}

	/**
	 * Sets the value of the numero property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setNumero(Long value) {
		this.numero = value;
	}

	/**
	 * Gets the value of the pais property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPais() {
		return pais;
	}

	/**
	 * Sets the value of the pais property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPais(String value) {
		this.pais = value;
	}

	/**
	 * Gets the value of the uf property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUf() {
		return uf;
	}

	/**
	 * Sets the value of the uf property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUf(String value) {
		this.uf = value;
	}

}
