package br.com.infox.trf.distribuicao;

import java.io.Serializable;

public class VaraCargo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8494334245693271522L;
	private String nomeVara;
	private String nomeCargo;
	private int quantidadeClasse;
	private int quantidadeTotal;
	private int quantidadeInicialClasse;
	private int quantidadeInicialTotal;
	private int quantidadeDistribuidos;
	private float peso;

	public VaraCargo(String nomeVara, String nomeCargo, int quantidadeClasse, int quantidadeTotal) {
		this.nomeVara = nomeVara;
		this.nomeCargo = nomeCargo;
		this.quantidadeClasse = quantidadeClasse;
		this.quantidadeTotal = quantidadeTotal;
		quantidadeInicialClasse = this.quantidadeClasse;
		quantidadeInicialTotal = this.quantidadeTotal;
		quantidadeDistribuidos = 0;
	}

	public void incClasse() {
		quantidadeClasse++;
		quantidadeTotal++;
		quantidadeDistribuidos++;
	}

	public String getNomeVara() {
		return nomeVara;
	}

	public int getQuantidadeClasse() {
		return quantidadeClasse;
	}

	public int getQuantidadeTotal() {
		return quantidadeTotal;
	}

	@Override
	public int hashCode() {
		return nomeVara.hashCode();
	}

	public float getPeso() {
		return peso;
	}

	public void setPeso(float peso) {
		this.peso = peso;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof VaraCargo) {
			VaraCargo varaCargo = (VaraCargo) obj;
			return varaCargo.nomeVara.equals(nomeVara) && varaCargo.nomeCargo.equals(nomeCargo);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return nomeVara + "\t Qt classe: " + quantidadeClasse + "\t Qt total: " + quantidadeTotal + "\t Peso:" + peso;
	}

	public void setQuantidadeInicialClasse(int quantidadeInicialClasse) {
		this.quantidadeInicialClasse = quantidadeInicialClasse;
	}

	public int getQuantidadeInicialClasse() {
		return quantidadeInicialClasse;
	}

	public void setQuantidadeInicialTotal(int quantidadeInicialTotal) {
		this.quantidadeInicialTotal = quantidadeInicialTotal;
	}

	public int getQuantidadeInicialTotal() {
		return quantidadeInicialTotal;
	}

	public void setQuantidadeDistribuidos(int quantidadeDistribuidos) {
		this.quantidadeDistribuidos = quantidadeDistribuidos;
	}

	public int getQuantidadeDistribuidos() {
		return quantidadeDistribuidos;
	}

	public void setNomeCargo(String nomeCargo) {
		this.nomeCargo = nomeCargo;
	}

	public String getNomeCargo() {
		return nomeCargo;
	}

}