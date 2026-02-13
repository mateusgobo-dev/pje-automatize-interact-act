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

import java.sql.Time;
import java.text.MessageFormat;
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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.PericiaStatusEnum;

@Entity
@Table(name = "tb_processo_pericia")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_pericia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_pericia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoPericia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoPericia,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoPericia;
	private ProcessoTrf processoTrf;
	private PessoaPerito pessoaPerito;
	private Pessoa pessoaPericiado;
	private PericiaStatusEnum status;
	private ProcessoPericia periciaAnterior;
	private Especialidade especialidade;
	private Pessoa pessoaMarcador;
	private ProcessoDocumento processoDocumento;
	private Pessoa pessoaCancela;
	private Date dataMarcacao;
	private Time horaMarcada;
	private Double valorPericia;
	private Date dataCancelamento;
	private String tipoBeneficio;
	private Date dataEntradaRequisicao;
	private Date dataCessacaoBeneficio;
	private String quesitos;
	private String enfermidades;
	private String motivo;
	private String objetoPericia;
	private String motivoDesignar;
	private String numeroBeneficio;
	private FormularioExterno formularioExterno;

	private List<PagamentoPericia> pagamentoPericiaList = new ArrayList<PagamentoPericia>(0);

	public ProcessoPericia() {
	}

	@Id
	@GeneratedValue(generator = "gen_processo_pericia")
	@Column(name = "id_processo_pericia", unique = true, nullable = false)
	public int getIdProcessoPericia() {
		return this.idProcessoPericia;
	}

	public void setIdProcessoPericia(int idProcessoPericia) {
		this.idProcessoPericia = idProcessoPericia;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Column(name = "cd_status_pericia", length = 1)
	@Enumerated(EnumType.STRING)
	public PericiaStatusEnum getStatus() {
		return status;
	}

	public void setStatus(PericiaStatusEnum status) {
		this.status = status;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_perito")
	public PessoaPerito getPessoaPerito() {
		return pessoaPerito;
	}

	public void setPessoaPerito(PessoaPerito pessoaPerito) {
		this.pessoaPerito = pessoaPerito;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_processo_parte")
	public Pessoa getPessoaPericiado() {
		return pessoaPericiado;
	}

	public void setPessoaPericiado(Pessoa pessoaPericiado) {
		this.pessoaPericiado = pessoaPericiado;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaPericiado(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaPericiado(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaPericiado(pessoa.getPessoa());
		} else {
			setPessoaPericiado((Pessoa)null);
		}
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_pericia_anterior")
	public ProcessoPericia getPericiaAnterior() {
		return periciaAnterior;
	}

	public void setPericiaAnterior(ProcessoPericia periciaAnterior) {
		this.periciaAnterior = periciaAnterior;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_especialidade")
	public Especialidade getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(Especialidade especialidade) {
		this.especialidade = especialidade;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_marcador_pericia")
	public Pessoa getPessoaMarcador() {
		return pessoaMarcador;
	}

	public void setPessoaMarcador(Pessoa pessoaMarcador) {
		this.pessoaMarcador = pessoaMarcador;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaMarcador(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaMarcador(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaMarcador(pessoa.getPessoa());
		} else {
			setPessoaMarcador((Pessoa)null);
		}
	}

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_documento")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_cancela_pericia")
	public Pessoa getPessoaCancela() {
		return pessoaCancela;
	}

	public void setPessoaCancela(Pessoa pessoaCancela) {
		this.pessoaCancela = pessoaCancela;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaCancela(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaCancela(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaCancela(pessoa.getPessoa());
		} else {
			setPessoaCancela((Pessoa)null);
		}
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_marcacao", nullable = false)
	@NotNull
	public Date getDataMarcacao() {
		return dataMarcacao;
	}

	public void setDataMarcacao(Date dataMarcacao) {
		this.dataMarcacao = dataMarcacao;
	}

	@Temporal(TemporalType.TIME)
	@Column(name = "dt_hora_marcada", nullable = false)
	@NotNull
	public Date getHoraMarcada() {
		return horaMarcada;
	}

	public void setHoraMarcada(Time horaMarcada) {
		this.horaMarcada = horaMarcada;
	}

	@Column(name = "vl_pericia")
	public Double getValorPericia() {
		return valorPericia;
	}

	public void setValorPericia(Double valorPericia) {
		this.valorPericia = valorPericia;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cancelamento")
	public Date getDataCancelamento() {
		return dataCancelamento;
	}

	public void setDataCancelamento(Date dataCancelamento) {
		this.dataCancelamento = dataCancelamento;
	}

	@Column(name = "ds_tipo_beneficio", length = 200)
	@Length(max = 200)
	public String getTipoBeneficio() {
		return tipoBeneficio;
	}

	public void setTipoBeneficio(String tipoBeneficio) {
		this.tipoBeneficio = tipoBeneficio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_entrada_requisicao")
	public Date getDataEntradaRequisicao() {
		return dataEntradaRequisicao;
	}

	public void setDataEntradaRequisicao(Date dataEntradaRequisicao) {
		this.dataEntradaRequisicao = dataEntradaRequisicao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cessacao_beneficio")
	public Date getDataCessacaoBeneficio() {
		return dataCessacaoBeneficio;
	}

	public void setDataCessacaoBeneficio(Date dataCessacaoBeneficio) {
		this.dataCessacaoBeneficio = dataCessacaoBeneficio;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_quesitos")
	public String getQuesitos() {
		return quesitos;
	}

	public void setQuesitos(String quesitos) {
		this.quesitos = quesitos;
	}

	@Column(name = "ds_enfermidades", length = 200)
	@Length(max = 200)
	public String getEnfermidades() {
		return enfermidades;
	}

	public void setEnfermidades(String enfermidades) {
		this.enfermidades = enfermidades;
	}

	@Column(name = "ds_motivo", length = 40)
	@Length(max = 40)
	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	@Column(name = "ds_objeto_pericia", length = 200)
	@Length(max = 200)
	public String getObjetoPericia() {
		return objetoPericia;
	}

	public void setObjetoPericia(String objetoPericia) {
		this.objetoPericia = objetoPericia;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processoPericia")
	public List<PagamentoPericia> getPagamentoPericiaList() {
		return pagamentoPericiaList;
	}

	public void setPagamentoPericiaList(List<PagamentoPericia> pagamentoPericiaList) {
		this.pagamentoPericiaList = pagamentoPericiaList;
	}

	public void setMotivoDesignar(String motivoDesignar) {
		this.motivoDesignar = motivoDesignar;
	}

	@Column(name = "ds_motivo_designar", length = 50)
	@Length(max = 50)
	public String getMotivoDesignar() {
		return motivoDesignar;
	}

	@Column(name = "ds_numero_beneficio", nullable = true, length = 10)
	@Length(max = 10)
	public String getNumeroBeneficio() {
		return numeroBeneficio;
	}

	public void setNumeroBeneficio(String numeroBeneficio) {
		this.numeroBeneficio = numeroBeneficio;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_formulario_externo")
	public FormularioExterno getFormularioExterno() {
		return formularioExterno;
	}
	
	public void setFormularioExterno(FormularioExterno formularioExterno) {
		this.formularioExterno = formularioExterno;
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0}-{2}({1}):{3,date,short} {4,time,short}", this.processoTrf.getNumeroProcesso(),
				getEspecialidade(), getPessoaPerito(), getDataMarcacao());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoPericia)) {
			return false;
		}
		ProcessoPericia other = (ProcessoPericia) obj;
		if (getIdProcessoPericia() != other.getIdProcessoPericia()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoPericia();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoPericia> getEntityClass() {
		return ProcessoPericia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoPericia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
