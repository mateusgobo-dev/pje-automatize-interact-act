package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ComplementoClasseProcessoTrf;

public abstract class AbstractComplementoClasseProcessoTrfHome<T> extends AbstractHome<ComplementoClasseProcessoTrf> {

	private static final long serialVersionUID = 1L;

	public void setComplementoClasseProcessoTrfIdComplementoClasseProcessoTrf(Integer id) {
		setId(id);
	}

	public Integer getComplementoClasseProcessoTrfIdComplementoClasseProcessoTrf() {
		return (Integer) getId();
	}

	@Override
	protected ComplementoClasseProcessoTrf createInstance() {
		ComplementoClasseProcessoTrf complementoClasseProcessoTrf = new ComplementoClasseProcessoTrf();
		ComplementoClasseHome complementoClasseHome = (ComplementoClasseHome) Component.getInstance(
				"complementoClasseHome", false);
		if (complementoClasseHome != null) {
			complementoClasseProcessoTrf.setComplementoClasse(complementoClasseHome.getDefinedInstance());
		}
		ProcessoTrfHome processoTrfHome = (ProcessoTrfHome) Component.getInstance("processoTrfHome", false);
		if (processoTrfHome != null) {
			complementoClasseProcessoTrf.setProcessoTrf(processoTrfHome.getDefinedInstance());
		}
		return complementoClasseProcessoTrf;
	}

	@Override
	public String remove() {
		ComplementoClasseHome complementoClasse = (ComplementoClasseHome) Component.getInstance(
				"complementoClasseHome", false);
		if (complementoClasse != null) {
			complementoClasse.getInstance().getComplementoClasseProcessoTrfList().remove(instance);
		}
		ProcessoTrfHome processoTrf = (ProcessoTrfHome) Component.getInstance("processoTrfHome", false);
		if (processoTrf != null) {
			processoTrf.getInstance().getComplementoClasseProcessoTrfList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(ComplementoClasseProcessoTrf obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("complementoClasseProcessoTrfGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getComplementoClasse() != null) {
			List<ComplementoClasseProcessoTrf> complementoClasseList = getInstance().getComplementoClasse()
					.getComplementoClasseProcessoTrfList();
			if (!complementoClasseList.contains(instance)) {
				getEntityManager().refresh(getInstance().getComplementoClasse());
			}
		}
		if (getInstance().getProcessoTrf() != null) {
			List<ComplementoClasseProcessoTrf> ptrocessoTrfList = getInstance().getProcessoTrf()
					.getComplementoClasseProcessoTrfList();
			if (!ptrocessoTrfList.contains(instance)) {
				getEntityManager().refresh(getInstance().getProcessoTrf());
			}
		}
		newInstance();
		return action;
	}

}