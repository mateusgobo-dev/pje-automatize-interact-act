package br.com.infox.ibpm.component.tree;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Name("papelUsuarioLocalizacaoPJETree")
@BypassInterceptors
public class PapelUsuarioLocalizacaoPJETree extends PapelTreeHandler {

	private static final long serialVersionUID = 1L;

	public static PapelUsuarioLocalizacaoPJETree instance() {
		return (PapelUsuarioLocalizacaoPJETree) Component.getInstance("papelUsuarioLocalizacaoPJETree");
	}

	public PapelUsuarioLocalizacaoPJETree() {
		super();
		System.out.println("Criando: " + this.getClass().getName());
	}

	@Override
	public String getIconFolder() {
		return "/img/globe16.png";
	}

}