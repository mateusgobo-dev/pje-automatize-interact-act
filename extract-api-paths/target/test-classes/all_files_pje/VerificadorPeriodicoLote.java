package br.jus.pje.nucleo.entidades;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "tb_verificador_periodico_lote")
@org.hibernate.annotations.GenericGenerator(name = "gen_verificador_periodico_lote", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_verificador_periodico_lote"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1") })
public class VerificadorPeriodicoLote
		implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<VerificadorPeriodicoLote, Integer> {

	private static final long serialVersionUID = -27240876328014240L;

	private int idVerificadorPeriodicoLote;
	private String passo;
	private UUID lote;
	private Integer idJob;
	private boolean processado = Boolean.FALSE;
	private Date dataJob;
	private Date dataProcessado;
	private Integer qtProcessadoJob;
	private Integer tamanhoJob;

	public VerificadorPeriodicoLote() {
	}

	public VerificadorPeriodicoLote(String passo, UUID lote, Integer idJob, Integer qtProcessadoJob, Integer tamanhoJob,
			Date dataJob) {
		this.passo = passo;
		this.lote = lote;
		this.idJob = idJob;
		this.qtProcessadoJob = qtProcessadoJob;
		this.tamanhoJob = tamanhoJob;
		this.dataJob = dataJob;
	}

	public VerificadorPeriodicoLote(String passo, UUID lote, Integer idJob, Integer qtProcessadoJob,
			Integer tamanhoJob) {
		this.passo = passo;
		this.lote = lote;
		this.idJob = idJob;
		this.qtProcessadoJob = qtProcessadoJob;
		this.tamanhoJob = tamanhoJob;
	}

	@Id
	@GeneratedValue(generator = "gen_verificador_periodico_lote")
	@Column(name = "id_verificador_periodico_lote", unique = true, nullable = false)
	public int getIdVerificadorPeriodicoLote() {
		return idVerificadorPeriodicoLote;
	}

	public void setIdVerificadorPeriodicoLote(int idVerificadorPeriodicoLote) {
		this.idVerificadorPeriodicoLote = idVerificadorPeriodicoLote;
	}

	@Column(name = "ds_passo", nullable = false)
	@NotNull
	public String getPasso() {
		return passo;
	}

	public void setPasso(String passo) {
		this.passo = passo;
	}

	@NotNull
	@Type(type = "pg-uuid")
	@Column(name = "id_lote", nullable = false)
	public UUID getLote() {
		return lote;
	}

	public void setLote(UUID lote) {
		this.lote = lote;
	}

	@NotNull
	@Column(name = "id_job", nullable = false)
	public Integer getIdJob() {
		return idJob;
	}

	public void setIdJob(Integer idJob) {
		this.idJob = idJob;
	}

	@NotNull
	@Column(name = "in_processado", nullable = false)
	public boolean isProcessado() {
		return processado;
	}

	public void setProcessado(boolean processado) {
		this.processado = processado;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_job")
	public Date getDataJob() {
		return dataJob;
	}

	public void setDataJob(Date dataJob) {
		this.dataJob = dataJob;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_processado")
	public Date getDataProcessado() {
		return dataProcessado;
	}

	public void setDataProcessado(Date dataProcessado) {
		this.dataProcessado = dataProcessado;
	}

	@Column(name = "qt_processado_job")
	public Integer getQtProcessadoJob() {
		return qtProcessadoJob;
	}

	public void setQtProcessadoJob(Integer qtProcessadoJob) {
		this.qtProcessadoJob = qtProcessadoJob;
	}

	@Column(name = "nr_tamanho_job")
	public Integer getTamanhoJob() {
		return tamanhoJob;
	}

	public void setTamanhoJob(Integer tamanhoJob) {
		this.tamanhoJob = tamanhoJob;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends VerificadorPeriodicoLote> getEntityClass() {
		return VerificadorPeriodicoLote.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdVerificadorPeriodicoLote());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@Override
	public String toString() {
		return "VerificadorPeriodicoLote [idVerificadorPeriodicoLote=" + idVerificadorPeriodicoLote + ", passo=" + passo
				+ ", lote=" + lote + ", idJob=" + idJob + ", processado=" + processado + ", dataJob=" + dataJob
				+ ", dataProcessado=" + dataProcessado + "]";
	}
}