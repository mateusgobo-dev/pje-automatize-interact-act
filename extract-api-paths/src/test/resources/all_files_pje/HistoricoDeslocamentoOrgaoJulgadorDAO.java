/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.manager.EntityManagerUtil;
import br.jus.pje.jt.entidades.HistoricoDeslocamentoOrgaoJulgador;

/**
 * PJEII-3236
 * @author Frederico Carneiro
 *
 */
@Name("historicoDeslocamentoOrgaoJulgadorDAO")
public class HistoricoDeslocamentoOrgaoJulgadorDAO extends BaseDAO<HistoricoDeslocamentoOrgaoJulgador>{

	@Override
	public Long getId(HistoricoDeslocamentoOrgaoJulgador e){
		return e.getIdHistoricoDeslocamentoOrgaoJulgador();
	}
	
	
	/**
	 * @author José Borges - jose.borges@tst.jus.br
	 * @since 1.4.5
	 * @category PJE-JT
	 * @return retona lista de históricos encontrados de acordo com o número do processo só leva em consideração o número do processo
	 */
	@SuppressWarnings("unchecked")
	public List<HistoricoDeslocamentoOrgaoJulgador> obterListaHistorico(int idProcessoTrf)
	{
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("SELECT o FROM HistoricoDeslocamentoOrgaoJulgador AS o " );
		queryStr.append("WHERE o.processoTrf.idProcessoTrf = :IDPROCESSOTRF" );
		
		Query q = getEntityManager().createQuery(queryStr.toString());
		q.setParameter("IDPROCESSOTRF", idProcessoTrf);
		
		return (List<HistoricoDeslocamentoOrgaoJulgador>) q.getResultList();
	}
	
	
	/**
	 * @author José Borges - jose.borges@tst.jus.br
	 * @since 1.4.5
	 * @category PJE-JT
	 * @return retorna histórico quando a data de retorno não estiver preenchida E a data de deslocamento estiver definida
	 */
	public HistoricoDeslocamentoOrgaoJulgador obterHistoricoSemDataRetornoDefinida(int idProcessoTrf) throws PJeException
	{
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("SELECT o FROM HistoricoDeslocamentoOrgaoJulgador AS o " );
		queryStr.append("WHERE o.dataDeslocamento IS NOT null ");
		queryStr.append("AND o.dataRetorno IS null ");
		queryStr.append("AND o.processoTrf.idProcessoTrf = :IDPROCESSOTRF" );
		
		Query q = getEntityManager().createQuery(queryStr.toString());
		q.setParameter("IDPROCESSOTRF", idProcessoTrf);
		
		HistoricoDeslocamentoOrgaoJulgador resultado = null;
		
		try {
		 resultado = (HistoricoDeslocamentoOrgaoJulgador) q.getSingleResult();
		}
		catch(NoResultException nre)
		{
		  //nada a ser feito. O resultado já é null	
		}
		catch(NonUniqueResultException nure)
		{
		  throw new PJeException("O mesmo processo [idProcessoTrf = " + idProcessoTrf + "] está em plantão mais de uma vez.");	
		}
		
		
		return resultado;
	}
	
	
	/**
	 * @author José Borges - jose.borges@tst.jus.br
	 * @since 1.4.5
	 * @category PJE-JT
	 * @return retona histórico encontrado quando as datas de deslocamento e de retorno estiverem indefinidas
	 */
	public HistoricoDeslocamentoOrgaoJulgador obterHistoricoSemDatasDefinidas(int idProcessoTrf) throws PJeException
	{
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("SELECT o FROM HistoricoDeslocamentoOrgaoJulgador AS o " );
		queryStr.append("WHERE o.dataDeslocamento IS null ");
		queryStr.append("AND o.dataRetorno IS null ");
		queryStr.append("AND o.processoTrf.idProcessoTrf = :IDPROCESSOTRF" );
		
		Query q = getEntityManager().createQuery(queryStr.toString());
		q.setParameter("IDPROCESSOTRF", idProcessoTrf);
		
		HistoricoDeslocamentoOrgaoJulgador resultado = null;
		
		try {
			 resultado = (HistoricoDeslocamentoOrgaoJulgador) q.getSingleResult();
			}
			catch(NoResultException nre)
			{
			  //nada a ser feito. O resultado já é null	
			}
			catch(NonUniqueResultException nure)
			{
			  throw new PJeException("ERRO: O mesmo processo [idProcessoTrf = " + idProcessoTrf + "] está no fluxo mais de uma vez.");	
			}
		
		return resultado;
	}
	
	
	/**
	 * 
	 * @since 1.4.5
	 * @category PJE-JT
	 * @return retona histórico encontrado quando as datas de deslocamento e de retorno estiverem indefinidas
	 */
	@SuppressWarnings("unchecked")
	public List<HistoricoDeslocamentoOrgaoJulgador> verificaDeslocamentoOrgaoJulgadorEmAndamento(int idProcessoTrf)
	{
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("SELECT o FROM HistoricoDeslocamentoOrgaoJulgador AS o " );
		queryStr.append("WHERE o.dataDeslocamento IS NOT null AND o.dataRetorno IS null ");
		queryStr.append("AND o.processoTrf.idProcessoTrf = :IDPROCESSOTRF" );
		
		Query q = getEntityManager().createQuery(queryStr.toString());
		q.setParameter("IDPROCESSOTRF", idProcessoTrf);
		
		return (List<HistoricoDeslocamentoOrgaoJulgador>) q.getResultList();
		
	}

	public HistoricoDeslocamentoOrgaoJulgador obtemUltimoDeslocamento(int idProcessoTrf) {
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("SELECT o FROM HistoricoDeslocamentoOrgaoJulgador AS o " );
		queryStr.append("WHERE o.processoTrf.idProcessoTrf = :idP and o.dataDeslocamento = ");
		queryStr.append(" 	(select max(o2.dataDeslocamento) from HistoricoDeslocamentoOrgaoJulgador o2 ");
		queryStr.append(" 	   where o2.processoTrf.idProcessoTrf = :idP and o2.dataDeslocamento is not null) ");
		
		Query q = getEntityManager().createQuery(queryStr.toString());
		q.setParameter("idP", idProcessoTrf);
		
		return EntityUtil.getSingleResult(q);
	}
}
