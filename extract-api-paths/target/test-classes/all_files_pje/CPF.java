package br.gov.cjf.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for CPF complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CPF">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bairro" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nome" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="numCEP" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="complemento" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="numLogradouro" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sexo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="logradouro" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="numCPF" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nomeMae" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="numTituloEleitor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="situacaoCadastral" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="siglaUF" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="municipio" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tipoLogradouro" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dataNascimento" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CPF", propOrder = { "bairro", "nome", "numCEP", "complemento", "numLogradouro", "sexo", "logradouro",
		"numCPF", "nomeMae", "numTituloEleitor", "situacaoCadastral", "siglaUF", "municipio", "tipoLogradouro",
		"dataNascimento" })
public class CPF {

	@XmlElement(required = true, nillable = true)
	protected String bairro;
	@XmlElement(required = true, nillable = true)
	protected String nome;
	@XmlElement(required = true, nillable = true)
	protected String numCEP;
	@XmlElement(required = true, nillable = true)
	protected String complemento;
	@XmlElement(required = true, nillable = true)
	protected String numLogradouro;
	@XmlElement(required = true, nillable = true)
	protected String sexo;
	@XmlElement(required = true, nillable = true)
	protected String logradouro;
	@XmlElement(required = true, nillable = true)
	protected String numCPF;
	@XmlElement(required = true, nillable = true)
	protected String nomeMae;
	@XmlElement(required = true, nillable = true)
	protected String numTituloEleitor;
	@XmlElement(required = true, nillable = true)
	protected String situacaoCadastral;
	@XmlElement(required = true, nillable = true)
	protected String siglaUF;
	@XmlElement(required = true, nillable = true)
	protected String municipio;
	@XmlElement(required = true, nillable = true)
	protected String tipoLogradouro;
	@XmlElement(required = true, nillable = true)
	protected String dataNascimento;

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
	 * Gets the value of the nome property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Sets the value of the nome property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNome(String value) {
		this.nome = value;
	}

	/**
	 * Gets the value of the numCEP property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumCEP() {
		return numCEP;
	}

	/**
	 * Sets the value of the numCEP property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumCEP(String value) {
		this.numCEP = value;
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
	 * Gets the value of the numLogradouro property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumLogradouro() {
		return numLogradouro;
	}

	/**
	 * Sets the value of the numLogradouro property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumLogradouro(String value) {
		this.numLogradouro = value;
	}

	/**
	 * Gets the value of the sexo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSexo() {
		return sexo;
	}

	/**
	 * Sets the value of the sexo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSexo(String value) {
		this.sexo = value;
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
	 * Gets the value of the numCPF property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumCPF() {
		return numCPF;
	}

	/**
	 * Sets the value of the numCPF property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumCPF(String value) {
		this.numCPF = value;
	}

	/**
	 * Gets the value of the nomeMae property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeMae() {
		return nomeMae;
	}

	/**
	 * Sets the value of the nomeMae property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeMae(String value) {
		this.nomeMae = value;
	}

	/**
	 * Gets the value of the numTituloEleitor property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumTituloEleitor() {
		return numTituloEleitor;
	}

	/**
	 * Sets the value of the numTituloEleitor property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumTituloEleitor(String value) {
		this.numTituloEleitor = value;
	}

	/**
	 * Gets the value of the situacaoCadastral property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSituacaoCadastral() {
		return situacaoCadastral;
	}

	/**
	 * Sets the value of the situacaoCadastral property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSituacaoCadastral(String value) {
		this.situacaoCadastral = value;
	}

	/**
	 * Gets the value of the siglaUF property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSiglaUF() {
		return siglaUF;
	}

	/**
	 * Sets the value of the siglaUF property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSiglaUF(String value) {
		this.siglaUF = value;
	}

	/**
	 * Gets the value of the municipio property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMunicipio() {
		return municipio;
	}

	/**
	 * Sets the value of the municipio property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMunicipio(String value) {
		this.municipio = value;
	}

	/**
	 * Gets the value of the tipoLogradouro property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTipoLogradouro() {
		return tipoLogradouro;
	}

	/**
	 * Sets the value of the tipoLogradouro property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTipoLogradouro(String value) {
		this.tipoLogradouro = value;
	}

	/**
	 * Gets the value of the dataNascimento property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataNascimento() {
		return dataNascimento;
	}

	/**
	 * Sets the value of the dataNascimento property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataNascimento(String value) {
		this.dataNascimento = value;
	}

}
