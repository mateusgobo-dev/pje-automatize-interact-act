package br.com.infox.cliente.component.tree;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.pje.action.RpvAction;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("rpvProcessoPartePoloAtivoTree")
@Scope(ScopeType.EVENT)
@BypassInterceptors
public class RpvProcessoPartePoloAtivoTreeHandler extends AbstractRpvProcessoParteTreeHandler<ProcessoParte> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Integer getIdAutorCabeca() {
		Pessoa autor = RpvAction.instance().getAutorCabeca().getPessoa();
		return autor != null ? autor.getIdUsuario() : null;
	}

	@Override
	protected int getIdProcesso() {
		return RpvAction.instance().getProcessoTrf().getIdProcessoTrf();
	}

	@Override
	protected ProcessoParteParticipacaoEnum getInParticipacao() {
		return ProcessoParteParticipacaoEnum.A;
	}

}