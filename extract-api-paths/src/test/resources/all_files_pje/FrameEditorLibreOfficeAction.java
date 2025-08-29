package br.jus.cnj.pje.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.cnj.pje.editor.lool.LibreOfficeManager;
import br.jus.cnj.pje.editor.lool.LoolException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(FrameEditorLibreOfficeAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class FrameEditorLibreOfficeAction implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "frameEditorLibreOfficeAction";
	
	@In
	private ProcessoDocumentoBinManager processoDocumentoBinManager;
	
	@In
	private DocumentoJudicialService documentoJudicialService;

	@In(create = false, required = true)
	private FacesMessages facesMessages;
	
	private ProcessoDocumento processoDocumento;
	
	@RequestParameter
	private Long newTaskId;
	
	@RequestParameter
	private Integer idProcessoDocumento;
	
	@RequestParameter
	private Integer idProcesso;
	
	private LibreOfficeManager libreOfficeManager;
	
	
	@Create
	public void init() throws Exception {
		this.processoDocumento = documentoJudicialService.getDocumento(idProcessoDocumento);
		this.libreOfficeManager = new LibreOfficeManager(this.processoDocumento.getProcessoDocumentoBin().getNomeDocumentoWopi()); 
		
	}

	public void gravarAlteracoes() {
		try {
			InputStream pdf = this.libreOfficeManager.getPDFContent();
			int size = pdf.available();
			File arquivoBinario = processoDocumento.getProcessoDocumentoBin().getFile();
			if ( arquivoBinario==null ) {
				arquivoBinario = this.libreOfficeManager.salvarPDFTemp(pdf);
				processoDocumento.getProcessoDocumentoBin().setFile(arquivoBinario);
			} else {
				IOUtils.copy(pdf, new FileOutputStream(arquivoBinario));
			}
			
			processoDocumento.getProcessoDocumentoBin().setSize(size);
			DocumentoJudicialService.instance().updateMD5(processoDocumento.getProcessoDocumentoBin());
			processoDocumentoBinManager.persist(processoDocumento.getProcessoDocumentoBin());
			processoDocumentoBinManager.flush();
		} catch(Exception e) {
			facesMessages.add(Severity.ERROR, "Erro ao salvar o documento. Tente novamente.");
			e.printStackTrace();
		}
		
	}
	
	public Long getNewTaskId() {
		return newTaskId;
	}

	public void setNewTaskId(Long newTaskId) {
		this.newTaskId = newTaskId;
	}

	public Integer getIdProcessoDocumento() {
		return idProcessoDocumento;
	}

	public void setIdProcessoDocumento(Integer idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}
	
	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso= idProcesso;
	}
}
		