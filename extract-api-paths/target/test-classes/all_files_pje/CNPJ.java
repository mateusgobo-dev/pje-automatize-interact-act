package br.gov.cjf.pj.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for CNPJ complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CNPJ">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ideMatrizFilial" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="correioEletronicoPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cepResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="logradouroResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cnaeFiscal" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dddTelefonePJFAX" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tipoLogradouroResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="correioEletronicoResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nomeFantasia" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cnpj" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="indSocio" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="codQualificacaoResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="numLogradouroResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dddTelefonePJ2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dddTelefonePJ1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="complementoResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="codSituacaoCadastral" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="numTelefonePJ1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="desQualificacaoResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nomeResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="logradouroPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="desSituacaoCadastral" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="codMunicipioPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dataSituacaoCNPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cpfResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="desCnaeFiscal" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="numTelefonePJ2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="complementoPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dddTelefoneResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dataAberturaPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="siglaUFPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="desNaturezaJuridica" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="num_logradouroPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="codNaturezaJuridica" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="municipioPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="codMunicipioResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="siglaUFResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tipoLogradouroPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bairroPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="numTelefonePJFAX" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bairroResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cepPJ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nire" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="situacaoAtualizacao" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="municipioResponsavel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="razaoSocial" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CNPJ", propOrder = { "ideMatrizFilial", "correioEletronicoPJ", "cepResponsavel",
		"logradouroResponsavel", "cnaeFiscal", "dddTelefonePJFAX", "tipoLogradouroResponsavel",
		"correioEletronicoResponsavel", "nomeFantasia", "cnpj", "indSocio", "codQualificacaoResponsavel",
		"numLogradouroResponsavel", "dddTelefonePJ2", "dddTelefonePJ1", "complementoResponsavel",
		"codSituacaoCadastral", "numTelefonePJ1", "desQualificacaoResponsavel", "nomeResponsavel", "logradouroPJ",
		"desSituacaoCadastral", "codMunicipioPJ", "dataSituacaoCNPJ", "cpfResponsavel", "desCnaeFiscal",
		"numTelefonePJ2", "complementoPJ", "dddTelefoneResponsavel", "dataAberturaPJ", "siglaUFPJ",
		"desNaturezaJuridica", "numLogradouroPJ", "codNaturezaJuridica", "municipioPJ", "codMunicipioResponsavel",
		"siglaUFResponsavel", "tipoLogradouroPJ", "bairroPJ", "numTelefonePJFAX", "bairroResponsavel", "cepPJ", "nire",
		"situacaoAtualizacao", "municipioResponsavel", "razaoSocial" })
public class CNPJ {

	@XmlElement(required = true, nillable = true)
	protected String ideMatrizFilial;
	@XmlElement(required = true, nillable = true)
	protected String correioEletronicoPJ;
	@XmlElement(required = true, nillable = true)
	protected String cepResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String logradouroResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String cnaeFiscal;
	@XmlElement(required = true, nillable = true)
	protected String dddTelefonePJFAX;
	@XmlElement(required = true, nillable = true)
	protected String tipoLogradouroResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String correioEletronicoResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String nomeFantasia;
	@XmlElement(required = true, nillable = true)
	protected String cnpj;
	@XmlElement(required = true, nillable = true)
	protected String indSocio;
	@XmlElement(required = true, nillable = true)
	protected String codQualificacaoResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String numLogradouroResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String dddTelefonePJ2;
	@XmlElement(required = true, nillable = true)
	protected String dddTelefonePJ1;
	@XmlElement(required = true, nillable = true)
	protected String complementoResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String codSituacaoCadastral;
	@XmlElement(required = true, nillable = true)
	protected String numTelefonePJ1;
	@XmlElement(required = true, nillable = true)
	protected String desQualificacaoResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String nomeResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String logradouroPJ;
	@XmlElement(required = true, nillable = true)
	protected String desSituacaoCadastral;
	@XmlElement(required = true, nillable = true)
	protected String codMunicipioPJ;
	@XmlElement(required = true, nillable = true)
	protected String dataSituacaoCNPJ;
	@XmlElement(required = true, nillable = true)
	protected String cpfResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String desCnaeFiscal;
	@XmlElement(required = true, nillable = true)
	protected String numTelefonePJ2;
	@XmlElement(required = true, nillable = true)
	protected String complementoPJ;
	@XmlElement(required = true, nillable = true)
	protected String dddTelefoneResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String dataAberturaPJ;
	@XmlElement(required = true, nillable = true)
	protected String siglaUFPJ;
	@XmlElement(required = true, nillable = true)
	protected String desNaturezaJuridica;
	@XmlElement(name = "num_logradouroPJ", required = true, nillable = true)
	protected String numLogradouroPJ;
	@XmlElement(required = true, nillable = true)
	protected String codNaturezaJuridica;
	@XmlElement(required = true, nillable = true)
	protected String municipioPJ;
	@XmlElement(required = true, nillable = true)
	protected String codMunicipioResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String siglaUFResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String tipoLogradouroPJ;
	@XmlElement(required = true, nillable = true)
	protected String bairroPJ;
	@XmlElement(required = true, nillable = true)
	protected String numTelefonePJFAX;
	@XmlElement(required = true, nillable = true)
	protected String bairroResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String cepPJ;
	@XmlElement(required = true, nillable = true)
	protected String nire;
	@XmlElement(required = true, nillable = true)
	protected String situacaoAtualizacao;
	@XmlElement(required = true, nillable = true)
	protected String municipioResponsavel;
	@XmlElement(required = true, nillable = true)
	protected String razaoSocial;

