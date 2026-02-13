package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoPessoa;

public abstract class AbstractTipoPessoaHome<T> extends AbstractHome<TipoPessoa> {

	private static final long serialVersionUID = 1L;

	public void setTipoPessoaIdTipoPessoa(Integer id) {
		setId(id);
	}

	public Integer getTipoPessoaIdTipoPessoa() {
		return (Integer) getId();
	}

	@Override
	protected TipoPessoa createInstance() {
		TipoPessoa tipoPessoa = new TipoPessoa();
		TipoPessoaHome tipoPessoaHome = (TipoPessoaHome) Component.getInstance("tipoPessoaHome", false);
		if (tipoPessoaHome != null) {
			tipoPessoa.setTipoPessoaSuperior(tipoPessoaHome.getDefinedInstance());
		}
		return tipoPessoa;
	}

	@Override
	public String remove(TipoPessoa obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("tipoPessoaGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getTipoPessoaSuperior() != null) {
			List<TipoPessoa> tipoPessoaSuperiorList = getInstance().getTipoPessoaSuperior().getTipoPessoaList();
			if (!tipoPessoaSuperiorList.contains(instance)) {
				getEntityManager().refresh(getInstance().getTipoPessoaSuperior());
			}
		}
		// newInstance();
		return action;
	}

	public List<TipoPessoa> getTipoPessoaList() {
		return getInstance() == null ? null : getInstance().getTipoPessoaList();
	}
}