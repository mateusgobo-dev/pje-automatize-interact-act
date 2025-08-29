package br.com.infox.cliente.component.tree;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.pje.action.RpvAction;
import br.jus.pje.nucleo.entidades.RpvPessoaParte;

@Name("rpvPoloPassivoTree")
@Scope(ScopeType.EVENT)
@BypassInterceptors
public class RpvPoloPassivoTreeHandler extends AbstractRpvParteTreeHandler<RpvPessoaParte> {

	private static final long serialVersionUID = 1L;

	@Override
	protected int getIdRpv() {
		return RpvAction.instance().getRpv().getIdRpv();
	}

	@Override
	protected String getInParticipacao() {
		return "P";
	}
}