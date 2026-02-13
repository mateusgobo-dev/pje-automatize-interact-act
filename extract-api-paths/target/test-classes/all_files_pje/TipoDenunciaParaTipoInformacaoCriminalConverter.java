package br.jus.cnj.pje.intercomunicacao.v223.converter;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import br.jus.cnj.intercomunicacao.v223.criminal.TipoDenuncia;
import br.jus.pje.nucleo.beans.criminal.TipoEventoCriminal;
import br.jus.pje.nucleo.beans.criminal.TipoEventoCriminalEnum;
import br.jus.pje.nucleo.beans.criminal.TipoProcessoEnum;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;


public class TipoDenunciaParaTipoInformacaoCriminalConverter
		extends IntercomunicacaoConverterAbstrato<TipoDenuncia, TipoEventoCriminal> {

	private static Map<TipoDenuncia, TipoEventoCriminalEnum> mapaCorrelacaoCri = carregarMapaCorrelacaoCRI();
	private static Map<TipoDenuncia, TipoEventoCriminalEnum> mapaCorrelacaoInf = carregarMapaCorrelacaoINF();
	private ProcessoCriminalDTO processoCriminal;
	
	public TipoDenunciaParaTipoInformacaoCriminalConverter() {
	}
	
	public TipoDenunciaParaTipoInformacaoCriminalConverter(ProcessoCriminalDTO processoCriminal) {
		this.processoCriminal = processoCriminal;
	}

	@Override
	public TipoEventoCriminal converter(TipoDenuncia tipoDenuncia) {

		TipoEventoCriminalEnum tipoEventoCriminalEnum = null;
		
		if (processoCriminal != null && processoCriminal.getTipoProcesso() != null) {
			if (processoCriminal.getTipoProcesso().equals(TipoProcessoEnum.CRI)) {
				tipoEventoCriminalEnum = mapaCorrelacaoCri.get(tipoDenuncia);
			} else if (processoCriminal.getTipoProcesso().equals(TipoProcessoEnum.INF)) {
				tipoEventoCriminalEnum = mapaCorrelacaoInf.get(tipoDenuncia);
			}
		} else {
			tipoEventoCriminalEnum = mapaCorrelacaoCri.get(tipoDenuncia);
		}

		if (tipoEventoCriminalEnum == null) {
			return null;
		}

		TipoEventoCriminal tipoEventoCriminal = new TipoEventoCriminal();
		tipoEventoCriminal.setCodTipoIc(tipoEventoCriminalEnum);
		return tipoEventoCriminal;
	}

	private static Map<TipoDenuncia, TipoEventoCriminalEnum> carregarMapaCorrelacaoCRI() {
		Map<TipoDenuncia, TipoEventoCriminalEnum> mapaCorrelacao = new EnumMap<> (TipoDenuncia.class);
		mapaCorrelacao.put(TipoDenuncia.INDICIAMENTO, TipoEventoCriminalEnum.IND);
		mapaCorrelacao.put(TipoDenuncia.ADITAMENTO, TipoEventoCriminalEnum.ADD);
		mapaCorrelacao.put(TipoDenuncia.OFERECIMENTO, TipoEventoCriminalEnum.OFD);
		return mapaCorrelacao;
	}
	
	private static Map<TipoDenuncia, TipoEventoCriminalEnum> carregarMapaCorrelacaoINF() {
		EnumMap<TipoDenuncia, TipoEventoCriminalEnum> mapaCorrelacao = new EnumMap<> (TipoDenuncia.class);
		mapaCorrelacao.put(TipoDenuncia.INDICIAMENTO, TipoEventoCriminalEnum.IND);
		mapaCorrelacao.put(TipoDenuncia.ADITAMENTO, TipoEventoCriminalEnum.ADR);
		mapaCorrelacao.put(TipoDenuncia.OFERECIMENTO, TipoEventoCriminalEnum.OFR);
		return mapaCorrelacao;
	}

}