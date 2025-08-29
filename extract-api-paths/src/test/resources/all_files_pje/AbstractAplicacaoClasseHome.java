package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;

public abstract class AbstractAplicacaoClasseHome<T> extends AbstractHome<AplicacaoClasse> {

	private static final long serialVersionUID = 1L;

	public void setAplicacaoClasseIdAplicacaoClasse(Integer id) {
		setId(id);
	}

	public Integer getAplicacaoClasseIdAplicacaoClasse() {
		return (Integer) getId();
	}

	@Override
	protected AplicacaoClasse createInstance() {
		AplicacaoClasse aplicacaoClasse = new AplicacaoClasse();
		return aplicacaoClasse;
	}

	@Override
	public String remove(AplicacaoClasse obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("aplicacaoClasseGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		// newInstance();
		return action;
	}

}