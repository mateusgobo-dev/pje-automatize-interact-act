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

public interface ProcessoFilter extends Filter{

	public static final String FILTER_PARAM_ID_TIPO_PROCESSO_DOCUMENTO_EXPEDIENTE = "idTipoProcessoDocumentoExpediente";
	public static final String FILTER_PARAM_IDS_LOCALIZACOES_FISICAS_FILHAS = "idsLocalizacoesFisicas";
	public static final String FILTER_PARAM_ID_ORGAO_JULGADOR = "idOrgaoJulgador";
	public static final String FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO = "idOrgaoJulgadorColegiado";
	public static final String FILTER_PARAM_SERVIDOR_EXCLUSIVO_COLEGIADO = "isServidorExclusivoColegiado";
	public static final String FILTER_PARAM_VISUALIZA_SIGILOSO = "visualizaSigiloso";

	public static final String FILTER_PARAM_ID_USUARIO = "idUsuario";
	public static final String FILTER_PARAM_ID_USUARIO_LOCALIZACAO = "idUsuarioLocalizacao";
	public static final String FILTER_PARAM_ID_LOCALIZACAO_FISICA = "idLocalizacaoFisica";
	public static final String FILTER_PARAM_ID_LOCALIZACAO_MODELO = "idLocalizacaoModelo";
	public static final String FILTER_PARAM_ID_PAPEL = "idPapel";

	public static final String FILTER_PARAM_DATA_ATUAL = "dataAtual";
	public static final String FILTER_PARAM_NUMERO_PROCESSO = "numeroProcesso";
	public static final String FILTER_PARAM_ID_COMPETENCIA = "idCompetencia";
	
