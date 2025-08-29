package br.jus.cnj.pje.nucleo.manager;

import java.io.IOException;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ListProcessoCompletoBetaDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.csjt.pje.business.pdf.PdfException;
import br.jus.pje.nucleo.dto.AutoProcessualDTO;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.search.Search;

@Name(ListProcessoCompletoBetaManager.NAME)
public class ListProcessoCompletoBetaManager extends BaseManager<AutoProcessualDTO>{
	
	public static final String NAME = "listProcessoCompletoBetaManager";
	
	@In
	ListProcessoCompletoBetaDAO listProcessoCompletoBetaDAO;

	public AutoProcessualDTO recuperarAuto(Integer idProcesso, Integer idDocumento) {
		return listProcessoCompletoBetaDAO.recuperarAuto(idProcesso, idDocumento);
	}

	public AutoProcessualDTO recuperarPrimeiroAuto(Integer idProcesso) throws PdfException, IOException, PJeBusinessException {
		return listProcessoCompletoBetaDAO.recuperarPrimeiroAuto(idProcesso);
	}

	public AutoProcessualDTO recuperarUltimoAuto(Integer idProcesso) {
		return listProcessoCompletoBetaDAO.recuperarUltimoAuto(idProcesso);
	}
	
	public AutoProcessualDTO recuperarUltimoAutoPrincipal(Integer idProcesso) {
		return listProcessoCompletoBetaDAO.recuperarUltimoAutoPrincipal(idProcesso);
	}

	public AutoProcessualDTO recuperarProximoAuto(Integer idProcesso, Integer idDocumentoReferencia) {
		return listProcessoCompletoBetaDAO.recuperarProximoAuto(idProcesso, idDocumentoReferencia);
	}

	public AutoProcessualDTO recuperarAutoAnterior(Integer idProcesso, Integer idDocumentoReferencia) {
		return listProcessoCompletoBetaDAO.recuperarAutoAnterior(idProcesso, idDocumentoReferencia);
	}
	
	public List<AutoProcessualDTO> recuperarAutos(Integer idProcesso, boolean documentos, boolean movimentos, Search search) {
		return listProcessoCompletoBetaDAO.recuperarAutos(idProcesso, documentos, movimentos, search);
	}
	
	
	public List<AutoProcessualDTO> recuperarTodosAutos(Integer idProcesso, boolean documentos, boolean movimentos, Search search) {
		return listProcessoCompletoBetaDAO.recuperarTodosAutos(idProcesso, documentos, movimentos, search);
	}
	
	public Long countAutos(Integer idProcesso, boolean documentos, boolean movimentos, Search search) {
		return listProcessoCompletoBetaDAO.countAutos(idProcesso, documentos, movimentos, search);
	}
	
	public List<TipoProcessoDocumento> recuperarTiposDocumentosAutos(Integer idProcesso) {
		return listProcessoCompletoBetaDAO.recuperarTiposDocumentosAutos(idProcesso);
	}
	
	public void recuperarConteudoBinario(ProcessoDocumentoBin bin) {
		listProcessoCompletoBetaDAO.recuperarConteudoBinario(bin);
	}
	
	public Long countDocumentosNaoLidosAutos(Integer idProcesso) {
		return listProcessoCompletoBetaDAO.countDocumentosNaoLidosAutos(idProcesso);
	}
	
	public String recuperarConteudoModelo(int idProcessoDocumentoBin) {
		return listProcessoCompletoBetaDAO.recuperarConteudoModelo(idProcessoDocumentoBin);
	}
	
	public byte[] recuperarConteudoBinario(String numeroDocumentoStorage) {
		return listProcessoCompletoBetaDAO.recuperarConteudoBinario(numeroDocumentoStorage);
	}
	
	@Override
	protected BaseDAO<AutoProcessualDTO> getDAO() {
		return null;
	}
	
	public void marcarDocumentosComoLidosAutos(Integer idProcesso) {
		listProcessoCompletoBetaDAO.marcarDocumentosComoLidosAutos(idProcesso);
	}
}
