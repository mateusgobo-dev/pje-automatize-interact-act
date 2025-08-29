package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.query.ProcessoTrfConexaoQuery;
import br.jus.cnj.pje.util.QueryUtils;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(ProcessoTrfConexaoDAO.NAME)
public class ProcessoTrfConexaoDAO extends BaseDAO<ProcessoTrfConexao> implements ProcessoTrfConexaoQuery {

    public static final String NAME = "processoTrfConexaoDAO";

    @Override
    public Integer getId(ProcessoTrfConexao e) {
        return e.getIdProcessoTrfConexao();
    }

    /**
     * @param processoPrincipal
     * @param processoConexo
     * @return ProcessoTrfConexao relacionado aos processos passados como parâmetro
     */
    public ProcessoTrfConexao getProcessoTrfConexao(ProcessoTrf processoPrincipal, ProcessoTrf processoConexo) {
        Query query = getEntityManager().createQuery(PROCESSO_TRF_CONEXAO_POR_PRINCIPAL_E_CONEXO_LIST);
        query.setParameter(PROCESSO_TRF_PARAM, processoPrincipal);
        query.setParameter(PROCESSO_TRF_CONEXO_PARAM, processoConexo);

        return EntityUtil.getSingleResult(query);
    }
    
    @SuppressWarnings("unchecked")
	public List<ProcessoTrfConexao> getProcessosPreventos(Integer idProcessoTrf) {
    	EntityManager entityManager = this.getEntityManager(); 
    	Query query = entityManager.createQuery(PROCESSO_TRF_CONEXAO_LIST);
        query.setParameter(PROCESSO_TRF_ID_PARAM, idProcessoTrf);

        return query.getResultList();
    }
    
    public int getQuantidadeProcessosPreventos(Integer idProcessoTrf) {
    	EntityManager entityManager = this.getEntityManager(); 
    	Query query = entityManager.createQuery(PROCESSO_TRF_CONEXAO_COUNT);
        query.setParameter(PROCESSO_TRF_ID_PARAM, idProcessoTrf);

        Number number = (Number) query.getSingleResult();
        return number.intValue();
    }
    
    public int getQuantidadeProcessosAssociados(Integer idProcessoTrf) {
    	Query query = entityManager.createQuery(PROCESSO_TRF_CONEXAO_COUNT);
    	query.setParameter(PROCESSO_TRF_ID_PARAM, idProcessoTrf);
    	
    	Number number = (Number) EntityUtil.getSingleResult(query);
    	return number.intValue();
    }

    
    /**
     * Retorna os processos conexos ao processo paradigma que ainda estão pendentes de análise.
     * 
     * @param idProcessoTrf
     */
    @SuppressWarnings("unchecked")
    public List<ProcessoTrfConexao> getProcessosPreventosPendentesAnalise(Integer idProcessoTrf) {
        EntityManager entityManager = this.getEntityManager(); 
        Query query = entityManager.createQuery(PROCESSO_TRF_CONEXAO_LIST_PREVENCAO + " AND o.prevencao = '" + PrevencaoEnum.PE + "' ");
        query.setParameter(PROCESSO_TRF_ID_PARAM, idProcessoTrf);

        return query.getResultList();
    }
    
    /**
     * Retorna a quantidade de processos conexos ao processo judicial paradigma que ainda estão pendentes de análise.
     * 
     * @param idProcessoTrf
     */
    public int getQuantidadeProcessosPreventosPendentesAnalise(Integer idProcessoTrf) {
        EntityManager entityManager = this.getEntityManager(); 
        Query query = entityManager.createQuery(PROCESSO_TRF_CONEXAO_COUNT_PREVENCAO + " AND o.prevencao = '" + PrevencaoEnum.PE + "' ");
        query.setParameter(PROCESSO_TRF_ID_PARAM, idProcessoTrf);

        Number number = (Number) query.getSingleResult();
        return number.intValue();
    }    

    /**
     * Retorna lista de processos associados
     * 
     * @param idProcessoTrf
     * @return List<ProcessoTrfConexao>
     */
    public List<ProcessoTrfConexao> getListProcessosAssociados(Integer idProcessoTrf, Boolean somenteAtivos) {
    	StringBuilder hql = new StringBuilder();
    	hql.append("from ProcessoTrfConexao where processoTrf.idProcessoTrf = :idProcessoTrf ");
    	if (somenteAtivos) {
    		hql.append("and ativo = true ");
    	}
    	Query query = EntityUtil.createQuery(hql.toString());
    	query.setParameter("idProcessoTrf", idProcessoTrf);
    	@SuppressWarnings("unchecked")
    	List<ProcessoTrfConexao> listaProcessoTrfConexao = (List<ProcessoTrfConexao>) query.getResultList();	
    	return listaProcessoTrfConexao;
    }

