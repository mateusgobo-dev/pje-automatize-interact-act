package br.com.infox.pje.webservices.estatisticaEventoProcessoTrf.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for estatisticaEventoProcessoBean complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="estatisticaEventoProcessoBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="classeJudicial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codEstado" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codEvento" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="competencia" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataInclusao" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="documentoApelacao" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="documentoSentenca" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="idEstatisticaProcesso" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="idProcessoTrf" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="jurisdicao" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroProcessoTrf" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orgaoJulgador" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "estatisticaEventoProcessoBean", propOrder = { "classeJudicial", "codEstado", "codEvento",
		"competencia", "dataInclusao", "documentoApelacao", "documentoSentenca", "idEstatisticaProcesso",
		"idProcessoTrf", "jurisdicao", "numeroProcessoTrf", "orgaoJulgador" })
public class EstatisticaEventoProcessoBean {

	protected String classeJudicial;
	protected String codEstado;
	protected String codEvento;
	protected String competencia;
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar dataInclusao;
	protected Boolean documentoApelacao;
	protected Boolean documentoSentenca;
	protected int idEstatisticaProcesso;
	protected int idProcessoTrf;
	protected String jurisdicao;
	protected String numeroProcessoTrf;
	protected String orgaoJulgador;

	/**
	 * Gets the value of the classeJudicial property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getClasseJudicial() {
		return classeJudicial;
	}

	/**
	 * Sets the value of the classeJudicial property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setClasseJudicial(String value) {
		this.classeJudicial = value;
	}

	/**
	 * Gets the value of the codEstado property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodEstado() {
		return codEstado;
	}

	/**
	 * Sets the value of the codEstado property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodEstado(String value) {
		this.codEstado = value;
	}

	/**
	 * Gets the value of the codEvento property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodEvento() {
		return codEvento;
	}

	/**
	 * Sets the value of the codEvento property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodEvento(String value) {
		this.codEvento = value;
	}

	/**
	 * Gets the value of the competencia property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCompetencia() {
		return competencia;
	}

	/**
	 * Sets the value of the competencia property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCompetencia(String value) {
		this.competencia = value;
	}

	/**
	 * Gets the value of the dataInclusao property.
	 * 
	 * @return possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	public XMLGregorianCalendar getDataInclusao() {
		return dataInclusao;
	}

	/**
	 * Sets the value of the dataInclusao property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	public void setDataInclusao(XMLGregorianCalendar value) {
		this.dataInclusao = value;
	}

	/**
	 * Gets the value of the documentoApelacao property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isDocumentoApelacao() {
		return documentoApelacao;
	}

	/**
	 * Sets the value of the documentoApelacao property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setDocumentoApelacao(Boolean value) {
		this.documentoApelacao = value;
	}

	/**
	 * Gets the value of the documentoSentenca property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isDocumentoSentenca() {
		return documentoSentenca;
	}

	/**
	 * Sets the value of the documentoSentenca property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setDocumentoSentenca(Boolean value) {
		this.documentoSentenca = value;
	}

	/**
	 * Gets the value of the idEstatisticaProcesso property.
	 * 
	 */
	public int getIdEstatisticaProcesso() {
		return idEstatisticaProcesso;
	}

	/**
	 * Sets the value of the idEstatisticaProcesso property.
	 * 
	 */
	public void setIdEstatisticaProcesso(int value) {
		this.idEstatisticaProcesso = value;
	}

	/**
	 * Gets the value of the idProcessoTrf property.
	 * 
	 */
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	/**
	 * Sets the value of the idProcessoTrf property.
	 * 
	 */
	public void setIdProcessoTrf(int value) {
		this.idProcessoTrf = value;
	}

	/**
	 * Gets the value of the jurisdicao property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getJurisdicao() {
		return jurisdicao;
	}

	/**
	 * Sets the value of the jurisdicao property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setJurisdicao(String value) {
		this.jurisdicao = value;
	}

	/**
	 * Gets the value of the numeroProcessoTrf property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumeroProcessoTrf() {
		return numeroProcessoTrf;
	}

	/**
	 * Sets the value of the numeroProcessoTrf property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumeroProcessoTrf(String value) {
		this.numeroProcessoTrf = value;
	}

	/**
	 * Gets the value of the orgaoJulgador property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	/**
	 * Sets the value of the orgaoJulgador property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOrgaoJulgador(String value) {
		this.orgaoJulgador = value;
	}

}
