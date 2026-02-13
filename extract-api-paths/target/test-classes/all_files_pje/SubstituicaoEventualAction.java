/**
 * 
 */
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorCargoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoInstanceManager;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;

/**
 * @author cristof
 *
 */
@Name("substituicaoEventualAction")
@Scope(ScopeType.CONVERSATION)
public class SubstituicaoEventualAction implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6158919806279437797L;

	@Logger
	private Log logger;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private transient TramitacaoProcessualService tramitacaoProcessualService;
	
	@In
	private transient TaskInstanceHome taskInstanceHome;
	
	@In
	private transient OrgaoJulgadorManager orgaoJulgadorManager;
	
	@In
	private transient OrgaoJulgadorCargoManager orgaoJulgadorCargoManager;
	
	@In
	private transient OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager;
	
	@In
	private ProcessInstance processInstance;
	
	@In
	private transient ProcessoInstanceManager processoInstanceManager;
	
	private String transicaoPadrao;
	
	private OrgaoJulgador orgaoDestino;
	
	private OrgaoJulgadorCargo cargoDestino;
	
	private OrgaoJulgadorColegiado colegiadoDestino;
	
	private List<OrgaoJulgador> orgaosCandidatos;
	
	private List<OrgaoJulgadorColegiado> colegiadosCandidatos;
	
	private List<OrgaoJulgadorCargo> cargosCandidatos;
	
	@Create
	public void init(){
		if(tramitacaoProcessualService != null){
			transicaoPadrao = (String) tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		}
		orgaosCandidatos = orgaoJulgadorManager.findAll();
	}
	
	public void finalizar(){
		if(orgaoDestino == null){
			facesMessages.add(Severity.ERROR, "É necessário escolher o órgão julgador de destino.");
			return;
		}
		if(cargoDestino == null){
			facesMessages.add(Severity.ERROR, "É necessário escolher o cargo de destino.");
			return;
		}
		tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_SUBSEVENTUAL_ORGAO, orgaoDestino.getIdOrgaoJulgador());
		tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_SUBSEVENTUAL_CARGO, cargoDestino.getIdOrgaoJulgadorCargo());
		if(colegiadoDestino != null){
			tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_SUBSEVENTUAL_COLEGIADO, colegiadoDestino.getIdOrgaoJulgadorColegiado());
		}
		if(transicaoPadrao != null){
			try{
				taskInstanceHome.end(transicaoPadrao);
			}catch(Exception e){
				facesMessages.add(Severity.ERROR, "Houve um erro ao tentar movimentar o processo adiante.");
			}
		}
	}

	public OrgaoJulgador getOrgaoDestino() {
		return orgaoDestino;
	}

	public void setOrgaoDestino(OrgaoJulgador orgaoDestino) {
		this.orgaoDestino = orgaoDestino;
		try {
			cargosCandidatos = orgaoJulgadorCargoManager.findByRange(null, null, orgaoDestino, "orgaoJulgador");
			colegiadosCandidatos = new ArrayList<OrgaoJulgadorColegiado>();
			for(OrgaoJulgadorColegiadoOrgaoJulgador oj: orgaoDestino.getOrgaoJulgadorColegiadoOrgaoJulgadorList()){
				colegiadosCandidatos.add(oj.getOrgaoJulgadorColegiado());
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar os cargos do órgão julgador");
		}
	}

	public OrgaoJulgadorCargo getCargoDestino() {
		return cargoDestino;
	}

	public void setCargoDestino(OrgaoJulgadorCargo cargoDestino) {
		this.cargoDestino = cargoDestino;
	}

	public OrgaoJulgadorColegiado getColegiadoDestino() {
		return colegiadoDestino;
	}

	public void setColegiadoDestino(OrgaoJulgadorColegiado colegiadoDestino) {
		this.colegiadoDestino = colegiadoDestino;
	}

	public List<OrgaoJulgador> getOrgaosCandidatos() {
		return orgaosCandidatos;
	}

	public List<OrgaoJulgadorColegiado> getColegiadosCandidatos() {
		return colegiadosCandidatos;
	}

	public List<OrgaoJulgadorCargo> getCargosCandidatos() {
		return cargosCandidatos;
	}
	
}
