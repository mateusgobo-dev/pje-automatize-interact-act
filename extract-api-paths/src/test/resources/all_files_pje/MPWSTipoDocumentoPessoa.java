package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for tipoDocumentoPessoa.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="tipoDocumentoPessoa">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="RG"/>
 *     &lt;enumeration value="CPF"/>
 *     &lt;enumeration value="CNH"/>
 *     &lt;enumeration value="TITULO_ELEITOR"/>
 *     &lt;enumeration value="CERTIDAO_NASCIMENTO"/>
 *     &lt;enumeration value="CERTIDAO_CASAMENTO"/>
 *     &lt;enumeration value="PASSAPORTE"/>
 *     &lt;enumeration value="CARTEIRA_TRABALHO"/>
 *     &lt;enumeration value="RIC"/>
 *     &lt;enumeration value="CADASTRO_MINISTERIO_FAZENDA"/>
 *     &lt;enumeration value="PIS_PASEP"/>
 *     &lt;enumeration value="INSS"/>
 *     &lt;enumeration value="NUM_ID_TRABALHO"/>
 *     &lt;enumeration value="CONSELHO_PROFISSIONAL"/>
 *     &lt;enumeration value="IDENTIDADE_FUNCIONAL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tipoDocumentoPessoa")
@XmlEnum
public enum MPWSTipoDocumentoPessoa {

	RG, CPF, CNH, TITULO_ELEITOR, CERTIDAO_NASCIMENTO, CERTIDAO_CASAMENTO, PASSAPORTE, CARTEIRA_TRABALHO, RIC, CADASTRO_MINISTERIO_FAZENDA, PIS_PASEP, INSS, NUM_ID_TRABALHO, CONSELHO_PROFISSIONAL, IDENTIDADE_FUNCIONAL;

	public String value() {
		return name();
	}

	public static MPWSTipoDocumentoPessoa fromValue(String v) {
		return valueOf(v);
	}

}
