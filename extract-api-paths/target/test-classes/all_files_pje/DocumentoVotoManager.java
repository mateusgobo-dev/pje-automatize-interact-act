package br.com.jt.pje.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.dao.DocumentoVotoDAO;
import br.jus.pje.jt.entidades.DocumentoVoto;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name(DocumentoVotoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class DocumentoVotoManager extends GenericManager{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "documentoVotoManager";
	@In
	private DocumentoVotoDAO documentoVotoDAO;
	
	public List<DocumentoVoto> getDocumentoVotoByVoto(Voto voto){
		return documentoVotoDAO.getDocumentoVotoByVoto(voto);		
	}
	
	public DocumentoVoto getDocumentoVotoByVotoETipo(Voto voto, TipoProcessoDocumento tipoProcessoDocumento){
		return documentoVotoDAO.getDocumentoVotoByVotoETipo(voto, tipoProcessoDocumento);
	}
	
	public void copiarDocumentos(Voto voto, Voto novoVoto) throws InstantiationException, IllegalAccessException {
		for (DocumentoVoto documentoVoto : getDocumentoVotoByVoto(voto)) {
			DocumentoVoto documento = EntityUtil.cloneEntity(documentoVoto, false);
			ProcessoDocumentoBin processoDocumentoBin = EntityUtil.cloneEntity(documento.getProcessoDocumentoBin(), false);
			persist(processoDocumentoBin);
			documento.setProcessoDocumentoBin(processoDocumentoBin);
			documento.setVoto(novoVoto);
			persist(documento);
		}
	}
	
	public DocumentoVoto getUltimoDocumentoVotoAssinado(TipoProcessoDocumento tipoProcessoDocumento, Processo processo, Voto voto){
		if(tipoProcessoDocumento == null || processo == null || voto == null || voto.getIdVoto() == 0){
			return null;
		}
		return documentoVotoDAO.getUltimoDocumentoVotoAssinadoByProcessoTipoProcessoDocumento(tipoProcessoDocumento, processo, voto);
	}
	
	public DocumentoVoto getUltimoDocumentoVoto(TipoProcessoDocumento tipoProcessoDocumento, Processo processo, Voto voto){
		if(tipoProcessoDocumento == null || processo == null || voto == null || voto.getIdVoto() == 0){
			return null;
		}
		return documentoVotoDAO.getUltimoDocumentoVotoByProcessoTipoProcessoDocumento(tipoProcessoDocumento, processo, voto);
	}
	
	public void removerDaTabelaDocumentoVoto(DocumentoVoto documentoVoto) {
		documentoVotoDAO.removerDaTabelaDocumentoVoto(documentoVoto);
	}
}
