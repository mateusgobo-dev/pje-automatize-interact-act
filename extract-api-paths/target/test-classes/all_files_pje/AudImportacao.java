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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Entity
@Table(name = "tb_aud_importacao")
@org.hibernate.annotations.GenericGenerator(name = "gen_aud_importacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_aud_importacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AudImportacao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<AudImportacao,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idAudImportacao;
	private List<AudParteImportacao> audParteImportacao;
	private List<AudParcelaImportacao> audParcelaImportacao;
	private List<AudVerbaImportacao> audVerbaImportacao;

	private Integer idProcesso;
	private Integer idProcessoAudiencia;
	private Date dtInicio;
	private Date dtFim;
	private Date dtConsolidacao;
	private Date dtValidacao;
	private Integer numProcesso;
	private Integer anoProcesso;
	private Integer origemProcesso;
	private Integer orgaoJustica;// primeiro dígito de
	// nr_identificacao_orgao_justica
	private Integer regional;// dois últimos dígitos de
	// nr_identificacao_orgao_justica
	private Integer dvProcesso;
	private Double valorCausa;
	private String nomeMagistrado;
	private Integer idPessoaMagistrado;
	private String audienciaAdiada;

	// Conteúdo HTML da ata de audiência e outros possíveis documentos
	private String conteudoDocumento;

	private String observacoes;

	// Dados de Acordo (Conciliação)
	private Double valorAcordo;
	private String numParcelas;

	// dados de pericia
	private String nomePerito;
	private String prazoQuesitos;
	private Date dtComumQuesitos;
	private Date dtAutorQuesitos;
	private Date dtReuQuesitos;
	private String prazoLaudo;
	private Date dtInicioPrazoLaudo;
	private String prazoPartes;
	private Date dtInicioConstestarAutor;
	private Date dtInicioConstestarReu;

	private String desistencia;
	private String incompetencia;
	private String andamentoEncerramento;
	private Date dtAdiamento;

	// dados das custas
	private Double valorcustasAutor;
	private Double valorcustasReu;
	private String autorIsento;
	private String reuIsento;

	private ProcessoTrf processoTrf;
	@Transient        
	private String dataAdiamento;

	@Id
	@Column(name = "id_aud_importacao", unique = true, nullable = false)
	@GeneratedValue(generator = "gen_aud_importacao")
	public Integer getIdAudImportacao() {
		return idAudImportacao;
	}

	public void setIdAudImportacao(Integer idAudImportacao) {
		this.idAudImportacao = idAudImportacao;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "audImportacao")
	public List<AudParteImportacao> getAudParteImportacao() {
		return audParteImportacao;
	}

	public void setAudParteImportacao(List<AudParteImportacao> audParteImportacao) {
		this.audParteImportacao = audParteImportacao;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "audImportacao")
	public List<AudParcelaImportacao> getAudParcelaImportacao() {
		return audParcelaImportacao;
	}

	public void setAudParcelaImportacao(List<AudParcelaImportacao> audParcelaImportacao) {
		this.audParcelaImportacao = audParcelaImportacao;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "audImportacao")
	public List<AudVerbaImportacao> getAudVerbaImportacao() {
		return audVerbaImportacao;
	}

	public void setAudVerbaImportacao(List<AudVerbaImportacao> audVerbaImportacao) {
		this.audVerbaImportacao = audVerbaImportacao;
	}

	@Column(name = "id_processo", nullable = false)
	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}

	@Column(name = "id_processo_audiencia", nullable = false)
	public Integer getIdProcessoAudiencia() {
		return idProcessoAudiencia;
	}

	public void setIdProcessoAudiencia(Integer idProcessoAudiencia) {
		this.idProcessoAudiencia = idProcessoAudiencia;
	}

	@Column(name = "dt_inicio")
	public Date getDtInicio() {
		return dtInicio;
	}

	public void setDtInicio(Date dtInicio) {
		this.dtInicio = dtInicio;
	}

	@Column(name = "dt_fim")
	public Date getDtFim() {
		return dtFim;
	}

	public void setDtFim(Date dtFim) {
		this.dtFim = dtFim;
	}

	@Column(name = "dt_consolidacao")
	public Date getDtConsolidacao() {
		return dtConsolidacao;
	}

	public void setDtConsolidacao(Date dtConsolidacao) {
		this.dtConsolidacao = dtConsolidacao;
	}

	@Column(name = "dt_validacao")
	public Date getDtValidacao() {
		return dtValidacao;
	}

	public void setDtValidacao(Date dtValidacao) {
		this.dtValidacao = dtValidacao;
	}

	@Column(name = "nr_sequencia")
	public Integer getNumProcesso() {
		return numProcesso;
	}

	public void setNumProcesso(Integer numProcesso) {
		this.numProcesso = numProcesso;
	}

	@Column(name = "nr_ano")
	public Integer getAnoProcesso() {
		return anoProcesso;
	}

	public void setAnoProcesso(Integer anoProcesso) {
		this.anoProcesso = anoProcesso;
	}

	@Column(name = "nr_origem_processo")
	public Integer getOrigemProcesso() {
		return origemProcesso;
	}

	public void setOrigemProcesso(Integer origemProcesso) {
		this.origemProcesso = origemProcesso;
	}

	@Column(name = "nr_orgao_justica")
	public Integer getOrgaoJustica() {
		return orgaoJustica;
	}

	public void setOrgaoJustica(Integer orgaoJustica) {
		this.orgaoJustica = orgaoJustica;
	}

	@Column(name = "nr_regional")
	public Integer getRegional() {
		return regional;
	}

	public void setRegional(Integer regional) {
		this.regional = regional;
	}

	@Column(name = "nr_digito_verificador")
	public Integer getDvProcesso() {
		return dvProcesso;
	}

	public void setDvProcesso(Integer dvProcesso) {
		this.dvProcesso = dvProcesso;
	}

	@Column(name = "vl_causa")
	public Double getValorCausa() {
		return valorCausa;
	}

	public void setValorCausa(Double valorCausa) {
		this.valorCausa = valorCausa;
	}

	@Column(name = "ds_nome_magistrado")
	public String getNomeMagistrado() {
		return nomeMagistrado;
	}

	public void setNomeMagistrado(String nomeMagistrado) {
		this.nomeMagistrado = nomeMagistrado;
	}

	@Column(name = "id_pessoa_magistrado")
	public Integer getIdPessoaMagistrado() {
		return idPessoaMagistrado;
	}

	public void setIdPessoaMagistrado(Integer idPessoaMagistrado) {
		this.idPessoaMagistrado = idPessoaMagistrado;
	}

	@Column(name = "in_audiencia_adiada")
	public String getAudienciaAdiada() {
		return audienciaAdiada;
	}

	public void setAudienciaAdiada(String audienciaAdiada) {
		this.audienciaAdiada = audienciaAdiada;
	}

	@Column(name = "ds_conteudo_documento")
	public String getConteudoDocumento() {
		return conteudoDocumento;
	}

	public void setConteudoDocumento(String conteudoDocumento) {
		this.conteudoDocumento = conteudoDocumento;
	}

	@Column(name = "ds_observacoes")
	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	@Column(name = "vl_acordo")
	public Double getValorAcordo() {
		return valorAcordo;
	}

	public void setValorAcordo(Double valorAcordo) {
		this.valorAcordo = valorAcordo;
	}

	@Column(name = "nr_parcelas")
	public String getNumParcelas() {
		return numParcelas;
	}

	public void setNumParcelas(String numParcelas) {
		this.numParcelas = numParcelas;
	}

	@Column(name = "ds_nome_perito")
	public String getNomePerito() {
		return nomePerito;
	}

	public void setNomePerito(String nomePerito) {
		this.nomePerito = nomePerito;
	}

	@Column(name = "nr_prazo_quesitos")
	public String getPrazoQuesitos() {
		return prazoQuesitos;
	}

	public void setPrazoQuesitos(String prazoQuesitos) {
		this.prazoQuesitos = prazoQuesitos;
	}

	@Column(name = "dt_comum_quesitos")
	public Date getDtComumQuesitos() {
		return dtComumQuesitos;
	}

	public void setDtComumQuesitos(Date dtComumQuesitos) {
		this.dtComumQuesitos = dtComumQuesitos;
	}

	@Column(name = "dt_autor_quesitos")
	public Date getDtAutorQuesitos() {
		return dtAutorQuesitos;
	}

	public void setDtAutorQuesitos(Date dtAutorQuesitos) {
		this.dtAutorQuesitos = dtAutorQuesitos;
	}

	@Column(name = "dt_reu_quesitos")
	public Date getDtReuQuesitos() {
		return dtReuQuesitos;
	}

	public void setDtReuQuesitos(Date dtReuQuesitos) {
		this.dtReuQuesitos = dtReuQuesitos;
	}

	@Column(name = "nr_prazo_laudo")
	public String getPrazoLaudo() {
		return prazoLaudo;
	}

	public void setPrazoLaudo(String prazoLaudo) {
		this.prazoLaudo = prazoLaudo;
	}

	@Column(name = "dt_ini_prazo_laudo")
	public Date getDtInicioPrazoLaudo() {
		return dtInicioPrazoLaudo;
	}

	public void setDtInicioPrazoLaudo(Date dtInicioPrazoLaudo) {
		this.dtInicioPrazoLaudo = dtInicioPrazoLaudo;
	}

	@Column(name = "nr_prazo_partes")
	public String getPrazoPartes() {
		return prazoPartes;
	}

	public void setPrazoPartes(String prazoPartes) {
		this.prazoPartes = prazoPartes;
	}

	@Column(name = "dt_ini_contestar_autor")
	public Date getDtInicioConstestarAutor() {
		return dtInicioConstestarAutor;
	}

	public void setDtInicioConstestarAutor(Date dtInicioConstestarAutor) {
		this.dtInicioConstestarAutor = dtInicioConstestarAutor;
	}

	@Column(name = "dt_ini_contestar_reu")
	public Date getDtInicioConstestarReu() {
		return dtInicioConstestarReu;
	}

	public void setDtInicioConstestarReu(Date dtInicioConstestarReu) {
		this.dtInicioConstestarReu = dtInicioConstestarReu;
	}

	@Column(name = "ds_desistencia")
	public String getDesistencia() {
		return desistencia;
	}

	public void setDesistencia(String desistencia) {
		this.desistencia = desistencia;
	}

	@Column(name = "ds_incompetencia")
	public String getIncompetencia() {
		return incompetencia;
	}

	public void setIncompetencia(String incompetencia) {
		this.incompetencia = incompetencia;
	}

	@Column(name = "ds_andamento_encerramento")
	public String getAndamentoEncerramento() {
		return andamentoEncerramento;
	}

	public void setAndamentoEncerramento(String andamentoEncerramento) {
		this.andamentoEncerramento = andamentoEncerramento;
	}

	@Column(name = "vl_custas_autor")
	public Double getValorcustasAutor() {
		return valorcustasAutor;
	}

	public void setValorcustasAutor(Double valorcustasAutor) {
		this.valorcustasAutor = valorcustasAutor;
	}

	@Column(name = "vl_custas_reu")
	public Double getValorcustasReu() {
		return valorcustasReu;
	}

	public void setValorcustasReu(Double valorcustasReu) {
		this.valorcustasReu = valorcustasReu;
	}

	@Column(name = "in_autor_isento")
	public String getAutorIsento() {
		return autorIsento;
	}

	public void setAutorIsento(String autorIsento) {
		this.autorIsento = autorIsento;
	}

	@Column(name = "in_reu_isento")
	public String getReuIsento() {
		return reuIsento;
	}

	public void setReuIsento(String reuIsento) {
		this.reuIsento = reuIsento;
	}

	@Column(name = "dt_adiamento")
	public Date getDtAdiamento() {
		return dtAdiamento;
	}

	public void setDtAdiamento(Date dtAdiamento) {
		this.dtAdiamento = dtAdiamento;
	}

	@XmlTransient
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo", nullable = false, insertable = false, updatable = false)
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Transient
	public boolean isNovoProcesso() {
		return getIdProcesso() != null && getIdProcesso() > 0;
	}

	public void transformarDados() throws Exception { 
		if(dataAdiamento != null && !dataAdiamento.equals("")) {
			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm"); 
			Date date = formatter.parse(dataAdiamento);  
			setDtAdiamento(date); 
		}
	} 

	@Transient        
	public String getDataAdiamento() {                
		return dataAdiamento;        
	}        

	public void setDataAdiamento(String dataAdiamento) {                
		this.dataAdiamento = dataAdiamento;        
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AudImportacao> getEntityClass() {
		return AudImportacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdAudImportacao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
