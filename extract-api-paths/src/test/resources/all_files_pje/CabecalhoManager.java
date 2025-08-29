package br.com.infox.editor.manager;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.editor.interpretadorDocumento.LinguagemFormalException;
import br.com.infox.editor.service.ProcessaModeloService;
import br.jus.pje.nucleo.entidades.editor.Cabecalho;

@Name(CabecalhoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class CabecalhoManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "cabecalhoManager";
	
	@In
	private ProcessaModeloService processaModeloService;
	
	public String getCabecalhoProcessado(Cabecalho cabecalho) {
		try {
			return processaModeloService.processaVariaveisModelo(cabecalho.getConteudo());
		} catch (LinguagemFormalException e) {
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao avaliar liguagem formal", e);
			return "";
		}
	}
}
