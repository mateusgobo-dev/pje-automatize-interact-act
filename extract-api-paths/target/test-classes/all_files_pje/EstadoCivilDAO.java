package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.EstadoCivilQuery;
import br.jus.pje.nucleo.entidades.EstadoCivil;

@Name(EstadoCivilDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EstadoCivilDAO extends GenericDAO implements Serializable, EstadoCivilQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estadoCivilDAO";

	@SuppressWarnings("unchecked")
	public List<EstadoCivil> estadoCivilItems() {
		Query q = getEntityManager().createQuery(ESTADO_CIVIL_ITEMS_QUERY);
		return q.getResultList();
	}

}