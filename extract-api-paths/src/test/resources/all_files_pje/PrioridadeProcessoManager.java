/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.PrioridadeProcessoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial da entidade {@link PrioridadeProcesso}.
 * 
 * @author cristof
 * @since 1.4.6.2.RC4
 *
 */
@Name("prioridadeProcessoManager")
public class PrioridadeProcessoManager extends BaseManager<PrioridadeProcesso> {
	
	@In
	private PrioridadeProcessoDAO prioridadeProcessoDAO;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected PrioridadeProcessoDAO getDAO() {
		return prioridadeProcessoDAO;
	}
	
	/**
	 * Recupera todas as prioridades ativas existentes na instalação.
	 * 
	 * @return a lista de prioridades ativas da instalação
	 * @throws PJeBusinessException caso haja algum erro na recuperação
	 */
	public List<PrioridadeProcesso> listActive() throws PJeBusinessException{
		Search search = new Search(PrioridadeProcesso.class);
		addCriteria(search, Criteria.equals("ativo", true));
		search.close();
		return list(search);
	}


	/**
	 * Consulta a PrioridadeProcesso pelo ID ou Descrição.
	 * 
	 * @param prioridade
	 * @return PrioridadeProcesso
	 */
	public PrioridadeProcesso obterPeloIDouDescricao(String prioridade) {
		PrioridadeProcesso resultado = null;

		if (StringUtils.isNotBlank(prioridade)) {
			if (NumberUtils.isDigits(prioridade)) {
				resultado = getDAO().findById(prioridade);
			} else {
				resultado = getDAO().findByDescricao(prioridade);
			}
		}
		return resultado;
	}
}
