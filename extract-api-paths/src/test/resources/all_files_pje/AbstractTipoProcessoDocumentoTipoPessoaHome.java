package br.com.infox.cliente.home;

import br.com.infox.ibpm.home.TipoProcessoDocumentoHome;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoTipoPessoa;

public abstract class AbstractTipoProcessoDocumentoTipoPessoaHome<T> extends
		AbstractHome<TipoProcessoDocumentoTipoPessoa> {

	private static final long serialVersionUID = 1L;

	public void setTipoProcessoDocumentoTipoPessoaIdTipoProcessoDocumentoTipoPessoa(Integer id) {
		setId(id);
	}

	public Integer getTipoProcessoDocumentoTipoPessoaIdTipoProcessoDocumentoTipoPessoa() {
		return (Integer) getId();
	}

	@Override
	protected TipoProcessoDocumentoTipoPessoa createInstance() {
		TipoProcessoDocumentoTipoPessoa tipoProcessoDocumentoTipoPessoa = new TipoProcessoDocumentoTipoPessoa();

		TipoProcessoDocumentoHome tipoProcessoDocumentoHome = TipoProcessoDocumentoHome.instance();
		if (tipoProcessoDocumentoHome != null) {
			tipoProcessoDocumentoTipoPessoa.setTipoProcessoDocumento(tipoProcessoDocumentoHome.getDefinedInstance());
		}

		TipoPessoaHome tipoPessoaHome = TipoPessoaHome.instance();
		if (tipoPessoaHome != null) {
			tipoProcessoDocumentoTipoPessoa.setTipoPessoa(tipoPessoaHome.getDefinedInstance());
		}
		return tipoProcessoDocumentoTipoPessoa;
	}

	@Override
	public String remove() {
		TipoPessoaHome tipoPessoa = TipoPessoaHome.instance();
		if (tipoPessoa != null) {
			tipoPessoa.getInstance().getTipoProcessoDocumentoTipoPessoaList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(TipoProcessoDocumentoTipoPessoa obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("tipoProcessoDocumentoTipoPessoaGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		// if (action != null) {
		// newInstance();
		// }
		newInstance();
		return action;
	}
}