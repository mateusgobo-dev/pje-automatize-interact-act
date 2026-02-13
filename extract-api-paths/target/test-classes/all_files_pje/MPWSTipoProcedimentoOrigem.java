package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for tipoProcedimentoOrigem.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="tipoProcedimentoOrigem">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AUTO_PRISAO_FLAGRANTE"/>
 *     &lt;enumeration value="BOLETIM_OCORRENCIA"/>
 *     &lt;enumeration value="INQUERITO_POLICIAL"/>
 *     &lt;enumeration value="TERMO_CIRCUNSTANCIADO_OCORRENCIA"/>
 *     &lt;enumeration value="OUTRO"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tipoProcedimentoOrigem")
@XmlEnum
public enum MPWSTipoProcedimentoOrigem {

	AUTO_PRISAO_FLAGRANTE, BOLETIM_OCORRENCIA, INQUERITO_POLICIAL, TERMO_CIRCUNSTANCIADO_OCORRENCIA, OUTRO;

	public String value() {
		return name();
	}

	public static MPWSTipoProcedimentoOrigem fromValue(String v) {
		return valueOf(v);
	}

}
