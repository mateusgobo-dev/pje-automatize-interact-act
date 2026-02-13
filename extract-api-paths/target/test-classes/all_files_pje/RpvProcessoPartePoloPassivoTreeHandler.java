package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.pje.action.RpvAction;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("rpvProcessoPartePoloPassivoTree")
@BypassInterceptors
public class RpvProcessoPartePoloPassivoTreeHandler extends AbstractRpvProcessoParteTreeHandler<ProcessoParte> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Integer getIdAutorCabeca() {
		return null;
	}

	@Override
	protected int getIdProcesso() {
		return RpvAction.instance().getProcessoTrf().getIdProcessoTrf();
	}

	@Override
	protected ProcessoParteParticipacaoEnum getInParticipacao() {
		return ProcessoParteParticipacaoEnum.P;
	}


}