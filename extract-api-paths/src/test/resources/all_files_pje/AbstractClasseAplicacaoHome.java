package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ClasseAplicacao;
import br.jus.pje.nucleo.entidades.ComplementoClasse;

public abstract class AbstractClasseAplicacaoHome<T> extends AbstractHome<ClasseAplicacao> {

	private static final long serialVersionUID = 1L;

	public void setClasseAplicacaoIdClasseAplicacao(Integer id) {
		setId(id);
	}

	public Integer getClasseAplicacaoIdClasseAplicacao() {
		return (Integer) getId();
	}

	@Override
	protected ClasseAplicacao createInstance() {
		ClasseAplicacao classeAplicacao = new ClasseAplicacao();
		ClasseJudicialHome classeJudicialHome = (ClasseJudicialHome) Component.getInstance("classeJudicialHome", false);
		if (classeJudicialHome != null) {
			classeAplicacao.setClasseJudicial(classeJudicialHome.getDefinedInstance());
		}
		AplicacaoClasseHome aplicacaoClasseHome = (AplicacaoClasseHome) Component.getInstance("aplicacaoClasseHome",
				false);
		if (aplicacaoClasseHome != null) {
			classeAplicacao.setAplicacaoClasse(aplicacaoClasseHome.getDefinedInstance());
		}
		return classeAplicacao;
	}

	@Override
	public String remove() {
		ClasseJudicialHome classeJudicial = (ClasseJudicialHome) Component.getInstance("classeJudicialHome", false);
		if (classeJudicial != null) {
			classeJudicial.getInstance().getClasseAplicacaoList().remove(instance);
		}
		AplicacaoClasseHome aplicacaoClasse = (AplicacaoClasseHome) Component.getInstance("aplicacaoClasseHome", false);
		if (aplicacaoClasse != null) {
			aplicacaoClasse.getInstance().getClasseAplicacaoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(ClasseAplicacao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("classeAplicacaoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		return action;
	}

	public List<ComplementoClasse> getComplementoClasseList() {
		return getInstance() == null ? null : getInstance().getComplementoClasseList();
	}
}