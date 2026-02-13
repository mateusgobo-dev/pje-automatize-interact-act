/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;

import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoInstanceManager;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Componente de ação da tela Processo/Fluxo/subev/retomadasubev.xhtml.
 * 
 * @author cristof
 * @see <a href="http://www.cnj.jus.br/jira/PJEII-3916">PJEII-3916</a>
 */
@Name("retomadaSubstituicaoEventualAction")
@Scope(ScopeType.CONVERSATION)
public class RetomadaSubstituicaoEventualAction implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2108055539026171450L;

	@Logger
	private Log logger;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private transient TramitacaoProcessualService tramitacaoProcessualService;
	
	@In
	private ProcessInstance processInstance;
	
	@In
	private transient ProcessoInstanceManager processoInstanceManager;
	
	@In
	private transient TaskInstanceHome taskInstanceHome;
	
	@In
	private transient OrgaoJulgadorManager orgaoJulgadorManager;
	
	@In
	private transient LocalizacaoManager localizacaoManager;
	
	private OrgaoJulgador orgaoResponsavelAtual;
	
	private Token tokenSubstituto;
	
	private List<String> errosInicializacao;
	
	private boolean errosEncontrados;
	
	private boolean retomadaPossivel;
	
	private boolean processoRetomado;
	
	private String transicaoPadrao;
	
	@Create
	public void init(){
		errosInicializacao = new ArrayList<String>();
		transicaoPadrao = (String) tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		if(processInstance == null){
			retomadaPossivel = false;
			String msg = "Não foi possível identificar o processo sobre o qual poderia haver a retomada.";
			errosInicializacao.add(msg);
			errosEncontrados = true;
			facesMessages.add(Severity.ERROR, msg);
			return;
		}
		processoRetomado = false;
		verificaCondicoesRetomada();
		if(errosInicializacao.size() > 0){
			errosEncontrados = true;
		}
	}
	
	/**
	 * Verifica se as condições necessárias para retomada do processo estão presentes.
	 * São elas:
	 * <li>estar o nó em uma perna de um fork; e</li>
	 * <li>estar o processo judicial vinculado a um subprocesso existente na outra perna do fork</li>
	 * 
	 */
	private void verificaCondicoesRetomada(){
		Token tk = processInstance.getRootToken();
		Map<String, Token> sieblings = tk.getActiveChildren();
		// É necessário que somente existam 2 pernas ativas no fork
		if(sieblings.size() != 2){
			errosInicializacao.add("O número de pernas ativas neste fluxo é diferente de 2.");
			retomadaPossivel = false;
			return;
		}
		for(Token t: sieblings.values()){
			if(t.getSubProcessInstance() != null){
				try{
					// É necessário que a outra perna esteja em um subprocesso que, por sua vez, tem que estar atribuído a terceiro
					ProcessoInstance pi = processoInstanceManager.findById(t.getSubProcessInstance().getId());
					if(pi != null && pi.getIdLocalizacao() != tramitacaoProcessualService.recuperaProcesso().getOrgaoJulgador().getIdOrgaoJulgador()){
						tokenSubstituto = t;
						Localizacao localizacao = localizacaoManager.findById(pi.getIdLocalizacao());
						if(localizacao != null) {
							orgaoResponsavelAtual =  orgaoJulgadorManager.getOrgaoJulgadorByLocalizacao(localizacao);
						}
						break;
					}
				}catch(PJeBusinessException e){
				}
			}
		}
		if(tokenSubstituto == null){
			retomadaPossivel = false;
			return;
		}else{
			Object atividadeIniciada = tokenSubstituto.getProcessInstance().getContextInstance().getVariable("pje:fluxo:substituicaoeventual:iniciada");
			if(atividadeIniciada != null && Boolean.class.isAssignableFrom(atividadeIniciada.getClass()) && ((Boolean) atividadeIniciada).booleanValue()){
				retomadaPossivel = false;
			}else{
				retomadaPossivel = true;
			}
		}
//		try {
//			ProcessoInstance pi = processoInstanceManager.findById(tokenSubstituto.getSubProcessInstance().getId());
//			if(pi == null){
//				errosInicializacao.add("Não foi possível recuperar a perna da substituição eventual.");
//				retomadaPossivel = false;
//				return;
//			}
//			if(pi.getOrgaoJulgador() == null || pi.getOrgaoJulgador() == tramitacaoProcessualService.recuperaProcesso().getOrgaoJulgador().getIdOrgaoJulgador()){
//				processoRetomado = true;
//				orgaoResponsavelAtual = tramitacaoProcessualService.recuperaProcesso().getOrgaoJulgador();
//				errosInicializacao.add("A perna da substituição eventual está atribuída a este órgão julgador.");
//				retomadaPossivel = false;
//				return;
//			}else{
//				orgaoResponsavelAtual =  orgaoJulgadorManager.findById(pi.getOrgaoJulgador());
//			}
			// Tenho que recuperar a outra perna e verificar se a variável indicativa do início do trabalho está em funcionamento.
//		} catch (PJeBusinessException e) {
//			errosInicializacao.add("Houve um erro ao tentar recuperar a perna da substituição eventual.");
//			retomadaPossivel = false;
//		}
	}
	
	public void retomar(){
		verificaCondicoesRetomada();
		if(retomadaPossivel){
			tramitacaoProcessualService.apagaVariavel(Variaveis.VARIAVEL_SUBSEVENTUAL_CARGO);
			tramitacaoProcessualService.apagaVariavel(Variaveis.VARIAVEL_SUBSEVENTUAL_ORGAO);
			tramitacaoProcessualService.apagaVariavel(Variaveis.VARIAVEL_SUBSEVENTUAL_COLEGIADO);
			retomar_(tokenSubstituto, true);
			facesMessages.add(Severity.INFO, "Retomada realizada com sucesso.");
			if(transicaoPadrao != null && !transicaoPadrao.isEmpty()){
				try{
					taskInstanceHome.end(transicaoPadrao);
				}catch(Exception e){
					facesMessages.add(Severity.INFO, "Houve um erro ao tentar transitar para a saída [{0}].", transicaoPadrao);
				}
			}
			processoRetomado = true;
		}else{
			facesMessages.add(Severity.ERROR, "A retomada não é possível.");
		}
	}
	
	private void retomar_(Token tk, boolean flush){
		try {
			ProcessoInstance pi = processoInstanceManager.findById(tk.getProcessInstance().getId());
			if(pi != null){
				ProcessoTrf pj = tramitacaoProcessualService.recuperaProcesso();
				pi.setIdLocalizacao(pj.getOrgaoJulgador().getLocalizacao().getIdLocalizacao());
				pi.setOrgaoJulgadorCargo(pj.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo());
				pi.setOrgaoJulgadorColegiado(pj.getOrgaoJulgadorColegiado() != null ? pj.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado() : null);
				tk.getProcessInstance().getContextInstance().deleteVariable(Variaveis.VARIAVEL_SUBSEVENTUAL_CARGO);
				tk.getProcessInstance().getContextInstance().deleteVariable(Variaveis.VARIAVEL_SUBSEVENTUAL_ORGAO);
				tk.getProcessInstance().getContextInstance().deleteVariable(Variaveis.VARIAVEL_SUBSEVENTUAL_COLEGIADO);
				if(tk.getSubProcessInstance() != null){
					retomar_(tk.getSubProcessInstance().getRootToken(), false);
				}else if(tk.getActiveChildren() != null && tk.getActiveChildren().size() > 0){
					for(Token child: tk.getActiveChildren().values()){
						retomar_(child, false);
					}
				}
				if(flush){
					processoInstanceManager.flush();
				}
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar retomar o processo {0}.", tk.getFullName());
		}
	}

	public List<String> getErrosInicializacao() {
		List<String> ret = new ArrayList<String>(errosInicializacao);
		errosInicializacao.clear();
		errosEncontrados = false;
		return ret;
	}

	public boolean isRetomadaPossivel() {
		return retomadaPossivel;
	}
	
	public boolean isProcessoRetomado() {
		return processoRetomado;
	}
	
	public OrgaoJulgador getOrgaoResponsavelAtual() {
		return orgaoResponsavelAtual;
	}
	
	public boolean isErrosEncontrados() {
		return errosEncontrados;
	}

}
