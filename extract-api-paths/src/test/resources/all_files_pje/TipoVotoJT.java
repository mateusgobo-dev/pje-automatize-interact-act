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
package br.jus.pje.jt.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.jt.enums.ConclusaoEnum;
import br.jus.pje.jt.enums.TipoResponsavelEnum;

@Entity
@Table(name="tb_jt_tipo_voto")
@org.hibernate.annotations.GenericGenerator(name = "gen_jt_tipo_voto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "SQ_TB_JT_TIPO_VOTO"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoVotoJT implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoVotoJT,Integer>{

	private static final long serialVersionUID = 1L;

	private Integer idTipoVoto;
	private String tipoVoto;
	private String textoCertidao;
	private TipoResponsavelEnum tipoResponsavel;
	private ConclusaoEnum conclusao;
	private Boolean ativo;

	@Id
	@GeneratedValue(generator = "gen_jt_tipo_voto")
	@Column(name = "id_tipo_voto", unique = true, nullable = false)
	public Integer getIdTipoVoto() {
		return idTipoVoto;
	}

	public void setIdTipoVoto(Integer idTipoVoto) {
		this.idTipoVoto = idTipoVoto;
	}

	@Column(name="ds_tipo_voto", unique=true, nullable=false, length=150)
	@NotNull
	@Length(max=150)
	public String getTipoVoto() {
		return tipoVoto;
	}

	public void setTipoVoto(String tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

	@Column(name="ds_texto_certidao", nullable=false)
	@NotNull
	public String getTextoCertidao() {
		return textoCertidao;
	}

	public void setTextoCertidao(String textoCertidao) {
		this.textoCertidao = textoCertidao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="in_tipo_responsavel", nullable=false)
	@NotNull
	public TipoResponsavelEnum getTipoResponsavel() {
		return tipoResponsavel;
	}

	public void setTipoResponsavel(TipoResponsavelEnum tipoResponsavel) {
		this.tipoResponsavel = tipoResponsavel;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="in_conclusao", unique=true, nullable=false)
	@NotNull
	public ConclusaoEnum getConclusao() {
		return conclusao;
	}

	public void setConclusao(ConclusaoEnum conclusao) {
		this.conclusao = conclusao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoVoto();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TipoVotoJT)) {
			return false;
		}
		if(getIdTipoVoto() == null){
			return false;
		}
		TipoVotoJT other = (TipoVotoJT) obj;
		if (!idTipoVoto.equals(other.getIdTipoVoto()))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return tipoVoto;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoVotoJT> getEntityClass() {
		return TipoVotoJT.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdTipoVoto();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
