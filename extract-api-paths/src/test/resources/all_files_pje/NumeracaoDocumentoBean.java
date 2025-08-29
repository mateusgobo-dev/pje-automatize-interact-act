package br.com.infox.editor.bean;

import br.jus.pje.nucleo.enums.TipoNumeracaoEnum;

public class NumeracaoDocumentoBean {

	private int nivel;
	private TipoNumeracaoEnum tipoNumeracao;
	private String separador;

	public NumeracaoDocumentoBean(int nivel, TipoNumeracaoEnum tipoNumeracao, String separador) {
		this.nivel = nivel;
		this.tipoNumeracao = tipoNumeracao;
		this.separador = separador;
	}

	public int getNivel() {
		return nivel;
	}

	public void setNivel(int nivel) {
		this.nivel = nivel;
	}

	public TipoNumeracaoEnum getTipoNumeracao() {
		return tipoNumeracao;
	}

	public void setTipoNumeracao(TipoNumeracaoEnum tipoNumeracao) {
		this.tipoNumeracao = tipoNumeracao;
	}

	public String getSeparador() {
		return separador;
	}

	public void setSeparador(String separador) {
		this.separador = separador;
	}
}
