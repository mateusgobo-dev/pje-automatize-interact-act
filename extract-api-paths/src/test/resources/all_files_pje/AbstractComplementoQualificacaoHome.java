package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ComplementoPessoaQualificacao;
import br.jus.pje.nucleo.entidades.ComplementoQualificacao;

public abstract class AbstractComplementoQualificacaoHome<T> extends AbstractHome<ComplementoQualificacao> {

	private static final long serialVersionUID = 1L;

	public void setComplementoQualificacaoIdComplementoQualificacao(Integer id) {
		setId(id);
	}

	public Integer getComplementoQualificacaoIdComplementoQualificacao() {
		return (Integer) getId();
	}

	@Override
	protected ComplementoQualificacao createInstance() {
		ComplementoQualificacao complementoQualificacao = new ComplementoQualificacao();
		QualificacaoHome qualificacaoHome = (QualificacaoHome) Component.getInstance("qualificacaoHome", false);
		if (qualificacaoHome != null) {
			complementoQualificacao.setQualificacao(qualificacaoHome.getDefinedInstance());
		}
		return complementoQualificacao;
	}

	@Override
	public String remove() {
		QualificacaoHome qualificacao = (QualificacaoHome) Component.getInstance("qualificacaoHome", false);
		if (qualificacao != null) {
			qualificacao.getInstance().getComplementoQualificacaoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(ComplementoQualificacao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("complementoQualificacaoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getQualificacao() != null) {
			List<ComplementoQualificacao> qualificacaoList = getInstance().getQualificacao()
					.getComplementoQualificacaoList();
			if (!qualificacaoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getQualificacao());
			}
		}
		newInstance();
		return action;
	}

	public List<ComplementoPessoaQualificacao> getComplementoPessoaQualificacaoList() {
		return getInstance() == null ? null : getInstance().getComplementoPessoaQualificacaoList();
	}

}