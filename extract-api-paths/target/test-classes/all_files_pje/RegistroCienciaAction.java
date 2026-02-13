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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.Util;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;


/**
 * Componente de controle de tela do frame de fluxo WEB-INF/xhtml/flx/exped/controleCorreios.xhtml
 * 
 * @author Thiago de Andrade Vieira
 * @author cristof
 *
 */
@Name("registroCienciaAction")
@Scope(ScopeType.CONVERSATION)
public class RegistroCienciaAction extends TramitacaoFluxoAction implements Serializable {
	 
	private static final long serialVersionUID = -6903021077469426502L;

    @In
    private AtoComunicacaoService atoComunicacaoService;
    
    @In
    private ProcessoParteExpedienteManager processoParteExpedienteManager;
    
    private List<ProcessoParteExpediente> expedientes = null;
    
    private static Map<String, String> prms;
    
    private String meiosExpedientes = "P,L,D,N,S,T";
    
    private Set<Integer> idsExpedientes;
    
    private List<ExpedicaoExpedienteEnum> meios = null;
    
    static{
    	prms = new HashMap<String, String>();
    	prms.put("meiosExpedientes", "pje:fluxo:registrociencia:meios");
    }

	@Override
	public void init() {
		super.init();
	}
	
	public List<ProcessoParteExpediente> getExpedientes() {
		if(expedientes == null){
			if(idsExpedientes == null){
				idsExpedientes = new HashSet<Integer>();
			}
			try {
				idsExpedientes.addAll(processoParteExpedienteManager.getAtosComunicacaoPendentesCienciaIds(getMeios(), null,processoJudicial));
				expedientes = new ArrayList<ProcessoParteExpediente>();
				for(Integer id: idsExpedientes){
					expedientes.add(processoParteExpedienteManager.findById(id));
				}
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
		return expedientes;
	}
    
	private List<ExpedicaoExpedienteEnum> getMeios(){
		if(meios == null){
			ExpedicaoExpedienteEnum[] meiosDisponiveis = ExpedicaoExpedienteEnum.values();
			meios = new ArrayList<ExpedicaoExpedienteEnum>();
			for(ExpedicaoExpedienteEnum m: meiosDisponiveis){
				if(Util.listaContem(meiosExpedientes, m.name())){
					meios.add(m);
				}
			}
		}
		return meios;
	}
	
	public void registrar(Integer idExpediente) {
		if (idsExpedientes.contains(idExpediente)) {
			Date dataAtual = new Date();
			for (ProcessoParteExpediente expediente: expedientes) {
				if (expediente.getIdProcessoParteExpediente() == idExpediente) {
					if (expediente.getDtCienciaParte() == null) {
						facesMessages.add(Severity.WARN, String.format(
								"A data não foi informada para o expediente %d.", expediente.getIdProcessoParteExpediente()));
					
					} else if (expediente.getDtCienciaParte().compareTo(dataAtual) > 0) {
						facesMessages.add(Severity.WARN, String.format(
								"A data informada para o expediente %d deve ser menor ou igual a data atual.", expediente.getIdProcessoParteExpediente()));
						
					} else if (expediente.getDtCienciaParte().compareTo(expediente.getDataDisponibilizacao()) < 0) {
						facesMessages.add(Severity.WARN, String.format(
								"A data informada para o expediente %d é anterior a sua data de criação.", expediente.getIdProcessoParteExpediente()));
						
					} else {
						registrarCiencia(expediente);
						facesMessages.add(Severity.INFO, String.format(
								"Registro de intimação para o expediente %d concluído com sucesso.", expediente.getIdProcessoParteExpediente()));
					}
				}
			}
		}
	}
	
	public void registrarTodos() {
		for (ProcessoParteExpediente exp: expedientes) {
			registrar(exp.getIdProcessoParteExpediente());
		}
	}
	
	private void registrarCiencia(ProcessoParteExpediente ppe){
		Pessoa p = Authenticator.getPessoaLogada();
		ppe.setPessoaCiencia(p);
		ppe.setNomePessoaCiencia(p.getNome());
		atoComunicacaoService.registraCienciaAutomatizada(ppe.getDtCienciaParte(), true, Arrays.asList(ppe));
	}
	
	@Override
	protected Map<String, String> getParametrosConfiguracao() {
		return prms;
	}
	
	public boolean getMostrarMensagemDiario(){
		return getMeios().contains(ExpedicaoExpedienteEnum.P);
	}

}
