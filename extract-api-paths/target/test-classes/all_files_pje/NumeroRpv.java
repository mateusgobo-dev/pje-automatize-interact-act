package br.com.infox.cliente.component;

import br.com.infox.cliente.NumeroRpvUtil;

public class NumeroRpv {

	private String numeroRpv;
	private Integer ano;
	private Integer numeroOrigemProcesso;
	private Integer numeroSequencia;
	private Integer numeroVara;

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

	public Integer getNumeroVara() {
		return numeroVara;
	}

	public void setNumeroVara(Integer numeroVara) {
		this.numeroVara = numeroVara;
	}

	public Integer getNumeroOrigemProcesso() {
		return numeroOrigemProcesso;
	}

	public void setNumeroOrigemProcesso(Integer numeroOrigemProcesso) {
		this.numeroOrigemProcesso = numeroOrigemProcesso;
	}

	public String getNumeroRpv() {
		return numeroRpv;
	}

	public void setNumeroRpv(String numeroRpv) {
		this.numeroRpv = numeroRpv;
		setNumeroRpv(null, null, null, null);
		if (numeroRpv != null && !numeroRpv.matches("____.__.__.___.______")) {
			numeroRpv = numeroRpv.replace("_", " ");
			String[] nr = numeroRpv.split("[-\\.]");
			String ano = nr[0].trim();
			if (!ano.isEmpty()) {
				setAno(Integer.parseInt(ano));
			}
			String numeroOrigemProcesso = nr[1].trim() + nr[2].trim();
			if (!numeroOrigemProcesso.isEmpty()) {
				setNumeroOrigemProcesso(Integer.parseInt(numeroOrigemProcesso));
			}
			String numeroVara = nr[3].trim();
			if (!numeroVara.isEmpty()) {
				setNumeroVara(Integer.parseInt(numeroVara));
			}
			String numeroSequencia = nr[4].trim();
			if (!numeroSequencia.isEmpty()) {
				setNumeroSequencia(Integer.parseInt(numeroSequencia));
			}

		}
	}

	public void setNumeroRpv(Integer ano, Integer numeroOrigemProcesso, Integer numeroVara, Integer numeroSequencia) {
		setAno(ano);
		setNumeroOrigemProcesso(numeroOrigemProcesso);
		setNumeroVara(numeroVara);
		setNumeroSequencia(numeroSequencia);
	}

	@Override
	public String toString() {
		return NumeroRpvUtil.formatNumeroRpv(asInt(ano), asInt(numeroOrigemProcesso), asInt(numeroVara),
				asInt(numeroSequencia));
	}

	private int asInt(Integer i) {
		if (i == null)
			return 0;
		return i;
	}

	public static void main(String[] args) {
		NumeroRpv np = new NumeroRpv();
		np.setNumeroRpv("2011.84.00.007.300018");
		System.out.println(np);
	}

}