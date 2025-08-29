package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoDocumentoEstruturado;

@Name("tipoDocumentoEstruturadoDAO")
public class TipoDocumentoEstruturadoDAO extends BaseDAO<TipoDocumentoEstruturado>{

	@Override
	public Object getId(TipoDocumentoEstruturado e) {
		return e.getIdTipoDocumentoEstruturado();
	}
	
	@SuppressWarnings("unchecked")
	public TipoDocumentoEstruturado findByNamespace(String namespace){
		
		String sql = "select o from TipoDocumentoEstruturado o where o.namespace = :namespace";
		
		Query query = getEntityManager().createQuery(sql);
		query.setParameter("namespace", namespace);
		
		List<TipoDocumentoEstruturado> resultList = query.getResultList();
		
		if(resultList != null && !resultList.isEmpty()){
			return resultList.get(0);
		}
		
		return null;
	}
	
}