	public static final String CONDITION_PROCURADOR = "id_processo_trf IN ("
			+ "(select pt.id_processo_trf FROM tb_processo_trf pt "
			+ " inner join tb_processo p on (p.id_processo = pt.id_processo_trf) "
			+ " where p.id_usuario_cadastro_processo = :" + FILTER_PARAM_ID_USUARIO +") " 
			+ "UNION "
			+ "(select distinct ppa.id_processo_trf FROM tb_pessoa_procurador pp "
			+ "inner join tb_procuradoria pr on (pp.id_procuradoria = pr.id_procuradoria) "
			+ "inner join tb_pess_prcrdoria_entidade ppe on (ppe.id_procuradoria = pr.id_procuradoria) "
			+ "inner join tb_processo_parte ppa on (ppa.id_pessoa = ppe.id_pessoa) "
			+ "where pp.id = :" + FILTER_PARAM_ID_USUARIO 
			+ "and ppa.in_situacao = 'A'"
			+ "and ( not exists " 
			+ "     (select 1 " 
			+ "      from tb_pess_procrdor_prcrdoria ppp "
			+ "      where ppp.id_pessoa_fisica = pp.id) "
			+ "      or ppe.id_pess_procuradoria_entidade in " 
			+ "     (select ppp.id_pess_procuradoria_entidade "
			+ "      from tb_pess_procrdor_prcrdoria ppp "
			+ "      where ppp.id_pessoa_fisica = pp.id "
			+ "      and ppp.id_pess_procuradoria_entidade = ppe.id_pess_procuradoria_entidade )))" 
			+ "UNION "
			+ "(select distinct ppa.id_processo_trf FROM tb_pessoa_procurador pp "
			+ "inner join tb_procuradoria pr on (pp.id_procuradoria = pr.id_procuradoria) "
			+ "inner join tb_pess_prcrdoria_entidade ppe on (ppe.id_procuradoria = pr.id_procuradoria) "
			+ "inner join tb_pessoa_autoridade pau on pau.id_orgao_vinculacao = ppe.id_pessoa "
			+ "inner join tb_processo_parte ppa on (ppa.id_pessoa = pau.id_pessoa_autoridade) "
			+ "where pp.id = :" + FILTER_PARAM_ID_USUARIO 
			+ "and ppa.in_situacao = 'A'"
			+ "and ( not exists " 
			+ "     (select 1 " 
			+ "      from tb_pess_procrdor_prcrdoria ppp "
			+ "      where ppp.id_pessoa_fisica = pp.id ) "
			+ "      or ppe.id_pess_procuradoria_entidade in " 
			+ "     (select ppp.id_pess_procuradoria_entidade "
			+ "      from tb_pess_procrdor_prcrdoria ppp "
			+ "      where ppp.id_pessoa_fisica = pp.id "
			+ "      and ppp.id_pess_procuradoria_entidade = ppe.id_pess_procuradoria_entidade )))" 			
			+ " UNION "
			+ "(select distinct pt.id_processo_trf FROM tb_procuradoria proc "
			+ "inner join tb_pess_prcrdoria_entidade ppe on (ppe.id_procuradoria = proc.id_procuradoria) "
			+ "inner join tb_pess_procrdor_prcrdoria ppp on (ppe.id_pess_procuradoria_entidade = ppp.id_pess_procuradoria_entidade) "
			+ "inner join tb_pessoa_procurador pp on (pp.id = ppp.id_pessoa_fisica) "
			+ "inner join tb_processo p on (p.id_usuario_cadastro_processo = ppp.id_pessoa_fisica) "
			+ "inner join tb_processo_trf pt on (p.id_processo = pt.id_processo_trf) "
			+ "WHERE ppe.id_procuradoria in "  
			+ "(SELECT DISTINCT proc.id_procuradoria FROM tb_procuradoria proc, tb_pessoa_procurador pp "
			+ "where pp.id = :" + FILTER_PARAM_ID_USUARIO + " and proc.id_procuradoria = pp.id_procuradoria)) "
			+ " UNION "
			+ "(select distinct pt.id_processo_trf FROM tb_procuradoria proc "
			+ "inner join tb_pess_prcrdoria_entidade ppe on (ppe.id_procuradoria = proc.id_procuradoria) "
			+ "inner join tb_pess_procrdor_prcrdoria ppp on (ppe.id_pess_procuradoria_entidade = ppp.id_pess_procuradoria_entidade) "
			+ "inner join tb_pessoa_procurador pp on (pp.id = ppp.id_pessoa_fisica) "
			+ "inner join tb_processo_parte ppa on (ppa.id_pessoa = pp.id) "
			+ "inner join tb_processo_trf pt on (pt.id_processo_trf = ppa.id_processo_trf) "
			+ "WHERE ppe.id_procuradoria in "  
			+ "(SELECT DISTINCT proc.id_procuradoria FROM tb_procuradoria proc, tb_pessoa_procurador pp "
			+ "where pp.id = :" + FILTER_PARAM_ID_USUARIO + " and proc.id_procuradoria = pp.id_procuradoria)) "
			+ " UNION "
			+ "(select distinct exp.id_processo_trf FROM tb_pessoa_procurador pp "
			+ "inner join tb_procuradoria pr on (pp.id_procuradoria = pr.id_procuradoria) "
			+ "inner join tb_pess_prcrdoria_entidade ppe on (ppe.id_procuradoria = pr.id_procuradoria) "
			+ "inner join tb_proc_parte_expediente exp on (exp.id_pessoa_parte = ppe.id_pessoa) "
			+ "where pp.id = :" + FILTER_PARAM_ID_USUARIO 
			+ "and ( not exists " 
			+ "     (select 1 " 
			+ "      from tb_pess_procrdor_prcrdoria ppp "
			+ "      where ppp.id_pessoa_fisica = pp.id ) "
			+ "      or ppe.id_pess_procuradoria_entidade in " 
			+ "     (select ppp.id_pess_procuradoria_entidade "
			+ "      from tb_pess_procrdor_prcrdoria ppp "
			+ "      where ppp.id_pessoa_fisica = pp.id "
			+ "      and ppp.id_pess_procuradoria_entidade = ppe.id_pess_procuradoria_entidade )))"
			+")";

