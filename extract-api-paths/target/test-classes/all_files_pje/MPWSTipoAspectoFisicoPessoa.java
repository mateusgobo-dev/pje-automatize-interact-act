package br.jus.cnj.mandadoprisao.webservices;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for tipoAspectoFisicoPessoa.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="tipoAspectoFisicoPessoa">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ALTURA_1_60"/>
 *     &lt;enumeration value="ALTURA_1_70"/>
 *     &lt;enumeration value="ALTURA_1_80"/>
 *     &lt;enumeration value="ALTURA_1_90"/>
 *     &lt;enumeration value="ALTURA_Mais_DE_1_90"/>
 *     &lt;enumeration value="BARBA_CAVANHAQUE"/>
 *     &lt;enumeration value="BARBA_CHEIA"/>
 *     &lt;enumeration value="BARBA_IMBERBE"/>
 *     &lt;enumeration value="BARBA_RALA"/>
 *     &lt;enumeration value="BARBA_RASPADA"/>
 *     &lt;enumeration value="BIGODE_COMPRIDO"/>
 *     &lt;enumeration value="BIGODE_CURTO"/>
 *     &lt;enumeration value="BIGODE_FINO"/>
 *     &lt;enumeration value="BIGODE_GROSSO"/>
 *     &lt;enumeration value="BIGODE_NORMAL"/>
 *     &lt;enumeration value="BIGODE_RASPADO"/>
 *     &lt;enumeration value="BOCA_GRANDE"/>
 *     &lt;enumeration value="BOCA_MEDIA"/>
 *     &lt;enumeration value="BOCA_PEQUENA"/>
 *     &lt;enumeration value="COMPLEIO_GORDO"/>
 *     &lt;enumeration value="COMPLEIO_MAGRO"/>
 *     &lt;enumeration value="COMPLEIO_MEDIO"/>
 *     &lt;enumeration value="COMPLEIO_RAQUITICO"/>
 *     &lt;enumeration value="COMPLEIO_TRONCUDO"/>
 *     &lt;enumeration value="TIPO_CABELO_CALVO"/>
 *     &lt;enumeration value="TIPO_CABELO_CRESPOS"/>
 *     &lt;enumeration value="TIPO_CABELO_ENCARACOLADOS"/>
 *     &lt;enumeration value="TIPO_CABELO_LISOS"/>
 *     &lt;enumeration value="TIPO_CABELO_ONDULADOS"/>
 *     &lt;enumeration value="COR_CABELO_BRANCOS"/>
 *     &lt;enumeration value="COR_CABELO_CASTANHOS"/>
 *     &lt;enumeration value="COR_CABELO_GRISALHOS"/>
 *     &lt;enumeration value="COR_CABELO_LOUROS"/>
 *     &lt;enumeration value="COR_CABELO_PRETOS"/>
 *     &lt;enumeration value="COR_CABELO_RUIVOS"/>
 *     &lt;enumeration value="COR_OLHOS_AZUIS"/>
 *     &lt;enumeration value="COR_OLHOS_CASTANHOS"/>
 *     &lt;enumeration value="COR_OLHOS_MISTOS"/>
 *     &lt;enumeration value="COR_OLHOS_PRETOS"/>
 *     &lt;enumeration value="COR_OLHOS_VERDES"/>
 *     &lt;enumeration value="FORMATO_OLHOS_GRANDES"/>
 *     &lt;enumeration value="FORMATO_OLHOS_ORIENTAIS"/>
 *     &lt;enumeration value="FORMATO_OLHOS_PEQUENOS"/>
 *     &lt;enumeration value="FORMATO_OLHOS_REDONDOS"/>
 *     &lt;enumeration value="COR_PELE_AMARELA"/>
 *     &lt;enumeration value="COR_PELE_BRANCA"/>
 *     &lt;enumeration value="COR_PELE_INDIGENA"/>
 *     &lt;enumeration value="COR_PELE_NEGRA"/>
 *     &lt;enumeration value="COR_PELE_OUTRAS"/>
 *     &lt;enumeration value="COR_PELE_PARDA"/>
 *     &lt;enumeration value="LABIOS_FINOS"/>
 *     &lt;enumeration value="LABIOS_GROSSOS"/>
 *     &lt;enumeration value="LABIOS_LEPORINOS"/>
 *     &lt;enumeration value="LABIOS_MEDIOS"/>
 *     &lt;enumeration value="NARIZ_ACHATADO"/>
 *     &lt;enumeration value="NARIZ_AFILADO"/>
 *     &lt;enumeration value="NARIZ_ARREBITADO"/>
 *     &lt;enumeration value="NARIZ_COMPRIDO"/>
 *     &lt;enumeration value="NARIZ_CURVO_ADUNCO"/>
 *     &lt;enumeration value="NARIZ_PEQUENO"/>
 *     &lt;enumeration value="ORELHAS_ABERTAS"/>
 *     &lt;enumeration value="ORELHAS_COLADAS"/>
 *     &lt;enumeration value="ORELHAS_GRANDES"/>
 *     &lt;enumeration value="ORELHAS_MEDIAS"/>
 *     &lt;enumeration value="ORELHAS_PEQUENAS"/>
 *     &lt;enumeration value="PESCOCO_COMPRIDO"/>
 *     &lt;enumeration value="PESCOCO_CURTO"/>
 *     &lt;enumeration value="PESCOCO_FINO"/>
 *     &lt;enumeration value="PESCOCO_GROSSO"/>
 *     &lt;enumeration value="PESCOCO_MEDIO"/>
 *     &lt;enumeration value="ROSTO_OVAL"/>
 *     &lt;enumeration value="ROSTO_QUADRADO"/>
 *     &lt;enumeration value="ROSTO_REDONDO"/>
 *     &lt;enumeration value="ROSTO_TRIANGULAR"/>
 *     &lt;enumeration value="SOBRANCELHAS_FINAS"/>
 *     &lt;enumeration value="SOBRANCELHAS_GROSSAS"/>
 *     &lt;enumeration value="SOBRANCELHAS_SEPARADAS"/>
 *     &lt;enumeration value="SOBRANCELHAS_UNIDAS"/>
 *     &lt;enumeration value="TESTA_ALTA"/>
 *     &lt;enumeration value="TESTA_COM_ENTRADAS"/>
 *     &lt;enumeration value="TESTA_CURTA"/>
 *     &lt;enumeration value="OUTROS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tipoAspectoFisicoPessoa")
