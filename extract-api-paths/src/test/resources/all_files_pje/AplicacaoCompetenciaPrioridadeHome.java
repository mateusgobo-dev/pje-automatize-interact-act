package br.com.infox.cliente.home;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.component.suggest.AplicacaoClasseSuggestBean;
import br.com.infox.cliente.component.suggest.ClasseJudicialSuggestBean;
import br.com.infox.cliente.component.suggest.CompetenciaSuggestBean;
import br.jus.pje.nucleo.entidades.AplicacaoCompetenciaPrioridade;

@Name("aplicacaoCompetenciaPrioridadeHome")
@BypassInterceptors
public class AplicacaoCompetenciaPrioridadeHome extends
		AbstractAplicacaoCompetenciaPrioridadeHome<AplicacaoCompetenciaPrioridade> {

	private static final long serialVersionUID = 1L;

	@Override
	public String remove(AplicacaoCompetenciaPrioridade obj) {
		setInstance(obj);
		return super.remove(obj);
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setPrioridadeProcesso(PrioridadeProcessoHome.instance().getInstance());
		getInstance().setAplicacaoClasse(getAplicacaoClasseSuggest().getInstance());
		getInstance().setCompetencia(getCompetenciaSuggest().getInstance());
		getInstance().setClasseJudicial(getClasseJudicialSuggest().getInstance());
		return super.beforePersistOrUpdate();
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		refreshGrid("aplicacaoCompetenciaPrioridadeGrid");
		newInstance();
		return super.afterPersistOrUpdate(ret);
	}

	private AplicacaoClasseSuggestBean getAplicacaoClasseSuggest() {
		AplicacaoClasseSuggestBean aplicacaoClasseSuggest = (AplicacaoClasseSuggestBean) Component
				.getInstance("aplicacaoClasseSuggest");
		return aplicacaoClasseSuggest;
	}

	private CompetenciaSuggestBean getCompetenciaSuggest() {
		CompetenciaSuggestBean competenciaSuggest = (CompetenciaSuggestBean) Component
				.getInstance("competenciaSuggest");
		return competenciaSuggest;
	}

	private ClasseJudicialSuggestBean getClasseJudicialSuggest() {
		ClasseJudicialSuggestBean classeJudicialSuggest = (ClasseJudicialSuggestBean) Component
				.getInstance("classeJudicialSuggest");
		return classeJudicialSuggest;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			getAplicacaoClasseSuggest().setInstance(getInstance().getAplicacaoClasse());
			getCompetenciaSuggest().setInstance(getInstance().getCompetencia());
			getClasseJudicialSuggest().setInstance(getInstance().getClasseJudicial());
		}
		if (id == null) {
			getAplicacaoClasseSuggest().setInstance(null);
			getCompetenciaSuggest().setInstance(null);
			getClasseJudicialSuggest().setInstance(null);
		}
	}

}