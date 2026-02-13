package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for tipoSituacaoMandado.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="tipoSituacaoMandado">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AGUARDANDO_CUMPRIMENTO"/>
 *     &lt;enumeration value="CUMPRIDO"/>
 *     &lt;enumeration value="REVOGADO"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tipoSituacaoMandado")
@XmlEnum
public enum MPWSTipoSituacaoMandado {

	AGUARDANDO_CUMPRIMENTO, CUMPRIDO, REVOGADO;

	public String value() {
		return name();
	}

	public static MPWSTipoSituacaoMandado fromValue(String v) {
		return valueOf(v);
	}

}
