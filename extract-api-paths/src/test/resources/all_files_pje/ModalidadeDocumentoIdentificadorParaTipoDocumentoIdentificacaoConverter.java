/**
 * ModalidadeDocumentoIdentificadorParaTipoDocumentoIdentificacaoConverter.java
 * 
 * Data de criação: 14/06/2018
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeDocumentoIdentificador;
import br.jus.cnj.intercomunicacao.v222.beans.TipoQualificacaoPessoa;

/**
 * Conversor de ModalidadeDocumentoIdentificadorConverter para código do TipoDocumentoIdentificacao.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name(ModalidadeDocumentoIdentificadorParaTipoDocumentoIdentificacaoConverter.NAME)
public class ModalidadeDocumentoIdentificadorParaTipoDocumentoIdentificacaoConverter
		extends
		IntercomunicacaoConverterAbstrato<ModalidadeDocumentoIdentificador, String> {

	public static final String NAME = "v222.modalidadeDocumentoIdentificadorParaTipoDocumentoIdentificacaoConverter";
	
	@Override
	public String converter(ModalidadeDocumentoIdentificador identificacao) {
		return converter(identificacao, TipoQualificacaoPessoa.FISICA);
	}
	
	public String converter(ModalidadeDocumentoIdentificador identificacao, TipoQualificacaoPessoa qualificacaoPessoa) {
		String resultado = null;
		
		if (isNotNull(identificacao, qualificacaoPessoa)) {
			
			resultado = getMapaModalidadeParaTipoDocumento(qualificacaoPessoa).get(identificacao);
		}
		return resultado;
	}
	
	protected Map<ModalidadeDocumentoIdentificador, String> getMapaModalidadeParaTipoDocumento(TipoQualificacaoPessoa qualificacaoPessoa) {
		Map<ModalidadeDocumentoIdentificador, String> mapa = new HashMap<ModalidadeDocumentoIdentificador, String>();
		switch (qualificacaoPessoa) {
		case JURIDICA:
			mapa.put(ModalidadeDocumentoIdentificador.CMF, "CPJ");
			mapa.put(ModalidadeDocumentoIdentificador.RJC, "RJC");
		case AUTORIDADE:
		case ORGAOREPRESENTACAO:
			mapa.put(ModalidadeDocumentoIdentificador.CMF, "CPJ");
			mapa.put(ModalidadeDocumentoIdentificador.RJC, "RJC");
			break;
		case FISICA:
		default:
			mapa.put(ModalidadeDocumentoIdentificador.CMF, "CPF");
			mapa.put(ModalidadeDocumentoIdentificador.CI, "RG");
			mapa.put(ModalidadeDocumentoIdentificador.PAS, "PAS");
			mapa.put(ModalidadeDocumentoIdentificador.TE, "TIT");
			mapa.put(ModalidadeDocumentoIdentificador.CNH, "CNH");
			mapa.put(ModalidadeDocumentoIdentificador.CN, "CNA");
			mapa.put(ModalidadeDocumentoIdentificador.CC, "CCA");
			mapa.put(ModalidadeDocumentoIdentificador.CT, "CTR");
			mapa.put(ModalidadeDocumentoIdentificador.RIC, "RIC");
			mapa.put(ModalidadeDocumentoIdentificador.PIS_PASEP, "PIS");
			mapa.put(ModalidadeDocumentoIdentificador.CEI, "CEI");
			mapa.put(ModalidadeDocumentoIdentificador.NIT, "NIT");
			mapa.put(ModalidadeDocumentoIdentificador.CP, "CCP");
			mapa.put(ModalidadeDocumentoIdentificador.IF, "IDF");
			mapa.put(ModalidadeDocumentoIdentificador.OAB, "OAB");
			mapa.put(ModalidadeDocumentoIdentificador.RJC, "RJC");
			mapa.put(ModalidadeDocumentoIdentificador.RGE, "RGE");
		}
		return mapa;
	}
}
