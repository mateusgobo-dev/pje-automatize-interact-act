package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ComplementoPessoaQualificacao;

public abstract class AbstractComplementoPessoaQualificacaoHome<T> extends AbstractHome<ComplementoPessoaQualificacao> {

	private static final long serialVersionUID = 1L;

	public void setComplementoPessoaQualificacaoIdComplementoPessoaQualificacao(Integer id) {
		setId(id);
	}

	public Integer getComplementoPessoaQualificacaoIdComplementoPessoaQualificacao() {
		return (Integer) getId();
	}

	@Override
	protected ComplementoPessoaQualificacao createInstance() {
		ComplementoPessoaQualificacao complementoPessoaQualificacao = new ComplementoPessoaQualificacao();
		ComplementoQualificacaoHome complementoQualificacaoHome = (ComplementoQualificacaoHome) Component.getInstance(
				"complementoQualificacaoHome", false);
		if (complementoQualificacaoHome != null) {
			complementoPessoaQualificacao.setComplementoQualificacao(complementoQualificacaoHome.getDefinedInstance());
		}
		PessoaQualificacaoHome pessoaQualificacaoHome = (PessoaQualificacaoHome) Component.getInstance(
				"pessoaQualificacaoHome", false);
		if (pessoaQualificacaoHome != null) {
			complementoPessoaQualificacao.setPessoaQualificacao(pessoaQualificacaoHome.getDefinedInstance());
		}
		return complementoPessoaQualificacao;
	}

	@Override
	public String remove() {
		ComplementoQualificacaoHome complementoQualificacao = (ComplementoQualificacaoHome) Component.getInstance(
				"complementoQualificacaoHome", false);
		if (complementoQualificacao != null) {
			complementoQualificacao.getInstance().getComplementoPessoaQualificacaoList().remove(instance);
		}
		PessoaQualificacaoHome pessoaQualificacao = (PessoaQualificacaoHome) Component.getInstance(
				"pessoaQualificacaoHome", false);
		if (pessoaQualificacao != null) {
			pessoaQualificacao.getInstance().getComplementoPessoaQualificacaoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(ComplementoPessoaQualificacao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("complementoPessoaQualificacaoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getComplementoQualificacao() != null) {
			List<ComplementoPessoaQualificacao> complementoQualificacaoList = getInstance()
					.getComplementoQualificacao().getComplementoPessoaQualificacaoList();
			if (!complementoQualificacaoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getComplementoQualificacao());
			}
		}
		if (getInstance().getPessoaQualificacao() != null) {
			List<ComplementoPessoaQualificacao> pessoaQualificacaoList = getInstance().getPessoaQualificacao()
					.getComplementoPessoaQualificacaoList();
			if (!pessoaQualificacaoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getPessoaQualificacao());
			}
		}
		newInstance();
		return action;
	}

}