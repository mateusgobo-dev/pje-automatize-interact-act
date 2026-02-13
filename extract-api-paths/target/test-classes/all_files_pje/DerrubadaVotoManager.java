package br.com.jt.pje.manager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoVotoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.service.SessaoJulgamentoService;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.util.DateUtil;


@Name(DerrubadaVotoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class DerrubadaVotoManager extends BaseManager<SessaoProcessoDocumentoVoto> {
    
	public static final String NAME = "derrubadaVotoManager";
	
	@In
	private VotoManager votoManager;
	
	@In
	private SessaoProcessoDocumentoVotoDAO sessaoProcessoDocumentoVotoDAO;
    
	@Override
	protected BaseDAO<SessaoProcessoDocumentoVoto> getDAO() {
		return sessaoProcessoDocumentoVotoDAO;
	}
	
    
    /**
     * Analisa ações relacionadas ao fluxo de voto derrubado em função de um voto que acabou de ser persistido.
     * Caso seja um voto de relator, verifica a necessidade de criação de fluxo de derrubada para os vogais que já votaram.
     * Caso seja um voto de um vogal, então verifica a necessidade de finalização do fluxo de derrubada para esse vogal.
     * 
     * @param voto voto que acabou de ser persistido
     * @throws PJeBusinessException
     */
    public void analisarTramitacaoFluxoVotoDerrubado(SessaoProcessoDocumentoVoto voto) throws PJeBusinessException{
    	if (voto != null && isExisteFluxoVotoDerrubadoParametrizado() ){
        	if (isVotoRelatorProcesso(voto)){
        		tramitarFluxoVotoDerrubado(voto.getProcessoTrf());
        	} else if (isFinalizarTarefaDerrubadaVotoAoVotar()){
       			finalizarTarefaVotoDerrubadoNaoRelator(voto);
        	}
    	}
    }
    
    
	/**
     * Dado um voto, retorna true caso ele tenha sido proferido pelo relator do processo. 
     * @param voto em questão
     * @return true caso tenha sido proferido pelo relator o processo.
     */
    private Boolean isVotoRelatorProcesso(SessaoProcessoDocumentoVoto voto){
    	return voto != null && voto.getOrgaoJulgador() != null && voto.getOrgaoJulgador().equals(voto.getProcessoTrf().getOrgaoJulgador());
    }
    
    
    /**
     * Retorna true caso exista parametro de sistema relacioando a finalizacao automática de tarefa de 
     * derrubada de voto e o mesmo esteja configurado como "true". 
     * @return true caso parametro mencionado esteja configurado como true.
     */
    private Boolean isFinalizarTarefaDerrubadaVotoAoVotar(){
		try {
			return "true".equals(ParametroUtil.getParametro(Parametros.PJE_FLUXO_VOTO_DERRUBADO_EXCLUI_TAREFA_AO_VOTAR).trim()); 
		}
		catch (Exception e){
			return false;
		}
    }
    
    
	/**
	 * Metodo que tramita o processo trf para a tarefa de Voto Derrubado
	 * @param processoTrf ProcessoTrf processo para o qual o flux de derrubada de voto será gerado.
	 * @throws PJeBusinessException
	 */
	private void tramitarFluxoVotoDerrubado(ProcessoTrf processoTrf) throws PJeBusinessException {
		String codigo = ParametroUtil.getParametro(Variaveis.VARIAVEL_FLUXO_VOTO_DERRUBADO);
		SessaoJulgamentoService sessaoJulgamentoService = ComponentUtil.getComponent("sessaoJulgamentoServiceCNJ");

		if (JbpmUtil.isExecutandoEmFrameTarefa()){
			sessaoJulgamentoService.criarFluxoVotoDerrubadoAssincrono(processoTrf, codigo);
		} else {
			sessaoJulgamentoService.criarFluxoVotoDerrubado(processoTrf, codigo);
		}

		StringBuilder mensagemLog = new StringBuilder();
		mensagemLog.append(" Processo [").append(processoTrf.getNumeroProcesso()).append("]");
		mensagemLog.append(" tramitado para a tarefa ").append(Variaveis.VARIAVEL_FLUXO_VOTO_DERRUBADO).append(" com sucesso!");
		logger.info(mensagemLog.toString());
	}
	
	
    /**
     * Finaliza tarefa de voto derrubado relacionada ao voto em questão proferio por um não relator 
     * @param voto voto de um não relator
     */
    private void finalizarTarefaVotoDerrubadoNaoRelator(SessaoProcessoDocumentoVoto voto) {
		SessaoJulgamentoService sessaoJulgamentoService = ComponentUtil.getComponent("sessaoJulgamentoServiceCNJ");
		sessaoJulgamentoService.finalizarTarefaDerrubadaVoto (voto);
	}

    
	
	
	/**
	 * Funciona da mesma forma que o método <code>verificaValidadeVotoRelator(SessaoProcessoDocumentoVoto, SessaoProcessoDocumentoVoto)</code>
	 * Com a diferença de que infere o voto do relator e o voto do NÃO relator
	 * através do parametro <code>processoPautado</code> e de informações do usuário logado.  
	 * 
	 * @param processoPautado objeto representando um processo pautado em uma sessão de julgamento
	 * 
	 * @see verificaValidadeVotoRelator(SessaoProcessoDocumentoVoto, SessaoProcessoDocumentoVoto)
	 */
	public String verificaValidadeVotoRelator(SessaoPautaProcessoTrf processoPautado) {
		SessaoProcessoDocumentoVoto votoRelator = votoManager.getVotoRelator(processoPautado);
		SessaoProcessoDocumentoVoto votoProprio = votoManager.getVotoProprio(processoPautado);
		return verificaValidadeVotoRelator(votoRelator, votoProprio);
	}
	
	
	/**  
	 * Dado o voto de um relator e o voto de um não relator, baseado na data dos votos, retorna uma mensagem
	 * caso o voto do NÃO relator esteja desatualizado em relação ao voto do relator,
	 * @param votoRelator
	 * @param votoNaoRelator
	 * @return possível mensagem informando que o voto do não relator está desatualizado
	 */
	public String verificaValidadeVotoRelator(SessaoProcessoDocumentoVoto votoRelator, SessaoProcessoDocumentoVoto votoNaoRelator) {
		String retorno = "";
		
		if (!verificarVotosValidosMostrarMensagem(votoRelator, votoNaoRelator)){
			return retorno;
		}
		
		if(isVotoRelatorAlterado(votoNaoRelator, votoRelator)) {
			retorno = "Voto do Relator alterado em <br/>"+DateUtil.dateHourToString(votoRelator.getDtVoto());
		}
		
		return retorno;
	}
	
	
	/**
	 * Método que retorna true casos as instancias de voto passadas sejam validas para posterior
	 * análise de mensagem para voto expirado 
	 * @param votoRelator voto proferido pelo relator do processo
	 * @param votoNaoRelator voto proferido por um não relator do processo 
	 * @return true caso as instancias de votos sejam válidas
	 */
	private Boolean verificarVotosValidosMostrarMensagem(SessaoProcessoDocumentoVoto votoRelator, SessaoProcessoDocumentoVoto votoNaoRelator){
		if (votoRelator == null || votoNaoRelator == null){
			return false;
		}
		return (votoRelator.getLiberacao() && !votoNaoRelator.getImpedimentoSuspeicao());
	}
	
	
	/**
	 * Verifica se o voto do relator foi alterado.
	 * @param votoProprio Votante
	 * @param votoRelator Votante Relator
	 * @return true se o votoProprio for anterior ao voto do relator
	 */
	private boolean isVotoRelatorAlterado(SessaoProcessoDocumentoVoto votoProprio, SessaoProcessoDocumentoVoto votoRelator) {
		return votoRelator != null && DateUtil.isDataComHoraMenor(votoProprio.getDtVoto(),votoRelator.getDtVoto());
	}
	
	
	/**
	 * Verifica se existe fluxo de voto derrubado cadastrado no sitema
	 * @return True se existir o fluxo False se o fluxo nao existir
	 */
	private Boolean isExisteFluxoVotoDerrubadoParametrizado() {
		return ParametroUtil.getParametro(Variaveis.VARIAVEL_FLUXO_VOTO_DERRUBADO) != null ? true : false;
	}
	
	public static DerrubadaVotoManager instance() {
    	return ComponentUtil.getComponent(NAME);
    }
	
}
