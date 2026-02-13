/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades.filters;

public interface ProcessoTrfFilter extends ProcessoFilter {
	public static final String FILTER_PROCURADOR = "procuradorProcessoTrf";
	public static final String FILTER_ASSISTENTE_PROCURADORIA = "assistenteProcuradoriaProcessoTrf";
	public static final String FILTER_ADVOGADO = "advogadoProcessoTrf";
	public static final String FILTER_JUS_POSTULANDI = "jusPostulandiProcessoTrf";
	public static final String FILTER_PERITO = "peritoProcessoTrf";
	public static final String FILTER_ORGAO_JULGADOR = "servidorProcessoTrf";

	public static final String FILTER_LOCALIZACAO_SERVIDOR = "servidorLocalizacaoProcessoTrf";
	public static final String FILTER_ORGAO_JULGADOR_COLEGIADO = "orgaoColegiadoProcessoTrf";
	public static final String FILTER_SEGREDO_JUSTICA = "segredoJusticaProcessoTrf";
	public static final String FILTER_ORGAO_JULGADOR_CARGO = "orgaoJulgadorCargoProcessoTrf";
	public static final String FILTER_CARGO = "cargoProcessoTrf";
	public static final String FILTER_PARAM_ID_ESTRUTURA = "idEstrutura";

	// nao ha condicoes especificas para os filtros de processoTrf - utilizado para a abertura dos autos e pesquisa de processos
}