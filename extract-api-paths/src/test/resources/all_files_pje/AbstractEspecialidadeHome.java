package br.com.infox.cliente.home;

import java.util.List;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Especialidade;

public abstract class AbstractEspecialidadeHome<T> extends AbstractHome<Especialidade> {

	private static final long serialVersionUID = 1L;

	public void setEspecialidadeIdEspecialidade(Integer id) {
		setId(id);
	}

	public Integer getEspecialidadeIdEspecialidade() {
		return (Integer) getId();
	}

	@Override
	protected Especialidade createInstance() {
		Especialidade especialidade = new Especialidade();
		return especialidade;
	}

	@Override
	public String remove(Especialidade obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("especialidadeGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getEspecialidadePai() != null) {
			List<Especialidade> especialidadeList = getInstance().getEspecialidadePai().getEspecialidadeList();
			if (!especialidadeList.contains(instance)) {
				getEntityManager().refresh(getInstance().getEspecialidadePai());
			}
		}
		return action;
	}
}