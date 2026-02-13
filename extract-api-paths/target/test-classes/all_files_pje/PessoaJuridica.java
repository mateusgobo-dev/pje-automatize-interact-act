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

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.TipoOrgaoPublicoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Entity
@Table(name = PessoaJuridica.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_pessoa_juridica")
@Cacheable
public class PessoaJuridica extends Pessoa implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pessoa_juridica";
	private static final long serialVersionUID = 1L;

	private Integer idPessoaJuridica;
	private RamoAtividade ramoAtividade;
	private String numeroCNPJ;
	private String nomeFantasia;
	private Date dataAbertura;
	private String numeroCpfResponsavel;
	private String nomeResponsavel;
	private String numeroRegistroJuntaComercial;
	private Estado ufJuntaComercial;
	private Date dataFimAtividade;
	private Boolean orgaoPublico = Boolean.FALSE;
	private Integer prazoExpedienteAutomatico;
	private TipoOrgaoPublicoEnum tipoOrgaoPublico;
	private Double valorLimiteRpv;
	private Boolean matriz = Boolean.TRUE;
	private PessoaJuridica pessoaJuridicaMatriz;
	private Boolean associarPapelParaRemessa = Boolean.FALSE;
	
	public PessoaJuridica() {
		setInTipoPessoa(TipoPessoaEnum.J);
	}

	@Basic(optional=true)
	@Column(name="id_pessoa_juridica", insertable=false, updatable=false)
	public Integer getIdPessoaJuridica() {
		return idPessoaJuridica;
	}
	
	public void setIdPessoaJuridica(Integer idPessoaJuridica) {
		this.idPessoaJuridica = idPessoaJuridica;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_ramo_atividade")
	public RamoAtividade getRamoAtividade() {
		return ramoAtividade;
	}

	public void setRamoAtividade(RamoAtividade ramoAtividade) {
		this.ramoAtividade = ramoAtividade;
	}

	@Transient
	public String getNumeroCNPJ() {
		return this.numeroCNPJ;
	}

    


	public void setNumeroCNPJ(String numeroCNPJ) {
		this.numeroCNPJ = numeroCNPJ;
	}

	@Column(name = "nm_fantasia", length = 255)
	@Length(max = 255)
	public String getNomeFantasia() {
		return this.nomeFantasia;
	}

	public void setNomeFantasia(String nomeFantasia) {
		this.nomeFantasia = nomeFantasia;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_abertura")
	public Date getDataAbertura() {
		return this.dataAbertura;
	}

	public void setDataAbertura(Date dataAbertura) {
		this.dataAbertura = dataAbertura;
	}

	@Column(name = "nr_cpf_responsavel", length = 15)
	@Length(max = 15)
	public String getNumeroCpfResponsavel() {
		return numeroCpfResponsavel;
	}

	public void setNumeroCpfResponsavel(String numeroCpfResponsavel) {
		this.numeroCpfResponsavel = numeroCpfResponsavel;
	}

	@Column(name = "in_orgao_publico")
	public Boolean getOrgaoPublico() {
		return orgaoPublico;
	}

	public void setOrgaoPublico(Boolean orgaoPublico) {
		this.orgaoPublico = orgaoPublico;
	}

	@Column(name = "nm_responsavel", length = 255)
	@Length(max = 255)
	public String getNomeResponsavel() {
		return this.nomeResponsavel;
	}

	public void setNomeResponsavel(String nomeResponsavel) {
		this.nomeResponsavel = nomeResponsavel;
	}

	@Transient
	public String getNumeroRegistroJuntaComercial() {
		return this.numeroRegistroJuntaComercial;
	}

	public void setNumeroRegistroJuntaComercial(String numeroRegistroJuntaComercial) {
		this.numeroRegistroJuntaComercial = numeroRegistroJuntaComercial;
	}

	@Transient
	public Estado getUfJuntaComercial() {
		return this.ufJuntaComercial;
	}

	public void setUfJuntaComercial(Estado ufJuntaComercial) {
		this.ufJuntaComercial = ufJuntaComercial;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim_atividade")
	public Date getDataFimAtividade() {
		return this.dataFimAtividade;
	}

	public void setDataFimAtividade(Date dataFimAtividade) {
		this.dataFimAtividade = dataFimAtividade;
	}

	@Column(name = "tp_prazo_expediente_automatico")
	public Integer getPrazoExpedienteAutomatico() {
		return prazoExpedienteAutomatico;
	}

	public void setPrazoExpedienteAutomatico(Integer prazoExpedienteAutomatico) {
		this.prazoExpedienteAutomatico = prazoExpedienteAutomatico;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="tp_orgao_publico")	
	public TipoOrgaoPublicoEnum getTipoOrgaoPublico() {
		return tipoOrgaoPublico;
	}
	
	public void setTipoOrgaoPublico(TipoOrgaoPublicoEnum tipoOrgaoPublico) {
		this.tipoOrgaoPublico = tipoOrgaoPublico;
	}
	
	@Column(name = "vl_limite_rpv")
	public Double getValorLimiteRpv() {
		return valorLimiteRpv;
	}
	
	public void setValorLimiteRpv(Double valorLimiteRpv) {
		this.valorLimiteRpv = valorLimiteRpv;
	}

	@Column(name = "in_matriz", nullable = false)
	public Boolean getMatriz() {
		return matriz;
	}

	public void setMatriz(Boolean matriz) {
		this.matriz = matriz;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "id_pessoa_juridica_matriz")
	public PessoaJuridica getPessoaJuridicaMatriz() {
		return pessoaJuridicaMatriz;
	}

	public void setPessoaJuridicaMatriz(PessoaJuridica pessoaJuridicaMatriz) {
		this.pessoaJuridicaMatriz = pessoaJuridicaMatriz;
	}	

	@Transient
	@Override
	public Class<? extends UsuarioLogin> getEntityClass() {
		return PessoaJuridica.class;
	}

	/**
	 * @return associarPapelParaRemessa.
	 */
	@Transient
	public Boolean getAssociarPapelParaRemessa() {
		return associarPapelParaRemessa;
	}

	/**
	 * @param associarPapelParaRemessa Atribui associarPapelParaRemessa.
	 */
	public void setAssociarPapelParaRemessa(Boolean associarPapelParaRemessa) {
		this.associarPapelParaRemessa = associarPapelParaRemessa;
	}
}
