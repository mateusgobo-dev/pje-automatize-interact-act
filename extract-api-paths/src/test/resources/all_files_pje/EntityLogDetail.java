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
package br.jus.pje.nucleo.entidades.log;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.IEntidade;

@Ignore
@Entity
@Table(name = "tb_log_detalhe")
@javax.persistence.Cacheable(false)
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class EntityLogDetail implements IEntidade<EntityLogDetail, Long>, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private Long idLogDetalhe;
	private EntityLog entityLog;
	private String nomeAtributo;
	private String valorAnterior;
	private String valorAtual;

	public EntityLogDetail() {
	}

	public EntityLogDetail(EntityLog log, String name, String actual, String oldValue) {
		this.entityLog = log;
		this.nomeAtributo = name;
		this.valorAtual = actual;
		this.valorAnterior = oldValue;
	}

	@Id
	@GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@Parameter(name = "sequence", value = "sq_tb_log_detalhe")
		, @Parameter(name = "allocationSize", value = "-1")})
	@GeneratedValue(generator = "generator", strategy = GenerationType.AUTO)
	@Column(name = "id_log_detalhe", unique = true, nullable = false, updatable = false)
	public Long getIdLogDetalhe() {
		return idLogDetalhe;
	}

	public void setIdLogDetalhe(Long idLogDetalhe) {
		this.idLogDetalhe = idLogDetalhe;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_log", nullable = false)
	@NotNull
	public EntityLog getEntityLog() {
		return entityLog;
	}

	public void setEntityLog(EntityLog entityLog) {
		this.entityLog = entityLog;
	}

	@Column(name = "nm_atributo", length = 150)
	@Length(max = 150)
	public String getNomeAtributo() {
		return nomeAtributo;
	}

	public void setNomeAtributo(String nomeAtributo) {
		this.nomeAtributo = nomeAtributo;
	}

	@Lob
	@org.hibernate.annotations.Type(type = "org.hibernate.type.TextType") 
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "ds_valor_anterior")
	public String getValorAnterior() {
		return valorAnterior;
	}

	public void setValorAnterior(String valorAnterior) {
		this.valorAnterior = valorAnterior;
	}

	@Lob
	@org.hibernate.annotations.Type(type = "org.hibernate.type.TextType") 
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "ds_valor_atual")
	public String getValorAtual() {
		return valorAtual;
	}

	public void setValorAtual(String valorAtual) {
		this.valorAtual = valorAtual;
	}

	@Override
	public String toString() {
		return nomeAtributo;
	}

	@Override
	@Transient
	public Class<EntityLogDetail> getEntityClass() {
		return EntityLogDetail.class;
	}

	@Override
	@Transient
	public Long getEntityIdObject() {
		return idLogDetalhe;
	}

	@Override
	@Transient
	public boolean isLoggable() {
		return false;
	}

}
