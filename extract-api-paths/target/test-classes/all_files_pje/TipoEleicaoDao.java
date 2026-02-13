package br.jus.je.pje.persistence.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.infox.core.dao.GenericDAO;
import br.jus.pje.je.entidades.TipoEleicao;

@Name(TipoEleicaoDao.NAME)
@AutoCreate
public class TipoEleicaoDao extends GenericDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "tipoEleicaoDao";
	
	@SuppressWarnings("unchecked")
	public List<TipoEleicao> tipoEleicaoList() {
		Query query = getEntityManager().createQuery("select o from TipoEleicao o");
		return query.getResultList();
	}
	
}