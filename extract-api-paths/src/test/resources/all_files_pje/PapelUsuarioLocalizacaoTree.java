package br.com.infox.ibpm.component.tree;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Name("papelUsuarioLocalizacaoTree")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PapelUsuarioLocalizacaoTree extends PapelTreeHandler {

	private static final long serialVersionUID = 1L;

	@Override
	public String getIconFolder() {
		return "/img/globe16.png";
	}

}