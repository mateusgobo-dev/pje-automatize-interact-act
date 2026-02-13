package br.jus.je.pje.business.dto;

import java.util.List;

public class ControleVersoesDocumentoDTO {
	private List<ControleVersaoDocumentoDTO> versoes;

	public List<ControleVersaoDocumentoDTO> getVersoes() {
		return versoes;
	}

	public void setVersoes(List<ControleVersaoDocumentoDTO> versoes) {
		this.versoes = versoes;
	}
}