	public static final String CONDITION_ASSISTENTE_PROCURADORIA = "id_processo_trf IN ( "
			+ " (select pt.id_processo_trf FROM tb_processo_trf pt "
			+ " inner join tb_processo p on (p.id_processo = pt.id_processo_trf) "
			+ " where p.id_usuario_cadastro_processo = :" + FILTER_PARAM_ID_USUARIO + ") "
			+ " UNION "
			+ " (select distinct ppa.id_processo_trf FROM tb_pess_assistente_procurd pp "
			+ " inner join tb_usuario_localizacao ul on (ul.id_usuario = pp.id) "
			+ " inner join tb_pess_assist_proc_local pap on (pap.id_pessoa_assist_proc_local = ul.id_usuario_localizacao) "
			+ " inner join tb_assist_proc_local_entde ape on (ape.id_pessoa_assist_proc_local = pap.id_pessoa_assist_proc_local) "
			+ " inner join tb_pess_prcrdoria_entidade ppe on (ppe.id_pess_procuradoria_entidade = ape.id_pess_procuradoria_entidade) "
			+ " inner join tb_processo_parte ppa on (ppa.id_pessoa = ppe.id_pessoa) "
			+ " where pp.id = :" + FILTER_PARAM_ID_USUARIO + " and ul.id_localizacao_fisica = :"+ FILTER_PARAM_ID_LOCALIZACAO_FISICA
			+ " and (ppa.in_participacao = 'A' OR "
			+ " (ppa.in_participacao = 'P' and EXISTS ( " + "select 1 from tb_processo_documento pd "
			+ " where pd.id_processo = ppa.id_processo_trf and pd.id_tipo_processo_documento = "
			+ " :" + FILTER_PARAM_ID_TIPO_PROCESSO_DOCUMENTO_EXPEDIENTE + ")) OR ppa.in_participacao = 'T' OR ppa.in_participacao = 'P')))";


	public static final String CONDITION_PERITO = "id_processo_trf IN (select distinct pp.id_processo_trf from "
			+ "tb_processo_pericia pp where pp.id_pessoa_perito = :" + FILTER_PARAM_ID_USUARIO + " and pp.cd_status_pericia != 'C')";
	
	public static final String CONDITION_ADVOGADO = "((id_processo_trf IN (select pp.id_processo from tb_processo pp "
			+ "inner join tb_processo_parte pa on pp.id_processo = pa.id_processo_trf and pa.id_pessoa = :" + FILTER_PARAM_ID_USUARIO + " and pa.in_situacao = 'A' "
			+ "where pp.id_processo = id_processo_trf and (pp.id_usuario_cadastro_processo = :" + FILTER_PARAM_ID_USUARIO + " OR "
			+ "pp.id_usuario_cadastro_processo in (select ula.id_usuario from tb_usuario_localizacao ula where ula.id_localizacao_fisica = :idLocalizacaoFisica)))) "
			+ "OR (exists (select 1 from tb_processo_parte pp where "
			+ "pp.id_processo_trf = id_processo_trf and (pp.id_pessoa = :" + FILTER_PARAM_ID_USUARIO + " OR "
			+ "pp.id_pessoa in (select ula.id_usuario from tb_usuario_localizacao ula where ula.id_localizacao_fisica = :idLocalizacaoFisica))"
			+ "and pp.in_situacao = 'A')) "
			+ "OR (exists (select 1 from tb_localizacao l "
			+ "inner join tb_pessoa_localizacao pl on (l.id_localizacao = pl.id_localizacao) "
			+ "inner join tb_pessoa_juridica pj on (pj.id_pessoa_juridica = pl.id_pessoa) "
			+ "inner join tb_usuario_localizacao ul on (ul.id_localizacao_fisica = l.id_localizacao) "
			+ "inner join tb_processo_parte pp on (pp.id_pessoa = pj.id_pessoa_juridica) "
			+ "where ul.id_usuario = :" + FILTER_PARAM_ID_USUARIO + " and pp.id_processo_trf = id_processo_trf and pp.in_situacao = 'A')))";

