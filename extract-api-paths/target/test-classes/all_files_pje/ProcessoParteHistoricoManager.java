/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoParteHistoricoDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteHistorico;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial da entidade {@link ProcessoParteHistorico}.
 * 
 * @author cristof
 *
 */
@Name("processoParteHistoricoManager")
public class ProcessoParteHistoricoManager extends BaseManager<ProcessoParteHistorico> {
	
	@In
	private ProcessoParteHistoricoDAO processoParteHistoricoDAO;

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected ProcessoParteHistoricoDAO getDAO() {
		return processoParteHistoricoDAO;
	}
	
	/**
	 * Recupera o registro mais recente de modificação de estado da parte em um processo.
	 * 
	 * @param parte a parte a ser pesquisada
	 * @return o registro mais recente, ou nulo se a parte nunca teve sua situação modificada.
	 */
	public ProcessoParteHistorico recuperaRegistroRecente(ProcessoParte parte){
		Search s = new Search(ProcessoParteHistorico.class);
		addCriteria(s, 
				Criteria.equals("processoParte", parte));
		s.addOrder("o.dataHistorico", Order.DESC);
		s.setMax(1);
		List<ProcessoParteHistorico> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}

	/**
	 * recupera todos os processosParteHistoricos da pessoa passada em parametro.
	 * @param _pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<ProcessoParteHistorico> recuperaProcessosParteHistoricos(Pessoa _pessoa) throws Exception {
		return processoParteHistoricoDAO.recuperaProcessosParteHistoricos(_pessoa);
	}

}
