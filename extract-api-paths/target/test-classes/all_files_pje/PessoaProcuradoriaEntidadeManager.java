package br.com.infox.pje.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.PessoaProcuradoriaEntidadeDAO;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Classe com métodos referentes a regra de negócio da entidade de
 * PessoaProcuradoriaEntidade
 * 
 * @author Joao Paulo Lacerda
 * 
 */
@Name(PessoaProcuradoriaEntidadeManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PessoaProcuradoriaEntidadeManager extends BaseManager<PessoaProcuradoriaEntidade>{


	public static final String NAME = "pessoaProcuradoriaEntidadeManager";

	@In
	private PessoaProcuradoriaEntidadeDAO pessoaProcuradoriaEntidadeDAO;

	@Deprecated
	public PessoaProcuradoriaEntidade getPessoaProcuradoriaEntidade(Pessoa pessoa) {
		return pessoaProcuradoriaEntidadeDAO.getPessoaProcuradoriaEntidade(pessoa);
	}
	
	/**
	 * [PJEII-4244] Método que carrega a lista de PessoaProcuradoriaEntidade de determinada pessoa.
	 * @param pessoa
	 * @return
	 */
	public List<PessoaProcuradoriaEntidade> getListaPessoaProcuradoriaEntidade(Pessoa pessoa) {
		return pessoaProcuradoriaEntidadeDAO.getListaPessoaProcuradoriaEntidade(pessoa);
	}

	@Override
	protected PessoaProcuradoriaEntidadeDAO getDAO() {
		return pessoaProcuradoriaEntidadeDAO;
	}
	
	
	/**
	 * Recupera a procuradoria padrao de uma dada pessoa - como no banco não há a marcação, a procuradoria padrão será a mais antiga vinculação 
	 * 
	 * @param pessoa
	 * @return
	 */
	public Procuradoria getProcuradoriaPadraoPessoa (Pessoa pessoa){
		if(pessoa != null){
			List<PessoaProcuradoriaEntidade> pessoaProcuradoriaEntidadeList = this.
					getListaPessoaProcuradoriaEntidade(pessoa);
			
			if(pessoaProcuradoriaEntidadeList != null && !pessoaProcuradoriaEntidadeList.isEmpty()){
				Procuradoria procuradoria = pessoaProcuradoriaEntidadeList.get(0).getProcuradoria();
				return procuradoria;
			}
		}
		return null;
	}
	
	public List<Integer> getListaIdPessoaProcuradoriaEntidadeProcuradoria(Procuradoria procuradoria){
		Search search = new Search(PessoaProcuradoriaEntidade.class);
		search.setRetrieveField("pessoa.idPessoa");
		addCriteria(search, Criteria.equals("procuradoria", procuradoria));
		return list(search);
	}
	
	public List<Integer> getIdsPessoasRepresentadasPorProcuradoria(Integer idProcuradoria){
		return pessoaProcuradoriaEntidadeDAO.getIdsPessoasRepresentadasPorProcuradoria(idProcuradoria);
	}

}