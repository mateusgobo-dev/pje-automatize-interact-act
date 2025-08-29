package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.EtniaQuery;
import br.jus.pje.nucleo.entidades.Etnia;

@Name(EtniaDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EtniaDAO extends GenericDAO implements Serializable, EtniaQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "etniaDAO";

	@SuppressWarnings("unchecked")
	public List<Etnia> etniaItems() {
		Query q = getEntityManager().createQuery(ETNIA_ITEMS_QUERY);
		return q.getResultList();
	}

}