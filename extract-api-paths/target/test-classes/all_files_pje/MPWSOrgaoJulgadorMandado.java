package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for orgaoJulgadorMandado complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="orgaoJulgadorMandado">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codigo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="municipio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nome" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroGrauJurisdicao" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="UF" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orgaoJulgadorMandado", propOrder = { "codigo", "municipio", "nome", "numeroGrauJurisdicao", "uf" })
public class MPWSOrgaoJulgadorMandado {

	protected String codigo;
	protected String municipio;
	protected String nome;
	protected Integer numeroGrauJurisdicao;
	@XmlElement(name = "UF")
	protected String uf;

	/**
	 * Gets the value of the codigo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodigo() {
		return codigo;
	}

	/**
	 * Sets the value of the codigo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodigo(String value) {
		this.codigo = value;
	}

	/**
	 * Gets the value of the municipio property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMunicipio() {
		return municipio;
	}

	/**
	 * Sets the value of the municipio property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMunicipio(String value) {
		this.municipio = value;
	}

	/**
	 * Gets the value of the nome property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Sets the value of the nome property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNome(String value) {
		this.nome = value;
	}

	/**
	 * Gets the value of the numeroGrauJurisdicao property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getNumeroGrauJurisdicao() {
		return numeroGrauJurisdicao;
	}

	/**
	 * Sets the value of the numeroGrauJurisdicao property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setNumeroGrauJurisdicao(Integer value) {
		this.numeroGrauJurisdicao = value;
	}

	/**
	 * Gets the value of the uf property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUF() {
		return uf;
	}

	/**
	 * Sets the value of the uf property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUF(String value) {
		this.uf = value;
	}

}
