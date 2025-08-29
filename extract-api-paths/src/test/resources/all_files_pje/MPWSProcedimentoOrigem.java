package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for procedimentoOrigem complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="procedimentoOrigem">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="nomeTipoProcedimento" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroProcedimento" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="tipoProcedimentoOrigem" type="{http://www.cnj.jus.br/mpws}tipoProcedimentoOrigem" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "procedimentoOrigem", propOrder = { "nomeTipoProcedimento", "numeroProcedimento",
		"tipoProcedimentoOrigem" })
public class MPWSProcedimentoOrigem {

	protected String nomeTipoProcedimento;
	protected Long numeroProcedimento;
	protected MPWSTipoProcedimentoOrigem tipoProcedimentoOrigem;

	/**
	 * Gets the value of the nomeTipoProcedimento property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeTipoProcedimento() {
		return nomeTipoProcedimento;
	}

	/**
	 * Sets the value of the nomeTipoProcedimento property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeTipoProcedimento(String value) {
		this.nomeTipoProcedimento = value;
	}

	/**
	 * Gets the value of the numeroProcedimento property.
	 * 
	 * @return possible object is {@link Long }
	 * 
	 */
	public Long getNumeroProcedimento() {
		return numeroProcedimento;
	}

	/**
	 * Sets the value of the numeroProcedimento property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setNumeroProcedimento(Long value) {
		this.numeroProcedimento = value;
	}

	/**
	 * Gets the value of the tipoProcedimentoOrigem property.
	 * 
	 * @return possible object is {@link MPWSTipoProcedimentoOrigem }
	 * 
	 */
	public MPWSTipoProcedimentoOrigem getTipoProcedimentoOrigem() {
		return tipoProcedimentoOrigem;
	}

	/**
	 * Sets the value of the tipoProcedimentoOrigem property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSTipoProcedimentoOrigem }
	 * 
	 */
	public void setTipoProcedimentoOrigem(MPWSTipoProcedimentoOrigem value) {
		this.tipoProcedimentoOrigem = value;
	}

}
