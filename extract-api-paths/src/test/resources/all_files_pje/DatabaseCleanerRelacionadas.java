package br.com.itx.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Classe Auxiliar à classe DataBaseCleaner. Executa a limpeza das tabelas que
 * são relacionadas indiretamente ao fluxo. Ordem de limpeza das tabelas:
 * tb_processo_evento, tb_processo_tarefa_evento, tb_tarefa_jbpm,
 * tb_tarefa_even_agrupamento, tb_tarefa_evento,
 * tb_tarefa_trans_even_agrup, tb_tarefa_transicao_evento,
 * tb_tarefa, tb_grupo_modelo_documento, tb_tipo_modelo_documento;
 */
@SuppressWarnings("unchecked")
public class DatabaseCleanerRelacionadas {

	private String sqlLimparCriminal = "truncate "
			+ "criminal.tb_conc_crme_tpfcco_delito, "
			+ "criminal.tb_concurso_crime, "
			+ "criminal.tb_tipificacao_delito, "
			+ "criminal.tb_dispositivo_norma_tipificacao_delito, "
			+ "criminal.tb_dispositivo_norma, "
			+ "criminal.tb_estabelcmento_prisional, "
			+ "criminal.tb_icr_fuga, "
			+ "criminal.tb_icr_prisao, "
			+ "criminal.tb_icr_prisao_tipo_evento, "
			+ "criminal.tb_icr_processo_evento, "
			+ "criminal.tb_icr_sent_abs_impropria, "
			+ "criminal.tb_icr_sentnca_abs_propria, "
			+ "criminal.tb_icr_sentenca_ext_puni, "
			+ "criminal.tb_icr_soltura, "
			+ "criminal.tb_icr_soltura_tipo_evento, "
			+ "criminal.tb_icr_transferencia_reu, "
			+ "criminal.tb_norma_penal, "
			+ "criminal.tb_org_prcdmnto_originario, "
			+ "criminal.tb_prioridade_processo, "
			+ "criminal.tb_processo_proc_origem, "
			+ "criminal.tb_transito_julgado, "
			+ "criminal.tb_tp_org_tp_proced_origem, "
			+ "criminal.tb_tipo_origem, "
			+ "criminal.tb_tipo_pena, "
			+ "criminal.tb_tipo_procedimento_origem, "
			// + "criminal.tb_icr_sent_condenatoria, "
			+ "criminal.tb_multa_pena_privativa, "
			+ "criminal.tb_regime, "
			+ "criminal.tb_tipo_dispositivo_norma, "
			+ "criminal.tb_tipo_icr, "
			+ "criminal.tb_tipo_norma_penal, "
			+ "criminal.tb_icr_dis_ext_puniblidade,"
			+ "criminal.tb_icr_encerrar_susp_proc,"
			+ "criminal.tb_icr_sent_absol_sumaria,"
			+ "criminal.tb_icr_sentenca_pronuncia,"
			+ "criminal.tb_icr_sent_impronuncia,"
			// + "criminal.tb_motivo_encerramento_prisao,"
			+ "criminal.tb_icr_dis_abs_impropria,"
			+ "criminal.tb_icr_atribuicao_autoria,"
			+ "criminal.tb_icr_encerr_trans_penal,"
			+ "criminal.tb_icr_transacao_penal,"
			+ "criminal.tb_icr_suspensao,"
			+ "criminal.tb_icr_dis_abs_propria,"
			+ "criminal.tb_icr_dis_anlcao_sentenca,"
			+ "criminal.tb_icr, "
			+ "criminal.tb_mtvo_ence_trnscao_penal,"
			+ "criminal.tb_acompanhamento_tarefa_suspensao,"
			+ "criminal.tb_tarefa_suspensao,"
			+ "criminal.tb_cond_icr_transcao_penal,"
			+ "criminal.tb_condicao_suspensao,"
			+ "criminal.tb_tipo_suspensao,"
			+ "criminal.tb_icr_dis_ext_puniblidade,"
			+ "criminal.tb_icr_desclassificacao,"
			+ "client.tb_modif_para_distribuicao,"
			+ "criminal.tb_icr_suspender_suspensao,"
			+ "criminal.tb_icr_retomar_suspensao,"
			+ "criminal.tb_motivo_prisao,"
			+ "criminal.tb_motivo_soltura,"
			+ "criminal.tb_mandado_prisao,"
			+ "criminal.tb_alvara_soltura,"
			+ "criminal.tb_contra_mandado,"
			+ "criminal.tb_alvara_proc_origem,"
			+ "criminal.tb_mandado_proc_origem,"
			+ "criminal.tb_mandados_alcancados,"
			+ "criminal.tb_icr_sent_condenatoria,"
			+ "criminal.tb_pena_tipificacao,"
			+ "criminal.tb_pena_concurso_crime,"
			+ "criminal.tb_icr_desclassificacao;"
			+

			"SELECT setval('criminal.sq_processo_proc_origem', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_concurso_crime', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_dispositivo_norma', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_estabelcimento_prisional', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_icr', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_icr_prisao_tipo_evento', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_icr_processo_evento', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_icr_soltura_tipo_evento', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_multa_pena_privativa', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_norma_penal', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_org_prcdmnto_originario', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_prioridade_processo', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_regime', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_tipificacao_delito', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_tipo_dispositivo_norma', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_tipo_norma_penal', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_tipo_origem', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_tipo_pena', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_tipo_procedimento_origem', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_transito_julgado', 1 , true);"
			+ "SELECT setval('criminal.sq_tb_tarefa_suspensao', 1, true);"
			+ "SELECT setval('criminal.sq_tb_acompanhamento_tarefa_suspensao', 1, true);"
			+ "SELECT setval('criminal.sq_tb_mot_encerr_trans_penal', 1, true);"
			+ "SELECT setval('criminal.sq_tb_motivo_encerramento_prisao', 1, true);"
			+ "SELECT setval('criminal.sq_condicao_icr_transacao_penal', 1, true);"
			+ "SELECT setval('criminal.sq_tb_condicao_suspensao', 1, true);"
			+ "SELECT setval('criminal.sq_tb_tipo_suspensao', 1, true);"
			+ "SELECT setval('criminal.sq_motivo_soltura', 1, true);"
			+ "SELECT setval('client.sq_tb_mod_param_distribuicao', 1, true);"
			+ "SELECT setval('criminal.sq_pena_concurso_crime', 1, true);"
			+ "SELECT setval('criminal.sq_motivo_prisao', 1, true);"
			+ "SELECT setval('criminal.sq_pena_tipificacao', 1, true);"
			+ "SELECT setval('criminal.sq_pena_concurso_crime', 1, true);"
			+

			"INSERT INTO criminal.tb_multa_pena_privativa (ds_multa_pena_privativa, in_ativo) VALUES ('Isolada', 'S'); "
			+ "INSERT INTO criminal.tb_multa_pena_privativa (ds_multa_pena_privativa, in_ativo) VALUES ('Cumulativa', 'S'); "
			+ "INSERT INTO criminal.tb_multa_pena_privativa (ds_multa_pena_privativa, in_ativo) VALUES ('Alternativa', 'S'); "
			+ "INSERT INTO criminal.tb_regime (ds_regime, in_ativo) VALUES ('Preventiva', 'S'); "
			+ "INSERT INTO criminal.tb_regime (ds_regime, in_ativo) VALUES ('Temporária', 'S'); "
			+ "INSERT INTO criminal.tb_regime (ds_regime, in_ativo) VALUES ('Flagrante', 'S'); "
			+ "INSERT INTO criminal.tb_regime (ds_regime, in_ativo) VALUES ('Provisória', 'S'); "
			+ "INSERT INTO criminal.tb_regime (ds_regime, in_ativo) VALUES ('Para Deportação', 'S'); "
			+ "INSERT INTO criminal.tb_regime (ds_regime, in_ativo) VALUES ('Para Expulsão', 'S'); "
			+ "INSERT INTO criminal.tb_regime (ds_regime, in_ativo) VALUES ('Definitiva', 'S'); "
			+

