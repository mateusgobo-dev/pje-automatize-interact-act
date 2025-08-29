package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.TipoNaturezaDebitoDAO;
import br.jus.pje.nucleo.entidades.TipoNaturezaDebito;

/**
 * Manager da entidade TipoNaturezaDebito.
 * 
 * @author Adriano Pamplona
 */
@Name(TipoNaturezaDebitoManager.NAME)
public class TipoNaturezaDebitoManager extends BaseManager<TipoNaturezaDebito> {

	public static final String NAME = "tipoNaturezaDebitoManager";

	@In
	private TipoNaturezaDebitoDAO tipoNaturezaDebitoDAO;

	/**
	 * @return TipoNaturezaDebitoManager
	 */
	public static TipoNaturezaDebitoManager instance() {
		return ComponentUtil.getComponent(NAME);
	}

	@Override
	protected TipoNaturezaDebitoDAO getDAO() {
		return tipoNaturezaDebitoDAO;
	}

	/**
	 * @param codigo
	 * @return TipoNaturezaDebito do código informado.
	 */
	public TipoNaturezaDebito findByCodigo(String codigo) {
		return getDAO().findByCodigo(codigo);
	}
}
