package br.com.infox.bpm.taskPage.remessacnj;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.infox.bpm.action.TaskAction;
import br.jus.pje.nucleo.entidades.AssuntoTrf;


@Name(AssuntoTrfManifestacaoProcessualTaskPageAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class AssuntoTrfManifestacaoProcessualTaskPageAction extends TaskAction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Logger
	private Log log;
	
	public static final String NAME = "assuntoTrfManifestacaoProcessualTaskPageAction";

	private AssuntoTrf assuntoSelecionado;
	
	@In(required=false)
	private ManifestacaoProcessualMetaData manifestacaoProcessualMetaData;
	
	public AssuntoTrf getAssuntoSelecionado() {
		return assuntoSelecionado;
	}
	
	public void setAssuntoSelecionado(AssuntoTrf assuntoSelecionado) {
		this.assuntoSelecionado = assuntoSelecionado;
		if(!manifestacaoProcessualMetaData.getAssuntos().contains(assuntoSelecionado) && assuntoSelecionado != null){
			manifestacaoProcessualMetaData.getAssuntos().add(assuntoSelecionado);
		}
		assuntoSelecionado = null;
	}
	
	
	

}