    /**
     * reucpera todas as validacoes de prevencao realizadas pela pessoa passada em parametro
     * @param _pessoa
     * @return
     */
	public List<ProcessoTrfConexao> recuperaConexoesPrevencoes(Pessoa _pessoa) {
		List<ProcessoTrfConexao> resultado = null;
		Search search = new Search(ProcessoTrfConexao.class);
		try {
			search.addCriteria(Criteria.equals("pessoaFisica.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}
	
	public void removeDocumento(int idDocumento) {
		StringBuilder hql = new StringBuilder();
		hql.append("UPDATE ProcessoTrfConexao SET processoDocumento = NULL WHERE processoDocumento.idProcessoDocumento = :idDocumento");

		Query query = entityManager.createQuery(hql.toString());
		query.setParameter("idDocumento", idDocumento);
		
		query.executeUpdate();
	}
    

	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> verificaExistenciaPrevencaoPorReferencia(ProcessoTrf processoTrf, String processoReferencia) {
		EntityManager em = EntityUtil.getEntityManager();
		String sql = "select distinct pro from ProcessoTrf pro where pro.desProcReferencia = :procReferencia and pro.idProcessoTrf <> :idProcessoTrf and pro.processoStatus = :distribuido";
		Query query = em.createQuery(sql);
		query.setParameter("procReferencia", processoReferencia);
		query.setParameter("idProcessoTrf", Integer.valueOf(processoTrf.getIdProcessoTrf()));
		query.setParameter("distribuido", ProcessoStatusEnum.D);
		return query.getResultList();
	}
	
	public void insereProcessoConexao(ProcessoTrfConexao processoTrfConexao) {
		insereProcessoConexao(processoTrfConexao, Boolean.TRUE);
	}
	public void insereProcessoConexao(ProcessoTrfConexao processoTrfConexao, Boolean flagFlush) {
		EntityUtil.getEntityManager().persist(processoTrfConexao);
		if(Boolean.TRUE.equals(flagFlush)) {
			EntityUtil.getEntityManager().flush();
		}
	}
	
	public void insereProcessosConexao(List<ProcessoTrfConexao> processosTrfConexao) {
		for (ProcessoTrfConexao processoTrfConexao : processosTrfConexao) {
			insereProcessoConexao(processoTrfConexao, Boolean.FALSE);
		}
		EntityUtil.getEntityManager().flush();
	}
	
	@SuppressWarnings("unchecked")
	public void insereProcessoConexaoNativeQuery(List<ProcessoTrfConexao> processosTrfConexao) {
		Integer sizeLista = processosTrfConexao.size();
		StringBuilder sb = new StringBuilder();
		sb.append("insert ");
		sb.append("into ");
		sb.append("client.tb_processo_trf_conexao (id_processo_trf_conexao, ");
		sb.append("id_processo_trf_conexo, ");
		sb.append("id_processo_trf, ");
		sb.append("tp_tipo_conexao, ");
		sb.append("in_valida_prenvencao, ");
		sb.append("dt_possivel_prevencao, ");
		sb.append("in_ativo) ");
		sb.append("values ");
		List<Object> listaDeIdsProcessoConexao = entityManager.createNativeQuery(toSqlToGetSequence(sizeLista)).getResultList();
		for(int i = 0; i < sizeLista; i++){
			ProcessoTrfConexao processoTrfConexao = processosTrfConexao.get(i);
			processoTrfConexao.setIdProcessoTrfConexao(Integer.parseInt(listaDeIdsProcessoConexao.get(i).toString()));
			processoTrfConexao.setAtivo(Boolean.TRUE);
			
			sb.append("(");
			sb.append("" + QueryUtils.toParameterOfNativeQuery(processoTrfConexao.getIdProcessoTrfConexao())+",");
			sb.append("" + QueryUtils.toParameterOfNativeQuery(processoTrfConexao.getProcessoTrfConexo().getIdProcessoTrf())+",");
			sb.append("" + QueryUtils.toParameterOfNativeQuery(processoTrfConexao.getProcessoTrf().getIdProcessoTrf())+",");
			sb.append("" + QueryUtils.toParameterOfNativeQuery(processoTrfConexao.getTipoConexao().toString())+",");
			sb.append("" + QueryUtils.toParameterOfNativeQuery(processoTrfConexao.getPrevencao().toString())+",");
			sb.append("" + "now()" + ",");
			sb.append("" + "true");
			sb.append(")");
			if(i == (sizeLista -1)) {
				entityManager.createNativeQuery(sb.toString()).executeUpdate();						
			} else {
				sb.append(",");
			}
			
		}
		
		for (ProcessoTrfConexao processoTrfConexao : processosTrfConexao) {
			Events.instance().raiseEvent(Eventos.CONEXAO_PROCESSUAL_CRIADA, processoTrfConexao);
		}
	}

	private String toSqlToGetSequence(Integer sizeLista) {
		StringBuilder sb = new StringBuilder();
		sb.append("select nextval('sq_tb_processo_trf_conexao') from generate_series(1,");
		sb.append(sizeLista);
		sb.append(")");
		return sb.toString();
	}

	public void apagarHistoricoDaPrevencao(ProcessoTrf processoTrf) {
		String sql = "DELETE FROM ProcessoTrfConexao p WHERE p.processoTrf = :processoTrf AND p.prevencao = :prevencao AND p.tipoConexao = :tipoConexao AND p.justificativa is null";
		Query query = EntityUtil.createQuery(sql);
		query.setParameter("processoTrf", processoTrf);
		query.setParameter("prevencao", PrevencaoEnum.PE);
		query.setParameter("tipoConexao", TipoConexaoEnum.PR);
		query.executeUpdate();
	}
	
}
