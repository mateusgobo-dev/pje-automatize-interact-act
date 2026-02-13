package br.com.infox.pje.webservice.consultaoutrasessao;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import br.jus.pje.nucleo.entidades.Estado;

/**
 * <p>
 * Java class for beanConsultaProcesso complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="beanConsultaProcesso">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codigoAssunto" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codigoClasseJudicial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cpfCnpj" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataFinal" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="dataInicial" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="letraOab" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="link" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nomeParte" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroOab" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroProcesso" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ufOab" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "beanConsultaProcesso", propOrder = { "codigoAssunto", "codigoClasseJudicial", "cpfCnpj", "dataFinal",
		"dataInicial", "letraOab", "link", "nomeParte", "numeroOab", "numeroProcesso", "ufOab" })
public class BeanConsultaProcesso {

	protected String codigoAssunto;
	protected String codigoClasseJudicial;
	protected String cpfCnpj;
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar dataFinal;
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar dataInicial;
	protected String letraOab;
	protected String link;
	protected String nomeParte;
	protected String numeroOab;
	protected String numeroProcesso;
	protected Estado ufOab;

	/**
	 * Gets the value of the codigoAssunto property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodigoAssunto() {
		return codigoAssunto;
	}

	/**
	 * Sets the value of the codigoAssunto property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodigoAssunto(String value) {
		this.codigoAssunto = value;
	}

	/**
	 * Gets the value of the codigoClasseJudicial property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodigoClasseJudicial() {
		return codigoClasseJudicial;
	}

	/**
	 * Sets the value of the codigoClasseJudicial property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodigoClasseJudicial(String value) {
		this.codigoClasseJudicial = value;
	}

	/**
	 * Gets the value of the cpfCnpj property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCpfCnpj() {
		return cpfCnpj;
	}

	/**
	 * Sets the value of the cpfCnpj property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCpfCnpj(String value) {
		this.cpfCnpj = value;
	}

	/**
	 * Gets the value of the dataFinal property.
	 * 
	 * @return possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	public XMLGregorianCalendar getDataFinal() {
		return dataFinal;
	}

	/**
	 * Sets the value of the dataFinal property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	public void setDataFinal(XMLGregorianCalendar value) {
		this.dataFinal = value;
	}

	/**
	 * Gets the value of the dataInicial property.
	 * 
	 * @return possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	public XMLGregorianCalendar getDataInicial() {
		return dataInicial;
	}

	/**
	 * Sets the value of the dataInicial property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	public void setDataInicial(XMLGregorianCalendar value) {
		this.dataInicial = value;
	}

	/**
	 * Gets the value of the letraOab property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLetraOab() {
		return letraOab;
	}

	/**
	 * Sets the value of the letraOab property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLetraOab(String value) {
		this.letraOab = value;
	}

	/**
	 * Gets the value of the link property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Sets the value of the link property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLink(String value) {
		this.link = value;
	}

	/**
	 * Gets the value of the nomeParte property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeParte() {
		return nomeParte;
	}

	/**
	 * Sets the value of the nomeParte property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeParte(String value) {
		this.nomeParte = value;
	}

	/**
	 * Gets the value of the numeroOab property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumeroOab() {
		return numeroOab;
	}

	/**
	 * Sets the value of the numeroOab property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumeroOab(String value) {
		this.numeroOab = value;
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
	 * Gets the value of the ufOab property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public Estado getUfOab() {
		return ufOab;
	}

	/**
	 * Sets the value of the ufOab property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUfOab(Estado value) {
		this.ufOab = value;
	}

}
