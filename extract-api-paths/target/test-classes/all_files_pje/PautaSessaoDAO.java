package br.com.jt.pje.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.dao.GenericDAO;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.query.PautaSessaoQuery;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.enums.ClassificacaoTipoSituacaoPautaEnum;
import br.jus.pje.jt.enums.ResultadoVotacaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;


@Name(PautaSessaoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PautaSessaoDAO extends GenericDAO implements PautaSessaoQuery,
    Serializable {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "pautaSessaoDAO";

    public boolean existePautaSessao(SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(PAUTA_SESSAO_BY_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        Boolean result = EntityUtil.getSingleResult(q) != null;

        return result;
    }

    public Date getDataUltimaSessaoByProcesso(ProcessoTrf processoTrf) {
        Query q = getEntityManager()
                      .createQuery(DATA_ULTIMA_SESSAO_BY_PROCESSO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processoTrf);

        Date result = EntityUtil.getSingleResult(q);

        return result;
    }

    /**
	 * Método responsável por obter a quantidade de processos em pauta
	 * 
	 * @param sessao
	 *            sessão para se saber a quantidade de processos em pauta
	 * @return <code>Integer</code>, quantidade de processos em pauta
	 */
    public Integer quantidadeProcessosEmPauta(Object sessao) {
    	int idSessao = obterIdSessao(sessao);
    	    	
    	Query q = getEntityManager().createQuery("SELECT COUNT(ps) FROM PautaSessao AS ps WHERE ps.sessao.idSessao = :idSessao");
    	q.setParameter("idSessao", idSessao);

        Long result = EntityUtil.getSingleResult(q);

        return result.intValue();
    }

    /**
	 * Método responsável por obter o id da sessão sendo ela {@link Sessao} ou
	 * {@link SessaoJT}.
	 * 
	 * @param sessao
	 *            objeto {@link Sessao} ou {@link SessaoJT}
	 * @return <code>int</code>, id sessão
	 */
	private int obterIdSessao(Object sessao) {
		int idSessao;
		if (ParametroJtUtil.instance().justicaTrabalho()) {
    		idSessao = ((SessaoJT) sessao).getIdSessao();
		} else {
			idSessao = ((Sessao) sessao).getIdSessao();
		}
		return idSessao;
	}

    public Integer quantidadeProcessosByOrgaoJulgador(SessaoJT sessao,
        OrgaoJulgador orgaoJulgador) {
        Query q = getEntityManager()
                      .createQuery(QUANTIDADE_PROCESSOS_BY_ORGAO_JULGADOR_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);

        Long result = EntityUtil.getSingleResult(q);

        return Integer.parseInt(result.toString());
    }

    public Integer quantidadeProcessosResultadoVotacao(SessaoJT sessao,
        ResultadoVotacaoEnum resultadoVotacaoEnum) {
        Query q = getEntityManager()
                      .createQuery(QUANTIDADE_PROCESSOS_RESULTADO_VOTACAO_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_RESULTADO_VOTACAO, resultadoVotacaoEnum);

        Long result = EntityUtil.getSingleResult(q);

        return Integer.parseInt(result.toString());
    }

    @SuppressWarnings("unchecked")
    public List<PautaSessao> listaPautaSessaoBySessao(SessaoJT sessao) {
        Query q = getEntityManager().createQuery(PAUTA_SESSAO_BY_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<PautaSessao> resultList = q.getResultList();

        return resultList;
    }

    @SuppressWarnings("unchecked")
    public List<PautaSessao> getProcessosPautaSessaoInclusaoPA(SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(PROCESSOS_PAUTA_SESSAO_INCLUSAO_PA_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<PautaSessao> resultList = new ArrayList<PautaSessao>(0);
        resultList = q.getResultList();

        return resultList;
    }

    public OrgaoJulgador getOrgaoJulgadorRedatorByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(ORGAO_JULGADOR_REDATOR_BY_PROCESSO_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processoTrf);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao.getIdSessao());

        OrgaoJulgador result = EntityUtil.getSingleResult(q);

        return result;
    }

    public PautaSessao getPautaSessaoAbertaByProcesso(ProcessoTrf processoTrf) {
        Query q = getEntityManager()
                      .createQuery(GET_PAUTA_SESSAO_EM_ANDAMENTO_BY_PROCESSO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processoTrf);

        PautaSessao result = EntityUtil.getSingleResult(q);

        return result;
    }

    public Long quantidateProcessoBySessaoClassificacao(SessaoJT sessao,
        ClassificacaoTipoSituacaoPautaEnum classificacao) {
        Query q = getEntityManager()
                      .createQuery(QUANTIDADE_PROCESSOS_BY_SESSAO_CLASSIFICACAO_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_CLASSIFICACAO, classificacao);

        Long result = EntityUtil.getSingleResult(q);

        return result;
    }

    public PautaSessao getPautaProcessoApregoadoBySessao(SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(GET_PAUTA_PROCESSO_APREGOADO_BY_SESSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_TIPO_SITUACAO_PAUTA_APREGOADO,
            ParametroUtil.instance().getTipoSituacaoPautaApregoado());

        PautaSessao result = EntityUtil.getSingleResult(q);

        return result;
    }

    public boolean existeProcessoPendente(SessaoJT sessao) {
        Query q = getEntityManager().createQuery(EXISTE_PROCESSO_PENDENTE_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_TIPO_SITUACAO_PAUTA_APREGOADO,
            ParametroUtil.instance().getTipoSituacaoPautaApregoado());
        q.setParameter(QUERY_PARAMETER_TIPO_SITUACAO_PAUTA_PENDENTE,
            ParametroUtil.instance().getTipoSituacaoPautaPendente());

        Long result = EntityUtil.getSingleResult(q);

        return result > 0;
    }

    public boolean existeProcessoJulgadoSemConclusao(SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(EXISTE_PROCESSO_JULGADO_SEM_CONCLUSAO_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_TIPO_SITUACAO_PAUTA_JULGADO,
            ParametroUtil.instance().getTipoSituacaoPautaJulgado());

        Long result = EntityUtil.getSingleResult(q);

        return result > 0;
    }

    @SuppressWarnings("unchecked")
    public List<PautaSessao> getProcessosRetiradoPautaOuDeliberado(
        SessaoJT sessao) {
        Query q = getEntityManager()
                      .createQuery(PROCESSOS_RETIRADO_PAUTA_OU_DELIBERADO_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<PautaSessao> resultList = q.getResultList();

        return resultList;
    }

    @SuppressWarnings("unchecked")
    public List<PautaSessao> getProcessosJulgados(SessaoJT sessao) {
        Query q = getEntityManager().createQuery(PROCESSOS_JULGADOS_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);

        List<PautaSessao> resultList = q.getResultList();

        return resultList;
    }

    // TODO remover daqui
    public boolean existeDocumentoAcordao(ProcessoTrf processoTrf,
        SessaoJT sessao) {
        Query q = getEntityManager().createQuery(EXISTE_DOCUMENTO_ACORDAO_QUERY);
        q.setParameter(QUERY_PARAMETER_SESSAO, sessao);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processoTrf);
        q.setParameter(QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO,
            ParametroUtil.instance().getTipoProcessoDocumentoAcordao());

        Long result = EntityUtil.getSingleResult(q);

        return result > 0;
    }

    public PautaSessao getUltimaPautaByProcesso(ProcessoTrf processoTrf) {
        Query q = getEntityManager()
                      .createQuery(GET_ULTIMA_PAUTA_BY_PROCESSO_QUERY);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processoTrf);

        PautaSessao result = EntityUtil.getSingleResult(q);

        return result;
    }
    
    
    public List<PautaSessao> recuperarProcessosPorSessao(SessaoJT sessao) {

        StringBuilder sb = new StringBuilder("select o from PautaSessao o ");
    	sb.append(" where o.sessao.idSessao = :idSessao ");
    	sb.append(" and o.processoTrf.orgaoJulgadorColegiado = :orgaoJulgadorColegiadoAtual ");
    	if(Authenticator.getOrgaoJulgadorAtual() != null){
    		sb.append(" and o.processoTrf.orgaoJulgador = :orgaoJulgadorAtual ");
    	}
    	Query q = getEntityManager().createQuery(sb.toString());
    	q.setParameter("idSessao",sessao.getIdSessao());
    	q.setParameter("orgaoJulgadorColegiadoAtual", Authenticator.getOrgaoJulgadorColegiadoAtual());
    	if(Authenticator.getOrgaoJulgadorAtual() != null){
    		q.setParameter("orgaoJulgadorAtual", Authenticator.getOrgaoJulgadorAtual());
    	}
        
        
    	List<PautaSessao> result = q.getResultList();

        return result;
    }
    
    
    
    
}
