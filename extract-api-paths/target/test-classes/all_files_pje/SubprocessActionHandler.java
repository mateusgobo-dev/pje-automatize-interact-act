/**
 * pje-web
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.com.infox.ibpm.jbpm;

import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;

import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ProcessoInstanceManager;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoInstance;

/**
 * Componente responsável por observar eventos ocorridos na criação e destruição de instâncias
 * de fluxo disparadas por nós de subprocesso.
 * 
 * @author Infox Tecnologia
 * @author Thiago de Andrade Vieira (@cnj)
 * @author Paulo Cristovão Filho (@cnj)
 *
 */
@Name("subprocessActionHandler")
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class SubprocessActionHandler {

	/**
	 * Observa o evento de criação de um subprocesso.
	 * 
	 * @param ctx o contexto de execução de fluxo em que o subprocesso foi criado
	 * 
	 */
	@Observer(Event.EVENTTYPE_SUBPROCESS_CREATED)
	public void copyVariablesToSubprocess(ExecutionContext ctx) {
		try {
			Token token = ctx.getToken();
			Map<String, Object> variables = token.getProcessInstance().getContextInstance().getVariables();
			Integer idProcesso = (Integer) token.getProcessInstance().getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO);
			ProcessInstance spi = token.getSubProcessInstance();			
			ProcessoInstanceManager piManager = (ProcessoInstanceManager)Component.getInstance(ProcessoInstanceManager.class);
            ProcessoInstance principal = piManager.findById(token.getProcessInstance().getId());
            if (principal != null) {
            	// cria o subprocesso com  as informações de deslocamento do fluxo principal.
				ProcessoHome.insereProcessoInstance(idProcesso, spi.getId(),
						principal.getIdLocalizacao(),
						principal.getOrgaoJulgadorColegiado(),
						principal.getOrgaoJulgadorCargo());
            } else {
            	ProcessoHome.insereProcessoInstance(idProcesso, spi.getId());
            }
			spi.getContextInstance().addVariables(variables);
			EntityUtil.getEntityManager().flush();
		} catch (Exception ex) {
			throw new AplicationException(AplicationException.createMessage("copiar variaveis para o subprocesso",
					"copyVariablesToSubprocess()", "SubprocessoActionHandler", "BPM"));
		}
	}
	
	/**
	 * Observa o evento de início de processo de negócio (fluxo),
	 * inserindo a variável "processo" com o identificador do {@link Processo}
	 * que estiver definido em {@link ProcessoHome#getInstance()}, 
	 * assim como outras variáveis que estiverem definidas no contexto de evento sob o nome de "pje:fluxo:variables:startState".
	 * 
	 */
	@Observer(Event.EVENTTYPE_PROCESS_START)
	public void createVariablesToProcess(ExecutionContext ctx) {
		ctx.getProcessInstance().getContextInstance().setVariable(Variaveis.VARIAVEL_PROCESSO,
				ProcessoHome.instance().getInstance().getIdProcesso());

		@SuppressWarnings("unchecked")
		Map<String, Object> flxvars = (Map<String, Object>) Contexts.getEventContext().get(Variaveis.PJE_FLUXO_VARIABLES_STARTSTATE);
		if (flxvars != null) {
			ctx.getProcessInstance().getContextInstance().addVariables(flxvars);
		}
	}
	
	/**
	 * Observa o evento de finalização de um subprocesso, replicando as variáveis criadas
	 * e ainda existentes no subprocesso para o processo que o invocou.
	 * 
	 * @param ctx o contexto de execução jbpm em que a finalização está ocorrendo.
	 */
	@Observer(Event.EVENTTYPE_SUBPROCESS_END)
	public void copyVariablesFromSubprocess(ExecutionContext ctx) {
		try {
			ProcessInstance spi = ctx.getSubProcessInstance();
			ProcessInstance pi = ctx.getProcessInstance();
			Map<String, Object> variables = spi.getContextInstance().getVariables();
			pi.getContextInstance().addVariables(variables);
			ProcessoInstanceManager pim = (ProcessoInstanceManager) org.jboss.seam.Component.getInstance("processoInstanceManager");
			ProcessoInstance pin = pim.findById(spi.getId());
			pin.setAtivo(false);
			ProcessoInstance piPai = pim.findById(pi.getId());
			
		   if (piPai == null) {
			   
                Integer idProcesso = (Integer) ctx.getToken().getProcessInstance().getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO);
                piPai = ProcessoHome.insereProcessoInstance(idProcesso, pi.getId());
                
            }
		   
			piPai.setIdLocalizacao(pin.getIdLocalizacao());
			piPai.setOrgaoJulgadorCargo(pin.getOrgaoJulgadorCargo());
			piPai.setOrgaoJulgadorColegiado(pin.getOrgaoJulgadorColegiado());         			
			pim.persistAndFlush(pin);
		} catch (Exception ex) {
			throw new AplicationException(AplicationException.createMessage("copiar as variaveis do subprocesso",
					"copyVariablesFromSubprocess()", "SubprocessoActionHandler", "BPM"));
		}
	}

}