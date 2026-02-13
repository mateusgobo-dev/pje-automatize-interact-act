package br.jus.csjt.pje.persistence.dao;

import java.util.Map;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

/**
 * Classe List para Tipo de Documento
 * 
 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
 * 
 * @category PJE-JT
 * @since 1.2.0
 * @created 10/08/2011
 */
public class TipoProcessoDocumentoList extends EntityList<TipoProcessoDocumento> {

	private static final long serialVersionUID = 1L;

	/**
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 10/08/2011
	 * @see br.com.infox.DAO.EntityList#addSearchFields()
	 */
	@Override
	protected void addSearchFields() {
	}

	/**
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @return
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 10/08/2011
	 * @see br.com.infox.DAO.EntityList#getDefaultEjbql()
	 */
	@Override
	protected String getDefaultEjbql() {
		return "select o from TipoProcessoDocumento o";
	}

	/**
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @return
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 10/08/2011
	 * @see br.com.infox.DAO.EntityList#getDefaultOrder()
	 */
	@Override
	protected String getDefaultOrder() {
		return "codigoDocumento";
	}

	/**
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @return
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 10/08/2011
	 * @see br.com.infox.DAO.EntityList#getCustomColumnsOrder()
	 */
	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	public TipoProcessoDocumento getByCod(String cod) {
		return (TipoProcessoDocumento) getEntityManager()
				.createQuery(getDefaultEjbql() + " where o.codigoDocumento = :cod").setParameter("cod", cod)
				.getSingleResult();
	}

}
