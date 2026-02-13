package br.jus.pje.nucleo.dto.domicilioeletronico;

import java.util.List;

public class DownloadLotePessoasDTO {
	private String data;
	private List<String> mensagens;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public List<String> getMensagens() {
		return mensagens;
	}

	public void setMensagens(List<String> mensagens) {
		this.mensagens = mensagens;
	}

}
