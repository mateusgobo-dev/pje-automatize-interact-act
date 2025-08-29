package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for tipoSexoPessoa.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="tipoSexoPessoa">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="MASCULINO"/>
 *     &lt;enumeration value="FEMININO"/>
 *     &lt;enumeration value="TRANSEXUAL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tipoSexoPessoa")
@XmlEnum
public enum MPWSTipoSexoPessoa {

	MASCULINO, FEMININO, TRANSEXUAL;

	public String value() {
		return name();
	}

	public static MPWSTipoSexoPessoa fromValue(String v) {
		return valueOf(v);
	}

}
