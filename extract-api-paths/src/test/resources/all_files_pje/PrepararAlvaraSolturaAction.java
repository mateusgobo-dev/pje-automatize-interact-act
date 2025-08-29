package br.jus.cnj.pje.view.fluxo;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.pje.service.AbstractAssinarExpedienteCriminalService;
import br.com.infox.pje.service.AssinarAlvaraSolturaService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AlvaraSolturaManager;
import br.jus.cnj.pje.nucleo.manager.MandadoPrisaoManager;
import br.jus.pje.nucleo.entidades.AlvaraSoltura;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.enums.TipoSolturaEnum;

@Name("prepararAlvaraSolturaAction")
@Scope(ScopeType.CONVERSATION)
public class PrepararAlvaraSolturaAction extends PrepararMandadoAlvaraAction<AlvaraSoltura, AlvaraSolturaManager> {

	private static final long serialVersionUID = -5725587126891004916L;

	
	@In(create = true, required=true)
	private MandadoPrisaoManager mandadoPrisaoManager;

	@In(create = true, required=true)
	private AlvaraSolturaManager alvaraSolturaManager;
	
	@In
	private AssinarAlvaraSolturaService assinarAlvaraSolturaService;	

	private TipoSolturaEnum[] tiposSoltura = TipoSolturaEnum.values();
	private List<MandadoPrisao> mandadosPesquisados = new ArrayList<MandadoPrisao>(0);

	private Integer numeroMandadoPesquisa;
	
	@Override
	public void init() {
		super.init();
		popularPessoas();
		setMaxPasso(3);
		setPasso(0);
		
		if(isPreparando()){
			pesquisarPartesCandidatas();
		}else{			
			pesquisarExpedientesNaoAssinados();
		}
	}
	
	@Override
	public AlvaraSolturaManager getManager(){
		return alvaraSolturaManager;
	}

	@Override
	public void buscarDemaisMandados(){
		try{
			setMandados(mandadoPrisaoManager.recuperarMandadosPessoa(getProcessoExpedienteCriminalEdit().getPessoa(), true));
		} catch (PJeBusinessException e){
			FacesMessages.instance().add(Severity.ERROR, "pje.error.prepararAlvaraSolturaAction.erroBuscarDemaisMandados", e);
		}
	}
	
	@Override
	public AbstractAssinarExpedienteCriminalService<AlvaraSoltura> getAssinarExpedienteCriminalService(){
		return assinarAlvaraSolturaService;
	}

	public void associarMandadoPesquisa(MandadoPrisao mandadoPrisao){
		getProcessoExpedienteCriminalEdit().getMandadosAlcancados().add(mandadoPrisao);
		mandadosPesquisados.remove(mandadoPrisao);
	}

	public Integer getNumeroMandadoPesquisa(){
		return numeroMandadoPesquisa;
	}

	public void setNumeroMandadoPesquisa(Integer numeroMandadoPesquisa){
		this.numeroMandadoPesquisa = numeroMandadoPesquisa;
	}

	public List<MandadoPrisao> getMandadosPesquisados(){
		return mandadosPesquisados;
	}

	public void setMandadosPesquisados(List<MandadoPrisao> mandadosPesquisados){
		this.mandadosPesquisados = mandadosPesquisados;
	}

	public void pesquisarMandados(){
		try{
			mandadosPesquisados = mandadoPrisaoManager.recuperarMandados(getNumeroMandadoPesquisa(),
					getProcessoExpedienteCriminalEdit().getPessoa(),null);
		} catch (PJeBusinessException e){
			FacesMessages.instance().add(Severity.ERROR, "pje.error.prepararAlvaraSolturaAction.erroPesquisarMandados", e);
		}
	}

	public TipoSolturaEnum[] getTiposSoltura(){
		return tiposSoltura;
	}

	public void setTiposSoltura(TipoSolturaEnum[] tiposSoltura){
		this.tiposSoltura = tiposSoltura;
	}	
}