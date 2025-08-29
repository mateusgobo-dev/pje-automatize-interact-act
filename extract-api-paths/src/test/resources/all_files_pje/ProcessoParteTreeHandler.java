package br.com.infox.ibpm.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("processoParteTree")
@BypassInterceptors
public class ProcessoParteTreeHandler extends AbstractProcessoParteTreeHandler<ProcessoParte> {

	private static final long serialVersionUID = 1L;

	public ProcessoParteTreeHandler() {
		setInParticipacao(ProcessoParteParticipacaoEnum.T);
	}

}