package br.jus.cnj.pje.view;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.ProcessoParte;

@Name(InformacoesCriminaisAction.NAME)
@Scope(ScopeType.PAGE)
public class InformacoesCriminaisAction implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public static final String NAME = "informacoesCriminaisAction";
	
	private ProcessoParte parteSelecionada;
	
	private Boolean showIncidenciasPenais = Boolean.FALSE;
	
	@Create
	public void init(){
		
	}
	
	public ProcessoParte getParteSelecionada() {
		return parteSelecionada;
	}

	public void setParteSelecionada(ProcessoParte parteSelecionada) {
		this.parteSelecionada = parteSelecionada;
	}

	public Boolean getShowIncidenciasPenais() {
		return showIncidenciasPenais;
	}

	public void setShowIncidenciasPenais(Boolean showIncidenciasPenais) {
		this.showIncidenciasPenais = showIncidenciasPenais;
	}
	
}
