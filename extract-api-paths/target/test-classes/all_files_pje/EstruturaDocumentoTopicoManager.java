package br.com.infox.editor.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.editor.dao.EstruturaDocumentoTopicoDao;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopico;
import br.jus.pje.nucleo.entidades.editor.Topico;

@Name(EstruturaDocumentoTopicoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EstruturaDocumentoTopicoManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estruturaDocumentoTopicoManager";

	@In
	private EstruturaDocumentoTopicoDao estruturaDocumentoTopicoDao;


	public EstruturaDocumentoTopico criarEstruturaDocumentoTopico(EstruturaDocumento estruturaDocumento, Topico topico) {
		EstruturaDocumentoTopico estruturaDocumentoTopico = new EstruturaDocumentoTopico();
		estruturaDocumentoTopico.setEstruturaDocumento(estruturaDocumento);
		estruturaDocumentoTopico.setTopico(topico);
		return estruturaDocumentoTopico;
	}

	public boolean temProcessoDocumentoAssociado(EstruturaDocumentoTopico estruturaDocumentoTopico) {
		return estruturaDocumentoTopicoDao.temProcessoDocumentoAssociado(estruturaDocumentoTopico);
	}

	public List<EstruturaDocumentoTopico> getEstruturaDocumentoTopicoList(EstruturaDocumento estruturaDocumento) {
		return estruturaDocumentoTopicoDao.getEstruturaDocumentoTopicoList(estruturaDocumento);
	}
}