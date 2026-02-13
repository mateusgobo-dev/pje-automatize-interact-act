package br.com.infox.bpm.action;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Name(value = DistribuirProcessanteAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class DistribuirProcessanteAction extends TaskAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "distribuirProcessanteAction";

}