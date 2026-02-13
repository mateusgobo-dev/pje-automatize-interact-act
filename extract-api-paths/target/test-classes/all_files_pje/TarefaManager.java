/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Collections;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.TarefaDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial da entidade {@link Tarefa}.
 * 
 * @author cristof
 *
 */
@Name("tarefaManager")
public class TarefaManager extends BaseManager<Tarefa> {
	
	@In
	private TarefaDAO tarefaDAO;

	@Override
	protected TarefaDAO getDAO() {
		return tarefaDAO;
	}
	
	public List<String> nomesTarefasAtuais(ProcessoTrf processo) throws PJeBusinessException{
		return nomesTarefasAtuais(processo.getIdProcessoTrf());
	}
	
	public List<String> nomesTarefasAtuais(Integer idProcesso) throws PJeBusinessException{
		Search search = new Search(SituacaoProcesso.class);
		search.setRetrieveField("idTarefa");
		addCriteria(search, Criteria.equals("idProcesso", idProcesso));
		List<Integer> ids = list(search);
		ids.removeAll(Collections.singleton(null));
		if(ids.isEmpty()){
			return Collections.emptyList();
		}
		search = new Search(Tarefa.class);
		search.setRetrieveField("tarefa");
		addCriteria(search, Criteria.in("idTarefa", ids.toArray(new Integer[ids.size()])));
		return list(search);
	}
	
	public Boolean isTarefaVisivel(String nomeTarefa, Integer idProcessoTrf, List<Integer> idsLocalizacoesFisicas, Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC){
		Boolean tarefaVisivel = Boolean.FALSE;
		
		Search search = new Search(SituacaoProcesso.class);
		search.setRetrieveField("idTarefa");
		
		addCriteria(search, Criteria.equals("idProcesso", idProcessoTrf));
		addCriteria(search, Criteria.equals("nomeTarefa", nomeTarefa));
		
		if(!isServidorExclusivoOJC && CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicas)) {
			addCriteria(search, Criteria.in("idLocalizacao", CollectionUtilsPje.convertListIntegerToListLong(idsLocalizacoesFisicas).toArray()));
		}

		if(idOrgaoJulgadorColegiado != null){
			addCriteria(search, Criteria.equals("idOrgaoJulgadoColegiado", new Long(idOrgaoJulgadorColegiado)));
		}
		List<Integer> ids = list(search);
		
		if(ids != null && !ids.isEmpty()){
			tarefaVisivel = Boolean.TRUE;
		}
		
		return tarefaVisivel;
	}
	
	public Long recuperarIdTaskInstanceByNomeTarefaAndIdProcessoTrf(String nomeTarefa, Integer idProcessoTrf){
		Long idTaskInstance;
		
		Search search = new Search(SituacaoProcesso.class);
		search.setRetrieveField("idTaskInstance");
		
		addCriteria(search, Criteria.equals("idProcesso", idProcessoTrf));
		addCriteria(search, Criteria.equals("nomeTarefa", nomeTarefa));
		
		List<Long> ids = list(search);
		
		if(ids != null && !ids.isEmpty()){
			idTaskInstance = ids.get(0);
		} else {
			idTaskInstance = null;
		}
		
		return idTaskInstance;
	}
	
	public List<Tarefa> findByName(String name){
	  	return tarefaDAO.findByName(name);
	}

}