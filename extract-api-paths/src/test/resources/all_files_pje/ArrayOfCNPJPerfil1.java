package br.jus.cnj.pje.webservice.client.consultacnpj;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ArrayOfCNPJPerfil1 complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfCNPJPerfil1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CNPJPerfil1" type="{nsProxyRFBCNJ}CNPJPerfil1" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfCNPJPerfil1", propOrder = { "cnpjPerfil1" })
public class ArrayOfCNPJPerfil1 {

	@XmlElement(name = "CNPJPerfil1", nillable = true)
	protected List<CNPJPerfil1> cnpjPerfil1;

	/**
	 * Gets the value of the cnpjPerfil1 property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the cnpjPerfil1 property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCNPJPerfil1().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link CNPJPerfil1 }
	 * 
	 * 
	 */
	public List<CNPJPerfil1> getCNPJPerfil1() {
		if (cnpjPerfil1 == null) {
			cnpjPerfil1 = new ArrayList<CNPJPerfil1>();
		}
		return this.cnpjPerfil1;
	}

}
