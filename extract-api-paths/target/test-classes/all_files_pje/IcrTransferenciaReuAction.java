package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.EstabelecimentoPrisional;
import br.jus.pje.nucleo.entidades.IcrTransferenciaReu;

@Name("icrTransferenciaReuAction")
@Scope(ScopeType.CONVERSATION)
public class IcrTransferenciaReuAction extends
		IcrAssociarIcrPrisaoAction<IcrTransferenciaReu, IcrTransferenciaReuManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9220147822802110067L;

	@Override
	protected EstabelecimentoPrisional getEstabelecimentoPrisional(IcrTransferenciaReu entity) {
		return getInstance().getEstabelecimentoPrisional();
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		//
	}
}
