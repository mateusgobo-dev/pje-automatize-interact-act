package br.jus.cnj.pje.webservice.client.consultacpf;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ArrayOfPessoaPerfil2 complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfPessoaPerfil2">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PessoaPerfil2" type="{nsProxyRFBCNJ}PessoaPerfil2" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfPessoaPerfil2", propOrder = { "pessoaPerfil2" })
public class ArrayOfPessoaPerfil2 {

	@XmlElement(name = "PessoaPerfil2", nillable = true)
	protected List<PessoaPerfil2> pessoaPerfil2;

	/**
	 * Gets the value of the pessoaPerfil2 property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the pessoaPerfil2 property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPessoaPerfil2().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link PessoaPerfil2 }
	 * 
	 * 
	 */
	public List<PessoaPerfil2> getPessoaPerfil2() {
		if (pessoaPerfil2 == null) {
			pessoaPerfil2 = new ArrayList<PessoaPerfil2>();
		}
		return this.pessoaPerfil2;
	}

}
