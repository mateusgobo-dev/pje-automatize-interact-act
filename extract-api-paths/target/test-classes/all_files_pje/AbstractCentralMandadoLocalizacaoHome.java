package br.com.infox.cliente.home;

import br.com.infox.ibpm.home.LocalizacaoHome;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.CentralMandadoLocalizacao;

public abstract class AbstractCentralMandadoLocalizacaoHome<T> extends AbstractHome<CentralMandadoLocalizacao> {

	private static final long serialVersionUID = 1L;

	public void setCentralMandadoLocalizacaoIdCentralMandadoLocalizacao(Integer id) {
		setId(id);
	}

	public Integer getCentralMandadoLocalizacaoIdCentralMandadoLocalizacao() {
		return (Integer) getId();
	}

	@Override
	protected CentralMandadoLocalizacao createInstance() {
		CentralMandadoLocalizacao centralMandadoLocalizacao = new CentralMandadoLocalizacao();

		CentralMandadoHome centralMandadoHome = CentralMandadoHome.instance();
		if (centralMandadoHome != null) {
			centralMandadoLocalizacao.setCentralMandado(centralMandadoHome.getDefinedInstance());
		}

		LocalizacaoHome localizacaoHome = LocalizacaoHome.instance();
		if (localizacaoHome != null) {
			centralMandadoLocalizacao.setLocalizacao(localizacaoHome.getDefinedInstance());
		}
		return centralMandadoLocalizacao;
	}

	@Override
	public String remove() {
		CentralMandadoHome centralMandado = CentralMandadoHome.instance();
		if (centralMandado != null) {
			centralMandado.getInstance().getCentralMandadoLocalizacaoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(CentralMandadoLocalizacao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("centralMandadoLocalizacao Grid");
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