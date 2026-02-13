package br.com.infox.cliente.component;

import java.io.Serializable;
import java.util.StringTokenizer;

import br.com.infox.cliente.NumeroProcessoUtil;

public class NumeroProcesso implements Serializable {

	private static final long serialVersionUID = 1L;

	private String numeroProcesso;
	private Integer numeroSequencia;
	private Integer ano;
	private Integer numeroDigitoVerificador;
	private Integer numeroOrgaoJustica;
	private Integer numeroOrigem;

	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Integer getNumeroDigitoVerificador() {
		return numeroDigitoVerificador;
	}

	public void setNumeroDigitoVerificador(Integer numeroDigitoVerificador) {
		this.numeroDigitoVerificador = numeroDigitoVerificador;
	}

	public Integer getNumeroOrgaoJustica() {
		return numeroOrgaoJustica;
	}

	public void setNumeroOrgaoJustica(Integer numeroOrgaoJustica) {
		this.numeroOrgaoJustica = numeroOrgaoJustica;
	}

	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
		setNumeroProcesso(null, null, null, null, null);
		if (numeroProcesso != null && !numeroProcesso.matches("_______-__.____._.__.____")) {
			numeroProcesso = numeroProcesso.replace("_", " ");

			StringTokenizer st = new StringTokenizer(numeroProcesso, "-.");

			String numeroSequencia = st.hasMoreElements() ? st.nextToken().trim() : "";
			if (!numeroSequencia.isEmpty()) {
				setNumeroSequencia(Integer.parseInt(numeroSequencia));
			}
			String numeroDigitoVerificador = st.hasMoreElements() ? st.nextToken().trim() : "";
			if (!numeroDigitoVerificador.isEmpty()) {
				setNumeroDigitoVerificador(Integer.parseInt(numeroDigitoVerificador));
			}
			String ano = st.hasMoreElements() ? st.nextToken().trim() : "";
			if (!ano.isEmpty()) {
				setAno(Integer.parseInt(ano));
			}
			String numeroOrgaoJustica = st.hasMoreElements() ? st.nextToken().trim() + st.nextToken().trim() : "";
			if (!numeroOrgaoJustica.isEmpty()) {
				setNumeroOrgaoJustica(Integer.parseInt(numeroOrgaoJustica));
			}
			String numeroOrigem = st.hasMoreElements() ? st.nextToken().trim() : "";
			if (!numeroOrigem.isEmpty()) {
				setNumeroOrigem(Integer.parseInt(numeroOrigem));
			}
		}
	}

	public void setNumeroProcesso(Integer numeroSequencia, Integer ano, Integer numeroDigitoVerificador,
			Integer numeroOrgaoJustica, Integer numeroOrigem) {
		setNumeroSequencia(numeroSequencia);
		setAno(ano);
		setNumeroDigitoVerificador(numeroDigitoVerificador);
		setNumeroOrgaoJustica(numeroOrgaoJustica);
		setNumeroOrigem(numeroOrigem);
	}

	@Override
	public String toString() {
		return NumeroProcessoUtil.formatNumeroProcesso(asInt(numeroSequencia), asInt(numeroDigitoVerificador),
				asInt(ano), asInt(numeroOrgaoJustica), asInt(numeroOrigem));
	}

	private int asInt(Integer i) {
		if (i == null)
			return 0;
		return i;
	}

}