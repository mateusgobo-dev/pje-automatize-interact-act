package br.com.infox.cliente.home;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


@Name("postoAvancadoAction")
public class PostoAvancadoAction implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String VAR_POSTO_AVANCADO = "_ojPostoAvancado";
	
	private OrgaoJulgador postoSelecionado;
	
	@Logger
	private Log logger;

	@In(create = true)
	private ProcessInstance processInstance;
	
	@In(create = true)
	private transient OrgaoJulgadorManager orgaoJulgadorManager;
	
	@In(create = true)
	private transient ProcessoJudicialService processoJudicialService;
	
	public OrgaoJulgador getPostoSelecionado() {
		return postoSelecionado;
	}

	public void setPostoSelecionado(OrgaoJulgador postoSelecionado) {
		this.postoSelecionado = postoSelecionado;
	}

	public Integer numeroDePostosDoOrgaoJulgador() {
		if(ProcessoTrfHome.instance() == null || ProcessoTrfHome.instance().getInstance().getIdProcessoTrf() == 0) {
			return 0;
		}
		
		ProcessoTrf processo = ProcessoTrfHome.instance().getInstance();
		
		return orgaoJulgadorManager.numeroDePostosDoOrgaoJulgador(processo.getOrgaoJulgador());
	}
	
	public List<OrgaoJulgador> buscaPostosDoOrgaoJulgador() {
		ProcessoTrf processo = ProcessoTrfHome.instance().getInstance();
		
		return orgaoJulgadorManager.buscaPostosDoOrgaoJulgador(processo.getOrgaoJulgador());
	}
	
	public void gravarPostoAvancado() {
		try {
			ProcessoTrf processo = ProcessoTrfHome.instance().getInstance();
			processInstance.getContextInstance().setVariable(VAR_POSTO_AVANCADO + processInstance.getId(), this.getPostoSelecionado());
			processoJudicialService.transitaParaProximaTarefa(processo, false, null);
			FacesMessages.instance().clear();
		} catch(Exception e) {
			logger.error("{verificaPrazoAguardandoED} Erro ao transitar processo: " + e.getMessage());
			logger.error(e);
			e.printStackTrace();
			FacesMessages.instance().add("Erro ao salvar Posto Avançado.");
		}
		
	}
	
	public void encaminharParaPostoAvancado() {
		ProcessoTrf processo = ProcessoTrfHome.instance().getInstance();
		OrgaoJulgador posto = (OrgaoJulgador) processInstance.getContextInstance().getVariable(VAR_POSTO_AVANCADO + processInstance.getId());
		
		/*
		 * se não possuir um posto selecionado é sinal de que o órgão possui
		 * somente um posto avançado e não passou pela tarefa
		 * "Escolher posto avançado", portanto tenho que selecionar o único
		 * posto avançado disponível para o OJ.
		 */
		if (posto == null) {
			posto = orgaoJulgadorManager.buscaPostosDoOrgaoJulgador(processo.getOrgaoJulgador()).get(0);
		}
		
		try {
			processoJudicialService.deslocarOrgaoJulgador(posto.getIdOrgaoJulgador());
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			FacesMessages.instance().add("Erro ao encaminhar processo!");
		}
	}
	
	public void retornarParaOrgaoJulgador() {
		try {
			processoJudicialService.retornaOrgaoJulgadorDeslocado();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			FacesMessages.instance().add("Erro ao encaminhar processo!");
		}
	}
}
