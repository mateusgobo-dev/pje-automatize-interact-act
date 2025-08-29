package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.AssertionFailure;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.component.suggest.TipoPichacaoSuggestBean;
import br.jus.pje.nucleo.entidades.ClasseJudicialTipoCertidao;
import br.jus.pje.nucleo.entidades.TipoPichacaoClasseJudicial;

@Name("tipoPichacaoClasseJudicialHome")
@BypassInterceptors
public class TipoPichacaoClasseJudicialHome extends AbstractTipoPichacaoClasseJudicialHome<TipoPichacaoClasseJudicial> {

	private static final long serialVersionUID = 1L;

	private TipoPichacaoSuggestBean getTipoPichacaoSuggestBean() {
		return getComponent("tipoPichacaoSuggest");
	}

	@Override
	public String persist() {
		refreshGrid("tipoPichacaoClasseJudicialGrid");
		return super.persist();
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	public String update() {
		refreshGrid("tipoPichacaoClasseJudicialGrid");
		return super.update();
	}

	@Override
	public String remove(TipoPichacaoClasseJudicial obj) {
		removerClassTipoCertidao();
		String ret = remover(obj);
		return ret;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			if (getInstance().getTipoPichacao() != null) {
				getTipoPichacaoSuggestBean().setInstance(getInstance().getTipoPichacao());
			}
		}
		if (id == null) {
			getTipoPichacaoSuggestBean().setInstance(null);
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setTipoPichacao(getTipoPichacaoSuggestBean().getInstance());
		return super.beforePersistOrUpdate();
	}

	@SuppressWarnings("unchecked")
	public void removerClassTipoCertidao() {
		EntityManager em = getEntityManager();
		String query = "select o from ClasseJudicialTipoCertidao o " + "where o.classeJudicial = :classJudicial";
		Query q = em.createQuery(query);
		q.setParameter("classJudicial", getInstance().getClasseJudicial());

		List<ClasseJudicialTipoCertidao> list = q.getResultList();

		if (list.size() > 0) {
			for (ClasseJudicialTipoCertidao classeJudicialTipoCertidao : list) {
				getEntityManager().remove(classeJudicialTipoCertidao);
				try {
					getEntityManager().flush();
				} catch (AssertionFailure e) {
					// Igonorar
				}
			}
		}
	}
}
