package br.com.infox.cliente.home;

import java.util.List;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ComplementoQualificacao;
import br.jus.pje.nucleo.entidades.PessoaQualificacao;
import br.jus.pje.nucleo.entidades.Qualificacao;
import br.jus.pje.nucleo.entidades.TipoPessoaQualificacao;

public abstract class AbstractQualificacaoHome<T> extends AbstractHome<Qualificacao> {

	private static final long serialVersionUID = 1L;

	public void setQualificacaoIdQualificacao(Integer id) {
		setId(id);
	}

	public Integer getQualificacaoIdQualificacao() {
		return (Integer) getId();
	}

	@Override
	protected Qualificacao createInstance() {
		Qualificacao qualificacao = new Qualificacao();
		return qualificacao;
	}

	@Override
	public String persist() {
		String action = super.persist();
		// newInstance();
		return action;
	}

	public List<TipoPessoaQualificacao> getTipoPessoaQualificacaoList() {
		return getInstance() == null ? null : getInstance().getTipoPessoaQualificacaoList();
	}

	public List<PessoaQualificacao> getPessoaQualificacaoList() {
		return getInstance() == null ? null : getInstance().getPessoaQualificacaoList();
	}

	public List<ComplementoQualificacao> getComplementoQualificacaoList() {
		return getInstance() == null ? null : getInstance().getComplementoQualificacaoList();
	}
}