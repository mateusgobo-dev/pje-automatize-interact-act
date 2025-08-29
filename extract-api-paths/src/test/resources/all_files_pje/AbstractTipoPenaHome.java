package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.TipoPena;

public class AbstractTipoPenaHome<T> extends AbstractHome<TipoPena> {

	private static final long serialVersionUID = 1L;

	public void setTipoPenaIdTipoPena(Integer id) {
		setId(id);
	}

	public Integer getTipoPenaIdTipoPena() {
		return (Integer) getId();
	}

	@Override
	protected TipoPena createInstance() {
		TipoPena tipoPena = new TipoPena();
		return tipoPena;
	}

	@Override
	public String remove(TipoPena obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		GridQuery grid = (GridQuery) Component.getInstance("tipoPenaGrid");
		grid.refresh();
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		// if (action != null) {
		// newInstance();
		// }
		return action;
	}

	@Override
	public void setTab(String tab) {
		super.setTab(tab);
		if (tab.equals("search") && isManaged()) {
			EntityUtil.getEntityManager().refresh(getInstance());
		}
	}

}
