package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoTrfUsuarioLocalizacaoMagistradoServidor;

public abstract class AbstractProcessoTrfUsuarioLocalizacaoMagistradoServidorHome<T> extends
		AbstractHome<ProcessoTrfUsuarioLocalizacaoMagistradoServidor> {

	private static final long serialVersionUID = 1L;

	public void setProcessoTrfUsuarioLocalizacaoMagistradoServidorIdProcessoTrfUsuarioLocalizacaoMagistradoServidor(
			Integer id) {
		setId(id);
	}

	public Integer getProcessoTrfUsuarioLocalizacaoMagistradoServidorIdProcessoTrfUsuarioLocalizacaoMagistradoServidor() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoTrfUsuarioLocalizacaoMagistradoServidor createInstance() {
		ProcessoTrfUsuarioLocalizacaoMagistradoServidor processoTrfUsuarioLocalizacaoMagistradoServidor = new ProcessoTrfUsuarioLocalizacaoMagistradoServidor();
		return processoTrfUsuarioLocalizacaoMagistradoServidor;
	}

	@Override
	public String remove(ProcessoTrfUsuarioLocalizacaoMagistradoServidor obj) {
		setInstance(obj);
		String ret = super.update();
		newInstance();
		// refreshGrid("processoParteExpedienteVisitaGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (action != null) {
			newInstance();
		}
		return action;
	}
}