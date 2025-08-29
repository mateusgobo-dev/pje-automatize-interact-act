package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;
import java.util.Date;

public class ProcessoEventoDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer idProcessoEvento;
	private String codEvento;
	private String dsEvento;
	private Date dataAtualizacao;
	private String textoFinalExterno;

	
	public ProcessoEventoDTO(Integer idProcessoEvento, String codEvento, String dsEvento,
			Date dataAtualizacao, String textoFinalExterno) {
		super();
		this.idProcessoEvento = idProcessoEvento;
		this.codEvento = codEvento;
		this.dsEvento = dsEvento;
		this.dataAtualizacao = dataAtualizacao;
		this.textoFinalExterno = textoFinalExterno;
	}

	public Integer getIdProcessoEvento() {
		return idProcessoEvento;
	}

	public void setIdProcessoEvento(Integer idProcessoEvento) {
		this.idProcessoEvento = idProcessoEvento;
	}

	public String getCodEvento() {
		return codEvento;
	}

	public void setCodEvento(String codEvento) {
		this.codEvento = codEvento;
	}

	public String getDsEvento() {
		return dsEvento;
	}

	public void setDsEvento(String dsEvento) {
		this.dsEvento = dsEvento;
	}

	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	public String getTextoFinalExterno() {
		return textoFinalExterno;
	}

	public void setTextoFinalExterno(String textoFinalExterno) {
		this.textoFinalExterno = textoFinalExterno;
	}

}
