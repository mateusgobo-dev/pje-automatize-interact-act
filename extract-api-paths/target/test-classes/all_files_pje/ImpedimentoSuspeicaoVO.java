package br.jus.cnj.pje.entidades.vo;

import java.io.Serializable;
import br.jus.pje.nucleo.entidades.ImpedimentoSuspeicao;

public class ImpedimentoSuspeicaoVO implements Serializable {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 8834394394821072120L;
	
	private String numeroProcesso;
	
	private String orgaoJulgador;
	
	private ImpedimentoSuspeicao impedimentoSuspeicao;
	/**
	 * @return the numeroProcesso
	 */
	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	/**
	 * @param numeroProcesso the numeroProcesso to set
	 */
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
	/**
	 * @return the impedimentoSuspeicao
	 */
	public ImpedimentoSuspeicao getImpedimentoSuspeicao() {
		return impedimentoSuspeicao;
	}
	/**
	 * @param impedimentoSuspeicao the impedimentoSuspeicao to set
	 */
	public void setImpedimentoSuspeicao(ImpedimentoSuspeicao impedimentoSuspeicao) {
		this.impedimentoSuspeicao = impedimentoSuspeicao;
	}
	/**
	 * @return the orgaoJulgador
	 */
	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}
	/**
	 * @param orgaoJulgador the orgaoJulgador to set
	 */
	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
}