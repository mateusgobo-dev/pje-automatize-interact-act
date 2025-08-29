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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.jt.enums.ValorPesoEnum;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = OrgaoJulgadorCargo.TABLE_NAME)

@DynamicUpdate(value = true)
@org.hibernate.annotations.GenericGenerator(name = "gen_orgao_julgador_cargo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_orgao_julgador_cargo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoJulgadorCargo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoJulgadorCargo,Integer> {

	private static final long serialVersionUID = -4714764337694000333L;

	public static final String TABLE_NAME = "tb_orgao_julgador_cargo";

	private int idOrgaoJulgadorCargo;
	private OrgaoJulgador orgaoJulgador;
	private Cargo cargo;
	private Boolean recebeDistribuicao;
	private Boolean auxiliar;
	private List<ModificacaoParametroDistribuicao> modificacoesParametroDistribuicao = new ArrayList<ModificacaoParametroDistribuicao>();
    private Long version = 0L;
	/**
	 * Inclusões do CNJ
	 */
	private String descricao;
	private String sigla;
	private boolean colegiado = false;
	private List<Competencia> competencia = new ArrayList<Competencia>(0);
	private Double pesoDistribuicao;
	private Double acumuladorDistribuicao;
	private Double acumuladorProcesso;
	private Double valorPeso;
	private Boolean ativo;
	
	private ValorPesoEnum valorPesoEnum;
	
	private Set<VinculacaoDependenciaEleitoral> vinculacoesDependencia = new HashSet<VinculacaoDependenciaEleitoral>(0);
	
	public OrgaoJulgadorCargo() {
	}

	@Id
	@GeneratedValue(generator = "gen_orgao_julgador_cargo")
	@Column(name = "id_orgao_julgador_cargo", unique = true, nullable = false)
	public int getIdOrgaoJulgadorCargo() {
		return this.idOrgaoJulgadorCargo;
	}

	public void setIdOrgaoJulgadorCargo(int idOrgaoJulgadorCargo) {
		this.idOrgaoJulgadorCargo = idOrgaoJulgadorCargo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgador() {
		return this.orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo", nullable = false)
	@NotNull
	public Cargo getCargo() {
		return this.cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	@Column(name = "in_recebe_distribuicao", nullable = false)
	@NotNull
	public Boolean getRecebeDistribuicao() {
		return this.recebeDistribuicao;
	}

	public void setRecebeDistribuicao(Boolean recebeDistribuicao) {
		this.recebeDistribuicao = recebeDistribuicao;
	}

	@Column(name = "in_auxiliar", nullable = false)
	@NotNull
	public Boolean getAuxiliar() {
		return this.auxiliar;
	}

	public void setAuxiliar(Boolean auxiliar) {
		this.auxiliar = auxiliar;
	}

	/**
	 * 
	 * Getters and Setters implementados pelo CNJ
	 */

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "ds_cargo", nullable = false, length = 100, unique = true)
	@Length(max = 100)
	@NotNull
	public String getDescricao() {
		return descricao;
	}

	@Column(name = "cd_sigla_cargo", length = 20)
	@Length(max = 20)
	public String getSigla() {
		return this.sigla;
	}

	public void setSigla(String sigla) {
		this.sigla = sigla;
	}

	@Column(name = "in_colegiado", nullable = false)
	@NotNull
	public boolean getColegiado() {
		return colegiado;
	}

	public void setColegiado(boolean colegiado) {
		this.colegiado = colegiado;
	}

	@Column(name = "nr_peso_distribuicao", nullable = true)
	public Double getPesoDistribuicao() {
		return pesoDistribuicao;
	}

	public void setPesoDistribuicao(Double pesoDistribuicao) {
		this.pesoDistribuicao = pesoDistribuicao;
	}

	@Transient
	public List<Competencia> getCompetencia() {
		if (this.orgaoJulgador != null) {
			for (OrgaoJulgadorCompetencia ojc : this.orgaoJulgador.getOrgaoJulgadorCompetenciaList()) {
				if (ojc.getCompetencia() != null)
					competencia.add(ojc.getCompetencia());
			}
		}
		return competencia;
	}

	public void setCompetencia(List<Competencia> competencia) {
		this.competencia = competencia;
		if (this.orgaoJulgador != null) {
			List<OrgaoJulgadorCompetencia> listaOJC = this.orgaoJulgador.getOrgaoJulgadorCompetenciaList();
			for (Competencia c : competencia) {
				OrgaoJulgadorCompetencia ojc = new OrgaoJulgadorCompetencia();
				ojc.setCompetencia(c);
				ojc.setDataInicio(new Date());
				ojc.setOrgaoJulgador(this.orgaoJulgador);
				ojc.setIdOrgaoJulgadorCompetencia(this.getOrgaoJulgador().getIdOrgaoJulgador());
				if (!listaOJC.contains(ojc)) {
					listaOJC.add(ojc);
				}
			}
		}
	}

	@Column(name = "nr_acumulador_distribuicao", nullable = true)
	public Double getAcumuladorDistribuicao() {
		return acumuladorDistribuicao;
	}

	public void setAcumuladorDistribuicao(Double acumuladorDistribuicao) {
		this.acumuladorDistribuicao = acumuladorDistribuicao;
	}

	@Column(name = "nr_acumulador_processo", nullable = true)
	public Double getAcumuladorProcesso() {
		return acumuladorProcesso;
	}

	public void setAcumuladorProcesso(Double acumuladorProcesso) {
		this.acumuladorProcesso = acumuladorProcesso;
	}

	public void setValorPeso(Double valorPeso) {
		this.valorPeso = valorPeso;		
	}
	
	@Column(name = "vl_peso", nullable = false)
	@NotNull
	public Double getValorPeso() {
		if (this.valorPeso!=null){
			this.valorPesoEnum = ValorPesoEnum.getEnum(this.valorPeso);					
		}
		return valorPeso;		
	}
	
	@Transient
	public ValorPesoEnum getValorPesoEnum() {
		return valorPesoEnum;
	}
	
	public void setValorPesoEnum(ValorPesoEnum valorPesoEnum) {		
		this.valorPesoEnum = valorPesoEnum;
		this.valorPeso = valorPesoEnum.getValor();
	}
	
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return getOrgaoJulgador().getOrgaoJulgador() + " / " + getDescricao();
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "orgaoJulgadorCargo")
	public List<ModificacaoParametroDistribuicao> getModificacoesParametroDistribuicao() {
		return modificacoesParametroDistribuicao;
	}

	public void setModificacoesParametroDistribuicao(
			List<ModificacaoParametroDistribuicao> modificacoesParametroDistribuicao) {
		this.modificacoesParametroDistribuicao = modificacoesParametroDistribuicao;
	}
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="cargoJudicial")
	public Set<VinculacaoDependenciaEleitoral> getVinculacoesDependencia() {
		return vinculacoesDependencia;
	}

	public void setVinculacoesDependencia(
			Set<VinculacaoDependenciaEleitoral> vinculacoesDependencia) {
		this.vinculacoesDependencia = vinculacoesDependencia;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdOrgaoJulgadorCargo();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof OrgaoJulgadorCargo))
			return false;
		OrgaoJulgadorCargo other = (OrgaoJulgadorCargo) obj;
		if (getIdOrgaoJulgadorCargo() != other.getIdOrgaoJulgadorCargo())
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoJulgadorCargo> getEntityClass() {
		return OrgaoJulgadorCargo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdOrgaoJulgadorCargo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
	
	@Version
	@Column(name = "version")
	public long getVersion() {
		return version;
	}
	
	public void setVersion(long version) {
		this.version = version;
	}

}
