package br.com.infox.editor.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.editor.bean.Estilo;
import br.com.infox.editor.dao.CssDocumentoDao;
import br.jus.pje.nucleo.entidades.editor.CssDocumento;

@Name(CssDocumentoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class CssDocumentoManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "cssDocumentoManager";
	
	@In
	private CssDocumentoDao cssDocumentoDao;
	
	public List<CssDocumento> getCssDocumentos() {
		return cssDocumentoDao.getCssDocumentos();
	}
	
	public List<Estilo> getEstilos() {
		return cssDocumentoDao.getEstilos();
	}
}
