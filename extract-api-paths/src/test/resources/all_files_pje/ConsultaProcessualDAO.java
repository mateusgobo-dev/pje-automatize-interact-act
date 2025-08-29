package br.jus.cnj.pje.business.dao;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name("consultaProcessualDAO")
@AutoCreate
public class ConsultaProcessualDAO extends BaseDAO<ProcessoTrf> implements Serializable{

	/**
	 * Metodo responsavel por realizar a pesquisa no banco de dados 
	 * a partir dos parametros informados.
	 * 
	 * @param criterias
	 * @param ordenacao
	 * @param quantidadeMaxima
	 * @return
	 */
	public List<Integer> getIdProcessosByCriterias(List<Criteria> criterias, Map<String, Order> ordenacao, Integer quantidadeMaxima){
		
		Search search = new Search(ProcessoTrf.class);
		search.setRetrieveField("idProcessoTrf");
		search.setGroupBy("o.idProcessoTrf");

		if (quantidadeMaxima != null) {
			search.setMax(quantidadeMaxima);
		}
		
		if (ordenacao != null) {
			for (Map.Entry<String,Order> entry : ordenacao.entrySet()) {
				search.addOrder(entry.getKey(), entry.getValue());
			}
		}
		
		try {
			search.addCriteria(criterias);
			
			return list(search); 
			
		} catch (NoSuchFieldException e) {
			logger.error(e);
			return Collections.emptyList();
		}
	}
	
	@Override
	public Object getId(ProcessoTrf e) {
		return e.getIdProcessoTrf();
	}
	
}
