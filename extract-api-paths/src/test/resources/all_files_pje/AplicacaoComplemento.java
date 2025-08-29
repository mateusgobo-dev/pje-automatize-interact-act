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
package br.jus.pje.nucleo.entidades.lancadormovimento;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = AplicacaoComplemento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_aplic_complemento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_aplicacao_complemento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AplicacaoComplemento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AplicacaoComplemento,Long> {

	public static final String TABLE_NAME = "tb_aplicacao_complemento";
	private static final long serialVersionUID = 1L;

	private Long idAplicacaoComplemento;
	private TipoComplemento tipoComplemento;
	private AplicacaoMovimento aplicacaoMovimento;
	private Boolean multivalorado;
	private Boolean visibilidadeExterna;

	public AplicacaoComplemento() {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_aplic_complemento")
	@Column(name = "id_aplicacao_complemento", unique = true, nullable = false)
	public Long getIdAplicacaoComplemento() {
		return idAplicacaoComplemento;
	}

	public void setIdAplicacaoComplemento(Long idAplicacaoComplemento) {
		this.idAplicacaoComplemento = idAplicacaoComplemento;
	}

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_complemento", nullable = false)
	@ForeignKey(name = "id_tipo_complemento_aplicacao_fkey")
	@NotNull
	public TipoComplemento getTipoComplemento() {
		return tipoComplemento;
	}

	public void setTipoComplemento(TipoComplemento tipoComplemento) {
		this.tipoComplemento = tipoComplemento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aplicacao_movimento", nullable = false)
	@ForeignKey(name = "id_aplicacao_movimento_fkey")
	public AplicacaoMovimento getAplicacaoMovimento() {
		return aplicacaoMovimento;
	}

	public void setAplicacaoMovimento(AplicacaoMovimento aplicacaoMovimento) {
		this.aplicacaoMovimento = aplicacaoMovimento;
	}

	@Column(name = "in_multivalorado")
	public Boolean getMultivalorado() {
		return multivalorado;
	}

	public void setMultivalorado(Boolean multivalorado) {
		this.multivalorado = multivalorado;
	}

	@Column(name = "in_visibilidade_externa")
	public Boolean getVisibilidadeExterna() {
		return visibilidadeExterna;
	}

	public void setVisibilidadeExterna(Boolean visibilidadeExterna) {
		this.visibilidadeExterna = visibilidadeExterna;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AplicacaoComplemento> getEntityClass() {
		return AplicacaoComplemento.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdAplicacaoComplemento();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
