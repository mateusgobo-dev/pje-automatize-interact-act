package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ComplementoClasse;
import br.jus.pje.nucleo.entidades.ComplementoClasseProcessoTrf;

public abstract class AbstractComplementoClasseHome<T> extends AbstractHome<ComplementoClasse> {

	private static final long serialVersionUID = 1L;

	public void setComplementoClasseIdComplementoClasse(Integer id) {
		setId(id);
	}

	public Integer getComplementoClasseIdComplementoClasse() {
		return (Integer) getId();
	}

	@Override
	protected ComplementoClasse createInstance() {
		ComplementoClasse complementoClasse = new ComplementoClasse();
		ClasseAplicacaoHome classeAplicacaoHome = (ClasseAplicacaoHome) Component
				.getInstance("qualificacaoHome", false);
		if (classeAplicacaoHome != null) {
			complementoClasse.setClasseAplicacao(classeAplicacaoHome.getDefinedInstance());
		}
		return complementoClasse;
	}

	@Override
	public String remove() {
		ClasseAplicacaoHome classeAplicacao = (ClasseAplicacaoHome) Component.getInstance("qualificacaoHome", false);
		if (classeAplicacao != null) {
			classeAplicacao.getInstance().getComplementoClasseList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(ComplementoClasse obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("complementoClasseGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getClasseAplicacao() != null) {
			List<ComplementoClasse> classeAplicacaoList = getInstance().getClasseAplicacao().getComplementoClasseList();
			if (!classeAplicacaoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getClasseAplicacao());
			}
		}
		newInstance();
		return action;
	}

	public List<ComplementoClasseProcessoTrf> getComplementoClasseProcessoTrfList() {
		return getInstance() == null ? null : getInstance().getComplementoClasseProcessoTrfList();
	}

}