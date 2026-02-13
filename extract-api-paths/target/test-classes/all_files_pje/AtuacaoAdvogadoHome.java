package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.AtuacaoAdvogado;

@Name(AtuacaoAdvogadoHome.NAME)
@BypassInterceptors
public class AtuacaoAdvogadoHome extends AbstractAtuacaoAdvogadoHome<AtuacaoAdvogado> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "atuacaoAdvogadoHome";

	public static AtuacaoAdvogadoHome instance() {
		return ComponentUtil.getComponent(AtuacaoAdvogadoHome.NAME);
	}

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
	public String remove(AtuacaoAdvogado obj) {
		String ret = null;
		try {
			ret = super.update();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	@Override
	public boolean isEditable() {
		return ParametroUtil.instance().getPermitirCadastrosBasicos();
	}

}
