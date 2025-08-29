package br.org.oab.www5.consultanacionalws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="numInsc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="uf" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nome" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "numInsc", "uf", "nome" })
@XmlRootElement(name = "UfInscNome")
public class UfInscNome {

	protected String numInsc;
	protected String uf;
	protected String nome;

	/**
	 * Gets the value of the numInsc property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumInsc() {
		return numInsc;
	}

	/**
	 * Sets the value of the numInsc property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumInsc(String value) {
		this.numInsc = value;
	}

	/**
	 * Gets the value of the uf property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUf() {
		return uf;
	}

	/**
	 * Sets the value of the uf property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUf(String value) {
		this.uf = value;
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

}
