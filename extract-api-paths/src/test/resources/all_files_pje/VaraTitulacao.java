package br.com.infox.trf.distribuicao;

import br.jus.pje.nucleo.entidades.Cargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;

public class VaraTitulacao {

	private OrgaoJulgador vara;
	private Cargo cargo;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private int quantidadeClasse;
	private int quantidadeTotal;
	private int quantidadeInicialClasse;
	private int quantidadeInicialTotal;
	private int quantidadeDistribuidos;
	private float peso;

	public VaraTitulacao(OrgaoJulgador vara, Cargo cargo, int quantidadeClasse, int quantidadeTotal,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.vara = vara;
		this.cargo = cargo;
		this.quantidadeClasse = quantidadeClasse;
		this.quantidadeTotal = quantidadeTotal;
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
		quantidadeInicialClasse = this.quantidadeClasse;
		quantidadeInicialTotal = this.quantidadeTotal;
		quantidadeDistribuidos = 0;
	}

	public void incClasse() {
		quantidadeClasse++;
		quantidadeTotal++;
		quantidadeDistribuidos++;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return vara;
	}

	public int getQuantidadeClasse() {
		return quantidadeClasse;
	}

	public int getQuantidadeTotal() {
		return quantidadeTotal;
	}

	@Override
	public int hashCode() {
		return vara.hashCode();
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
		} else if (obj instanceof VaraTitulacao) {
			VaraTitulacao varaCargo = (VaraTitulacao) obj;
			return varaCargo.vara.equals(vara) && varaCargo.cargo.equals(cargo);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return vara + " - " + cargo;
	}

	public String toStringPesos() {
		return vara + " - " + cargo + "\t Qt classe: " + quantidadeClasse + "\t Qt total: " + quantidadeTotal
				+ "\t Peso:" + peso;
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

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	public Cargo getCargo() {
		return cargo;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

}