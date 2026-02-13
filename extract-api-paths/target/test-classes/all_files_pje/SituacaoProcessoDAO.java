package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.SituacaoProcessoQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.Tarefa;

@Name(SituacaoProcessoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SituacaoProcessoDAO extends GenericDAO implements Serializable, SituacaoProcessoQuery {

	private static final String ID_PROCESSO = "idProcesso";
	private static final long serialVersionUID = 1L;
	public static final String NAME = "situacaoProcessoDAO";

	public Long countProcessosByFluxoAndTarefa(String nomeFluxo, String nomeTarefa) {
		Query q = getEntityManager().createQuery(COUNT_BY_FLUXO_TAREFA_QUERY);
		q.setParameter(QUERY_PARAM_NOME_FLUXO, nomeFluxo);
		q.setParameter(QUERY_PARAM_NOME_TAREFA, nomeTarefa);
		return EntityUtil.getSingleResult(q);
	}
	
	public SituacaoProcesso obtemSituacaoByProcesso(Integer idProcesso) {
		Query q = getEntityManager().createQuery(SITUACAO_BY_PROCESSO_QUERY);
		q.setParameter(QUERY_PARAM_ID_PROCESSO, idProcesso);
		return EntityUtil.getSingleResult(q);
	}
	
	@SuppressWarnings("unchecked")
	public boolean existeSituacaoComTarefaByProcessoSemFiltros(Integer idProcesso) {
		String q = 
				" SELECT nm_tarefa " +
				" FROM tb_processo_tarefa " +
				" WHERE id_processo_trf = :idProcesso " +
				" AND nm_tarefa IS NOT null AND nm_tarefa != '' ";
		
		EntityManager em = EntityUtil.getEntityManager();
		List<Object[]> situacoes = em.createNativeQuery(q).setParameter(ID_PROCESSO, idProcesso).getResultList();
		return ProjetoUtil.isNotVazioSize(situacoes);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> listTarefasByProcessoSemFiltros(Integer idProcesso) {
		String q = 
				" SELECT distinct nm_tarefa " +
				" FROM tb_processo_tarefa " +
				" WHERE id_processo_trf = :idProcesso " +
				" AND nm_tarefa IS NOT null AND nm_tarefa != '' ";
		EntityManager em = EntityUtil.getEntityManager();
		return em.createNativeQuery(q).setParameter(ID_PROCESSO, idProcesso).getResultList();
	}

	public SituacaoProcesso obtemSituacaoByTaskInstance(Long idTaskInstance) {
		Query q = getEntityManager().createQuery("select s from SituacaoProcesso s where s.idTaskInstance = :idT");
		q.setParameter("idT", idTaskInstance);
		return EntityUtil.getSingleResult(q);
	}

	public SituacaoProcesso obtemSituacaoByTaskInstanceLocalizacoes(Long idTaskInstance, List<Integer> idsLocalizacoesFisicas, Integer idOrgaoJulgadorColegiado, 
			boolean isServidorExclusivoOJC, boolean isVisualizaSigiloso) {
		
		StringBuilder hql = new StringBuilder("SELECT s ");
		
		String idsLocalizacoesFilhas = "-1";
		if(idsLocalizacoesFisicas != null) {
			idsLocalizacoesFilhas = StringUtils.join(idsLocalizacoesFisicas, ", ");
		}
		hql.append(this.getQueryFromTarefasPermissoes("s", true, isVisualizaSigiloso, idsLocalizacoesFilhas, 
				isServidorExclusivoOJC, idOrgaoJulgadorColegiado));
				
		hql.append(" AND s.idTaskInstance = :idT ");
		
		Query q = getEntityManager().createQuery(hql.toString());
		q.setParameter("idT", idTaskInstance);
		return EntityUtil.getSingleResult(q);
	}
	
	public SituacaoProcesso obtemSituacaoByTaskInstanceOrgaoJulgadorColegiado(Long idTaskInstance, Integer idOrgaoJulgadorColegiado) {
		Query q = getEntityManager().createQuery("SELECT s FROM SituacaoProcesso s WHERE s.idTaskInstance = :idT AND idOrgaoJulgadoColegiado = :idOjc ");
		q.setParameter("idT", idTaskInstance);
		q.setParameter("idOjc", idOrgaoJulgadorColegiado.longValue());
		return EntityUtil.getSingleResult(q);
	}
	
	/**
	 * Obtem os processos que estão na tarefa informada e verifica se não existe
	 * nenhum documento que foi incluido depois da data em que o processo chegou
	 * na tarefa.
	 * 
	 * @param t
	 *            tarefa em que deseja se obter os processos
	 * @return lista de ids dos processos que estão na tarefa <code>t</code>
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> listProcessosByTarefaDocumento(Tarefa t) {
		Query q = getEntityManager().createQuery(LIST_PROCESSOS_BY_TAREFA_DOCUMENTO_QUERY);
		q.setParameter(QUERY_PARAM_ID_TAREFA, t.getIdTarefa());
		return q.getResultList();
	}
	
	/**
	 * Obtém os processos que estão na tarefa com o identificador informado.
	 * 
	 * @param idTarefa o identificador da tarefa nas quais estão os processos que se pretende identificador
	 * @return lista de identificadores dos processos que estão na tarefa
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> listProcessosByTarefa(Integer idTarefa) {
		String query = "SELECT s.idProcesso FROM SituacaoProcesso s WHERE s.idTarefa = :idTarefa";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("idTarefa", idTarefa);
		return q.getResultList();
	}
	
	public SituacaoProcesso getByIdSituacaoIdTarefa(Long idSituacao, Integer idTarefa){
		Query q = getEntityManager().createQuery("select s from SituacaoProcesso s where s.id = :idS and s.idTarefa = :idT");
		q.setParameter("idS", idSituacao);
		q.setParameter("idT", idTarefa);
		return EntityUtil.getSingleResult(q);
	}
	
	public String getQueryFromTarefasPermissoes(String siglaEntidade, 
			boolean isSegredo, boolean isVisualizaSigiloso, String idsLocalizacoesFilhas, 
			boolean isServidorExclusivoColegiado, Integer idOrgaoJulgadorColegiado) {
		
		StringBuilder hqlFrom = new StringBuilder(" FROM SituacaoProcesso ").append(siglaEntidade).append(" WHERE 1=1 ");
		if(!(isSegredo && isVisualizaSigiloso)) {
			hqlFrom.append(" AND (").append(siglaEntidade).append(".segredoJustica = false ").append(" OR ").append(siglaEntidade).append(".segredoJustica IS NULL ").append(") ");
		}
		if(idOrgaoJulgadorColegiado != null) {
			hqlFrom.append(" AND (")
				.append(" true = ").append(isServidorExclusivoColegiado)
				.append(" OR ")
				.append(siglaEntidade).append(".idLocalizacao IN (").append(idsLocalizacoesFilhas).append(")")
			.append(") ")
			.append(" AND ").append(siglaEntidade).append(".idOrgaoJulgadoColegiado IN (").append(idOrgaoJulgadorColegiado.longValue()).append(") ");
		}else {
			hqlFrom.append(" AND ")
				.append(siglaEntidade).append(".idLocalizacao IN (").append(idsLocalizacoesFilhas).append(")");
		}
		
		return hqlFrom.toString();
	}
	
	public SituacaoProcesso getSituacaoProcessoByIdProcessoFluxo(int idProcesso, Fluxo fluxo) {
		StringBuilder sb = new StringBuilder();
		sb.append("select pt ");
		sb.append("from SituacaoProcesso pt ");
		sb.append("where pt.idProcesso = :idProcesso ");
		sb.append(  "and pt.tarefa.fluxo = :fluxo ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter(ID_PROCESSO, idProcesso);
		q.setParameter("fluxo", fluxo);
		q.setMaxResults(1);
		return EntityUtil.getSingleResult(q);
	}

	@SuppressWarnings("unchecked")
	public List<SituacaoProcesso> getByProcessoSemFiltros(Integer idProcesso) {
		Query q = getEntityManager().createNativeQuery("select * from client.tb_processo_tarefa pt where pt.id_processo_trf = :idProcesso", SituacaoProcesso.class);
		q.setParameter(ID_PROCESSO, idProcesso);
		return q.getResultList();
	}	
}