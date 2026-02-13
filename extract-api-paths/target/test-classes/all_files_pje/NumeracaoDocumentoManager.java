package br.com.infox.editor.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.editor.dao.NumeracaoDocumentoDao;
import br.jus.pje.nucleo.entidades.editor.NumeracaoDocumento;

@Name(NumeracaoDocumentoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class NumeracaoDocumentoManager implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "numeracaoDocumentoManager";

	@In
	private NumeracaoDocumentoDao numeracaoDocumentoDao;

	public List<NumeracaoDocumento> getNumeracaoDocumentoList() {
		return numeracaoDocumentoDao.getNumeracaoDocumentoList();
	}

}
