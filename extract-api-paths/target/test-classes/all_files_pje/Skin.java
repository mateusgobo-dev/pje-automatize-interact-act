package br.com.itx.jsf;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.Selector;
import org.jboss.seam.util.Strings;

import br.com.itx.component.Util;

@Name("wiSkin")
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class Skin extends Selector {

	private static final long serialVersionUID = 1L;

	private boolean exibeAltoContraste = false;

	public Skin() {
		Util util = (Util) Component.getInstance("util");
		setCookiePath(util.getContextPath());
		setCookieEnabled(true);
		String cookieValue = getCookieValueIfEnabled();
		if (!Strings.isEmpty(cookieValue)) {
			exibeAltoContraste = Boolean.getBoolean(cookieValue);
		}
	}
	
	/**
	 * Alterna a visibilidade do alto-contraste.
	 */
	public void alternarAltoContraste(){
		exibeAltoContraste = !exibeAltoContraste;
		setCookieValueIfEnabled(Boolean.toString(exibeAltoContraste));
	}
	
	/**
	 * Retorna a indicação da exibição (ou não) do alto-contraste.
	 * 
	 * @return Verdadeiro, o alto-contraste será exibido. Falso, o auto-contraste não será exibido. 
	 */
	public boolean isExibeAltoContraste(){
		return exibeAltoContraste;
	}

	@Override
	protected String getCookieName() {
		return "pje.exibe.alto_contraste";
	}

}