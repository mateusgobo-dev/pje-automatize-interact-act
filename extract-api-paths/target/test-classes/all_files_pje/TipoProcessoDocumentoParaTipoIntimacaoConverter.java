/**
 * TipoProcessoDocumentoParaTipoIntimacaoConverter.java
 * 
 * Data de criação: 15/10/2024
 */
package br.jus.cnj.pje.webservice.client.domicilioeletronico.converter;

import java.util.HashMap;
import java.util.Map;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.enums.TipoIntimacaoEnum;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

/**
 * Conversor de TipoProcessoDocumento para TipoIntimacaoEnum.
 * 
 * @author Adriano Pamplona
 */
public class TipoProcessoDocumentoParaTipoIntimacaoConverter {

	/**
	 * Converte TipoProcessoDocumento para TipoIntimacaoEnum.
	 * 
	 * @param tipoProcessoDocumento
	 * @return TipoIntimacaoEnum
	 */
	public TipoIntimacaoEnum converter(TipoProcessoDocumento tipoProcessoDocumento) {
		TipoIntimacaoEnum resultado = null;

		if (tipoProcessoDocumento != null) {

			Map<Integer, TipoIntimacaoEnum> mapa = new HashMap<>();
			mapa.put(getIdTipoProcessoDocumentoLiminar(), TipoIntimacaoEnum.LIMINAR);
			mapa.put(getIdTipoProcessoDocumentoObrigacaoDeFazer(), TipoIntimacaoEnum.OBRIGACAO_DE_FAZER);
			mapa.put(getIdTipoProcessoDocumentoSentenca(), TipoIntimacaoEnum.SENTENCA);
			mapa.put(getIdTipoProcessoDocumentoAcordao(), TipoIntimacaoEnum.ACORDAO);
			mapa.put(getIdTipoProcessoDocumentoTransitoEmJulgado(), TipoIntimacaoEnum.TRANSITO_EM_JULGADO);
			mapa.put(getIdTipoProcessoDocumentoOficio(), TipoIntimacaoEnum.OFICIO);

			resultado = mapa.get(tipoProcessoDocumento.getIdTipoProcessoDocumento());
		}
		return resultado;
	}

	/**
	 * @return Tipo de documento Liminar
	 */
	protected Integer getIdTipoProcessoDocumentoLiminar() {
		TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoLiminar();
		return (tipo != null ? tipo.getIdTipoProcessoDocumento() : null);
	}

	/**
	 * @return Tipo de documento Obrigação de fazer.
	 */
	protected Integer getIdTipoProcessoDocumentoObrigacaoDeFazer() {
		TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoObrigacaoDeFazer();
		return (tipo != null ? tipo.getIdTipoProcessoDocumento() : null);
	}

	/**
	 * @return Tipo de documento Sentença
	 */
	protected Integer getIdTipoProcessoDocumentoSentenca() {
		TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoSentenca();
		return (tipo != null ? tipo.getIdTipoProcessoDocumento() : null);
	}

	/**
	 * @return Tipo de documento Acordão
	 */
	protected Integer getIdTipoProcessoDocumentoAcordao() {
		TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoAcordao();
		return (tipo != null ? tipo.getIdTipoProcessoDocumento() : null);
	}

	/**
	 * @return Tipo de documento Trânsito em Julgado
	 */
	protected Integer getIdTipoProcessoDocumentoTransitoEmJulgado() {
		TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoTransitoJulgado();
		return (tipo != null ? tipo.getIdTipoProcessoDocumento() : null);
	}

	/**
	 * @return Tipo de documento Ofício
	 */
	protected Integer getIdTipoProcessoDocumentoOficio() {
		TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoOficio();
		return (tipo != null ? tipo.getIdTipoProcessoDocumento() : null);
	}
}