	public static final String CONDITION_JUS_POSTULANDI = 
			"(id_processo_trf IN "
			+ "  ( select pp.id_processo_trf from " 
			+ "    tb_processo_parte pp,  "
			+ "    tb_processo_trf ptrf  " 
			+ "    where  "
			+ "    pp.id_processo_trf=ptrf.id_processo_trf  "
			+ "    and pp.id_pessoa = :" + FILTER_PARAM_ID_USUARIO 
			+ "    and  not (exists (select  " 
			+ "                      ppr.id_proc_parte_representante  "
			+ "                      from  " 
			+ "                      tb_proc_parte_represntante ppr  "
			+ "                      where  " 
			+ "                      pp.id_processo_parte=ppr.id_processo_parte) "
			+ "             ) " 
			+ "    and (  " 
			+ "         pp.in_parte_principal = TRUE     "
			+ "			and pp.in_participacao='A' " 
			+ "         and not (exists (select  " 
			+ "                           pp2.id_processo_parte  "
			+ "                           from tb_processo_parte pp2 " 
			+ "                           where  "
			+ "                           pp2.in_parte_principal = TRUE " 
			+ "                           and pp2.id_processo_trf=pp.id_processo_trf  "
			+ "                           and pp2.id_pessoa <> :" + FILTER_PARAM_ID_USUARIO + ")  AND pp2.in_participacao='A'   " 
			+ "                  ) "
			+ "         or pp.in_participacao='P'  " 
			+ "        )  " 
			+ "   )  " 
			+ " )";

	public static final String CONDITION_LOCALIZACAO_PROCESSO = ""
			+ "		("
			+ " 		(:"+FILTER_PARAM_SERVIDOR_EXCLUSIVO_COLEGIADO + " = 'true' OR id_orgao_julgador IN ( "
			+ "				SELECT oj.id_orgao_julgador "
			+ "					FROM client.tb_orgao_julgador oj WHERE oj.id_localizacao IN (:"+FILTER_PARAM_IDS_LOCALIZACOES_FISICAS_FILHAS + ")"
			+ "			))"
			+ "			AND (:"+FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO+" = 0 OR id_orgao_julgador_colegiado = :"+FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO+") "
			+ "		) ";
	
	public static final String CONDITION_LOCALIZACAO_TAREFA = ""
			+ " 	id_processo_trf IN ("
			+ "			SELECT pt.id_processo_trf "
			+ " 		FROM client.tb_processo_tarefa pt WHERE "
			+ "			("
			+ " 			(:"+ FILTER_PARAM_SERVIDOR_EXCLUSIVO_COLEGIADO + " = 'true' OR pt.id_localizacao IN (:"+FILTER_PARAM_IDS_LOCALIZACOES_FISICAS_FILHAS + "))"
			+ "				AND (:"+FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO+" = 0 OR pt.id_orgao_julgador_colegiado = :"+FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO+") "
			+ "			) "
			+ "		) ";

	/**
	 * Verifica:
	 * - se há algum processo para um OJ que esteja em uma localizacão igual/inferior à localização do usuário logado
	 * - OU se o processo é do colegiado do usuário logado e o usuário está lotado apenas neste colegiado
	 * - OU se há uma tarefa para uma localização igual/inferior à localização do usuário logado
	 * - OU se a tarefa está no colegiado do usuáiro logado e este usuário está lotado apenas neste colegiado
	 */
	public static final String CONDITION_LOCALIZACOES_SERVIDOR = ""
			+ "	( "
			+ 	CONDITION_LOCALIZACAO_PROCESSO + " OR "+ CONDITION_LOCALIZACAO_TAREFA
			+ "	) ";

	public static final String CONDITION_ORGAO_JULGADOR_CARGO = "exists ( select 1 from tb_usu_local_visibilidade ulv"
			+ " where ulv.id_usu_local_mgstrado_servidor = :" + FILTER_PARAM_ID_USUARIO_LOCALIZACAO 
			+ " and ulv.dt_inicio <= :"+ FILTER_PARAM_DATA_ATUAL
			+ " and (ulv.dt_final is null or (ulv.dt_final >= :" + FILTER_PARAM_DATA_ATUAL + "))"
			+ " and ("
			+ "		ulv.id_org_julg_cargo_visibilidade IS NULL "
			+ "		OR ulv.id_org_julg_cargo_visibilidade = id_orgao_julgador_cargo"
			+ "		)"
			+ ")";

