/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.AgrupamentoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.pje.nucleo.entidades.Agrupamento;

/**
 * Componente de controle negocial da entidade {@link Agrupamento}.
 * 
 * @author cristof
 *
 */
@Name("agrupamentoManager")
public class AgrupamentoManager extends BaseManager<Agrupamento> {
	
	@In
	private AgrupamentoDAO agrupamentoDAO;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected AgrupamentoDAO getDAO() {
		return agrupamentoDAO;
	}
	
	/**
	 * Recupera o agrupamento de tipos de movimentação por seu nome.
	 * 
	 * @param nome o nome do agrupamento.
	 * @return o agrupamento com o nome dado, ou null, se inexistente.
	 * @throws PJeBusinessException caso ocorra algum erro na recuperação
	 */
	public Agrupamento findByNome(String nome) throws PJeBusinessException{
		try {
			return agrupamentoDAO.findByNome(nome);
		} catch (PJeDAOException e) {
			throw new PJeBusinessException(e.getCode(), e, e.getParams());
		}
	}

}
