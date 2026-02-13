package br.jus.cnj.pje.vo;

public class AdvogadoParteMural {
    private String nomeAdvogado;
    private String numeroOAB;
    private String ufOAB;

    public String getNome() {
	return nomeAdvogado;
    }

    public void setNomeAdvogado(String nomeAdvogado) {
	this.nomeAdvogado = nomeAdvogado;
    }

    public String getNumeroOAB() {
	return numeroOAB;
    }

    public void setNumeroOAB(String numeroOAB) {
	this.numeroOAB = numeroOAB;
    }

    public String getUfOAB() {
	return ufOAB;
    }

    public void setUfOAB(String ufOAB) {
	this.ufOAB = ufOAB;
    }
}