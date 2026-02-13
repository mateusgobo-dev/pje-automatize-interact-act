package br.jus.pje.nucleo.dto.domicilioeletronico;

import java.util.List;

public class LotePessoasDTO {
	public DataDTO data;
	public List<String> mensagens;

	public DataDTO getData() {
		return data;
	}

	public void setData(DataDTO data) {
		this.data = data;
	}

	public List<String> getMensagens() {
		return mensagens;
	}

	public void setMensagens(List<String> mensagens) {
		this.mensagens = mensagens;
	}

}
