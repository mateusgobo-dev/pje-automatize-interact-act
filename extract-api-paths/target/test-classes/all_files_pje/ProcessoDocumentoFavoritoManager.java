package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoDocumentoFavoritoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoFavorito;

@Name("processoDocumentoFavoritoManager")
public class ProcessoDocumentoFavoritoManager extends BaseManager<ProcessoDocumentoFavorito> {
	
	@In
	private ProcessoDocumentoFavoritoDAO processoDocumentoFavoritoDAO;
	
	@Override
	protected ProcessoDocumentoFavoritoDAO getDAO() {
		return processoDocumentoFavoritoDAO;
	}
	
	public List<ProcessoDocumentoFavorito> findByProcesso(Integer idProcesso, Integer idUsuario) {
		return getDAO().findByProcesso(idProcesso, idUsuario);
	}
	
	@Override
	public void remove(ProcessoDocumentoFavorito entity) throws PJeBusinessException{
		getDAO().remove(entity);
	}

	/**
	 * metodo responsavel por recuperar todos os processos documentos favoritos da pessoa passada em parametro.
	 * @param pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<ProcessoDocumentoFavorito> recuperarProcessosDocumentosFavoritos(Pessoa pessoa) throws Exception {
		return processoDocumentoFavoritoDAO.recuperarProcessosDocumentosFavoritos(pessoa);
	}

	public ProcessoDocumentoFavorito recuperarPorId(Integer procDocFavoritoId) {
		return processoDocumentoFavoritoDAO.find(procDocFavoritoId);
	}
}