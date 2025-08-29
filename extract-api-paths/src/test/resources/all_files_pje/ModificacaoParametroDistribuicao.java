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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "tb_modif_para_distribuicao")
@org.hibernate.annotations.GenericGenerator(name = "gen_mod_param_distribuicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_mod_param_distribuicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ModificacaoParametroDistribuicao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ModificacaoParametroDistribuicao,Integer> {

	private static final long serialVersionUID = -8960974139934281795L;
	private int idModificacaoParametroDistribuicao;
	private String descricao;
	private String atoNormativo;
	private OrgaoJulgadorCargo orgaoJulgadorCargo;
	private Date dataInclusao;
	private Usuario responsavel;
	private Double valorAntigoAcumuladorDistribuicao;

	@Id
	@GeneratedValue(generator = "gen_mod_param_distribuicao")
	@Column(name = "id", unique = true, nullable = false)
	public int getIdModificacaoParametroDistribuicao() {
		return idModificacaoParametroDistribuicao;
	}

	public void setIdModificacaoParametroDistribuicao(int idModificacaoParametroDistribuicao) {
		this.idModificacaoParametroDistribuicao = idModificacaoParametroDistribuicao;
	}

	@Column(name = "descricao", length = 255)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "ato_normativo")
	public String getAtoNormativo() {
		return atoNormativo;
	}

	public void setAtoNormativo(String atoNormativo) {
		this.atoNormativo = atoNormativo;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "orgao_julgador_cargo", nullable = false)
	public OrgaoJulgadorCargo getOrgaoJulgadorCargo() {
		return orgaoJulgadorCargo;
	}

	public void setOrgaoJulgadorCargo(OrgaoJulgadorCargo orgaoJulgadorCargo) {
		this.orgaoJulgadorCargo = orgaoJulgadorCargo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_inclusao")
	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "responsavel", nullable = false)
	public Usuario getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(Usuario responsavel) {
		this.responsavel = responsavel;
	}	
	
	@Column(name = "vr_antgo_acmldor_distribuicao", nullable = true)
	public Double getValorAntigoAcumuladorDistribuicao() {
		return valorAntigoAcumuladorDistribuicao;
	}

	public void setValorAntigoAcumuladorDistribuicao(Double valorAntigoAcumuladorDistribuicao) {
		this.valorAntigoAcumuladorDistribuicao = valorAntigoAcumuladorDistribuicao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ModificacaoParametroDistribuicao> getEntityClass() {
		return ModificacaoParametroDistribuicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdModificacaoParametroDistribuicao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
