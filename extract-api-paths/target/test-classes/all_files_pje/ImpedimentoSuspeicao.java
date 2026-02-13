package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.enums.RegraImpedimentoSuspeicaoEnum;

@Entity
@Table(name = "tb_impedimento_suspeicao")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_impedimento_suspeicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_impedimento_suspeicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ImpedimentoSuspeicao implements Cloneable, Serializable, br.jus.pje.nucleo.entidades.IEntidade<ImpedimentoSuspeicao,Integer> {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = -5302357219994205427L;
	
	private Long id;
	private RegraImpedimentoSuspeicaoEnum regraImpedimentoSuspeicaoEnum;
	private Boolean poloAtivo;
	private Boolean poloPassivo;
	private Boolean poloIndefinido;
	private Pessoa pessoaParteAdvogado;
	private Estado estado;
	private Municipio municipio;
	private Eleicao eleicao;
	private String descricaoMotivo;
	private PessoaMagistrado pessoaMagistrado;

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(generator = "gen_impedimento_suspeicao")
	@Column(name = "id_impedimento_suspeicao", unique = true, nullable = false)
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name = "in_regra_impedimento_suspeicao", nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull
	public RegraImpedimentoSuspeicaoEnum getRegraImpedimentoSuspeicaoEnum() {
		return regraImpedimentoSuspeicaoEnum;
	}
	/**
	 * @param regraImpedimentoSuspeicaoEnum the regraImpedimentoSuspeicaoEnum to set
	 */
	public void setRegraImpedimentoSuspeicaoEnum(RegraImpedimentoSuspeicaoEnum regraImpedimentoSuspeicaoEnum) {
		this.regraImpedimentoSuspeicaoEnum = regraImpedimentoSuspeicaoEnum;
	}
	/**
	 * @return the poloAtivo
	 */
	@Column(name = "in_polo_ativo", nullable = true)
	public Boolean getPoloAtivo() {
		return poloAtivo;
	}
	/**
	 * @param poloAtivo the poloAtivo to set
	 */
	public void setPoloAtivo(Boolean poloAtivo) {
		this.poloAtivo = poloAtivo;
	}
	/**
	 * @return the poloPassivo
	 */
	@Column(name = "in_polo_passivo", nullable = true)
	public Boolean getPoloPassivo() {
		return poloPassivo;
	}
	/**
	 * @param poloPassivo the poloPassivo to set
	 */
	public void setPoloPassivo(Boolean poloPassivo) {
		this.poloPassivo = poloPassivo;
	}
	/**
	 * @return the poloIndefinido
	 */
	@Column(name = "in_polo_indefinido", nullable = true)
	public Boolean getPoloIndefinido() {
		return poloIndefinido;
	}
	/**
	 * @param poloIndefinido the poloIndefinido to set
	 */
	public void setPoloIndefinido(Boolean poloIndefinido) {
		this.poloIndefinido = poloIndefinido;
	}
	/**
	 * @return the pessoaParteAdvogado
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_parte_advogado", referencedColumnName = "id_pessoa")
	public Pessoa getPessoaParteAdvogado() {
		return pessoaParteAdvogado;
	}
	/**
	 * @param pessoaParteAdvogado the pessoaParteAdvogado to set
	 */
	public void setPessoaParteAdvogado(Pessoa pessoaParteAdvogado) {
		this.pessoaParteAdvogado = pessoaParteAdvogado;
	}
	/**
	 * @return the estado
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estado", referencedColumnName = "id_estado", nullable = true)
	public Estado getEstado() {
		return estado;
	}

	/**
	 * @param estado the estado to set
	 */
	public void setEstado(Estado estado) {
		this.estado = estado;
	}
	/**
	 * @return the municipio
	 */
	@ManyToOne
	@JoinColumn(name = "id_municipio", referencedColumnName = "id_municipio", nullable = true)
	public Municipio getMunicipio() {
		return municipio;
	}
	/**
	 * @param municipio the municipio to set
	 */
	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}
	/**
	 * @return the eleicao
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_eleicao", referencedColumnName = "id_eleicao", nullable = true)
	public Eleicao getEleicao() {
		return eleicao;
	}
	/**
	 * @param eleicao the eleicao to set
	 */
	public void setEleicao(Eleicao eleicao) {
		this.eleicao = eleicao;
	}
	/**
	 * @return the descricaoMotivo
	 */
	@Column(name = "ds_motivo", length = 3000)
	public String getDescricaoMotivo() {
		return descricaoMotivo;
	}
	/**
	 * @param descricaoMotivo the descricaoMotivo to set
	 */
	public void setDescricaoMotivo(String descricaoMotivo) {
		this.descricaoMotivo = descricaoMotivo;
	}
	/**
	 * @return the pessoaMagistrado
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_magistrado", referencedColumnName = "id", nullable = false)
	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}
	/**
	 * @param pessoaMagistrado the pessoaMagistrado to set
	 */
	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	/** (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImpedimentoSuspeicao other = (ImpedimentoSuspeicao) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
	@Override
	@javax.persistence.Transient
	public Class<? extends ImpedimentoSuspeicao> getEntityClass() {
		return ImpedimentoSuspeicao.class;
	}
	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getId().intValue());
	}
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	public ImpedimentoSuspeicao clone() throws CloneNotSupportedException {
		return (ImpedimentoSuspeicao)super.clone();
	}

}