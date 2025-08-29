package br.com.jt.pje.manager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.jus.pje.jt.entidades.DocumentoVoto;
import br.com.jt.pje.dao.DocumentoValidacaoHashDAO;

@Name(DocumentoValidacaoHashManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class DocumentoValidacaoHashManager extends GenericManager{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "documentoValidacaoHashManager";
	
	@In
	private DocumentoValidacaoHashDAO documentoValidacaoHashDAO;
	
	public void removerDaTabelaDocumentoValidacaoHash(DocumentoVoto documentoVoto) {
		documentoValidacaoHashDAO.removerDaTabelaDocumentoValidacaoHash(documentoVoto);
	}
	
}