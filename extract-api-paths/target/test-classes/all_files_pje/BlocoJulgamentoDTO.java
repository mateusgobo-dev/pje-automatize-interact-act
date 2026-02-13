package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.BlocoJulgamento;

public class BlocoJulgamentoDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private boolean check;
	private BlocoJulgamento bloco;
	
	public BlocoJulgamentoDTO(BlocoJulgamento bloco, boolean check) {
		this.bloco = bloco;
		this.check = check;
	}

	public BlocoJulgamento getBloco() {
		return bloco;
	}
	
	public boolean isCheck() {
		return check;
	}
	
	public void setBloco(BlocoJulgamento bloco) {
		this.bloco = bloco;
	}
	
	public void setCheck(boolean check) {
		this.check = check;
	}
}