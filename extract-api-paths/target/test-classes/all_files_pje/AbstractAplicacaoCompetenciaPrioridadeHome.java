package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.AplicacaoCompetenciaPrioridade;

public abstract class AbstractAplicacaoCompetenciaPrioridadeHome<T> extends
		AbstractHome<AplicacaoCompetenciaPrioridade> {

	private static final long serialVersionUID = 1L;

	public void setAplicacaoCompetenciaPrioridadeIdAplicacaoCompetenciaPrioridade(Integer id) {
		setId(id);
	}

	public Integer getAplicacaoCompetenciaPrioridadeIdAplicacaoCompetenciaPrioridade() {
		return (Integer) getId();
	}

	@Override
	protected AplicacaoCompetenciaPrioridade createInstance() {
		return new AplicacaoCompetenciaPrioridade();
	}

	@Override
	public String remove(AplicacaoCompetenciaPrioridade obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("aplicacaoCompetenciaPrioridadeGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		return action;
	}
}