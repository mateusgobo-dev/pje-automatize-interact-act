package br.com.infox.ibpm.home;

import java.util.ArrayList;
import java.util.List;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.TipoEvento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

public abstract class AbstractEventoHome<T> extends AbstractHome<Evento> {

	private static final long serialVersionUID = 1L;

	public void setEventoIdEvento(Integer id) {
		setId(id);
	}

	public Integer getEventoIdEvento() {
		return (Integer) getId();
	}

	@Override
	protected Evento createInstance() {
		Evento evento = new Evento();
		return evento;
	}

	@Override
	public String remove(Evento obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("eventoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (action != null) {
			if (getInstance().getEventoSuperior() != null) {
				List<Evento> eventoSuperiorList = getInstance().getEventoSuperior().getEventoList();
				for (Evento e : eventoSuperiorList) {
					if (e.getIdEvento() == instance.getIdEvento()) {
						getEntityManager().refresh(getInstance().getEventoSuperior());
					}
				}
			}
		} else {
			getInstance().setTipoProcessoDocumentoList(new ArrayList<TipoProcessoDocumento>(0));
		}
		return action;
	}

	public void addTipoDocumento(TipoEvento obj, String gridId) {
		if (getInstance() != null) {
			getInstance().getTipoEventoList().add(obj);
			refreshGrid(gridId);
		}
	}
}