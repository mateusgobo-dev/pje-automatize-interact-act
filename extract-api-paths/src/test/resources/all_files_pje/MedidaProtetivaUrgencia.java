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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;

import br.jus.pje.nucleo.enums.TipoMedidaProtetivaUrgenciaEnum;

@Entity
@Table(name = "tb_mdda_protetiva_urgencia")
@org.hibernate.annotations.GenericGenerator(name = "gen_medida_prottiva_urgencia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_medida_prottiva_urgencia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class MedidaProtetivaUrgencia implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<MedidaProtetivaUrgencia,Integer>{

	private static final long serialVersionUID = -3275914439800098337L;

	private Integer id;
	private Boolean ativo = true;
	private TipoMedidaProtetivaUrgenciaEnum tipo;
	private IcrMedidaProtetivaUrgencia icr;
	private String observacao;

	@Transient
	public static MedidaProtetivaUrgencia getInstance(
			TipoMedidaProtetivaUrgenciaEnum tipo)
			throws InstantiationException, IllegalAccessException{
		MedidaProtetivaUrgencia medida = new MedidaProtetivaUrgencia(tipo);
		return medida;
	}

	public MedidaProtetivaUrgencia(){
	}

	public MedidaProtetivaUrgencia(TipoMedidaProtetivaUrgenciaEnum tipo){
		this.tipo = tipo;
	}

	/**
	 * Campos Comuns a todas as Medidas Protetivas de Urgencia
	 */
	@Id
	@GeneratedValue(generator = "gen_medida_prottiva_urgencia")
	@Column(name = "id_med_protetiva_urgencia", unique = true, nullable = false)
	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	public void setTipo(TipoMedidaProtetivaUrgenciaEnum tipo){
		this.tipo = tipo;
	}

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "cd_tp_medda_protetiva_urgencia", nullable = false)
	public TipoMedidaProtetivaUrgenciaEnum getTipo(){
		return tipo;
	}

	public void setIcr(IcrMedidaProtetivaUrgencia icr){
		this.icr = icr;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_icr", nullable = false)
	public IcrMedidaProtetivaUrgencia getIcr(){
		return icr;
	}

	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo(){
		return this.ativo;
	}

	public void setAtivo(Boolean inAtivo){
		this.ativo = inAtivo;
	}

	@Column(name = "ds_observacao")
	public String getObservacao(){
		return observacao;
	}

	public void setObservacao(String observacao){
		this.observacao = observacao;
	}

	/**
	 * Campos medida protetiva Art22I
	 */
	private String numDoc;

	@Column(name = "ds_num_documento")
	public String getNumDoc(){
		return numDoc;
	}

	public void setNumDoc(String numDoc){
		this.numDoc = numDoc;
	}

	/**
	 * Campos medida protetiva Art22II
	 */

	private List<MedidaProtetivaPessoasAfastamento> medidasProtetivasPessoasAfastamento = new ArrayList<MedidaProtetivaPessoasAfastamento>(0);

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "medidaProtetivaUrgencia")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<MedidaProtetivaPessoasAfastamento> getMedidasProtetivasPessoasAfastamento(){
		return medidasProtetivasPessoasAfastamento;
	}

	public void setMedidasProtetivasPessoasAfastamento(
			List<MedidaProtetivaPessoasAfastamento> medidasProtetivasPessoasAfastamento){
		this.medidasProtetivasPessoasAfastamento = medidasProtetivasPessoasAfastamento;
	}

	/**
	 * Campos medida protetiva Art22IIIc
	 */
	private List<TipoLocalProibicao> tipoLocalProibicaoList = new ArrayList<TipoLocalProibicao>(0);

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_medida_protetiva_urgencia_tipo_local_proibicao",
			joinColumns = {@JoinColumn(name = "id_med_protetiva_urgencia")},
			inverseJoinColumns = {@JoinColumn(name = "id_tipo_local_proibicao")})
	public List<TipoLocalProibicao> getTipoLocalProibicaoList(){
		return tipoLocalProibicaoList;
	}

	public void setTipoLocalProibicaoList(List<TipoLocalProibicao> tipoLocalProibicaoList){
		this.tipoLocalProibicaoList = tipoLocalProibicaoList;
	}

	private Boolean flagLocalEspecifico;
	private String descricaoLocalEspecifico;

	@Column(name = "is_local_especifico")
	public Boolean getFlagLocalEspecifico(){
		return flagLocalEspecifico;
	}

	public void setFlagLocalEspecifico(Boolean flagLocalEspecifico){
		this.flagLocalEspecifico = flagLocalEspecifico;
	}

	@Column(name = "ds_local_especifico")
	public String getDescricaoLocalEspecifico(){
		return descricaoLocalEspecifico;
	}

	public void setDescricaoLocalEspecifico(String descricaoLocalEspecifico){
		this.descricaoLocalEspecifico = descricaoLocalEspecifico;
	}

	/**
	 * Campos medida protetiva Art22OutrasMedidas
	 */
	private String descricaoOutrasMedidas;

	@Column(name = "ds_outras_medidas")
	public String getDescricaoOutrasMedidas(){
		return descricaoOutrasMedidas;
	}

	public void setDescricaoOutrasMedidas(String descricaoMedida){
		this.descricaoOutrasMedidas = descricaoMedida;
	}

	/**
	 * Campos medida protetiva Art22V
	 */
	private Double valor;

	@Column(name = "alimentos_valor")
	public Double getValor(){
		return valor;
	}

	public void setValor(Double valor){
		this.valor = valor;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends MedidaProtetivaUrgencia> getEntityClass() {
		return MedidaProtetivaUrgencia.class;
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
