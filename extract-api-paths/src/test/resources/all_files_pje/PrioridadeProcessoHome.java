package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.enums.SexoAmbosEnum;

@Name("prioridadeProcessoHome")
@BypassInterceptors
public class PrioridadeProcessoHome extends AbstractPrioridadeProcessoHome<PrioridadeProcesso> {

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

	public SexoAmbosEnum getSexoAmbosEnumValues() {
		return SexoAmbosEnum.A;
	}

	public static PrioridadeProcessoHome instance() {
		return ComponentUtil.getComponent("prioridadeProcessoHome");
	}

}