package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.JurisdicaoMunicipio;

public abstract class AbstractJurisdicaoMunicipioHome<T> extends AbstractHome<JurisdicaoMunicipio> {

	private static final long serialVersionUID = 1L;

	public void setJurisdicaoMunicipioIdJurisdicaoMunicipio(Integer id) {
		setId(id);
	}

	public Integer getJurisdicaoMunicipioIdJurisdicaoMunicipio() {
		return (Integer) getId();
	}

	@Override
	protected JurisdicaoMunicipio createInstance() {
		JurisdicaoMunicipio jurisdicaoMunicipio = new JurisdicaoMunicipio();
		JurisdicaoHome jurisdicaoHome = (JurisdicaoHome) Component.getInstance("jurisdicaoHome", false);
		if (jurisdicaoHome != null) {
			jurisdicaoMunicipio.setJurisdicao(jurisdicaoHome.getDefinedInstance());
		}

		return jurisdicaoMunicipio;
	}

	@Override
	public String remove() {
		JurisdicaoHome jurisdicaoHome = (JurisdicaoHome) Component.getInstance("jurisdicaoHome", false);
		if (jurisdicaoHome != null) {
			jurisdicaoHome.getInstance().getMunicipioList().remove(instance);
		}

		return super.remove();
	}

	@Override
	public String remove(JurisdicaoMunicipio obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("jurisdicaoMunicipioGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getJurisdicao() != null) {
			List<JurisdicaoMunicipio> jurisdicaoList = getInstance().getJurisdicao().getMunicipioList();
			if (!jurisdicaoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getJurisdicao());
			}
		}
		refreshGrid("jurisdicaoMunicipioGrid");
		newInstance();
		return action;
	}

	@Override
	public String update() {
		refreshGrid("jurisdicaoMunicipioGrid");
		return super.update();
	}

}