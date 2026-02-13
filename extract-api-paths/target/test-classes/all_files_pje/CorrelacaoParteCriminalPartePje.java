package br.jus.cnj.pje.intercomunicacao.v223.servico;

import br.jus.cnj.intercomunicacao.v223.criminal.Parte;
import br.jus.pje.nucleo.entidades.ProcessoParte;

public class CorrelacaoParteCriminalPartePje {
	private Integer posicao;
	private Parte parteCriminal;
	private br.jus.cnj.intercomunicacao.v223.beans.Parte parteMni;
	private ProcessoParte partePje;

	public Integer getPosicao() {
		return posicao;
	}

	public void setPosicao(Integer posicao) {
		this.posicao = posicao;
	}

	public Parte getParteCriminal() {
		return parteCriminal;
	}

	public void setParteCriminal(Parte parteCriminal) {
		this.parteCriminal = parteCriminal;
	}

	public br.jus.cnj.intercomunicacao.v223.beans.Parte getParteMni() {
		return parteMni;
	}

	public void setParteMni(br.jus.cnj.intercomunicacao.v223.beans.Parte parteMni) {
		this.parteMni = parteMni;
	}

	public ProcessoParte getPartePje() {
		return partePje;
	}

	public void setPartePje(ProcessoParte partePje) {
		this.partePje = partePje;
	}

	public boolean isParteCriminalJaCorrelacionada() {
		return this.parteCriminal != null;
	}

	public boolean todasAsCorrelacoesEstaoPreenchidas() {
		return parteCriminal != null && parteMni != null && partePje != null;
	}

}
