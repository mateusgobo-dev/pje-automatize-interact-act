package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

public abstract class AbstractAssuntoTrfHome<T> extends AbstractHome<AssuntoTrf> {

	private static final long serialVersionUID = 1L;

	public void setAssuntoTrfIdAssuntoTrf(Integer id) {
		setId(id);
	}

	public Integer getAssuntoTrfIdAssuntoTrf() {
		return (Integer) getId();
	}

	@Override
	protected AssuntoTrf createInstance() {
		AssuntoTrf assuntoTrf = new AssuntoTrf();
		AssuntoTrfHome assuntoTrfHome = (AssuntoTrfHome) Component.getInstance("assuntoTrfHome", false);
		if (assuntoTrfHome != null) {
			assuntoTrf.setAssuntoTrfSuperior(assuntoTrfHome.getDefinedInstance());
		}
		return assuntoTrf;
	}

	@Override
	public String remove(AssuntoTrf obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("assuntoTrfGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getAssuntoTrfSuperior() != null) {
			List<AssuntoTrf> assuntoTrfSuperiorList = getInstance().getAssuntoTrfSuperior().getAssuntoTrfList();
			if (!assuntoTrfSuperiorList.contains(instance)) {
				getEntityManager().merge(getInstance().getAssuntoTrfSuperior());
			}
		}
		// newInstance();
		return action;
	}

	public List<AssuntoTrf> getAssuntoTrfList() {
		return getInstance() == null ? null : getInstance().getAssuntoTrfList();
	}
}