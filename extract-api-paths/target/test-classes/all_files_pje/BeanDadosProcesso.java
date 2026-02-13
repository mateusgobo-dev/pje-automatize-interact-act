package br.com.infox.pje.webservice.consultaoutrasessao;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for beanDadosProcesso complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="beanDadosProcesso">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataDistribuicao" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="idProcesso" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="hash" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="linkConsulta" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nomeClasseJudicial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nomeOrgaoJulgador" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroProcesso" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="poloAtivo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="poloPassivo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "beanDadosProcesso", propOrder = { "dataDistribuicao", "idProcesso", "hash", "linkConsulta",
		"nomeClasseJudicial", "nomeOrgaoJulgador", "numeroProcesso", "poloAtivo", "poloPassivo", "prioritario" })
public class BeanDadosProcesso {

	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar dataDistribuicao;
	protected int idProcesso;
	protected String hash;
	protected String linkConsulta;
	protected String nomeClasseJudicial;
	protected String nomeOrgaoJulgador;
	protected String numeroProcesso;
	protected String poloAtivo;
	protected String poloPassivo;
	private boolean prioritario;

	/**
	 * Gets the value of the dataDistribuicao property.
	 * 
	 * @return possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	public XMLGregorianCalendar getDataDistribuicao() {
		return dataDistribuicao;
	}

	/**
	 * Sets the value of the dataDistribuicao property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	public void setDataDistribuicao(XMLGregorianCalendar value) {
		this.dataDistribuicao = value;
	}

	/**
	 * Gets the value of the idProcesso property.
	 * 
	 */
	public int getIdProcesso() {
		return idProcesso;
	}

	/**
	 * Sets the value of the idProcesso property.
	 * 
	 */
	public void setIdProcesso(int value) {
		this.idProcesso = value;
	}

	/**
	 * Gets the value of the hash property.
	 * 
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * Sets the value of the hash property.
	 * 
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * Gets the value of the linkConsulta property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLinkConsulta() {
		return linkConsulta;
	}

	/**
	 * Sets the value of the linkConsulta property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLinkConsulta(String value) {
		this.linkConsulta = value;
	}

	/**
	 * Gets the value of the nomeClasseJudicial property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeClasseJudicial() {
		return nomeClasseJudicial;
	}

	/**
	 * Sets the value of the nomeClasseJudicial property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeClasseJudicial(String value) {
		this.nomeClasseJudicial = value;
	}

	/**
	 * Gets the value of the nomeOrgaoJulgador property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeOrgaoJulgador() {
		return nomeOrgaoJulgador;
	}

	/**
	 * Sets the value of the nomeOrgaoJulgador property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeOrgaoJulgador(String value) {
		this.nomeOrgaoJulgador = value;
	}

	/**
	 * Gets the value of the numeroProcesso property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	/**
	 * Sets the value of the numeroProcesso property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumeroProcesso(String value) {
		this.numeroProcesso = value;
	}

	/**
	 * Gets the value of the poloAtivo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPoloAtivo() {
		return poloAtivo;
	}

	/**
	 * Sets the value of the poloAtivo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPoloAtivo(String value) {
		this.poloAtivo = value;
	}

	/**
	 * Gets the value of the poloPassivo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPoloPassivo() {
		return poloPassivo;
	}

	/**
	 * Sets the value of the poloPassivo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPoloPassivo(String value) {
		this.poloPassivo = value;
	}

	public void setPrioritario(boolean prioritario) {
		this.prioritario = prioritario;
	}

	public boolean getPrioritario() {
		return prioritario;
	}

}
