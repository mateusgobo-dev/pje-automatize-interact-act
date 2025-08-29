package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for documentoPessoa complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="documentoPessoa">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="identificacao" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orgaoExpedidor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tipoDocumento" type="{http://www.cnj.jus.br/mpws}tipoDocumentoPessoa" minOccurs="0"/>
 *         &lt;element name="uf" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentoPessoa", propOrder = { "identificacao", "orgaoExpedidor", "tipoDocumento", "uf" })
public class MPWSDocumentoPessoa {

	protected String identificacao;
	protected String orgaoExpedidor;
	protected MPWSTipoDocumentoPessoa tipoDocumento;
	protected String uf;

	/**
	 * Gets the value of the identificacao property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIdentificacao() {
		return identificacao;
	}

	/**
	 * Sets the value of the identificacao property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIdentificacao(String value) {
		this.identificacao = value;
	}

	/**
	 * Gets the value of the orgaoExpedidor property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOrgaoExpedidor() {
		return orgaoExpedidor;
	}

	/**
	 * Sets the value of the orgaoExpedidor property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOrgaoExpedidor(String value) {
		this.orgaoExpedidor = value;
	}

	/**
	 * Gets the value of the tipoDocumento property.
	 * 
	 * @return possible object is {@link MPWSTipoDocumentoPessoa }
	 * 
	 */
	public MPWSTipoDocumentoPessoa getTipoDocumento() {
		return tipoDocumento;
	}

	/**
	 * Sets the value of the tipoDocumento property.
	 * 
	 * @param value
	 *            allowed object is {@link MPWSTipoDocumentoPessoa }
	 * 
	 */
	public void setTipoDocumento(MPWSTipoDocumentoPessoa value) {
		this.tipoDocumento = value;
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

}
