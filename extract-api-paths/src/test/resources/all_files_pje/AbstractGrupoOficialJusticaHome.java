package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.GrupoOficialJustica;

public abstract class AbstractGrupoOficialJusticaHome<T> extends AbstractHome<GrupoOficialJustica> {

	private static final long serialVersionUID = 1L;

	public void setGrupoOficialJusticaIdGrupoOficialJustica(Integer id) {
		setId(id);
	}

	public Integer getGrupoOficialJusticaIdGrupoOficialJustica() {
		return (Integer) getId();
	}

	@Override
	public String remove(GrupoOficialJustica obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("grupoOficialJusticaGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}
}