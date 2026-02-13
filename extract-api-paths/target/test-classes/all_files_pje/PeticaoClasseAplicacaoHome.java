package br.com.infox.cliente.home;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.cliente.component.suggest.AplicacaoClassePeticaoClasseAplicacaoSuggestBean;
import br.com.infox.cliente.component.suggest.ClasseJudicialPeticaoClasseAplicacaoSuggestBean;
import br.jus.pje.nucleo.entidades.ClasseAplicacao;
import br.jus.pje.nucleo.entidades.PeticaoClasseAplicacao;

@Name("peticaoClasseAplicacaoHome")
@BypassInterceptors
public class PeticaoClasseAplicacaoHome extends AbstractPeticaoClasseAplicacaoHome<PeticaoClasseAplicacao> {

	private static final long serialVersionUID = 1L;

	private ClasseJudicialPeticaoClasseAplicacaoSuggestBean getClasseJudicialPeticaoClasseAplicacaoSuggestBean() {
		return getComponent("classeJudicialPeticaoClasseAplicacaoSuggest");
	}

	private AplicacaoClassePeticaoClasseAplicacaoSuggestBean getAplicacaoClassePeticaoClasseAplicacaoSuggestBean() {
		return getComponent("aplicacaoClassePeticaoClasseAplicacaoSuggest");
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			if (getInstance().getClasseAplicacao().getClasseJudicial() != null) {
				getClasseJudicialPeticaoClasseAplicacaoSuggestBean().setInstance(
						getInstance().getClasseAplicacao().getClasseJudicial());
				getAplicacaoClassePeticaoClasseAplicacaoSuggestBean().setInstance(
						getInstance().getClasseAplicacao().getAplicacaoClasse());
			}
		}
		if (id == null) {
			ClasseAplicacao classeAplicacao = new ClasseAplicacao();
			getClasseJudicialPeticaoClasseAplicacaoSuggestBean().setInstance(classeAplicacao.getClasseJudicial());
			getAplicacaoClassePeticaoClasseAplicacaoSuggestBean().setInstance(classeAplicacao.getAplicacaoClasse());
		}
	}

	public String getCJSuggest() {
		String cj = "";
		if (getClasseJudicialPeticaoClasseAplicacaoSuggestBean().getInstance() != null) {
			cj = getClasseJudicialPeticaoClasseAplicacaoSuggestBean().getInstance().getClasseJudicial();
		}
		return cj;
	}

	public String getACSuggest() {
		String ac = "";
		if (getAplicacaoClassePeticaoClasseAplicacaoSuggestBean().getInstance() != null) {
			ac = getAplicacaoClassePeticaoClasseAplicacaoSuggestBean().getInstance().getAplicacaoClasse();
		}
		return ac;
	}

	public void limparACSuggest() {
		getAplicacaoClassePeticaoClasseAplicacaoSuggestBean().setInstance(null);
		Contexts.removeFromAllContexts("aplicacaoClassePeticaoClasseAplicacaoSuggest");
	}

	private ClasseAplicacao getClasseAplicacao() {
		String query = "select o from ClasseAplicacao o " + "where o.classeJudicial = :classeJudicial "
				+ "and o.aplicacaoClasse = :aplicacaoClasse";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("classeJudicial", getClasseJudicialPeticaoClasseAplicacaoSuggestBean().getInstance());
		q.setParameter("aplicacaoClasse", getAplicacaoClassePeticaoClasseAplicacaoSuggestBean().getInstance());
		return (ClasseAplicacao) q.getSingleResult();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setClasseAplicacao(getClasseAplicacao());
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		String ret = super.persist();
		refreshGrid("peticaoClasseAplicacaoGrid");
		return ret;
	}

	@Override
	public String update() {
		refreshGrid("peticaoClasseAplicacaoGrid");
		return super.update();
	}

	@Override
	public String remove(PeticaoClasseAplicacao obj) {
		obj.setAtivo(Boolean.FALSE);
		setInstance(obj);
		String ret = super.update();
		newInstance();
		return ret;
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("classeJudicialPeticaoClasseAplicacaoSuggest");
		Contexts.removeFromAllContexts("aplicacaoClassePeticaoClasseAplicacaoSuggest");
		super.newInstance();
	}
}