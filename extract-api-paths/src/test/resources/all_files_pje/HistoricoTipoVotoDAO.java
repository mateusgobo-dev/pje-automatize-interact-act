package br.com.jt.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.jus.pje.jt.entidades.HistoricoTipoVoto;
import br.jus.pje.jt.entidades.Voto;

@Name(HistoricoTipoVotoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class HistoricoTipoVotoDAO extends GenericDAO implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "historicoTipoVotoDAO";

	@SuppressWarnings("unchecked")
	public List<HistoricoTipoVoto> getAllHistoricoTipoVoto(Voto voto){
		Query q = getEntityManager().createQuery("from HistoricoTipoVoto where voto = :voto");
		q.setParameter("voto", voto);
		List<HistoricoTipoVoto> list = q.getResultList();
		return list;
	}

}