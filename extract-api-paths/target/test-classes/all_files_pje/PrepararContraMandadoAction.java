package br.jus.cnj.pje.view.fluxo;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.pje.service.AssinarContraMandadoService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ContraMandadoManager;
import br.jus.cnj.pje.nucleo.manager.MandadoPrisaoManager;
import br.jus.pje.nucleo.entidades.ContraMandado;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;

@Name("prepararContraMandadoAction")
@Scope(ScopeType.CONVERSATION)
public class PrepararContraMandadoAction extends PreparaExpedienteCriminalAction<ContraMandado, ContraMandadoManager>{
	
	private static final long serialVersionUID = 2687547930732384582L;
	
	

	@In(create = true, required = true)
	private ContraMandadoManager contraMandadoManager;

	@In(create = true, required = true)
	private MandadoPrisaoManager mandadoPrisaoManager;

	@In(create = true, required = true)
	private AssinarContraMandadoService assinarContraMandadoService;

	private List<MandadoPrisao> mandadosPesquisados = new ArrayList<MandadoPrisao>(0);
	private Integer numeroMandadoPesquisa;
	private Boolean mostrarMandados;
	
	@Override
	public void init() {
		super.init();
		popularPessoas();
		setMaxPasso(1);
		
		if(isPreparando()){
			setMostrarMandados(false);
			pesquisarPartesCandidatas();
			setPasso(0);
		}else{
			pesquisarExpedientesNaoAssinados();			
			if(isRetificando()){				
				setPasso(0);
			}else{
				setPasso(getMaxPasso());				
			}
		}
	}
	
	@Override
	public AssinarContraMandadoService getAssinarExpedienteCriminalService(){
		return assinarContraMandadoService;
	}	

	@Override
	public void buscarDemaisMandados(){
		try{
			setMandados(mandadoPrisaoManager.recuperarDemaisMandadosDoProcesso(getProcessoJudicial(),
					getProcessoExpedienteCriminalEdit().getPessoa(), null, true));
		} catch (PJeBusinessException e){
			FacesMessages.instance().add(Severity.ERROR, "pje.error.prepararAlvaraSolturaAction.erroBuscarDemaisMandados",e);
		}
	}

	@Override
	public ContraMandadoManager getManager(){
		return contraMandadoManager;
	}

	public void pesquisarMandados(){
		try{
			SituacaoExpedienteCriminalEnum sitExpCrim = SituacaoExpedienteCriminalEnum.PC; 
			mandadosPesquisados = mandadoPrisaoManager.recuperarMandados(getNumeroMandadoPesquisa(),getProcessoExpedienteCriminalEdit().getPessoa(), sitExpCrim);
		} catch (PJeBusinessException e){
			FacesMessages.instance().add(Severity.ERROR, "pje.error.prepararAlvaraSolturaAction.erroPesquisarMandados", e);
		}
		if (mandadosPesquisados == null || mandadosPesquisados.isEmpty()){
			mostrarMandados = false;
		}
		else{
			if (mandadosPesquisados.size() == 1){
				getProcessoExpedienteCriminalEdit().setMandadoPrisao(mandadosPesquisados.get(0));
				mostrarMandados = false;
			}
			else{
				mostrarMandados = true;
			}
		}
	}
	
	@Override
	public void proximoPasso() {
		if(getProcessoExpedienteCriminalEdit().getMandadoPrisao() == null){
			setPasso(0);
		}else{
			super.proximoPasso();
		}
	}

	public void selecionarMandado(MandadoPrisao mandadoPrisao){
		getProcessoExpedienteCriminalEdit().setMandadoPrisao(mandadoPrisao);
		mostrarMandados = false;
	}

	public List<MandadoPrisao> getMandadosPesquisados(){
		return mandadosPesquisados;
	}

	public void setMandadosPesquisados(List<MandadoPrisao> mandadosPesquisados){
		this.mandadosPesquisados = mandadosPesquisados;
	}

	public Integer getNumeroMandadoPesquisa(){
		return numeroMandadoPesquisa;
	}

	public void setNumeroMandadoPesquisa(Integer numeroMandadoPesquisa){
		this.numeroMandadoPesquisa = numeroMandadoPesquisa;
	}

	public Boolean getMostrarMandados(){
		return mostrarMandados;
	}

	public void setMostrarMandados(Boolean mostrarMandados){
		this.mostrarMandados = mostrarMandados;
	}	
}
