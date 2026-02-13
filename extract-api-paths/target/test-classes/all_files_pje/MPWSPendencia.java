package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for pendencia complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="pendencia">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="numeroMandado" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroProcesso" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pendencias" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pendencia", propOrder = { "numeroMandado", "numeroProcesso", "pendencias" })
public class MPWSPendencia {

	protected String numeroMandado;
	protected String numeroProcesso;
	protected String pendencias;

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
	 * Gets the value of the pendencias property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPendencias() {
		return pendencias;
	}

	/**
	 * Sets the value of the pendencias property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPendencias(String value) {
		this.pendencias = value;
	}

}
