/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;

import br.com.infox.pje.query.ProcessoExpedienteQuery;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * @author cristof
 * 
 */
@Name("processoExpedienteDAO")
public class ProcessoExpedienteDAO extends BaseDAO<ProcessoExpediente> implements ProcessoExpedienteQuery{

	public static enum CriterioPesquisa {
		INTIMACAO_PENDENTE,
		INTIMACAO_AUTOMATICA,
		INTIMACAO_INTERESSADO,
		ENCERRADO_MENOS_10,
		SEM_PRAZO;
	}

	private static String SQL_PENDENTE = "SELECT o FROM ProcessoExpediente AS o "
		+ "	WHERE o.destinatario IN (:destinatario) " + "		AND o.dtCiencia IS NULL " + "		AND o.resposta IS NULL";

	private static String SQL_INTIMACAO_AUTOMATICA = "SELECT o FROM ProcessoExpediente AS o "
		+ "	WHERE o.destinatario IN (:destinatario) " + "		AND o.cienciaSistema = true "
		+ "		AND o.dtCiencia IS NOT NULL " + "		AND o.dtPrazoLegal >= :agora " + "		AND o.dtPrazoLegal IS NOT NULL"
		+ "		AND o.resposta IS NULL";

	private static String SQL_INTIMACAO_INTERESSADO = "SELECT o FROM ProcessoExpediente AS o "
		+ "	WHERE o.destinatario IN (:destinatario) " + "		AND o.cienciaSistema = false "
		+ "		AND o.dtCiencia IS NOT NULL " + "		AND o.dtPrazoLegal >= :agora " + "		AND o.dtPrazoLegal IS NOT NULL"
		+ "		AND o.resposta IS NULL";

	private static String SQL_ENCERRADO_MENOS_10 = "SELECT o FROM ProcessoExpediente AS o "
		+ "	WHERE o.destinatario IN (:destinatario) " + "		AND o.dtPrazoLegal IS NOT NULL"
		+ "		AND o.dtPrazoLegal >= :dataLimite " + "		AND o.dtPrazoLegal < :agora" + "		AND o.resposta IS NULL";

	private static String SQL_SEM_PRAZO = "SELECT o FROM ProcessoExpediente AS o "
		+ "	WHERE (o.prazoLegal IS NULL OR o.prazoLegal = 0) " + "		AND o.resposta IS NULL";

	private static String SQL_ULTIMO_EXPEDIENTE = "select e from ProcessoExpediente e where e.dtCriacao = " 
		+ " (select max(e2.dtCriacao) from ProcessoExpediente e2 where e2.processoTrf = :" + QUERY_PARAM_PROCESSO_TRF
		+ " and e2.dtCriacao >= :data) and e.processoTrf = :" + QUERY_PARAM_PROCESSO_TRF;
	
	private static String SQL_TELEGRAMA = "select e from ProcessoExpediente e "
	    + "where e.dtCriacao between :dataInicio and :dataFim "
		+ "and e.meioExpedicaoExpediente = 'G' "
	    + "and e.dtExclusao is null";
	
	@Override
	public Integer getId(ProcessoExpediente e){
		return e.getIdProcessoExpediente();
	}

