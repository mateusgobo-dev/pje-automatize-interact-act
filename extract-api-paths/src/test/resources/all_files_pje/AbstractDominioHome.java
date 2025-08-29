/* $Id: AbstractDominioHome.java 10746 2010-08-12 23:23:46Z jplacerda $ */

package br.com.infox.ibpm.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoEvento;
import br.jus.pje.nucleo.entidades.lancadormovimento.Dominio;

public abstract class AbstractDominioHome<T> extends AbstractHome<Dominio> {

	private static final long serialVersionUID = 1L;

	public void setEventoIdEvento(Integer id) {
		setId(id);
	}

	public Integer getEventoIdEvento() {
		return (Integer) getId();
	}

	@Override
	protected Dominio createInstance() {
		Dominio evento = new Dominio();
		return evento;
	}

	public String remove(Dominio obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("eventoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		/*if (action != null) {
			if (getInstance().getEventoSuperior() != null){
				List<Evento> eventoSuperiorList = getInstance().getEventoSuperior().getEventoList();
				for (Evento e : eventoSuperiorList) {
					if(e.getIdEvento() == instance.getIdEvento()) {
						getEntityManager().refresh(getInstance().getEventoSuperior());
					}
				}
			}
		}else{
			getInstance().setTipoProcessoDocumentoList(new ArrayList<TipoProcessoDocumento>(0));
		}*/
		return action;
	}
	
	public void addTipoDocumento(TipoEvento obj, String gridId) {
		if (getInstance() != null) {
			//getInstance().getTipoEventoList().add(obj);
			refreshGrid(gridId);
		}
	}
}
