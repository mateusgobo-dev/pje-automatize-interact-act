package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ProcessoAudienciaPessoa;

public abstract class AbstractProcessoAudienciaPessoaHome<T> extends AbstractHome<ProcessoAudienciaPessoa> {

	private static final long serialVersionUID = 1L;

	public void setProcessoAudienciaPessoaIdProcessoAudienciaPessoa(Integer id) {
		setId(id);
	}

	public Integer getProcessoAudienciaPessoaIdProcessoAudienciaPessoa() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoAudienciaPessoa createInstance() {
		ProcessoAudienciaPessoa processoAudienciaPessoa = new ProcessoAudienciaPessoa();
		return processoAudienciaPessoa;
	}

	@Override
	public String remove(ProcessoAudienciaPessoa obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("processoPoloAtivoTestemunhaGrid");
		refreshGrid("processoPoloPassivoTestemunhaGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		return action;
	}

}