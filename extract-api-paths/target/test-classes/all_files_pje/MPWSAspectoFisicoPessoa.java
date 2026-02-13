package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for aspectoFisicoPessoa complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="aspectoFisicoPessoa">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="observacao" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tipoAspectoFisico" type="{http://www.cnj.jus.br/mpws}tipoAspectoFisicoPessoa" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "aspectoFisicoPessoa", propOrder = { "observacao", "tipoAspectoFisico" })
public class MPWSAspectoFisicoPessoa {

	protected String observacao;
	protected MPWSTipoAspectoFisicoPessoa tipoAspectoFisico;

	/**
	 * Gets the value of the observacao property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getObservacao() {
		return observacao;
	}

	/**
	 * Sets the value of the observacao property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setObservacao(String value) {
		this.observacao = value;
	}

	/**
	 * Gets the value of the tipoAspectoFisico property.
	 * 
	 * @return possible object is {@link MPWSTipoAspectoFisicoPessoa }
	 * 
	 */
	public MPWSTipoAspectoFisicoPessoa getTipoAspectoFisico() {
		return tipoAspectoFisico;
	}

	/**
	 * Sets the value of the tipoAspectoFisico property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSTipoAspectoFisicoPessoa }
	 * 
	 */
	public void setTipoAspectoFisico(MPWSTipoAspectoFisicoPessoa value) {
		this.tipoAspectoFisico = value;
	}

}
