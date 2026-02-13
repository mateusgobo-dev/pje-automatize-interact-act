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
 *         &lt;element name="numrSegu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "numrSegu" })
@XmlRootElement(name = "RetornaImgsAdvogado")
public class RetornaImgsAdvogado {

	protected String numrSegu;

	/**
	 * Gets the value of the numrSegu property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNumrSegu() {
		return numrSegu;
	}

	/**
	 * Sets the value of the numrSegu property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNumrSegu(String value) {
		this.numrSegu = value;
	}

}
