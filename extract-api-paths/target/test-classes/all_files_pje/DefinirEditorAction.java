package br.jus.cnj.pje.view;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;

import br.com.infox.cliente.util.ParametroUtil;

@Name(DefinirEditorAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class DefinirEditorAction {
	
	public static final String  NAME  = "definirEditorAction";
	
	public static final String EDITOR_ANTIGO = "/Processo/Fluxo/revisarMinuta.xhtml";
	public static final String EDITOR_NOVO = "/tags/pje/editorLibreOffice.xhtml";
	
	private String urlEditor;

	@RequestParameter
	private Boolean edicao;

	
	@Create
	public void init(){
		this.defineQualEditorSeraUtilizado();
	}


	private void defineQualEditorSeraUtilizado() {
		if(ParametroUtil.instance().isEditorLibreOfficeHabilitado()) {
			this.setUrlEditor(EDITOR_NOVO);
		}else {
			this.setUrlEditor(EDITOR_ANTIGO);
		}
	}


	public String getUrlEditor() {
		return urlEditor;
	}


	public void setUrlEditor(String urlEditor) {
		this.urlEditor = urlEditor;
	}

}
