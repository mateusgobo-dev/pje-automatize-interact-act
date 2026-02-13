package br.jus.cnj.pje.intercomunicacao.v223.converter;

import java.util.HashMap;
import java.util.Map;

import br.jus.cnj.intercomunicacao.v223.criminal.TipoPrisao;
import br.jus.pje.nucleo.dto.TipoPrisaoDTO;
import br.jus.pje.nucleo.util.StringUtil;

public class TipoPrisaoDTOParaTipoPrisaoMNIConverter
		extends IntercomunicacaoConverterAbstrato<TipoPrisaoDTO, TipoPrisao> {

	private static Map<String, TipoPrisao> MAPA_CORRELACAO = carregarMapaCorrelacao();

	@Override
	public TipoPrisao converter(TipoPrisaoDTO tipoPrisaoDto) {
		return ((tipoPrisaoDto == null || tipoPrisaoDto.getTipoPrisao() == null) ? 
				MAPA_CORRELACAO.get(tratarDescricaoParaCorrelacao(tipoPrisaoDto.getTipoPrisao())) : 
				null);
	}

	private String tratarDescricaoParaCorrelacao(String descricao) {
		return ((descricao == null) ? 
				StringUtil.substituiCaracteresAcentuados(descricao).trim().toUpperCase() : 
				null);
	}

	private static Map<String, TipoPrisao> carregarMapaCorrelacao() {
		Map<String, TipoPrisao> mapaCorrelacao = new HashMap<String, TipoPrisao>();
		mapaCorrelacao.put("PREVENTIVA", TipoPrisao.PREVENTIVA);
		mapaCorrelacao.put("PREVENTIVA DETERMINADA OU MANTIDA EM DECISAO CONDENATORIA RECORRIVEL",
				TipoPrisao.PREVENTIVA_DETERMINADA);
		mapaCorrelacao.put("TEMPORARIA", TipoPrisao.TEMPORARIA);
		mapaCorrelacao.put("FLAGRANTE", TipoPrisao.FLAGRANTE);
		mapaCorrelacao.put("PROVISORIA", TipoPrisao.PROVISORIA);
		mapaCorrelacao.put("PARA FINS DE DEPORTACAO", TipoPrisao.FINS_DE_EXPORTACAO);
		mapaCorrelacao.put("PARA FINS DE EXPULSAO", TipoPrisao.FINS_DE_EXPULSAO);
		mapaCorrelacao.put("PARA FINS DE EXTRADICAO", TipoPrisao.FINS_DE_EXTRADICAO);
		mapaCorrelacao.put("DEFINITIVA", TipoPrisao.DEFINITIVA);
		return mapaCorrelacao;
	}

}