@XmlEnum
public enum MPWSTipoAspectoFisicoPessoa {

	ALTURA_1_60("ALTURA_1_60"), ALTURA_1_70("ALTURA_1_70"), ALTURA_1_80("ALTURA_1_80"), ALTURA_1_90("ALTURA_1_90"), @XmlEnumValue("ALTURA_Mais_DE_1_90")
	ALTURA_MAIS_DE_1_90("ALTURA_Mais_DE_1_90"), BARBA_CAVANHAQUE("BARBA_CAVANHAQUE"), BARBA_CHEIA("BARBA_CHEIA"), BARBA_IMBERBE(
			"BARBA_IMBERBE"), BARBA_RALA("BARBA_RALA"), BARBA_RASPADA("BARBA_RASPADA"), BIGODE_COMPRIDO(
			"BIGODE_COMPRIDO"), BIGODE_CURTO("BIGODE_CURTO"), BIGODE_FINO("BIGODE_FINO"), BIGODE_GROSSO("BIGODE_GROSSO"), BIGODE_NORMAL(
			"BIGODE_NORMAL"), BIGODE_RASPADO("BIGODE_RASPADO"), BOCA_GRANDE("BOCA_GRANDE"), BOCA_MEDIA("BOCA_MEDIA"), BOCA_PEQUENA(
			"BOCA_PEQUENA"), COMPLEIO_GORDO("COMPLEIO_GORDO"), COMPLEIO_MAGRO("COMPLEIO_MAGRO"), COMPLEIO_MEDIO(
			"COMPLEIO_MEDIO"), COMPLEIO_RAQUITICO("COMPLEIO_RAQUITICO"), COMPLEIO_TRONCUDO("COMPLEIO_TRONCUDO"), TIPO_CABELO_CALVO(
			"TIPO_CABELO_CALVO"), TIPO_CABELO_CRESPOS("TIPO_CABELO_CRESPOS"), TIPO_CABELO_ENCARACOLADOS(
			"TIPO_CABELO_ENCARACOLADOS"), TIPO_CABELO_LISOS("TIPO_CABELO_LISOS"), TIPO_CABELO_ONDULADOS(
			"TIPO_CABELO_ONDULADOS"), COR_CABELO_BRANCOS("COR_CABELO_BRANCOS"), COR_CABELO_CASTANHOS(
			"COR_CABELO_CASTANHOS"), COR_CABELO_GRISALHOS("COR_CABELO_GRISALHOS"), COR_CABELO_LOUROS(
			"COR_CABELO_LOUROS"), COR_CABELO_PRETOS("COR_CABELO_PRETOS"), COR_CABELO_RUIVOS("COR_CABELO_RUIVOS"), COR_OLHOS_AZUIS(
			"COR_OLHOS_AZUIS"), COR_OLHOS_CASTANHOS("COR_OLHOS_CASTANHOS"), COR_OLHOS_MISTOS("COR_OLHOS_MISTOS"), COR_OLHOS_PRETOS(
			"COR_OLHOS_PRETOS"), COR_OLHOS_VERDES("COR_OLHOS_VERDES"), FORMATO_OLHOS_GRANDES("FORMATO_OLHOS_GRANDES"), FORMATO_OLHOS_ORIENTAIS(
			"FORMATO_OLHOS_ORIENTAIS"), FORMATO_OLHOS_PEQUENOS("FORMATO_OLHOS_PEQUENOS"), FORMATO_OLHOS_REDONDOS(
			"FORMATO_OLHOS_REDONDOS"), COR_PELE_AMARELA("COR_PELE_AMARELA"), COR_PELE_BRANCA("COR_PELE_BRANCA"), COR_PELE_INDIGENA(
			"COR_PELE_INDIGENA"), COR_PELE_NEGRA("COR_PELE_NEGRA"), COR_PELE_OUTRAS("COR_PELE_OUTRAS"), COR_PELE_PARDA(
			"COR_PELE_PARDA"), LABIOS_FINOS("LABIOS_FINOS"), LABIOS_GROSSOS("LABIOS_GROSSOS"), LABIOS_LEPORINOS(
			"LABIOS_LEPORINOS"), LABIOS_MEDIOS("LABIOS_MEDIOS"), NARIZ_ACHATADO("NARIZ_ACHATADO"), NARIZ_AFILADO(
			"NARIZ_AFILADO"), NARIZ_ARREBITADO("NARIZ_ARREBITADO"), NARIZ_COMPRIDO("NARIZ_COMPRIDO"), NARIZ_CURVO_ADUNCO(
			"NARIZ_CURVO_ADUNCO"), NARIZ_PEQUENO("NARIZ_PEQUENO"), ORELHAS_ABERTAS("ORELHAS_ABERTAS"), ORELHAS_COLADAS(
			"ORELHAS_COLADAS"), ORELHAS_GRANDES("ORELHAS_GRANDES"), ORELHAS_MEDIAS("ORELHAS_MEDIAS"), ORELHAS_PEQUENAS(
			"ORELHAS_PEQUENAS"), PESCOCO_COMPRIDO("PESCOCO_COMPRIDO"), PESCOCO_CURTO("PESCOCO_CURTO"), PESCOCO_FINO(
			"PESCOCO_FINO"), PESCOCO_GROSSO("PESCOCO_GROSSO"), PESCOCO_MEDIO("PESCOCO_MEDIO"), ROSTO_OVAL("ROSTO_OVAL"), ROSTO_QUADRADO(
			"ROSTO_QUADRADO"), ROSTO_REDONDO("ROSTO_REDONDO"), ROSTO_TRIANGULAR("ROSTO_TRIANGULAR"), SOBRANCELHAS_FINAS(
			"SOBRANCELHAS_FINAS"), SOBRANCELHAS_GROSSAS("SOBRANCELHAS_GROSSAS"), SOBRANCELHAS_SEPARADAS(
			"SOBRANCELHAS_SEPARADAS"), SOBRANCELHAS_UNIDAS("SOBRANCELHAS_UNIDAS"), TESTA_ALTA("TESTA_ALTA"), TESTA_COM_ENTRADAS(
			"TESTA_COM_ENTRADAS"), TESTA_CURTA("TESTA_CURTA"), OUTROS("OUTROS");
	private final String value;

	MPWSTipoAspectoFisicoPessoa(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static MPWSTipoAspectoFisicoPessoa fromValue(String v) {
		for (MPWSTipoAspectoFisicoPessoa c : MPWSTipoAspectoFisicoPessoa.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