			"INSERT INTO criminal.tb_tipo_dispositivo_norma VALUES (2,'Artigo', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_dispositivo_norma VALUES (3,'Parágrafo', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_dispositivo_norma VALUES (4,'Inciso', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_dispositivo_norma VALUES (5,'Alínea', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_dispositivo_norma VALUES (6,'Item', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_dispositivo_norma VALUES (7,'Parte', 'S');"
			+ "SELECT setval('criminal.sq_tb_tipo_dispositivo_norma', max(id_tipo_dispositivo)) FROM criminal.tb_tipo_dispositivo_norma;"
			+

			"INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('OFD', 'Oferecimento da Denúncia', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('RCD', 'Recebimento da Denúncia', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('ADD', 'Aditamento da Denúncia', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('IND', 'Indiciamento', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('PRI', 'Prisão', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('FUG', 'Fuga', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('SOL', 'Soltura', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('TRR', 'Transferência do Réu', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('SAP', 'Sentença Absolutória', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('SAI', 'Sentença Absolutória Imprópria', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('SEP', 'Sentença de Extinção da Punibilidade', 'S'); "
			+

			"INSERT INTO criminal.tb_tipo_norma_penal  VALUES (2,'Lei', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_norma_penal  VALUES (3, 'Decreto Lei', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_norma_penal  VALUES (4, 'Lei Complementar', 'S'); "
			+ "INSERT INTO criminal.tb_tipo_norma_penal  VALUES (5, 'Lei Delegada', 'S'); "
			+ "SELECT setval('criminal.sq_tb_tipo_norma_penal', max(id_tipo_norma_penal)) FROM criminal.tb_tipo_norma_penal;"
			+

