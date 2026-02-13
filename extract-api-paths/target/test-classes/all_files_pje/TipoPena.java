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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.Parent;
import br.jus.pje.nucleo.enums.GeneroPenaEnum;

@Entity
@Table(name = "tb_tipo_pena")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_pena", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_pena"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoPena implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoPena,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idTipoPena;
	private String dsTipoPena;
	private String dsSigla;
	private TipoPena tipoPenaPai;
	private Boolean inTempoAno;
	private Boolean inTempoMes;
	private Boolean inTempoDia;
	private Boolean inTempoHoras;
	private Boolean inValor;
	private Boolean inQuantidadeDiasMulta;
	private Boolean inTipoBem;
	private Boolean inDescricaoBem;
	private Boolean inDescricaoLocal;
	private Boolean inPenaRestritivaDireito;
	private Boolean inPenaPrivativaLiberdade;
	private Boolean inMulta;
	private Boolean ativo;
	private GeneroPenaEnum generoPena;

	public TipoPena() {

	}

	public TipoPena(Integer id) {
		this.idTipoPena = id;
	}

	@Override
	public String toString() {
		return this.getDsTipoPena();
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_pena")
	@Column(name = "id_tipo_pena", unique = true, nullable = false)
	public Integer getIdTipoPena() {
		return idTipoPena;
	}

	public void setIdTipoPena(Integer idTipoPena) {
		this.idTipoPena = idTipoPena;
	}

	@Column(name = "ds_tipo_pena", unique = true, length = 100)
	@NotNull
	@Length(max = 100)
	public String getDsTipoPena() {
		return dsTipoPena;
	}

	public void setDsTipoPena(String dsTipoPena) {
		this.dsTipoPena = dsTipoPena;
	}

	@Column(name = "ds_sigla", length = 10)
	@Length(max = 10)
	public String getDsSigla() {
		return dsSigla;
	}

	public void setDsSigla(String dsSigla) {
		this.dsSigla = dsSigla;
	}

	@ManyToOne
	@JoinColumn(name = "id_tipo_pena_pai", nullable = true)
	@Parent
	public TipoPena getTipoPenaPai() {
		return tipoPenaPai;
	}

	public void setTipoPenaPai(TipoPena tipoPenaPai) {
		this.tipoPenaPai = tipoPenaPai;
	}

	@Column(name = "in_tempo_ano", nullable = false)
	@NotNull
	public Boolean getInTempoAno() {
		return inTempoAno;
	}

	public void setInTempoAno(Boolean inTempoAno) {
		this.inTempoAno = inTempoAno;
	}

	@Column(name = "in_tempo_mes", nullable = false)
	@NotNull
	public Boolean getInTempoMes() {
		return inTempoMes;
	}

	public void setInTempoMes(Boolean inTempoMes) {
		this.inTempoMes = inTempoMes;
	}

	@Column(name = "in_tempo_dia", nullable = false)
	@NotNull
	public Boolean getInTempoDia() {
		return inTempoDia;
	}

	public void setInTempoDia(Boolean inTempoDia) {
		this.inTempoDia = inTempoDia;
	}

	@Column(name = "in_tempo_horas", nullable = false)
	@NotNull
	public Boolean getInTempoHoras() {
		return inTempoHoras;
	}

	public void setInTempoHoras(Boolean inTempoHoras) {
		this.inTempoHoras = inTempoHoras;
	}

	@Column(name = "in_valor", nullable = false)
	@NotNull
	public Boolean getInValor() {
		return inValor;
	}

	public void setInValor(Boolean inValor) {
		this.inValor = inValor;
	}

	@Column(name = "in_qtd_dias_multa", nullable = false)
	@NotNull
	public Boolean getInQuantidadeDiasMulta() {
		return inQuantidadeDiasMulta;
	}

	public void setInQuantidadeDiasMulta(Boolean inQuantidadeDiasMulta) {
		this.inQuantidadeDiasMulta = inQuantidadeDiasMulta;
	}

	@Column(name = "in_tipo_bem", nullable = false)
	@NotNull
	public Boolean getInTipoBem() {
		return inTipoBem;
	}

	public void setInTipoBem(Boolean inTipoBem) {
		this.inTipoBem = inTipoBem;
	}

	@Column(name = "in_descricao_bem", nullable = false)
	@NotNull
	public Boolean getInDescricaoBem() {
		return inDescricaoBem;
	}

	public void setInDescricaoBem(Boolean inDescricaoBem) {
		this.inDescricaoBem = inDescricaoBem;
	}

	@Column(name = "in_descricao_local", nullable = false)
	@NotNull
	public Boolean getInDescricaoLocal() {
		return inDescricaoLocal;
	}

	public void setInDescricaoLocal(Boolean inDescricaoLocal) {
		this.inDescricaoLocal = inDescricaoLocal;
	}

	@Column(name = "in_pena_restritiva_direito", nullable = false)
	@NotNull
	public Boolean getInPenaRestritivaDireito() {
		return inPenaRestritivaDireito;
	}

	public void setInPenaRestritivaDireito(Boolean inPenaRestritivaDireito) {
		this.inPenaRestritivaDireito = inPenaRestritivaDireito;
	}

	@Column(name = "in_pena_privativa_liberdade", nullable = false)
	@NotNull
	public Boolean getInPenaPrivativaLiberdade() {
		return inPenaPrivativaLiberdade;
	}

	public void setInPenaPrivativaLiberdade(Boolean inPenaPrivativaLiberdade) {
		this.inPenaPrivativaLiberdade = inPenaPrivativaLiberdade;
	}

	@Column(name = "in_multa", nullable = false)
	@NotNull
	public Boolean getInMulta() {
		return inMulta;
	}

	public void setInMulta(Boolean inMulta) {
		this.inMulta = inMulta;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_genero_pena", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	public GeneroPenaEnum getGeneroPena() {
		return generoPena;
	}

	public void setGeneroPena(GeneroPenaEnum generoPena) {
		this.generoPena = generoPena;
	}

	@Transient
	public boolean isExigeDadosPrivativaLiberdade() {
		return (getInTempoAno() || getInTempoMes() || getInTempoDia() || getInTempoHoras());
	}

	@Transient
	public boolean isExigeDadosMultaDias() {
		return getInQuantidadeDiasMulta();
	}

	@Transient
	public boolean isExigeDadosMultaValor() {
		return getInValor();
	}

	@Transient
	public boolean isExigeDadosRestritivaDireito() {
		return (getInDescricaoBem() || getInDescricaoLocal());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ativo == null) ? 0 : ativo.hashCode());
		result = prime * result + ((dsSigla == null) ? 0 : dsSigla.hashCode());
		result = prime * result
				+ ((generoPena == null) ? 0 : generoPena.hashCode());
		result = prime * result
				+ ((idTipoPena == null) ? 0 : idTipoPena.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if (!(obj instanceof TipoPena)){
			return false;
		}
		TipoPena other = (TipoPena) obj;
		if (other.getIdTipoPena() == null || this.getIdTipoPena() == null) {
			if (getGeneroPena() == null || other.getGeneroPena() == null) {
				return false;
			} else if (generoPena != null && other.getGeneroPena() != null
					&& !generoPena.equals(other.getGeneroPena())) {
				return false;
			}

			if (getDsSigla() == null || other.getDsSigla() == null) {
				return false;
			} else if (dsSigla != null && other.getDsSigla() != null
					&& !dsSigla.equals(other.getDsSigla())) {
				return false;
			}

			if (!other.getAtivo().equals(this.getAtivo())) {
				return false;
			}

			return true;
		} else {
			return other.getIdTipoPena().equals(this.getIdTipoPena());
		}
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoPena> getEntityClass() {
		return TipoPena.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdTipoPena();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
