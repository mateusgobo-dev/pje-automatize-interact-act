package br.jus.cnj.mandadoprisao.webservices;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for processoMandadoPrisao complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="processoMandadoPrisao">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codigoAssuntos" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="codigoClasseProcesual" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="numeroProcessoR65" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processoMandadoPrisao", propOrder = { "codigoAssuntos", "codigoClasseProcesual", "numeroProcessoR65" })
public class MPWSProcessoMandadoPrisao {

	@XmlElement(nillable = true)
	protected List<Long> codigoAssuntos;
	protected Long codigoClasseProcesual;
	protected String numeroProcessoR65;

	/**
	 * Gets the value of the codigoAssuntos property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the codigoAssuntos property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCodigoAssuntos().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Long }
	 * 
	 * 
	 */
	public List<Long> getCodigoAssuntos() {
		if (codigoAssuntos == null) {
			codigoAssuntos = new ArrayList<Long>();
		}
		return this.codigoAssuntos;
	}

	/**
	 * Gets the value of the codigoClasseProcesual property.
	 * 
	 * @return possible object is {@link Long }
	 * 
	 */
	public Long getCodigoClasseProcesual() {
		return codigoClasseProcesual;
	}

	/**
	 * Sets the value of the codigoClasseProcesual property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setCodigoClasseProcesual(Long value) {
		this.codigoClasseProcesual = value;
	}

	/**
	 * Gets the value of the numeroProcessoR65 property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumeroProcessoR65() {
		return numeroProcessoR65;
	}

	/**
	 * Sets the value of the numeroProcessoR65 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumeroProcessoR65(String value) {
		this.numeroProcessoR65 = value;
	}

}
