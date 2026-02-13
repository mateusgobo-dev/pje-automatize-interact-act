package br.com.infox.editor.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.editor.dao.EstruturaDocumentoDao;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;


@Name(EstruturaDocumentoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EstruturaDocumentoManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estruturaDocumentoManager";

	@In
	private EstruturaDocumentoDao estruturaDocumentoDao;
	
	public List<EstruturaDocumento> getEstruturaDocumentoList(TipoProcessoDocumento tipoProcessoDocumento) {
		return estruturaDocumentoDao.getEstruturaDocumentoList(tipoProcessoDocumento);
	}

}
