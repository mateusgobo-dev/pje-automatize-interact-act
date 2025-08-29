package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.infox.ibpm.home.TipoModeloDocumentoHome;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PeticaoTipoModeloDocumento;

public abstract class AbstractPeticaoTipoModeloDocumentoHome<T> extends AbstractHome<PeticaoTipoModeloDocumento> {

	private static final long serialVersionUID = 1L;

	public void setPeticaoTipoModeloDocumentoIdPeticaoTipoModeloDocumento(Integer id) {
		setId(id);
	}

	public Integer getPeticaoTipoModeloDocumentoIdPeticaoTipoModeloDocumento() {
		return (Integer) getId();
	}

	@Override
	protected PeticaoTipoModeloDocumento createInstance() {
		PeticaoTipoModeloDocumento peticaoTipoModeloDocumento = new PeticaoTipoModeloDocumento();
		PeticaoHome peticaoHome = (PeticaoHome) Component.getInstance("peticaoHome", false);
		if (peticaoHome != null) {
			peticaoTipoModeloDocumento.setPeticao(peticaoHome.getDefinedInstance());
		}
		TipoModeloDocumentoHome tipoModeloDocumentoHome = (TipoModeloDocumentoHome) Component.getInstance(
				"tipoModeloDocumentoHome", false);
		if (tipoModeloDocumentoHome != null) {
			peticaoTipoModeloDocumento.setTipoModeloDocumento(tipoModeloDocumentoHome.getDefinedInstance());
		}
		return peticaoTipoModeloDocumento;
	}

	@Override
	public String remove() {
		PeticaoHome peticao = (PeticaoHome) Component.getInstance("peticaoHome", false);
		if (peticao != null) {
			peticao.getInstance().getPeticaoTipoModeloDocumentoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(PeticaoTipoModeloDocumento obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("peticaoTipoModeloDocumentoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		return action;
	}

}