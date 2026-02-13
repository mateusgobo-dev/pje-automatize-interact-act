package br.com.infox.cliente.home;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.ClasseJudicialSuggestBean;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ClasseJudicialAgrupamento;

@Name(ClasseJudicialAgrupamentoHome.NAME)
@BypassInterceptors
public class ClasseJudicialAgrupamentoHome extends AbstractHome<ClasseJudicialAgrupamento> {

	public static final String NAME = "classeJudicialAgrupamentoHome";
	private static final long serialVersionUID = 1L;

	@Override
	public String remove() {
		String remove = super.remove();
		newInstance();
		refreshGrid("classeJudicialAgrupamentoGrid");
		return remove;
	}

	@Override
	public String remove(ClasseJudicialAgrupamento c) {
		setInstance(c);
		String remove = super.remove();
		newInstance();
		refreshGrid("classeJudicialAgrupamentoGrid");
		return remove;
	}

	public void setClasseJudicialAgrupamentoIdAgrupamentoClasses(Integer id) {
		setId(id);
	}

	public Integer getClasseJudicialAgrupamentoIdAgrupamentoClasses() {
		return (Integer) getId();
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("classeJudicialSuggest");
		super.newInstance();

	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setAgrupamento(AgrupamentoClasseJudicialHome.instance().getInstance());
		if (getClasseJudicialSuggest().getInstance() != null) {
			getInstance().setClasse(getClasseJudicialSuggest().getInstance());
		} else {
			FacesMessages.instance().add(Severity.ERROR, "É necessário escolher uma Classe Judicial.");
			return false;
		}
		if (verificaAgrupamentoClasse()) {
			FacesMessages.instance().add(Severity.ERROR, "Registro já cadastrado.");
			return false;
		}

		getInstance().setAgrupamento(AgrupamentoClasseJudicialHome.instance().getInstance());
		return super.beforePersistOrUpdate();
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		refreshGrid("classeJudicialAgrupamentoGrid");
		return super.afterPersistOrUpdate(ret);
	}

	private ClasseJudicialSuggestBean getClasseJudicialSuggest() {
		ClasseJudicialSuggestBean classeJudicialSuggestBean = (ClasseJudicialSuggestBean) Component
				.getInstance("classeJudicialSuggest");
		return classeJudicialSuggestBean;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		ClasseJudicial classe = getInstance().getClasse();
		if (changed) {
			getClasseJudicialSuggest().setInstance(classe);
		}
		if (id == null) {
			getClasseJudicialSuggest().setInstance(classe);
		}
	}

	public boolean verificaAgrupamentoClasse() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from ClasseJudicialAgrupamento o ");
		sb.append("where o.classe = :classe ");
		sb.append("and o.agrupamento = :agrupamento ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("classe", getInstance().getClasse());
		q.setParameter("agrupamento", getInstance().getAgrupamento());
		Long count = EntityUtil.getSingleResult(q);
		return count > 0;

	}

}
