package br.com.infox.cliente.home;

import br.com.infox.ibpm.home.TipoProcessoDocumentoHome;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.AplicacaoClasseTipoProcessoDocumento;

public abstract class AbstractAplicacaoClasseTipoProcessoDocumentoHome<T> extends
		AbstractHome<AplicacaoClasseTipoProcessoDocumento> {

	private static final long serialVersionUID = 1L;

	public void setAplicacaoClasseTipoProcessoDocumentoIdAplicacaoClasseTipoProcessoDocumento(Integer id) {
		setId(id);
	}

	public Integer getAplicacaoClasseTipoProcessoDocumentoIdAplicacaoClasseTipoProcessoDocumento() {
		return (Integer) getId();
	}

	@Override
	protected AplicacaoClasseTipoProcessoDocumento createInstance() {
		AplicacaoClasseTipoProcessoDocumento aplicacaoClasseTipoProcessoDocumento = new AplicacaoClasseTipoProcessoDocumento();

		TipoProcessoDocumentoHome tipoProcessoDocumentoHome = TipoProcessoDocumentoHome.instance();
		if (tipoProcessoDocumentoHome != null) {
			aplicacaoClasseTipoProcessoDocumento.setTipoProcessoDocumento(tipoProcessoDocumentoHome
					.getDefinedInstance());
		}

		AplicacaoClasseHome aplicacaoClasseHome = AplicacaoClasseHome.instance();
		if (aplicacaoClasseHome != null) {
			aplicacaoClasseTipoProcessoDocumento.setAplicacaoClasse(aplicacaoClasseHome.getDefinedInstance());
		}
		return aplicacaoClasseTipoProcessoDocumento;
	}

	@Override
	public String remove() {
		AplicacaoClasseHome aplicacaoClasse = AplicacaoClasseHome.instance();
		if (aplicacaoClasse != null) {
			aplicacaoClasse.getInstance().getAplicacaoClasseTipoProcessoDocumentoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(AplicacaoClasseTipoProcessoDocumento obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("aplicacaoClasseTipoProcessoDocumentoGrid");
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