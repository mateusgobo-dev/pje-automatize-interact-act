package br.jus.pje.nucleo.dto.portal;

import java.io.Serializable;

public class RespostaTribunalPeticaoInicialDTO extends RespostaTribunalDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private DistribuicaoDTO distribuicao;

	public DistribuicaoDTO getDistribuicao() {
		return distribuicao;
	}

	public void setDistribuicao(DistribuicaoDTO distribuicao) {
		this.distribuicao = distribuicao;
	}
}
