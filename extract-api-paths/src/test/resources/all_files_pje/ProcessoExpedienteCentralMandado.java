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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.jus.pje.nucleo.enums.DistribuirRedistribuirEnum;
import br.jus.pje.nucleo.enums.ProcessoExpedienteCentralMandadoStatusEnum;

@Entity
@Table(name = "tb_proc_exped_cntral_mnddo")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_exped_cntrl_mandado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_exped_cntrl_mandado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoExpedienteCentralMandado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoExpedienteCentralMandado,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoExpedienteCentralMandado;
	private ProcessoExpediente processoExpediente;
	private CentralMandado centralMandado;
	private ProcessoExpedienteCentralMandado processoExpedienteCentralMandadoAnterior;
	private PessoaGrupoOficialJustica pessoaGrupoOficialJustica;

	private Date dtDistribuicaoExpediente;
	private Date dtPrazoCentralMandado;
	private Date dtRecebido;
	private Boolean urgencia = Boolean.FALSE;
	private Boolean check;
	private Boolean enviadoScm = Boolean.TRUE;
	private DistribuirRedistribuirEnum distribuir;
	private ProcessoExpedienteCentralMandadoStatusEnum statusExpedienteCentral;

	private ProcessoParteExpediente parteExpedienteUnica;
	private Endereco enderecoParteExpedienteUnico;
	
	private List<Diligencia> diligenciaList = new ArrayList<Diligencia>(0);
	
	public ProcessoExpedienteCentralMandado() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_exped_cntrl_mandado")
	@Column(name = "id_proc_expedi_central_mandado", unique = true, nullable = false)
	public int getIdProcessoExpedienteCentralMandado() {
		return this.idProcessoExpedienteCentralMandado;
	}

	public void setIdProcessoExpedienteCentralMandado(int idProcessoExpedienteCentralMandado) {
		this.idProcessoExpedienteCentralMandado = idProcessoExpedienteCentralMandado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_expediente")
	public ProcessoExpediente getProcessoExpediente() {
		return this.processoExpediente;
	}

	public void setProcessoExpediente(ProcessoExpediente processoExpediente) {
		this.processoExpediente = processoExpediente;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_central_mandado")
	public CentralMandado getCentralMandado() {
		return this.centralMandado;
	}

	public void setCentralMandado(CentralMandado centralMandado) {
		this.centralMandado = centralMandado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_expd_cntrl_mndo_antror")
	public ProcessoExpedienteCentralMandado getProcessoExpedienteCentralMandadoAnterior() {
		return this.processoExpedienteCentralMandadoAnterior;
	}

	public void setProcessoExpedienteCentralMandadoAnterior(
			ProcessoExpedienteCentralMandado processoExpedienteCentralMandadoAnterior) {
		this.processoExpedienteCentralMandadoAnterior = processoExpedienteCentralMandadoAnterior;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pess_grupo_oficial_justica")
	public PessoaGrupoOficialJustica getPessoaGrupoOficialJustica() {
		return pessoaGrupoOficialJustica;
	}

	public void setPessoaGrupoOficialJustica(PessoaGrupoOficialJustica pessoaGrupoOficialJustica) {
		this.pessoaGrupoOficialJustica = pessoaGrupoOficialJustica;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_distribuicao_expediente")
	public Date getDtDistribuicaoExpediente() {
		return dtDistribuicaoExpediente;
	}

	public void setDtDistribuicaoExpediente(Date dtDistribuicaoExpediente) {
		this.dtDistribuicaoExpediente = dtDistribuicaoExpediente;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_prazo_central_mandado")
	public Date getDtPrazoCentralMandado() {
		return dtPrazoCentralMandado;
	}

	public void setDtPrazoCentralMandado(Date dtPrazoCentralMandado) {
		this.dtPrazoCentralMandado = dtPrazoCentralMandado;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_recebido")
	public Date getDtRecebido() {
		return dtRecebido;
	}

	public void setDtRecebido(Date dtRecebido) {
		this.dtRecebido = dtRecebido;
	}

	@Column(name = "in_urgencia")
	public Boolean getUrgencia() {
		return this.urgencia;
	}

	public void setUrgencia(Boolean urgencia) {
		this.urgencia = urgencia;
	}
	
	@Column(name = "in_enviado_scm")
	public Boolean getEnviadoScm() {
		return this.enviadoScm;
	}

	public void setEnviadoScm(Boolean enviadoScm) {
		this.enviadoScm = enviadoScm;
	}

	@Column(name = "in_status_expediente_central", length = 1)
	@Enumerated(EnumType.STRING)
	public ProcessoExpedienteCentralMandadoStatusEnum getStatusExpedienteCentral() {
		return this.statusExpedienteCentral;
	}

	public void setStatusExpedienteCentral(ProcessoExpedienteCentralMandadoStatusEnum statusExpedienteCentral) {
		this.statusExpedienteCentral = statusExpedienteCentral;
	}

	@Transient
	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}

	@Transient
	public DistribuirRedistribuirEnum getDistribuir() {
		return distribuir;
	}

	public void setDistribuir(DistribuirRedistribuirEnum distribuir) {
		this.distribuir = distribuir;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)	
	@JoinColumn(name = "id_processo_parte_expediente")
	public ProcessoParteExpediente getParteExpedienteUnica() {
		return parteExpedienteUnica;
	}

	public void setParteExpedienteUnica(ProcessoParteExpediente parteExpedienteUnica) {
		this.parteExpedienteUnica = parteExpedienteUnica;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte_exp_endereco")	
	public Endereco getEnderecoParteExpedienteUnico() {
		return enderecoParteExpedienteUnico;
	}

	public void setEnderecoParteExpedienteUnico(
			Endereco enderecoParteExpedienteUnico) {
		this.enderecoParteExpedienteUnico = enderecoParteExpedienteUnico;
	}
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processoExpedienteCentralMandado")
	public List<Diligencia> getDiligenciaList(){
		return diligenciaList;
	}

	public void setDiligenciaList(List<Diligencia> diligenciaList){
		this.diligenciaList = diligenciaList;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoExpedienteCentralMandado)) {
			return false;
		}
		ProcessoExpedienteCentralMandado other = (ProcessoExpedienteCentralMandado) obj;
		if (getIdProcessoExpedienteCentralMandado() != other.getIdProcessoExpedienteCentralMandado()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoExpedienteCentralMandado();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoExpedienteCentralMandado> getEntityClass() {
		return ProcessoExpedienteCentralMandado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoExpedienteCentralMandado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
