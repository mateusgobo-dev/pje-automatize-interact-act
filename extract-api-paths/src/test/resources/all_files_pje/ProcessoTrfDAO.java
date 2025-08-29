



package br.com.infox.pje.dao;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.NoResultException;
import javax.persistence.PessimisticLockException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.ProcessoTrfQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Rpv;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;
import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;

/**
 * Classe com as consultas a entidade de ProcessoTrf.
 */
@Name(ProcessoTrfDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProcessoTrfDAO extends GenericDAO implements Serializable, ProcessoTrfQuery {

	private static final String PROCESSO2 = "processo";
	private static final String SITUACAO2 = "situacao";
	private static final String POLO2 = "polo";
	private static final String AND_P_IN_SITUACAO_IN_SITUACAO = "AND p.inSituacao in (:situacao) ";
	private static final String AND_P_IN_PARTICIPACAO_POLO = "AND p.inParticipacao = :polo ";
	private static final long serialVersionUID = 1L;
	public static final String NAME = "processoTrfDAO";
	
	private static final LogProvider log = Logging.getLogProvider(ProcessoTrfDAO.class);

	/**
	 * Obtem a primeira parte ativa ou passiva de um processo que não seja
	 * advogado atraves do processoTrf e da participação (Ativo ou Passivo) e
	 * que não sejam advogados.
	 * 
	 * @param procTrf
	 *            a se obter a parte
	 * @paran inParticipacao participaçao da parte (Ativo ou Passivo)
	 * @return ProcessoParte.
	 */
	public ProcessoParte getProcessoParteByProcessoTrf(ProcessoTrf procTrf, String inParticipacao) {
		Query q = getEntityManager().createQuery(GET_PESSOA_PARTE_BY_PROCESSO_TRF_QUERY);
		q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, procTrf);
		q.setParameter(QUERY_PARAMETER_IN_PARTICIPACAO, inParticipacao);
		q.setParameter(QUERY_PARAMETER_TIPO_PARTE_ADVOGADO, ParametroUtil.instance().getTipoParteAdvogado());

		ProcessoParte singleResult = EntityUtil.getSingleResult(q);
		return singleResult;
	}

	public SituacaoProcesso getSituacaoProcesso(ProcessoTrf processoTrf) {
		return getSituacaoProcesso(processoTrf, false);
	}

	/**
	 * Retorna a situação do processo informado
	 * @param processoTrf processo a ser consultada a situação
	 * @param refresh Boolean: informe TRUE caso queira os dados atualizados, sem cache. 
	 * @return retorna o objeto SituacaoProcesso
	 */
	public SituacaoProcesso getSituacaoProcesso(ProcessoTrf processoTrf, boolean refresh) {
		Query q = getEntityManager().createQuery(GET_SITUACAO_PROCESSO_BY_PROCESSO_TRF_QUERY);
		q.setParameter(QUERY_PARAMETER_ID_PROCESSO_TRF, processoTrf.getIdProcessoTrf());
		SituacaoProcesso singleResult = EntityUtil.getSingleResult(q);
		if(refresh){
			refresh(singleResult);
		}			
		return singleResult;
	}

	/**
	 * Retorna as pericias de um processo
	 * 
	 * @param procTrf
	 * @return lista de ProcessoPericia
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaPerito> getPeritosByProcessoTrf(ProcessoTrf procTrf) {
		Query q = getEntityManager().createQuery(GET_PERITOS_BY_PROCESSO_TRF_QUERY);
		q.setParameter(QUERY_PARAMETER_ID_PROCESSO_TRF, procTrf);
		List<PessoaPerito> list = q.getResultList();
		return list;
	}

	/**
	 * Retorna as rpvs não canceladas ou rejeitadas de um processo
	 * 
	 * @param processoTrf
	 * @return lista de rpv
	 */
	@SuppressWarnings("unchecked")
	public List<Rpv> getRpvsByProcessoTrfList(ProcessoTrf processoTrf) {
		int idRpvStatusCancelado = ParametroUtil.instance().getStatusRpvCancelada().getIdRpvStatus();
		int idRpvStatusRejeitada = ParametroUtil.instance().getStatusRpvRejeitada().getIdRpvStatus();
		Query q = getEntityManager().createQuery(GET_RPVS_BY_PROCESSO_TRF_QUERY);
		q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
		q.setParameter(QUERY_PARAMETER_ID_RPV_STATUS_CANCELADA, idRpvStatusCancelado);
		q.setParameter(QUERY_PARAMETER_ID_RPV_STATUS_REJEITADA, idRpvStatusRejeitada);

		List<Rpv> list = q.getResultList();
		return list;
	}

	/**
	 * Traz uma lista de ProcessoParteRepresentante de uma parte do processo
	 * 
	 * @param processoTrf
	 * @param pessoa
	 * @return lista de ProcessoParteRepresentante
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteRepresentante> getListRepresentanteByPessoaAndProcessoTrf(ProcessoTrf processoTrf,
			Pessoa pessoa) {
		Query q = getEntityManager().createQuery(LIST_REPRESENTANTE_BY_PESSOA_AND_PROCESSO_TRF_QUERY);
		q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
		q.setParameter(QUERY_PARAMETER_PESSOA, pessoa);
		List<ProcessoParteRepresentante> list = q.getResultList();
		return list;
	}

	/**
	 * Traz um processoParte especifico, filtrando por processo, pessoa e polo
	 * 
	 * @param pessoa
	 * @param processoTrf
	 * @param polo
	 * @return processoParte
	 */
	public ProcessoParte getParteByPessoaPoloAndProcesso(Pessoa pessoa, ProcessoTrf processoTrf, String polo) {
		Query q = getEntityManager().createQuery(GET_PROCESSO_PARTE_BY_PESSOA_POLO_AND_PROCESSO_TRF_QUERY);
		q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
		q.setParameter(QUERY_PARAMETER_PESSOA, pessoa);
		q.setParameter(QUERY_PARAMETER_IN_PARTICIPACAO, polo);

		ProcessoParte parte = EntityUtil.getSingleResult(q);
		return parte;
	}

	/**
	 * Traz a lista de autores ou reus de um processo
	 * 
	 * @param procTrf
	 * @param inParticipacao
	 * @return lista de ProcessoParte
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> getListProcessoPartePrincipalByProcessoTrf(ProcessoTrf procTrf, String inParticipacao) {
		Query q = getEntityManager().createQuery(LIST_PROCESSO_PARTE_PRINCIPAL_TRF_BY_PROCESSO_TRF_QUERY);
		q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, procTrf);
		q.setParameter(QUERY_PARAMETER_IN_PARTICIPACAO, inParticipacao);
		q.setParameter(QUERY_PARAMETER_TIPO_PARTE_ADVOGADO, ParametroUtil.instance().getTipoParteAdvogado());

		List<ProcessoParte> list = q.getResultList();
		return list;
	}
	

	public Boolean isProcessoAptoParaSessao(ProcessoTrf processoTrf){
		Query q = getEntityManager().createQuery(IS_PROCESSO_APTO_PARA_SESSAO);
		q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
		Boolean result = EntityUtil.getSingleResult(q);
		return result;
	}
	/**
	 * verifica o status do processo
	 * @param idProcessoTrf
	 * @return
	 */
	public boolean isProcessoRemetido(Integer idProcessoTrf){
		String query = "select o.inOutraInstancia from ProcessoTrf o where o.idProcessoTrf = :idProcessoVerificacao";
		try {
			Query q = getEntityManager().createQuery(query);
			q.setParameter("idProcessoVerificacao", idProcessoTrf);
			return (Boolean) q.getSingleResult();
		} catch (NoResultException ex) {
			return false;
		} catch (Exception e) {
			return false;
		}
	}
        
	/**
	 * [PJEII-4329] Criado para verificar se o processo está concluso
	 * @param processoTrf Processo
	 * @return flag indicando se o processo está concluso
	 */
	public Boolean isProcessoConcluso(ProcessoTrf processoTrf){
		Query query = getEntityManager().createQuery(GET_PROCESSO_EVENTO_CONCLUSO_QUERY);
		query.setParameter(QUERY_PARAMETER_PROCESSO, processoTrf.getProcesso());
		query.setParameter(QUERY_PARAMETER_EVENTO, ParametroUtil.instance().getEventoConclusao());
		if ((Long) query.getSingleResult() > 0){
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	/**
	 * Método responsável por recuperar os processos dos quais a {@link Pessoa} faz parte.
	 * 
	 * @param pessoa {@link Pessoa}.
	 * @return Os processos dos quais a {@link Pessoa} faz parte.
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> recuperarProcessosRelacionados(Pessoa pessoa) {
		StringBuilder jpql = new StringBuilder("select a.processoTrf from ProcessoParte a where a.pessoa = :pessoa ");
		jpql.append("and a.inSituacao = 'A' and a.processoTrf.processoStatus = 'D' and not exists ");
		jpql.append("(select b.processoTrf.idProcessoTrf from ProcessoPush b where b.processoTrf.idProcessoTrf = a.processoTrf.idProcessoTrf and b.pessoa = a.pessoa and b.dtExclusao is null) ");
		jpql.append("and ((a.processoTrf.segredoJustica = true and exists(select pvs.pessoa from ProcessoVisibilidadeSegredo pvs where pvs.processo.idProcesso = a.processoTrf.idProcessoTrf and pvs.pessoa = a.pessoa)) or a.processoTrf.segredoJustica = false) ");
		
		Query query = EntityUtil.createQuery(jpql.toString()).setParameter("pessoa", pessoa);
		return query.getResultList();
	}
	
	/**
	 * Método responsável por recuperar um {@link ProcessoTrf}.
	 * 
	 * @param numeroProcesso Número do processo.
	 * @param pessoa {@link Pessoa}.
	 * @return {@link ProcessoTrf}.
	 */
	public ProcessoTrf recuperarProcesso(String numeroProcesso, Pessoa pessoa) {
		StringBuilder jpql = new StringBuilder("select a from ProcessoTrf a left join a.visualizadores b ");
		jpql.append("where a.processo.numeroProcesso = :numeroProcesso ");
		jpql.append("and (a.segredoJustica = false or (b.processo.idProcesso = a.processo.idProcesso and b.pessoa = :pessoa))");
		
		Query query = EntityUtil.createQuery(jpql.toString())
				.setParameter("numeroProcesso", numeroProcesso)
				.setParameter("pessoa", pessoa);
		
		try {
			return (ProcessoTrf)query.getSingleResult();
		} catch(NoResultException ex) {
			return null;
		}
	}

	/**
     * Retorna processoTrf
     * 
     * @param Integer idProcessoTrf
     * @return ProcessoTrf
     */
	public ProcessoTrf getProcessoTrfByIdProcessoTrf(Integer idProcessoTrf) {
		Map<String, Object> cacheHints = new HashMap<>();
		cacheHints.put("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
		cacheHints.put("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);

		return EntityUtil.getEntityManager().find(ProcessoTrf.class, idProcessoTrf, cacheHints);
	}

	/**
	 * Método responsável por recuperar um {@link ProcessoTrf}.
	 * 
	 * @param numeroProcesso Número do processo
	 * @param classes Array de {@link ClasseJudicialInicialEnum}.
	 * @return {@link ProcessoTrf}.
	 */
	public ProcessoTrf recuperarProcesso(String numeroProcesso, ClasseJudicialInicialEnum... classes) {
		StringBuilder jpql = new StringBuilder("select a from ProcessoTrf a ");
		jpql.append("where a.processo.numeroProcesso = :numeroProcesso and a.inicial in (:classes)");
		
		Query query = EntityUtil.createQuery(jpql.toString())
				.setParameter("numeroProcesso", numeroProcesso)
				.setParameter("classes", Arrays.asList(classes));
		
		try {
			return (ProcessoTrf)query.getSingleResult();
		} catch(NoResultException ex) {
			return null;
		}
	}

	/**
	 * Retorna o tamanho do processo, ou seja, a soma do tamanho dos documentos ativos do processo.
	 * 
	 * @param processo ProcessoTrf
	 * @return Long do tamanho do processo.
	 */
	public Long obterTamanho(ProcessoTrf processo) {
		StringBuilder hql = new StringBuilder();
		hql.append("select sum(pdb.size) ");
		hql.append("from ");
		hql.append("	ProcessoDocumento pd ");
		hql.append("		left join pd.processoDocumentoBin pdb ");
		hql.append("where ");
		hql.append("	pd.ativo = true and ");
		hql.append("	pd.dataJuntada is not null and ");
		hql.append("	pd.processoTrf = :processoTrf");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("processoTrf", processo);
		
		Number sum = (Number) query.getSingleResult();
		return Long.valueOf((sum != null ? sum.longValue(): 0l));
	}
	
	/**
	 * Metodo responsavel por remover o processo
	 * 
	 * @param processoTrf O {@link ProcessoTrf}
	 */
	public void removerProcesso(ProcessoTrf processoTrf) {
		StringBuilder sb = new StringBuilder();

		sb.append("DELETE FROM tb_informacao_criminal_rascunho ");
		sb.append("WHERE id_processo_parte IN (SELECT id_processo_parte FROM tb_processo_parte WHERE id_processo_trf = :idProcesso); ");
		
		sb.append("DELETE FROM tb_processo_rascunho WHERE id_processo_trf = :idProcesso; ");

		sb.append("DELETE FROM tb_processo_jt WHERE id_processo_trf = :idProcesso; ");
		
		sb.append("DELETE FROM tb_processo_alerta WHERE id_processo_trf = :idProcesso; ");
		
		sb.append("DELETE FROM tb_proc_parte_represntante ");
		sb.append("WHERE id_processo_parte IN (SELECT id_processo_parte FROM tb_processo_parte WHERE id_processo_trf = :idProcesso); ");
		
		sb.append("DELETE FROM tb_processo_parte_advogado ");
		sb.append("WHERE id_processo_parte IN (SELECT id_processo_parte FROM tb_processo_parte WHERE id_processo_trf = :idProcesso); ");

		sb.append("DELETE FROM tb_processo_parte_endereco ");
		sb.append("WHERE id_processo_parte IN (SELECT id_processo_parte FROM tb_processo_parte WHERE id_processo_trf = :idProcesso); ");

		sb.append("DELETE FROM tb_unificacao_pessoas_parte ");
		sb.append("WHERE id_parte IN (SELECT id_processo_parte FROM tb_processo_parte WHERE id_processo_trf = :idProcesso); ");
		
		sb.append("DELETE FROM tb_processo_parte WHERE id_processo_trf = :idProcesso;");
		sb.append("DELETE FROM tb_estatistica WHERE id_processo = :idProcesso; ");

		sb.append("DELETE FROM tb_proc_doc_ptcao_nao_lida ");
		sb.append("WHERE id_processo_documento IN (SELECT id_processo_documento FROM tb_processo_documento WHERE id_processo = :idProcesso); ");
		
		sb.append("DELETE FROM tb_proc_trf_doc_impresso ");
		sb.append("WHERE id_processo_documento IN (SELECT id_processo_documento FROM tb_processo_documento WHERE id_processo = :idProcesso); ");

		sb.append("DELETE FROM tb_proc_doc_expediente ");
		sb.append("WHERE id_processo_documento IN (SELECT id_processo_documento FROM tb_processo_documento WHERE id_processo = :idProcesso); ");

		sb.append("DELETE FROM tb_doc_validacao_hash ");
		sb.append("WHERE id_processo_documento IN (SELECT id_processo_documento FROM tb_processo_documento WHERE id_processo = :idProcesso); ");

		sb.append("DELETE FROM tb_complemento_segmentado ");
		sb.append("WHERE id_movimento_processo IN (SELECT id_processo_evento FROM tb_processo_evento WHERE id_processo = :idProcesso); ");
		
		sb.append("DELETE FROM tb_processo_evento WHERE id_processo = :idProcesso; ");

		sb.append("DELETE FROM tb_proc_doc_bin_pess_assin ");
		sb.append("WHERE id_processo_documento_bin IN (SELECT id_processo_documento_bin FROM tb_processo_documento WHERE id_processo = :idProcesso); ");

		sb.append("DELETE FROM tb_proc_doc_associacao ");
		sb.append("WHERE id_proc_doc IN (SELECT id_processo_documento FROM tb_processo_documento WHERE id_processo = :idProcesso); ");

		sb.append("DELETE FROM tb_proc_doc_associacao ");
		sb.append("WHERE id_doc_associado IN (SELECT id_processo_documento FROM tb_processo_documento WHERE id_processo = :idProcesso); ");
		
		sb.append("DELETE FROM tb_controle_versao_documento ");
		sb.append("WHERE id_processo_documento_bin IN (SELECT id_processo_documento_bin FROM tb_processo_documento WHERE id_processo = :idProcesso); ");
		
		sb.append("DELETE FROM tb_processo_documento_trf ");
		sb.append("WHERE id_processo_documento_trf IN (SELECT id_processo_documento FROM tb_processo_documento WHERE id_processo = :idProcesso); ");
		
		sb.append("DELETE FROM tb_processo_documento WHERE id_processo = :idProcesso; ");

		sb.append("DELETE FROM tb_processo_assunto WHERE id_processo_trf = :idProcesso; ");

		sb.append("DELETE FROM tb_processo_trf_impresso WHERE id_processo_trf = :idProcesso; ");

		sb.append("DELETE FROM tb_rpv_pessoa_parte ");
		sb.append("WHERE id_rpv IN (SELECT id_rpv FROM tb_rpv WHERE id_processo_trf = :idProcesso); ");

		sb.append("DELETE FROM tb_rpv WHERE id_processo_trf = :idProcesso; ");

		sb.append("DELETE FROM tb_proc_visibilida_segredo WHERE id_processo_trf = :idProcesso; ");

		sb.append("DELETE FROM tb_proc_prioridde_processo WHERE id_processo_trf = :idProcesso; ");

		sb.append("DELETE FROM tb_complem_classe_proc_trf WHERE id_processo_trf = :idProcesso; ");

		sb.append("DELETE FROM tb_processo_segredo WHERE id_processo_trf = :idProcesso; ");

		sb.append("DELETE FROM tb_processo_trf_conexao WHERE id_processo_trf_conexo = :idProcesso; ");

		sb.append("DELETE FROM tb_processo_trf_conexao WHERE id_processo_trf = :idProcesso; ");
		
		sb.append("DELETE FROM tb_complemento_processo_je WHERE id_processo_trf	= :idProcesso; ");

		sb.append("DELETE FROM tb_processo_trf WHERE id_processo_trf = :idProcesso; ");

		sb.append("DELETE FROM tb_processo WHERE id_processo = :idProcesso; ");
		
		String query = sb.toString().replaceAll(":idProcesso", String.valueOf(processoTrf.getIdProcessoTrf()));
		EntityUtil.createNativeQuery(getEntityManager(), query, 
				"tb_processo_jt",
				"tb_processo_alerta",
				"tb_proc_parte_represntante",
				"tb_processo_parte_advogado",
				"tb_processo_parte_endereco",
				"tb_unificacao_pessoas_parte",
				"tb_processo_parte",
				"tb_estatistica",
				"tb_proc_doc_ptcao_nao_lida",
				"tb_proc_trf_doc_impresso",
				"tb_proc_doc_expediente",
				"tb_doc_validacao_hash",
				"tb_complemento_segmentado",
				"tb_processo_evento",
				"tb_proc_doc_bin_pess_assin",
				"tb_proc_doc_associacao",
				"tb_proc_doc_associacao",
				"tb_controle_versao_documento",
				"tb_processo_documento_trf",
				"tb_processo_documento",
				"tb_processo_assunto",
				"tb_processo_trf_impresso",
				"tb_rpv_pessoa_parte",
				"tb_rpv",
				"tb_proc_visibilida_segredo",
				"tb_proc_prioridde_processo",
				"tb_complem_classe_proc_trf",
				"tb_processo_segredo",
				"tb_processo_trf_conexao",
				"tb_processo_trf_conexao",
				"tb_complemento_processo_je",
				"tb_processo_trf",
				"tb_processo"
				).executeUpdate();
		
		Integer idProcessoDocumentoBin = 0;
		if (processoTrf.getProcesso().getProcessoDocumentoList().size() > 0) {
			idProcessoDocumentoBin = processoTrf.getProcesso().getProcessoDocumentoList().get(0).getProcessoDocumentoBin().getIdProcessoDocumentoBin();
		}
		
		sb = new StringBuilder();
		sb.append("DELETE FROM tb_processo_documento_bin ");
		sb.append("WHERE id_processo_documento_bin = :id");

		String query2 = sb.toString().replaceAll(":id", String.valueOf(idProcessoDocumentoBin));
		EntityUtil.createNativeQuery(getEntityManager(), query2, "tb_processo_documento_bin").executeUpdate();
	}

	/**
	 * Dada uma substituição de magistrado, retorna os processos que foram distribuídos durante a substituição no órgão julgador em 
	 * que tal afastamento ocorreu.
	 * 
	 * @param substituicaoMagistrado substituição a ser considerada.
	 * @return processos distribuidos.
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> obterProcessosDistribuidosDuranteSubstituicao(SubstituicaoMagistrado substituicaoMagistrado){
		StringBuilder jpql = new StringBuilder();
		jpql.append(" SELECT a "); 
		jpql.append(" FROM ProcessoTrf a ");
		jpql.append(" WHERE a.dataDistribuicao BETWEEN :dataInicial AND :dataFinal ");
		jpql.append(" AND a.orgaoJulgador = :orgaoJulgador ");
		jpql.append(" ORDER BY a.dataDistribuicao DESC ");
		
		Query query = EntityUtil.createQuery(jpql.toString());
		query.setParameter("dataInicial", substituicaoMagistrado.getDataInicio());
		query.setParameter("dataFinal", substituicaoMagistrado.getDataFim());
		query.setParameter("orgaoJulgador", substituicaoMagistrado.getOrgaoJulgador());

		return query.getResultList();
		
	}
	
	public String obterQueryConsolidadaIdsProcessos(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, ConsultaProcessoVO criteriosPesquisa) {
		
		List<Map<String, String>> consultasProcessos = this.obterQueryProcessos(
				idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisa);

		Map<String, String> restricoesProcessos = this.obterQueryRestricoesProcessos(idPessoa, idLocalizacao, idProcuradoria, criteriosPesquisa);

		String universoPesquisaProcessos = null;
		if(criteriosPesquisa != null && criteriosPesquisa.getUniversoContextoPesquisa() != null) {
			universoPesquisaProcessos = this.obterQueryConsolidadaIdsProcessos(idPessoa, idLocalizacao, tipoUsuarioExterno, idProcuradoria, isProcuradorGestor, criteriosPesquisa.getUniversoContextoPesquisa());
		}
		
		return consolidarConsultaProcessos(consultasProcessos, restricoesProcessos, universoPesquisaProcessos);
	}

	private String aplicarRestricaoUniversoProcessos(String consultaProcessos, String universoPesquisaProcessos) {
		StringBuilder consultaConsolidada = new StringBuilder();

		consultaConsolidada.append("SELECT DISTINCT processos_consultados.id_processo_trf FROM ")
			.append("("+consultaProcessos+") as processos_consultados ")
			.append("INNER JOIN ("+universoPesquisaProcessos+") universo_consulta_processos "
					+ "ON (universo_consulta_processos.id_processo_trf = processos_consultados.id_processo_trf) ");
		
		return consultaConsolidada.toString();		
	}
	
	public List<Map<String, String>> obterQueryProcessos(Integer idPessoa, Integer idLocalizacao, TipoUsuarioExternoEnum tipoUsuarioExterno, 
			Integer idProcuradoria, boolean isProcuradorGestor, ConsultaProcessoVO criteriosPesquisa){
		
		List<Map<String, String>> consultas = new ArrayList<Map<String, String>>(0);
		
		if (idProcuradoria != null && idProcuradoria != 0) {
			if (isProcuradorGestor) {
				consultas.add(this.obterQueryProcessosProcuradorGestor(idProcuradoria));
			}else{
				// Usuário logado é procurador padrão ou distribuidor.
				// A query deve ser a união da consulta de procuradores padrão (caixa) com procuradores distribuidores (jurisdição).
				consultas.add(this.obterQueryProcessosProcuradorCaixa(idPessoa, idLocalizacao, idProcuradoria));
				consultas.add(this.obterQueryProcessosProcuradorJurisdicao(idPessoa, idProcuradoria));
			}
		} else {
			// Se não for de procuradoria, deve retornar sempre os expedientes da parte
			consultas.add(this.obterQueryProcessosParte(idPessoa));
			// Se a pessoa atual é advogado ou assistente de advogado - deve-se juntar com a consulta da parte
			if (TipoUsuarioExternoEnum.A.equals(tipoUsuarioExterno) || TipoUsuarioExternoEnum.AA.equals(tipoUsuarioExterno)) {
				consultas.add(TipoUsuarioExternoEnum.A.equals(tipoUsuarioExterno) ?
					this.obterQueryProcessoRepresentanteParte(idPessoa) : 
						this.obterQueryProcessosAssistenteRepresentanteParte(idPessoa, idLocalizacao));
			}
		}
		return consultas;
	}
	
	/**
	 * Método responsável por recuperar um {@link ProcessoTrf}.
	 * 
	 * @param numeroProcesso Número do processo
	 * @return {@link ProcessoTrf}.
	 */
	public ProcessoTrf recuperarProcesso(String numeroProcesso) {
		
		StringBuilder jpql = new StringBuilder("select a from ProcessoTrf a ");
		jpql.append("where a.processo.numeroProcesso = :numeroProcesso");
		
		Query query = EntityUtil.createQuery(jpql.toString());
		query.setParameter("numeroProcesso", numeroProcesso);
		
		return getSingleResult(query);
	}
	
	private String obterQueryAdminJurisdicao(Boolean isAdminJurisdicao) {
		Integer valorIsAdmin = 0;
		if(isAdminJurisdicao) {
			valorIsAdmin = 1;
		}
		return "INNER JOIN (SELECT "+valorIsAdmin+" in_admin_jurisdicao) as administracao_jurisdicao ON (1=1) ";
	}

	/**
	 * 
	 * @param idProcuradoria
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryProcessosProcuradorGestor(Integer idProcuradoria) {
		StringBuilder strQueryFrom = new StringBuilder(" FROM tb_processo_parte pp ")
				.append(" INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao in ('A', 'S')) ")
				.append(" INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = ptf.id_jurisdicao) ")
				.append(this.obterQueryAdminJurisdicao(true));
		
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ptf.cd_processo_status = 'D' AND pp.id_procuradoria = " + idProcuradoria + " ");
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
		
		return queryPesquisa;
	}
	
	/**
	 * 
	 * @param idPessoa
	 * @param idLocalizacao
	 * @param idProcuradoria
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryProcessosProcuradorCaixa(Integer idPessoa, Integer idLocalizacao, Integer idProcuradoria) {
		StringBuilder strQueryFrom = new StringBuilder(" FROM tb_caixa_representante cx_rep ")
				.append(" INNER JOIN tb_caixa_adv_proc cx ON (cx_rep.id_caixa_adv_proc = cx.id_caixa_adv_proc) ")
				.append(" INNER JOIN tb_processo_caixa_adv_proc cx_proc ON (cx.id_caixa_adv_proc = cx_proc.id_caixa_adv_proc) ")
				.append(" INNER JOIN tb_cabecalho_processo ptf ON (cx_proc.id_processo_trf = ptf.id_processo_trf) ")
				.append(" INNER JOIN tb_processo_parte pp ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') ")
				.append(" INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = ptf.id_jurisdicao) ")
				.append(this.obterQueryAdminJurisdicao(false));
		
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ptf.cd_processo_status = 'D' AND pp.id_procuradoria = " + idProcuradoria + " ")
				// Restricoes relacionadas ao procurador da caixa.
			.append(" AND (cx_rep.id_pessoa_fisica = "+idPessoa+") ")
			.append(" AND (cx.id_localizacao = "+idLocalizacao+") ")
				// Retira as caixas que estiverem com o periodo de inatividade no momento atual
			.append(" AND NOT EXISTS (")
			.append(" SELECT 1 FROM tb_periodo_inativ_caixa_rep cx_in WHERE cx_in.id_caixa_adv_proc = cx.id_caixa_adv_proc ")
			.append(" AND CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim GROUP BY cx_in.id_caixa_adv_proc) ");
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
		
		return queryPesquisa;
	}

	/**
	 * 
	 * @param idPessoa
	 * @param idProcuradoria
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryProcessosProcuradorJurisdicao(Integer idPessoa, Integer idProcuradoria) {
		StringBuilder strQueryFrom = new StringBuilder(" FROM tb_pessoa_procuradoria pproc ")
				.append(" INNER JOIN tb_pess_proc_jurisdicao ppj ON (pproc.id_pessoa_procuradoria = ppj.id_pessoa_procuradoria) ")
				.append(" INNER JOIN tb_cabecalho_processo ptf ON (ppj.id_jurisdicao = ptf.id_jurisdicao AND ppj.in_ativo = true) ")
				.append(" INNER JOIN tb_processo_parte pp ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A' AND pp.id_procuradoria = pproc.id_procuradoria) ")
				.append(" INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = ptf.id_jurisdicao) ")
				.append(this.obterQueryAdminJurisdicao(true));
			
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ptf.cd_processo_status = 'D' AND pp.id_procuradoria = " + idProcuradoria + " ")
				// Restrições relacionadas ao procurador da jurisdição.
			.append("AND pproc.id_pessoa = " + idPessoa + " ");
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
		
		return queryPesquisa;
	}

	/**
	 * 
	 * @param idPessoa
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryProcessosParte(Integer idPessoa) {
		StringBuilder strQueryFrom = new StringBuilder("FROM tb_processo_parte pp ")
				.append("INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf AND pp.in_situacao = 'A') ")
				.append("INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = ptf.id_jurisdicao) ")
				.append(this.obterQueryAdminJurisdicao(true));
			
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ptf.cd_processo_status = 'D' ")
			.append(" AND pp.id_pessoa = " + idPessoa + " ");
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
		
		return queryPesquisa;
	}

	/**
	 * 
	 * @param idPessoa
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryProcessoRepresentanteParte(Integer idPessoa) {
		StringBuilder strQueryFrom = new StringBuilder("FROM tb_proc_parte_represntante ppr ")
				.append(" INNER JOIN tb_processo_parte pp ON (ppr.id_processo_parte = pp.id_processo_parte ")
				.append("   AND ppr.id_tipo_representante = " + Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)) + "  AND pp.in_situacao = 'A' AND ppr.in_situacao = 'A') ")
				.append(" INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf) ")
				.append(" INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = ptf.id_jurisdicao) ")
				.append(this.obterQueryAdminJurisdicao(true));
			
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ptf.cd_processo_status = 'D' ")
				// Restricoes para representante da parte
			.append(" AND ppr.id_representante = " + idPessoa + " ");
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
		
		return queryPesquisa;
	}

	/**
	 * 
	 * @param idPessoa
	 * @param idLocalizacao
	 * @param isAdminJurisdicao
	 * @return
	 */
	private Map<String, String> obterQueryProcessosAssistenteRepresentanteParte(Integer idPessoa, Integer idLocalizacao) {
		StringBuilder strQueryFrom = new StringBuilder("FROM core.tb_usuario_localizacao ul ")
				.append(" INNER JOIN client.tb_pessoa_localizacao pl ON (ul.id_localizacao_fisica = pl.id_localizacao AND ul.id_usuario != pl.id_pessoa) ")
				.append("INNER JOIN tb_proc_parte_represntante ppr ON (ppr.id_tipo_representante = " + Integer.parseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO))) 
				.append(" AND ppr.in_situacao = 'A' AND ppr.id_representante = pl.id_pessoa) ")
				.append(" INNER JOIN tb_processo_parte pp ON (ppr.id_processo_parte = pp.id_processo_parte AND pp.in_situacao = 'A') ")
				.append(" INNER JOIN tb_cabecalho_processo ptf ON (pp.id_processo_trf = ptf.id_processo_trf) ")
				.append(" INNER JOIN tb_jurisdicao jur ON (jur.id_jurisdicao = ptf.id_jurisdicao) ")
				.append(this.obterQueryAdminJurisdicao(true));
			
		StringBuilder strQueryWhere = new StringBuilder(" WHERE ptf.cd_processo_status = 'D' ")
				// Restrições para assistentes de representante da parte */
			.append(" AND ul.id_usuario = "+idPessoa+" ")
			.append(" AND ul.id_localizacao_fisica = "+idLocalizacao+" ");
		
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
		
		return queryPesquisa;
	}
	
	/**
	 * 
	 * 
	 * @param idPessoa
	 * @param idProcuradoria
	 * @return
	 */
	private String obterQueryRestricoesVisibilidade(Integer idPessoa, Integer idProcuradoria) {
		StringBuilder strQuery = new StringBuilder(" AND (ptf.in_segredo_justica = false OR EXISTS (")
			.append(" SELECT 1 FROM tb_proc_visibilida_segredo vis WHERE vis.id_processo_trf = ptf.id_processo_trf ")
			.append(" AND (vis.id_pessoa = " + idPessoa);
		
		if (idProcuradoria != null) {
			strQuery.append(" OR vis.id_procuradoria = " + idProcuradoria + " ");
		}
		strQuery.append("))) ");

		return strQuery.toString();
	}
	
	/**
	 * 
	 * @param criteriosPesquisa
	 * @return
	 */
	private String obterFiltrosProcessos(ConsultaProcessoVO criteriosPesquisa) {
		StringBuilder strQuery = new StringBuilder();
		
		if(criteriosPesquisa.getIdJurisdicao() != null) {
			strQuery.append(" AND jur.id_jurisdicao = " + criteriosPesquisa.getIdJurisdicao());
		}
		
		// busca por um número do processo dado
		if(criteriosPesquisa.getNumeroProcesso() != null) {
			strQuery.append(" AND ptf.id_processo_trf = (select p.id_processo from tb_processo p where p.nr_processo='"+ criteriosPesquisa.getNumeroProcesso() +"')");
		}else {
			if(criteriosPesquisa.getNumeroSequencia() != null) {
				strQuery.append(" AND ptf.nr_sequencia = "+criteriosPesquisa.getNumeroSequencia());
			}
			if(criteriosPesquisa.getDigitoVerificador() != null) {
				strQuery.append(" AND ptf.nr_digito_verificador = "+criteriosPesquisa.getDigitoVerificador());
			}
			if(criteriosPesquisa.getNumeroAno() != null) {
				strQuery.append(" AND ptf.nr_ano = "+criteriosPesquisa.getNumeroAno());
			}
			if(criteriosPesquisa.getNumeroOrgaoJustica() != null) {
				strQuery.append(" AND ptf.nr_identificacao_orgao_justica = "+criteriosPesquisa.getNumeroOrgaoJustica());
			}
			if(criteriosPesquisa.getNumeroOrigem() != null) {
				strQuery.append(" AND ptf.nr_origem_processo = "+criteriosPesquisa.getNumeroOrigem());
			}
		}		
		
		if(criteriosPesquisa.getIdCaixaAdvProc() != null) {
			strQuery.append(" AND fcaixa.id_caixa_adv_proc = " + criteriosPesquisa.getIdCaixaAdvProc());
		}else {
			if(criteriosPesquisa.getApenasSemCaixa()) {
				strQuery.append(" AND fcaixa.id_caixa_adv_proc IS NULL ");
			}else {
				// Elimina as caixas com período de inatividade atual
				if(criteriosPesquisa.getApenasCaixasAtivas()) {
					strQuery.append(" AND NOT EXISTS (")
						.append(" SELECT 1 FROM tb_periodo_inativ_caixa_rep cx_in WHERE cx_in.id_caixa_adv_proc = fcaixa.id_caixa_adv_proc ")
						.append(" AND CURRENT_TIMESTAMP BETWEEN cx_in.dt_inicio AND cx_in.dt_fim GROUP BY cx_in.id_caixa_adv_proc) ");			
				}
			}
		}
		
		// busca pelo assunto
		if(criteriosPesquisa.getAssuntoJudicialObj() != null && criteriosPesquisa.getAssuntoJudicialObj().getIdAssunto() > 0) {
			strQuery.append(" AND assunto.id_processo_trf = " + criteriosPesquisa.getAssuntoJudicialObj().getIdAssunto());
		}else {
			if(criteriosPesquisa.getAssuntoJudicial() != null) {
				strQuery
					.append(" AND ( ")
						.append("assunto.cd_assunto_trf = '" + criteriosPesquisa.getAssuntoJudicial() + "' ")
						.append(" OR assunto.ds_assunto_trf ilike '%" + criteriosPesquisa.getAssuntoJudicial() + "%'")
					.append(") ");
			}
		}
		
		// busca pela classe
		if(criteriosPesquisa.getClasseJudicialObj() != null && criteriosPesquisa.getClasseJudicialObj().getIdClasseJudicial() > 0) {
			strQuery.append(" AND ptf.id_classe_judicial = " + criteriosPesquisa.getClasseJudicialObj().getIdClasseJudicial());
		}else {
			if(criteriosPesquisa.getClasseJudicial() != null) {
				strQuery
					.append(" AND ( ")
						.append("ptf.cd_classe_judicial = '" + criteriosPesquisa.getClasseJudicial() + "' ")
						.append(" OR ptf.ds_classe_judicial_sigla = '" + criteriosPesquisa.getClasseJudicial() + "'")
						.append(" OR ptf.ds_classe_judicial ilike '%" + criteriosPesquisa.getClasseJudicial() + "%'")
					.append(") ");
			}
		}
		
		// prioridade
		if(criteriosPesquisa.getPrioridadeObj() != null && criteriosPesquisa.getPrioridadeObj().getIdPrioridadeProcesso() > 0) {
			strQuery.append(" AND prioridade.id_prioridade_processo = "+ criteriosPesquisa.getPrioridadeObj().getIdPrioridadeProcesso());
		}else {
			if(criteriosPesquisa.getApenasPrioridade()) {
				strQuery.append(" AND ptf.in_prioridade = true ");
			}
		}
		
		// Data da distribuição do processo
		if(criteriosPesquisa.getDataDistribuicaoInicial() != null) {
			strQuery.append(" AND ptf.dt_distribuicao > '"+new java.sql.Date(criteriosPesquisa.getDataDistribuicaoInicial().getTime()) +"'");
		}
		if(criteriosPesquisa.getDataDistribuicaoFinal() != null) {
			strQuery.append(" AND ptf.dt_distribuicao < '"+ new java.sql.Date(criteriosPesquisa.getDataDistribuicaoFinal().getTime()) +"'");
		}
		
		// Órgão jugador
		if(criteriosPesquisa.getOrgaoJulgadorObj() != null && criteriosPesquisa.getOrgaoJulgadorObj().getIdOrgaoJulgador() > 0) {
			strQuery.append(" AND ptf.id_orgao_julgador = "+criteriosPesquisa.getOrgaoJulgadorObj().getIdOrgaoJulgador());
		}

		// Órgão jugador colegiado
		if(criteriosPesquisa.getOrgaoJulgadorColegiadoObj() != null && criteriosPesquisa.getOrgaoJulgadorColegiadoObj().getIdOrgaoJulgadorColegiado() > 0) {
			strQuery.append(" AND ptf.id_orgao_julgador_colegiado = "+criteriosPesquisa.getOrgaoJulgadorColegiadoObj().getIdOrgaoJulgadorColegiado());
		}

		// destinatario
		if(criteriosPesquisa.getNomeParte() != null) {
			strQuery.append(" AND parte.ds_nome ilike '%" + criteriosPesquisa.getNomeParte() + "%'");
		}
		
		// documento de identificacao
		if(criteriosPesquisa.getCpfParte() != null) {
			strQuery.append(" AND cpf.nr_documento_identificacao = '" + criteriosPesquisa.getCpfParte() + "'");
		}
		if(criteriosPesquisa.getCnpjParte() != null) {
			strQuery.append(" AND cnpj.nr_documento_identificacao = '" + criteriosPesquisa.getCnpjParte() + "'");
		}
		if(criteriosPesquisa.getOutroDocumentoParte() != null) {
			strQuery.append(" AND outroDocIdentificacao.nr_documento_identificacao = '" + criteriosPesquisa.getOutroDocumentoParte() + "'");
		}
		
		// OAB representante
		if(criteriosPesquisa.getOabRepresentanteParte() != null) {
			strQuery.append(" AND oabRepresentante.nr_documento_identificacao = '"+criteriosPesquisa.getOabRepresentanteParte()+"'");
		}
				
		return strQuery.toString();
	}
	
	/**
	 * 
	 * @param idLocalizacao
	 * @param criteriosPesquisa
	 * @return
	 */
	private String obterQueryFromFiltrosProcessos(Integer idLocalizacao, ConsultaProcessoVO criteriosPesquisa) {
		StringBuilder strQuery = new StringBuilder();
		
		if(criteriosPesquisa.getIdCaixaAdvProc() != null || criteriosPesquisa.getApenasCaixasAtivas()) {
			strQuery.append(this.obterQueryFromFiltroCaixas(idLocalizacao));
		}else {
			if(criteriosPesquisa.getApenasSemCaixa()) {
				strQuery.append(this.obterQueryFromFiltroSemCaixa(idLocalizacao));
			}
		}
		
		if((criteriosPesquisa.getAssuntoJudicialObj() != null && criteriosPesquisa.getAssuntoJudicialObj().getIdAssunto() > 0) || (criteriosPesquisa.getAssuntoJudicial() != null)) {
			strQuery.append(this.obterQueryFromAssuntos());
		}
		
		if(criteriosPesquisa.getPrioridadeObj() != null && criteriosPesquisa.getPrioridadeObj().getIdPrioridadeProcesso() > 0) {
			strQuery.append(this.obterQueryFromPrioridade());
		}

		if(
				criteriosPesquisa.getNomeParte() != null 
				|| criteriosPesquisa.getCpfParte() != null || criteriosPesquisa.getCnpjParte() != null || criteriosPesquisa.getOutroDocumentoParte() != null
				|| criteriosPesquisa.getOabRepresentanteParte() != null) {

			strQuery.append(this.obterQueryFromParte());
			
			if(criteriosPesquisa.getNomeParte() != null) {
				strQuery.append(this.obterQueryFromDadosParte());				
			}
		
			if(criteriosPesquisa.getCpfParte() != null || criteriosPesquisa.getCnpjParte() != null || criteriosPesquisa.getOutroDocumentoParte() != null) {
				strQuery.append(this.obterQueryFromDocumentosIdentificacao(criteriosPesquisa));
			}

			if(criteriosPesquisa.getOabRepresentanteParte() != null) {
				strQuery.append(this.obterQueryFromOabRepresentante());
			}
		}
		

		return strQuery.toString();
	}

	private String obterQueryFromParte() {
		StringBuilder strQuery = new StringBuilder();
		strQuery.append(" INNER JOIN client.tb_processo_parte ppparte ON (ptf.id_processo_trf = ppparte.id_processo_trf AND ppparte.in_parte_principal = true AND ppparte.in_situacao in ('A', 'S')) ");
		
		return strQuery.toString();		
	}
	
	private String obterQueryFromOabRepresentante() {
		StringBuilder strQuery = new StringBuilder();

		strQuery.append(" INNER JOIN client.tb_proc_parte_represntante fppr ON (fppr.in_situacao in ('A', 'S') AND fppr.id_processo_parte = ppparte.id_processo_parte) ");
		
		strQuery.append(" INNER JOIN client.tb_pess_doc_identificacao oabRepresentante ON ( ")
			.append(" oabRepresentante.in_ativo = true ")
			.append(" AND oabRepresentante.in_usado_falsamente = false ")
			.append(" AND oabRepresentante.cd_tp_documento_identificacao = 'OAB' ")
			.append(" AND oabRepresentante.id_pessoa = fppr.id_representante) ");
		
		return strQuery.toString();
	}
	
	private String obterQueryFromDadosParte() {
		StringBuilder strQuery = new StringBuilder();
		strQuery.append(" INNER JOIN acl.tb_usuario_login parte ON (parte.id_usuario = ppparte.id_pessoa) ");
		
		return strQuery.toString();		
	}
	
	private String obterQueryFromDocumentosIdentificacao(ConsultaProcessoVO criteriosPesquisa) {
		StringBuilder strQuery = new StringBuilder();
		
		if(criteriosPesquisa.getCpfParte() != null) {
			strQuery.append(" INNER JOIN client.tb_pess_doc_identificacao cpf ON ( ")
				.append(" cpf.in_ativo = true ")
				.append(" AND cpf.in_usado_falsamente = false ")
				.append(" AND cpf.cd_tp_documento_identificacao = 'CPF' ")
				.append(" AND cpf.id_pessoa = ppparte.id_pessoa) ");
		}

		if(criteriosPesquisa.getCnpjParte() != null) {
			strQuery.append(" INNER JOIN client.tb_pess_doc_identificacao cnpj ON ( ")
				.append(" cnpj.in_ativo = true ")
				.append(" AND cnpj.in_usado_falsamente = false ")
				.append(" AND cnpj.cd_tp_documento_identificacao = 'CNPJ' ")
				.append(" AND cnpj.id_pessoa = ppparte.id_pessoa) ");
		}
		
		if(criteriosPesquisa.getOutroDocumentoParte() != null) {
			strQuery.append(" INNER JOIN client.tb_pess_doc_identificacao outroDocIdentificacao ON ( ")
				.append(" outroDocIdentificacao.in_ativo = true ")
				.append(" AND outroDocIdentificacao.in_usado_falsamente = false ")
				.append(" AND outroDocIdentificacao.cd_tp_documento_identificacao not in ('CPF', 'CNPJ') ")
				.append(" AND outroDocIdentificacao.id_pessoa = ppparte.id_pessoa) ");
		}

		return strQuery.toString();
	}
		
	private String obterQueryFromPrioridade() {
		StringBuilder strQuery = new StringBuilder();
		
		strQuery.append(" INNER JOIN client.tb_proc_prioridde_processo prioridade ON (prioridade.id_processo_trf = ptf.id_processo_trf) ");
		
		return strQuery.toString();
	}
	
	private String obterQueryFromAssuntos() {
		StringBuilder strQuery = new StringBuilder();
		strQuery.append(" INNER JOIN client.tb_processo_assunto pass ON (pass.id_processo_trf = ptf.id_processo_trf) ")
			.append(" INNER JOIN client.tb_assunto_trf assunto ON (assunto.id_assunto_trf = pass.id_assunto_trf AND assunto.in_ativo = true) ");

		return strQuery.toString();
	}
	
	private String obterQueryFromFiltroCaixas(Integer idLocalizacao) {
		StringBuilder strQuery = new StringBuilder();
		strQuery.append("INNER JOIN tb_processo_caixa_adv_proc fcx_proc ON (fcx_proc.id_processo_trf = ptf.id_processo_trf) ")
			.append("INNER JOIN tb_caixa_adv_proc fcaixa ON (fcaixa.id_caixa_adv_proc = fcx_proc.id_caixa_adv_proc AND fcaixa.id_localizacao = "+idLocalizacao+") ");
		
		return strQuery.toString();
	}
	
	private String obterQueryFromFiltroSemCaixa(Integer idLocalizacao) {
		StringBuilder strQuery = new StringBuilder();
		strQuery.append(" LEFT JOIN (SELECT caixa.id_caixa_adv_proc, fcx_proc.id_processo_trf FROM tb_processo_caixa_adv_proc fcx_proc ")
			.append(" INNER JOIN tb_caixa_adv_proc caixa ON (caixa.id_caixa_adv_proc = fcx_proc.id_caixa_adv_proc AND caixa.id_localizacao = "+idLocalizacao + ")")
			.append(" ) fcaixa ON (fcaixa.id_processo_trf = ptf.id_processo_trf) ");

		return strQuery.toString();
	}

	
	/**
	 * 
	 * @param idPessoa
	 * @param idProcuradoria
	 * @param criteriosPesquisa
	 * @return
	 */
	public Map<String, String> obterQueryRestricoesProcessos(Integer idPessoa, Integer idLocalizacao, Integer idProcuradoria, ConsultaProcessoVO criteriosPesquisa) {
		StringBuilder strQueryFrom = new StringBuilder(this.obterQueryFromFiltrosProcessos(idLocalizacao, criteriosPesquisa));
		
		StringBuilder strQueryWhere = new StringBuilder(this.obterQueryRestricoesVisibilidade(idPessoa, idProcuradoria))
				.append(this.obterFiltrosProcessos(criteriosPesquisa));
		
		Map<String, String> queryPesquisa = new HashMap<String,String>();
		queryPesquisa.put("FROM", strQueryFrom.toString());
		queryPesquisa.put("WHERE", strQueryWhere.toString());
			
		return queryPesquisa;
	}

	/**
	 * Dada uma lista de consultas de processos e restricoes, este método retorna uma query consolidada de pesquisa de processos que retorna seus IDs
	 *  
	 * @param consultasExpedientes
	 * @param restricoesExpedientes
	 * @return
	 */
	private String consolidarConsultaProcessos(List<Map<String, String>> consultasProcessos, Map<String, String> restricoes, String universoPesquisaProcessos) {
		StringBuilder consultaConsolidada = new StringBuilder();

		consultaConsolidada.append("SELECT DISTINCT id_processo_trf FROM ( ");
		
		int count = 0;
		for (Map<String, String> consulta : consultasProcessos) {
			if (count > 0) {
				consultaConsolidada.append(" UNION ALL ");
			}
			consultaConsolidada.append("SELECT ptf.id_processo_trf ");
			if(consulta.get("FROM") != null) {
				consultaConsolidada.append(consulta.get("FROM"));
			}
			if(restricoes.get("FROM") != null) {
				consultaConsolidada.append(restricoes.get("FROM"));
			}
			if(consulta.get("WHERE") != null) {
				consultaConsolidada.append(consulta.get("WHERE"));
			}
			if(restricoes.get("WHERE") != null){
				consultaConsolidada.append(restricoes.get("WHERE"));
			}			
		 	count++;
		}
		consultaConsolidada.append( ") consulta_consolidada");
		
		String consultaSQL = consultaConsolidada.toString();
		
		if(universoPesquisaProcessos != null) {
			consultaSQL = this.aplicarRestricaoUniversoProcessos(consultaSQL, universoPesquisaProcessos);
		}

		return consultaSQL;
	}
	
	public void merge(ProcessoTrf processoTrf) {
		getEntityManager().merge(processoTrf);
	}
	
	/**
	 * Realiza uma consulta por processos já distribuídos - exceto aqueles
	 * por prevenção (PP) - e vinculados à mesma eleição e origem do processo
	 * em distribuição e que possua vínculo de dependência eleitoral.
	 */
	public ProcessoTrf obterProcessoDistribuido(Eleicao eleicao, VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral, Estado estado, Municipio municipio) {
		ProcessoTrf processoTrf = null;
		StringBuilder jpql = new StringBuilder();

		jpql.append("select p from ProcessoTrfLogDistribuicao l ")
			.append("inner join l.processoTrf p ")
			.append("inner join p.processoAssuntoList pa ")
			.append("inner join p.complementoJE comp ")
			.append("where comp.eleicao = :eleicao ")
			.append("and comp.vinculacaoDependenciaEleitoral = :vinculo ")
			.append("and p.numeroSequencia != null ")
			.append("and p.processoStatus = :processoStatus ");

		if (eleicao.isGeral()) {
			jpql.append("and comp.estadoEleicao = :origem ");
		} else if (eleicao.isMunicipal()) {
			jpql.append("and comp.municipioEleicao = :origem ");
		}
		jpql.append("order by comp.dtAtualizacao asc ");

		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("eleicao", eleicao);
		query.setParameter("processoStatus", ProcessoStatusEnum.D);
		query.setParameter("vinculo", vinculacaoDependenciaEleitoral);

		if (eleicao.isGeral()) {
			query.setParameter("origem", estado);
		} else if (eleicao.isMunicipal()) {
			query.setParameter("origem", municipio);
		}

		query.setMaxResults(1);

		try {
			processoTrf = (ProcessoTrf) query.getSingleResult();
		} catch (NoResultException no) {
			log.info("Nenhum resultado encontrado");
		}
		return processoTrf;
	}
	
	public ProcessoTrf obterPorNumero(String nrProcesso){
		StringBuilder jpql = new StringBuilder();
		jpql.append(" SELECT pTrf FROM ProcessoTrf pTrf");
		jpql.append(" JOIN pTrf.processo proc");
		jpql.append(" WHERE proc.numeroProcesso = :numeroProcesso");
		
		Query query = getEntityManager().createQuery(jpql.toString());
		query.setParameter("numeroProcesso", nrProcesso);
		return (ProcessoTrf) query.getSingleResult();
	}
	
	public List<ProcessoTrf> consultarProcessosPorProcessoReferencia(String numeroProcessoReferencia) {
		List<ProcessoTrf> result = new ArrayList<>();
		
		if (StringUtils.isNotBlank(numeroProcessoReferencia)) {			
			StringBuilder jpql = new StringBuilder();
			jpql.append(" SELECT ptrf FROM ProcessoTrf ptrf ");
			jpql.append(" WHERE regexp_replace(ptrf.desProcReferencia, '[^0-9]', '', 'g') = :numeroProcessoReferencia and ptrf.processoStatus = 'D' ");
			
			Query query = getEntityManager().createQuery(jpql.toString());
			query.setParameter("numeroProcessoReferencia", numeroProcessoReferencia);
			return query.getResultList();
		}
		return result;
	}
	
	
    public void gravarSugestaoSessao(ProcessoTrf processo, Sessao sessao) {
        String query;
        if(sessao != null) {
            query = "UPDATE client.tb_processo_trf set id_sessao_sugerida = " + sessao.getIdSessao() + " , dt_sugestao_sessao = '" + sessao.getDataSessao() + "' WHERE id_processo_trf = " + processo.getIdProcessoTrf();   
        } else {
            query = "UPDATE client.tb_processo_trf set id_sessao_sugerida = null , dt_sugestao_sessao = null WHERE id_processo_trf = " + processo.getIdProcessoTrf();
        }
        getEntityManager().createNativeQuery(query).executeUpdate();
    }

	@SuppressWarnings("unchecked")
	public int[] getProcessosEnviadosAoDomicilioEletronico() throws Exception {
		if (entityManager == null || !entityManager.isOpen()) {
			throw new Exception("EntityManager nulo ou fechado ao consultar processos para o fluxo de tratamento de comunicações.");
		}

	    StringJoiner sql = new StringJoiner("\n")
				.add("     SELECT trf.id_processo_trf                                                                                      ")
				.add("       FROM client.tb_processo_trf          AS trf                                                                   ")
				.add(" INNER JOIN   core.tb_processo              AS tp                                                                    ")
				.add("         ON   tp.id_processo            = trf.id_processo_trf                                                        ")
				.add(" INNER JOIN client.tb_processo_expediente   AS tpe                                                                   ")
				.add("         ON  tpe.id_processo_trf        =  tp.id_processo                                                            ")
				.add(" INNER JOIN client.tb_proc_parte_expediente AS tppe                                                                  ")
				.add("         ON tppe.id_processo_expediente = tpe.id_processo_expediente                                                 ")
				.add("        AND tppe.id_processo_trf        = tpe.id_processo_trf                                                        ")
				.add("  LEFT JOIN client.tb_log_integracao        AS tli                                                                   ")
				.add("         ON  tli.id_processo_parte_expediente = tppe.id_processo_parte_expediente					   				   ")
				.add("        AND  tli.numero_processo        = tp.nr_processo                   					   					   ")
				.add("  LEFT JOIN client.tb_processo_tarefa       AS tpt                                                                   ")
				.add("         ON  tpt.id_processo_trf        = tp.id_processo                                                             ")
				.add("        AND  tpt.nm_fluxo               = 'Tratamento de Comunicações Domicílio Eletrônico'                          ")
				.add("      WHERE                                                                                                          ")
				.add("             tpe.dt_criacao_expediente        >= :ultimaDataHoraJob                                                  ")
				.add("        AND  tpe.in_meio_expedicao_expediente  = 'E'                                                                 ")
				.add("        AND  tpe.dt_exclusao                   IS NULL                                                               ")
				.add("        AND  tpt.id_processo_trf               IS NULL -- Esta condição substitui a cláusula NOT EXISTS              ")
				.add("        AND (                                                                                                        ")
				.add("                (    tppe.dt_ciencia_parte     IS NULL                                                               ")
				.add("                 AND tppe.in_fechado           IS FALSE                                                              ")
				.add("                 AND tppe.in_enviado_domicilio IS TRUE                                                               ")
				.add("                )                                                                                                    ")
				.add("             OR                                                                                                      ")
				.add("                (tli.request_method = 'POST')                                                                        ")
				.add("         )                                                                                                           ")
				.add("   GROUP BY trf.id_processo_trf;                                                                                     ");

		Timestamp ultimaDataHoraJob = ComponentUtil.getParametroUtil().getDataHoraUltimaExecucaoJobTCD();

		Query query = entityManager.createNativeQuery(sql.toString());

		query.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
		query.setHint("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);

		query.setParameter("ultimaDataHoraJob", ultimaDataHoraJob);

		final int MAX_TENTATIVAS = 60;

		// TENTA REALIZAR A CONSULTA POR ATÉ 60 SEGUNDOS (60 TENTATIVAS DE 1 SEGUNDO CADA)
		for (int indiceTentativa = 0; indiceTentativa < MAX_TENTATIVAS; ++indiceTentativa) {
			try {
				List<Integer> resultList = (List<Integer>) query.getResultList();

				return resultList.stream().mapToInt(Integer::intValue).toArray();
			} catch (PessimisticLockException e) {
				logger.warn("Lock no banco ao consultar processos para o fluxo de tratamento de comunicações. Erro: [{0}]", e.getLocalizedMessage());
			} catch (NoResultException e) {
				logger.warn("Nenhum processo encontrado. Erro: [{0}]", e.getLocalizedMessage());

				break;
			} catch (Exception e) {
				throw new Exception("Houve um erro ao consultar processos para o fluxo de tratamento de comunicações. Excecao: [{0}]", e);
			}

			TimeUnit.SECONDS.sleep(1);
		}

		return new int[0];
	}

	public List<Integer> consultarIdProcessoDistribuidoPorIdPessoaAtivo(Integer idPessoa) {
		StringBuilder sql = new StringBuilder(0);
		sql.append( "select distinct tpp.id_processo_trf from tb_processo_parte tpp ");
		sql.append( "inner join tb_processo_trf tpt on tpp.id_processo_trf = tpt.id_processo_trf  ");
		sql.append( "where tpp.id_pessoa = :idPessoa ");
		sql.append( "and tpt.cd_processo_status = 'D' ");
		sql.append( "and tpp.in_situacao = 'A' ");
		Query query = getEntityManager().createNativeQuery(sql.toString());
		query.setParameter("idPessoa", idPessoa);
		return (List<Integer>) query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> recuperarProcessosApensados(ProcessoTrf processoTrf) {
		StringBuilder query = new StringBuilder();

		query.append(" WITH eventos AS ( ");
		query.append("    SELECT tp.id_processo, tp2.id_processo AS id_processo_apensado, tpe.dt_atualizacao, te.cd_evento ");
		query.append("    FROM core.tb_processo tp ");
		query.append("    JOIN core.tb_processo_evento tpe ON tpe.id_processo = tp.id_processo ");
		query.append("    JOIN client.tb_complemento_segmentado tcs ON tcs.id_movimento_processo = tpe.id_processo_evento ");
		query.append("    JOIN core.tb_processo tp2 ON tp2.nr_processo = tcs.ds_valor_complemento ");
		query.append("    JOIN core.tb_evento te ON te.id_evento = tpe.id_evento ");
		query.append("    WHERE tp.id_processo = :idProcesso ");
		query.append("    AND tpe.in_ativo = true ");
		query.append("    AND te.cd_evento IN ('135', '137') ");
		query.append("), ");
		query.append(" filtros AS ( ");
		query.append("    SELECT id_processo_apensado ");
		query.append("    FROM eventos ");
		query.append("    GROUP BY id_processo_apensado ");
		query.append("    HAVING MAX(CASE WHEN cd_evento = '135' THEN dt_atualizacao END) < ");
		query.append("           MAX(CASE WHEN cd_evento = '137' THEN dt_atualizacao END) ");
		query.append(") ");
		query.append(" ");
		query.append(" SELECT distinct tpt.* ");
		query.append(" FROM core.tb_processo tp ");
		query.append(" JOIN core.tb_processo_evento tpe ON tpe.id_processo = tp.id_processo ");
		query.append(" JOIN client.tb_complemento_segmentado tcs ON tcs.id_movimento_processo = tpe.id_processo_evento ");
		query.append(" JOIN core.tb_processo tp2 ON tp2.nr_processo = tcs.ds_valor_complemento ");
		query.append(" JOIN core.tb_evento te ON te.id_evento = tpe.id_evento ");
		query.append(" JOIN client.tb_processo_trf tpt on tpt.id_processo_trf  = tp2.id_processo  ");
		query.append(" WHERE tp.id_processo = :idProcesso ");
		query.append(" AND tpe.in_ativo = true ");
		query.append(" AND te.cd_evento IN ('135', '137') ");
		query.append(" AND tp2.id_processo NOT IN (SELECT id_processo_apensado FROM filtros) ");

		Query q = entityManager.createNativeQuery(query.toString(), ProcessoTrf.class);
		q.setParameter("idProcesso", processoTrf.getProcesso().getIdProcesso());


		return q.getResultList();
	}
		
	public boolean possuiMenor(ProcessoTrf processo, ProcessoParteParticipacaoEnum polo, ProcessoParteSituacaoEnum... situacao) {
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT COUNT(DISTINCT p.idProcessoParte) FROM ProcessoParte AS p JOIN p.pessoaFisica pf ");
		hql.append("WHERE p.processoTrf = :processo AND pf.dataNascimento > :dataLimiteMenor AND p.partePrincipal = true ");
		adicionarCondicaoDePoloESituacao(polo, hql, situacao);

		Query query = entityManager.createQuery(hql.toString());

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -18);
		Date dataLimiteMenor = calendar.getTime();

		query.setParameter("dataLimiteMenor", dataLimiteMenor);

		adicionarParametroProcessoEIfNeedPoloESituacao(processo, polo, query, situacao);

		return (Long) query.getSingleResult() > 0;
	}

	public boolean possuiDefensoria(ProcessoTrf processo, ProcessoParteParticipacaoEnum polo, ProcessoParteSituacaoEnum... situacao) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT COUNT(DISTINCT p.idProcessoParte) FROM ProcessoParte AS p JOIN p.procuradoria procuradoria ");
		stringBuilder.append("WHERE p.processoTrf = :processo AND p.partePrincipal = true AND procuradoria.tipo = :tipo ");
		adicionarCondicaoDePoloESituacao(polo, stringBuilder, situacao);

		Query query = entityManager.createQuery(stringBuilder.toString());
		adicionarParametroProcessoEIfNeedPoloESituacao(processo, polo, query, situacao);
		query.setParameter("tipo", TipoProcuradoriaEnum.D);

		return (Long) query.getSingleResult() > 0;
	}

	public boolean possuiProcuradoria(ProcessoTrf processo, ProcessoParteParticipacaoEnum polo, int idProcuradoria, ProcessoParteSituacaoEnum... situacao) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(DISTINCT p.idProcessoParte) FROM ProcessoParte AS p JOIN p.procuradoria procuradoria ");
		sb.append("WHERE p.processoTrf = :processo AND p.partePrincipal = true AND procuradoria.idProcuradoria = :idProcuradoria ");
		adicionarCondicaoDePoloESituacao(polo, sb, situacao);

		Query query = entityManager.createQuery(sb.toString());
		
		adicionarParametroProcessoEIfNeedPoloESituacao(processo, polo, query, situacao);
		query.setParameter("idProcuradoria", idProcuradoria);

		return (Long) query.getSingleResult() > 0;
	}

	private void adicionarParametroProcessoEIfNeedPoloESituacao(ProcessoTrf processo, ProcessoParteParticipacaoEnum polo, Query query,
			ProcessoParteSituacaoEnum... situacao) {
		query.setParameter(PROCESSO2, processo);
		
		if (polo != null) {
			query.setParameter(POLO2, polo);
		}
		if (situacao != null) {
			query.setParameter(SITUACAO2, Arrays.asList(situacao));
		}
	}

	private void adicionarCondicaoDePoloESituacao(ProcessoParteParticipacaoEnum polo, StringBuilder hql,
			ProcessoParteSituacaoEnum... situacao) {
		if (polo != null) {
			hql.append(AND_P_IN_PARTICIPACAO_POLO);
		}
		if (situacao != null) {
			hql.append(AND_P_IN_SITUACAO_IN_SITUACAO);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> getProcessos(List<Integer> ids) {
		List<ProcessoTrf> result = new ArrayList<>();
		if (ids != null && !ids.isEmpty()) {			
			String jpqlString = "SELECT p FROM ProcessoTrf p WHERE p.idProcessoTrf in (:ids)";
			Query query = getEntityManager().createQuery(jpqlString);
			query.setParameter("ids", ids);
			return query.getResultList();
		}
		return result;
	}
}
