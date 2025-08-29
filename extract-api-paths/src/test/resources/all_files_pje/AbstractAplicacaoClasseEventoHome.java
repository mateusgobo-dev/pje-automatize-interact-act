package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.infox.ibpm.home.EventoHome;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.AplicacaoClasseEvento;

public abstract class AbstractAplicacaoClasseEventoHome<T> extends AbstractHome<AplicacaoClasseEvento> {

	private static final long serialVersionUID = 1L;

	public void setAplicacaoClasseEventoIdAplicacaoClasseEvento(Integer id) {
		setId(id);
	}

	public Integer getAplicacaoClasseEventoIdAplicacaoClasseEvento() {
		return (Integer) getId();
	}

	@Override
	protected AplicacaoClasseEvento createInstance() {
		AplicacaoClasseEvento aplicacaoClasseEvento = new AplicacaoClasseEvento();
		EventoHome eventoHome = (EventoHome) Component.getInstance("eventoHome", false);
		if (eventoHome != null) {
			aplicacaoClasseEvento.setEvento(eventoHome.getDefinedInstance());
		}
		AplicacaoClasseHome aplicacaoClasseHome = (AplicacaoClasseHome) Component.getInstance("aplicacaoClasseHome",
				false);
		if (aplicacaoClasseHome != null) {
			aplicacaoClasseEvento.setAplicacaoClasse(aplicacaoClasseHome.getDefinedInstance());
		}
		return aplicacaoClasseEvento;
	}

	@Override
	public String remove() {
		AplicacaoClasseHome aplicacaoClasse = (AplicacaoClasseHome) Component.getInstance("aplicacaoClasseHome", false);
		if (aplicacaoClasse != null) {
			aplicacaoClasse.getInstance().getAplicacaoClasseEventoList().remove(instance);
		}
		EventoHome evento = (EventoHome) Component.getInstance("eventoHome", false);
		if (evento != null) {
			evento.getInstance().getAplicacaoClasseEventoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(AplicacaoClasseEvento obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("aplicacaoClasseEventoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getEvento() != null) {
			List<AplicacaoClasseEvento> eventoList = getInstance().getEvento().getAplicacaoClasseEventoList();
			if (!eventoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getEvento());
			}
		}
		if (getInstance().getAplicacaoClasse() != null) {
			List<AplicacaoClasseEvento> aplicacaoClasseList = getInstance().getAplicacaoClasse()
					.getAplicacaoClasseEventoList();
			if (!aplicacaoClasseList.contains(instance)) {
				getEntityManager().refresh(getInstance().getAplicacaoClasse());
			}
		}
		newInstance();
		return action;
	}
}