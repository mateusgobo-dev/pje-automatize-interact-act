package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.EstadoCivilDAO;
import br.jus.pje.nucleo.entidades.EstadoCivil;

@Name(EstadoCivilManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EstadoCivilManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estadoCivilManager";

	@In
	private EstadoCivilDAO estadoCivilDAO;

	public List<EstadoCivil> estadoCivilItems() {
		return estadoCivilDAO.estadoCivilItems();
	}

}