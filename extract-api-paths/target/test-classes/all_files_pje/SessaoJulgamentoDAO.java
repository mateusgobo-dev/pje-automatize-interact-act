/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.min.SessaoJulgamentoMin;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;

@Name("sessaoJulgamentoDAO")
public class SessaoJulgamentoDAO extends BaseDAO<Sessao> {

	private static final String DATE_FORMAT = "yyyyMMdd";
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(Sessao e) {
		return e.getIdSessao();
	}
	
	@SuppressWarnings("unchecked")
	public List<Sessao> findSessoesPautasPendentesFechamentoAutomatico(Date dataReferencia) {
		/*
		 * PJEII-5309 A versao do hibernate do JBoss EAP 5.1 nao manipulada a subtracao
		 * de data corretamente.
		 * 
		 * Apesar da subtracao nao ser 'exata', por exemplo 02/02/2013 - 5 ficara
		 * 20130197, servira para comparacao de menor ou igual
		 */

		DateFormat dateFormat = new SimpleDateFormat(SessaoJulgamentoDAO.DATE_FORMAT);

		String dataStr = dateFormat.format(dataReferencia);

		StringBuilder builder = new StringBuilder();

		builder.append("SELECT s");
		builder.append("  FROM Sessao AS s");
		builder.append("       JOIN s.orgaoJulgadorColegiado AS ojc");
		builder.append(" WHERE ojc.fechamentoAutomatico = true");
		builder.append(
				"   AND (s.dataFechamentoPauta IS NULL OR to_char(s.dataFechamentoPauta, 'YYYYmmdd') > :dataReferencia)");
		builder.append(
				"   AND (to_char((to_number(to_char(s.dataSessao, 'YYYYmmdd'), '99999999') - ojc.prazoDisponibilizaJulgamento), '99999999') <= :dataReferencia)");

		String hql = builder.toString();

		Query q = entityManager.createQuery(hql);

		q.setParameter("dataReferencia", dataStr);

		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Integer> findIdsSessoesPautasPendentesFechamentoAutomatico(Date dataReferencia) {
		/*
		 * PJEII-5309 A versao do hibernate do JBoss EAP 5.1 nao manipulada a subtracao
		 * de data corretamente.
		 * 
		 * Apesar da subtracao nao ser 'exata', por exemplo 02/02/2013 - 5 ficara
		 * 20130197, servira para comparacao de menor ou igual
		 */

		DateFormat dateFormat = new SimpleDateFormat(SessaoJulgamentoDAO.DATE_FORMAT);

		String dataStr = dateFormat.format(dataReferencia);

		StringBuilder builder = new StringBuilder();

		builder.append("SELECT s.idSessao");
		builder.append("  FROM Sessao AS s");
		builder.append("       JOIN s.orgaoJulgadorColegiado AS ojc");
		builder.append(" WHERE ojc.fechamentoAutomatico = true");
		builder.append(
				"   AND (s.dataFechamentoPauta IS NULL OR to_char(s.dataFechamentoPauta, 'YYYYmmdd') > :dataReferencia)");
		builder.append(
				"   AND (to_char((to_number(to_char(s.dataSessao, 'YYYYmmdd'), '99999999') - ojc.prazoDisponibilizaJulgamento), '99999999') <= :dataReferencia)");

		String hql = builder.toString();

		Query q = entityManager.createQuery(hql);

		q.setParameter("dataReferencia", dataStr);

		return q.getResultList();
	}
	
	/**
	 * Recupera a lista de sessões de julgamento cuja data de fechamento de pauta 
	 * ainda não existe ou é posterior à data de referência dada.
	 * 
	 * @param dataReferencia data de referência que servirá como parâmetro de comparação
	 * @return a lista de sessões de julgamento cujas datas de fechamento não existem ou são
	 * posteriores à data de referência.
	 */
	@SuppressWarnings("unchecked")
	public List<Sessao> findSessoesPautaAberta(Date dataReferencia){
		DateFormat dateFormat = new SimpleDateFormat(SessaoJulgamentoDAO.DATE_FORMAT);
		String dataStr = dateFormat.format(dataReferencia);
		
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT s");
		builder.append("  FROM Sessao AS s");
		builder.append("       JOIN s.orgaoJulgadorColegiado AS ojc");
		builder.append(" WHERE (s.dataFechamentoPauta IS NULL AND to_char((to_number(to_char(s.dataSessao, 'YYYYmmdd'), '99999999') - ojc.prazoDisponibilizaJulgamento), '99999999') <= :dataReferencia)");
		builder.append("    OR to_char(s.dataFechamentoPauta, 'YYYYmmdd') > :dataReferencia");
		
		String hql = builder.toString();
		
		Query q = entityManager.createQuery(hql);
		q.setParameter("dataReferencia", dataStr);
		List<Sessao> list = q.getResultList();
		return list;
	}
	
	public String getProcessosSemJulgamento(int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SessaoPautaProcessoTrf o where o.sessao.idSessao = :idSessao and o.dataExclusaoProcessoTrf is null");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("idSessao", idSessao);
		Long val = (Long) query.getSingleResult();
		return String.valueOf(val);
	}
	
	public String getProcessosEmJulgamento(int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SessaoPautaProcessoTrf o where o.sessao.idSessao = :idSessao and o.situacaoJulgamento = :situacaoJulgamento and o.dataExclusaoProcessoTrf is null");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("idSessao", idSessao);
		query.setParameter("situacaoJulgamento", TipoSituacaoPautaEnum.EJ);
		Long val = (Long) query.getSingleResult();
		return String.valueOf(val);
	}

	public String getProcessosJulgados(int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SessaoPautaProcessoTrf o where o.sessao.idSessao = :idSessao and o.situacaoJulgamento = :situacaoJulgamento and o.dataExclusaoProcessoTrf is null");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("idSessao", idSessao);
		query.setParameter("situacaoJulgamento", TipoSituacaoPautaEnum.JG);
		Long val = (Long) query.getSingleResult();
		return String.valueOf(val);
	}

	public String getVista(int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SessaoPautaProcessoTrf o where o.sessao.idSessao = :idSessao and o.adiadoVista = :vista and o.dataExclusaoProcessoTrf is null");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("idSessao", idSessao);
		query.setParameter("vista", AdiadoVistaEnum.PV);
		Long val = (Long) query.getSingleResult();
		return String.valueOf(val);
	}

	public String getAdiado(int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SessaoPautaProcessoTrf o where o.sessao.idSessao = :idSessao and o.adiadoVista = :adiado and o.retiradaJulgamento = false and o.dataExclusaoProcessoTrf is null");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("idSessao", idSessao);
		query.setParameter("adiado", AdiadoVistaEnum.AD);
		Long val = (Long) query.getSingleResult();
		return String.valueOf(val);
	}

	public String getRetiradoJulgamento(int idSessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SessaoPautaProcessoTrf o where o.sessao.idSessao = :idSessao and o.adiadoVista = :adiado and o.retiradaJulgamento = true and o.dataExclusaoProcessoTrf is null");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("idSessao", idSessao);
		query.setParameter("adiado", AdiadoVistaEnum.AD);
		Long val = (Long) query.getSingleResult();
		return String.valueOf(val);
	}

	/**
	 * Consulta os processos julgados da sessão.
	 * 
	 * @param sessao Sessão.
	 * @return processos julgados.
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoPautaProcessoTrf> consultarJulgados(Sessao sessao) {
		List<SessaoPautaProcessoTrf> listaSessaoPautaProcessoTrf = new ArrayList<SessaoPautaProcessoTrf>(0);
		
		if(sessao != null && sessao.getIdSessao() != 0) {
			StringBuilder hql = new StringBuilder();
			
			hql.append("select o ");
			hql.append("from SessaoPautaProcessoTrf o "); 
			hql.append("where ");
			hql.append("	o.sessao = :sessao and "); 
			hql.append("	o.situacaoJulgamento = :situacaoJulgamento and ");
			hql.append("	o.dataExclusaoProcessoTrf is null ");
			hql.append("order by o.numeroOrdem");
	
			Query query = getEntityManager().createQuery(hql.toString());
			query.setParameter("sessao", sessao);
			query.setParameter("situacaoJulgamento", TipoSituacaoPautaEnum.JG);
			
			listaSessaoPautaProcessoTrf = query.getResultList();
		}
		
		return listaSessaoPautaProcessoTrf;
	}
	
	/**
	 * Consulta os processos retirados da sessão.
	 * 
	 * @param sessao Sessão.
	 * @return processos retirados.
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoPautaProcessoTrf> consultarRetirados(Sessao sessao) {
		StringBuilder hql = new StringBuilder();
		
		hql.append("select o ");
		hql.append("from SessaoPautaProcessoTrf o "); 
		hql.append("where ");
		hql.append("	o.sessao = :sessao and "); 
		hql.append("	o.adiadoVista = :adiadoVista and ");
		hql.append("	o.retiradaJulgamento = true and ");
		hql.append("	o.dataExclusaoProcessoTrf is null ");
		hql.append("order by o.numeroOrdem");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("sessao", sessao);
		query.setParameter("adiadoVista", AdiadoVistaEnum.AD);
		
		return query.getResultList();
	}
	
	/**
	 * Consulta os processos com pedido de vista da sessão.
	 * 
	 * @param sessao Sessão.
	 * @return processos com pedido de vista.
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoPautaProcessoTrf> consultarPedidosVista(Sessao sessao) {
		StringBuilder hql = new StringBuilder();
		
		hql.append("select o ");
		hql.append("from SessaoPautaProcessoTrf o ");
		hql.append("where ");
		hql.append("	o.sessao = :sessao and ");
		hql.append("	o.adiadoVista = :adiadoVista and ");
		hql.append("	o.dataExclusaoProcessoTrf is null ");
		hql.append("order by o.numeroOrdem");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("sessao", sessao);
		query.setParameter("adiadoVista", AdiadoVistaEnum.PV);
		
		return query.getResultList();
	}
	
	/**
	 * Consulta os processos adiados da sessão.
	 * 
	 * @param sessao Sessão.
	 * @return processos adiados.
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoPautaProcessoTrf> consultarAdiados(Sessao sessao) {
		StringBuilder hql = new StringBuilder();
		
		hql.append("select o ");
		hql.append("from SessaoPautaProcessoTrf o ");
		hql.append("where ");
		hql.append("	o.sessao = :sessao and ");
		hql.append("	o.adiadoVista = :adiadoVista and ");
		hql.append("	o.retiradaJulgamento = false and ");
		hql.append("	o.dataExclusaoProcessoTrf is null ");
		hql.append("order by o.numeroOrdem");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("sessao", sessao);
		query.setParameter("adiadoVista", AdiadoVistaEnum.AD);
		
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<SessaoJulgamentoMin> buscarSessoes(Integer idOrgaoJulgadorColegiado, CriterioPesquisa crit) {
		Query hql = getEntityManager().createQuery(montaHqlBuscaSessoes(false, idOrgaoJulgadorColegiado, crit));

		addParameters(false, hql, idOrgaoJulgadorColegiado, crit);

		return hql.getResultList();
	}

    public Long buscarQtdSessoes(Integer idOrgaoJulgadorColegiado, CriterioPesquisa crit) {
        Query hql = getEntityManager().createQuery(montaHqlBuscaSessoes(true,idOrgaoJulgadorColegiado,crit));
        addParameters(true,hql,idOrgaoJulgadorColegiado,crit);
        return (Long) hql.getResultList().get(0);
    }

    private String montaHqlBuscaSessoes(Boolean isCount, Integer idOrgaoJulgadorColegiado, CriterioPesquisa crit) {
        StringBuilder sb = new StringBuilder();
        String param = isCount ? "count(o)" : "distinct o";

        sb.append("select ");
        sb.append(param);
        sb.append(" from SessaoJulgamentoMin o ");

        if (!isCount) {
            sb.append("join fetch o.tipoSessao t ");
        }

        if (StringUtils.isNotBlank(crit.getNumeroProcesso())) {
            sb.append("inner join o.pautasJulgamento pj ");
            sb.append("inner join pj.processoJudicial p ");
        }

        sb.append("where o.dataExclusao is null ");

        if (idOrgaoJulgadorColegiado != null) {
            sb.append("and o.orgaoJulgadorColegiado.id = :orgaoJulgadorColegiadoAtual ");
        }

        if (crit != null) {
            if (StringUtils.isNotEmpty(crit.getApelidoSessao())) {
                sb.append("and lower(to_ascii(o.apelido)) like '%'|| lower(to_ascii(:apelido)) || '%' ");
            }

            if (crit.getDataSessao() != null) {
                sb.append("and ((o.dataSessao >= :dataSessao) or (o.dataSessao <= :dataSessao and o.dataFimSessao >= :dataSessao)) ");
            }

            if (crit.getIdTipoSessao() != null) {
                sb.append("and o.tipoSessao.id = :idTipoSessao ");
            }

            if (StringUtils.isNotBlank(crit.getNumeroProcesso())) {
                sb.append("and p.processo.numeroProcesso like '%' || :numeroProcesso || '%' ");
            }
        }

        if (!isCount) {
            sb.append("ORDER BY o.dataSessao asc");
        }

        return sb.toString();
    }

    private void addParameters(Boolean isCount, Query hql,Integer idOrgaoJulgadorColegiado, CriterioPesquisa crit) {
        if(idOrgaoJulgadorColegiado != null){
            hql.setParameter("orgaoJulgadorColegiadoAtual", idOrgaoJulgadorColegiado);
        }
        if(crit != null) {
            if (StringUtils.isNotEmpty(crit.getApelidoSessao())) {
                hql.setParameter("apelido", crit.getApelidoSessao());
            }
            if (crit.getDataSessao() != null) {
                hql.setParameter("dataSessao", crit.getDataSessao());
            }
            if (crit.getIdTipoSessao() != null) {
                hql.setParameter("idTipoSessao", crit.getIdTipoSessao());
            }

            if (crit.getNumeroProcesso() != null && !crit.getNumeroProcesso().isEmpty()) {
                hql.setParameter("numeroProcesso", crit.getNumeroProcesso());
            }

            if (crit.getPage() != null && !isCount) {
                hql.setFirstResult(crit.getPage());
            }
            if (crit.getMaxResults() != null && !isCount) {
                hql.setMaxResults(crit.getMaxResults());
            }
        }
    }	
}