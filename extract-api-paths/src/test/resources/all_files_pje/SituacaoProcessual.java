/**
 * pje-comum
 * Copyright (C) 2014 Conselho Nacional de Justiça
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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;

/**
 * Entidade representativa de uma situação que um processo pode assumir.
 * O processo pode assumir mais de uma situação no mesmo momento, desde que a situação
 * a ser incluída não seja de tipo incompatível com situação existente e ativa.
 * 
 * @author cristof
 *
 */
@Entity
@Table(name="tb_situacao_processual")
@IndexedEntity(id="id", value="situacao", owners={"processo"},
	mappings={
		@Mapping(beanPath="instancia", mappedPath="instancia"),
		@Mapping(beanPath="tipoSituacaoProcessual.codigo", mappedPath="tipo"),
		@Mapping(beanPath="dataInicial", mappedPath="dataInicial"),
		@Mapping(beanPath="dataFinal", mappedPath="dataFinal"),
		@Mapping(beanPath="valida", mappedPath="valida"),
		@Mapping(beanPath="ativo", mappedPath="ativo")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_sit_processual", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sit_processual"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SituacaoProcessual implements Serializable {

	private static final long serialVersionUID = 3483232409370326802L;
	
	@Id
	@GeneratedValue(generator = "gen_sit_processual", strategy = GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)
    private Long id;
    
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="id_processo", nullable=false)
    private ProcessoTrf processo;
    
	@Basic(optional=false)
	@NotNull
	@Length(min=3, max=50)
	@Column(name="ds_instancia", nullable=false, length=50)
    private String instancia;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="id_tipo_sit_processual")
	private TipoSituacaoProcessual tipoSituacaoProcessual;
	
	@Basic(optional=false)
	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	@Column(name="dt_inicial", nullable=false)
	private Date dataInicial;
	
	@Basic(optional=true)
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="dt_final", nullable=true)
	private Date dataFinal;
    
	@Basic(optional=true)
	@Column(name = "in_valida", nullable = false)
    private Boolean valida = Boolean.TRUE;

	@Basic(optional=true)
	@Column(name = "in_ativo", nullable = false)
    private Boolean ativo = Boolean.TRUE;
	
	public SituacaoProcessual() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ProcessoTrf getProcesso() {
		return processo;
	}

	public void setProcesso(ProcessoTrf processo) {
		this.processo = processo;
	}

	public String getInstancia() {
		return instancia;
	}

	public void setInstancia(String instancia) {
		this.instancia = instancia;
	}
	
	public TipoSituacaoProcessual getTipoSituacaoProcessual() {
		return tipoSituacaoProcessual;
	}

	public void setTipoSituacaoProcessual(TipoSituacaoProcessual tipoSituacaoProcessual) {
		this.tipoSituacaoProcessual = tipoSituacaoProcessual;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDateFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public Boolean getValida() {
		return valida;
	}

	public void setValida(Boolean valida) {
		this.valida = valida;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
    
}
