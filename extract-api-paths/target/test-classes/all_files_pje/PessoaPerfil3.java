package br.jus.cnj.pje.webservice.client.consultacpf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for PessoaPerfil3 complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="PessoaPerfil3">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CPF" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Nome" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SituacaoCadastral" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ResidenteExterior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CodigoPaisExterior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomePaisExterior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomeMae" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataNascimento" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Sexo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NaturezaOcupacao" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OcupacaoPrincipal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ExercicioOcupacao" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TipoLogradouro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Logradouro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NumeroLogradouro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Complemento" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CEP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Bairro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CodigoMunicipio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Municipio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UF" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DDD" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Telefone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UnidadeAdministrativa" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AnoObito" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Estrangeiro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataAtualizacao" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TituloEleitor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "PessoaPerfil3", propOrder = { "cpf", "nome", "situacaoCadastral", "residenteExterior",
		"codigoPaisExterior", "nomePaisExterior", "nomeMae", "dataNascimento", "sexo", "naturezaOcupacao",
		"ocupacaoPrincipal", "exercicioOcupacao", "tipoLogradouro", "logradouro", "numeroLogradouro", "complemento",
		"cep", "bairro", "codigoMunicipio", "municipio", "uf", "ddd", "telefone", "unidadeAdministrativa", "anoObito",
		"estrangeiro", "dataAtualizacao", "tituloEleitor", "erro" })
public class PessoaPerfil3 {

	@XmlElement(name = "CPF")
	protected String cpf;
	@XmlElement(name = "Nome")
	protected String nome;
	@XmlElement(name = "SituacaoCadastral")
	protected String situacaoCadastral;
	@XmlElement(name = "ResidenteExterior")
	protected String residenteExterior;
	@XmlElement(name = "CodigoPaisExterior")
	protected String codigoPaisExterior;
	@XmlElement(name = "NomePaisExterior")
	protected String nomePaisExterior;
	@XmlElement(name = "NomeMae")
	protected String nomeMae;
	@XmlElement(name = "DataNascimento")
	protected String dataNascimento;
	@XmlElement(name = "Sexo")
	protected String sexo;
	@XmlElement(name = "NaturezaOcupacao")
	protected String naturezaOcupacao;
	@XmlElement(name = "OcupacaoPrincipal")
	protected String ocupacaoPrincipal;
	@XmlElement(name = "ExercicioOcupacao")
	protected String exercicioOcupacao;
	@XmlElement(name = "TipoLogradouro")
	protected String tipoLogradouro;
	@XmlElement(name = "Logradouro")
	protected String logradouro;
	@XmlElement(name = "NumeroLogradouro")
	protected String numeroLogradouro;
	@XmlElement(name = "Complemento")
	protected String complemento;
	@XmlElement(name = "CEP")
	protected String cep;
	@XmlElement(name = "Bairro")
	protected String bairro;
	@XmlElement(name = "CodigoMunicipio")
	protected String codigoMunicipio;
	@XmlElement(name = "Municipio")
	protected String municipio;
	@XmlElement(name = "UF")
	protected String uf;
	@XmlElement(name = "DDD")
	protected String ddd;
	@XmlElement(name = "Telefone")
	protected String telefone;
	@XmlElement(name = "UnidadeAdministrativa")
	protected String unidadeAdministrativa;
	@XmlElement(name = "AnoObito")
	protected String anoObito;
	@XmlElement(name = "Estrangeiro")
	protected String estrangeiro;
	@XmlElement(name = "DataAtualizacao")
	protected String dataAtualizacao;
	@XmlElement(name = "TituloEleitor")
	protected String tituloEleitor;
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
	 * Gets the value of the codigoPaisExterior property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodigoPaisExterior() {
		return codigoPaisExterior;
	}

	/**
	 * Sets the value of the codigoPaisExterior property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodigoPaisExterior(String value) {
		this.codigoPaisExterior = value;
	}

	/**
	 * Gets the value of the nomePaisExterior property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomePaisExterior() {
		return nomePaisExterior;
	}

	/**
	 * Sets the value of the nomePaisExterior property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomePaisExterior(String value) {
		this.nomePaisExterior = value;
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
	 * Gets the value of the naturezaOcupacao property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNaturezaOcupacao() {
		return naturezaOcupacao;
	}

	/**
	 * Sets the value of the naturezaOcupacao property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNaturezaOcupacao(String value) {
		this.naturezaOcupacao = value;
	}

	/**
	 * Gets the value of the ocupacaoPrincipal property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOcupacaoPrincipal() {
		return ocupacaoPrincipal;
	}

	/**
	 * Sets the value of the ocupacaoPrincipal property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOcupacaoPrincipal(String value) {
		this.ocupacaoPrincipal = value;
	}

	/**
	 * Gets the value of the exercicioOcupacao property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getExercicioOcupacao() {
		return exercicioOcupacao;
	}

	/**
	 * Sets the value of the exercicioOcupacao property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setExercicioOcupacao(String value) {
		this.exercicioOcupacao = value;
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
	 * Gets the value of the numeroLogradouro property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumeroLogradouro() {
		return numeroLogradouro;
	}

	/**
	 * Sets the value of the numeroLogradouro property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumeroLogradouro(String value) {
		this.numeroLogradouro = value;
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
	 * Gets the value of the cep property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCEP() {
		return cep;
	}

	/**
	 * Sets the value of the cep property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCEP(String value) {
		this.cep = value;
	}

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
	 * Gets the value of the codigoMunicipio property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodigoMunicipio() {
		return codigoMunicipio;
	}

	/**
	 * Sets the value of the codigoMunicipio property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodigoMunicipio(String value) {
		this.codigoMunicipio = value;
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
	 * Gets the value of the uf property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUF() {
		return uf;
	}

	/**
	 * Sets the value of the uf property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUF(String value) {
		this.uf = value;
	}

	/**
	 * Gets the value of the ddd property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDDD() {
		return ddd;
	}

	/**
	 * Sets the value of the ddd property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDDD(String value) {
		this.ddd = value;
	}

	/**
	 * Gets the value of the telefone property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTelefone() {
		return telefone;
	}

	/**
	 * Sets the value of the telefone property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTelefone(String value) {
		this.telefone = value;
	}

	/**
	 * Gets the value of the unidadeAdministrativa property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUnidadeAdministrativa() {
		return unidadeAdministrativa;
	}

	/**
	 * Sets the value of the unidadeAdministrativa property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUnidadeAdministrativa(String value) {
		this.unidadeAdministrativa = value;
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
	 * Gets the value of the estrangeiro property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEstrangeiro() {
		return estrangeiro;
	}

	/**
	 * Sets the value of the estrangeiro property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEstrangeiro(String value) {
		this.estrangeiro = value;
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
