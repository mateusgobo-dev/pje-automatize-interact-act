package br.com.infox.cliente.home;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Cargo;

@Name(CargoHome.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class CargoHome extends AbstractHome<Cargo> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "cargoHome";

	public void setCargoIdCargo(Integer id) {
		setId(id);
	}

	public Integer getCargoIdCargo() {
		return (Integer) getId();
	}

	public static CargoHome instance() {
		return (CargoHome) Component.getInstance(NAME);
	}

}