	public static final String CONDITION_CARGO = "id_cargo IN (select c.id_cargo from "
			+ "tb_usu_local_visibilidade ulv inner join "
			+ "tb_orgao_julgador_cargo ojc on (ojc.id_orgao_julgador_cargo = ulv.id_org_julg_cargo_visibilidade) "
			+ "inner join tb_cargo c on (c.id_cargo = ojc.id_cargo) "
			+ "inner join tb_usuario_localizacao ul on (ul.id_usuario_localizacao = ulv.id_usu_local_mgstrado_servidor) "
			+ "where ul.id_usuario_localizacao = :" + FILTER_PARAM_ID_USUARIO_LOCALIZACAO + " and " + "ulv.dt_inicio <= :" + FILTER_PARAM_DATA_ATUAL + " and "
			+ "(ulv.dt_final is null or (ulv.dt_final >= :" + FILTER_PARAM_DATA_ATUAL + ")))";

	public static final String CONDITION_ORGAO_COLEGIADO = "id_orgao_julgador_colegiado = :"
			+ FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO;

	public static final String CONDITION_SEGREDO_JUSTICA = "(in_segredo_justica = 'false' " +
			"		OR in_segredo_justica is null " +
			"		OR (:" + FILTER_PARAM_VISUALIZA_SIGILOSO + " = 'true' AND " + CONDITION_LOCALIZACOES_SERVIDOR +" ) " +
			"		OR id_processo_trf IN (" +
			"			(select pvs.id_processo_trf from client.tb_proc_visibilida_segredo pvs " +
			"				where pvs.id_pessoa = :" + FILTER_PARAM_ID_USUARIO + ") " +
			"				UNION " +
			"			(select distinct ppa.id_processo_trf FROM client.tb_pess_procrdor_prcrdoria ppp " +
			"				inner join client.tb_pess_prcrdoria_entidade ppe on (ppe.id_pess_procuradoria_entidade = ppp.id_pess_procuradoria_entidade) " +
			"				inner join client.tb_processo_parte ppa on (ppa.id_pessoa = ppe.id_pessoa) " +
			"					where ppp.id_pessoa_fisica = :" + FILTER_PARAM_ID_USUARIO + 
			"						and ppa.in_situacao = 'A')" +
			"				UNION " +
			"			(select distinct ppex.id_processo_trf FROM client.tb_pess_procrdor_prcrdoria ppp " +
			"				inner join client.tb_pess_prcrdoria_entidade ppe on (ppe.id_pess_procuradoria_entidade = ppp.id_pess_procuradoria_entidade) " +
			"				inner join client.tb_proc_parte_expediente ppex on (ppex.id_pessoa_parte = ppe.id_pessoa) " +
			"				inner join client.tb_processo_trf p on (p.id_processo_trf = ppex.id_processo_trf)  " +
			"					where ppp.id_pessoa_fisica = :" + FILTER_PARAM_ID_USUARIO + 
			"						AND p.in_segredo_justica = 'true') " +
			"			)" +
			"		) ";

	
	public static final String CONDITION_COMPETENCIA = "id_processo_trf IN "
			+ "(select o.id_processo_trf from tb_processo_trf o where o.id_competencia = :" + FILTER_PARAM_ID_COMPETENCIA + ")";
	
	public static final String CONDITION_NUMERO_PROCESSO = "id_processo_trf IN "
			+ "(select a.id_processo_trf from tb_processo_trf a inner join tb_processo b on a.id_processo_trf = b.id_processo "
			+ "where b.nr_processo like '%' || :" + FILTER_PARAM_NUMERO_PROCESSO + " || '%')";

}