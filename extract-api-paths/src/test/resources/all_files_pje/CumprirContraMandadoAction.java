package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.ProcessoExpedienteCriminal;

@Name("cumprirContraMandadoAction")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class CumprirContraMandadoAction implements Serializable{

	private static final long serialVersionUID = 5575659935714910988L;

	private ProcessoExpedienteCriminal processoExpedienteCriminalEdit;
	private int passo;
	private int maxPasso;	

	public ProcessoExpedienteCriminal getProcessoExpedienteCriminalEdit(){
		return processoExpedienteCriminalEdit;
	}

	public void setProcessoExpedienteCriminalEdit(ProcessoExpedienteCriminal processoExpedienteCriminalEdit){
		this.processoExpedienteCriminalEdit = processoExpedienteCriminalEdit;
	}
	
	public int getPasso(){
		return passo;
	}
	
	public void setPasso(int passo){
		this.passo = passo;
	}
	
	public int getMaxPasso(){
		return maxPasso;
	}
	
	public void setMaxPasso(int maxPasso){
		this.maxPasso = maxPasso;
	}
}
