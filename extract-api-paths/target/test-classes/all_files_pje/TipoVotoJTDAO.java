package br.com.jt.pje.dao;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import br.com.infox.core.dao.GenericDAO;
import br.com.jt.pje.query.TipoVotoQuery;
import br.jus.pje.jt.entidades.TipoVotoJT;

@Name(TipoVotoJTDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class TipoVotoJTDAO extends GenericDAO implements TipoVotoQuery, Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoVotoJTDAO";

	@SuppressWarnings("unchecked")
	public List<TipoVotoJT> getTipoVotoRelator(){
		Query q = getEntityManager().createQuery(TIPO_VOTO_RELATOR_QUERY);
		List<TipoVotoJT> list = q.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	public List<TipoVotoJT> getTipoVotoVogal(){
		Query q = getEntityManager().createQuery(TIPO_VOTO_VOGAL_QUERY);
		List<TipoVotoJT> list = q.getResultList();
		return list;
	}

}