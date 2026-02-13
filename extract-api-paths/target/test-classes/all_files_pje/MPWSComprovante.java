package br.jus.cnj.mandadoprisao.webservices;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for comprovante complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="comprovante">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="comprovante" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="dataEnvio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroProtocolo" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="pendencias" type="{http://www.cnj.jus.br/mpws}pendencia" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="qtdMandadoEnviado" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="qtdMandadoPendente" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="qtdMandadoRecebido" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "comprovante", propOrder = { "comprovante", "dataEnvio", "numeroProtocolo", "pendencias",
		"qtdMandadoEnviado", "qtdMandadoPendente", "qtdMandadoRecebido" })
public class MPWSComprovante {

	protected byte[] comprovante;
	protected String dataEnvio;
	protected Long numeroProtocolo;
	@XmlElement(nillable = true)
	protected List<MPWSPendencia> pendencias;
	protected Integer qtdMandadoEnviado;
	protected Integer qtdMandadoPendente;
	protected Integer qtdMandadoRecebido;

	/**
	 * Gets the value of the comprovante property.
	 * 
	 * @return possible object is byte[]
	 */
	public byte[] getComprovante() {
		return comprovante;
	}

	/**
	 * Sets the value of the comprovante property.
	 * 
	 * @param value
	 *            allowed object is byte[]
	 */
	public void setComprovante(byte[] value) {
		this.comprovante = value;
	}

	/**
	 * Gets the value of the dataEnvio property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDataEnvio() {
		return dataEnvio;
	}

	/**
	 * Sets the value of the dataEnvio property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDataEnvio(String value) {
		this.dataEnvio = value;
	}

	/**
	 * Gets the value of the numeroProtocolo property.
	 * 
	 * @return possible object is {@link Long }
	 * 
	 */
	public Long getNumeroProtocolo() {
		return numeroProtocolo;
	}

	/**
	 * Sets the value of the numeroProtocolo property.
	 * 
	 * @param value
	 *            allowed object is {@link Long }
	 * 
	 */
	public void setNumeroProtocolo(Long value) {
		this.numeroProtocolo = value;
	}

	/**
	 * Gets the value of the pendencias property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the pendencias property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPendencias().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link MPWSPendencia }
	 * 
	 * 
	 */
	public List<MPWSPendencia> getPendencias() {
		if (pendencias == null) {
			pendencias = new ArrayList<MPWSPendencia>();
		}
		return this.pendencias;
	}

	/**
	 * Gets the value of the qtdMandadoEnviado property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getQtdMandadoEnviado() {
		return qtdMandadoEnviado;
	}

	/**
	 * Sets the value of the qtdMandadoEnviado property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setQtdMandadoEnviado(Integer value) {
		this.qtdMandadoEnviado = value;
	}

	/**
	 * Gets the value of the qtdMandadoPendente property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getQtdMandadoPendente() {
		return qtdMandadoPendente;
	}

	/**
	 * Sets the value of the qtdMandadoPendente property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setQtdMandadoPendente(Integer value) {
		this.qtdMandadoPendente = value;
	}

	/**
	 * Gets the value of the qtdMandadoRecebido property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getQtdMandadoRecebido() {
		return qtdMandadoRecebido;
	}

	/**
	 * Sets the value of the qtdMandadoRecebido property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setQtdMandadoRecebido(Integer value) {
		this.qtdMandadoRecebido = value;
	}

}
