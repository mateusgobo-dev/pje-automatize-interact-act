package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;

public abstract class AbstractClienteHome<T> extends AbstractHome<T> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getPersistenceContextName() {
		return "entityManager";
	}

}
