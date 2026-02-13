package br.jus.cnj.pje.webservice.client.consultacnpj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for CNPJPerfil3 complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CNPJPerfil3">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CNPJ" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Estabelecimento" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomeEmpresarial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomeFantasia" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SituacaoCadastral" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataSituacaoCadastral" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CidadeExterior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CodigoPais" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomePais" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NaturezaJuridica" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataAbertura" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CNAEPrincipal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CNAESecundario" type="{nsProxyRFBCNJ}ArrayOfString" minOccurs="0"/>
 *         &lt;element name="TipoLogradouro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Logradouro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NumeroLogradouro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Complemento" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Bairro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CEP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UF" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CodigoMunicipio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomeMunicipio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DDD1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Telefone1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DDD2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Telefone2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CPFResponsavel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomeResponsavel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CapitalSocial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Sociedade" type="{nsProxyRFBCNJ}ArrayOfSocio" minOccurs="0"/>
 *         &lt;element name="TipoCRCContadorPJ" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ClassificacaoCRCContadorPJ" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NumeroCRCContadorPJ" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UFCRCContadorPJ" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CNPJContador" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TipoCRCContadorPF" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ClassificacaoCRCContadorPF" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NumeroCRCContadorPF" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UFCRCContadorPF" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CPFContador" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Porte" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OpcaoSimples" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataOpcaoSimples" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataExclusaoSimples" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CNPJSucedida" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CNPJSucessora" type="{nsProxyRFBCNJ}ArrayOfString" minOccurs="0"/>
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
@XmlType(name = "CNPJPerfil3", propOrder = { "cnpj", "estabelecimento", "nomeEmpresarial", "nomeFantasia",
		"situacaoCadastral", "dataSituacaoCadastral", "cidadeExterior", "codigoPais", "nomePais", "naturezaJuridica",
		"dataAbertura", "cnaePrincipal", "cnaeSecundario", "tipoLogradouro", "logradouro", "numeroLogradouro",
		"complemento", "bairro", "cep", "uf", "codigoMunicipio", "nomeMunicipio", "ddd1", "telefone1", "ddd2",
		"telefone2", "email", "cpfResponsavel", "nomeResponsavel", "capitalSocial", "sociedade", "tipoCRCContadorPJ",
		"classificacaoCRCContadorPJ", "numeroCRCContadorPJ", "ufcrcContadorPJ", "cnpjContador", "tipoCRCContadorPF",
		"classificacaoCRCContadorPF", "numeroCRCContadorPF", "ufcrcContadorPF", "cpfContador", "porte", "opcaoSimples",
		"dataOpcaoSimples", "dataExclusaoSimples", "cnpjSucedida", "cnpjSucessora", "erro" })
public class CNPJPerfil3 {

