package br.com.infox.editor.manager;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.editor.dao.EstruturaDocumentoTopicoMagistradoDao;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopico;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopicoMagistrado;

@Name(EstruturaDocumentoTopicoMagistradoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EstruturaDocumentoTopicoMagistradoManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "estruturaDocumentoTopicoMagistradoManager";
	
	@In
	private EstruturaDocumentoTopicoMagistradoDao estruturaDocumentoTopicoMagistradoDao;
	
	public String getConteudoPadrao(EstruturaDocumentoTopico documentoTopico, PessoaMagistrado pessoaMagistrado) {
		EstruturaDocumentoTopicoMagistrado estruturaDocumentoTopicoMagistrado = getEstruturaDocumentoTopicoMagistrado(documentoTopico, pessoaMagistrado);
		if (estruturaDocumentoTopicoMagistrado != null) {
			return estruturaDocumentoTopicoMagistrado.getConteudo();
		}
		return documentoTopico.getTopico().getConteudoPadrao();
	}

	public EstruturaDocumentoTopicoMagistrado getEstruturaDocumentoTopicoMagistrado(EstruturaDocumentoTopico estruturaDocumentoTopico, PessoaMagistrado pessoaMagistrado) {
		return estruturaDocumentoTopicoMagistradoDao.getEstruturaDocumentoTopicoMagistrado(estruturaDocumentoTopico, pessoaMagistrado);
	}

	public void removerTopicoMagistradoAssociado(EstruturaDocumentoTopico estruturaDocumentoTopico) {
		estruturaDocumentoTopicoMagistradoDao.removerTopicoMagistradoAssociado(estruturaDocumentoTopico);
	}

}
