package br.com.infox.pje.webservice.consultaoutrasessao;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for beanRespostaConsultaProcesso complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="beanRespostaConsultaProcesso">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="beanDadosProcessos" type="{http://webservices.pje.infox.com.br/}beanDadosProcesso" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="resposta" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "beanRespostaConsultaProcesso", propOrder = { "beanDadosProcessos", "resposta" })
public class BeanRespostaConsultaProcesso {

	@XmlElement(nillable = true)
	protected List<BeanDadosProcesso> beanDadosProcessos;
	protected String resposta;

	/**
	 * Gets the value of the beanDadosProcessos property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the beanDadosProcessos property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getBeanDadosProcessos().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link BeanDadosProcesso }
	 * 
	 * 
	 */
	public List<BeanDadosProcesso> getBeanDadosProcessos() {
		if (beanDadosProcessos == null) {
			beanDadosProcessos = new ArrayList<BeanDadosProcesso>();
		}
		return this.beanDadosProcessos;
	}

	/**
	 * Gets the value of the resposta property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getResposta() {
		return resposta;
	}

	/**
	 * Sets the value of the resposta property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setResposta(String value) {
		this.resposta = value;
	}

}
