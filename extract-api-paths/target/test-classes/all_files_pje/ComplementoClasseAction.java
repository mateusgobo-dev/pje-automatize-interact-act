package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.jus.cnj.pje.nucleo.manager.ComplementoClasseManager;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.pje.nucleo.entidades.ComplementoClasse;
import br.jus.pje.nucleo.entidades.ComplementoClasseProcessoTrf;

@Name("complementoClasseAction")
@Scope(ScopeType.PAGE)
public class ComplementoClasseAction extends BaseAction<ComplementoClasse> implements Serializable {
	
	/**
	 * @author Carlos Lisboa.
  	 */
	private static final long serialVersionUID = 1L;
	
	//private ProcessoTrf processoTrf;
	private List<ComplementoClasseProcessoTrf> complementoClasseProcessoTrfs;
	private List<ComplementoClasse> complementoClasses;
	
	
	@In(create = true)
	private ComplementoClasseManager complementoClasseManager;

	@Override
	protected ComplementoClasseManager getManager() {
		return this.complementoClasseManager;
	}

	@Override
	public EntityDataModel<ComplementoClasse> getModel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Boolean exibirAba(){
		if(ProcessoTrfHome.instance().getInstance().getComplementoClasseProcessoTrfList().size() > 0){
			return true;
		}else{
			return false;
		}
			
	}

	public List<ComplementoClasseProcessoTrf> getComplementoClasseProcessoTrfs() {
		return complementoClasseProcessoTrfs;
	}

	public void setComplementoClasseProcessoTrfs(
			List<ComplementoClasseProcessoTrf> complementoClasseProcessoTrfs) {
		this.complementoClasseProcessoTrfs = complementoClasseProcessoTrfs;
	}


	public List<ComplementoClasse> getComplementoClasses() {
		return complementoClasses;
	}
	
	public void setComplementoClasses(List<ComplementoClasse> complementoClasses) {
		this.complementoClasses = complementoClasses;
	}
	
	public void listarComplementoClasseProcesso(){
		setComplementoClasses(getManager().getListComplementoClasse(ProcessoTrfHome.instance().getInstance()));
		//setComplementoClasseProcessoTrfs(ProcessoTrfHome.instance().getInstance().getComplementoClasseProcessoTrfList());
		retornarValorComplemento();
	}

	public void retornarValorComplemento(){
		List<ComplementoClasseProcessoTrf> listFinal = new ArrayList<ComplementoClasseProcessoTrf>();
		for(ComplementoClasse comp : getComplementoClasses()){
			for(ComplementoClasseProcessoTrf compTrf: ProcessoTrfHome.instance().getInstance().getComplementoClasseProcessoTrfList()){
				if(comp.equals(compTrf.getComplementoClasse())){
					listFinal.add(compTrf);
				}
			}
		}
		setComplementoClasseProcessoTrfs(listFinal);
	}
	
}
