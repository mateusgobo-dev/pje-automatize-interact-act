/**
 *  pje-web
 *  Copyright (C) 2014 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;

/**
 * Componente de controle de tela do frame de fluxo (WEB-INF/xhtml/flx/exped/encerrarExpedienteManual.xhtml)
 * 
 * @author Rafael Dias de Souza - CNJ
 *
 */
@Name(EncerrarExpedienteManualAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EncerrarExpedienteManualAction extends TramitacaoFluxoAction implements Serializable {
	 
	private static final long serialVersionUID = -6903021077469426502L;
	
	public static final String NAME = "encerrarExpedienteManualAction";

    @In
    private ProcessoParteExpedienteManager processoParteExpedienteManager;
    
    @In
	private PessoaService pessoaService;

    private List<ProcessoParteExpediente> expedientes = new ArrayList<ProcessoParteExpediente>();
    
    private static Map<String, String> prms;
    
    private List<ExpedicaoExpedienteEnum> meios = null;
    
    private String meiosExpedientesDisponiveis = null;
    
    private boolean checkMarcarTodos = false;
	private List<ProcessoParteExpediente> expedienteMarcadoList = new ArrayList<ProcessoParteExpediente>();
	private List<ProcessoParteExpediente> expedientePrazoNaoVencidoList = new ArrayList<ProcessoParteExpediente>();
	
    static{
    	prms = new HashMap<String, String>();
    	prms.put("meiosExpedientesDisponiveis", "pje:fluxo:registroRespostaManual:meios");
    }

	public List<ProcessoParteExpediente> getExpedientes() {
		if(expedientes.isEmpty()) {
			expedientes = processoParteExpedienteManager.recuperaExpedientesAbertosPorMeiosComunicacaoAndProcessoTrf(getMeiosComunicacao(), processoJudicial);
		}
		return expedientes;
	}
    
	private List<ExpedicaoExpedienteEnum> getMeiosComunicacao() {
		if(meios == null){
			if(meiosExpedientesDisponiveis != null && !meiosExpedientesDisponiveis.isEmpty()) {
				String[] arrMeios = meiosExpedientesDisponiveis.split(",");
				meios = new ArrayList<ExpedicaoExpedienteEnum>();
				for(String m: arrMeios){
					meios.add(ExpedicaoExpedienteEnum.valueOf(m));
				}
			}
		}
		return meios;
	}
	
	/**
	 * Metodo para tratar os expedientes que foram selecionados para serem encerrados.
	 */
	public void tratarEncerramentoExpedientesSelecionados() {

		if(expedienteMarcadoList == null || expedienteMarcadoList.isEmpty()) {
			FacesMessages.instance().add(Severity.INFO, "encerrarExpediente.info.nenhum.selecionado");
		} else {								
			encerrarExpedientesSelecionados();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "encerrarExpediente.sucesso.encerrar.manual");
		}
	}

	/**
	 * Metodo que encerra os expedientes selecionados.
	 */
	private void encerrarExpedientesSelecionados() {

		try{
			for (ProcessoParteExpediente ppe : expedienteMarcadoList){
				EntityUtil.getSession().refresh(ppe);
				ppe.setFechado(true);
				ppe.setPendenteManifestacao(false);
				Pessoa p = pessoaService.findById(Authenticator.getUsuarioLogado().getIdUsuario());
				ppe.setPessoaEncerramentoManual(p);
				ppe.setDtEncerramentoManual(new Date());

				ppe = processoParteExpedienteManager.persist(ppe);
 				
 				EntityUtil.getEntityManager().detach(ppe.getProcessoJudicial());
				expedientes.remove(ppe);
				expedientePrazoNaoVencidoList.remove(ppe);
			}
			
			processoParteExpedienteManager.flush();
			DomicilioEletronicoService.instance().cancelarExpediente(expedienteMarcadoList);
			expedienteMarcadoList.clear();
		} catch (PJeBusinessException e){
			expedientes.clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "encerrarExpediente.erro.encerrar.manual");
		}
	}
	
	public void atualizarExpedienteMarcado(ProcessoParteExpediente processoParteExpediente){

		if (processoParteExpediente.getCheck()){
			expedienteMarcadoList.add(processoParteExpediente);
			
			if (processoParteExpediente.getDtPrazoLegal() != null && new Date().after(processoParteExpediente.getDtPrazoLegal())){
				expedientePrazoNaoVencidoList.add(processoParteExpediente);
			}
			
		} else {
			expedienteMarcadoList.remove(processoParteExpediente);
			expedientePrazoNaoVencidoList.remove(processoParteExpediente);
		}
	}
	
	public void atualizarTodosExpedientesMarcados() {
		for (ProcessoParteExpediente ppe : expedientes){
			ppe.setCheck(checkMarcarTodos);
		}

		expedienteMarcadoList.clear();
		expedientePrazoNaoVencidoList.clear();
		
		if (checkMarcarTodos){
			for (ProcessoParteExpediente ppe : expedientes){
				atualizarExpedienteMarcado(ppe);
			}
		}
	}
	
	public boolean contemExpedienteNaoVencido(){
		return expedientePrazoNaoVencidoList.size() > 0;
	}
	
	@Override
	protected Map<String, String> getParametrosConfiguracao() {
		return prms;
	}
	
	public void setCheckMarcarTodos(boolean checkMarcarTodos){
		this.checkMarcarTodos = checkMarcarTodos;
	}
	
	public boolean isCheckMarcarTodos(){
		return checkMarcarTodos;
	}
	
}
