/**
 * TipoDocumentoIdentificacaoParaModalidadeDocumentoIdentificadorConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.HashMap;
import java.util.Map;

import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeDocumentoIdentificador;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;

/**
 * Conversor de TipoDocumentoIdentificacao para ModalidadeDocumentoIdentificadorConverter.
 * 
 * @author Adriano Pamplona
 */
public class TipoDocumentoIdentificacaoParaModalidadeDocumentoIdentificadorConverter
		extends
		IntercomunicacaoConverterAbstrato<TipoDocumentoIdentificacao, ModalidadeDocumentoIdentificador> {

	@Override
	public ModalidadeDocumentoIdentificador converter(TipoDocumentoIdentificacao identificacao) {
		ModalidadeDocumentoIdentificador resultado = null;
		
		if (isNotNull(identificacao)) {
			
			Map<String ,ModalidadeDocumentoIdentificador> mapa = new HashMap<String, ModalidadeDocumentoIdentificador>();
			mapa.put("CPF", ModalidadeDocumentoIdentificador.CMF);
			mapa.put("CPJ", ModalidadeDocumentoIdentificador.CMF);
			mapa.put("RG", ModalidadeDocumentoIdentificador.CI);
			mapa.put("PAS", ModalidadeDocumentoIdentificador.PAS);
			mapa.put("TIT", ModalidadeDocumentoIdentificador.TE);
			mapa.put("CNH", ModalidadeDocumentoIdentificador.CNH);
			mapa.put("CNA", ModalidadeDocumentoIdentificador.CN);
			mapa.put("CCA", ModalidadeDocumentoIdentificador.CC);
			mapa.put("CTR", ModalidadeDocumentoIdentificador.CT);
			mapa.put("RIC", ModalidadeDocumentoIdentificador.RIC);
			mapa.put("PIS", ModalidadeDocumentoIdentificador.PIS_PASEP);
			mapa.put("CEI", ModalidadeDocumentoIdentificador.CEI);
			mapa.put("NIT", ModalidadeDocumentoIdentificador.NIT);
			mapa.put("CCP", ModalidadeDocumentoIdentificador.CP);
			mapa.put("IDF", ModalidadeDocumentoIdentificador.IF);
			mapa.put("OAB", ModalidadeDocumentoIdentificador.OAB);
			mapa.put("RJC", ModalidadeDocumentoIdentificador.RJC);
			mapa.put("RGE", ModalidadeDocumentoIdentificador.RGE);
			
			resultado = mapa.get(identificacao.getCodTipo().trim());
		}
		return resultado;
	}
}
