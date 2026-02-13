package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.component.suggest.AplicacaoClasseEventoSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.AplicacaoClasseEvento;

@Name(AplicacaoClasseEventoHome.NAME)
@BypassInterceptors
public class AplicacaoClasseEventoHome extends AbstractAplicacaoClasseEventoHome<AplicacaoClasseEvento> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "aplicacaoClasseEventoHome";

	public static AplicacaoClasseEventoHome instance() {
		return ComponentUtil.getComponent(AplicacaoClasseEventoHome.NAME);
	}

	public AplicacaoClasseEventoSuggestBean getAplicacaoClasseEventoSuggestBean() {
		return getComponent("aplicacaoClasseEventoSuggest");
	}

	@Override
	public String persist() {
		getInstance().setAplicacaoClasse(getAplicacaoClasseEventoSuggestBean().getInstance());
		String ret = super.persist();
		refreshGrid("aplicacaoClasseEventoGrid");
		return ret;
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	public String update() {
		String ret = super.update();
		refreshGrid("aplicacaoClasseEventoGrid");
		return ret;
	}

	@Override
	public String remove(AplicacaoClasseEvento obj) {
		setInstance(obj);
		refreshGrid("aplicacaoClasseEventoGrid");
		return super.remove(obj);
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			if (getInstance().getAplicacaoClasse() != null) {
				getAplicacaoClasseEventoSuggestBean().setInstance(getInstance().getAplicacaoClasse());
			}
		}
		if (id == null) {
			getAplicacaoClasseEventoSuggestBean().setInstance(null);
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setAplicacaoClasse(getAplicacaoClasseEventoSuggestBean().getInstance());
		return super.beforePersistOrUpdate();
	}

	// public String getSujeitoAtivo (String sigla){
	// String sujeito = SujeitoAtivoEnum.valueOf(sigla).getLabel();
	// return sujeito;
	// }

}
