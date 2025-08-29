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
import java.sql.Time;
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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;

import br.jus.pje.nucleo.enums.FrequenciaComparecimentoEmJuizo;
import br.jus.pje.nucleo.enums.TipoMedidaCautelarDiversaEnum;

@Entity
@Table(name = "tb_medida_cautelar_diversa")
@org.hibernate.annotations.GenericGenerator(name = "gen_medida_cautelar_diversa", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_medida_cautelar_diversa"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class MedidaCautelarDiversa implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<MedidaCautelarDiversa,Integer>{

	/**
	 * Campos comuns a todas as Medidas Cautelares
	 */
	private static final long serialVersionUID = -3275914439800098337L;
	private Integer id;
	private Boolean ativo = true;
	private TipoMedidaCautelarDiversaEnum tipo;
	private IcrMedidaCautelarDiversa icr;
	private String observacao;
	
	private List<AcompanhamentoMedidaCautelar> acompanhamentos = new ArrayList<AcompanhamentoMedidaCautelar>(
			0);

	public MedidaCautelarDiversa(){
	}

	public MedidaCautelarDiversa(TipoMedidaCautelarDiversaEnum tipo){
		this.tipo = tipo;
	}

	@Id
	@GeneratedValue(generator = "gen_medida_cautelar_diversa")
	@Column(name = "id_medida_cautelar_diversa", unique = true, nullable = false)
	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "cd_tp_medida_cautelar_diversa", nullable = false)
	public TipoMedidaCautelarDiversaEnum getTipo(){
		return tipo;
	}

	public void setTipo(TipoMedidaCautelarDiversaEnum tipo){
		this.tipo = tipo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_icr", nullable = false)
	public IcrMedidaCautelarDiversa getIcr(){
		return icr;
	}

	public void setIcr(IcrMedidaCautelarDiversa icr){
		this.icr = icr;
	}

	@NotNull
	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo(){
		return this.ativo;
	}

	public void setAtivo(Boolean ativo){
		this.ativo = ativo;
	}

	@Column(name = "ds_observacao")
	public String getObservacao(){
		return observacao;
	}

	public void setObservacao(String observacao){
		this.observacao = observacao;
	}

	/**
	 * Campos CPP319I
	 */
	private Integer prazoAno;
	private Integer prazoMes;
	private Integer prazoDia;
	private FrequenciaComparecimentoEmJuizo frequencia;
	private String condicao;

	@Column(name = "prazo_ano")
	public Integer getPrazoAno(){
		return this.prazoAno;
	}

	public void setPrazoAno(Integer prazoAno){
		this.prazoAno = prazoAno;
	}

	@Column(name = "prazo_mes")
	public Integer getPrazoMes(){
		return prazoMes;
	}

	public void setPrazoMes(Integer prazoMes){
		this.prazoMes = prazoMes;
	}

	@Column(name = "prazo_dia")
	public Integer getPrazoDia(){
		return prazoDia;
	}

	public void setPrazoDia(Integer prazoDia){
		this.prazoDia = prazoDia;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "frequencia")
	public FrequenciaComparecimentoEmJuizo getFrequencia(){
		return frequencia;
	}

	public void setFrequencia(FrequenciaComparecimentoEmJuizo frequencia){
		this.frequencia = frequencia;
	}

	@Column(name = "ds_condicao")
	public String getCondicao(){
		return condicao;
	}

	public void setCondicao(String condicao){
		this.condicao = condicao;
	}
	
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "medidaCautelarDiversa")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@OrderBy("numeroSequencia")
	public List<AcompanhamentoMedidaCautelar> getAcompanhamentos() {
		return acompanhamentos;
	}

	public void setAcompanhamentos(List<AcompanhamentoMedidaCautelar> acompanhamentos) {
		this.acompanhamentos = acompanhamentos;
	}

	/**
	 * Campos CPP319II
	 */
	private List<TipoLocalProibicao> tipoLocalProibicaoList = new ArrayList<TipoLocalProibicao>(0);
	private Boolean localEspecifico;
	private String textLocal;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_md_cat_dvsa_tp_loc_proi",
			joinColumns = {@JoinColumn(name = "id_medida_cautelar_diversa")},
			inverseJoinColumns = {@JoinColumn(name = "id_tipo_local_proibicao")})
	public List<TipoLocalProibicao> getTipoLocalProibicaoList(){
		return tipoLocalProibicaoList;
	}

	public void setTipoLocalProibicaoList(List<TipoLocalProibicao> tipoLocalProibicaoList){
		this.tipoLocalProibicaoList = tipoLocalProibicaoList;
	}

	@Column(name = "is_local_especifico")
	public Boolean getLocalEspecifico(){
		return localEspecifico;
	}

	public void setLocalEspecifico(Boolean localEspecifico){
		this.localEspecifico = localEspecifico;
	}

	@Column(name = "ds_local")
	public String getTextLocal(){
		return textLocal;
	}

	public void setTextLocal(String textLocal){
		this.textLocal = textLocal;
	}

	/**
	 * Campos CPP319III
	 */
	private List<MedidaCautelarPessoaAfastamento> medidaCautelarPessoaAfastamento = new ArrayList<MedidaCautelarPessoaAfastamento>(0);

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "medidaCautelarDiversa")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<MedidaCautelarPessoaAfastamento> getMedidaCautelarPessoaAfastamento(){
		return medidaCautelarPessoaAfastamento;
	}

	public void setMedidaCautelarPessoaAfastamento(List<MedidaCautelarPessoaAfastamento> pessoasAfastamento){
		this.medidaCautelarPessoaAfastamento = pessoasAfastamento;
	}

	/**
	 * Campos CPP319V
	 */
	private Time horarioRecolhimento;

	@Column(name = "horario_recolhimento_domicilio")
	public Time getHorarioRecolhimento(){
		return horarioRecolhimento;
	}

	public void setHorarioRecolhimento(Time horarioRecolhimento){
		this.horarioRecolhimento = horarioRecolhimento;
	}

	/**
	 * Campos CPP319VI
	 */
	private String dsAtividadesVedadas;

	@Column(name = "ds_atividades_vedadas")
	public String getDsAtividadesVedadas(){
		return dsAtividadesVedadas;
	}

	public void setDsAtividadesVedadas(String dsAtividadesVedadas){
		this.dsAtividadesVedadas = dsAtividadesVedadas;
	}

	/**
	 * Campos CPP319VII
	 */
	private EstabelecimentoPrisional estabelecimentoPrisional;// = new EstabelecimentoPrisional();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estabelecimento_prisional")
	public EstabelecimentoPrisional getEstabelecimentoPrisional(){
		return estabelecimentoPrisional;
	}

	public void setEstabelecimentoPrisional(EstabelecimentoPrisional estabelecimentoPrisional){
		this.estabelecimentoPrisional = estabelecimentoPrisional;
	}

	/**
	 * Campos CPP319VIII
	 */

	private Double fiancaValor;
	private PessoaMagistrado fiancaAutoridade;
	private Boolean fiancaPaga;
	private String fiancaBemEspecie;
	private String fiancaBemLocalDeposito;

	@Column(name = "fianca_valor")
	public Double getFiancaValor(){
		return fiancaValor;
	}

	public void setFiancaValor(Double fiancaValor){
		this.fiancaValor = fiancaValor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fianca_autoridade")
	public PessoaMagistrado getFiancaAutoridade(){
		return fiancaAutoridade;
	}

	public void setFiancaAutoridade(PessoaMagistrado fiancaAutoridade){
		this.fiancaAutoridade = fiancaAutoridade;
	}

	@Column(name = "fianca_paga")
	public Boolean getFiancaPaga(){
		return fiancaPaga;
	}

	public void setFiancaPaga(Boolean fiancaPaga){
		this.fiancaPaga = fiancaPaga;
	}

	@Column(name = "fianca_especie_bem")
	public String getFiancaBemEspecie(){
		return fiancaBemEspecie;
	}

	public void setFiancaBemEspecie(String fiancaBemEspecie){
		this.fiancaBemEspecie = fiancaBemEspecie;
	}

	@Column(name = "fianca_local_deposito_bem")
	public String getFiancaBemLocalDeposito(){
		return fiancaBemLocalDeposito;
	}

	public void setFiancaBemLocalDeposito(String fiancaBemLocalDeposito){
		this.fiancaBemLocalDeposito = fiancaBemLocalDeposito;
	}

	/**
	 * Campos CPP320A
	 */
	private Boolean passaporteEntregue;

	@Column(name = "passaporte_entregue")
	public Boolean getPassaporteEntregue(){
		return passaporteEntregue;
	}

	public void setPassaporteEntregue(Boolean passaporteEntregue){
		this.passaporteEntregue = passaporteEntregue;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends MedidaCautelarDiversa> getEntityClass() {
		return MedidaCautelarDiversa.class;
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
