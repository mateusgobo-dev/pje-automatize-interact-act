package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.EscolaridadeQuery;
import br.jus.pje.nucleo.entidades.Escolaridade;

@Name(EscolaridadeDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EscolaridadeDAO extends GenericDAO implements Serializable, EscolaridadeQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "escolaridadeDAO";

	@SuppressWarnings("unchecked")
	public List<Escolaridade> escolaridadeItems() {
		Query q = getEntityManager().createQuery(ESCOLARIDADE_ITEMS_QUERY);
		return q.getResultList();
	}

}