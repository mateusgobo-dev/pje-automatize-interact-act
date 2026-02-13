/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.ProcessoVisibilidadeSegredoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoVisibilidadeSegredo;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.search.Criteria;

@Name("processoVisibilidadeSegredoManager")
public class ProcessoVisibilidadeSegredoManager extends BaseManager<ProcessoVisibilidadeSegredo>{

	private static final long serialVersionUID = 1L;
	@In
	private ProcessoVisibilidadeSegredoDAO processoVisibilidadeSegredoDAO;

	@Override
	protected ProcessoVisibilidadeSegredoDAO getDAO(){
		return processoVisibilidadeSegredoDAO;
	}

	public boolean visivel(ProcessoTrf processoJudicial, Usuario u){
		return processoVisibilidadeSegredoDAO.visivel(processoJudicial, u);
	}
	
	public boolean visivel(ProcessoTrf processoJudicial, Usuario u, Procuradoria procuradoria) {
		return processoVisibilidadeSegredoDAO.visivel(processoJudicial, u, procuradoria);
	}

	public void limpaRegistros(ProcessoTrf processo) throws PJeBusinessException {
		processoVisibilidadeSegredoDAO.limpaRegistros(processo);
	}

	public ProcessoVisibilidadeSegredo criar(Pessoa p, ProcessoTrf processo, Procuradoria procuradoria) {
		ProcessoVisibilidadeSegredo ret = new ProcessoVisibilidadeSegredo();
		ret.setPessoa(p);
		ret.setProcesso(processo.getProcesso());
		if (procuradoria!= null && procuradoria.getAtivo()) {
			ret.setProcuradoria(procuradoria);
		}
		return ret;
	}
	
    @Observer(Eventos.ALTERACAO_PROCURADORIA_PESSOA)
    public void observadorAlteracaoProcuradoriaPessoa(Map<String, Object> payload) {
		String tipoAlteracao = (String) (payload.get("tipoAlteracao"));
    	int idPessoa = (Integer) (payload.get("idPessoa"));
		int idProcuradoria = (Integer) (payload.get("idProcuradoria"));
		int idProcuradoriaPadrao = (Integer) payload.get("idProcuradoriaPadrao");
		
		Pessoa pessoa = null;
		if(idPessoa != 0 ){
			pessoa = EntityUtil.find(Pessoa.class, idPessoa);
		}
		Procuradoria procuradoria = null;
		if(idProcuradoria != 0 ){
			procuradoria = EntityUtil.find(Procuradoria.class, idProcuradoria);
		}
		Procuradoria procuradoriaPadrao = null;
		if(idProcuradoriaPadrao != 0 ){
			procuradoriaPadrao = EntityUtil.find(Procuradoria.class, idProcuradoriaPadrao);
		}
    	this.atualizarSegredo(tipoAlteracao, pessoa, procuradoria, procuradoriaPadrao);
    	
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Visibilidade segredo do processo com procuradoria alterada.");
    }

    @Observer(Eventos.ALTERACAO_PROCURADORIA_PARTE)
    public void observadorAlteracaoProcuradoriaParte(Map<String, Object> payload) {
		int idPessoa = ((Integer) payload.get("idPessoa"));
		int idProcuradoriaNova = ((Integer) payload.get("idProcuradoriaNova"));
		int idProcuradoriaAnterior = (Integer) payload.get("idProcuradoriaAnterior");
		int idProcesso = ((Integer) payload.get("idProcesso"));

		if(idProcuradoriaAnterior != idProcuradoriaNova){
			Pessoa pessoa = null;
			if(idPessoa != 0 ){
				pessoa = EntityUtil.find(Pessoa.class, idPessoa);
			}
			Procuradoria procuradoriaNova = null;
			if(idProcuradoriaNova != 0 ){
				procuradoriaNova = EntityUtil.find(Procuradoria.class, idProcuradoriaNova);
			}
			Procuradoria procuradoriaAnterior = null;
			if(idProcuradoriaAnterior != 0 ){
				procuradoriaAnterior = EntityUtil.find(Procuradoria.class, idProcuradoriaAnterior);
			}
			ProcessoTrf processo = null;
			if(idProcesso != 0 ){
				processo = EntityUtil.find(ProcessoTrf.class, idProcesso);
			}
		
			this.atualizarSegredoProcessoEspecifico(pessoa, procuradoriaNova, procuradoriaAnterior, processo);
	    	
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Visibilidade segredo do processo parte com procuradoria alterada.");			
		}
    }
    
