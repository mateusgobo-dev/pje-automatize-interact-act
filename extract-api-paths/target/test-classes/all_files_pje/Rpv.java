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
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import br.jus.pje.nucleo.enums.EspecieRequisicaoEnum;
import br.jus.pje.nucleo.enums.NaturezaCreditoEnum;
import br.jus.pje.nucleo.enums.RpvPessoaParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.RpvPrecatorioEnum;
import br.jus.pje.nucleo.enums.RpvTipoCessaoEnum;
import br.jus.pje.nucleo.enums.RpvTipoFormaHonorarioEnum;
import br.jus.pje.nucleo.enums.RpvTipoRestricaoPagamentoEnum;

@Entity
@Table(name = "tb_rpv")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_rpv", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_rpv"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Rpv implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Rpv,Integer> {

	private static final long serialVersionUID = 1L;

	private int idRpv;
	private ProcessoTrf processoTrf;
	private PessoaMagistrado magistradoValidacao;
	private Pessoa pessoaCadastro;
	private Pessoa pessoaConferencia;
	private RpvStatus rpvStatus;
	private Date dataCadastro;
	private Date dataConferencia;
	private String numeroRpvPrecatorio;
	private RpvPrecatorioEnum inRpvPrecatorio = RpvPrecatorioEnum.R;
	private boolean inOposicaoEmbargos = false;
	private Date dataTransitoEmbargos;
	private Boolean inDesapropriacao;
	private boolean inDesapropriacaoUnicoImovel = false;
	private NaturezaCreditoEnum inNaturezaCredito;
	private EspecieRequisicaoEnum inEspecieRequisicao;
	private Date dataExecucao;
	private Double valorCustasAjuizamento;
	private Date dataBaseCalculo;
	private boolean inReembolsoHonorariosSecao = false;
	private boolean inPagamentoDiretoPerito = false;
	private boolean inCreditoSomenteAdvogado = false;
	private Pessoa autorCabecaAcao;
	private Pessoa reu;
	private Double valorCustas;
	private Date dataTransitoJulgado;
	private boolean inCessionario = false;
	private Double valorRequisitado;
	private Rpv rpvRejeitado;
	private AssuntoTrf assuntoPrincipal;
	private Integer ano;
	private Integer numeroSequencia;
	private Integer numeroVara;
	private Integer numeroOrigemProcesso;
	private boolean inValorCompensar = false;
	private boolean inMultaAstreintes = false;
	private RpvTipoCessaoEnum tipoCessao = RpvTipoCessaoEnum.P;
	private RpvTipoFormaHonorarioEnum inTipoFormaHonorario;
	private RpvTipoRestricaoPagamentoEnum inTipoRestricaoPagamento;
	private String observacaoSucumbencia;
	private boolean inEnvioTrf = false;
	private Date dataEnvioTrf;
	private boolean inRessarcimentoCustas = false;
	private Integer codigoJusticaOriginaria;
	private Double valorTotalExecucao;
	private Date dataIntimacaoExecutada;
	private List<RpvPessoaParte> rpvParteList = new ArrayList<RpvPessoaParte>(0);
	private Pessoa beneficiario;
	private Pessoa pessoaCancelamento;;
	private Date dataCancelamento;
	private String motivoCancelamento;
	private Pessoa pessoaDevolucao;;
	private Date dataDevolucao;
	private Date dataValidacao;
	private String motivoDevolucao;
	private String obsRessarcimentoCustas;

	private List<RpvPessoaParte> listaParteAtivo;
	private List<RpvPessoaParte> listaPartePassivo;
	private List<RpvPessoaParte> listaParteTerceiro;

	public Rpv() {
	}

	@Id
	@GeneratedValue(generator = "gen_rpv")
	@Column(name = "id_rpv", unique = true, nullable = false)
	public int getIdRpv() {
		return idRpv;
	}

	public void setIdRpv(int idRpv) {
		this.idRpv = idRpv;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf")
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_magistrado_validacao")
	public PessoaMagistrado getMagistradoValidacao() {
		return magistradoValidacao;
	}

	public void setMagistradoValidacao(PessoaMagistrado magistradoValidacao) {
		this.magistradoValidacao = magistradoValidacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_cadastro")
	@NotNull
	public Pessoa getPessoaCadastro() {
		return pessoaCadastro;
	}

	public void setPessoaCadastro(Pessoa pessoaCadastro) {
		this.pessoaCadastro = pessoaCadastro;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaCadastro(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaCadastro(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaCadastro(pessoa.getPessoa());
		} else {
			setPessoaCadastro((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_conferencia")
	public Pessoa getPessoaConferencia() {
		return pessoaConferencia;
	}

	public void setPessoaConferencia(Pessoa pessoaConferencia) {
		this.pessoaConferencia = pessoaConferencia;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaConferencia(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaConferencia(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaConferencia(pessoa.getPessoa());
		} else {
			setPessoaConferencia((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_rpv_status")
	@NotNull
	public RpvStatus getRpvStatus() {
		return rpvStatus;
	}

	public void setRpvStatus(RpvStatus rpvStatus) {
		this.rpvStatus = rpvStatus;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cadastro")
	@NotNull
	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_conferencia")
	public Date getDataConferencia() {
		return dataConferencia;
	}

	public void setDataConferencia(Date dataConferencia) {
		this.dataConferencia = dataConferencia;
	}

	@Column(name = "nr_rpv_precatorio")
	public String getNumeroRpvPrecatorio() {
		return numeroRpvPrecatorio;
	}

	public void setNumeroRpvPrecatorio(String numeroRpvPrecatorio) {
		this.numeroRpvPrecatorio = numeroRpvPrecatorio;
	}

	@Column(name = "in_rpv_precatorio", length = 1)
	@Enumerated(EnumType.STRING)
	@NotNull
	public RpvPrecatorioEnum getInRpvPrecatorio() {
		return inRpvPrecatorio;
	}

	public void setInRpvPrecatorio(RpvPrecatorioEnum inRpvPrecatorio) {
		this.inRpvPrecatorio = inRpvPrecatorio;
	}

	@Column(name = "in_oposicao_embargos", length = 1, nullable = false)
	@NotNull
	public boolean getInOposicaoEmbargos() {
		return inOposicaoEmbargos;
	}

	public void setInOposicaoEmbargos(boolean inOposicaoEmbargos) {
		this.inOposicaoEmbargos = inOposicaoEmbargos;
	}

	@Column(name = "in_desapropriacao", length = 1)
	public Boolean getInDesapropriacao() {
		return inDesapropriacao;
	}

	public void setInDesapropriacao(Boolean inDesapropriacao) {
		this.inDesapropriacao = inDesapropriacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_transito_embargos")
	public Date getDataTransitoEmbargos() {
		return dataTransitoEmbargos;
	}

	public void setDataTransitoEmbargos(Date dataTransitoEmbargos) {
		this.dataTransitoEmbargos = dataTransitoEmbargos;
	}

	@Column(name = "in_desapropriacao_unico_imovel", length = 1, nullable = false)
	@NotNull
	public boolean getInDesapropriacaoUnicoImovel() {
		return inDesapropriacaoUnicoImovel;
	}

	public void setInDesapropriacaoUnicoImovel(boolean inDesapropriacaoUnicoImovel) {
		this.inDesapropriacaoUnicoImovel = inDesapropriacaoUnicoImovel;
	}

	@Column(name = "in_natureza_credito", length = 1)
	@Enumerated(EnumType.STRING)
	public NaturezaCreditoEnum getInNaturezaCredito() {
		return inNaturezaCredito;
	}

	public void setInNaturezaCredito(NaturezaCreditoEnum inNaturezaCredito) {
		this.inNaturezaCredito = inNaturezaCredito;
	}

	@Column(name = "in_especie_requisicao", length = 1)
	@Enumerated(EnumType.STRING)
	@NotNull
	public EspecieRequisicaoEnum getInEspecieRequisicao() {
		return inEspecieRequisicao;
	}

	public void setInEspecieRequisicao(EspecieRequisicaoEnum inEspecieRequisicao) {
		this.inEspecieRequisicao = inEspecieRequisicao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_execucao")
	public Date getDataExecucao() {
		return dataExecucao;
	}

	public void setDataExecucao(Date dataExecucao) {
		this.dataExecucao = dataExecucao;
	}

	@Column(name = "vl_custa_ajuizamento")
	public Double getValorCustasAjuizamento() {
		return valorCustasAjuizamento;
	}

	public void setValorCustasAjuizamento(Double valorCustasAjuizamento) {
		this.valorCustasAjuizamento = valorCustasAjuizamento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_base_calculo")
	public Date getDataBaseCalculo() {
		return dataBaseCalculo;
	}

	public void setDataBaseCalculo(Date dataBaseCalculo) {
		this.dataBaseCalculo = dataBaseCalculo;
	}

	@Column(name = "in_reembolso_honorarios_secao", length = 1, nullable = false)
	@NotNull
	public boolean getInReembolsoHonorariosSecao() {
		return inReembolsoHonorariosSecao;
	}

	public void setInReembolsoHonorariosSecao(boolean inReembolsoHonorariosSecao) {
		this.inReembolsoHonorariosSecao = inReembolsoHonorariosSecao;
	}

	@Column(name = "in_pagamento_direto_perito", length = 1, nullable = false)
	@NotNull
	public boolean getInPagamentoDiretoPerito() {
		return inPagamentoDiretoPerito;
	}

	public void setInPagamentoDiretoPerito(boolean inPagamentoDiretoPerito) {
		this.inPagamentoDiretoPerito = inPagamentoDiretoPerito;
	}

	@Column(name = "in_credito_somente_advogado", length = 1, nullable = false)
	@NotNull
	public boolean getInCreditoSomenteAdvogado() {
		return inCreditoSomenteAdvogado;
	}

	public void setInCreditoSomenteAdvogado(boolean inCreditoSomenteAdvogado) {
		this.inCreditoSomenteAdvogado = inCreditoSomenteAdvogado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_autor_cabeca_acao")
	public Pessoa getAutorCabecaAcao() {
		return autorCabecaAcao;
	}

	public void setAutorCabecaAcao(Pessoa autorCabecaAcao) {
		this.autorCabecaAcao = autorCabecaAcao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_reu")
	public Pessoa getReu() {
		return reu;
	}

	public void setReu(Pessoa reu) {
		this.reu = reu;
	}

	@Column(name = "vl_custas")
	public Double getValorCustas() {
		return valorCustas;
	}

	public void setValorCustas(Double valorCustas) {
		this.valorCustas = valorCustas;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_transito_julgado")
	public Date getDataTransitoJulgado() {
		return dataTransitoJulgado;
	}

	public void setDataTransitoJulgado(Date dataTransitoJulgado) {
		this.dataTransitoJulgado = dataTransitoJulgado;
	}

	@Column(name = "in_cessionario", length = 1, nullable = false)
	@NotNull
	public boolean getInCessionario() {
		return inCessionario;
	}

	public void setInCessionario(boolean inCessionario) {
		this.inCessionario = inCessionario;
	}

	@Column(name = "vl_valor_requisitado")
	public Double getValorRequisitado() {
		return valorRequisitado;
	}

	public void setValorRequisitado(Double valorRequisitado) {
		this.valorRequisitado = valorRequisitado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_rpv_rejeitado")
	public Rpv getRpvRejeitado() {
		return rpvRejeitado;
	}

	public void setRpvRejeitado(Rpv rpvRejeitado) {
		this.rpvRejeitado = rpvRejeitado;
	}

	@Override
	public String toString() {
		return numeroRpvPrecatorio;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_assunto_principal")
	public AssuntoTrf getAssuntoPrincipal() {
		return assuntoPrincipal;
	}

	public void setAssuntoPrincipal(AssuntoTrf assuntoPrincipal) {
		this.assuntoPrincipal = assuntoPrincipal;
	}

	@Column(name = "nr_ano")
	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	@Column(name = "nr_sequencia")
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	@Column(name = "nr_vara")
	public Integer getNumeroVara() {
		return numeroVara;
	}

	public void setNumeroVara(Integer numeroVara) {
		this.numeroVara = numeroVara;
	}

	@Column(name = "nr_origem_processo")
	public Integer getNumeroOrigemProcesso() {
		return numeroOrigemProcesso;
	}

	public void setNumeroOrigemProcesso(Integer numeroOrigemProcesso) {
		this.numeroOrigemProcesso = numeroOrigemProcesso;
	}

	@Column(name = "in_valor_compensar", length = 1, nullable = false)
	@NotNull
	public boolean getInValorCompensar() {
		return inValorCompensar;
	}

	public void setInValorCompensar(boolean inValorCompensar) {
		this.inValorCompensar = inValorCompensar;
	}

	@Column(name = "in_multa_astreintes", length = 1, nullable = false)
	public boolean getInMultaAstreintes() {
		return inMultaAstreintes;
	}

	public void setInMultaAstreintes(boolean inMultaAstreintes) {
		this.inMultaAstreintes = inMultaAstreintes;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_tipo_cessao")
	public RpvTipoCessaoEnum getTipoCessao() {
		return tipoCessao;
	}

	public void setTipoCessao(RpvTipoCessaoEnum tipoCessao) {
		this.tipoCessao = tipoCessao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_tipo_forma_honorario")
	public RpvTipoFormaHonorarioEnum getInTipoFormaHonorario() {
		return inTipoFormaHonorario;
	}

	public void setInTipoFormaHonorario(RpvTipoFormaHonorarioEnum inTipoFormaHonorario) {
		this.inTipoFormaHonorario = inTipoFormaHonorario;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_tipo_restricao_pagto")
	public RpvTipoRestricaoPagamentoEnum getInTipoRestricaoPagamento() {
		return inTipoRestricaoPagamento;
	}

	public void setInTipoRestricaoPagamento(RpvTipoRestricaoPagamentoEnum inTipoRestricaoPagamento) {
		this.inTipoRestricaoPagamento = inTipoRestricaoPagamento;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_observacao_sucumbencia")
	public String getObservacaoSucumbencia() {
		return observacaoSucumbencia;
	}

	public void setObservacaoSucumbencia(String observacaoSucumbencia) {
		this.observacaoSucumbencia = observacaoSucumbencia;
	}

	@Column(name = "in_envio_trf", length = 1, nullable = false)
	@NotNull
	public boolean getInEnvioTrf() {
		return inEnvioTrf;
	}

	public void setInEnvioTrf(boolean inEnvioTrf) {
		this.inEnvioTrf = inEnvioTrf;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_envio_trf")
	public Date getDataEnvioTrf() {
		return dataEnvioTrf;
	}

	public void setDataEnvioTrf(Date dataEnvioTrf) {
		this.dataEnvioTrf = dataEnvioTrf;
	}

	@Column(name = "in_ressarcimento_custas", length = 1)
	public boolean getInRessarcimentoCustas() {
		return inRessarcimentoCustas;
	}

	public void setInRessarcimentoCustas(boolean inRessarcimentoCustas) {
		this.inRessarcimentoCustas = inRessarcimentoCustas;
	}

	@Column(name = "cd_justica_originaria")
	public Integer getCodigoJusticaOriginaria() {
		return codigoJusticaOriginaria;
	}

	public void setCodigoJusticaOriginaria(Integer codigoJusticaOriginaria) {
		this.codigoJusticaOriginaria = codigoJusticaOriginaria;
	}

	@Column(name = "vl_total_execucao")
	public Double getValorTotalExecucao() {
		return valorTotalExecucao;
	}

	public void setValorTotalExecucao(Double valorTotalExecucao) {
		this.valorTotalExecucao = valorTotalExecucao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_intimacao_executada")
	public Date getDataIntimacaoExecutada() {
		return dataIntimacaoExecutada;
	}

	public void setDataIntimacaoExecutada(Date dataIntimacaoExecutada) {
		this.dataIntimacaoExecutada = dataIntimacaoExecutada;
	}

	@OneToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "rpv")
	public List<RpvPessoaParte> getRpvParteList() {
		return rpvParteList;
	}

	public void setRpvParteList(List<RpvPessoaParte> rpvParteList) {
		this.rpvParteList = rpvParteList;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_beneficiario")
	public Pessoa getBeneficiario() {
		return beneficiario;
	}

	public void setBeneficiario(Pessoa beneficiario) {
		this.beneficiario = beneficiario;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_cancelamento")
	public Pessoa getPessoaCancelamento() {
		return pessoaCancelamento;
	}

	public void setPessoaCancelamento(Pessoa pessoaCancelamento) {
		this.pessoaCancelamento = pessoaCancelamento;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaCancelamento(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPesssoaCancelamento(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaCancelamento(pessoa.getPessoa());
		} else {
			setPessoaCancelamento((Pessoa)null);
		}
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cancelamento")
	public Date getDataCancelamento() {
		return dataCancelamento;
	}

	public void setDataCancelamento(Date dataCancelamento) {
		this.dataCancelamento = dataCancelamento;
	}

	@Column(name = "ds_motivo_cancelamento")
	public String getMotivoCancelamento() {
		return motivoCancelamento;
	}

	public void setMotivoCancelamento(String motivoCancelamento) {
		this.motivoCancelamento = motivoCancelamento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_devolucao")
	public Pessoa getPessoaDevolucao() {
		return pessoaDevolucao;
	}

	public void setPessoaDevolucao(Pessoa pessoaDevolucao) {
		this.pessoaDevolucao = pessoaDevolucao;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaDevolucao(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaDevolucao(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaDevolucao(pessoa.getPessoa());
		} else {
			setPessoaDevolucao((Pessoa)null);
		}
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_devolucao")
	public Date getDataDevolucao() {
		return dataDevolucao;
	}

	public void setDataDevolucao(Date dataDevolucao) {
		this.dataDevolucao = dataDevolucao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_validacao")
	public Date getDataValidacao() {
		return dataValidacao;
	}

	public void setDataValidacao(Date dataValidacao) {
		this.dataValidacao = dataValidacao;
	}

	@Column(name = "ds_motivo_devolucao")
	public String getMotivoDevolucao() {
		return motivoDevolucao;
	}

	public void setMotivoDevolucao(String motivoDevolucao) {
		this.motivoDevolucao = motivoDevolucao;
	}

	@Column(name = "ds_obs_ressarcimento_custas")
	public String getObsRessarcimentoCustas() {
		return obsRessarcimentoCustas;
	}

	public void setObsRessarcimentoCustas(String obsRessarcimentoCustas) {
		this.obsRessarcimentoCustas = obsRessarcimentoCustas;
	}

	@Transient
	public boolean isNumerado() {
		return getNumeroSequencia() != null;
	}

	/*
	 * Traz as partes de uma rpv concatenada com o CPF ou CNPJ de acordo com o
	 * polo
	 */
	@Transient
	public List<String> getListaPartePolo(RpvPessoaParteParticipacaoEnum participacaoEnum) {
		List<String> list = new ArrayList<String>();
		StringBuilder partes = new StringBuilder();
		for (RpvPessoaParte rpvParte : getListaPartePoloObj(participacaoEnum)) {
			partes.append(rpvParte.getTipoParte().getTipoParte()).append(": ");
			partes.append(rpvParte.getPessoa().getNome()).append(" (");
			partes.append(rpvParte.getPessoa().getDocumentoCpfCnpj());
			partes.append(")");
			list.add(partes.toString());
			partes = new StringBuilder();
		}
		return list;
	}

	@Transient
	public List<RpvPessoaParte> getListaPartePoloObj(RpvPessoaParteParticipacaoEnum participacaoEnum) {
		if (participacaoEnum == null) {
			throw new IllegalArgumentException("A participação da parte é requerida");
		}
		List<RpvPessoaParte> list = new ArrayList<RpvPessoaParte>();
		for (RpvPessoaParte rpvParte : rpvParteList) {
			if (rpvParte.getInParticipacao().equals(participacaoEnum)) {
				list.add(rpvParte);
			}
		}
		return list;
	}

	// Traz uma lista com as partes Ativas
	@Transient
	public List<RpvPessoaParte> getListaParteAtivo() {
		this.listaParteAtivo = getListaPartePoloObj(RpvPessoaParteParticipacaoEnum.A);
		return listaParteAtivo;
	}

	// Traz uma lista com as partes Passivas
	@Transient
	public List<RpvPessoaParte> getListaPartePassivo() {
		this.listaPartePassivo = getListaPartePoloObj(RpvPessoaParteParticipacaoEnum.P);
		return listaPartePassivo;
	}

	// Tras uma lista com as partes Terceira
	@Transient
	public List<RpvPessoaParte> getListaParteTerceiro() {
		this.listaParteTerceiro = getListaPartePoloObj(RpvPessoaParteParticipacaoEnum.T);
		return listaParteTerceiro;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Rpv)) {
			return false;
		}
		Rpv other = (Rpv) obj;
		if (getIdRpv() != other.getIdRpv()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRpv();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Rpv> getEntityClass() {
		return Rpv.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRpv());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
