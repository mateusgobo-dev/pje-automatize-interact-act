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
package br.jus.pje.jt.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.jt.enums.MotivoDispensaEnum;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;

@Entity
@Table(name = AssistenteAdmissibilidadeRecurso.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_assist_admis_rec", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_ass_admis_rec"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AssistenteAdmissibilidadeRecurso implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AssistenteAdmissibilidadeRecurso,Integer> {

	public static final String TABLE_NAME = "tb_ass_admis_rec";
	private static final long serialVersionUID = 1L;

	private int idAssistenteAdmissibilidadeRecurso;
	private ProcessoDocumento processoDocumento;
	private AssistenteAdmissibilidade assistenteAdmissibilidade;
	private String observacao;
	private Date dataCienciaDecisao;
	private Date dataRecurso;
	private Integer prazoDias;
	private Boolean tempestividade = Boolean.FALSE;
	private Double valorDeposito;
	private Boolean dispensado = Boolean.FALSE;
	private MotivoDispensaEnum motivoDispensa;
	private Double valorCustas;
	private Boolean dispensa = Boolean.FALSE;
	private Boolean preparo = Boolean.FALSE;
	private Boolean representacao = Boolean.TRUE;
	private String tipoPolo;
	private Integer prioridade;
	private Boolean admissibilidade = Boolean.FALSE;
	
	private List<ProcessoParte> listaProcessoParte = new ArrayList<ProcessoParte>();

	@Id
	@GeneratedValue(generator = "gen_assist_admis_rec")
	@Column(name = "id_assis_admis_rec", columnDefinition = "integer", nullable = false, unique = true)
	@NotNull
	public int getIdAssistenteAdmissibilidadeRecurso() {
		return idAssistenteAdmissibilidadeRecurso;
	}

	public void setIdAssistenteAdmissibilidadeRecurso(int idAssistenteAdmissibilidadeRecurso) {
		this.idAssistenteAdmissibilidadeRecurso = idAssistenteAdmissibilidadeRecurso;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_doc")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_assis_admis")
	public AssistenteAdmissibilidade getAssistenteAdmissibilidade() {
		return assistenteAdmissibilidade;
	}

	public void setAssistenteAdmissibilidade(AssistenteAdmissibilidade assistenteAdmissibilidade) {
		this.assistenteAdmissibilidade = assistenteAdmissibilidade;
	}

	@Column(name = "ds_observacao")
	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ciencia_decisao")	
	public Date getDataCienciaDecisao() {
		return dataCienciaDecisao;
	}

	public void setDataCienciaDecisao(Date dataCienciaDecisao) {
		this.dataCienciaDecisao = dataCienciaDecisao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_recurso")
	public Date getDataRecurso() {
		return dataRecurso;
	}

	public void setDataRecurso(Date dataRecurso) {
		this.dataRecurso = dataRecurso;
	}

	@Column(name = "nr_prazo_dias")
	public Integer getPrazoDias() {
		return prazoDias;
	}

	public void setPrazoDias(Integer prazoDias) {
		this.prazoDias = prazoDias;
	}

	@Column(name = "in_tempestividade")
	public Boolean getTempestividade() {
		return tempestividade;
	}

	public void setTempestividade(Boolean tempestividade) {
		this.tempestividade = tempestividade;
	}

	@Column(name = "vl_deposito")
	public Double getValorDeposito() {
		return valorDeposito;
	}

	public void setValorDeposito(Double valorDeposito) {
		this.valorDeposito = valorDeposito;
	}

	@Column(name = "in_dispensado")
	public Boolean getDispensado() {
		return dispensado;
	}

	public void setDispensado(Boolean dispensado) {
		this.dispensado = dispensado;
	}

	@Column(name = "ds_motivo_dispensa", length = 50)
	@Enumerated(EnumType.STRING)
	public MotivoDispensaEnum getMotivoDispensa() {
		return motivoDispensa;
	}

	public void setMotivoDispensa(MotivoDispensaEnum motivoDispensa) {
		this.motivoDispensa = motivoDispensa;
	}

	@Column(name = "vl_custas")
	public Double getValorCustas() {
		return valorCustas;
	}

	public void setValorCustas(Double valorCustas) {
		this.valorCustas = valorCustas;
	}

	@Column(name = "in_dispensa")
	public Boolean getDispensa() {
		return dispensa;
	}

	public void setDispensa(Boolean dispensa) {
		this.dispensa = dispensa;
	}

	@Column(name = "in_preparo")
	public Boolean getPreparo() {
		return preparo;
	}

	public void setPreparo(Boolean preparo) {
		this.preparo = preparo;
	}

	@Column(name = "in_representacao")
	public Boolean getRepresentacao() {
		return representacao;
	}

	public void setRepresentacao(Boolean representacao) {
		this.representacao = representacao;
	}

	@Column(name = "ds_tipo_polo", length = 100)
	@Length(max = 100)
	public String getTipoPolo() {
		return tipoPolo;
	}

	public void setTipoPolo(String tipoPolo) {
		this.tipoPolo = tipoPolo;
	}
	
	@Column(name = "nr_prioridade")
	public Integer getPrioridade() {
		return prioridade;
	}
	
	public void setPrioridade(Integer prioridade) {
		this.prioridade = prioridade;
	}

	@Column(name = "in_admissibilidade")
	public Boolean getAdmissibilidade() {
		return admissibilidade;
	}

	public void setAdmissibilidade(Boolean admissibilidade) {
		this.admissibilidade = admissibilidade;
	}

	@Transient
	public List<ProcessoParte> getListaProcessoParte() {
		return listaProcessoParte;
	}

	public void setListaProcessoParte(List<ProcessoParte> listaProcessoParte) {
		this.listaProcessoParte = listaProcessoParte;
	}
	
	@Transient
	public String getPartes(){
		String partes = "";
		if(listaProcessoParte != null && !listaProcessoParte.isEmpty()){
			partes = listaProcessoParte.get(0).getNomeParte();
			if(listaProcessoParte.size() > 1){
				partes = partes + " e outros";
			}
		}
		return partes;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AssistenteAdmissibilidadeRecurso> getEntityClass() {
		return AssistenteAdmissibilidadeRecurso.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAssistenteAdmissibilidadeRecurso());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
