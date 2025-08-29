/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author cristof
 *
 */
@Entity
@Table(name = "tb_processo_instance")
public class ProcessoInstance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long idProcessoInstance;
	private Integer idProcesso;
	private Integer idLocalizacao;
	private Integer orgaoJulgadorCargo;
	private Integer orgaoJulgadorColegiado;
	private Boolean ativo = true;

	@Id
	@Column(name = "id_proc_inst")
	public Long getIdProcessoInstance() {
		return idProcessoInstance;
	}
	
	public void setIdProcessoInstance(Long idProcessoInstance) {
		this.idProcessoInstance = idProcessoInstance;
	}
	
	@Column(name = "id_processo")
	public Integer getIdProcesso() {
		return idProcesso;
	}
	
	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}

	@Column(name = "id_localizacao")
	public Integer getIdLocalizacao() {
		return idLocalizacao;
	}

	public void setIdLocalizacao(Integer idLocalizacao) {
		this.idLocalizacao = idLocalizacao;
	}

	@Column(name = "id_orgao_julgador_cargo")
	public Integer getOrgaoJulgadorCargo() {
		return orgaoJulgadorCargo;
	}

	public void setOrgaoJulgadorCargo(Integer orgaoJulgadorCargo) {
		this.orgaoJulgadorCargo = orgaoJulgadorCargo;
	}

	@Column(name = "id_orgao_julgador_colegiado")
	public Integer getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(Integer orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@Column(name="in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
}
