/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.CaixaRepresentante;

@Name(CaixaRepresentanteDAO.NAME)
public class CaixaRepresentanteDAO extends BaseDAO<CaixaRepresentante> implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "caixaRepresentanteDAO";
	
	@Override
	public Object getId(CaixaRepresentante e) {
		return e.getIdCaixaRepresentante();
	}
	
	@SuppressWarnings("unchecked")
	public List<CaixaRepresentante> getCaixasRepresentantes(Integer idPessoaFisica, Integer idCaixaAdvogadoProcurador) {
		StringBuilder query = new StringBuilder("SELECT o FROM CaixaRepresentante AS o WHERE 1=1 ");
		Map<String, Object> mapParametros = new HashMap<String, Object>();
		
		if(idPessoaFisica != null) {
			query.append(" AND o.representante.idPessoa = :idPessoa ");
			mapParametros.put("idPessoa", idPessoaFisica);
		}
		
		if(idCaixaAdvogadoProcurador != null) {
			query.append(" AND o.caixaAdvogadoProcurador.idCaixaAdvogadoProcurador = :idCaixaAdvogadoProcurador ");
			mapParametros.put("idCaixaAdvogadoProcurador", idCaixaAdvogadoProcurador);
		}
		
		Query q = EntityUtil.getEntityManager().createQuery(query.toString());
		if(!mapParametros.isEmpty()) {
			for(Entry<String, Object> parametro : mapParametros.entrySet()) {
				q.setParameter(parametro.getKey(), parametro.getValue());
			}
		}
		
		return q.getResultList();
	}
	
}
