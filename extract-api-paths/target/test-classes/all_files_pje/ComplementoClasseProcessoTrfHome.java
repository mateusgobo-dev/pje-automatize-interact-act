package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ComplementoClasseProcessoTrf;

@Name("complementoClasseProcessoTrfHome")
@BypassInterceptors
public class ComplementoClasseProcessoTrfHome extends
		AbstractComplementoClasseProcessoTrfHome<ComplementoClasseProcessoTrf> {

	private static final long serialVersionUID = 1L;

	public static ComplementoClasseProcessoTrfHome instance() {
		return ComponentUtil.getComponent("complementoClasseProcessoTrfHome");
	}

	/**
	 * Metodo utilizado para juntar o complemento da classe com o valor do
	 * complemento da classe do processoTrf
	 * 
	 * @param obj
	 * @return complemento
	 */
	public static String getComplementoCompleto(ComplementoClasseProcessoTrf obj) {
		String complemento = "";
		if (obj != null) {
			complemento = obj.getComplementoClasse().getComplementoClasse() + ":"
					+ obj.getValorComplementoClasseProcessoTrf();
		}
		return complemento;
	}

	@Override
	public String update() {
		refreshGrid("complementoClasseProcessoTrfGrid");
		return super.update();
	}

	@Override
	public String persist() {
		refreshGrid("complementoClasseProcessoTrfGrid");
		return super.persist();
	}
}