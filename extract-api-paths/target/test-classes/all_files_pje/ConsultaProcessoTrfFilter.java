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

public interface ConsultaProcessoTrfFilter extends ProcessoFilter {
	public static final String FILTER_PROCURADOR = "procuradorConsultaProcessoTrf";
	public static final String FILTER_ASSISTENTE_PROCURADORIA = "assistenteProcuradoriaConsultaProcessoTrf";
	public static final String FILTER_ADVOGADO = "advogadoConsultaProcessoTrf";
	public static final String FILTER_JUS_POSTULANDI = "jusPostulandiConsultaProcessoTrf"; 
	public static final String FILTER_PERITO = "peritoConsultaProcessoTrf";
	
	public static final String FILTER_LOCALIZACAO_SERVIDOR = "servidorConsultaLocalizacaoProcessoTrf";
	public static final String FILTER_ORGAO_JULGADOR_COLEGIADO = "orgaoColegiadoConsultaProcessoTrf";
	public static final String FILTER_MAGISTRADO = "magistradoConsultaProcessoTrf";
	public static final String FILTER_SEGREDO_JUSTICA = "segredoJusticaConsultaProcessoTrf";
	public static final String FILTER_ORGAO_JULGADOR_CARGO = "orgaoJulgadorCargoConsultaProcessoTrf";
	public static final String FILTER_CARGO = "cargoConsultaProcessoTrf";
	
	public static final String CONDITION_LOCALIZACAO_PROCESSO = ""
			+ "		("
			+ " 		(:"+FILTER_PARAM_SERVIDOR_EXCLUSIVO_COLEGIADO + " = 'true' OR id_localizacao_oj IN (:"+FILTER_PARAM_IDS_LOCALIZACOES_FISICAS_FILHAS + "))"
			+ "			AND (:"+FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO+" = 0 OR id_orgao_julgador_colegiado = :"+FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO+") "
			+ "		) ";
}