			"INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('DEP', 'Decisão em Instância Superior - Extinção da Punibilidade', 'S');"
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('SPR','Sentença de Pronúncia','S');"
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('CRQ','Cadastrar Recebimento de Queixa','S');"
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr,ds_tipo_icr,in_ativo) VALUES ('ESP','Encerrar Suspensão do Processo','S');"
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('SAS','Sentença de Absolvição Sumária do Júri','S');"
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('ADQ','Aditamento da Queixa','S');"
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('SEI','Sentença de Impronúncia','S');"
			+
			// "insert into criminal.tb_motivo_encerramento_prisao(ds_motivo_encerramento_prisao)values('Conversão da prisão');"+
			"INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('DAI','Decisão em Instância Superior - Absolutória Imprópria','S');"
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('ETP','Encerrar Transação Penal','S');"
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('AAU','Atribuição de Autoria dos Fatos','S');"
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('DAS','Decisão em Instância Superior - Anulação de sentença','S');"
			+ "INSERT INTO criminal.tb_tipo_icr (cd_tipo_icr, ds_tipo_icr, in_ativo) VALUES ('DAP','Decisão em Instância Superior - Absolutória','S');"
			+ "insert into criminal.tb_tipo_icr values('TRP','Transação Penal','S');"
			+ "insert into criminal.tb_tipo_icr values('SUS','Suspensão do Processo','S')";

	private String limparJbpm = "delete from jbpm_taskactorpool; "
			+ "delete from jbpm_pooledactor; "
			+ "delete from jbpm_variableinstance; "
			+ "delete from jbpm_taskactorpool; "
			+ "delete from jbpm_taskinstance; "
			+ "delete from jbpm_swimlaneinstance; "
			+ "delete from jbpm_variableinstance; "
			+ "delete from jbpm_tokenvariablemap; "
			+ "delete from jbpm_moduleinstance;"
			+ "update jbpm_token set processinstance_ = null; "
			+ "update jbpm_token set subprocessinstance_ = null; "
			+ "update jbpm_processinstance  set roottoken_  = null; "
			+ "update jbpm_processinstance  set superprocesstoken_  = null; "
			+ "delete from jbpm_job; "
			+ "delete from jbpm_token; "
			+ "delete from jbpm_processinstance;"
			+ "update jbpm_taskinstance set taskmgmtinstance_=null; "
			+ "update jbpm_swimlaneinstance set taskmgmtinstance_=null;"
			+ "update jbpm_variableinstance set tokenvariablemap_=null; "
			+ "delete from jbpm_tokenvariablemap; "
			+ "delete from jbpm_moduleinstance;"
			+ "delete from jbpm_variableaccess; "
			+ "update jbpm_task set swimlane_ = null, taskmgmtdefinition_ = null, taskcontroller_=null; "
			+ "delete from jbpm_taskcontroller; "
			+ "delete from jbpm_variableinstance; "
			+ "delete from jbpm_taskinstance; "
			+ "delete from jbpm_swimlaneinstance; "
			+ "delete from jbpm_swimlane; "
			+ "delete from jbpm_byteblock; "
			+ "delete from jbpm_bytearray; "
			+ "delete from jbpm_moduledefinition; "
			+ "delete from core.tb_tarefa_jbpm;  SELECT setval('core.sq_tb_tarefa_jbpm', 1 , true);"
			+ "delete from core.tb_proc_localizacao_ibpm; SELECT setval('core.sq_tb_proc_localizacao_ibpm', 1 , true);"
			+ "delete from jbpm_task; "
			+ "update jbpm_processdefinition set startstate_ = null; delete from jbpm_transition; "
			+ "update jbpm_processinstance set roottoken_ = null, superprocesstoken_=null; delete from jbpm_token; "
			+ "delete from jbpm_processinstance; " + "update jbpm_node set action_=null; "
			+ "delete from jbpm_action; " + "delete from jbpm_event; " + "delete from jbpm_node; "
			+ "delete from jbpm_delegation; " + "delete from jbpm_processdefinition; "
			+ "SELECT setval('public.hibernate_sequence', 1, true);";

	private String sqlPreClasse = "update core.tb_parametro set id_usuario_modificacao = null; update core.tb_endereco set id_usuario = null;"
			+ "delete from client.tb_proc_assnto_antecedente; SELECT setval('client.sq_tb_proc_assunto_antecedente', 1 , true);"
			+ "delete from client.tb_processo_assunto; SELECT setval('client.sq_tb_processo_assunto', 1 , true);"
			+ "delete from client.tb_proc_parte_exped_visita; SELECT setval('client.sq_tb_proc_parte_exped_visita', 1 , true);"
			+ "delete from client.tb_proc_parte_expediente; SELECT setval('client.sq_tb_proc_parte_expediente', 1 , true);"
			+ "delete from client.tb_processo_parte_sigilo; SELECT setval('client.sq_tb_processo_parte_sigilo', 1 , true);"
			+ "delete from client.tb_proc_parte_represntante; SELECT setval('client.sq_tb_proc_parte_vsblde_sigilo', 1 , true);"
			+ "delete from client.tb_processo_parte_advogado; SELECT setval('client.sq_tb_processo_parte_advogado', 1 , true);"
			+ "delete from client.tb_proc_parte_represntante; SELECT setval('client.sq_tb_proc_parte_representante', 1 , true);"
			+ "delete from client.tb_processo_parte_endereco; SELECT setval('client.sq_tb_processo_parte_endereco', 1 , true);"
			+ // Delete adicionado no branch de distribuição

			/*
			 * Tabelas novas resultantes da integração entre o branch 1.0.5(CNJ)
			 * e a versão H4.0.87 da INFOX(2º Grau)
			 */
			"delete from client.tb_caixa_adv_proc_classe; SELECT setval('client.sq_tb_caixa_adv_proc_classe', 1 , true);"
			+ "delete from client.tb_caixa_adv_proc_assunto; SELECT setval('client.sq_tb_caixa_adv_proc_assunto', 1 , true);"
			+ "delete from client.tb_caixa_adv_proc; SELECT setval('client.sq_tb_caixa_adv_proc', 1 , true);"
			+ "delete from client.tb_endereco_wsdl; SELECT setval('client.sq_tb_endereco_wsdl', 1 , true);"
			+ "delete from public.qrtz_cron_triggers; "
			+ "delete from public.qrtz_triggers; "
			+ "delete from public.qrtz_job_details; "
			+ "delete from public.qrtz_blob_triggers; "
			+ "delete from public.qrtz_calendars; "
			+ "delete from public.qrtz_fired_triggers; "
			+ "delete from public.qrtz_job_listeners; "
			+ "delete from public.qrtz_locks; "
			+ "delete from public.qrtz_paused_trigger_grps; "
			+ "delete from public.qrtz_scheduler_state; "
			+ "delete from public.qrtz_simple_triggers; "
			+ "delete from public.qrtz_trigger_listeners; "
			+ "delete from client.tb_pess_assist_proc_local; "
			+ "delete from client.tb_assist_proc_local_entde; SELECT setval('client.sq_tb_asst_proc_local_entidade', 1 , true);"
			+ "delete from client.tb_assist_proc_local_entde; SELECT setval('client.sq_tb_asst_proc_local_entidade', 1 , true);"
			+ "delete from client.tb_processo_documento_lote; SELECT setval('client.sq_tb_processo_documento_lote', 1 , true);"
			+ "delete from client.tb_documento_lote; SELECT setval('client.sq_tb_documento_lote', 1 , true);"
			+ "delete from client.tb_modelo_doc_proc_local; "
			+ "delete from client.tb_of_just_central_mandado; SELECT setval('client.sq_tb_ofic_just_cntral_mandado', 1 , true);"
			+ "delete from client.tb_pess_assistente_procurd;"
			+ "delete from client.tb_prev_hosts_webservice; SELECT setval('client.sq_tb_prvncao_hosts_webservice', 1 , true);"
			+ "delete from client.tb_proc_doc_voto_relatorio;"
			+ "delete from client.tb_sessao_comp_processo; SELECT setval('client.sq_tb_sessao_comp_processo', 1 , true);"
			+ "delete from client.tb_ses_pauta_proc_voto; SELECT setval('client.sq_tb_ses_pauta_proc_voto', 1 , true);"
			+ "delete from client.tb_sessao_pauta_proc_trf; SELECT setval('client.sq_tb_sessao_pauta_proc_trf', 1 , true);"
			+ "delete from client.tb_sessao_composicao_ordem; SELECT setval('client.sq_tb_sessao_composicao_ordem', 1 , true);"
			+ "delete from client.tb_ses_pauta_proc_voto_log; SELECT setval('client.sq_tb_ses_pauta_proc_voto_log', 1 , true);"
			+ "delete from client.tb_pess_localz_mgstrdo_log; SELECT setval('client.sq_tb_pess_local_mgstrdo_log', 1 , true);"
			+

			"delete from client.tb_processo_parte; SELECT setval('client.sq_tb_processo_parte', 1 , true);"
			+ "delete from client.tb_proc_parte_historico; SELECT setval('client.sq_tb_proc_parte_historico', 1 , true);"
			+ "delete from client.tb_proc_trf_doc_impresso;"
			+ "delete from client.tb_processo_trf_impresso;"
			+ "delete from client.tb_pagamento_pericia; SELECT setval('client.sq_tb_pagamento_pericia', 1 , true);"
			+ "delete from client.tb_processo_pericia; SELECT setval('client.sq_tb_processo_pericia', 1 , true);"
			+ "delete from client.tb_proc_prioridde_processo; SELECT setval('client.sq_tb_proc_prioridade_processo', 1 , true);"
			+ "delete from client.tb_rpv_pessoa_parte; SELECT setval('client.sq_tb_rpv_pessoa_parte', 1 , true);"
			+ "delete from client.tb_rpv; SELECT setval('client.sq_tb_rpv', 1 , true);"
			+ "delete from client.tb_complem_classe_proc_trf; SELECT setval('client.sq_tb_complemento_cl_proc_trf', 1 , true);"
			+ "delete from client.tb_processo_trf_conexao; SELECT setval('client.sq_tb_processo_trf_conexao', 1 , true);"
			+ "delete from client.tb_proc_trf_redistribuicao; SELECT setval('client.sq_tb_proc_trf_redistribuicao', 1 , true);"
			+ "delete from client.tb_proc_audiencia_pessoa; SELECT setval('client.sq_tb_proc_audiencia_pessoa', 1 , true);"
			+ "delete from client.tb_processo_audiencia; SELECT setval('client.sq_tb_processo_audiencia', 1 , true);"
			+ "delete from client.tb_processo_segredo; SELECT setval('client.sq_tb_processo_segredo', 1 , true);"
			+ "delete from client.tb_proc_visibilida_segredo; SELECT setval('client.sq_tb_proc_visiblidade_segredo', 1 , true);"
			+ "delete from client.tb_proc_trf_lcliz_mgstrado; SELECT setval('client.sq_tb_proc_trf_usu_loc_mgstrdo', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_items_log; SELECT setval('client.sq_tb_items_log', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_processo_trf_log_dist;"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_proc_trf_log_prev_item;"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_processo_trf_log_prev; SELECT setval('client.sq_tb_proc_trf_log_prev_item', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_processo_trf_log; SELECT setval('client.sq_tb_processo_trf_log', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_solicitacao_no_desvio; SELECT setval('client.sq_tb_solicitacao_no_desvio', 1 , true);"
			+ // Delete adicionado na versão 1.0.2
			"delete from client.tb_proc_exped_diligencia; SELECT setval('client.sq_tb_proc_expdente_diligencia', 1 , true);"
			+ "delete from client.tb_proc_doc_expediente; SELECT setval('client.sq_tb_proc_documnto_expediente', 1 , true);"
			+ "delete from client.tb_diligencia; SELECT setval('client.sq_tb_diligencia', 1 , true);"
			+ "delete from client.tb_proc_exped_cntral_mnddo; SELECT setval('client.sq_tb_proc_exped_cntrl_mandado', 1 , true);"
			+ "delete from client.tb_processo_expediente; SELECT setval('client.sq_tb_processo_expediente', 1 , true);"
			+ "delete from client.tb_processo_alerta; SELECT setval('client.sq_tb_processo_alerta', 1 , true);"
			+ // deslocada da 1.0.3 p/ 1.2.0_M4
			"delete from client.tb_processo_trf;"
			+ "delete from client.tb_rpv_pagamento; SELECT setval('client.sq_tb_rpv_pagamento', 1 , true);"
			+ "delete from client.tb_rpv_status; SELECT setval('client.sq_tb_rpv_status', 1 , true);"
			+ "delete from client.tb_rpv_unidade_gestora;"
			+ "delete from client.tb_documento_pessoa; SELECT setval('client.sq_tb_documento_pessoa', 1 , true);"
			+ "delete from client.tb_org_julg_pessoa_perito; SELECT setval('client.sq_tb_org_julg_pessoa_perito', 1 , true);"
			+ "delete from client.tb_visita; SELECT setval('client.sq_tb_visita', 1 , true);"
			+ "delete from client.tb_diligencia; SELECT setval('client.sq_tb_diligencia', 1 , true);"
			+

			"delete from client.tb_meio_contato; SELECT setval('client.sq_tb_meio_contato', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_proc_doc_bin_pess_assin; SELECT setval('client.sq_tb_proc_doc_bin_pess_assina', 1 , true);"
			+ "delete from client.tb_proc_doc_visibi_segredo; SELECT setval('client.sq_tb_proc_doc_vsbldde_segredo', 1 , true);"
			+ "delete from client.tb_proc_documento_segredo; SELECT setval('client.sq_tb_proc_documento_segredo', 1 , true);"
			+ "delete from client.tb_plantao; SELECT setval('client.sq_tb_plantao', 1 , true);"
			+ "delete from client.tb_usu_local_visibilidade; SELECT setval('client.sq_tb_usu_localiz_visibilidade', 1 , true);"
			+ "delete from client.tb_usu_local_mgtdo_servdor;"
			+

			// adicionado na versão 1.0.3
			"update client.tb_sala set id_orgao_julgador_colegiado = null;"
			+ "delete from client.tb_org_julg_clgiado_compet; SELECT setval('client.sq_tb_org_julg_clgado_cmptncia', 1 , true);"
			+ "delete from client.tb_orgjlg_clgdo_orgjlg_log; SELECT setval('client.sq_tb_orgjlg_clgdo_orgjlg_log', 1 , true);"
			+ "delete from client.tb_org_julg_clgdo_org_julg; SELECT setval('client.sq_tb_org_julg_clgado_org_julg', 1 , true);"
			+ "delete from client.tb_orgao_julgador_colgiado; SELECT setval('client.sq_tb_orgao_julgador_colgiado', 1 , true);"
			+ "delete from client.tb_tempo_audienca_org_julg; SELECT setval('client.sq_tb_tempo_audiencia_org_julg', 1 , true);"
			+ "delete from client.tb_processo_documento_lido; SELECT setval('client.sq_tb_processo_documento_lido', 1 , true);"
			+ "delete from client.tb_proc_parte_historico; SELECT setval('client.sq_tb_proc_parte_historico', 1 , true);"
			+ "delete from client.tb_unificacao_pessoas_parte; SELECT setval('client.sq_tb_unificacao_pessoas_parte', 1 , true);"
			+ "delete from client.tb_unificacao_pessoas_doc; SELECT setval('client.sq_tb_unificacao_pessoas_doc', 1 , true);"
			+ "delete from client.tb_unificacao_pessoas_nome; SELECT setval('client.sq_tb_unificacao_pessoas_nome', 1 , true);"
			+ "delete from client.tb_unificacao_pessoas; SELECT setval('client.sq_tb_unificacao_pessoas', 1 , true);"
			+ "delete from client.tb_unificacao; SELECT setval('client.sq_tb_unificacao', 1 , true);"
			+
			// -------------------------

			/* DIMENSÕES */
			"delete from client.tb_competencia_dfuncional;"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_competencia_dpessoal;"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_dfuncional_autoridade;"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_dimensao_alcada;"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_dimensao_funcional;SELECT setval('client.sq_tb_dimensao_funcional', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_dpessoal_tipopessoa;SELECT setval('client.sq_tb_dpessoal_tipopessoa', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_dpessoal_pessoa;SELECT setval('client.sq_tb_dpessoal_pessoa', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_dimensao_pessoal;SELECT setval('client.sq_tb_dimensao_pessoal', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_distancia_maxima_dist;SELECT setval('client.sq_tb_distancia_maxima_dist', 1 , true);"
			+ // Delete adicionado no branch de distribuição

			/* PESSOA */
			"delete from client.tb_pess_perito_disponibili; SELECT setval('client.sq_tb_pess_prto_dspnbilidade', 1 , true);"
			+ "delete from client.tb_pess_perito_indisponibi; SELECT setval('client.sq_tb_pess_prto_indspnbilidade', 1 , true);"
			+ "delete from client.tb_pess_perito_especialida; SELECT setval('client.sq_tb_pess_prto_especialidade', 1 , true);"
			+ "delete from client.tb_pessoa_perito; SELECT setval('client.sq_tb_pessoa_perito', 1 , true);"
			+ "delete from client.tb_pess_procrdor_prcrdoria; SELECT setval('client.sq_tb_pess_prcrdor_procradoria', 1 , true);"
			+ "delete from client.tb_pessoa_procurador;"
			+ "delete from client.tb_pessoa_magistrado; SELECT setval('client.sq_tb_pessoa_localizacao_magistrado', 1 , true);"
			+ "delete from client.tb_pessoa_advogado;"
			+ "delete from client.tb_pessoa_servidor;"
			+ "delete from client.tb_pessoa_oficial_justica;"
			+ "delete from client.tb_autoridade_publica; SELECT setval('client.sq_tb_autoridade_publica', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_pessoa_autoridade;"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_pessoa_nome_alternativo; SELECT setval('client.sq_tb_pessoa_nome_alternativo', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_relacao_pessoal; SELECT setval('client.sq_tb_relacao_pessoal', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_pess_doc_identificacao; SELECT setval('client.sq_tb_pess_doc_identificacao', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_pessoa_localizacao; SELECT setval('client.sq_tb_pessoa_localizacao', 1 , true);"
			+ "delete from client.tb_pess_gpo_oficial_jstica; SELECT setval('client.sq_tb_pess_grupo_ofic_justica', 1 , true);"
			+ "delete from client.tb_pess_prcrdoria_entidade; SELECT setval('client.sq_tb_pess_prcrdoria_entidade', 1 , true);"
			+ "delete from client.tb_pessoa_assistente_adv;"
			+ "delete from client.tb_pessoa_fisica;"
			+ "delete from client.tb_pessoa_juridica; "
			+ "delete from client.tb_pessoa; "
			+
			// "delete from client.tb_pessoa where id_pessoa not in (select id_pessoa_juridica from client.tb_pessoa_juridica);"
			// + Comentado no branch de distribuicao
			// "delete from client.tb_pessoa_juridica where id_pessoa_juridica not in (select id_pessoa from client.tb_pessoa p inner join client.tb_tipo_pessoa tp on (p.id_tipo_pessoa = tp.id_tipo_pessoa) where ds_tipo_pessoa in ('Bancos Privados','Partidos Políticos','Sociedade de Economia Mista','Serviços Sociais Autônomos','Instituições de Ensino Estaduais e Municipais','Instituições de Ensino Privadas','Autoridades','Hospitais','Conselhos de Classe','Estados Estrangeiros','Organismos Internacionais','União/Instituições','Autarquias Federais','Agências Reguladoras Federais','Fundacões Públicas Federais','Empresas Públicas','Concessionárias, Permissionárias e Autorizatárias','Instituições de Ensino Públicas','Justiça do Trabalho','Justiça Estadual','Justiça Federal','Entidades','Juízos','Órgãos/Entidades Vinculadas') order by tp.ds_tipo_pessoa);"
			// + Comentado no branch de distribuicao

			// ****
			"delete from core.tb_item_tipo_documento; SELECT setval('core.sq_tb_item_tipo_documento', 1 , true);"
			+ "delete from core.tb_log_detalhe; SELECT setval('core.sq_tb_log_detalhe', 1 , true);"
			+ "delete from core.tb_log; SELECT setval('core.sq_tb_log', 1 , true);"
			+ "delete from client.tb_doc_validacao_hash; SELECT setval('client.sq_tb_doc_validacao_hash', 1 , true);"
			+ "delete from core.tb_processo_evento; SELECT setval('core.sq_tb_processo_evento', 1 , true);"
			+ "delete from client.tb_proc_doc_ptcao_nao_lida; SELECT setval('client.sq_tb_proc_doc_pticao_nao_lida', 1 , true);"
			+ "delete from core.tb_processo_documento; SELECT setval('core.sq_tb_processo_documento', 1 , true);"
			+ "delete from core.tb_processo_documento_bin; SELECT setval('core.sq_tb_processo_documento_bin', 1 , true);"
			+ "delete from client.tb_proc_exped_diligencia; SELECT setval('client.sq_tb_proc_expdente_diligencia', 1 , true);"
			+ "delete from client.tb_processo_expediente; SELECT setval('client.sq_tb_processo_expediente', 1 , true);"
			+ "delete from core.tb_proc_localizacao_ibpm; SELECT setval('core.sq_tb_proc_localizacao_ibpm', 1 , true);"
			+ "delete from core.tb_estatistica; SELECT setval('core.sq_tb_estatistica', 1 , true);"
			+ "delete from core.tb_processo_tarefa_evento; SELECT setval('core.sq_tb_processo_tarefa_evento', 1 , true);"
			+ "delete from core.tb_processo; SELECT setval('core.sq_tb_processo', 1 , true);"
			+ "delete from client.tb_pessoa_assist_adv_local;"
			+ "delete from core.tb_usuario_localizacao; SELECT setval('core.sq_tb_usuario_localizacao', 1 , true);"
			+ "delete from client.tb_calendario_eventos; SELECT setval('client.sq_tb_calendario_evento', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_caixa_filtro;"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_sala_horario; SELECT setval('client.sq_tb_sala_horario', 1 , true);"
			+ // adicionado na versão capela 1.2.0
			"delete from client.tb_sala_tipo_audiencia;"
			+ // adicionado na versão capela 1.2.0
			"delete from client.tb_sala; SELECT setval('client.sq_tb_sala', 1 , true); "
			+ // adicionado na versão capela 1.2.0
			"delete from client.tb_org_julg_competencia; SELECT setval('client.sq_tb_org_julg_competencia', 1 , true); "
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_orgao_julgador_cargo; SELECT setval('client.sq_tb_orgao_julgador_cargo', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_orgao_julgador o; SELECT setval('client.sq_tb_orgao_julgador', 1 , true); "
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_procuradoria; SELECT setval('client.sq_tb_procuradoria', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_central_mandado_localiz; SELECT setval('client.sq_tb_cntral_mnddo_localizacao', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from core.tb_localizacao;SELECT setval('core.sq_tb_localizacao', 1240 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from core.tb_endereco;SELECT setval('core.sq_tb_endereco', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from core.tb_usuario;SELECT setval('core.sq_tb_usuario', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from acl.tb_usuario_papel; "
			+ "delete from acl.tb_usuario_login; "
			+ // Delete adicionado no branch de distribuição
			"delete from core.tb_assunto; SELECT setval('core.sq_tb_assunto', 1 , true);"
			+ "delete from client.tb_certidao_pessoa; SELECT setval('client.sq_tb_certidao_pessoa', 1 , true);"
			+ "delete from client.tb_cl_judicial_tp_certidao; SELECT setval('client.sq_tb_cl_judicial_tp_certidao', 1 , true);"
			+ "delete from client.tb_tipo_certidao; SELECT setval('client.sq_tb_tipo_certidao', 1 , true);"
			+ "delete from client.tb_jurisdicao_municipio; SELECT setval('client.sq_tb_jurisdicao_municipio', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_jurisdicao; SELECT setval('client.sq_tb_jurisdicao', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_tipo_redistribuicao; SELECT setval('client.sq_tb_tipo_redistribuicao', 1 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_complemento_classe WHERE id_classe_aplicacao in (select id_classe_aplicacao from client.tb_classe_aplicacao WHERE id_aplicacao_classe not in (1,2));"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_classe_aplicacao WHERE id_aplicacao_classe not in (1,2);"
			+ // Delete adicionado no branch de distribuição
			"delete from client.tb_aplicacao_classe WHERE id_aplicacao_classe not in (1,2); SELECT setval('client.sq_tb_aplicacao_classe', 3 , true);"
			+ // Delete adicionado no branch de distribuição
			/* ALERTAS */
			"delete from client.tb_alerta; SELECT setval('client.sq_tb_alerta', 1 , true);"
			+ "delete from client.tb_processo_alerta; SELECT setval('client.sq_tb_processo_alerta', 1 , true);"
			+

			/* MODELOS DE DOCUMENTO */
			"delete from core.tb_modelo_documento WHERE id_modelo_documento > 13; SELECT setval('core.sq_tb_modelo_documento', 14 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from core.tb_tipo_modelo_documento WHERE id_tipo_modelo_documento > 14; SELECT setval('core.sq_tb_tipo_modelo_documento', 15 , true);"
			+ // Delete adicionado no branch de distribuição
			"delete from core.tb_grupo_modelo_documento WHERE id_grupo_modelo_documento > 9; SELECT setval('core.sq_tb_tipo_modelo_documento', 10 , true);"
			+ // Delete adicionado no branch de distribuição

			/* Cargos padrões */
			"delete from client.tb_cargo; SELECT setval('client.sq_tb_cargo', 2 , true);"
			+ // Delete adicionado no branch de distribuição
			"INSERT INTO client.tb_cargo(id_cargo,ds_cargo,in_ativo, cd_cargo) VALUES (1,'Juiz Federal','S','JFEDT'); "
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO client.tb_cargo(ds_cargo,in_ativo, cd_cargo) VALUES ('Juiz Federal Substituto','S','JFEDS'); "
			+ "INSERT INTO client.tb_cargo(ds_cargo,in_ativo, cd_cargo) VALUES ('Juiz de Direito','S','JDIRT'); "
			+ "INSERT INTO client.tb_cargo(ds_cargo,in_ativo, cd_cargo) VALUES ('Juiz de Direito Substituto','S','JDIRS'); "
			+ "INSERT INTO client.tb_cargo(ds_cargo,in_ativo, cd_cargo) VALUES ('Juiz do Trabalho','S','JTRAT'); "
			+ "INSERT INTO client.tb_cargo(ds_cargo,in_ativo, cd_cargo) VALUES ('Juiz do Trabalho Substituto','S','JTRAS'); "
			+ "INSERT INTO client.tb_cargo(ds_cargo,in_ativo, cd_cargo) VALUES ('Juiz de Direito Substituto de 2º Grau','S','JDI2S'); "
			+

			/* Peso das partes padrão */
			"delete from client.tb_processo_peso_parte; SELECT setval('client.sq_tb_processo_peso_parte', 4 , true);"
			+ // Delete adicionado no branch de distribuição
			"INSERT INTO client.tb_processo_peso_parte(id_processo_peso_parte, nr_partes_inicial,nr_partes_final,in_polo,vl_peso) VALUES (1,1,4,'T',1);"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO client.tb_processo_peso_parte(id_processo_peso_parte, nr_partes_inicial,nr_partes_final,in_polo,vl_peso) VALUES (2,5,10,'T',2);"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO client.tb_processo_peso_parte(id_processo_peso_parte, nr_partes_inicial,nr_partes_final,in_polo,vl_peso) VALUES (3,11,NULL,'T',3);"
			+ // Insert adicionado no branch de distribuição

			/* Distância máxima de distribuição padrão */
			"delete from client.tb_distancia_maxima_dist; SELECT setval('client.sq_tb_distancia_maxima_dist', 2 , true);"
			+ // Delete adicionado no branch de distribuição
			"INSERT INTO client.tb_distancia_maxima_dist (id_distancia_maxima, vl_intervalo_inicial, vl_intervalo_final,vl_distancia_maxima) VALUES (1,1,1000,150); "
			+ // Insert adicionado no branch de distribuição

			/* Peso de prevenção padrão */
			"delete from client.tb_peso_prevencao;"
			+ "SELECT setval('client.sq_tb_peso_prevencao', 3 , true);"
			+ // Delete adicionado no branch de distribuição
			"INSERT INTO client.tb_peso_prevencao(id_peso_prevencao,in_tipo_intervalo,in_ativo,vl_intervalo_final,vl_intervalo_inicial,vl_peso) VALUES (1,'E','S',2,1,1);"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO client.tb_peso_prevencao(id_peso_prevencao,in_tipo_intervalo,in_ativo,vl_intervalo_final,vl_intervalo_inicial,vl_peso) VALUES (2,'M','S',NULL,3,0.5);"
			+ // Insert adicionado no branch de distribuição

			"insert into acl.tb_usuario_login (id_usuario, ds_login, ds_nome, ds_senha, in_ativo) values (0, 'sistema', 'Usuário do sistema', 'liiHgKcA1sEBisdWUN9AAAA2gBo=', 'S');"
			+ "insert into acl.tb_usuario_login (id_usuario, ds_login, ds_nome, ds_senha, in_ativo) values (1, 'admin', 'Administrador', 'liiHgKcA1sEBisdWUN9fLEc2gBo=', 'S');"
			+ "insert into core.tb_usuario (id_usuario, in_bloqueio, in_provisorio) values (0, 'S', 'N');"
			+ "insert into core.tb_usuario (id_usuario, in_bloqueio, in_provisorio) values (1, 'N', 'N');"
			+ "INSERT INTO core.tb_endereco (id_endereco,nm_logradouro,nr_endereco,ds_complemento,nm_bairro,nm_cidade,cd_uf,in_correspondencia,id_usuario,id_cep,dt_alteracao_endereco, id_usuario_cadastrador) VALUES (1,'Endereço originário','1','','','','DF','N',1,1,NULL,1); "
			+ // Insert adicionado no branch de distribuição

			/* Localizações padrão */
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1228, 'VARA', 'S', NULL, NULL, NULL, 'S');"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1229, 'Direção da Secretaria', 'S', NULL, 1228, NULL, 'N');"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1230, 'Gabinete do Magistrado', 'S', NULL, 1228, NULL, 'N');"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1231, 'Triagem', 'S', NULL, 1228, NULL, 'N');"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1232, 'Conhecimento', 'S', NULL, 1228, NULL, 'N');"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1233, 'Contadoria', 'S', NULL, 1228, NULL, 'N');"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1234, 'Controle de Perícia', 'S', NULL, 1228, NULL, 'N');"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1235, 'Controle de Execução', 'S', NULL, 1228, NULL, 'N'); "
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1236, 'Controle de Audiência', 'S', NULL, 1228, NULL, 'N'); "
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1237, 'Arquivo', 'S', NULL, 1228, NULL, 'N');"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1238, 'Malote', 'S', NULL, 1228, NULL, 'N');"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1239, 'Assessoria', 'S', NULL, 1228, NULL, 'N');"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO core.tb_localizacao (id_localizacao, ds_localizacao, in_ativo, id_endereco, id_localizacao_pai, id_estrutura, in_estrutura) VALUES (1, 'Tribunal', 'S', NULL, NULL, NULL, 'N');"
			+ // Insert adicionado no branch de distribuição
			"insert into core.tb_usuario_localizacao (id_usuario, in_responsavel_localizacao, id_localizacao, id_papel) values (1, 'N', 1, 1);"
			+ // Insert adicionado no branch de distribuição
			"insert into client.tb_pessoa (id_pessoa, id_tipo_pessoa, in_tipo_pessoa, in_atrai_competencia, in_classificado) values (1, 2, 'F', 'N', 'S');"
			+ // Insert adicionado no branch de distribuição
			"INSERT INTO client.tb_pessoa_fisica(id_pessoa_fisica) VALUES (1); " + // Insert
																					// adicionado
																					// no
																					// branch
																					// de
																					// distribuição
			"INSERT INTO client.tb_pessoa_servidor (id_pessoa_servidor,in_visualizacao_processo) VALUES (1,'A'); "; // Insert
																													// adicionado
																													// no
																													// branch
																													// de
																													// distribuição

	private String sqlPosClasse = "delete from client.tb_aplicacao_classe_pticao;"
			+ "delete from client.tb_aplicacao_cmptnca_prrde; SELECT setval('client.sq_tb_aplic_compet_prioridade', 1 , true);"
			+ "delete from client.tb_atuacao_advogado where ds_atuacao_advogado not in ('Dativo', 'Voluntário');"
			+ "delete from client.tb_caixa_filtro;"
			+ "delete from client.tb_calendario_eventos; SELECT setval('client.sq_tb_calendario_evento', 1 , true);"
			+ "delete from client.tb_caracteristica_processo; SELECT setval('client.sq_tb_caracteristica_processo', 1 , true);"
			+ "delete from client.tb_central_mandado_localiz; SELECT setval('client.sq_tb_cntral_mnddo_localizacao', 1 , true);"
			+ "delete from client.tb_grupo_oficial_justica; SELECT setval('client.sq_tb_grupo_oficial_justica', 1 , true);"
			+ "delete from client.tb_central_mandado; SELECT setval('client.sq_tb_central_mandado', 1 , true);"
			+ "delete from client.tb_certidao_pessoa; SELECT setval('client.sq_tb_certidao_pessoa', 1 , true);"
			+ "delete from client.tb_cl_judicial_tp_certidao; SELECT setval('client.sq_tb_cl_judicial_tp_certidao', 1 , true);"
			+ "delete from client.tb_org_julg_competencia where id_orgao_julgador_competencia in (select id_orgao_julgador_competencia from client.tb_org_julg_competencia ojc inner join client.tb_competencia c on (c.id_competencia = ojc.id_competencia) where c.ds_competencia not in ('CÍVEL','TRIBUTÁRIO','EXECUÇÃO FISCAL','DESAPROPRIAÇÃO PARA FINS DE REFORMA AGRÁRIA','NATURALIZAÇÃO','PENAL','EXECUÇÃO PENAL','CRIMES CONTRA O SISTEMA FINANCEIRO OU DE LAVAGEM DE DINHEIRO OU PRATICADOS POR ORGANIZAÇÕES CRIMINOSAS'));"
			+ "delete from client.tb_competencia_cl_assunto where id_comp_class_assu in (select id_comp_class_assu from client.tb_competencia_cl_assunto cca inner join client.tb_competencia c on (cca.id_competencia = c.id_competencia) where ds_competencia not in ('CÍVEL','TRIBUTÁRIO','EXECUÇÃO FISCAL','DESAPROPRIAÇÃO PARA FINS DE REFORMA AGRÁRIA','NATURALIZAÇÃO','PENAL','EXECUÇÃO PENAL','CRIMES CONTRA O SISTEMA FINANCEIRO OU DE LAVAGEM DE DINHEIRO OU PRATICADOS POR ORGANIZAÇÕES CRIMINOSAS'));"
			+ "delete from client.tb_competencia where ds_competencia not in ('CÍVEL','TRIBUTÁRIO','EXECUÇÃO FISCAL','DESAPROPRIAÇÃO PARA FINS DE REFORMA AGRÁRIA','NATURALIZAÇÃO','PENAL','EXECUÇÃO PENAL','CRIMES CONTRA O SISTEMA FINANCEIRO OU DE LAVAGEM DE DINHEIRO OU PRATICADOS POR ORGANIZAÇÕES CRIMINOSAS');"
			+ "delete from client.tb_complem_classe_proc_trf; SELECT setval('client.sq_tb_complemento_cl_proc_trf', 1 , true);"
			+ "delete from client.tb_complem_pessoa_qualific; SELECT setval('client.sq_tb_comple_pess_qualificacao', 1 , true);"
			+ "delete from client.tb_complem_qualificacao; SELECT setval('client.sq_tb_complem_qualificacao', 1 , true);"
			+ "delete from client.tb_dado_oab_pess_advogado; SELECT setval('client.sq_tb_dado_oab_pess_advogado', 1 , true);"
			+ "delete from client.tb_dado_receita_pess_fsica; SELECT setval('client.sq_tb_dado_receita_pess_fisica', 1 , true);"
			+ "delete from client.tb_dado_receita_pess_jurid; SELECT setval('client.sq_tb_dado_rcita_pess_juridica', 1 , true);"
			+ "delete from client.tb_documento_pessoa; SELECT setval('client.sq_tb_documento_pessoa', 1 , true);"
			+ "delete from client.tb_doc_validacao_hash; SELECT setval('client.sq_tb_doc_validacao_hash', 1 , true);"
			+ "delete from client.tb_etnia where ds_etnia not in('AFRODESCENDENTE','AMERÍNDIO','BRANCO','MULATO','MULTIRRACIAL/PARDO','ASIÁTICO','NÃO DECLARADA');"
			+ "delete from client.tb_item_prioridade;"
			+ "delete from client.tb_lote; SELECT setval('client.sq_tb_lote', 1 , true);"
			+ "delete from client.tb_meio_contato; SELECT setval('client.sq_tb_meio_contato', 1 , true);"
			+ "delete from client.tb_org_julg_pessoa_perito; SELECT setval('client.sq_tb_org_julg_pessoa_perito', 1 , true);"
			+ "delete from client.tb_pagamento_pericia; SELECT setval('client.sq_tb_pagamento_pericia', 1 , true);"
			+ "delete from client.tb_perito_disponibilidade; SELECT setval('client.sq_tb_perito_disponibilidade', 1 , true);"
			+ "delete from client.tb_pessoa_localizacao_magistrado; SELECT setval('client.sq_tb_pessoa_localizacao_magistrado', 1 , true);"
			+ "delete from client.tb_pessoa_qualificacao; SELECT setval('client.sq_tb_pessoa_qualificacao', 1 , true);"
			+ "delete from client.tb_pessoa_tipo_pessoa; SELECT setval('client.sq_tb_pessoa_tipo_pessoa', 1 , true);"
			+ "delete from client.tb_peticao; SELECT setval('client.sq_tb_peticao', 1 , true);"
			+ "delete from client.tb_peticao_cl_aplicacao; SELECT setval('client.sq_tb_ptco_cl_aplicacao_tipo', 1 , true);"
			+ "delete from client.tb_peticao_classe_judicial; SELECT setval('client.sq_tb_peticao_classe_judicial', 1 , true);"
			+ "delete from client.tb_peticao_tp_modelo_doc; SELECT setval('client.sq_tb_ptco_tp_modelo_documento', 1 , true);"
			+ "delete from client.tb_plantao; SELECT setval('client.sq_tb_plantao', 1 , true);"
			+

			"delete from client.tb_processo_lote; SELECT setval('client.sq_tb_processo_lote', 1 , true);"
			+ "delete from client.tb_processo_lote_log; SELECT setval('client.sq_tb_processo_lote_log', 1 , true);"
			+ "delete from client.tb_proc_trf_log_prev_item; SELECT setval('client.sq_tb_proc_trf_pess_magistrado', 1 , true);"
			+ "delete from client.tb_proc_trf_lcliz_mgstrado; SELECT setval('client.sq_tb_proc_trf_usu_loc_mgstrdo', 1 , true);"
			+ "delete from client.tb_procuradoria where ds_nome not in ('UNIÃO', 'FAZENDA NACIONAL');"
			+ "delete from client.tb_qualificacao; SELECT setval('client.sq_tb_qualificacao', 1 , true);"
			+
			// "delete from client.tb_sala_audiencia; SELECT setval('client.sq_tb_sala_audiencia', 1 , true);"
			// +
			"delete from client.tb_tipo_audiencia where ds_tipo_audiencia not in ('Conciliação','Instrução','Julgamento','Instrução e Julgamento','Admonitória','Interrogatório','Justificação','Execução','Inicial','Preliminar');"
			+ "delete from client.tb_tipo_certidao; SELECT setval('client.sq_tb_tipo_certidao', 1 , true);"
			+ "delete from client.tb_tipo_cl_prioridade_proc; SELECT setval('client.sq_tb_tipo_cl_prrdde_processo', 1 , true);"
			+ "delete from client.tb_tipo_diligencia where ds_tipo_diligencia not in ('Citação','Intimação','Notificação','Avaliação','Penhora');"
			+ "delete from client.tb_tipo_evento; SELECT setval('client.sq_tb_tipo_evento', 1 , true);"
			+ "delete from client.tb_tipo_parte_trf; SELECT setval('client.sq_tb_tipo_parte_trf', 1 , true);"
			+ "delete from client.tb_tipo_pess_qualificacao;"
			+ "delete from client.tb_tipo_pichacao; SELECT setval('client.sq_tb_tipo_pichacao', 1 , true);"
			+ "delete from client.tb_tp_pichacao_cl_judicial;"
			+ "delete from client.tb_tp_proc_documto_detalhe; SELECT setval('client.sq_tb_tp_proc_documnto_detalhe', 1 , true);"
			+ "delete from client.tb_tp_proc_doc_tipo_pessoa; SELECT setval('client.sq_tb_tp_proc_doc_tipo_pessoa', 1 , true);"
			+

			/* Unificação */

			"delete from client.tb_unificacao; SELECT setval('client.sq_tb_unificacao', 1 , true);"
			+ "delete from client.tb_unificacao_pessoas; SELECT setval('client.sq_tb_unificacao_pessoas', 1 , true);"
			+ "delete from client.tb_unificacao_pessoas_nome; SELECT setval('client.sq_tb_unificacao_pessoas_nome', 1 , true);"
			+ "delete from client.tb_unificacao_pessoas_doc; SELECT setval('client.sq_tb_unificacao_pessoas_doc', 1 , true);"
			+ "delete from client.tb_unificacao_pessoas_parte; SELECT setval('client.sq_tb_unificacao_pessoas_parte', 1 , true);"
			+

			"delete from client.tb_usu_local_mgtdo_servdor;"
			+ "delete from client.tb_visita; SELECT setval('client.sq_tb_visita', 1 , true);"
			+ "INSERT INTO client.tb_usu_local_mgtdo_servdor(id_usu_local_mgstrado_servidor) VALUES (2);"; // Insert
																																		// adicionado
																																		// no
																																		// branch
																																		// de
																																		// distribuição

	private Connection connection;

	private Set<Integer> fluxoList = new HashSet<Integer>();
	private Set<Integer> modeloDocumentoList = new HashSet<Integer>();
	private Set<Integer> localizacaoList = new HashSet<Integer>();

	public DatabaseCleanerRelacionadas(Connection connection) {
		this.connection = connection;
	}

	public static void main(String[] args) throws Exception {
	}

	private String listToString(Collection c) {
		return c.toString().replaceAll("\\[", "").replaceAll("\\]", "");
	}

	public void setFluxoList(Set<Integer> fluxoList) {
		this.fluxoList = fluxoList;
	}

	public Set<Integer> getFluxoList() {
		return fluxoList;
	}

	public void setLocalizacaoList(Set<Integer> localizacaoList) {
		this.localizacaoList = localizacaoList;
	}

	public Set<Integer> getLocalizacaoList() {
		return localizacaoList;
	}

	public void setModeloDocumentoList(Set<Integer> modeloDocumentoList) {
		this.modeloDocumentoList = modeloDocumentoList;
	}

	public Set<Integer> getModeloDocumentoList() {
		return modeloDocumentoList;
	}

	public void limparTabelasRelacionadas(Set<Integer> fluxoList, Set<Integer> modeloDocumentoList,
			Set<Integer> localizacaoList) throws SQLException {
		setFluxoList(fluxoList);
		setModeloDocumentoList(modeloDocumentoList);
		setLocalizacaoList(localizacaoList);

		removerTabelas();

		removerTarefaEventoAgrupamento();
		removerTarefaEvento();
		removerTarefaTransicaoEventoAgrupamento();
		removerTarefaTransicaoEvento();
		removerCaixa();
		removerTarefa();

		// removerModeloDocumento();
		// removerTipoModeloDocumento();
		// removerGrupoModeloDocumento();

		// removerFluxos();
	}

	private String getTarefasNaoUtilizadas() {
		String query = "select id_tarefa from core.tb_tarefa " + "where id_fluxo in (" + getFluxosNaoUtilizados() + ")";
		return query;
	}

	private String getFluxosNaoUtilizados() {
		String query = "select id_fluxo from core.tb_fluxo "
				+ "where ds_fluxo not in (select name_ from jbpm_processdefinition " + "where id_ in ("
				+ listToString(getFluxoList()) + "))";
		return query;
	}

	/*
	 * private String getTipoModeloNaoUtilizados(){ String query =
	 * "select id_tipo_modelo_documento from core.tb_modelo_documento " +
	 * "where id_modelo_documento not in (" +
	 * listToString(getModeloDocumentoList()) + ")"; return query; }
	 */

	private void removerTarefaEventoAgrupamento() throws SQLException {
		PreparedStatement ps = null;
		try {
			String query = "delete from core.tb_tarefa_even_agrupamento " + "where id_tarefa_evento in "
					+ "(select id_tarefa_evento from core.tb_tarefa_evento " + "where id_tarefa in ("
					+ getTarefasNaoUtilizadas() + "))";
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	private void removerTarefaEvento() throws SQLException {
		PreparedStatement ps = null;
		try {
			String query = "delete from core.tb_tarefa_evento " + "where id_tarefa in (" + getTarefasNaoUtilizadas()
					+ ")";
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	private void removerTarefaTransicaoEventoAgrupamento() throws SQLException {
		PreparedStatement ps = null;
		try {
			String query = "delete from core.tb_tarefa_trans_even_agrup "
					+ "where id_tarefa_transicao_evento in ("
					+ "select id_tarefa_transicao_evento from core.tb_tarefa_transicao_evento "
					+ "where id_tarefa_origem in (" + getTarefasNaoUtilizadas() + ") " + "or id_tarefa_destino in ("
					+ getTarefasNaoUtilizadas() + "))";
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	private void removerTarefaTransicaoEvento() throws SQLException {
		PreparedStatement ps = null;
		try {
			String query = "delete from core.tb_tarefa_transicao_evento " + "where id_tarefa_origem in ("
					+ getTarefasNaoUtilizadas() + ") " + "or id_tarefa_destino in (" + getTarefasNaoUtilizadas() + ")";
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	private void removerCaixa() throws SQLException {
		PreparedStatement ps = null;
		try {
			String query = "delete from core.tb_caixa " + "where id_tarefa in (" + getTarefasNaoUtilizadas() + ")";
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	private void removerTarefa() throws SQLException {
		PreparedStatement ps = null;
		try {
			String query = "delete from core.tb_tarefa where id_fluxo in (" + getFluxosNaoUtilizados() + ")";
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	private void removerTabelas() throws SQLException {
		PreparedStatement ps = null;
		try {
			String query = "delete from core.tb_processo_evento; " + "delete from core.tb_processo_tarefa_evento; "
					+ "delete from core.tb_tarefa_jbpm;";
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	/*
	 * public void removerGrupoModeloDocumento() throws SQLException{
	 * PreparedStatement ps = null; try { String query=
	 * "delete from core.tb_grupo_modelo_documento " +
	 * "where id_grupo_modelo_documento in (" +
	 * "select id_grupo_modelo_documento from core.tb_tipo_modelo_documento " +
	 * "where id_tipo_modelo_documento in (" + getTipoModeloNaoUtilizados() +
	 * "))";; ps = connection.prepareStatement(query); ps.executeUpdate(); }
	 * finally { if (ps != null) { ps.close(); } } }
	 */

	/*
	 * public void removerTipoModeloDocumento() throws SQLException{
	 * PreparedStatement ps = null; try { String query=
	 * "delete from core.tb_tipo_modelo_documento " +
	 * "where id_tipo_modelo_documento in (" + getTipoModeloNaoUtilizados() +
	 * ")"; ps = connection.prepareStatement(query); ps.executeUpdate(); }
	 * finally { if (ps != null) { ps.close(); } } }
	 */

	public void limparSchemaCriminal() throws SQLException {

		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sqlLimparCriminal);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}

	}

	public void executarSqlPreClasse() throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sqlPreClasse);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	public void executarSqlPosClasse() throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sqlPosClasse);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	public void removerLocalizacao() throws SQLException {
		PreparedStatement ps = null;
		if (localizacaoList.isEmpty()) {
			localizacaoList.add(0);
		}
		try {
			String query = "delete from core.tb_item_tipo_documento where id_localizacao in "
					+ "(select id_localizacao from core.tb_localizacao where id_localizacao not in "
					+ "(select distinct id_localizacao_pai from core.tb_localizacao where id_localizacao_pai is not null) "
					+ "and id_localizacao_pai is null and id_localizacao not in ("
					+ listToString(localizacaoList)
					+ "));"
					+

					"delete from core.tb_localizacao where id_localizacao not in "
					+ "(select distinct id_localizacao_pai from core.tb_localizacao where id_localizacao_pai is not null) "
					+ "and id_localizacao_pai is null and id_localizacao not in (" + listToString(localizacaoList)
					+ ");";
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	public void executarSqlJbpm() throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(limparJbpm);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

}