package br.jus.pje.nucleo.dto.portal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;


public class RespostaTribunalDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NotNull
	private String protocoloPortal;
	
	@NotNull
	private String protocolo;
	
	@NotNull
	@JsonFormat(pattern="dd/MM/yyyy HH:mm:ss")
	private Date dataHora;
	
	@NotNull
    private Boolean sucesso;
	
    private List<String> erros = new ArrayList<>();

	@Override
	public String toString() {
		return "RespostaTribunalDTO [protocoloPortal=" + protocoloPortal + ", protocolo=" + protocolo + ", dataHora="
				+ dataHora + ", sucesso=" + sucesso + ", erros=" + erros + "]";
	}

	public String getProtocoloPortal() {
		return protocoloPortal;
	}

	public void setProtocoloPortal(String protocoloPortal) {
		this.protocoloPortal = protocoloPortal;
	}

	public String getProtocolo() {
		return protocolo;
	}

	public void setProtocolo(String protocolo) {
		this.protocolo = protocolo;
	}

	public Date getDataHora() {
		return dataHora;
	}

	public void setDataHora(Date dataHora) {
		this.dataHora = dataHora;
	}

	public Boolean getSucesso() {
		return sucesso;
	}

	public void setSucesso(Boolean sucesso) {
		this.sucesso = sucesso;
	}

	public List<String> getErros() {
		return erros;
	}

	public void setErros(List<String> erros) {
		this.erros = erros;
	}
}
