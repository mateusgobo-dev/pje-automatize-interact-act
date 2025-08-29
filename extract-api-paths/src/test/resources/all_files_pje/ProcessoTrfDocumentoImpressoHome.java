package br.com.infox.cliente.home;

import java.util.Date;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfDocumentoImpresso;

@Name("processoTrfDocumentoImpressoHome")
@BypassInterceptors
public class ProcessoTrfDocumentoImpressoHome extends
		AbstractProcessoTrfDocumentoImpressoHome<ProcessoTrfDocumentoImpresso> {

	private static final long serialVersionUID = 1L;

	public static ProcessoTrfDocumentoImpressoHome instance() {
		return ComponentUtil.getComponent("processoTrfDocumentoImpressoHome");
	}

	@Override
	public void newInstance() {
		super.newInstance();
		getInstance().setImpresso(false);
	}

	private Pessoa getPessoaLogada() {
		Pessoa resultado = null;
		Context sessionContext = Contexts.getSessionContext();
		if (sessionContext != null) {
			resultado = (Pessoa) Contexts.getSessionContext().get("pessoaLogada");
		}
		return resultado;
	}

	public String update(ProcessoTrfDocumentoImpresso docImpressao) {
		setInstance(docImpressao);
		getInstance().setDataImpressao(new Date());
		getInstance().setPessoaImpressao(getPessoaLogada());
		String update = super.update();
		refreshGrid("processoTrfIncidentalDocumentoImpressaoGrid");
		refreshGrid("processoTrfIncidentalDocumentoImpressoGrid");
		return update;
	}

	public String setNaoImpresso(ProcessoTrfDocumentoImpresso docImpressao) {
		setInstance(docImpressao);
		getInstance().setDataImpressao(null);
		getInstance().setPessoaImpressao((Pessoa) null);
		String update = super.update();
		refreshGrid("processoTrfIncidentalDocumentoImpressaoGrid");
		refreshGrid("processoTrfIncidentalDocumentoImpressoGrid");
		return update;
	}

	@Observer("processoDocumentoCreated")
	public void gravarDocumento(ProcessoDocumento documento) {
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, documento.getProcesso().getIdProcesso());

		ProcessoTrfDocumentoImpresso ptdi = new ProcessoTrfDocumentoImpresso();
		ptdi.setIdProcessoDocumento(documento.getIdProcessoDocumento());
		ptdi.setProcessoDocumento(documento);
		ptdi.setDataImpressao(new Date());
		ptdi.setProcessoTrf(processoTrf);
		ptdi.setPessoaImpressao(Authenticator.getPessoaLogada());
		getEntityManager().merge(ptdi);
		EntityUtil.flush();
	}

}