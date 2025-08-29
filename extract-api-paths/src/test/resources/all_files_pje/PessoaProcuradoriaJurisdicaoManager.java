/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PessoaProcuradoriaJurisdicaoDAO;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaJurisdicao;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name(PessoaProcuradoriaJurisdicaoManager.NAME)
@Scope(ScopeType.EVENT)
public class PessoaProcuradoriaJurisdicaoManager extends BaseManager<PessoaProcuradoriaJurisdicao> {
	
	@Logger
	private Log log;	
	
	@In
	private PessoaProcuradoriaJurisdicaoDAO pessoaProcuradoriaJurisdicaoDAO;
	
	@In
	private PessoaProcuradoriaManager pessoaProcuradoriaManager;
	
	public static final String  NAME = "pessoaProcuradoriaJurisdicaoManager";
	
	
	@Override
	protected BaseDAO<PessoaProcuradoriaJurisdicao> getDAO() {
		return pessoaProcuradoriaJurisdicaoDAO;
	}
	
	public PessoaProcuradoriaJurisdicao persist(PessoaProcuradoriaJurisdicao ppj) {
		ppj = pessoaProcuradoriaJurisdicaoDAO.persist(ppj);
		pessoaProcuradoriaJurisdicaoDAO.flush();
		return ppj;
	}
	
	public void persistList(List<PessoaProcuradoriaJurisdicao> list) {
		for(PessoaProcuradoriaJurisdicao ppj : list)
			persist(ppj);
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
		
		return pessoaProcuradoriaJurisdicaoDAO.getJurisdicoesAtivas(
				idProcuradoria, idProcurador);
	}

	/**
	 * @see #getJurisdicoesAtivas(Integer, Integer) 
	 */
	public List<Jurisdicao> getJurisdicoesAtivas(Procuradoria procuradoria,
			PessoaProcurador procurador) {
		
		return getJurisdicoesAtivas(procuradoria.getIdProcuradoria(),
				procurador.getIdUsuario());
	}
	
	
	public PessoaProcuradoriaJurisdicao getAtivoByProcuradorProcuradoriaJurisdicao(
			Integer idProcurador, Integer idProcuradoria, Integer idJurisdicao) {

		return pessoaProcuradoriaJurisdicaoDAO
				.getAtivoByProcuradorProcuradoriaJurisdicao(idProcurador,
						idProcuradoria, idJurisdicao);
	}
	
	public Boolean isProcuradorDistribuidor(Integer idProcurador,
			Integer idProcuradoria, Integer idJurisdicao) {
		
		PessoaProcuradoriaJurisdicao procuradorProcuradoriaJurisdicao = getAtivoByProcuradorProcuradoriaJurisdicao(
				idProcurador, idProcuradoria, idJurisdicao);
		
		if (procuradorProcuradoriaJurisdicao != null) {
			return true;
		}
		
		return false;
		
	}

	/**
	 * Retorna as jurições habilitadas para um procurador/procuradoria.
	 * 
	 * @param pessoaProcuradoria
	 *            Id da pessoaProcuradoria
	 * 
	 * @return Serão retornadas as juridições ativas em que o procurador possui
	 *         cadastro ativo em PessoaProcuradoriaJurisdicao.
	 * 
	 */
	public List<Jurisdicao> getPessoaProcuradoriaJurisdicoesAtivas(PessoaProcuradoria pessoaProcuradoria) {
		Search s = new Search(PessoaProcuradoriaJurisdicao.class);
		s.setDistinct(true);
		s.setRetrieveField("jurisdicao");
        
		if (pessoaProcuradoria != null){
			try {
				s.addCriteria(Criteria.equals("ativo", true));
				s.addCriteria(Criteria.equals("pessoaProcuradoria", pessoaProcuradoria));
				s.addCriteria(Criteria.equals("jurisdicao.ativo", true));
				s.addOrder("o.jurisdicao.jurisdicao", Order.ASC);
				
				return list(s);
				
			} catch (Exception e) {
				log.error("Erro ao pesquisar lista de jurisdições.", e);
				e.printStackTrace();
				return null;
				
			}
		}else{
			return new ArrayList<Jurisdicao>();
		}
	}

	public List<PessoaProcuradoriaJurisdicao> getPessoaProcuradoriaJurisdicoes(PessoaProcuradoria pessoaProcuradoria) {
		Search s = new Search(PessoaProcuradoriaJurisdicao.class);
        
		if (pessoaProcuradoria != null){
			try {
				s.addCriteria(Criteria.equals("ativo", true));
				s.addCriteria(Criteria.equals("pessoaProcuradoria", pessoaProcuradoria));
				s.addCriteria(Criteria.equals("jurisdicao.ativo", true));
				s.addOrder("o.jurisdicao.jurisdicao", Order.ASC);
				
				return list(s);
				
			} catch (Exception e) {
				log.error("Erro ao pesquisar lista de pessoasProcuradoriaJurisdições.", e);
				e.printStackTrace();
				return null;
				
			}
		}else{
			return new ArrayList<PessoaProcuradoriaJurisdicao>();
		}
	}
		
}
