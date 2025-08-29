package br.com.infox.cliente.home;

import java.util.List;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Profissao;

public abstract class AbstractProfissaoHome<T> extends AbstractHome<Profissao> {

	private static final long serialVersionUID = 1L;

	public void setProfissaoIdProfissao(Integer id) {
		setId(id);
	}

	public Integer getProfissaoIdProfissao() {
		return (Integer) getId();
	}

	@Override
	protected Profissao createInstance() {
		Profissao profissao = new Profissao();
		return profissao;
	}

	@Override
	public String remove(Profissao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("profissaoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getProfissaoSuperior() != null) {
			List<Profissao> profissaoList = getInstance().getProfissaoSuperior().getProfissaoList();
			if (!profissaoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getProfissaoSuperior());
			}
		}
		return action;
	}
}