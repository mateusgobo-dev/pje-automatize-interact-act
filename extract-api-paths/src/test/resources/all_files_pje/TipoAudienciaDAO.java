package br.jus.cnj.pje.business.dao;

import java.util.List;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoAudiencia;

@Name(TipoAudienciaDAO.NAME)
public class TipoAudienciaDAO extends BaseDAO<TipoAudiencia> {
	
	public static final String NAME="tipoAudienciaDAO";

	@Override
	public Object getId(TipoAudiencia e) {
		return e.getIdTipoAudiencia();
	}
	
	public List<TipoAudiencia> getTipoAudienciaList(){
		return entityManager.createQuery("SELECT o FROM TipoAudiencia o WHERE o.ativo = true ORDER BY o.tipoAudiencia", TipoAudiencia.class).getResultList();
	}
}
