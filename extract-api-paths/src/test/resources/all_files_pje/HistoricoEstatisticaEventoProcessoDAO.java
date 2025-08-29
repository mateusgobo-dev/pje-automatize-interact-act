package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.HistoricoEstatisticaEventoProcessoQuery;
import br.com.itx.util.EntityUtil;

/**
 * Classe com as consultas a entidade de HistoricoEstatisticaEventoProcesso.
 * 
 * @author Wilson
 * 
 */
@Name(HistoricoEstatisticaEventoProcessoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class HistoricoEstatisticaEventoProcessoDAO extends GenericDAO implements Serializable,
		HistoricoEstatisticaEventoProcessoQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "historicoEstatisticaEventoProcessoDAO";

	/**
	 * Obtem a lista para verificar se na tabela
	 * HistoricoEstatisticaEventoProcesso ocorreu uma atualização
	 * 
	 * @return a seção e a data de atualização
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> listSecaoNaoAtualizada() {
		Query q = getEntityManager().createQuery(LIST_SECAO_NAO_ATUALIZADA_QUERY);
		List<Object[]> resultList = null;
		resultList = q.getResultList();
		return resultList;
	}

	public String getDataAtualizacaoSessao(String estado) {
		Query q = getEntityManager().createQuery(DT_ULTIMA_ATUALIZACAO_SESSAO_QUERY);
		q.setParameter(QUERY_PARAMETER_COD_ESTADO, estado);
		String result = EntityUtil.getSingleResult(q);
		return result;
	}
}