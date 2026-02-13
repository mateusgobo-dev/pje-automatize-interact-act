package br.jus.cnj.pje.view.fluxo;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AlvaraSolturaManager;
import br.jus.pje.nucleo.entidades.AlvaraSoltura;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.enums.TipoSolturaEnum;

@Name("cumprirAlvaraSolturaAction")
@Scope(ScopeType.CONVERSATION)
public class CumprirAlvaraSolturaAction extends AbstractCumprirMandadoAlvaraAction<AlvaraSoltura, AlvaraSolturaManager> {

	private static final long serialVersionUID = -3341292070832596635L;
	
	@In
	private AlvaraSolturaManager alvaraSolturaManager;
	
	private List<PessoaMagistrado> autoridades = new ArrayList<PessoaMagistrado>();
	private TipoSolturaEnum[] tiposSoltura = TipoSolturaEnum.values();


	@Override
	public AlvaraSolturaManager getManager(){
		return alvaraSolturaManager;
	}

	@Override
	public void gravar(){
		try{
			getManager().gravarCumprimento(getProcessoExpedienteCriminalEdit());
		} catch (PJeBusinessException e){
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, e.getCode(), e.getParams());
		}
	}	
	
	public List<PessoaMagistrado> getAutoridades(){
		return autoridades;
	}	
	
	public void setAutoridades(List<PessoaMagistrado> autoridades){
		this.autoridades = autoridades;
	}
	
	@Override
	public void informarCumprimento(){
		setPasso(3);
		super.informarCumprimento();
	}	
	
	public TipoSolturaEnum[] getTiposSoltura(){
		return tiposSoltura;
	}
	
	public void setTiposSoltura(TipoSolturaEnum[] tiposSoltura){
		this.tiposSoltura = tiposSoltura;
	}
}
