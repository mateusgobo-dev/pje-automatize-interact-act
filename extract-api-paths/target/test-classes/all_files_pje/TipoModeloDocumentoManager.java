/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.TipoModeloDocumentoDAO;
import br.jus.pje.nucleo.entidades.TipoModeloDocumento;

@Name(TipoModeloDocumentoManager.NAME)
public class TipoModeloDocumentoManager extends BaseManager<TipoModeloDocumento>{
	
	public static final String NAME = "tipoModeloDocumentoManager";
	
	@In
	private TipoModeloDocumentoDAO tipoModeloDocumentoDAO;

	@Override
	protected TipoModeloDocumentoDAO getDAO() {
		return this.tipoModeloDocumentoDAO;
	}

	/**
	 * Metodo responsavel por recuperar os tipos de modelos de documento de acordo
	 * com o papel atual
	 * 
	 * @return List<TipoModeloDocumento>
	 */
	public List<TipoModeloDocumento> obterTipoModeloDocumentoPorPapelAtual() {
		return getDAO().obterTipoModeloDocumentoPorPapelAtual();
	}

}