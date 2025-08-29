package br.jus.cnj.mandadoprisao.webservices;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enviarMandado complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="enviarMandado">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mandados" type="{http://www.cnj.jus.br/mpws}mandadoPrisao" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="chaveAcesso" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "enviarMandado", propOrder = {
    "mandados",
    "chaveAcesso"
})
public class EnviarMandado {

    protected List<MPWSMandadoPrisao> mandados;
    protected String chaveAcesso;

    /**
     * Gets the value of the mandados property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mandados property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMandados().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MandadoPrisao }
     * 
     * 
     */
    public List<MPWSMandadoPrisao> getMandados() {
        if (mandados == null) {
            mandados = new ArrayList<MPWSMandadoPrisao>();
        }
        return this.mandados;
    }

    /**
     * Gets the value of the chaveAcesso property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChaveAcesso() {
        return chaveAcesso;
    }

    /**
     * Sets the value of the chaveAcesso property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChaveAcesso(String value) {
        this.chaveAcesso = value;
    }

}
