package br.jus.cnj.pje.webservice.client.consultacpf;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ArrayOfPessoaPerfil3 complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfPessoaPerfil3">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PessoaPerfil3" type="{nsProxyRFBCNJ}PessoaPerfil3" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfPessoaPerfil3", propOrder = { "pessoaPerfil3" })
public class ArrayOfPessoaPerfil3 {

	@XmlElement(name = "PessoaPerfil3", nillable = true)
	protected List<PessoaPerfil3> pessoaPerfil3;

	/**
	 * Gets the value of the pessoaPerfil3 property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the pessoaPerfil3 property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPessoaPerfil3().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link PessoaPerfil3 }
	 * 
	 * 
	 */
	public List<PessoaPerfil3> getPessoaPerfil3() {
		if (pessoaPerfil3 == null) {
			pessoaPerfil3 = new ArrayList<PessoaPerfil3>();
		}
		return this.pessoaPerfil3;
	}

}
