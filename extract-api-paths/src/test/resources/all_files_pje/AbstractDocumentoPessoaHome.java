package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.DocumentoPessoa;

public abstract class AbstractDocumentoPessoaHome<T> extends AbstractHome<DocumentoPessoa> {

	private static final long serialVersionUID = 1L;

	public void setDocumentoPessoaIdDocumentoPessoa(Integer id) {
		setId(id);
	}

	public Integer getDocumentoPessoaIdDocumentoPessoa() {
		return (Integer) getId();
	}

	@Override
	protected DocumentoPessoa createInstance() {
		DocumentoPessoa documentoPessoa = new DocumentoPessoa();
		return documentoPessoa;
	}

	@Override
	public String remove(DocumentoPessoa obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("documentoPessoaGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (action != null)
			newInstance();
		return action;
	}
}