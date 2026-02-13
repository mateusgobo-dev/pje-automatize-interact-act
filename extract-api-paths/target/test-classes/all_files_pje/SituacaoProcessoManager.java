package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.SituacaoProcessoDAO;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.Tarefa;

@Name(SituacaoProcessoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SituacaoProcessoManager implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "situacaoProcessoManager";

	@In
	private SituacaoProcessoDAO situacaoProcessoDAO;

	public boolean hasProcessOnTask(String nomeFluxo, String nomeTarefa) {
		Long result = situacaoProcessoDAO.countProcessosByFluxoAndTarefa(nomeFluxo, nomeTarefa);
		return result != null && result > 0;
	}

	public List<Integer> listProcessosByTarefaDocumento(Tarefa t) {
		return situacaoProcessoDAO.listProcessosByTarefaDocumento(t);
	}
	
	public List<Integer> listProcessosByTarefa(Tarefa t) {
		return situacaoProcessoDAO.listProcessosByTarefa(t.getIdTarefa());
	}
	
	public List<Integer> listProcessosByTarefa(Integer idTarefa){
		return situacaoProcessoDAO.listProcessosByTarefa(idTarefa);
	}
	
	public List<String> listTarefasByProcessoSemFiltros(Integer idProcesso){
		return situacaoProcessoDAO.listTarefasByProcessoSemFiltros(idProcesso);
	}
	
	public SituacaoProcesso getById(Long id){
		SituacaoProcesso find = situacaoProcessoDAO.find(SituacaoProcesso.class, id);
		situacaoProcessoDAO.getEntityManager().refresh(find);
		return find;
	}
	
	public SituacaoProcesso getByIdSituacaoIdTarefa(Long idSituacao, Integer idTarefa) {
		SituacaoProcesso find = situacaoProcessoDAO.getByIdSituacaoIdTarefa(idSituacao, idTarefa);
		return find;
	}
	
	public SituacaoProcesso getByIdProcesso(Integer idProcesso){
		return situacaoProcessoDAO.obtemSituacaoByProcesso(idProcesso);
	}
	
	
	public SituacaoProcesso getByIdTaskInstance(Long idTaskInstance){
		return situacaoProcessoDAO.obtemSituacaoByTaskInstance(idTaskInstance);
	}

	public SituacaoProcesso getByIdTaskInstanceLocalizacoes(Long idTaskInstance, List<Integer> idsLocalizacoesFisicas, Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC, boolean isVisualizaSigiloso){
		return situacaoProcessoDAO.obtemSituacaoByTaskInstanceLocalizacoes(idTaskInstance, idsLocalizacoesFisicas, idOrgaoJulgadorColegiado, isServidorExclusivoOJC, isVisualizaSigiloso);
	}
	
	public SituacaoProcesso obtemSituacaoByTaskInstanceOrgaoJulgadorColegiado(Long idTaskInstance, Integer idOrgaoJulgadorColegiado) {
		return situacaoProcessoDAO.obtemSituacaoByTaskInstanceOrgaoJulgadorColegiado(idTaskInstance, idOrgaoJulgadorColegiado);
	}
	
	public SituacaoProcesso getSituacaoProcessoByIdProcessoFluxo(int idProcesso, Fluxo fluxo) {
		return situacaoProcessoDAO.getSituacaoProcessoByIdProcessoFluxo(idProcesso, fluxo);
	}

	public List<SituacaoProcesso> getByProcessoSemFiltros(Integer idProcesso) {
		return situacaoProcessoDAO.getByProcessoSemFiltros(idProcesso);
	}
}