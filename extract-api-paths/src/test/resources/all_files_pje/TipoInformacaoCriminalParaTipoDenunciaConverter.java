package br.jus.cnj.pje.intercomunicacao.v223.converter;

import java.util.HashMap;
import java.util.Map;

import br.jus.cnj.intercomunicacao.v223.criminal.TipoDenuncia;
import br.jus.pje.nucleo.beans.criminal.TipoEventoCriminal;
import br.jus.pje.nucleo.beans.criminal.TipoEventoCriminalEnum;

public class TipoInformacaoCriminalParaTipoDenunciaConverter
		extends IntercomunicacaoConverterAbstrato<TipoEventoCriminal, TipoDenuncia> {

	private static Map<TipoEventoCriminalEnum, TipoDenuncia> MAPA_CORRELACAO = carregarMapaCorrelacao();

	@Override
	public TipoDenuncia converter(TipoEventoCriminal tipoEventoCriminal) {
		if (tipoEventoCriminal == null || tipoEventoCriminal.getCodTipoIc() == null) {
			return null;
		}
		return MAPA_CORRELACAO.get(tipoEventoCriminal.getCodTipoIc());
	}

	private static Map<TipoEventoCriminalEnum, TipoDenuncia> carregarMapaCorrelacao() {
		Map<TipoEventoCriminalEnum, TipoDenuncia> mapaCorrelacao = new HashMap<TipoEventoCriminalEnum, TipoDenuncia>();
		mapaCorrelacao.put(TipoEventoCriminalEnum.IND, TipoDenuncia.INDICIAMENTO);
		mapaCorrelacao.put(TipoEventoCriminalEnum.ADD, TipoDenuncia.ADITAMENTO);
		mapaCorrelacao.put(TipoEventoCriminalEnum.OFD, TipoDenuncia.OFERECIMENTO);
		mapaCorrelacao.put(TipoEventoCriminalEnum.ADR, TipoDenuncia.ADITAMENTO);
		mapaCorrelacao.put(TipoEventoCriminalEnum.OFR, TipoDenuncia.OFERECIMENTO);
		return mapaCorrelacao;
	}

}
