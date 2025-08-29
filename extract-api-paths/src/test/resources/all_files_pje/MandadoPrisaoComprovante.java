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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.SituacaoMandadoPrisaoEnum;

@Entity
@Table(name = "tb_mndo_prisao_comprovante")
@org.hibernate.annotations.GenericGenerator(name = "gen_mandado_prso_comprovante", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_id_mandado_prso_comprovante"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class MandadoPrisaoComprovante implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<MandadoPrisaoComprovante,Integer>, Comparable<MandadoPrisaoComprovante> {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private Date dataRecebimento;
	private Long numeroProtocoloRecebimento;
	private SituacaoMandadoPrisaoEnum situacaoMandadoPrisao;
	private MandadoPrisao mandadoPrisao;

	@Id
	@GeneratedValue(generator = "gen_mandado_prso_comprovante")
	@Column(name = "id_mandado_prisao_comprovante", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@NotNull
	@Column(name = "dt_recebimento", nullable = false)
	public Date getDataRecebimento() {
		return dataRecebimento;
	}

	public void setDataRecebimento(Date dataRecebimento) {
		this.dataRecebimento = dataRecebimento;
	}

	@Column(name = "nr_protocolo_recebimento", nullable = false)
	public Long getNumeroProtocoloRecebimento() {
		return numeroProtocoloRecebimento;
	}

	public void setNumeroProtocoloRecebimento(Long numeroProtocoloRecebimento) {
		this.numeroProtocoloRecebimento = numeroProtocoloRecebimento;
	}

	@Column(name = "in_situacao", nullable = false)
	@Enumerated(EnumType.STRING)
	public SituacaoMandadoPrisaoEnum getSituacaoMandadoPrisao() {
		return situacaoMandadoPrisao;
	}

	public void setSituacaoMandadoPrisao(SituacaoMandadoPrisaoEnum situacaoMandadoPrisao) {
		this.situacaoMandadoPrisao = situacaoMandadoPrisao;
	}

	@ManyToOne
	@JoinColumn(name = "id_mandado_prisao")
	public MandadoPrisao getMandadoPrisao() {
		return mandadoPrisao;
	}

	public void setMandadoPrisao(MandadoPrisao mandadoPrisao) {
		this.mandadoPrisao = mandadoPrisao;
	}
	
	@Override
	public int compareTo(MandadoPrisaoComprovante o){		
		return this.getDataRecebimento().compareTo(o.getDataRecebimento());
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends MandadoPrisaoComprovante> getEntityClass() {
		return MandadoPrisaoComprovante.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
