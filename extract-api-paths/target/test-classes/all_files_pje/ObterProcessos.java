package br.com.infox.pje.webservice.consultaoutrasessao;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for obterProcessos complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="obterProcessos">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="consultaProcesso" type="{http://webservices.pje.infox.com.br/}beanConsultaProcesso" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "obterProcessos", propOrder = { "consultaProcesso" })
public class ObterProcessos {

	protected BeanConsultaProcesso consultaProcesso;

	/**
	 * Gets the value of the consultaProcesso property.
	 * 
	 * @return possible object is {@link BeanConsultaProcesso }
	 * 
	 */
	public BeanConsultaProcesso getConsultaProcesso() {
		return consultaProcesso;
	}

	/**
	 * Sets the value of the consultaProcesso property.
	 * 
	 * @param value
	 *            allowed object is {@link BeanConsultaProcesso }
	 * 
	 */
	public void setConsultaProcesso(BeanConsultaProcesso value) {
		this.consultaProcesso = value;
	}

}