	/**
	 * Gets the value of the ideMatrizFilial property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIdeMatrizFilial() {
		return ideMatrizFilial;
	}

	/**
	 * Sets the value of the ideMatrizFilial property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIdeMatrizFilial(String value) {
		this.ideMatrizFilial = value;
	}

	/**
	 * Gets the value of the correioEletronicoPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCorreioEletronicoPJ() {
		return correioEletronicoPJ;
	}

	/**
	 * Sets the value of the correioEletronicoPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCorreioEletronicoPJ(String value) {
		this.correioEletronicoPJ = value;
	}

	/**
	 * Gets the value of the cepResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCepResponsavel() {
		return cepResponsavel;
	}

	/**
	 * Sets the value of the cepResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCepResponsavel(String value) {
		this.cepResponsavel = value;
	}

	/**
	 * Gets the value of the logradouroResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLogradouroResponsavel() {
		return logradouroResponsavel;
	}

	/**
	 * Sets the value of the logradouroResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLogradouroResponsavel(String value) {
		this.logradouroResponsavel = value;
	}

	/**
	 * Gets the value of the cnaeFiscal property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCnaeFiscal() {
		return cnaeFiscal;
	}

	/**
	 * Sets the value of the cnaeFiscal property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCnaeFiscal(String value) {
		this.cnaeFiscal = value;
	}

	/**
	 * Gets the value of the dddTelefonePJFAX property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDddTelefonePJFAX() {
		return dddTelefonePJFAX;
	}

	/**
	 * Sets the value of the dddTelefonePJFAX property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDddTelefonePJFAX(String value) {
		this.dddTelefonePJFAX = value;
	}

	/**
	 * Gets the value of the tipoLogradouroResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTipoLogradouroResponsavel() {
		return tipoLogradouroResponsavel;
	}

	/**
	 * Sets the value of the tipoLogradouroResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTipoLogradouroResponsavel(String value) {
		this.tipoLogradouroResponsavel = value;
	}

	/**
	 * Gets the value of the correioEletronicoResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCorreioEletronicoResponsavel() {
		return correioEletronicoResponsavel;
	}

	/**
	 * Sets the value of the correioEletronicoResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCorreioEletronicoResponsavel(String value) {
		this.correioEletronicoResponsavel = value;
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
	 * Gets the value of the cnpj property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCnpj() {
		return cnpj;
	}

	/**
	 * Sets the value of the cnpj property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCnpj(String value) {
		this.cnpj = value;
	}

	/**
	 * Gets the value of the indSocio property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIndSocio() {
		return indSocio;
	}

	/**
	 * Sets the value of the indSocio property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIndSocio(String value) {
		this.indSocio = value;
	}

	/**
	 * Gets the value of the codQualificacaoResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodQualificacaoResponsavel() {
		return codQualificacaoResponsavel;
	}

	/**
	 * Sets the value of the codQualificacaoResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodQualificacaoResponsavel(String value) {
		this.codQualificacaoResponsavel = value;
	}

	/**
	 * Gets the value of the numLogradouroResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumLogradouroResponsavel() {
		return numLogradouroResponsavel;
	}

	/**
	 * Sets the value of the numLogradouroResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumLogradouroResponsavel(String value) {
		this.numLogradouroResponsavel = value;
	}

	/**
	 * Gets the value of the dddTelefonePJ2 property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDddTelefonePJ2() {
		return dddTelefonePJ2;
	}

	/**
	 * Sets the value of the dddTelefonePJ2 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDddTelefonePJ2(String value) {
		this.dddTelefonePJ2 = value;
	}

	/**
	 * Gets the value of the dddTelefonePJ1 property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDddTelefonePJ1() {
		return dddTelefonePJ1;
	}

	/**
	 * Sets the value of the dddTelefonePJ1 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDddTelefonePJ1(String value) {
		this.dddTelefonePJ1 = value;
	}

	/**
	 * Gets the value of the complementoResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getComplementoResponsavel() {
		return complementoResponsavel;
	}

	/**
	 * Sets the value of the complementoResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setComplementoResponsavel(String value) {
		this.complementoResponsavel = value;
	}

	/**
	 * Gets the value of the codSituacaoCadastral property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodSituacaoCadastral() {
		return codSituacaoCadastral;
	}

	/**
	 * Sets the value of the codSituacaoCadastral property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodSituacaoCadastral(String value) {
		this.codSituacaoCadastral = value;
	}

	/**
	 * Gets the value of the numTelefonePJ1 property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumTelefonePJ1() {
		return numTelefonePJ1;
	}

	/**
	 * Sets the value of the numTelefonePJ1 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumTelefonePJ1(String value) {
		this.numTelefonePJ1 = value;
	}

	/**
	 * Gets the value of the desQualificacaoResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDesQualificacaoResponsavel() {
		return desQualificacaoResponsavel;
	}

	/**
	 * Sets the value of the desQualificacaoResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDesQualificacaoResponsavel(String value) {
		this.desQualificacaoResponsavel = value;
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
	 * Gets the value of the logradouroPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLogradouroPJ() {
		return logradouroPJ;
	}

	/**
	 * Sets the value of the logradouroPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLogradouroPJ(String value) {
		this.logradouroPJ = value;
	}

	/**
	 * Gets the value of the desSituacaoCadastral property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDesSituacaoCadastral() {
		return desSituacaoCadastral;
	}

	/**
	 * Sets the value of the desSituacaoCadastral property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDesSituacaoCadastral(String value) {
		this.desSituacaoCadastral = value;
	}

	/**
	 * Gets the value of the codMunicipioPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodMunicipioPJ() {
		return codMunicipioPJ;
	}

	/**
	 * Sets the value of the codMunicipioPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodMunicipioPJ(String value) {
		this.codMunicipioPJ = value;
	}

	/**
	 * Gets the value of the dataSituacaoCNPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataSituacaoCNPJ() {
		return dataSituacaoCNPJ;
	}

	/**
	 * Sets the value of the dataSituacaoCNPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataSituacaoCNPJ(String value) {
		this.dataSituacaoCNPJ = value;
	}

	/**
	 * Gets the value of the cpfResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCpfResponsavel() {
		return cpfResponsavel;
	}

	/**
	 * Sets the value of the cpfResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCpfResponsavel(String value) {
		this.cpfResponsavel = value;
	}

	/**
	 * Gets the value of the desCnaeFiscal property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDesCnaeFiscal() {
		return desCnaeFiscal;
	}

	/**
	 * Sets the value of the desCnaeFiscal property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDesCnaeFiscal(String value) {
		this.desCnaeFiscal = value;
	}

	/**
	 * Gets the value of the numTelefonePJ2 property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumTelefonePJ2() {
		return numTelefonePJ2;
	}

	/**
	 * Sets the value of the numTelefonePJ2 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumTelefonePJ2(String value) {
		this.numTelefonePJ2 = value;
	}

	/**
	 * Gets the value of the complementoPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getComplementoPJ() {
		return complementoPJ;
	}

	/**
	 * Sets the value of the complementoPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setComplementoPJ(String value) {
		this.complementoPJ = value;
	}

	/**
	 * Gets the value of the dddTelefoneResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDddTelefoneResponsavel() {
		return dddTelefoneResponsavel;
	}

	/**
	 * Sets the value of the dddTelefoneResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDddTelefoneResponsavel(String value) {
		this.dddTelefoneResponsavel = value;
	}

	/**
	 * Gets the value of the dataAberturaPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataAberturaPJ() {
		return dataAberturaPJ;
	}

	/**
	 * Sets the value of the dataAberturaPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataAberturaPJ(String value) {
		this.dataAberturaPJ = value;
	}

	/**
	 * Gets the value of the siglaUFPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSiglaUFPJ() {
		return siglaUFPJ;
	}

	/**
	 * Sets the value of the siglaUFPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSiglaUFPJ(String value) {
		this.siglaUFPJ = value;
	}

	/**
	 * Gets the value of the desNaturezaJuridica property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDesNaturezaJuridica() {
		return desNaturezaJuridica;
	}

	/**
	 * Sets the value of the desNaturezaJuridica property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDesNaturezaJuridica(String value) {
		this.desNaturezaJuridica = value;
	}

	/**
	 * Gets the value of the numLogradouroPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumLogradouroPJ() {
		return numLogradouroPJ;
	}

	/**
	 * Sets the value of the numLogradouroPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumLogradouroPJ(String value) {
		this.numLogradouroPJ = value;
	}

	/**
	 * Gets the value of the codNaturezaJuridica property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodNaturezaJuridica() {
		return codNaturezaJuridica;
	}

	/**
	 * Sets the value of the codNaturezaJuridica property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodNaturezaJuridica(String value) {
		this.codNaturezaJuridica = value;
	}

	/**
	 * Gets the value of the municipioPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMunicipioPJ() {
		return municipioPJ;
	}

	/**
	 * Sets the value of the municipioPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMunicipioPJ(String value) {
		this.municipioPJ = value;
	}

	/**
	 * Gets the value of the codMunicipioResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodMunicipioResponsavel() {
		return codMunicipioResponsavel;
	}

	/**
	 * Sets the value of the codMunicipioResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodMunicipioResponsavel(String value) {
		this.codMunicipioResponsavel = value;
	}

	/**
	 * Gets the value of the siglaUFResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSiglaUFResponsavel() {
		return siglaUFResponsavel;
	}

	/**
	 * Sets the value of the siglaUFResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSiglaUFResponsavel(String value) {
		this.siglaUFResponsavel = value;
	}

	/**
	 * Gets the value of the tipoLogradouroPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTipoLogradouroPJ() {
		return tipoLogradouroPJ;
	}

	/**
	 * Sets the value of the tipoLogradouroPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTipoLogradouroPJ(String value) {
		this.tipoLogradouroPJ = value;
	}

	/**
	 * Gets the value of the bairroPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBairroPJ() {
		return bairroPJ;
	}

	/**
	 * Sets the value of the bairroPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBairroPJ(String value) {
		this.bairroPJ = value;
	}

	/**
	 * Gets the value of the numTelefonePJFAX property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumTelefonePJFAX() {
		return numTelefonePJFAX;
	}

	/**
	 * Sets the value of the numTelefonePJFAX property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumTelefonePJFAX(String value) {
		this.numTelefonePJFAX = value;
	}

	/**
	 * Gets the value of the bairroResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBairroResponsavel() {
		return bairroResponsavel;
	}

	/**
	 * Sets the value of the bairroResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBairroResponsavel(String value) {
		this.bairroResponsavel = value;
	}

	/**
	 * Gets the value of the cepPJ property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCepPJ() {
		return cepPJ;
	}

	/**
	 * Sets the value of the cepPJ property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCepPJ(String value) {
		this.cepPJ = value;
	}

	/**
	 * Gets the value of the nire property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNire() {
		return nire;
	}

	/**
	 * Sets the value of the nire property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNire(String value) {
		this.nire = value;
	}

	/**
	 * Gets the value of the situacaoAtualizacao property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSituacaoAtualizacao() {
		return situacaoAtualizacao;
	}

	/**
	 * Sets the value of the situacaoAtualizacao property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSituacaoAtualizacao(String value) {
		this.situacaoAtualizacao = value;
	}

	/**
	 * Gets the value of the municipioResponsavel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMunicipioResponsavel() {
		return municipioResponsavel;
	}

	/**
	 * Sets the value of the municipioResponsavel property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMunicipioResponsavel(String value) {
		this.municipioResponsavel = value;
	}

	/**
	 * Gets the value of the razaoSocial property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getRazaoSocial() {
		return razaoSocial;
	}

	/**
	 * Sets the value of the razaoSocial property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setRazaoSocial(String value) {
		this.razaoSocial = value;
	}

}
