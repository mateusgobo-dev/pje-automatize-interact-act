package br.com.infox.cliente.home;

import java.util.Date;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrfImpresso;

@Name("processoTrfImpressoHome")
@BypassInterceptors
public class ProcessoTrfImpressoHome extends AbstractProcessoTrfImpressoHome<ProcessoTrfImpresso> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Pessoa getPessoaLogada() {
		Pessoa resultado = null;
		Context sessionContext = Contexts.getSessionContext();
		if (sessionContext != null) {
			resultado = (Pessoa) Contexts.getSessionContext().get("pessoaLogada");
		}
		return resultado;
	}

	public String update(ProcessoTrfImpresso procImpressao) {
		setInstance(procImpressao);
		getInstance().setPessoaImpressao(getPessoaLogada());
		getInstance().setDataImpressao(new Date());
		String update = super.update();
		refreshGrid("processoTrfInicialImpressaoGrid");
		refreshGrid("processoTrfInicialImpressoGrid");
		return update;
	}

	public String setNaoImpresso(ProcessoTrfImpresso procImpressao) {
		setInstance(procImpressao);
		getInstance().setPessoaImpressao((Pessoa) null);
		getInstance().setDataImpressao(null);
		String update = super.update();
		refreshGrid("processoTrfInicialImpressaoGrid");
		refreshGrid("processoTrfInicialImpressoGrid");
		return update;
	}

}