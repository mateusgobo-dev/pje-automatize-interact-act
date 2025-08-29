package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.ProcessoParteAdvogado;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("processoParteAdvogadoHome")
@BypassInterceptors
public class ProcessoParteAdvogadoHome extends AbstractProcessoParteAdvogadoHome<ProcessoParteAdvogado> {

	private static final long serialVersionUID = 1L;
	private ProcessoParteParticipacaoEnum polo;

	public ProcessoParteParticipacaoEnum getPolo() {
		return polo;
	}

	public void setPolo(ProcessoParteParticipacaoEnum polo) {
		this.polo = polo;
	}

}