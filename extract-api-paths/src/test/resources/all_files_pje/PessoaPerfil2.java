package br.jus.cnj.pje.webservice.client.consultacpf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for PessoaPerfil2 complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="PessoaPerfil2">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CPF" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Nome" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SituacaoCadastral" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ResidenteExterior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomeMae" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataNascimento" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Sexo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TituloEleitor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AnoObito" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataAtualizacao" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Erro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PessoaPerfil2", propOrder = { "cpf", "nome", "situacaoCadastral", "residenteExterior", "nomeMae",
		"dataNascimento", "sexo", "tituloEleitor", "anoObito", "dataAtualizacao", "erro" })
public class PessoaPerfil2 {

	@XmlElement(name = "CPF")
	protected String cpf;
	@XmlElement(name = "Nome")
	protected String nome;
	@XmlElement(name = "SituacaoCadastral")
	protected String situacaoCadastral;
	@XmlElement(name = "ResidenteExterior")
	protected String residenteExterior;
	@XmlElement(name = "NomeMae")
	protected String nomeMae;
	@XmlElement(name = "DataNascimento")
	protected String dataNascimento;
	@XmlElement(name = "Sexo")
	protected String sexo;
	@XmlElement(name = "TituloEleitor")
	protected String tituloEleitor;
	@XmlElement(name = "AnoObito")
	protected String anoObito;
	@XmlElement(name = "DataAtualizacao")
	protected String dataAtualizacao;
	@XmlElement(name = "Erro")
	protected String erro;

	/**
	 * Gets the value of the cpf property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCPF() {
		return cpf;
	}

	/**
	 * Sets the value of the cpf property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCPF(String value) {
		this.cpf = value;
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
	 * Gets the value of the residenteExterior property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getResidenteExterior() {
		return residenteExterior;
	}

	/**
	 * Sets the value of the residenteExterior property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setResidenteExterior(String value) {
		this.residenteExterior = value;
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
	 * Gets the value of the tituloEleitor property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTituloEleitor() {
		return tituloEleitor;
	}

	/**
	 * Sets the value of the tituloEleitor property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTituloEleitor(String value) {
		this.tituloEleitor = value;
	}

	/**
	 * Gets the value of the anoObito property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAnoObito() {
		return anoObito;
	}

	/**
	 * Sets the value of the anoObito property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAnoObito(String value) {
		this.anoObito = value;
	}

	/**
	 * Gets the value of the dataAtualizacao property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataAtualizacao() {
		return dataAtualizacao;
	}

	/**
	 * Sets the value of the dataAtualizacao property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataAtualizacao(String value) {
		this.dataAtualizacao = value;
	}

	/**
	 * Gets the value of the erro property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getErro() {
		return erro;
	}

	/**
	 * Sets the value of the erro property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setErro(String value) {
		this.erro = value;
	}

}