	public List<ProcessoExpediente> findByIds(List<Integer> ids) {
		if (CollectionUtils.isNotEmpty(ids)){
			Search search = new Search(ProcessoExpediente.class);
			try {
				search.addCriteria(Criteria.in("idProcessoExpediente", ids.toArray()));
				return list(search);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
				return Collections.emptyList();
			}
		} else {
			return Collections.emptyList();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoExpediente> getAtosComunicacao(Pessoa[] destinatarios, Integer firstRow, Integer maxRows,
			CriterioPesquisa criterio) throws PJeDAOException{
		Query q = null;
		Date agora = null;
		if (destinatarios.length == 0){
			throw new IllegalArgumentException(
					"A lista de destinatários para pesquisa deve conter pelo menos um destinatário.");
		}
		switch (criterio){
		case INTIMACAO_PENDENTE:
			q = getEntityManager().createQuery(SQL_PENDENTE);
			q.setParameter("destinatario", Util.isEmpty(destinatarios)?null:destinatarios);
			break;
		case INTIMACAO_AUTOMATICA:
			agora = new Date();
			q = getEntityManager().createQuery(SQL_INTIMACAO_AUTOMATICA);
			q.setParameter("destinatario", Util.isEmpty(destinatarios)?null:destinatarios);
			q.setParameter("agora", agora);
			break;
		case INTIMACAO_INTERESSADO:
			agora = new Date();
			q = getEntityManager().createQuery(SQL_INTIMACAO_INTERESSADO);
			q.setParameter("destinatario", Util.isEmpty(destinatarios)?null:destinatarios);
			q.setParameter("agora", agora);
			break;
		case ENCERRADO_MENOS_10:
			q = getEntityManager().createQuery(SQL_ENCERRADO_MENOS_10);
			agora = new Date();
			Date date = DateUtil.dataMenosDias(agora, 10);
			q.setParameter("destinatario", Util.isEmpty(destinatarios)?null:destinatarios);
			q.setParameter("agora", agora);
			q.setParameter("dataLimite", date);
			break;
		case SEM_PRAZO:
			q = getEntityManager().createQuery(SQL_SEM_PRAZO);
			q.setParameter("destinatario", destinatarios);
			break;
		}
		if (firstRow != null && firstRow.intValue() > 0){
			q.setFirstResult(firstRow.intValue());
		}
		if (maxRows != null && maxRows.intValue() > 0){
			q.setMaxResults(maxRows.intValue());
		}
		try{
			return q.getResultList();
		} catch (Exception e){
			throw new PJeDAOException(e.getLocalizedMessage(), e);
		}
	}

	public int contagemAtos(Pessoa[] destinatarios, CriterioPesquisa criterio){
		Query q = null;
		Date agora = null;
		if (destinatarios.length == 0){
			throw new IllegalArgumentException(
					"A lista de destinatários para pesquisa deve conter pelo menos um destinatário.");
		}
		String sql = null;
		switch (criterio){
		case INTIMACAO_PENDENTE:
			sql = SQL_PENDENTE.replace("SELECT o FROM ", "SELECT count(o) FROM");
			q = getEntityManager().createQuery(sql);
			q.setParameter("destinatario", Util.isEmpty(destinatarios)?null:destinatarios);
			break;
		case INTIMACAO_AUTOMATICA:
			sql = SQL_INTIMACAO_AUTOMATICA.replace("SELECT o FROM ", "SELECT count(o) FROM");
			agora = new Date();
			q = getEntityManager().createQuery(sql);
			q.setParameter("destinatario", Util.isEmpty(destinatarios)?null:destinatarios);
			q.setParameter("agora", agora);
			break;
		case INTIMACAO_INTERESSADO:
			sql = SQL_INTIMACAO_INTERESSADO.replace("SELECT o FROM ", "SELECT count(o) FROM");
			agora = new Date();
			q = getEntityManager().createQuery(sql);
			q.setParameter("destinatario", Util.isEmpty(destinatarios)?null:destinatarios);
			q.setParameter("agora", agora);
			break;
		case ENCERRADO_MENOS_10:
			sql = SQL_ENCERRADO_MENOS_10.replace("SELECT o FROM ", "SELECT count(o) FROM");
			q = getEntityManager().createQuery(sql);
			agora = new Date();
			Date date = DateUtil.dataMenosDias(agora, 10);
			q.setParameter("destinatario", Util.isEmpty(destinatarios)?null:destinatarios);
			q.setParameter("agora", agora);
			q.setParameter("dataLimite", date);
			break;
		case SEM_PRAZO:
			sql = SQL_SEM_PRAZO.replace("SELECT o FROM ", "SELECT count(o) FROM");
			q = getEntityManager().createQuery(sql);
			q.setParameter("destinatario", destinatarios);
			break;
		}
		return (Integer) q.getSingleResult();
	}

	/**
	 * Conta a quantidade de expedientes ativos para o processo informado.
	 * 
	 * @param processoTrf
	 * @return quantidade de expedientes ativos.
	 */
	public Long countProcessoExpedienteAtivo(ProcessoTrf processoTrf){
		Query q = getEntityManager().createQuery(COUNT_EXPEDIENTE_ATIVO_BY_PROCESSO_TRF_QUERY);
		q.setParameter(QUERY_PARAM_PROCESSO_TRF, processoTrf);

		Long result = EntityUtil.getSingleResult(q);
		return result;
	}

	/**
	 * Obtem os processoDocumentos de um determinado processo através de um tipo de documento.
	 * 
	 * @param proc Processo que se deseja obter os documentos.
	 * @param tpd Tipo do Documento que deseja se consultar do <cod>proc</code>
	 * @return Lista de Processos Documento
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoExpediente> listNaoEnviados(ProcessoTrf proc){
		Query q = getEntityManager().createQuery(LIST_NAO_ENVIADOS_QUERY);
		q.setParameter(QUERY_PARAM_PROCESSO_TRF, proc);

		List<ProcessoExpediente> list = q.getResultList();
		return list;
	}

	/**
	 * Retorna o último expediente do processo e que tenha sido criado após determinada data.
	 * @param processo 
	 * @param data
	 * @return
	 */
	public ProcessoExpediente getUltimoProcessoExpedienteApos(ProcessoTrf processo, Date data) {
		Query q = getEntityManager().createQuery(SQL_ULTIMO_EXPEDIENTE);
		q.setParameter(QUERY_PARAM_PROCESSO_TRF, processo);
		q.setParameter("data", data);
		
		return (ProcessoExpediente) EntityUtil.getSingleResult(q);
	}

	/**
	 * Metodo que verifica se tem expedientes para que tenham sido construídos com aquele ProcessoDocumento ou utilizando aquele ProcessoDocumento como vinculado
	 * 
	 * @param idProcessoDocumento ProcessoDocumento
	 * @param meios Meios de expedição que tenham sido utilizados para criar o expediente
	 * @return True se tiver expediente para os documentos
	 */
	public boolean verificarExistenciaExpedientePublicacao(Integer idProcessoDocumento, ExpedicaoExpedienteEnum...meios){
		StringBuilder query = new StringBuilder();
		query.append(" SELECT COUNT(pe.id_processo_expediente) FROM client.tb_processo_expediente pe ");
		query.append(" WHERE (pe.id_processo_documento = :idProcDoc OR pe.id_proc_documento_vinculado = :idProcDocVinculado) ");
		query.append(" AND (pe.in_meio_expedicao_expediente IN (:meios)) ");
		Query q = getEntityManager().createNativeQuery(query.toString());
		q.setParameter("idProcDoc", idProcessoDocumento);
		q.setParameter("idProcDocVinculado", idProcessoDocumento);
		q.setParameter("meios", (Arrays.asList(meios)).toString());
		q.setMaxResults(1);
		Number qtd = (Number) q.getSingleResult();
		return qtd.intValue() > 0;
	}

	/**
	 * Obtem os processoDocumentos com meio Telegrama criados em uma determinada data.
	 * @param data a data a ser considerada.
	 * @return a lista de ProcessoExpediente criados nesse dia.
	 */
	public List<ProcessoExpediente> getAtosComunicacaoTelegrama(Date data) {
		return getAtosComunicacaoTelegrama(data, data);
	}

	/**
	 * Obtem os processoDocumentos com meio Telegrama criados em um intervalo de datas.
	 * @param dataInicio a data do início do intervalo.
	 * @param dataFim a data do fim do intervalo.
	 * @return a lista de ProcessoExpediente criados nesse intervalo de datas.
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public List<ProcessoExpediente> getAtosComunicacaoTelegrama(Date dataInicio, Date dataFim) {
		Query q = getEntityManager().createQuery(SQL_TELEGRAMA);
		
		Date inicio = DateUtil.getBeginningOfDay(dataInicio);
		Date fim = DateUtil.getEndOfDay(dataFim);
		
		q.setParameter("dataInicio", inicio);
		q.setParameter("dataFim", fim);
		
		List<ProcessoExpediente> atosComunicacaoTelegrama = q.getResultList();
		return atosComunicacaoTelegrama;
	}
}
