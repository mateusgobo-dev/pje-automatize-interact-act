package br.com.infox.cliente.bean;

import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.TipoParte;

public class RpvParteAdvogado {
	private PessoaAdvogado pessoaAdvogado;
	private Double valorPagoContratual;
	private Double valorPagoSucumbencia;
	private TipoParte tipoParte;

	public PessoaAdvogado getPessoaAdvogado() {
		return pessoaAdvogado;
	}

	public void setPessoaAdvogado(PessoaAdvogado pessoaAdvogado) {
		this.pessoaAdvogado = pessoaAdvogado;
	}

	public void setTipoParte(TipoParte tipoParte) {
		this.tipoParte = tipoParte;
	}

	public TipoParte getTipoParte() {
		return tipoParte;
	}

	public void setValorPagoContratual(Double valorPagoContratual) {
		this.valorPagoContratual = valorPagoContratual;
	}

	public Double getValorPagoContratual() {
		return valorPagoContratual;
	}

	public void setValorPagoSucumbencia(Double valorPagoSucumbencia) {
		this.valorPagoSucumbencia = valorPagoSucumbencia;
	}

	public Double getValorPagoSucumbencia() {
		return valorPagoSucumbencia;
	}
}