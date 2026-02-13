package br.com.infox.cliente.util;

public class ValorComplementoSegmentado {

	private String texto;
	private String valorComplemento;

	public ValorComplementoSegmentado() {
	}

	public ValorComplementoSegmentado(String texto, String valorComplemento) {
		this.texto = texto;
		this.valorComplemento = valorComplemento;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public String getValorComplemento() {
		return valorComplemento;
	}

	public void setValorComplemento(String valorComplemento) {
		this.valorComplemento = valorComplemento;
	}
}
