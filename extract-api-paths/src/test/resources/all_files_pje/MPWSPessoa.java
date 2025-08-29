package br.jus.cnj.mandadoprisao.webservices;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for pessoa complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="pessoa">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="alcunhas" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="aspectosFisicos" type="{http://www.cnj.jus.br/mpws}aspectoFisicoPessoa" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="datasNascimento" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="documentos" type="{http://www.cnj.jus.br/mpws}documentoPessoa" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="enderecos" type="{http://www.cnj.jus.br/mpws}enderecoPessoa" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="fotografias" type="{http://www.w3.org/2001/XMLSchema}base64Binary" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="genitoras" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="genitores" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="nacionalidades" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="naturalidades" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="nomes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="observacao" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="profissoes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="sexos" type="{http://www.cnj.jus.br/mpws}tipoSexoPessoa" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pessoa", propOrder = { "alcunhas", "aspectosFisicos", "datasNascimento", "documentos", "enderecos",
		"fotografias", "genitoras", "genitores", "nacionalidades", "naturalidades", "nomes", "observacao",
		"profissoes", "sexos" })
public class MPWSPessoa {

	@XmlElement(nillable = true)
	protected List<String> alcunhas;
	@XmlElement(nillable = true)
	protected List<MPWSAspectoFisicoPessoa> aspectosFisicos;
	@XmlElement(nillable = true)
	protected List<String> datasNascimento;
	@XmlElement(nillable = true)
	protected List<MPWSDocumentoPessoa> documentos;
	@XmlElement(nillable = true)
	protected List<MPWSEnderecoPessoa> enderecos;
	@XmlElement(nillable = true)
	protected List<byte[]> fotografias;
	@XmlElement(nillable = true)
	protected List<String> genitoras;
	@XmlElement(nillable = true)
	protected List<String> genitores;
	@XmlElement(nillable = true)
	protected List<String> nacionalidades;
	@XmlElement(nillable = true)
	protected List<String> naturalidades;
	@XmlElement(nillable = true)
	protected List<String> nomes;
	protected String observacao;
	@XmlElement(nillable = true)
	protected List<String> profissoes;
	@XmlElement(nillable = true)
	protected List<MPWSTipoSexoPessoa> sexos;

	/**
	 * Gets the value of the alcunhas property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the alcunhas property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAlcunhas().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getAlcunhas() {
		if (alcunhas == null) {
			alcunhas = new ArrayList<String>();
		}
		return this.alcunhas;
	}

	/**
	 * Gets the value of the aspectosFisicos property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the aspectosFisicos property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAspectosFisicos().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link MPWSAspectoFisicoPessoa }
	 * 
	 * 
	 */
	public List<MPWSAspectoFisicoPessoa> getAspectosFisicos() {
		if (aspectosFisicos == null) {
			aspectosFisicos = new ArrayList<MPWSAspectoFisicoPessoa>();
		}
		return this.aspectosFisicos;
	}

	/**
	 * Gets the value of the datasNascimento property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the datasNascimento property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDatasNascimento().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getDatasNascimento() {
		if (datasNascimento == null) {
			datasNascimento = new ArrayList<String>();
		}
		return this.datasNascimento;
	}

	/**
	 * Gets the value of the documentos property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the documentos property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDocumentos().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link MPWSDocumentoPessoa }
	 * 
	 * 
	 */
	public List<MPWSDocumentoPessoa> getDocumentos() {
		if (documentos == null) {
			documentos = new ArrayList<MPWSDocumentoPessoa>();
		}
		return this.documentos;
	}

	/**
	 * Gets the value of the enderecos property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the enderecos property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getEnderecos().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link MPWSEnderecoPessoa }
	 * 
	 * 
	 */
	public List<MPWSEnderecoPessoa> getEnderecos() {
		if (enderecos == null) {
			enderecos = new ArrayList<MPWSEnderecoPessoa>();
		}
		return this.enderecos;
	}

	/**
	 * Gets the value of the fotografias property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the fotografias property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getFotografias().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list byte[]
	 * 
	 */
	public List<byte[]> getFotografias() {
		if (fotografias == null) {
			fotografias = new ArrayList<byte[]>();
		}
		return this.fotografias;
	}

	/**
	 * Gets the value of the genitoras property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the genitoras property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getGenitoras().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getGenitoras() {
		if (genitoras == null) {
			genitoras = new ArrayList<String>();
		}
		return this.genitoras;
	}

	/**
	 * Gets the value of the genitores property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the genitores property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getGenitores().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getGenitores() {
		if (genitores == null) {
			genitores = new ArrayList<String>();
		}
		return this.genitores;
	}

	/**
	 * Gets the value of the nacionalidades property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the nacionalidades property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getNacionalidades().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getNacionalidades() {
		if (nacionalidades == null) {
			nacionalidades = new ArrayList<String>();
		}
		return this.nacionalidades;
	}

	/**
	 * Gets the value of the naturalidades property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the naturalidades property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getNaturalidades().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getNaturalidades() {
		if (naturalidades == null) {
			naturalidades = new ArrayList<String>();
		}
		return this.naturalidades;
	}

	/**
	 * Gets the value of the nomes property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the nomes property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getNomes().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getNomes() {
		if (nomes == null) {
			nomes = new ArrayList<String>();
		}
		return this.nomes;
	}

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
	 * Gets the value of the profissoes property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the profissoes property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getProfissoes().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getProfissoes() {
		if (profissoes == null) {
			profissoes = new ArrayList<String>();
		}
		return this.profissoes;
	}

	/**
	 * Gets the value of the sexos property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the sexos property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSexos().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link MPWSTipoSexoPessoa }
	 * 
	 * 
	 */
	public List<MPWSTipoSexoPessoa> getSexos() {
		if (sexos == null) {
			sexos = new ArrayList<MPWSTipoSexoPessoa>();
		}
		return this.sexos;
	}

}
