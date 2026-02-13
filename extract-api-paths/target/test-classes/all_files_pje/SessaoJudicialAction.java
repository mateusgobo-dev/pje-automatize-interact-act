package br.jus.cnj.pje.view.fluxo;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;


/**
 * Classe destinada a manipulação/recuperação 
 * de dados referentes a julgamentos em sessão colegiada
 * de um dado processo, recuperado através de parâmetro.
 * @author Rodrigo Santos Menezes
 *
 */
@Name(SessaoJudicialAction.NAME)
@Scope(ScopeType.EVENT)
public class SessaoJudicialAction {
	
	public static final String NAME = "sessaoJudicialAction";
	
	@Logger
	private Log logger;	
	
	@RequestParameter
	private Integer id;

	@RequestParameter
	private Integer idProcesso;
	
	@In(create = false, required = false)
	private ProcessInstance processInstance;
	
	@In
	private transient ProcessoJudicialService processoJudicialService;	
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;
	
	private ProcessoTrf processoJudicial;

	@Create
	public void init(){
		
		if (processInstance != null && processInstance.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO) != null){
			try{
				logger.trace("Tentando recuperar o processo judicial pela instância de fluxo.");
				processoJudicial = processoJudicialService.findByProcessInstance(processInstance);
				logger.trace("Processo judicial {0} recuperado.", processoJudicial.getNumeroProcesso());
			} catch (PJeBusinessException e){
				facesMessages.add(Severity.WARN,
						"Não foi possível obter o processo judicial vinculado à instância de processo {0}.", processInstance.getId());
				return;
			}
		}else if ((idProcesso != null) || (id != null)){
			Integer idCorreto = idProcesso != null ? idProcesso : id;
			try{
				processoJudicial = processoJudicialService.findById(idCorreto);
			} catch (PJeBusinessException e){
				facesMessages.add(Severity.WARN, "Não foi possível obter o processo judicial com id {0}.", idCorreto);
				return;
			}
		}		
		
	}
	
	/**
	 * Este método verifica se o processo foi adiado ou pedido vista
	 * na última sessão em que foi pautado
	 * Este método é utilizado em ELs de fluxo
	 * @return
	 */
	public boolean processoAdiadoOuVista(){
		boolean retorno = false;
		
		SessaoPautaProcessoTrf sppt = sessaoPautaProcessoTrfManager.recuperaUltimaPautaProcesso(processoJudicial,true);
		if(sppt != null){
			retorno = true;
		}

		return retorno;
	}

	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}
	
	public void setProcessoJudicial(ProcessoTrf processoJudicial) {
		this.processoJudicial = processoJudicial;
	}
}
