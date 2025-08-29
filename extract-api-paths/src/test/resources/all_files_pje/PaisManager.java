/**
 *  pje-web
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

import br.jus.cnj.pje.business.dao.PaisDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Pais;

/**
 * Componente de controle negocial da entidade {@link Pais}.
 * 
 * @author cristof
 *
 */
@Name("paisManager")
public class PaisManager extends BaseManager<Pais> {
	
	@In
	private PaisDAO paisDAO;
	
	@Override
	protected PaisDAO getDAO() {
		return paisDAO;
	}
	
	public Pais findByCodigo(String codigo) throws PJeBusinessException{
		return paisDAO.findByCodigo(codigo);
	}
	
	public Pais recuperaBrasil() throws PJeBusinessException {
		return paisDAO.findByCodigo("076");
	}

}
