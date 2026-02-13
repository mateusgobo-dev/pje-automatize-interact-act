package br.com.infox.ibpm.component.tree;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * PJE-JT: David Vieira: [[PJE-779] Alteracoes feitas pela JT: 2011-11-08
 * 
 * Componente responsável gerenciar o lançamento de movimento no editor com
 * assinatura.
 */
@Name(EventsEditorTreeHandler.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EventsEditorTreeHandler extends EventsTreeHandler {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "eventsEditorTree";

	public static EventsEditorTreeHandler instance() {
		return (EventsEditorTreeHandler) org.jboss.seam.Component.getInstance(EventsEditorTreeHandler.NAME);
	}

	/**
	 * PJE-JT: David Vieira: [PJE-779] Não disparar o evento de registrar os
	 * movimentos no TarevaEvento
	 */
	@Override
	protected boolean isRegisterAfterRegisterEvent() {
		return false;
	}
	
}
