package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public abstract class AbstractProcessoTrfHome<T> extends AbstractHome<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	public void setClasseJudicialIdClasseJudicial(Integer id) {
		setId(id);
	}

	public Integer getClasseJudicialIdClasseJudicial() {
		return (Integer) getId();
	}

	public void setProcessoTrfIdProcessoTrf(Integer id) {
		setId(id);
	}

	public Integer getProcessoTrfIdProcessoTrf() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoTrf createInstance() {
		ProcessoTrf processoTrf = new ProcessoTrf();
		return processoTrf;
	}

	@Override
	public String remove(ProcessoTrf obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		return ret;
	}

}