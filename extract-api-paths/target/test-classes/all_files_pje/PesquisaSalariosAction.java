package br.com.infox.pje.action;

import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.view.GenericAction;
import br.com.jt.pje.list.SalarioMinimoList;
import br.jus.pje.jt.entidades.SalarioMinimo;

@Name(PesquisaSalariosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PesquisaSalariosAction extends GenericAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "pesquisaSalariosAction";
	
	private List<SalarioMinimo> listaSalarioMinimo = null;
	
	@In(create=true)
	private SalarioMinimoList salarioMinimoList = null;
	
	public void pesquisarSalarioMinimo(Date dataPesquisa){
		getSalarioMinimoList().setDataPesquisa(dataPesquisa);
		setListaSalarioMinimo(salarioMinimoList.list(15));
	}
	
	public void pesquisarSalarioMinimo(){
		setListaSalarioMinimo(salarioMinimoList.list(15));
	}
	
	public void limpar(){
		getSalarioMinimoList().newInstance();
		setListaSalarioMinimo(salarioMinimoList.list(15));
	}
	
	public SalarioMinimoList getSalarioMinimoList() {
		return salarioMinimoList;
	}

	public void setSalarioMinimoList(SalarioMinimoList salarioMinimoList) {
		this.salarioMinimoList = salarioMinimoList;
	}

	public List<SalarioMinimo> getListaSalarioMinimo() {
		return listaSalarioMinimo;
	}

	public void setListaSalarioMinimo(List<SalarioMinimo> listaSalarioMinimo) {
		this.listaSalarioMinimo = listaSalarioMinimo;
	}
	
}