	@XmlElement(name = "CNPJ")
	protected String cnpj;
	@XmlElement(name = "Estabelecimento")
	protected String estabelecimento;
	@XmlElement(name = "NomeEmpresarial")
	protected String nomeEmpresarial;
	@XmlElement(name = "NomeFantasia")
	protected String nomeFantasia;
	@XmlElement(name = "SituacaoCadastral")
	protected String situacaoCadastral;
	@XmlElement(name = "DataSituacaoCadastral")
	protected String dataSituacaoCadastral;
	@XmlElement(name = "CidadeExterior")
	protected String cidadeExterior;
	@XmlElement(name = "CodigoPais")
	protected String codigoPais;
	@XmlElement(name = "NomePais")
	protected String nomePais;
	@XmlElement(name = "NaturezaJuridica")
	protected String naturezaJuridica;
	@XmlElement(name = "DataAbertura")
	protected String dataAbertura;
	@XmlElement(name = "CNAEPrincipal")
	protected String cnaePrincipal;
	@XmlElement(name = "CNAESecundario")
	protected ArrayOfString cnaeSecundario;
	@XmlElement(name = "TipoLogradouro")
	protected String tipoLogradouro;
	@XmlElement(name = "Logradouro")
	protected String logradouro;
	@XmlElement(name = "NumeroLogradouro")
	protected String numeroLogradouro;
	@XmlElement(name = "Complemento")
	protected String complemento;
	@XmlElement(name = "Bairro")
	protected String bairro;
	@XmlElement(name = "CEP")
	protected String cep;
	@XmlElement(name = "UF")
	protected String uf;
	@XmlElement(name = "CodigoMunicipio")
	protected String codigoMunicipio;
	@XmlElement(name = "NomeMunicipio")
	protected String nomeMunicipio;
	@XmlElement(name = "DDD1")
	protected String ddd1;
	@XmlElement(name = "Telefone1")
	protected String telefone1;
	@XmlElement(name = "DDD2")
	protected String ddd2;
	@XmlElement(name = "Telefone2")
	protected String telefone2;
	@XmlElement(name = "Email")
	protected String email;
	@XmlElement(name = "CPFResponsavel")
	protected String cpfResponsavel;
	@XmlElement(name = "NomeResponsavel")
	protected String nomeResponsavel;
	@XmlElement(name = "CapitalSocial")
	protected String capitalSocial;
	@XmlElement(name = "Sociedade")
	protected ArrayOfSocio sociedade;
	@XmlElement(name = "TipoCRCContadorPJ")
	protected String tipoCRCContadorPJ;
	@XmlElement(name = "ClassificacaoCRCContadorPJ")
	protected String classificacaoCRCContadorPJ;
	@XmlElement(name = "NumeroCRCContadorPJ")
	protected String numeroCRCContadorPJ;
	@XmlElement(name = "UFCRCContadorPJ")
	protected String ufcrcContadorPJ;
	@XmlElement(name = "CNPJContador")
	protected String cnpjContador;
	@XmlElement(name = "TipoCRCContadorPF")
	protected String tipoCRCContadorPF;
	@XmlElement(name = "ClassificacaoCRCContadorPF")
	protected String classificacaoCRCContadorPF;
	@XmlElement(name = "NumeroCRCContadorPF")
	protected String numeroCRCContadorPF;
	@XmlElement(name = "UFCRCContadorPF")
	protected String ufcrcContadorPF;
	@XmlElement(name = "CPFContador")
	protected String cpfContador;
	@XmlElement(name = "Porte")
	protected String porte;
	@XmlElement(name = "OpcaoSimples")
	protected String opcaoSimples;
	@XmlElement(name = "DataOpcaoSimples")
	protected String dataOpcaoSimples;
	@XmlElement(name = "DataExclusaoSimples")
	protected String dataExclusaoSimples;
	@XmlElement(name = "CNPJSucedida")
	protected String cnpjSucedida;
	@XmlElement(name = "CNPJSucessora")
	protected ArrayOfString cnpjSucessora;
	@XmlElement(name = "Erro")
	protected String erro;

	/**
	 * Gets the value of the cnpj property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCNPJ() {
		return cnpj;
	}

	/**
	 * Sets the value of the cnpj property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCNPJ(String value) {
		this.cnpj = value;
	}

	/**
	 * Gets the value of the estabelecimento property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEstabelecimento() {
		return estabelecimento;
	}

	/**
	 * Sets the value of the estabelecimento property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEstabelecimento(String value) {
		this.estabelecimento = value;
	}

	/**
	 * Gets the value of the nomeEmpresarial property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeEmpresarial() {
		return nomeEmpresarial;
	}

	/**
	 * Sets the value of the nomeEmpresarial property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeEmpresarial(String value) {
		this.nomeEmpresarial = value;
	}

	/**
	 * Gets the value of the nomeFantasia property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeFantasia() {
		return nomeFantasia;
	}

	/**
	 * Sets the value of the nomeFantasia property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeFantasia(String value) {
		this.nomeFantasia = value;
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
	 * Gets the value of the dataSituacaoCadastral property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataSituacaoCadastral() {
		return dataSituacaoCadastral;
	}

	/**
	 * Sets the value of the dataSituacaoCadastral property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataSituacaoCadastral(String value) {
		this.dataSituacaoCadastral = value;
	}

	/**
	 * Gets the value of the cidadeExterior property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCidadeExterior() {
		return cidadeExterior;
	}

	/**
	 * Sets the value of the cidadeExterior property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCidadeExterior(String value) {
		this.cidadeExterior = value;
	}

	/**
	 * Gets the value of the codigoPais property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodigoPais() {
		return codigoPais;
	}

	/**
	 * Sets the value of the codigoPais property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodigoPais(String value) {
		this.codigoPais = value;
	}

	/**
	 * Gets the value of the nomePais property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomePais() {
		return nomePais;
	}

	/**
	 * Sets the value of the nomePais property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomePais(String value) {
		this.nomePais = value;
	}

	/**
	 * Gets the value of the naturezaJuridica property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNaturezaJuridica() {
		return naturezaJuridica;
	}

	/**
	 * Sets the value of the naturezaJuridica property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNaturezaJuridica(String value) {
		this.naturezaJuridica = value;
	}

	/**
	 * Gets the value of the dataAbertura property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataAbertura() {
		return dataAbertura;
	}

	/**
	 * Sets the value of the dataAbertura property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataAbertura(String value) {
		this.dataAbertura = value;
	}

	/**
	 * Gets the value of the cnaePrincipal property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCNAEPrincipal() {
		return cnaePrincipal;
	}

	/**
	 * Sets the value of the cnaePrincipal property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCNAEPrincipal(String value) {
		this.cnaePrincipal = value;
	}

	/**
	 * Gets the value of the cnaeSecundario property.
	 * 
	 * @return possible object is {@link ArrayOfString }
	 * 
	 */
	public ArrayOfString getCNAESecundario() {
		return cnaeSecundario;
	}

