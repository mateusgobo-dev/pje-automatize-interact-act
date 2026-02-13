package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for orgaoJtrVO complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="orgaoJtrVO">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="j" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OOOO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TR" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orgaoJtrVO", propOrder = { "j", "oooo", "tr" })
public class MPWSOrgaoJtrVO {

	protected String j;
	@XmlElement(name = "OOOO")
	protected String oooo;
	@XmlElement(name = "TR")
	protected String tr;

	/**
	 * Gets the value of the j property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getJ() {
		return j;
	}

	/**
	 * Sets the value of the j property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setJ(String value) {
		this.j = value;
	}

	/**
	 * Gets the value of the oooo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOOOO() {
		return oooo;
	}

	/**
	 * Sets the value of the oooo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOOOO(String value) {
		this.oooo = value;
	}

	/**
	 * Gets the value of the tr property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTR() {
		return tr;
	}

	/**
	 * Sets the value of the tr property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTR(String value) {
		this.tr = value;
	}

}
