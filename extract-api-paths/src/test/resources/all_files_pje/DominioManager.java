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

import br.jus.cnj.pje.business.dao.DominioDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.pje.nucleo.entidades.lancadormovimento.Dominio;

/**
 * Componente de tratamento negocial da entidade {@link Dominio}.
 * 
 * @author cristof
 *
 */
@Name("dominioManager")
public class DominioManager extends BaseManager<Dominio> {
	
	@In
	private DominioDAO dominioDAO;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected DominioDAO getDAO() {
		return dominioDAO;
	}
	
	/**
	 * Recupera um domínio por seu código identificador.
	 * 
	 * @param codigo o código identificador do domínio
	 * @return o domínio que tem o código, ou null se inexistir um domínio com o código indicado.
	 * @throws PJeBusinessException caso haja algum erro ao tentar recuperar o domínio com o código dado.
	 */
	public Dominio findByCodigo(String codigo) throws PJeBusinessException{
		try{
			return dominioDAO.findByCodigo(codigo);
		}catch (PJeDAOException e){
			throw new PJeBusinessException(e.getCode(),e, e.getParams());
		}
	}

}
