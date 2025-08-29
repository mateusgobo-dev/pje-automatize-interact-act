package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;

public class AutoProcessualDTO implements Serializable {
	private static final long serialVersionUID = 8418077339751737085L;

	private ProcessoDocumento documento = null;
	private ProcessoEvento movimento = null;
	private int indice = 0;
	private Integer idDocumentoFavorito = null;
	
	public AutoProcessualDTO() {
		this(null, null);
	}
	
	public AutoProcessualDTO(ProcessoDocumento documento, ProcessoEvento movimento) {
		this(documento, movimento, 0, null);
	}

	public AutoProcessualDTO(ProcessoDocumento documento, ProcessoEvento movimento, int indice, Integer idDocumentoFavorito) {
		this.documento = documento;
		this.movimento = movimento;
		this.indice = indice;
		this.setIdDocumentoFavorito(idDocumentoFavorito);
	}

	public ProcessoDocumento getDocumento() {
		return documento;
	}
	public void setDocumento(ProcessoDocumento documento) {
		this.documento = documento;
	}
	public ProcessoEvento getMovimento() {
		return movimento;
	}
	public void setMovimento(ProcessoEvento movimento) {
		this.movimento = movimento;
	}

	public int getIndice() {
		return indice;
	}

	public void setIndice(int indice) {
		this.indice = indice;
	}

	public Integer getIdDocumentoFavorito() {
		return idDocumentoFavorito;
	}

	public void setIdDocumentoFavorito(Integer idDocumentoFavorito) {
		this.idDocumentoFavorito = idDocumentoFavorito;
	}
	
}
