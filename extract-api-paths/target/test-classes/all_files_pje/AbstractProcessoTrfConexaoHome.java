package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;

public abstract class AbstractProcessoTrfConexaoHome<T> extends AbstractHome<ProcessoTrfConexao> {

	private static final long serialVersionUID = 1L;

	public void setProcessoTrfConexaoIdProcessoTrfConexao(Integer id) {
		setId(id);
	}

	public Integer getProcessoTrfConexaoIdProcessoTrfConexao() {
		return (Integer) getId();
	}

	public String persist(ProcessoTrfConexao processoTrfConexao) {
		setInstance(processoTrfConexao);
		String ret = super.persist();
		newInstance();
		return ret;
	}

}