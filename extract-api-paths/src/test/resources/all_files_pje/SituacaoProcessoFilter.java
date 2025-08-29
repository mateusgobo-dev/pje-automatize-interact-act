package br.jus.pje.nucleo.entidades.filters;

public interface SituacaoProcessoFilter extends ProcessoFilter {
	public static final String FILTER_LOCALIZACAO_SERVIDOR = "servidorConsultaLocalizacaoProcessoTrf";
	public static final String FILTER_ORGAO_JULGADOR_COLEGIADO = "orgaoColegiadoSituacaoProcesso";
	
	public static final String FILTER_PAPEL_LOCALIZACAO_FLUXO = "papelLocalizacaoSituacaoProcesso";
	public static final String FILTER_CARGO = "cargoSituacaoProcesso";
	public static final String FILTER_ORGAO_JULGADOR_CARGO = "orgaoJulgadorCargoSituacaoProcesso";
	public static final String FILTER_SEGREDO_JUSTICA = "segredoJusticaSituacaoProcesso";

	public static final String FILTER_COMPETENCIA = "competencia";
	public static final String FILTER_NUMERO_PROCESSO = "numeroProcesso";

	public static final String CONDITION_CARGO = "id_cargo IN (select c.id_cargo from "
			+ "tb_usu_local_visibilidade ulv inner join "
			+ "tb_orgao_julgador_cargo ojc on (ojc.id_orgao_julgador_cargo = ulv.id_org_julg_cargo_visibilidade) "
			+ "inner join tb_cargo c on (c.id_cargo = ojc.id_cargo) "
			+ "inner join tb_usuario_localizacao ul on (ul.id_usuario_localizacao = ulv.id_usu_local_mgstrado_servidor) "
			+ "where ul.id_usuario_localizacao = :" + FILTER_PARAM_ID_USUARIO_LOCALIZACAO + " and " + "ulv.dt_inicio <= :" + FILTER_PARAM_DATA_ATUAL + " and "
			+ "(ulv.dt_final is null or (ulv.dt_final >= :" + FILTER_PARAM_DATA_ATUAL + ")))";
	
	public static final String CONDITION_ORGAO_JULGADOR_CARGO = "exists ( select 1 from tb_usu_local_visibilidade ulv"
			+ " where ulv.id_usu_local_mgstrado_servidor = :" + FILTER_PARAM_ID_USUARIO_LOCALIZACAO
			+ " and ulv.dt_inicio <= :" + FILTER_PARAM_DATA_ATUAL
			+ " and (ulv.dt_final is null or (ulv.dt_final >= :" + FILTER_PARAM_DATA_ATUAL + "))"
			+ " and ("
			+ "		ulv.id_org_julg_cargo_visibilidade IS NULL "
			+ "		OR ulv.id_org_julg_cargo_visibilidade = id_orgao_julgador_cargo"
			+ "		)"
			+ " )";
	
	public static final String CONDITION_PAPEL_LOCALIZACAO_FLUXO = "exists (select 1 from tb_proc_localizacao_ibpm tl "
			+ "where tl.id_processo = id_processo_trf "
			+ "and tl.id_task_jbpm = id_task "
			+ "and tl.id_localizacao = :" + FILTER_PARAM_ID_LOCALIZACAO_MODELO + " and tl.id_papel = :" + FILTER_PARAM_ID_PAPEL + ")";
	
	/**
	 * O usuário deverá visualizar a tarefa se:
	 * - estiver em uma localização igual/inferior à localização da tarefa;
	 * - OU se estiver exatamente no mesmo colegiado da tarefa
	 */
	public static final String CONDITION_LOCALIZACAO_SERVIDOR = CONDITION_LOCALIZACAO_TAREFA;

	public static final String CONDITION_SEGREDO_JUSTICA = "EXISTS (select 1 from tb_processo_trf sub where" +
			" sub.id_processo_trf = id_processo_trf AND (" +
			"	sub.in_segredo_justica = 'false' " +
			"	OR (:" + FILTER_PARAM_VISUALIZA_SIGILOSO + " = 'true' AND " + CONDITION_LOCALIZACAO_SERVIDOR +" ) " +
			"	OR exists" +
			"	 	(select 1 from tb_proc_visibilida_segredo pvs where pvs.id_pessoa = :" + FILTER_PARAM_ID_USUARIO + " and pvs.id_processo_trf = id_processo_trf)"+
			"))";	
}