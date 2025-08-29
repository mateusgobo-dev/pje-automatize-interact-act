/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ProcuradoriaDAO;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * @author cristof
 *
 */
@Name(ProcuradoriaManager.NAME)
@Scope(ScopeType.EVENT)
public class ProcuradoriaManager extends BaseManager<Procuradoria> {
	
	@In
	private ProcuradoriaDAO procuradoriaDAO;
	
	public static final String  NAME = "procuradoriaManager";
	
	@Override
	protected BaseDAO<Procuradoria> getDAO() {
		return procuradoriaDAO;
	}
	
	public List<Pessoa> getPessoasRepresentadas(Procuradoria p){
		return procuradoriaDAO.obtemRepresentados(p);
	}
	
	public List<Procuradoria> getlistProcuradorias(){
		return procuradoriaDAO.getListProcuradorias();
	}
	
	public List<Procuradoria> getlistProcuradorias(Pessoa p){
		return getlistProcuradorias(p, TipoProcuradoriaEnum.P);
	}
	
	/**
	 * Retorna a lista de defensorias ativas
	 * @return lista de defensorias ativas
	 */
	public List<Procuradoria> getlistDefensorias(){
		Search s = new Search(Procuradoria.class);
		
		try {
			s.addCriteria(getCriteriosListDefensorias());
		} catch (Exception e) {
			return null;
		}
		return list(s);
	}
	
	/**
	 * Retorna a contagem de defensorias ativas
	 * @return quantidade de defensorias ativas
	 */
	public Integer getListDefensoriasCount(){
		Search s = new Search(Procuradoria.class);
		s.setCount(true);
		try {
			s.addCriteria(getCriteriosListDefensorias());
		} catch (Exception e) {
			return null;
		}
		return count(s).intValue();
	}	
	
	/**
	 * Recupera critérios para consulta de defensorias ativas
	 * @return Lista de critérios
	 */
	public List<Criteria> getCriteriosListDefensorias(){
		List<Criteria> criterios = new ArrayList<Criteria>();

		criterios.add(Criteria.equals("tipo", TipoProcuradoriaEnum.D));
		criterios.add(Criteria.equals("ativo", true));

		return criterios;
	}
	
	/**
	 * Recupera a lista de procuradorias ou defensorias a serem exibidas
	 * @param Pessoa representada
	 * @param tipoProcuradoria
	 * @return lista de representantes
	 */
	public List<Procuradoria> getlistProcuradorias(Pessoa p, TipoProcuradoriaEnum tipoProcuradoria){
		Search s = new Search(PessoaProcuradoriaEntidade.class);
		
		try {
			s.setRetrieveField("procuradoria");
			s.addCriteria(getCriteriosListProcuradorias(p, tipoProcuradoria));
			s.addOrder("o.procuradoria.nome", Order.ASC);
		} catch (Exception e) {
			return null;
		}
		List<Procuradoria> procuradorias = list(s);

		return procuradorias;
	}
	
	/**
	 * Recupera a procuradoria ou defensoria
	 * @param ProcessoParte representada
	 * @return representante da parte
	 */
	public Procuradoria getProcuradoria(ProcessoParte processoParte){
		return getProcuradoria(processoParte, null);
	}
	
	
	/**
	 * Recupera a procuradoria ou defensoria 
	 * @param ProcessoParte representada
	 * @param tipoProcuradoria - caso seja null, será desconsiderado como critério de busca
	 * @return representante da parte
	 */
	public Procuradoria getProcuradoria(ProcessoParte processoParte, TipoProcuradoriaEnum tipoProcuradoria){
		Search s = new Search(ProcessoParte.class);
		try {
			s.setRetrieveField("procuradoria");
			s.addCriteria(Criteria.equals("idProcessoParte", processoParte.getIdProcessoParte()));
			s.addCriteria(Criteria.equals("procuradoria.ativo", true));
			if(tipoProcuradoria != null) {
				s.addCriteria(Criteria.equals("procuradoria.tipo", tipoProcuradoria));
			}
		} catch (Exception e) {
			return null;
		}
		List<Procuradoria> procuradorias = list(s);
		if(procuradorias.size() == 1){
			return procuradorias.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Recupera a contagem de procuradorias representantes da pessoa dada
	 * @param Pessoa representada
	 * @param tipoProcuradoria
	 * @return Contagem
	 */
	public Integer getListProcuradoriasCount(Pessoa p, TipoProcuradoriaEnum tipoProcuradoria){
		Search s = new Search(PessoaProcuradoriaEntidade.class);

		try {
			s.setRetrieveField("procuradoria");
			s.addCriteria(getCriteriosListProcuradorias(p, tipoProcuradoria));
		} catch (Exception e) {
			return null;
		}
		
		return count(s).intValue();
	}	
	
	/**
	 * Recupera os critérios de consulta de procuradoria
	 * @param Pessoa representada
	 * @param tipoProcuradoria
	 * @return Critérios de consulta
	 */
	private List<Criteria> getCriteriosListProcuradorias(Pessoa p, TipoProcuradoriaEnum tipoProcuradoria){
		List<Criteria> criterios = new ArrayList<Criteria>();
		
		criterios.add(Criteria.equals("procuradoria.tipo", tipoProcuradoria));
		criterios.add(Criteria.equals("procuradoria.ativo", true));
		criterios.add(Criteria.equals("pessoa", p));
		
		return criterios;
	}
	
	public List<Procuradoria> getlistProcuradorias(TipoProcuradoriaEnum tipoProcuradoria){
		return procuradoriaDAO.getlistProcuradorias(tipoProcuradoria);
	}	
	
	public Procuradoria recuperaPorLocalizacao(Localizacao l){
		Search s = new Search(Procuradoria.class);
		
		try {
			s.addCriteria(Criteria.equals("localizacao", l));
			s.addCriteria(Criteria.equals("ativo", true));
		} catch (Exception e) {
			return null;
		}
		List<Procuradoria> ret = list(s); 
		return ret.size() > 0 ? ret.get(0) : null;
	}
	
	public Procuradoria findByPessoaJuridica(PessoaJuridica pessoaJuridica){
		Procuradoria resultado = null;
		
		if (pessoaJuridica != null && pessoaJuridica.getIdPessoaJuridica() != null) {
			resultado = procuradoriaDAO.findByPessoaJuridica(pessoaJuridica);			
		}
		return resultado;
	}
	
}