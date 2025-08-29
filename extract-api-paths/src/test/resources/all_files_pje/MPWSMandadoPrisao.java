package br.jus.cnj.mandadoprisao.webservices;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for mandadoPrisao complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="mandadoPrisao">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="assuntoDelitoAlvara" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataDelito" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataMandado" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataValidade" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nomeMagistrado" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroMandado" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroMandadoAnterior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orgaoJtrVo" type="{http://www.cnj.jus.br/mpws}orgaoJtrVO" minOccurs="0"/>
 *         &lt;element name="orgaoJulgador" type="{http://www.cnj.jus.br/mpws}orgaoJulgadorMandado" minOccurs="0"/>
 *         &lt;element name="penaImposta" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pessoa" type="{http://www.cnj.jus.br/mpws}pessoa" minOccurs="0"/>
 *         &lt;element name="prazo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="procedimentosOrigem" type="{http://www.cnj.jus.br/mpws}procedimentoOrigem" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="processo" type="{http://www.cnj.jus.br/mpws}processoMandadoPrisao" minOccurs="0"/>
 *         &lt;element name="recaptura" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="regimeCumprimentoPena" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sinteseDecisao" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tipoMagistrado" type="{http://www.cnj.jus.br/mpws}tipoMagistradoMandado" minOccurs="0"/>
 *         &lt;element name="tipoPrisao" type="{http://www.cnj.jus.br/mpws}tipoPrisaoMandado" minOccurs="0"/>
 *         &lt;element name="tipoSituacao" type="{http://www.cnj.jus.br/mpws}tipoSituacaoMandado" minOccurs="0"/>
 *         &lt;element name="valorFianca" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mandadoPrisao", propOrder = { "assuntoDelitoAlvara", "dataDelito", "dataMandado", "dataValidade",
		"nomeMagistrado", "numeroMandado", "numeroMandadoAnterior", "orgaoJtrVo", "orgaoJulgador", "penaImposta",
		"pessoa", "prazo", "procedimentosOrigem", "processo", "recaptura", "regimeCumprimentoPena", "sinteseDecisao",
		"tipoMagistrado", "tipoPrisao", "tipoSituacao", "valorFianca" })
public class MPWSMandadoPrisao {

	protected String assuntoDelitoAlvara;
	protected String dataDelito;
	protected String dataMandado;
	protected String dataValidade;
	protected String nomeMagistrado;
	protected String numeroMandado;
	protected String numeroMandadoAnterior;
	protected MPWSOrgaoJtrVO orgaoJtrVo;
	protected MPWSOrgaoJulgadorMandado orgaoJulgador;
	protected String penaImposta;
	protected MPWSPessoa pessoa;
	protected Integer prazo;
	@XmlElement(nillable = true)
	protected List<MPWSProcedimentoOrigem> procedimentosOrigem;
	protected MPWSProcessoMandadoPrisao processo;
	protected Boolean recaptura;
	protected String regimeCumprimentoPena;
	protected String sinteseDecisao;
	protected MPWSTipoMagistradoMandado tipoMagistrado;
	protected MPWSTipoPrisaoMandado tipoPrisao;
	protected MPWSTipoSituacaoMandado tipoSituacao;
	protected Double valorFianca;

	/**
	 * Gets the value of the assuntoDelitoAlvara property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAssuntoDelitoAlvara() {
		return assuntoDelitoAlvara;
	}

	/**
	 * Sets the value of the assuntoDelitoAlvara property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAssuntoDelitoAlvara(String value) {
		this.assuntoDelitoAlvara = value;
	}

	/**
	 * Gets the value of the dataDelito property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataDelito() {
		return dataDelito;
	}

	/**
	 * Sets the value of the dataDelito property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataDelito(String value) {
		this.dataDelito = value;
	}

	/**
	 * Gets the value of the dataMandado property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataMandado() {
		return dataMandado;
	}

	/**
	 * Sets the value of the dataMandado property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataMandado(String value) {
		this.dataMandado = value;
	}

	/**
	 * Gets the value of the dataValidade property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataValidade() {
		return dataValidade;
	}

	/**
	 * Sets the value of the dataValidade property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataValidade(String value) {
		this.dataValidade = value;
	}

	/**
	 * Gets the value of the nomeMagistrado property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeMagistrado() {
		return nomeMagistrado;
	}

	/**
	 * Sets the value of the nomeMagistrado property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeMagistrado(String value) {
		this.nomeMagistrado = value;
	}

	/**
	 * Gets the value of the numeroMandado property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumeroMandado() {
		return numeroMandado;
	}

	/**
	 * Sets the value of the numeroMandado property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumeroMandado(String value) {
		this.numeroMandado = value;
	}

	/**
	 * Gets the value of the numeroMandadoAnterior property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumeroMandadoAnterior() {
		return numeroMandadoAnterior;
	}

	/**
	 * Sets the value of the numeroMandadoAnterior property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumeroMandadoAnterior(String value) {
		this.numeroMandadoAnterior = value;
	}

	/**
	 * Gets the value of the orgaoJtrVo property.
	 * 
	 * @return possible object is {@link MPWSOrgaoJtrVO }
	 * 
	 */
	public MPWSOrgaoJtrVO getOrgaoJtrVo() {
		return orgaoJtrVo;
	}

