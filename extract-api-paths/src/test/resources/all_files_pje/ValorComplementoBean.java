package br.com.infox.ibpm.component.tree;

import java.io.Serializable;

/**
 * Bean que representa a abstração de um valor de complemento da modelagem de
 * movimentos com complementos.
 * 
 * Os dados preenchidos pelo usuário na tela serão carregados por este objeto.
 * 
 * @author David, Kelly
 */
public class ValorComplementoBean implements Serializable {

	private static final long serialVersionUID = -260360503704764662L;

	private String valor = "";
	private String codigo = "";

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((valor == null) ? 0 : valor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValorComplementoBean other = (ValorComplementoBean) obj;
		if (valor == null) {
			if (other.valor != null)
				return false;
		} else if (!valor.equals(other.valor))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ValorComplementoBean [valor=" + valor + ", codigo=" + codigo + "]";
	}

	// Metodos utilizados para o binding da tela
	public void setValorComplementoBean(ValorComplementoBean vcb) {
		if (vcb != null) {
			this.setCodigo(vcb.getCodigo());
			this.setValor(vcb.getValor());
		} else {
			this.setCodigo("");
			this.setValor("");
		}
	}

	public ValorComplementoBean getValorComplementoBean() {
		return this;
	}
}
