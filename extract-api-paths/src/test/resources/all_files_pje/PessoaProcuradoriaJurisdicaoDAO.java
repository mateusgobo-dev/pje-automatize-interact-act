/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaJurisdicao;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;


@Name("pessoaProcuradoriaJurisdicaoDAO")
public class PessoaProcuradoriaJurisdicaoDAO extends
		BaseDAO<PessoaProcuradoriaJurisdicao> {

	@Logger
	private Log log;	
	
	@Override
	public Integer getId(PessoaProcuradoriaJurisdicao e) {
		return e.getIdPessoaProcuradoriaJurisdicao();
	}
	
	/**
	 * Retorna as jurições habilitadas para um procurador/procuradoria.
	 * 
	 * @param idProcuradoria
	 *            Id da procuradoria do procurador.
	 * 
	 * @param idProcurador
	 *            Id do procurador.
	 * 
	 * @return Serão retornadas as juridições ativas em que o procurador possui
	 *         cadastro ativo em PessoaProcuradoriaJurisdicao.
	 * 
	 */
	public List<Jurisdicao> getJurisdicoesAtivas(Integer idProcuradoria,
			Integer idProcurador) {
		
		Search s = new Search(PessoaProcuradoriaJurisdicao.class);
		s.setDistinct(true);
		s.setRetrieveField("jurisdicao");
		
		try {
			s.addCriteria(Criteria.equals("ativo", true));
			s.addCriteria(Criteria.equals("pessoaProcuradoria.procuradoria.idProcuradoria", idProcuradoria));					
			s.addCriteria(Criteria.equals("pessoaProcuradoria.pessoa.idUsuario", idProcurador));
			s.addCriteria(Criteria.equals("jurisdicao.ativo", true));
			s.addOrder("o.jurisdicao.jurisdicao", Order.ASC);
			
		} catch (Exception e) {
			log.error("Erro ao pesquisar lista de jurisdi��es.", e);
			e.printStackTrace();
			return null;
			
		}
		
		return list(s);
	}
	
	/**
	 * Retorna o cadastro ativo do procurador na jurisdição da procuradoria.
	 * 
	 * @param idProcurador
	 * @param idProcuradoria
	 * @param idJurisdicao
	 * @return
	 */
	public PessoaProcuradoriaJurisdicao getAtivoByProcuradorProcuradoriaJurisdicao(
			Integer idProcurador, Integer idProcuradoria, Integer idJurisdicao) {
		
		StringBuilder sbHql = new StringBuilder()
			.append("SELECT o ")
			.append("FROM PessoaProcuradoriaJurisdicao o ")
			.append("WHERE o.jurisdicao.idJurisdicao = :idJurisdicao ")
			.append("AND o.pessoaProcuradoria.pessoa.idUsuario = :idProcurador ")
			.append("AND o.pessoaProcuradoria.procuradoria.idProcuradoria = :idProcuradoria ")
			.append("AND o.ativo IS TRUE ");
		
		Query query = getEntityManager().createQuery(sbHql.toString());
		
		query.setParameter("idJurisdicao", idJurisdicao);
		query.setParameter("idProcurador", idProcurador);
		query.setParameter("idProcuradoria", idProcuradoria);
		
		try {
			
			PessoaProcuradoriaJurisdicao result = (PessoaProcuradoriaJurisdicao) query
					.getSingleResult();
			
			return result;
			
		} catch (NoResultException e) {
			return null;
		}
		
	}
	
}
