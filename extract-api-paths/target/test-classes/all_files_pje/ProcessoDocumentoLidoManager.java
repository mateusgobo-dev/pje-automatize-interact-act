package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoDocumentoLidoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoLido;

@Name("processoDocumentoLidoManager")
public class ProcessoDocumentoLidoManager extends BaseManager<ProcessoDocumentoLido>{
	
	@In
	private ProcessoDocumentoLidoDAO processoDocumentoLidoDAO;

	@Override
	protected ProcessoDocumentoLidoDAO getDAO() {
		return processoDocumentoLidoDAO;
	}
	
	public ProcessoDocumentoLido definirDocumentoComoLido(
			ProcessoDocumento processoDocumento, Pessoa pessoa)
			throws PJeBusinessException {
		// Se documento já foi lido pela pessoa, não criar outro registro, pois irá violar restrição unique (pessoa <-> documento)
		// Esse restrição já existia no ProcessoDocumentoLidoHome, porém não foi copiada para esse Manager
		ProcessoDocumentoLido documentoLido = processoDocumentoLidoDAO.getProcessoDocumentoLido(processoDocumento, pessoa);
		if (documentoLido != null && documentoLido.getIdProcessoDocumentoLido() != 0) {
			return documentoLido;
		}
		Date data = new Date();
		ProcessoDocumentoLido lido = new ProcessoDocumentoLido();
		lido.setDataApreciacao(data);
		lido.setPessoa(pessoa);
		lido.setProcessoDocumento(processoDocumento);
		return persist(lido);
	}
	
	public List<ProcessoDocumentoLido> listProcessosDocumentosLidos(List<ProcessoDocumento> documentos) {
		return processoDocumentoLidoDAO.listProcessosDocumentosLidos(documentos);
	}
	
	/**
	 * Recupera o objeto {@link ProcessoDocumentoLido} de acordo com o argumento informado.
	 * 
	 * @param processoDocumento {@link ProcessoDocumento}
	 * @return O objeto {@link ProcessoDocumentoLido} de acordo com o argumento informado.
	 */
	public ProcessoDocumentoLido recuperarDocumentoLido(ProcessoDocumento processoDocumento){
		return getDAO().recuperarDocumentoLido(processoDocumento);
	}
}
