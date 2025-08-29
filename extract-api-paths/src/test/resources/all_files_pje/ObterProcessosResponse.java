package br.com.infox.pje.webservice.consultaoutrasessao;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for obterProcessosResponse complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="obterProcessosResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://webservices.pje.infox.com.br/}beanRespostaConsultaProcesso" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "obterProcessosResponse", propOrder = { "_return" })
public class ObterProcessosResponse {

	@XmlElement(name = "return")
	protected BeanRespostaConsultaProcesso _return;

	/**
	 * Gets the value of the return property.
	 * 
	 * @return possible object is {@link BeanRespostaConsultaProcesso }
	 * 
	 */
	public BeanRespostaConsultaProcesso getReturn() {
		return _return;
	}

	/**
	 * Sets the value of the return property.
	 * 
	 * @param value
	 *            allowed object is {@link BeanRespostaConsultaProcesso }
	 * 
	 */
	public void setReturn(BeanRespostaConsultaProcesso value) {
		this._return = value;
	}

}
