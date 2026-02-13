package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for situacaoMandado complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="situacaoMandado">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="numeroMandado" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroProcessoR65" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tipoSituacao" type="{http://www.cnj.jus.br/mpws}tipoSituacaoMandado" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "situacaoMandado", propOrder = { "numeroMandado", "numeroProcessoR65", "tipoSituacao" })
public class MPWSSituacaoMandado {

	protected String numeroMandado;
	protected String numeroProcessoR65;
	protected MPWSTipoSituacaoMandado tipoSituacao;

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

	/**
	 * Gets the value of the tipoSituacao property.
	 * 
	 * @return possible object is {@link MPWSTipoSituacaoMandado }
	 * 
	 */
	public MPWSTipoSituacaoMandado getTipoSituacao() {
		return tipoSituacao;
	}

	/**
	 * Sets the value of the tipoSituacao property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSTipoSituacaoMandado }
	 * 
	 */
	public void setTipoSituacao(MPWSTipoSituacaoMandado value) {
		this.tipoSituacao = value;
	}

}
