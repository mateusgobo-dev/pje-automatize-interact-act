package br.com.jt.pje.dao;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.dao.GenericDAO;

import br.com.itx.util.EntityUtil;

import br.com.jt.pje.query.VotoQuery;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Query;


@Name(VotoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class VotoDAO extends GenericDAO implements VotoQuery, Serializable {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "votoDAO";

    @SuppressWarnings("unchecked")
    public List<Voto> getVotosByProcessoSessao(ProcessoTrf processoTrf,
        SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(VOTOS_BY_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<Voto> list = q.getResultList();

        return list;
    }

    @SuppressWarnings("unchecked")
    public List<Voto> getVotosComposicaoProcessoByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(VOTOS_COMPOSICAO_PROCESSO_BY_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR,
            processoTrf.getOrgaoJulgador());

        List<Voto> list = q.getResultList();

        return list;
    }

    @SuppressWarnings("unchecked")
    public List<Voto> getVotosByProcesso(ProcessoTrf processoTrf) {
        Query q = getEntityManager().createQuery(VOTOS_BY_PROCESSO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);

        List<Voto> list = q.getResultList();

        return list;
    }

    @SuppressWarnings("unchecked")
    public List<OrgaoJulgador> getOrgaoJulgadorComVotoLiberado(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(ORGAO_JULGADOR_COM_VOTO_LIBERADO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<OrgaoJulgador> list = q.getResultList();

        return list;
    }

    @SuppressWarnings("unchecked")
    public List<Voto> getVotosProcessoSemSessaoByOrgaoJugador(SessaoJT sessao,
        ProcessoTrf processoTrf) {
        Query q = getEntityManager()
                      .createQuery(VOTOS_PROCESSO_SEM_SESSAO_BY_ORGAO_JULGADOR_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<Voto> list = q.getResultList();

        return list;
    }

    public Voto getVotoProcessoSemSessaoByOrgaoJulgador(
        ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
        Query q = getEntityManager()
                      .createQuery(VOTO_PROCESSO_SEM_SESSAO_BY_ORGAO_JULGADOR_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);

        Voto voto = EntityUtil.getSingleResult(q);

        return voto;
    }

    public Voto getVotoProcessoByOrgaoJulgadorSessao(ProcessoTrf processoTrf,
        OrgaoJulgador orgaoJulgador, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(VOTO_PROCESSO_BY_ORGAO_JULGADOR_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        Voto voto = EntityUtil.getSingleResult(q);

        return voto;
    }

    public Voto getUltimoVotoMagistradoByProcessoOrgaoJulgador(
        ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
        Query q = getEntityManager()
                      .createQuery(ULTIMO_VOTO_BY_PROCESSO_E_ORGAO_JULGADOR_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);

        Voto voto = EntityUtil.getSingleResult(q);

        return voto;
    }

    public Voto getUltimoVotoByOrgaoJulgadorProcessoSessao(
        OrgaoJulgador orgaoJulgador, ProcessoTrf processoTrf, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(ULTIMO_VOTO_BY_ORGAO_JULGADOR_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        Voto voto = EntityUtil.getSingleResult(q);

        return voto;
    }

    public Long quantidadeVotosAcompanhamRelatorByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(QUANTIDATE_VOTOS_ACOMPANHAM_RELATOR_BY_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_TIPO_VOTO_ACOMPANHA_RELATOR,
            ParametroUtil.instance().getTipoVotoAcompanhaRelator());

        Long result = EntityUtil.getSingleResult(q);

        return result;
    }

    public Long quantidadeVotosDivergentesByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(QUANTIDATE_VOTOS_DIVERGENTES_BY_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_TIPO_VOTO_DIVERGE_EM_PARTE,
            ParametroUtil.instance().getTipoVotoDivergeEmParte());
        q.setParameter(QUERY_PARAMETER_TIPO_VOTO_DIVERGERGENTE,
            ParametroUtil.instance().getTipoVotoDivergente());

        Long result = EntityUtil.getSingleResult(q);

        return result;
    }

    public Long quantidadeVotosSemConclusaoByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(QUANTIDATE_VOTOS_SEM_CONCLUSAO_BY_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        Long result = EntityUtil.getSingleResult(q);

        return result;
    }

    public Long quantidadeVotosNaoConhecidosByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(QUANTIDATE_VOTOS_NAO_CONHECE_BY_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_TIPO_VOTO_NAO_CONHECE,
            ParametroUtil.instance().getTipoVotoNaoConhece());

        Long result = EntityUtil.getSingleResult(q);

        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Voto> getVotosNaoLiberadosByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(VOTOS_NAO_LIBERADOS_BY_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<Voto> list = q.getResultList();

        return list;
    }

    public boolean existeVotoComDivergencia(ProcessoTrf processoTrf,
        SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(EXISTE_VOTO_COM_DIVERGENCIA_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        boolean result = EntityUtil.getSingleResult(q) != null;

        return result;
    }

    public boolean existeVotoComDestaque(ProcessoTrf processoTrf,
        SessaoJT sessao) {
        Query q = getEntityManager().createQuery(EXISTE_VOTO_COM_DESTAQUE_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        boolean result = EntityUtil.getSingleResult(q) != null;

        return result;
    }

    public boolean existeVotoComObservacao(ProcessoTrf processoTrf,
        SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(EXISTE_VOTO_COM_OBSERVACAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO_TRF, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        boolean result = EntityUtil.getSingleResult(q) != null;

        return result;
    }
}
