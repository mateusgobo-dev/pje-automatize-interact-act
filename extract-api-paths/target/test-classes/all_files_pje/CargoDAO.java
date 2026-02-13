package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.CargoQuery;
import br.jus.pje.nucleo.entidades.Cargo;

/**
 * Classe que realiza as consultas das NamedQueries e quaisquer outras consultas
 * da entidade de Cargo.
 * 
 * @author Allan
 * 
 */
@Name(CargoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class CargoDAO extends GenericDAO implements Serializable, CargoQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "cargoDAO";

	@SuppressWarnings("unchecked")
	public List<Cargo> cargoItems() {
		Query q = getEntityManager().createQuery(CARGO_ITEMS_QUERY);
		List<Cargo> result = q.getResultList();
		return result;
	}

}