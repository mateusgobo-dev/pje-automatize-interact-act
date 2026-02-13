package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.infox.pje.manager.TipoParteConfigClJudicialManager;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;

public abstract class AbstractTipoParteClasseJudicialHome<T> extends AbstractHome<TipoParteConfigClJudicial> {

	private static final long serialVersionUID = 1L;

	public void setTipoParteClasseJudicialIdTipoParteClasseJudicial(Integer id) {
		setId(id);
	}

	public Integer getTipoParteClasseJudicialIdTipoParteClasseJudicial() {
		return (Integer) getId();
	}

	@Override
	protected TipoParteConfigClJudicial createInstance() {
		TipoParteConfigClJudicial tipoParteConfigClJudicial = new TipoParteConfigClJudicial();
		ClasseJudicialHome classeJudicialHome = (ClasseJudicialHome) Component.getInstance("classeJudicialHome", false);
		if (classeJudicialHome != null) {
			tipoParteConfigClJudicial.setClasseJudicial(classeJudicialHome.getDefinedInstance());
		}
		TipoParteHome tipoParteHome = (TipoParteHome) Component.getInstance("tipoParteHome", false);
		return tipoParteConfigClJudicial;
	}

	@Override
	public String remove() {
		ClasseJudicialHome classeJudicial = (ClasseJudicialHome) Component.getInstance("classeJudicialHome", false);
		TipoParteConfigClJudicialManager tipoParteConfigClJudicialManager = ComponentUtil.getComponent(TipoParteConfigClJudicialManager.NAME); 
		if (classeJudicial != null) {
			classeJudicial.getInstance().getTipoParteConfigClJudicial().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(TipoParteConfigClJudicial obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("tipoParteClasseJudicialGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		return action;
	}
}