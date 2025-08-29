package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioFiltroDTO;
import br.jus.pje.nucleo.entidades.CriterioFiltro;

@Name(CriterioFiltroDAO.NAME)
public class CriterioFiltroDAO extends BaseDAO<CriterioFiltro>{
	
	public static final String NAME = "criterioFiltroDAO";
	
	@Override
	public Object getId(CriterioFiltro e) {
		return e.getId();
	}
	
	public void excluirPorIdFiltro(Integer idFiltro) {
		String hql = "delete from CriterioFiltro o where o.filtro.id = :id";
		
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("id",idFiltro);
		q.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public List<CriterioFiltroDTO> recuperarCriteriosPorIdFiltro(Integer idFiltro){
		StringBuilder sb = new StringBuilder("");
		
		sb.append("SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioFiltroDTO(cf.id, cf.tipoCriterio, cf.valorCriterio, cf.textoCriterio, cf.filtro.id) ");
		sb.append("FROM CriterioFiltro cf WHERE cf.filtro.id = :idFiltro ");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		q.setParameter("idFiltro", idFiltro);
		
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<CriterioFiltro> recuperarCriteriosEntitiesPorIdFiltro(Integer idFiltro){
		StringBuilder sb = new StringBuilder("");
		
		sb.append("SELECT cf FROM CriterioFiltro cf WHERE cf.filtro.id = :idFiltro ");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		q.setParameter("idFiltro", idFiltro);
		
		return q.getResultList();
	}
}
