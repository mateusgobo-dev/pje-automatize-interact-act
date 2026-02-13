package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoParteVisibilidadeSigilo;

public abstract class AbstractProcessoParteVisibilidadeSigiloHome<T> extends
		AbstractHome<ProcessoParteVisibilidadeSigilo> {

	private static final long serialVersionUID = 1L;

	public void setProcessoParteVisibilidadeSigiloIdProcessoParteVisibilidadeSigilo(Integer id) {
		setId(id);
	}

	public Integer getProcessoParteVisibilidadeSigiloIdProcessoParteVisibilidadeSigilo() {
		return (Integer) getId();
	}

	public String persist(ProcessoParteVisibilidadeSigilo obj) {
		setInstance(obj);
		String ret = super.persist();
		newInstance();
		return ret;
	}

	@Override
	public String remove(ProcessoParteVisibilidadeSigilo obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		return ret;
	}
}