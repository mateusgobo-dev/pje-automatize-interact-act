package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for tipoMagistradoMandado.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="tipoMagistradoMandado">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="MINISTRO"/>
 *     &lt;enumeration value="DESEMBARGADOR"/>
 *     &lt;enumeration value="JUIZ_FEDERAL"/>
 *     &lt;enumeration value="JUIZ_DIREITO"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tipoMagistradoMandado")
@XmlEnum
public enum MPWSTipoMagistradoMandado {

	MINISTRO, DESEMBARGADOR, JUIZ_FEDERAL, JUIZ_DIREITO;

	public String value() {
		return name();
	}

	public static MPWSTipoMagistradoMandado fromValue(String v) {
		return valueOf(v);
	}

}