	/**
	 * Sets the value of the orgaoJtrVo property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSOrgaoJtrVO }
	 * 
	 */
	public void setOrgaoJtrVo(MPWSOrgaoJtrVO value) {
		this.orgaoJtrVo = value;
	}

	/**
	 * Gets the value of the orgaoJulgador property.
	 * 
	 * @return possible object is {@link MPWSOrgaoJulgadorMandado }
	 * 
	 */
	public MPWSOrgaoJulgadorMandado getOrgaoJulgador() {
		return orgaoJulgador;
	}

	/**
	 * Sets the value of the orgaoJulgador property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSOrgaoJulgadorMandado }
	 * 
	 */
	public void setOrgaoJulgador(MPWSOrgaoJulgadorMandado value) {
		this.orgaoJulgador = value;
	}

	/**
	 * Gets the value of the penaImposta property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPenaImposta() {
		return penaImposta;
	}

	/**
	 * Sets the value of the penaImposta property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPenaImposta(String value) {
		this.penaImposta = value;
	}

	/**
	 * Gets the value of the pessoa property.
	 * 
	 * @return possible object is {@link MPWSPessoa }
	 * 
	 */
	public MPWSPessoa getPessoa() {
		return pessoa;
	}

	/**
	 * Sets the value of the pessoa property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSPessoa }
	 * 
	 */
	public void setPessoa(MPWSPessoa value) {
		this.pessoa = value;
	}

	/**
	 * Gets the value of the prazo property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getPrazo() {
		return prazo;
	}

	/**
	 * Sets the value of the prazo property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setPrazo(Integer value) {
		this.prazo = value;
	}

	/**
	 * Gets the value of the procedimentosOrigem property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the procedimentosOrigem property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getProcedimentosOrigem().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link MPWSProcedimentoOrigem }
	 * 
	 * 
	 */
	public List<MPWSProcedimentoOrigem> getProcedimentosOrigem() {
		if (procedimentosOrigem == null) {
			procedimentosOrigem = new ArrayList<MPWSProcedimentoOrigem>();
		}
		return this.procedimentosOrigem;
	}

	/**
	 * Gets the value of the processo property.
	 * 
	 * @return possible object is {@link MPWSProcessoMandadoPrisao }
	 * 
	 */
	public MPWSProcessoMandadoPrisao getProcesso() {
		return processo;
	}

	/**
	 * Sets the value of the processo property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSProcessoMandadoPrisao }
	 * 
	 */
	public void setProcesso(MPWSProcessoMandadoPrisao value) {
		this.processo = value;
	}

	/**
	 * Gets the value of the recaptura property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isRecaptura() {
		return recaptura;
	}

	/**
	 * Sets the value of the recaptura property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setRecaptura(Boolean value) {
		this.recaptura = value;
	}

	/**
	 * Gets the value of the regimeCumprimentoPena property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getRegimeCumprimentoPena() {
		return regimeCumprimentoPena;
	}

	/**
	 * Sets the value of the regimeCumprimentoPena property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setRegimeCumprimentoPena(String value) {
		this.regimeCumprimentoPena = value;
	}

	/**
	 * Gets the value of the sinteseDecisao property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSinteseDecisao() {
		return sinteseDecisao;
	}

	/**
	 * Sets the value of the sinteseDecisao property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSinteseDecisao(String value) {
		this.sinteseDecisao = value;
	}

	/**
	 * Gets the value of the tipoMagistrado property.
	 * 
	 * @return possible object is {@link MPWSTipoMagistradoMandado }
	 * 
	 */
	public MPWSTipoMagistradoMandado getTipoMagistrado() {
		return tipoMagistrado;
	}

	/**
	 * Sets the value of the tipoMagistrado property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSTipoMagistradoMandado }
	 * 
	 */
	public void setTipoMagistrado(MPWSTipoMagistradoMandado value) {
		this.tipoMagistrado = value;
	}

	/**
	 * Gets the value of the tipoPrisao property.
	 * 
	 * @return possible object is {@link MPWSTipoPrisaoMandado }
	 * 
	 */
	public MPWSTipoPrisaoMandado getTipoPrisao() {
		return tipoPrisao;
	}

	/**
	 * Sets the value of the tipoPrisao property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSTipoPrisaoMandado }
	 * 
	 */
	public void setTipoPrisao(MPWSTipoPrisaoMandado value) {
		this.tipoPrisao = value;
	}

	/**
	 * Gets the value of the tipoSituacao property.
	 * 
	 * @return possible object is {@link MPWSTipoSituacaoMandado }
	 * 
	 */
	public MPWSTipoSituacaoMandado getTipoSituacao() {
		return tipoSituacao;
	}

	/**
	 * Sets the value of the tipoSituacao property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSTipoSituacaoMandado }
	 * 
	 */
	public void setTipoSituacao(MPWSTipoSituacaoMandado value) {
		this.tipoSituacao = value;
	}

	/**
	 * Gets the value of the valorFianca property.
	 * 
	 * @return possible object is {@link Double }
	 * 
	 */
	public Double getValorFianca() {
		return valorFianca;
	}

	/**
	 * Sets the value of the valorFianca property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public void setValorFianca(Double value) {
		this.valorFianca = value;
	}
}