    /**
     * Altera o relacionamento de pessoas e procuradorias em um processo especifico
     * - só pode alterar do registro cuja procuradoria anterior foi alterado ou que não tinha procuradoria anterior
     * 
     * @param pessoa
     * @param procuradoria
     * @param processo
     */
	private void atualizarSegredoProcessoEspecifico(Pessoa pessoa, Procuradoria procuradoriaNova, Procuradoria procuradoriaAnterior, ProcessoTrf processo) {
		if(procuradoriaAnterior != null){
			this.setProcuradoria(processoVisibilidadeSegredoDAO.
					recuperaProcessoVisibilidadeSegredoPessoaProcuradoriaProcesso(pessoa, procuradoriaAnterior, processo), procuradoriaNova);			
		}
		
		this.setProcuradoria(processoVisibilidadeSegredoDAO.
					recuperaProcessoVisibilidadeSegredoPessoaProcessoSemProcuradoria(pessoa, processo), procuradoriaNova);
	}
	/**
	 * Recebe o tipo de alteracao realizada no relacionamento de pessoa e procuradoria e faz a alteracao nos relacionamentos dessas 
	 * entidades na lista de visibilidades dos processos
	 * 
	 * @param tipoAlteracao
	 * @param pessoa
	 * @param procuradoria
	 * @param procuradoriaPadrao
	 */
	private void atualizarSegredo(String tipoAlteracao, Pessoa pessoa, Procuradoria procuradoria, Procuradoria procuradoriaPadrao) {
		if(tipoAlteracao == "ADICIONA"){
			this.setProcuradoria(processoVisibilidadeSegredoDAO.recuperaProcessoVisibilidadeSegredoPessoaSemProcuradoria(pessoa), procuradoria);			
		}else{
			if(procuradoria != null && procuradoria.getIdProcuradoria() != 0){
				this.setProcuradoria(processoVisibilidadeSegredoDAO.recuperaProcessoVisibilidadeSegredoPessoaProcuradoria(pessoa, procuradoria), procuradoriaPadrao);
			}
		}
	}
	
	/**
	 * Altera a identificacao de qual a procuradoria vinculada a pessoa na lista de visibilidades dos processos
	 * 
	 * @param pvsList
	 * @param procuradoria
	 */
	private void setProcuradoria (List<ProcessoVisibilidadeSegredo> pvsList, Procuradoria procuradoria){
		if (!pvsList.isEmpty()) {
			for (ProcessoVisibilidadeSegredo pvs : pvsList) {
				pvs.setProcuradoria(procuradoria);
			}
			try {
				flush();
			} catch (PJeBusinessException e) {
				logger.error(Severity.FATAL, "Erro ao persistir informacao de visibilidade de procuraria: {0}.",
						e.getLocalizedMessage());
			}
		}
	}
	public void atualizaProcuradoriaSegredo (ProcessoVisibilidadeSegredo pvs, Procuradoria procuradoria){
		pvs.setProcuradoria(procuradoria);
		try {
				flush();
		} catch (PJeBusinessException e) {
			logger.error(Severity.FATAL, "Erro ao persistir informacao de visibilidade de procuraria: {0}.",
					e.getLocalizedMessage());
		}
	}
	public long contagemVisualizadores(ProcessoTrf processo) throws PJeBusinessException{
		return processoVisibilidadeSegredoDAO.contagemVisualizadores(processo);
	}
	
	public List<ProcessoVisibilidadeSegredo> recuperarVisualizadores(ProcessoTrf processo, Integer first, Integer max) throws PJeBusinessException {
		return processoVisibilidadeSegredoDAO.recuperaVisualizadores(processo, first, max);
	}
	
	/**
	 * Método responsável por retornar a visibilidade atribuida ao processo do usuário logado
	 * retornando uma lista com os IDs dos processos que o usuario tem visibilidade.
	 * @return uma lista de inteiros com os ids dos processos 
	 */
	public List<Integer> recuperaVisibilidadeAtribuidaProcessoUsuarioLogado(){
		return processoVisibilidadeSegredoDAO.recuperaVisibilidadeAtribuidaProcessoUsuarioLogado();
	}

	public ProcessoVisibilidadeSegredo recuperaProcessoVisibilidadeSegredo(Pessoa pessoa, ProcessoTrf processo) throws PJeBusinessException{
		return processoVisibilidadeSegredoDAO.recuperaProcessoVisibilidadeSegredo(pessoa, processo);
	}
	
	/**
	 * Resgata os processos que o usuário tem acesso de visualização
	 * Ex.: idProcessoTrf in (659, 660, 998) 
	 * @return uma <b>Criteria</b> contendo os ID's dos processos que o usuário faz parte.
	 */
	public Criteria consultaCriteriosVisibilidadeAtribuidaProcesso() { 
		List<Integer> processosComVisualizacao = recuperaVisibilidadeAtribuidaProcessoUsuarioLogado();
		
		Criteria visibilidadeProcessos = null;
		if (processosComVisualizacao != null && processosComVisualizacao.size() > 0){
			visibilidadeProcessos = Criteria.in("idProcessoTrf",processosComVisualizacao.toArray());
		}
		return visibilidadeProcessos;
	}

	public void removerVisualizadorProcessoSigilosoNoOrgaoJulgador(Integer idUsuario, Integer idOrgaoJulgador) {
		processoVisibilidadeSegredoDAO.removerVisualizadorProcessoSigilosoNoOrgaoJulgador(idUsuario, idOrgaoJulgador);
	}
}
