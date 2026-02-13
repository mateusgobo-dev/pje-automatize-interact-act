/**
 * pje-web
 * Copyright (C) 2009-2014 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoVoto;

/**
 * Componente de acesso a dados da entidade {@link TipoVoto}
 * 
 * @author cristof
 *
 */
@Name("tipoVotoDAO")
public class TipoVotoDAO extends BaseDAO<TipoVoto> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(TipoVoto t) {
		return t.getIdTipoVoto();
	}

}
