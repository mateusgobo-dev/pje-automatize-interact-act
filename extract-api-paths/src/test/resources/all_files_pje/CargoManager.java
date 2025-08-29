package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.CargoDAO;
import br.jus.pje.nucleo.entidades.Cargo;

/**
 * Classe manager para Cargo
 * 
 * @author Allan
 * 
 */
@Name(CargoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class CargoManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "cargoManager";

	@In
	private CargoDAO cargoDAO;

	public List<Cargo> cargoItems() {
		return cargoDAO.cargoItems();
	}

}