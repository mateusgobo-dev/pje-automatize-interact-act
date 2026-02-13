package br.com.jt.pje.dao;

import br.com.infox.core.dao.GenericDAO;

import br.com.itx.util.EntityUtil;

import br.com.jt.pje.query.ComposicaoProcessoSessaoQuery;
import br.jus.pje.jt.entidades.ComposicaoProcessoSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;


@Name(ComposicaoProcessoSessaoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ComposicaoProcessoSessaoDAO extends GenericDAO
    implements ComposicaoProcessoSessaoQuery, Serializable {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "composicaoProcessoSessaoDAO";

    @SuppressWarnings("unchecked")
    public List<ComposicaoProcessoSessao> getComposicaoProcessoByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(COMPOSICAO_PROCESSO_BY_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<ComposicaoProcessoSessao> resultList = q.getResultList();

        return resultList;
    }

    public boolean existeComposicaoProcessoByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(COUNT_COMPOSICAO_PROCESSO_BY_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        Long result = EntityUtil.getSingleResult(q);

        return result > 0;
    }

    public ComposicaoProcessoSessao getComposicaoProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao, OrgaoJulgador orgaoJulgador) {
        Query q = getEntityManager()
                      .createQuery(COMPOSICAO_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);

        ComposicaoProcessoSessao result = EntityUtil.getSingleResult(q);

        return result;
    }

    @SuppressWarnings("unchecked")
    public List<ComposicaoProcessoSessao> getComposicaoProcessoByProcessoSessaoOJList(
        ProcessoTrf processoTrf, SessaoJT sessao,
        List<OrgaoJulgador> orgaoJulgadorList) {
        Query q = getEntityManager()
                      .createQuery(COMPOSICAO_PROCESSO_BY_PROCESSO_SESSAO_OJ_LIST_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_OJ_LIST, orgaoJulgadorList);

        List<ComposicaoProcessoSessao> resultList = q.getResultList();

        return resultList;
    }
}
