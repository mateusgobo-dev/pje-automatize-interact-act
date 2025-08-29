package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.component.suggest.TipoCertidaoSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ClasseJudicialTipoCertidao;

@Name("tipoCertidaoClasseJudicialHome")
@BypassInterceptors
public class TipoCertidaoClasseJudicialHome extends AbstractTipoCertidaoClasseJudicialHome<ClasseJudicialTipoCertidao> {

	private static final long serialVersionUID = 1L;

	private TipoCertidaoSuggestBean getTipoCertidaoSuggestBean() {
		return getComponent("tipoCertidaoSuggest");
	}

	public static TipoCertidaoClasseJudicialHome instance() {
		return ComponentUtil.getComponent("tipoCertidaoClasseJudicialHome");
	}

	@Override
	public String persist() {
		refreshGrid("tipoCertidaoClasseJudicialGrid");
		return super.persist();
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	public String update() {
		refreshGrid("tipoCertidaoClasseJudicialGrid");
		return super.update();
	}

	@Override
	public String remove(ClasseJudicialTipoCertidao obj) {
		// removerClassTipoCertidao();
		newInstance();
		TipoCertidaoHome.instance().newInstance();
		String ret = remover(obj);
		return ret;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			if (getInstance().getTipoCertidao() != null) {
				getTipoCertidaoSuggestBean().setInstance(getInstance().getTipoCertidao());
			}
		}
		if (id == null) {
			getTipoCertidaoSuggestBean().setInstance(null);
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setTipoCertidao(getTipoCertidaoSuggestBean().getInstance());
		return super.beforePersistOrUpdate();
	}

	/*
	 * public void removerClassTipoCertidao() { EntityManager em =
	 * getEntityManager(); String query =
	 * "select o from ClasseJudicialTipoCertidao o "+
	 * "where o.classeJudicial = :classJudicial"; Query q =
	 * em.createQuery(query); q.setParameter("classJudicial",
	 * getInstance().getClasseJudicial());
	 * 
	 * List<ClasseJudicialTipoCertidao> list = new
	 * ArrayList<ClasseJudicialTipoCertidao>(0); list = q.getResultList();
	 * 
	 * if (list.size() > 0) { for (ClasseJudicialTipoCertidao
	 * classeJudicialTipoCertidao : list) {
	 * getEntityManager().remove(classeJudicialTipoCertidao); try {
	 * getEntityManager().flush(); } catch (AssertionFailure e) { // Igonorar }
	 * } } }
	 */
}