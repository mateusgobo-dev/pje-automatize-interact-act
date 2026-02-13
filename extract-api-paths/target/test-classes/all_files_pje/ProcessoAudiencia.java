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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.ConciliacaoEnum;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;

@Entity
@Table(name = "tb_processo_audiencia")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_audiencia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_audiencia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoAudiencia implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoAudiencia,Integer> {

	private static final long serialVersionUID = 1L;
	public static final String TIPO_DESIGNACAO_MANUAL = "M"; // Manual
	public static final String TIPO_DESIGNACAO_SUGERIDA = "S"; // Sugerida

	private int idProcessoAudiencia;
	private ProcessoTrf processoTrf = new ProcessoTrf();
	private ProcessoAudiencia processoAudienciaPai;
	private Pessoa pessoaMarcador;
	private PessoaFisica pessoaConciliador;
	private Pessoa pessoaCancelamento;
	private Pessoa pessoaRealizador;
	private Sala salaAudiencia;
	private Date dtInicio;
	private Date dtFim;
	private Date dtMarcacao;
	private Date dtAudiencia;
	private Date dtRemarcacao;
	private Date dtCancelamento;
	private String diaSearch;
	private String dsMotivo;
	private Boolean inAtivo;
	private Boolean inAcordo;
	private Double vlAcordo;
	private ProcessoDocumento processoDocumento;
	private StatusAudienciaEnum statusAudiencia;
	private ConciliacaoEnum conciliacao;
	private TipoAudiencia tipoAudiencia;
	private ProcessoParteAdvogado nomeAdvogado;
	private ProcessoParte parte;
	private String tipoDesignacao = TIPO_DESIGNACAO_SUGERIDA;
	private Sala salaAudienciaTemp = new Sala();
	private String identificadorPautaEspecifica;

	private List<ProcessoAudienciaPessoa> processoAudienciaPessoaList = new ArrayList<ProcessoAudienciaPessoa>(0);

	private String acoes;

	@Id
	@GeneratedValue(generator = "gen_processo_audiencia")
	@Column(name = "id_processo_audiencia", unique = true, nullable = false)
	public int getIdProcessoAudiencia() {
		return this.idProcessoAudiencia;
	}

	public void setIdProcessoAudiencia(int idProcessoAudiencia) {
		this.idProcessoAudiencia = idProcessoAudiencia;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_audiencia_pai")
	public ProcessoAudiencia getProcessoAudienciaPai() {
		return this.processoAudienciaPai;
	}

	public void setProcessoAudienciaPai(ProcessoAudiencia processoAudienciaPai) {
		this.processoAudienciaPai = processoAudienciaPai;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_marcador")
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

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa_conciliador", nullable = true, updatable = true)
	public PessoaFisica getPessoaConciliador() {
		return pessoaConciliador;
	}

	public void setPessoaConciliador(PessoaFisica pessoaConciliador) {
		this.pessoaConciliador = pessoaConciliador;
	}
	
	/**
	 * Sobrecarga {@link #setPessoaConciliador(PessoaFisica)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaConciliador(PessoaFisicaEspecializada pessoa){
		setPessoaConciliador(pessoa != null ? pessoa.getPessoa() : (PessoaFisica) null);
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
	public void setPessoaCancelamento(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaCancelamento(pessoa.getPessoa());
		} else {
			setPessoaCancelamento((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_realizador")
	public Pessoa getPessoaRealizador() {
		return pessoaRealizador;
	}

	public void setPessoaRealizador(Pessoa pessoaRealizador) {
		this.pessoaRealizador = pessoaRealizador;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaRealizador(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaRealizador(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaRealizador(pessoa.getPessoa());
		} else {
			setPessoaRealizador((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sala")
	public Sala getSalaAudiencia() {
		return salaAudiencia;
	}

	public void setSalaAudiencia(Sala salaAudiencia) {
		this.salaAudiencia = salaAudiencia;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio")
	public Date getDtInicio() {
		return dtInicio;
	}

	public void setDtInicio(Date dtInicio) {
		this.dtInicio = dtInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim")
	public Date getDtFim() {
		return dtFim;
	}

	public void setDtFim(Date dtFim) {
		this.dtFim = dtFim;
	}

	@Column(name = "in_ativo")
	public Boolean getInAtivo() {
		return inAtivo;
	}

	public void setInAtivo(Boolean inAtivo) {
		this.inAtivo = inAtivo;
	}

	@Column(name = "in_acordo")
	public Boolean getInAcordo() {
		return inAcordo;
	}

	public void setInAcordo(Boolean inAcordo) {
		this.inAcordo = inAcordo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_marcacao")
	public Date getDtMarcacao() {
		return dtMarcacao;
	}

	public void setDtMarcacao(Date dtMarcacao) {
		this.dtMarcacao = dtMarcacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_remarcacao")
	public Date getDtRemarcacao() {
		return dtRemarcacao;
	}

	public void setDtRemarcacao(Date dtRemarcacao) {
		this.dtRemarcacao = dtRemarcacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cancelamento")
	public Date getDtCancelamento() {
		return dtCancelamento;
	}

	public void setDtCancelamento(Date dtCancelamento) {
		this.dtCancelamento = dtCancelamento;
	}

	@Transient
	public Date getDtAudiencia() {
		return dtAudiencia;
	}

	public void setDtAudiencia(Date dtAudiencia) {
		this.dtAudiencia = dtAudiencia;
	}

	@Transient
	public String getDiaSearch() {
		return diaSearch;
	}

	public void setDiaSearch(String diaSearch) {
		this.diaSearch = diaSearch;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "processoAudiencia")
	public List<ProcessoAudienciaPessoa> getProcessoAudienciaPessoaList() {
		return processoAudienciaPessoaList;
	}

	public void setProcessoAudienciaPessoaList(List<ProcessoAudienciaPessoa> processoAudienciaPessoaList) {
		this.processoAudienciaPessoaList = processoAudienciaPessoaList;
	}

	@Column(name = "ds_motivo", length = 600)
	@Length(max = 600)
	public String getDsMotivo() {
		return this.dsMotivo;
	}

	public void setDsMotivo(String dsMotivo) {
		this.dsMotivo = dsMotivo;
	}

	@Column(name = "vl_acordo")
	public Double getVlAcordo() {
		return this.vlAcordo;
	}

	public void setVlAcordo(Double vlAcordo) {
		this.vlAcordo = vlAcordo;
	}

	@Column(name = "cd_status_audiencia", length = 1)
	@Enumerated(EnumType.STRING)
	public StatusAudienciaEnum getStatusAudiencia() {
		return statusAudiencia;
	}

	public void setStatusAudiencia(StatusAudienciaEnum statusAudiencia) {
		this.statusAudiencia = statusAudiencia;
	}

	@Column(name = "tp_conciliacao", length = 2)
	@Enumerated(EnumType.STRING)
	public ConciliacaoEnum getConciliacao() {
		return conciliacao;
	}

	public void setConciliacao(ConciliacaoEnum conciliacao) {
		this.conciliacao = conciliacao;
	}

	@Transient
	public String getAcoes() {
		return acoes;
	}

	public void setAcoes(String acoes) {
		this.acoes = acoes;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_audiencia")
	public TipoAudiencia getTipoAudiencia() {
		return tipoAudiencia;
	}

	public void setTipoAudiencia(TipoAudiencia tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Transient
	public ProcessoParteAdvogado getNomeAdvogado() {
		return nomeAdvogado;
	}

	public void setNomeAdvogado(ProcessoParteAdvogado nomeAdvogado) {
		this.nomeAdvogado = nomeAdvogado;
	}

	@Transient
	public ProcessoParte getParte() {
		return parte;
	}

	public void setParte(ProcessoParte parte) {
		this.parte = parte;
	}

	@Transient
	public String getDtInicioFormatada() {
		return (new SimpleDateFormat("dd/MM/yyyy HH:mm").format(dtInicio));
	}

	@Transient
	public String getDtFimFormatada() {
		return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(dtFim);
	}

	@Column(name = "cd_tipo_designacao")
	@NotNull
	public String getTipoDesignacao() {
		return tipoDesignacao;
	}

	public void setTipoDesignacao(String tipoDesignacao) {
		this.tipoDesignacao = tipoDesignacao;
	}

	@Transient
	public String getTipoDesignacaoCompleto() {
		return this.tipoDesignacao.equals(TIPO_DESIGNACAO_SUGERIDA) ? "Sugerida" : "Manual";
	}

	@Transient
	public String getProcessoAudienciaStr() {
		SimpleDateFormat fmData = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat fmHora = new SimpleDateFormat("HH:mm");
		StringBuilder sb = new StringBuilder();
		sb.append(getTipoAudiencia() == null ? "" : "Tipo: " + getTipoAudiencia().getTipoAudiencia() + "\n");
		sb.append(getSalaAudiencia() == null ? "" : "Sala: " + getSalaAudiencia().getSala() + "\n");
		sb.append(getDtMarcacao() == null ? "" : "Data: " + fmData.format(getDtInicio()) + "\n");
		sb.append(getDtMarcacao() == null ? "" : "Hora: " + fmHora.format(getDtInicio()));
		return sb.toString();
	}
	
	@Transient
	public String getProcessoAudienciaStr(String delimitador) {
		SimpleDateFormat fmData = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat fmHora = new SimpleDateFormat("HH:mm");
		StringBuilder sb = new StringBuilder();
		sb.append(getTipoAudiencia() == null ? "" : "Tipo: " + getTipoAudiencia().getTipoAudiencia() + delimitador);
		sb.append(getSalaAudiencia() == null ? "" : "Sala: " + getSalaAudiencia().getSala() + delimitador);
		sb.append(getDtMarcacao() == null ? "" : "Data: " + fmData.format(getDtInicio()) + delimitador);
		sb.append(getDtMarcacao() == null ? "" : "Hora: " + fmHora.format(getDtInicio()));
		return sb.toString();
	}

	/*
	 * Retorna uma String no mesmo formato da mensagem que é exibida ao se protocolar um processo.
	 */
	@Transient
	public String getProcessoAudienciaStrProtocolo() {
		SimpleDateFormat fmData = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat fmHora = new SimpleDateFormat("HH:mm");
		StringBuilder sb = new StringBuilder();
		
		sb.append("Audiência (");
		sb.append(getTipoAudiencia().getTipoAudiencia());
		sb.append(") designada para o dia: ");
		sb.append(fmData.format(getDtInicio()));
		sb.append(" ");
		sb.append(fmHora.format(getDtInicio()));
		
		return sb.toString();
	}
	
	@Transient
	public Sala getSalaAudienciaTemp() {
		return salaAudienciaTemp;
	}

	public void setSalaAudienciaTemp(Sala salaAudienciaTemp) {
		this.salaAudienciaTemp = salaAudienciaTemp;
	}

	@Transient
	public Boolean getAtivo() {
		return getInAtivo();
	}
	
	public void setAtivo(Boolean ativo) {
		setInAtivo(ativo);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoAudiencia)) {
			return false;
		}
		ProcessoAudiencia other = (ProcessoAudiencia) obj;
		if (getIdProcessoAudiencia() != other.getIdProcessoAudiencia()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoAudiencia();
		return result;
	}

	@Override
	public String toString() {
		return getProcessoAudienciaStr();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoAudiencia> getEntityClass() {
		return ProcessoAudiencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoAudiencia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@Transient
	public String getIdentificadorPautaEspecifica() {
		return identificadorPautaEspecifica;
	}

	public void setIdentificadorPautaEspecifica(String identificadorPautaEspecifica) {
		this.identificadorPautaEspecifica = identificadorPautaEspecifica;
	}
}
