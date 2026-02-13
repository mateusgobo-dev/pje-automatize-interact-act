package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaQualificacao;

public abstract class AbstractPessoaQualificacaoHome<T> extends AbstractHome<PessoaQualificacao> {

	private static final long serialVersionUID = 1L;

	public void setQualificacaoIdQualificacao(Integer id) {
		setId(id);
	}

	public Integer getQualificacaoIdQualificacao() {
		return (Integer) getId();
	}

	@Override
	protected PessoaQualificacao createInstance() {
		PessoaQualificacao pessoaQualificacao = new PessoaQualificacao();
		PessoaHome pessoaHome = (PessoaHome) Component.getInstance("pessoaHome", false);
		if (pessoaHome != null) {
			pessoaQualificacao.setPessoa(pessoaHome.getDefinedInstance());
		}
		QualificacaoHome qualificacao = (QualificacaoHome) Component.getInstance("qualificacaoHome", false);
		if (qualificacao != null) {
			pessoaQualificacao.setQualificacao(qualificacao.getDefinedInstance());
		}
		return pessoaQualificacao;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getPessoa() != null) {
			List<PessoaQualificacao> pessoaList = getInstance().getPessoa().getPessoaQualificacaoList();
			if (!pessoaList.contains(instance)) {
				getEntityManager().refresh(getInstance().getPessoa());
			}
		}
		if (getInstance().getQualificacao() != null) {
			List<PessoaQualificacao> qualificacaoList = getInstance().getQualificacao().getPessoaQualificacaoList();
			if (!qualificacaoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getQualificacao());
			}
		}
		newInstance();
		return action;
	}

	@Override
	public String remove(PessoaQualificacao obj) {
		setInstance(obj);
		String ret = remove();
		newInstance();
		return ret;
	}

	@Override
	public String remove() {
		PessoaHome pessoaHome = (PessoaHome) Component.getInstance("pessoaHome", false);
		if (pessoaHome != null) {
			pessoaHome.getInstance().getPessoaQualificacaoList().remove(instance);
		}
		QualificacaoHome qualificacao = (QualificacaoHome) Component.getInstance("qualificacaoHome", false);
		if (qualificacao != null) {
			qualificacao.getInstance().getPessoaQualificacaoList().remove(instance);
		}
		refreshGrid("pessoaQualificacaoGrid");
		return super.remove();
	}

}