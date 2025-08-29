package br.jus.csjt.pje.commons.model.dto;

import java.io.Serializable;

import br.jus.pje.jt.enums.CredorDevedorEnum;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;

public class ObrigacaoFazerParteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private ProcessoParte parte;
	private boolean selecionado;
	private CredorDevedorEnum credorDevedor;

	public ProcessoParte getParte() {
		return parte;
	}

	public void setParte(ProcessoParte parte) {
		this.parte = parte;
	}

	public boolean getSelecionado() {
		return selecionado;
	}

	public void setSelecionado(boolean selecionado) {
		this.selecionado = selecionado;
	}

	public CredorDevedorEnum getCredorDevedor() {
		return credorDevedor;
	}

	public void setCredorDevedor(CredorDevedorEnum credorDevedor) {
		this.credorDevedor = credorDevedor;
	}

	public String getPolo() {
		String papel = "";
		String inativo = "";

		if (Pessoa.instanceOf(parte.getPessoa(), PessoaAdvogado.class)) {
			papel = "ADVOGADO - ";
		}

		if (!isAtivoNoProcesso()) {
			inativo = " - Inativo/Suspenso";
		}

		return papel + parte.getPolo() + inativo;
	}

	public boolean isAtivoNoProcesso() {
		// Se for Inativo ou Suspenso não permitir a seleção na grid de partes.
		return parte.getInSituacao() == ProcessoParteSituacaoEnum.A;
	}

}
