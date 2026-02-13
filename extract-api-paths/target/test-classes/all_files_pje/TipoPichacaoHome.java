package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.TipoPichacao;

@Name("tipoPichacaoHome")
@BypassInterceptors
public class TipoPichacaoHome extends AbstractTipoPichacaoHome<TipoPichacao> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	@Override
	public String remove(TipoPichacao obj) {
		String ret = null;
		try {
			ret = super.update();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}
}
