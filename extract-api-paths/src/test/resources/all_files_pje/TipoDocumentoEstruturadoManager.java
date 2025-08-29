package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.TipoDocumentoEstruturadoDAO;
import br.jus.pje.nucleo.entidades.TipoDocumentoEstruturado;

@Name("tipoDocumentoEstruturadoManager")
public class TipoDocumentoEstruturadoManager extends BaseManager<TipoDocumentoEstruturado>{

	@In
	private TipoDocumentoEstruturadoDAO tipoDocumentoEstruturadoDAO;
	
	@Override
	protected BaseDAO<TipoDocumentoEstruturado> getDAO() {
		return tipoDocumentoEstruturadoDAO;
	}
	
	public TipoDocumentoEstruturado findByNamespace(String namespace){
		return tipoDocumentoEstruturadoDAO.findByNamespace(namespace);
	}
	
}
