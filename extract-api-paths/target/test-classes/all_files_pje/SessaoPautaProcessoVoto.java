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

import java.util.Date;

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

import br.jus.pje.nucleo.enums.VotoEnum;

@Entity
@Table(name = SessaoPautaProcessoVoto.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_ses_pauta_proc_voto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_ses_pauta_proc_voto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SessaoPautaProcessoVoto implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SessaoPautaProcessoVoto,Integer> {

	public static final String TABLE_NAME = "tb_ses_pauta_proc_voto";
	private static final long serialVersionUID = 1L;

	private int idSessaoPautaProcessoVoto;
	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;
	private ProcessoTrf processoTrf;
	private VotoEnum tipoResultadoJulgamento;
	private Date dataRelatorio;
	private Date dataVoto;

	private Boolean liberaVotoAntecipado;
	private Boolean liberaRelatorioAntecipado;
	private Boolean impedimentoSuspeicao;
	private Boolean destaqueSessao;
	private Boolean ativo;

	private ProcessoDocumento processoDocumentoVoto;
	private ProcessoDocumento processoDocumentoRelatorio;
	private ProcessoDocumento processoDocumentoAcordao;

	private PessoaMagistrado usuarioRelator;
	private PessoaMagistrado usuarioRevisor;
	private PessoaMagistrado usuarioVogal;
	private PessoaMagistrado usuarioDesembargadorAcompanhado;

	private OrgaoJulgador orgaoJulgadorRelator;
	private OrgaoJulgador orgaoJulgadorRevisor;
	private OrgaoJulgador orgaoJulgadorVogal;
	private OrgaoJulgador orgaoJulgadorDesembargadorAcompanhado;

	private Date dataAlteracaoVoto;

	private Boolean check;

	public SessaoPautaProcessoVoto() {
	}

	@Id
	@GeneratedValue(generator = "gen_ses_pauta_proc_voto")
	@Column(name = "id_ses_pauta_proc_voto", unique = true, nullable = false)
	public int getIdSessaoPautaProcessoVoto() {
		return this.idSessaoPautaProcessoVoto;
	}

	public void setIdSessaoPautaProcessoVoto(int idSessaoPautaProcessoVoto) {
		this.idSessaoPautaProcessoVoto = idSessaoPautaProcessoVoto;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao_pauta_processo_trf", nullable = false)
	@NotNull
	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf() {
		return this.sessaoPautaProcessoTrf;
	}

	public void setSessaoPautaProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		this.sessaoPautaProcessoTrf = sessaoPautaProcessoTrf;
	}

	@Column(name = "tp_resultado_julgamento", length = 2)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.VotoType")
	public VotoEnum getTipoResultadoJulgamento() {
		return this.tipoResultadoJulgamento;
	}

	public void setTipoResultadoJulgamento(VotoEnum tipoResultadoJulgamento) {
		this.tipoResultadoJulgamento = tipoResultadoJulgamento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_relatorio")
	public Date getDataRelatorio() {
		return this.dataRelatorio;
	}

	public void setDataRelatorio(Date dataRelatorio) {
		this.dataRelatorio = dataRelatorio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_voto")
	public Date getDataVoto() {
		return this.dataVoto;
	}

	public void setDataVoto(Date dataVoto) {
		this.dataVoto = dataVoto;
	}

	@Column(name = "in_libera_voto_antecipado", nullable = false)
	public Boolean getLiberaVotoAntecipado() {
		return liberaVotoAntecipado;
	}

	public void setLiberaVotoAntecipado(Boolean liberaVotoAntecipado) {
		this.liberaVotoAntecipado = liberaVotoAntecipado;
	}

	@Column(name = "in_libera_relatorio_antecipado", nullable = false)
	@NotNull
	public Boolean getLiberaRelatorioAntecipado() {
		return liberaRelatorioAntecipado;
	}

	public void setLiberaRelatorioAntecipado(Boolean liberaRelatorioAntecipado) {
		this.liberaRelatorioAntecipado = liberaRelatorioAntecipado;
	}

	@Column(name = "in_impedimento_suspeicao", nullable = false)
	@NotNull
	public Boolean getImpedimentoSuspeicao() {
		return impedimentoSuspeicao;
	}

	public void setImpedimentoSuspeicao(Boolean impedimentoSuspeicao) {
		this.impedimentoSuspeicao = impedimentoSuspeicao;
	}

	@Column(name = "in_destaque_sessao", nullable = false)
	@NotNull
	public Boolean getDestaqueSessao() {
		return destaqueSessao;
	}

	public void setDestaqueSessao(Boolean destaqueSessao) {
		this.destaqueSessao = destaqueSessao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_doc_voto")
	public ProcessoDocumento getProcessoDocumentoVoto() {
		return this.processoDocumentoVoto;
	}

	public void setProcessoDocumentoVoto(ProcessoDocumento processoDocumentoVoto) {
		this.processoDocumentoVoto = processoDocumentoVoto;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_doc_relatorio")
	public ProcessoDocumento getProcessoDocumentoRelatorio() {
		return this.processoDocumentoRelatorio;
	}

	public void setProcessoDocumentoRelatorio(ProcessoDocumento processoDocumentoRelatorio) {
		this.processoDocumentoRelatorio = processoDocumentoRelatorio;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_doc_acordao")
	public ProcessoDocumento getProcessoDocumentoAcordao() {
		return this.processoDocumentoAcordao;
	}

	public void setProcessoDocumentoAcordao(ProcessoDocumento processoDocumentoAcordao) {
		this.processoDocumentoAcordao = processoDocumentoAcordao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_relator")
	public PessoaMagistrado getUsuarioRelator() {
		return this.usuarioRelator;
	}

	public void setUsuarioRelator(PessoaMagistrado usuarioRelator) {
		this.usuarioRelator = usuarioRelator;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_revisor")
	public PessoaMagistrado getUsuarioRevisor() {
		return this.usuarioRevisor;
	}

	public void setUsuarioRevisor(PessoaMagistrado usuarioRevisor) {
		this.usuarioRevisor = usuarioRevisor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_vogal")
	public PessoaMagistrado getUsuarioVogal() {
		return this.usuarioVogal;
	}

	public void setUsuarioVogal(PessoaMagistrado usuarioVogal) {
		this.usuarioVogal = usuarioVogal;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usu_des_acompanhado")
	public PessoaMagistrado getUsuarioDesembargadorAcompanhado() {
		return this.usuarioDesembargadorAcompanhado;
	}

	public void setUsuarioDesembargadorAcompanhado(PessoaMagistrado usuarioDesembargadorAcompanhado) {
		this.usuarioDesembargadorAcompanhado = usuarioDesembargadorAcompanhado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj_relator")
	public OrgaoJulgador getOrgaoJulgadorRelator() {
		return this.orgaoJulgadorRelator;
	}

	public void setOrgaoJulgadorRelator(OrgaoJulgador orgaoJulgadorRelator) {
		this.orgaoJulgadorRelator = orgaoJulgadorRelator;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj_revisor")
	public OrgaoJulgador getOrgaoJulgadorRevisor() {
		return this.orgaoJulgadorRevisor;
	}

	public void setOrgaoJulgadorRevisor(OrgaoJulgador orgaoJulgadorRevisor) {
		this.orgaoJulgadorRevisor = orgaoJulgadorRevisor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj_vogal")
	public OrgaoJulgador getOrgaoJulgadorVogal() {
		return this.orgaoJulgadorVogal;
	}

	public void setOrgaoJulgadorVogal(OrgaoJulgador orgaoJulgadorVogal) {
		this.orgaoJulgadorVogal = orgaoJulgadorVogal;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj_des_acompanhado")
	public OrgaoJulgador getOrgaoJulgadorDesembargadorAcompanhado() {
		return this.orgaoJulgadorDesembargadorAcompanhado;
	}

	public void setOrgaoJulgadorDesembargadorAcompanhado(OrgaoJulgador orgaoJulgadorDesembargadorAcompanhado) {
		this.orgaoJulgadorDesembargadorAcompanhado = orgaoJulgadorDesembargadorAcompanhado;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_alteracao_voto")
	public Date getDataAlteracaoVoto() {
		return this.dataAlteracaoVoto;
	}

	public void setDataAlteracaoVoto(Date dataAlteracaoVoto) {
		this.dataAlteracaoVoto = dataAlteracaoVoto;
	}

	@Transient
	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SessaoPautaProcessoVoto> getEntityClass() {
		return SessaoPautaProcessoVoto.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdSessaoPautaProcessoVoto());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
