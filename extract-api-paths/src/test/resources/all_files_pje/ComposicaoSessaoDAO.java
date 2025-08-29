package br.com.jt.pje.dao;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.query.ComposicaoSessaoQuery;
import br.jus.pje.jt.entidades.ComposicaoSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ComposicaoSessaoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ComposicaoSessaoDAO extends GenericDAO implements ComposicaoSessaoQuery, Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "composicaoSessaoDAO";

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorBySessao(SessaoJT sessao){
		Query q = getEntityManager().createQuery(ORGAO_JULGADOR_BY_SESSAO_QUERY);
		q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
		List<OrgaoJulgador> resultList = q.getResultList();
		return resultList;
	}

	public ComposicaoSessao getComposicaoSessao(SessaoJT sessao, OrgaoJulgador orgaoJulgador){
		Query q = getEntityManager().createQuery(COMPOSICAO_SESSAO_QUERY);
		q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
		ComposicaoSessao result = EntityUtil.getSingleResult(q);
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<ComposicaoSessao> composicaoSessaoListBySessao(SessaoJT sessao){
		Query q = getEntityManager().createQuery(COMPOSICAO_SESSAO_LIST_BY_SESSAO_QUERY);
		q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
		List<ComposicaoSessao> resultList = q.getResultList();
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<ComposicaoSessao> composicaoSessaoSemComposicaoProcessoBySessaoProcesso(SessaoJT sessao, ProcessoTrf processoTrf){
		Query q = getEntityManager().createQuery(COMPOSICAO_SESSAO_SEM_COMPOSICAO_PROCESSO_BY_SESSAO_PROCESSO_QUERY);
		q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
		q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
		List<ComposicaoSessao> resultList = q.getResultList();
		return resultList;
	}

}