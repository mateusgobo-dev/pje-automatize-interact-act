/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.AgrupamentoPessoasDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.AgrupamentoPessoas;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de gerenciamento negocial da entidade {@link AgrupamentoPessoas}.
 * 
 * @author thiago.vieira
 *
 */
@Name("agrupamentoPessoasManager")
public class AgrupamentoPessoasManager extends BaseManager<AgrupamentoPessoas> {
	
	@In
	private AgrupamentoPessoasDAO agrupamentoPessoasDAO;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected AgrupamentoPessoasDAO getDAO() {
		return agrupamentoPessoasDAO;
	}
	
	/**
	 * Recupera a lista de todas as instâncias de fluxo ativas para um dado processo judicial.
	 * 
	 * @param pj o processo judicial cujas instâncias de fluxo ativas se pretende recuperar
	 * @return a lista de instâncias de fluxo ativas para o processo judicial dado
	 */
	public Set<Pessoa> recuperaPessoasPorCodigo(String codAgrupamento) throws PJeBusinessException{
		Search s = new Search(AgrupamentoPessoas.class);
		s.setRetrieveField("pessoas");
		addCriteria(s, 
				Criteria.equals("codAgrupamento", codAgrupamento),
				Criteria.equals("ativo", true));
		List<Pessoa> ret = list(s);
		return new HashSet<Pessoa>(ret);
	}
}
