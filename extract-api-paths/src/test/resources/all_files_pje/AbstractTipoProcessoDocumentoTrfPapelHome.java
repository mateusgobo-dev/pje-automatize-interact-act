package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoPapel;

public abstract class AbstractTipoProcessoDocumentoTrfPapelHome<T> extends AbstractHome<TipoProcessoDocumentoPapel> {
	private static final long serialVersionUID = 1L;

	public void setTipoProcessoDocumentoPessoaIdTipoProcessoDocumentoPapel(Integer id) {
		setId(id);
	}

	public Integer getTipoProcessoDocumentoPessoaIdTipoProcessoDocumentoPapel() {
		return (Integer) getId();
	}

	@Override
	public String remove(TipoProcessoDocumentoPapel obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("tipoProcessoDocumentoTrfPapelGrid");
		return ret;
	}

	@Override
	public String persist() {
		String ret = super.persist();
		newInstance();
		return ret;
	}
}