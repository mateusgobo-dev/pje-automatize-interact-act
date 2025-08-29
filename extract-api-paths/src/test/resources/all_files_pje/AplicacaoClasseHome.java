package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;

@Name(AplicacaoClasseHome.NAME)
@BypassInterceptors
public class AplicacaoClasseHome extends AbstractAplicacaoClasseHome<AplicacaoClasse> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "aplicacaoClasseHome";

	public static AplicacaoClasseHome instance() {
		return ComponentUtil.getComponent(AplicacaoClasseHome.NAME);
	}

	@Override
	public String persist() {
		refreshGrid("aplicacaoClasseGrid");
		return super.persist();
	}

	@Override
	public String update() {
		refreshGrid("aplicacaoClasseGrid");
		return super.update();
	}

	@Override
	public String remove(AplicacaoClasse obj) {
		setInstance(obj);
		obj.setAtivo(Boolean.FALSE);
		super.update();
		newInstance();
		refreshGrid("aplicacaoClasseGrid");
		return "updated";
	}

	@Override
	public boolean isEditable() {
		return ParametroUtil.instance().getPermitirCadastrosBasicos();
	}
}
