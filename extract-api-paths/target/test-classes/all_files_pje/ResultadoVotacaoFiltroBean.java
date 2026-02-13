package br.com.jt.pje.bean;

import br.jus.pje.jt.enums.ResultadoVotacaoEnum;

public class ResultadoVotacaoFiltroBean {

	private ResultadoVotacaoEnum resultadoVotacaoEnum;
	private boolean check;

	public ResultadoVotacaoEnum getResultadoVotacaoEnum() {
		return resultadoVotacaoEnum;
	}

	public void setResultadoVotacaoEnum(
			ResultadoVotacaoEnum resultadoVotacaoEnum) {
		this.resultadoVotacaoEnum = resultadoVotacaoEnum;
	}

	public boolean getCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

}
