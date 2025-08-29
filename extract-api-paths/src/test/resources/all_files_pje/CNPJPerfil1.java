package br.jus.cnj.pje.webservice.client.consultacnpj;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for CNPJPerfil1 complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CNPJPerfil1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CNPJ" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Estabelecimento" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomeEmpresarial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomeFantasia" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SituacaoCadastral" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CidadeExterior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CodigoPais" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomePais" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Erro" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CNPJPerfil1", propOrder = { "cnpj", "estabelecimento", "nomeEmpresarial", "nomeFantasia",
		"situacaoCadastral", "cidadeExterior", "codigoPais", "nomePais", "erro" })
public class CNPJPerfil1 {

	@XmlElement(name = "CNPJ")
	protected String cnpj;
	@XmlElement(name = "Estabelecimento")
	protected String estabelecimento;
	@XmlElement(name = "NomeEmpresarial")
	protected String nomeEmpresarial;
	@XmlElement(name = "NomeFantasia")
	protected String nomeFantasia;
	@XmlElement(name = "SituacaoCadastral")
	protected String situacaoCadastral;
	@XmlElement(name = "CidadeExterior")
	protected String cidadeExterior;
	@XmlElement(name = "CodigoPais")
	protected String codigoPais;
	@XmlElement(name = "NomePais")
	protected String nomePais;
	@XmlElement(name = "Erro")
	protected String erro;

	/**
	 * Gets the value of the cnpj property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCNPJ() {
		return cnpj;
	}

	/**
	 * Sets the value of the cnpj property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCNPJ(String value) {
		this.cnpj = value;
	}

	/**
	 * Gets the value of the estabelecimento property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEstabelecimento() {
		return estabelecimento;
	}

	/**
	 * Sets the value of the estabelecimento property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEstabelecimento(String value) {
		this.estabelecimento = value;
	}

	/**
	 * Gets the value of the nomeEmpresarial property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeEmpresarial() {
		return nomeEmpresarial;
	}

	/**
	 * Sets the value of the nomeEmpresarial property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeEmpresarial(String value) {
		this.nomeEmpresarial = value;
	}

	/**
	 * Gets the value of the nomeFantasia property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomeFantasia() {
		return nomeFantasia;
	}

	/**
	 * Sets the value of the nomeFantasia property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomeFantasia(String value) {
		this.nomeFantasia = value;
	}

	/**
	 * Gets the value of the situacaoCadastral property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSituacaoCadastral() {
		return situacaoCadastral;
	}

	/**
	 * Sets the value of the situacaoCadastral property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSituacaoCadastral(String value) {
		this.situacaoCadastral = value;
	}

	/**
	 * Gets the value of the cidadeExterior property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCidadeExterior() {
		return cidadeExterior;
	}

	/**
	 * Sets the value of the cidadeExterior property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCidadeExterior(String value) {
		this.cidadeExterior = value;
	}

	/**
	 * Gets the value of the codigoPais property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCodigoPais() {
		return codigoPais;
	}

	/**
	 * Sets the value of the codigoPais property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCodigoPais(String value) {
		this.codigoPais = value;
	}

	/**
	 * Gets the value of the nomePais property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNomePais() {
		return nomePais;
	}

	/**
	 * Sets the value of the nomePais property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNomePais(String value) {
		this.nomePais = value;
	}

	/**
	 * Gets the value of the erro property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getErro() {
		return erro;
	}

	/**
	 * Sets the value of the erro property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setErro(String value) {
		this.erro = value;
	}

}
