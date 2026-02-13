package br.com.jt.pje.dao;

import br.com.infox.core.dao.GenericDAO;

import br.com.itx.util.EntityUtil;

import br.com.jt.pje.query.AnotacaoVotoQuery;
import br.jus.pje.jt.entidades.AnotacaoVoto;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Query;


@Name(AnotacaoVotoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AnotacaoVotoDAO extends GenericDAO implements AnotacaoVotoQuery,
    Serializable {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "anotacaoVotoDAO";

    public AnotacaoVoto getAnotacaoVotoSemSessaoByProcessoOrgaoJulgadorEColegiado(
        ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador,
        OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
        Query q = getEntityManager()
                      .createQuery(ANOTACAO_VOTO_SEM_SESSAO_BY_PROCESSO_ORGAO_JULGADOR_E_COLEGIADO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO,
            orgaoJulgadorColegiado);

        AnotacaoVoto result = EntityUtil.getSingleResult(q);

        return result;
    }

    @SuppressWarnings("unchecked")
    public List<AnotacaoVoto> getAnotacoesSemSessaoByProcesso(
        ProcessoTrf processoTrf) {
        //[PJEII-3218] Inclusão de order by pela data
        Query q = getEntityManager()
                      .createQuery(ANOTACOES_SEM_SESSAO_BY_PROCESSO_QUERY +
                " order by o.dataInclusao desc");
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);

        List<AnotacaoVoto> resultList = q.getResultList();

        return resultList;
    }

    @SuppressWarnings("unchecked")
    public List<AnotacaoVoto> getAnotacoesBySessaoProcesso(SessaoJT sessao,
        ProcessoTrf processoTrf) {
        Query q = getEntityManager()
                      .createQuery(ANOTACOES_BY_SESSAO_PROCESSO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<AnotacaoVoto> resultList = q.getResultList();

        return resultList;
    }

    public AnotacaoVoto getAnotacaoVotoByProcessoSessaoOrgaoJulgadorEColegiado(
        ProcessoTrf processoTrf, SessaoJT sessao, OrgaoJulgador orgaoJulgador,
        OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
        Query q = getEntityManager()
                      .createQuery(ANOTACAO_VOTO_BY_PROCESSO_SESSAO_ORGAO_JULGADOR_E_COLEGIADO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO,
            orgaoJulgadorColegiado);

        AnotacaoVoto result = EntityUtil.getSingleResult(q);

        return result;
    }

    public AnotacaoVoto getAnotacaoVotoSemOJByProcessoSessaoEColegiado(
        ProcessoTrf processoTrf, SessaoJT sessao,
        OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
        Query q = getEntityManager()
                      .createQuery(ANOTACAO_VOTO_SEM_OJ_BY_PROCESSO_SESSAO_E_COLEGIADO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO,
            orgaoJulgadorColegiado);

        AnotacaoVoto result = EntityUtil.getSingleResult(q);

        return result;
    }

    @SuppressWarnings("unchecked")
    public List<AnotacaoVoto> getAnotacoesVotoByProcessoEColegiadoExcluindoSessaoAtual(
        ProcessoTrf processoTrf, OrgaoJulgadorColegiado orgaoJulgadorColegiado,
        SessaoJT sessaoAtual) {
        Query q = getEntityManager()
                      .createQuery(ANOTACOES_VOTO_BY_PROCESSO_E_ORGAO_JULGADOR_COLEGIADO_EXCLUINDO_SESSAO_ATUAL_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO,
            orgaoJulgadorColegiado);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessaoAtual);

        List<AnotacaoVoto> resultList = q.getResultList();

        return resultList;
    }

    public List<AnotacaoVoto> getAnotacoesVotoByProcessoSessaoOrgaoJulgadorEColegiadoExcluindoSessaoAtual(
        ProcessoTrf processoTrf, OrgaoJulgadorColegiado orgaoJulgadorColegiado,
        OrgaoJulgador orgaoJulgador, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(ANOTACOES_VOTO_BY_PROCESSO_E_ORGAO_JULGADOR_E_COLEGIADO_EXCLUINDO_SESSAO_ATUAL_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO,
            orgaoJulgadorColegiado);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<AnotacaoVoto> resultList = q.getResultList();

        return resultList;
    }

    public List<AnotacaoVoto> getAnotacoesVotoSemSessaoByProcessoOrgaoJulgadorEColegiado(
        ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgadorAtual,
        OrgaoJulgadorColegiado orgaoJulgadorColegiadoAtual) {
        Query q = getEntityManager()
                      .createQuery(ANOTACOES_VOTO_BY_PROCESSO_E_ORGAO_JULGADOR_E_COLEGIADO_SEM_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO,
            orgaoJulgadorColegiadoAtual);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgadorAtual);

        List<AnotacaoVoto> resultList = q.getResultList();

        return resultList;
    }

    public List<AnotacaoVoto> getAnotacaoVotoSemSessaoByProcessoEOrgaoJulgador(
        ProcessoTrf processoTrf, SessaoJT sessao, OrgaoJulgador orgaoJulgador) {
        Query q = getEntityManager()
                      .createQuery(ANOTACOES_VOTO_BY_PROCESSO_E_ORGAO_JULGADOR_SEM_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<AnotacaoVoto> resultList = q.getResultList();

        return resultList;
    }
}
