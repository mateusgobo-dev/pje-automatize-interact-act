package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;
import java.util.Date;

public class ProcessoDocumentoDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String dsDocumento;
	private String tipoDocumento;
	private String dsConteudo;
	private String nomeUsuarioJuntada;
	private Date dataJuntada;
	private Boolean documentoSigiloso;

	public ProcessoDocumentoDTO(Integer id, String dsDocumento, String tipoDocumento, String dsConteudo,
			String nomeUsuarioJuntada, Date dataJuntada, Boolean documentoSigiloso) {
		super();
		this.id = id;
		this.dsDocumento = dsDocumento;
		this.tipoDocumento = tipoDocumento;
		this.dsConteudo = dsConteudo;
		this.nomeUsuarioJuntada = nomeUsuarioJuntada;
		this.dataJuntada = dataJuntada;
		this.documentoSigiloso = documentoSigiloso;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDsDocumento() {
		return dsDocumento;
	}

	public void setDsDocumento(String dsDocumento) {
		this.dsDocumento = dsDocumento;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getDsConteudo() {
		return dsConteudo;
	}

	public void setDsConteudo(String dsConteudo) {
		this.dsConteudo = dsConteudo;
	}

	public String getNomeUsuarioJuntada() {
		return nomeUsuarioJuntada;
	}

	public void setNomeUsuarioJuntada(String nomeUsuarioJuntada) {
		this.nomeUsuarioJuntada = nomeUsuarioJuntada;
	}

	public Date getDataJuntada() {
		return dataJuntada;
	}

	public void setDataJuntada(Date dataJuntada) {
		this.dataJuntada = dataJuntada;
	}

	public Boolean getDocumentoSigiloso() {
		return documentoSigiloso;
	}

	public void setDocumentoSigiloso(Boolean documentoSigiloso) {
		this.documentoSigiloso = documentoSigiloso;
	}

}
