package br.com.infox.cliente.home;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Name("processoExpedienteHomeBP")
@Scope(ScopeType.STATELESS)
@BypassInterceptors
public class ProcessoExpedienteHomeBP extends ProcessoExpedienteHome {

	private static final long serialVersionUID = 1L;

}