	/**
	 * Sets the value of the cnaeSecundario property.
	 * 
	 * @param value
	 *            allowed object is {@link ArrayOfString }
	 * 
	 */
	public void setCNAESecundario(ArrayOfString value) {
		this.cnaeSecundario = value;
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
	 * Gets the value of the nomeMunicipio property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeMunicipio() {
		return nomeMunicipio;
	}

	/**
	 * Sets the value of the nomeMunicipio property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeMunicipio(String value) {
		this.nomeMunicipio = value;
	}

	/**
	 * Gets the value of the ddd1 property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDDD1() {
		return ddd1;
	}

	/**
	 * Sets the value of the ddd1 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDDD1(String value) {
		this.ddd1 = value;
	}

	/**
	 * Gets the value of the telefone1 property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTelefone1() {
		return telefone1;
	}

	/**
	 * Sets the value of the telefone1 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTelefone1(String value) {
		this.telefone1 = value;
	}

	/**
	 * Gets the value of the ddd2 property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDDD2() {
		return ddd2;
	}

	/**
	 * Sets the value of the ddd2 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDDD2(String value) {
		this.ddd2 = value;
	}

	/**
	 * Gets the value of the telefone2 property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTelefone2() {
		return telefone2;
	}

	/**
	 * Sets the value of the telefone2 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTelefone2(String value) {
		this.telefone2 = value;
	}

	/**
	 * Gets the value of the email property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the value of the email property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEmail(String value) {
		this.email = value;
	}

	/**
	 * Gets the value of the cpfResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCPFResponsavel() {
		return cpfResponsavel;
	}

	/**
	 * Sets the value of the cpfResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCPFResponsavel(String value) {
		this.cpfResponsavel = value;
	}

	/**
	 * Gets the value of the nomeResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeResponsavel() {
		return nomeResponsavel;
	}

	/**
	 * Sets the value of the nomeResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeResponsavel(String value) {
		this.nomeResponsavel = value;
	}

	/**
	 * Gets the value of the capitalSocial property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCapitalSocial() {
		return capitalSocial;
	}

	/**
	 * Sets the value of the capitalSocial property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCapitalSocial(String value) {
		this.capitalSocial = value;
	}

	/**
	 * Gets the value of the sociedade property.
	 * 
	 * @return possible object is {@link ArrayOfSocio }
	 * 
	 */
	public ArrayOfSocio getSociedade() {
		return sociedade;
	}

	/**
	 * Sets the value of the sociedade property.
	 * 
	 * @param value
	 *            allowed object is {@link ArrayOfSocio }
	 * 
	 */
	public void setSociedade(ArrayOfSocio value) {
		this.sociedade = value;
	}

	/**
	 * Gets the value of the tipoCRCContadorPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTipoCRCContadorPJ() {
		return tipoCRCContadorPJ;
	}

	/**
	 * Sets the value of the tipoCRCContadorPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTipoCRCContadorPJ(String value) {
		this.tipoCRCContadorPJ = value;
	}

	/**
	 * Gets the value of the classificacaoCRCContadorPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getClassificacaoCRCContadorPJ() {
		return classificacaoCRCContadorPJ;
	}

	/**
	 * Sets the value of the classificacaoCRCContadorPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setClassificacaoCRCContadorPJ(String value) {
		this.classificacaoCRCContadorPJ = value;
	}

	/**
	 * Gets the value of the numeroCRCContadorPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumeroCRCContadorPJ() {
		return numeroCRCContadorPJ;
	}

	/**
	 * Sets the value of the numeroCRCContadorPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumeroCRCContadorPJ(String value) {
		this.numeroCRCContadorPJ = value;
	}

	/**
	 * Gets the value of the ufcrcContadorPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUFCRCContadorPJ() {
		return ufcrcContadorPJ;
	}

	/**
	 * Sets the value of the ufcrcContadorPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUFCRCContadorPJ(String value) {
		this.ufcrcContadorPJ = value;
	}

	/**
	 * Gets the value of the cnpjContador property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCNPJContador() {
		return cnpjContador;
	}

	/**
	 * Sets the value of the cnpjContador property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCNPJContador(String value) {
		this.cnpjContador = value;
	}

	/**
	 * Gets the value of the tipoCRCContadorPF property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTipoCRCContadorPF() {
		return tipoCRCContadorPF;
	}

	/**
	 * Sets the value of the tipoCRCContadorPF property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTipoCRCContadorPF(String value) {
		this.tipoCRCContadorPF = value;
	}

	/**
	 * Gets the value of the classificacaoCRCContadorPF property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getClassificacaoCRCContadorPF() {
		return classificacaoCRCContadorPF;
	}

	/**
	 * Sets the value of the classificacaoCRCContadorPF property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setClassificacaoCRCContadorPF(String value) {
		this.classificacaoCRCContadorPF = value;
	}

	/**
	 * Gets the value of the numeroCRCContadorPF property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumeroCRCContadorPF() {
		return numeroCRCContadorPF;
	}

	/**
	 * Sets the value of the numeroCRCContadorPF property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumeroCRCContadorPF(String value) {
		this.numeroCRCContadorPF = value;
	}

	/**
	 * Gets the value of the ufcrcContadorPF property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUFCRCContadorPF() {
		return ufcrcContadorPF;
	}

	/**
	 * Sets the value of the ufcrcContadorPF property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUFCRCContadorPF(String value) {
		this.ufcrcContadorPF = value;
	}

	/**
	 * Gets the value of the cpfContador property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCPFContador() {
		return cpfContador;
	}

	/**
	 * Sets the value of the cpfContador property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCPFContador(String value) {
		this.cpfContador = value;
	}

	/**
	 * Gets the value of the porte property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPorte() {
		return porte;
	}

	/**
	 * Sets the value of the porte property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPorte(String value) {
		this.porte = value;
	}

	/**
	 * Gets the value of the opcaoSimples property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOpcaoSimples() {
		return opcaoSimples;
	}

	/**
	 * Sets the value of the opcaoSimples property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOpcaoSimples(String value) {
		this.opcaoSimples = value;
	}

	/**
	 * Gets the value of the dataOpcaoSimples property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataOpcaoSimples() {
		return dataOpcaoSimples;
	}

	/**
	 * Sets the value of the dataOpcaoSimples property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataOpcaoSimples(String value) {
		this.dataOpcaoSimples = value;
	}

	/**
	 * Gets the value of the dataExclusaoSimples property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataExclusaoSimples() {
		return dataExclusaoSimples;
	}

	/**
	 * Sets the value of the dataExclusaoSimples property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataExclusaoSimples(String value) {
		this.dataExclusaoSimples = value;
	}

	/**
	 * Gets the value of the cnpjSucedida property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCNPJSucedida() {
		return cnpjSucedida;
	}

	/**
	 * Sets the value of the cnpjSucedida property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCNPJSucedida(String value) {
		this.cnpjSucedida = value;
	}

	/**
	 * Gets the value of the cnpjSucessora property.
	 * 
	 * @return possible object is {@link ArrayOfString }
	 * 
	 */
	public ArrayOfString getCNPJSucessora() {
		return cnpjSucessora;
	}

	/**
	 * Sets the value of the cnpjSucessora property.
	 * 
	 * @param value
	 *            allowed object is {@link ArrayOfString }
	 * 
	 */
	public void setCNPJSucessora(ArrayOfString value) {
		this.cnpjSucessora = value;
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
