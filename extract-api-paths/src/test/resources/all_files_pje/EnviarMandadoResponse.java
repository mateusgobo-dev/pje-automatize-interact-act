package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for enviarMandadoResponse complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="enviarMandadoResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://www.cnj.jus.br/mpws}comprovante" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "enviarMandadoResponse", propOrder = { "_return" })
public class EnviarMandadoResponse {

	@XmlElement(name = "return")
	protected MPWSComprovante _return;

	/**
	 * Gets the value of the return property.
	 * 
	 * @return possible object is {@link MPWSComprovante }
	 * 
	 */
	public MPWSComprovante getReturn() {
		return _return;
	}

	/**
	 * Sets the value of the return property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSComprovante }
	 * 
	 */
	public void setReturn(MPWSComprovante value) {
		this._return = value;
	}

}
