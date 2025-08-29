package br.jus.je.pje.entity.vo;

import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


public class TrasladoDocumentosVO {
	
	
	private ProcessoTrf processo;
	
	private List<ProcessoDocumento> documentosSelecionados;
	
	public TrasladoDocumentosVO(ProcessoTrf processo, List<ProcessoDocumento> documentosSelecionados) {
		super();
		this.processo = processo;
		this.documentosSelecionados = documentosSelecionados;
	}

	public TrasladoDocumentosVO() {
		documentosSelecionados = new ArrayList<ProcessoDocumento>(); 
	}

	public ProcessoTrf getProcesso() {
		return processo;
	}

	public void setProcesso(ProcessoTrf processo) {
		this.processo = processo;
	}

	public List<ProcessoDocumento> getDocumentosSelecionados() {
		return documentosSelecionados;
	}

	public void setDocumentosSelecionados(List<ProcessoDocumento> documentosSelecionados) {
		this.documentosSelecionados = documentosSelecionados;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TrasladoDocumentosVO)) {
			return false;
		}
		TrasladoDocumentosVO other = (TrasladoDocumentosVO) obj;
		if (other.getProcesso() == null) {
			return false;
		}
		return other.getProcesso().equals(getProcesso());
	}

}
