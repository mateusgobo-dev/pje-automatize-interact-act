package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for tipoPrisaoMandado.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="tipoPrisaoMandado">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="TEMPORARIA"/>
 *     &lt;enumeration value="PREVENTIVA"/>
 *     &lt;enumeration value="PREVENTIVA_DECISAO_CONDENATORIA"/>
 *     &lt;enumeration value="DEFINITIVA"/>
 *     &lt;enumeration value="DEPORTACAO"/>
 *     &lt;enumeration value="EXTRADICAO"/>
 *     &lt;enumeration value="EXPULSAO"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tipoPrisaoMandado")
@XmlEnum
public enum MPWSTipoPrisaoMandado {

	TEMPORARIA, PREVENTIVA, PREVENTIVA_DECISAO_CONDENATORIA, DEFINITIVA, DEPORTACAO, EXTRADICAO, EXPULSAO;

	public String value() {
		return name();
	}

	public static MPWSTipoPrisaoMandado fromValue(String v) {
		return valueOf(v);
	}

}
