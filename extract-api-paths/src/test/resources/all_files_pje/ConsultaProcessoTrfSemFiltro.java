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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.SituacaoGuiaRecolhimentoEnum;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = ConsultaProcessoTrfSemFiltro.TABLE_NAME)
public class ConsultaProcessoTrfSemFiltro implements java.io.Serializable {

	private static final String E_OUTROS = " e outros";
	public static final String TABLE_NAME = "tb_cabecalho_processo";
	private static final long serialVersionUID = 1L;

	private int idProcessoTrf;
	private String numeroProcesso;
	private Integer numeroSequencia;
	private Integer ano;
	private Integer numeroDigitoVerificador;
	private Integer numeroOrgaoJustica;
	private Integer numeroOrigem;
	private Double valorCausa;
	private Boolean justicaGratuita;
	private Boolean tutelaLiminar;
	private Boolean segredoJustica;
	private String observacaoSegredo;
	private ProcessoTrfApreciadoEnum apreciadoSegredo;
	private ProcessoTrfApreciadoEnum apreciadoSigilo;
	private Boolean prioridade;
	private Integer pesoPrioridade;
	private Date dataAutuacao;
	private Date dataDistribuicao;
	private Date dtTransitadoJulgado;
	private Date dtSolicitacaoInclusaoPauta;
	private ClasseJudicialInicialEnum inicial;
	private ProcessoStatusEnum processoStatus;
	private Integer idLocalizacaoOrgaoJulgador;
	private Integer idOrgaoJulgador;
	private String orgaoJulgador;
	private String instanciaOrgaoJulgador;
	private String codigoIbgeOrgaoJulgador;
	private Integer idOrgaoJulgadorColegiado;
	private String orgaoJulgadorColegiado;
	private Integer idOrgaoJulgadorCargo;
	private String orgaoJulgadorCargo;
	private Integer idLocalizacaoInicial;
	private Integer idEstruturaInicial;
	private Integer idCaixa;
	private Integer idUsuarioCadastroProcesso;
	private Integer idProcessoAssuntoPrincipal;
	private Integer idAssuntoPrincipal;
	private String codigoAssuntoPrincipal;
	private String assuntoPrincipal;
	private Integer idClasseJudicial;
	private String codigoClasseJudicial;
	private String siglaClasseJudicial;
	private String classeJudicial;
	private Integer idJurisdicao;
	private String nomeJurisdicao;
	private Integer idCompetencia;
	private String competencia;
	private Integer idAutor;
	private String autor;
	private long qtAutor;
	private Integer idReu;
	private String reu;
	private long qtReu;
	private Integer idUltimoMovimento;
	private String ultimoMovimento;
	private Date dataUltimoMovimento;
	private Jurisdicao jurisdicao;
	private ClasseJudicial classeJudicialObj;
	private ProcessoTrf processoTrf;
	private Boolean check;
	private int nivelAcesso;
	private SituacaoGuiaRecolhimentoEnum situacaoGuiaRecolhimento;
	
	@Column(name = "nr_sequencia", insertable = false, updatable = false)
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	@Column(name = "nr_digito_verificador", insertable = false, updatable = false)
	public Integer getNumeroDigitoVerificador() {
		return numeroDigitoVerificador;
	}

	public void setNumeroDigitoVerificador(Integer numeroDigitoVerificador) {
		this.numeroDigitoVerificador = numeroDigitoVerificador;
	}

	@Column(name = "nr_identificacao_orgao_justica", insertable = false, updatable = false)
	public Integer getNumeroOrgaoJustica() {
		return numeroOrgaoJustica;
	}

	public void setNumeroOrgaoJustica(Integer numeroOrgaoJustica) {
		this.numeroOrgaoJustica = numeroOrgaoJustica;
	}

	@Column(name = "nr_ano", insertable = false, updatable = false)
	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	@Column(name = "nr_origem_processo", insertable = false, updatable = false)
	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	@Column(name = "vl_causa", insertable = false, updatable = false)
	public Double getValorCausa() {
		return this.valorCausa;
	}

	public void setValorCausa(Double valorCausa) {
		this.valorCausa = valorCausa;
	}

	public ConsultaProcessoTrfSemFiltro() {
	}

	@Id
	@Column(name = "id_processo_trf", insertable = false, updatable = false)
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(int idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	@Column(name = "nr_processo", insertable = false, updatable = false)
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_jurisdicao", insertable = false, updatable = false)
	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	@Column(name = "id_localizacao_oj", insertable = false, updatable = false)
	public Integer getIdLocalizacaoOj() {
		return idLocalizacaoOrgaoJulgador;
	}

	public void setIdLocalizacaoOj(Integer idLocalizacaoOrgaoJulgador) {
		this.idLocalizacaoOrgaoJulgador = idLocalizacaoOrgaoJulgador;
	}
	
	@Column(name = "id_orgao_julgador", insertable = false, updatable = false)
	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}

	@Column(name = "ds_orgao_julgador", insertable = false, updatable = false)
	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@Column(name = "in_instancia_orgao_julgador", insertable = false, updatable = false)
	public String getInstanciaOrgaoJulgador() {
		return instanciaOrgaoJulgador;
	}

	public void setInstanciaOrgaoJulgador(String instanciaOrgaoJulgador) {
		this.instanciaOrgaoJulgador = instanciaOrgaoJulgador;
	}

	@Column(name = "cd_ibge_orgao_julgador", insertable = false, updatable = false)
	public String getCodigoIbgeOrgaoJulgador() {
		return codigoIbgeOrgaoJulgador;
	}

	public void setCodigoIbgeOrgaoJulgador(String codigoIbgeOrgaoJulgador) {
		this.codigoIbgeOrgaoJulgador = codigoIbgeOrgaoJulgador;
	}

	@Column(name = "ds_orgao_julgador_colegiado", insertable = false, updatable = false)
	public String getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(String orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@Column(name = "id_orgao_julgador_cargo", insertable = false, updatable = false)
	public Integer getIdOrgaoJulgadorCargo() {
		return idOrgaoJulgadorCargo;
	}

	public void setIdOrgaoJulgadorCargo(Integer idOrgaoJulgadorCargo) {
		this.idOrgaoJulgadorCargo = idOrgaoJulgadorCargo;
	}

	@Column(name = "ds_orgao_julgador_cargo", insertable = false, updatable = false)
	public String getOrgaoJulgadorCargo() {
		return orgaoJulgadorCargo;
	}

	public void setOrgaoJulgadorCargo(String orgaoJulgadorCargo) {
		this.orgaoJulgadorCargo = orgaoJulgadorCargo;
	}

	@Column(name = "id_localizacao_inicial", insertable = false, updatable = false)
	public Integer getIdLocalizacaoInicial() {
		return idLocalizacaoInicial;
	}

	public void setIdLocalizacaoInicial(Integer idLocalizacaoInicial) {
		this.idLocalizacaoInicial = idLocalizacaoInicial;
	}

	@Column(name = "id_estrutura_inicial", insertable = false, updatable = false)
	public Integer getIdEstruturaInicial() {
		return idEstruturaInicial;
	}

	public void setIdEstruturaInicial(Integer idEstruturaInicial) {
		this.idEstruturaInicial = idEstruturaInicial;
	}

	@Column(name = "id_caixa", insertable = false, updatable = false)
	public Integer getIdCaixa() {
		return idCaixa;
	}

	public void setIdCaixa(Integer idCaixa) {
		this.idCaixa = idCaixa;
	}

	@Column(name = "id_usuario_cadastro_processo", insertable = false, updatable = false)
	public Integer getIdUsuarioCadastroProcesso() {
		return idUsuarioCadastroProcesso;
	}

	public void setIdUsuarioCadastroProcesso(Integer idUsuarioCadastroProcesso) {
		this.idUsuarioCadastroProcesso = idUsuarioCadastroProcesso;
	}
	
	@Column(name = "id_processo_assunto_principal", insertable = false, updatable = false)
	public Integer getIdProcessoAssuntoPrincipal() {
		return idProcessoAssuntoPrincipal;
	}

	public void setIdProcessoAssuntoPrincipal(Integer idProcessoAssuntoPrincipal) {
		this.idProcessoAssuntoPrincipal = idProcessoAssuntoPrincipal;
	}

	@Column(name = "id_assunto_principal", insertable = false, updatable = false)
	public Integer getIdAssuntoPrincipal() {
		return idAssuntoPrincipal;
	}

	public void setIdAssuntoPrincipal(Integer idAssuntoPrincipal) {
		this.idAssuntoPrincipal = idAssuntoPrincipal;
	}

	@Column(name = "cd_assunto_principal", insertable = false, updatable = false)
	public String getCodigoAssuntoPrincipal() {
		return codigoAssuntoPrincipal;
	}

	public void setCodigoAssuntoPrincipal(String codigoAssuntoPrincipal) {
		this.codigoAssuntoPrincipal = codigoAssuntoPrincipal;
	}

	@Column(name = "ds_assunto_principal", insertable = false, updatable = false)
	public String getAssuntoPrincipal() {
		return assuntoPrincipal;
	}

	public void setAssuntoPrincipal(String assuntoPrincipal) {
		this.assuntoPrincipal = assuntoPrincipal;
	}

	@Column(name = "id_classe_judicial", insertable = false, updatable = false)
	public Integer getIdClasseJudicial() {
		return idClasseJudicial;
	}

	public void setIdClasseJudicial(Integer idClasseJudicial) {
		this.idClasseJudicial = idClasseJudicial;
	}

	@Column(name = "cd_classe_judicial", insertable = false, updatable = false)
	public String getCodigoClasseJudicial() {
		return codigoClasseJudicial;
	}

	public void setCodigoClasseJudicial(String codigoClasseJudicial) {
		this.codigoClasseJudicial = codigoClasseJudicial;
	}

	@Column(name = "ds_classe_judicial_sigla", insertable = false, updatable = false)
	public String getSiglaClasseJudicial() {
		return siglaClasseJudicial;
	}

	public void setSiglaClasseJudicial(String siglaClasseJudicial) {
		this.siglaClasseJudicial = siglaClasseJudicial;
	}

	@Column(name = "ds_classe_judicial", insertable = false, updatable = false)
	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@Column(name = "id_jurisdicao", insertable = false, updatable = false)
	public Integer getIdJurisdicao() {
		return idJurisdicao;
	}

	public void setIdJurisdicao(Integer idJurisdicao) {
		this.idJurisdicao = idJurisdicao;
	}

	@Column(name = "ds_jurisdicao", insertable = false, updatable = false)
	public String getNomeJurisdicao() {
		return nomeJurisdicao;
	}

	public void setNomeJurisdicao(String nomeJurisdicao) {
		this.nomeJurisdicao = nomeJurisdicao;
	}

	@Column(name = "id_competencia", insertable = false, updatable = false)
	public Integer getIdCompetencia() {
		return idCompetencia;
	}

	public void setIdCompetencia(Integer idCompetencia) {
		this.idCompetencia = idCompetencia;
	}

	@Column(name = "ds_competencia", insertable = false, updatable = false)
	public String getCompetencia() {
		return competencia;
	}

	public void setCompetencia(String competencia) {
		this.competencia = competencia;
	}

	@Column(name = "dt_autuacao", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDataAutuacao() {
		return dataAutuacao;
	}

	public void setDataAutuacao(Date dataAutuacao) {
		this.dataAutuacao = dataAutuacao;
	}

	@Column(name = "id_pessoa_autor", insertable = false, updatable = false)
	public Integer getIdAutor() {
		return idAutor;
	}

	public void setIdAutor(Integer idAutor) {
		this.idAutor = idAutor;
	}

	@Column(name = "nm_pessoa_autor", insertable = false, updatable = false)
	public String getAutor() {
		if (autor != null) {
			return (qtAutor > 1 ? autor + E_OUTROS : autor);
		}
		return null;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	@Column(name = "qt_autor", insertable = false, updatable = false)
	public long getQtAutor() {
		return qtAutor;
	}

	public void setQtAutor(long qtAutor) {
		this.qtAutor = qtAutor;
	}

	@Column(name = "id_pessoa_reu", insertable = false, updatable = false)
	public Integer getIdReu() {
		return idReu;
	}

	public void setIdReu(Integer idReu) {
		this.idReu = idReu;
	}

	@Column(name = "nm_pessoa_reu", insertable = false, updatable = false)
	public String getReu() {
		if (reu != null) {
			return (qtReu > 1 ? reu + E_OUTROS : reu);
		}else{
			return "Não definido";
		}
	}

	public void setReu(String reu) {
		this.reu = reu;
	}

	@Column(name = "qt_reu", insertable = false, updatable = false)
	public long getQtReu() {
		return qtReu;
	}

	public void setQtReu(long qtReu) {
		this.qtReu = qtReu;
	}

	@Column(name = "id_ultimo_movimento", insertable = false, updatable = false)
	public Integer getIdUltimoMovimento() {
		return idUltimoMovimento;
	}

	public void setIdUltimoMovimento(Integer idUltimoMovimento) {
		this.idUltimoMovimento = idUltimoMovimento;
	}

	@Column(name = "ds_ultimo_movimento", insertable = false, updatable = false)
	public String getUltimoMovimento() {
		return ultimoMovimento;
	}

	public void setUltimoMovimento(String ultimoMovimento) {
		this.ultimoMovimento = ultimoMovimento;
	}

	@Column(name = "dt_ultimo_movimento", insertable = false, updatable = false)
	public Date getDataUltimoMovimento() {
		return dataUltimoMovimento;
	}

	public void setDataUltimoMovimento(Date dataUltimoMovimento) {
		this.dataUltimoMovimento = dataUltimoMovimento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", insertable = false, updatable = false)
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_classe_judicial", insertable = false, updatable = false)
	public ClasseJudicial getClasseJudicialObj() {
		return classeJudicialObj;
	}

	public void setClasseJudicialObj(ClasseJudicial classeJudicialObj) {
		this.classeJudicialObj = classeJudicialObj;
	}

	@Column(name = "cd_processo_status", length = 1, insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	public ProcessoStatusEnum getProcessoStatus() {
		return this.processoStatus;
	}

	public void setProcessoStatus(ProcessoStatusEnum processoStatus) {
		this.processoStatus = processoStatus;
	}

	@Column(name = "in_segredo_justica", insertable = false, updatable = false)
	public Boolean getSegredoJustica() {
		return this.segredoJustica;
	}

	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	@Column(name = "ds_observacao_segredo", length = 100, insertable = false, updatable = false)
	@Length(max = 100)
	public String getObservacaoSegredo() {
		return this.observacaoSegredo;
	}

	public void setObservacaoSegredo(String observacaoSegredo) {
		this.observacaoSegredo = observacaoSegredo;
	}

	@Column(name = "in_apreciado_segredo", insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	public ProcessoTrfApreciadoEnum getApreciadoSegredo() {
		return apreciadoSegredo;
	}

	public void setApreciadoSegredo(ProcessoTrfApreciadoEnum apreciadoSegredo) {
		this.apreciadoSegredo = apreciadoSegredo;
	}

	@Column(name = "in_apreciado_sigilo", insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	public ProcessoTrfApreciadoEnum getApreciadoSigilo() {
		return apreciadoSigilo;
	}

	public void setApreciadoSigilo(ProcessoTrfApreciadoEnum apreciadoSigilo) {
		this.apreciadoSigilo = apreciadoSigilo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_distribuicao", insertable = false, updatable = false)
	public Date getDataDistribuicao() {
		return dataDistribuicao;
	}

	public void setDataDistribuicao(Date dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_transitado_julgado", insertable = false, updatable = false)
	public Date getDtTransitadoJulgado() {
		return dtTransitadoJulgado;
	}

	public void setDtTransitadoJulgado(Date dtTransitadoJulgado) {
		this.dtTransitadoJulgado = dtTransitadoJulgado;
	}

	@Column(name = "in_justica_gratuita", insertable = false, updatable = false)
	public Boolean getJusticaGratuita() {
		return justicaGratuita;
	}

	public void setJusticaGratuita(Boolean justicaGratuita) {
		this.justicaGratuita = justicaGratuita;
	}

	@Column(name = "in_tutela_liminar", insertable = false, updatable = false)
	public Boolean getTutelaLiminar() {
		return tutelaLiminar;
	}

	public void setTutelaLiminar(Boolean tutelaLiminar) {
		this.tutelaLiminar = tutelaLiminar;
	}

	@Column(name = "in_prioridade", insertable = false, updatable = false)
	public Boolean getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(Boolean prioridade) {
		this.prioridade = prioridade;
	}

	@Column(name = "vl_peso_prioridade", insertable = false, updatable = false)
	public Integer getPesoPrioridade() {
		return pesoPrioridade;
	}

	public void setPesoPrioridade(Integer pesoPrioridade) {
		this.pesoPrioridade = pesoPrioridade;
	}

	@Transient
	public String getPrioridadesString() {
		StringBuilder s = new StringBuilder();
		for (ProcessoPrioridadeProcesso p : processoTrf.getProcessoPrioridadeProcessoList()) {
			s.append(p.getPrioridadeProcesso().getPrioridade() + "\n");
		}
		return s.toString();
	}

	@Transient
	public List<ProcessoPrioridadeProcesso> getPrioridadesList() {
		return processoTrf.getProcessoPrioridadeProcessoList();
	}

	@Column(name = "id_orgao_julgador_colegiado", insertable = false, updatable = false)
	public Integer getIdOrgaoJulgadorColegiado() {
		return idOrgaoJulgadorColegiado;
	}

	public void setIdOrgaoJulgadorColegiado(Integer idOrgaoJulgadorColegiado) {
		this.idOrgaoJulgadorColegiado = idOrgaoJulgadorColegiado;
	}

	/**
	 * Método que retorna o numero do processo com a sigla do cargo.
	 */
	@Transient
	@Deprecated
	public String getNumeroProcessoCargo() {
		return getNumeroProcesso();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ConsultaProcessoTrfSemFiltro)) {
			return false;
		}
		ConsultaProcessoTrfSemFiltro other = (ConsultaProcessoTrfSemFiltro) obj;
		if (getIdProcessoTrf() != other.getIdProcessoTrf()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoTrf();
		return result;
	}

	@Transient
	/**
	 *Retornar o valor do checkBox na página de Consulta Processo Não Protocolado  
	 */
	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}

	@Transient
	public String getNumeroProcessoComClasseJudicial() {
		return this.getNumeroProcesso() + " - " + this.getClasseJudicial();
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_solicitacao_inclusao_pauta", insertable = false, updatable = false)
	public Date getDtSolicitacaoInclusaoPauta() {
		return dtSolicitacaoInclusaoPauta;
	}

	public void setDtSolicitacaoInclusaoPauta(Date dtSolicitacaoInclusaoPauta) {
		this.dtSolicitacaoInclusaoPauta = dtSolicitacaoInclusaoPauta;
	}
	
	@Column(name = "in_inicial", length = 1, insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	public ClasseJudicialInicialEnum getInicial() {
		return this.inicial;
	}

	public void setInicial(ClasseJudicialInicialEnum inicial) {
		this.inicial = inicial;
	}

	@Column(name = "cd_nivel_acesso")
	@NotNull
	public int getNivelAcesso() {
		return nivelAcesso;
	}

	public void setNivelAcesso(int nivelAcesso) {
		this.nivelAcesso = nivelAcesso;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="tp_situacao_guia_recolhimento", nullable=true, length = 2)
	public SituacaoGuiaRecolhimentoEnum getSituacaoGuiaRecolhimento() {
		return situacaoGuiaRecolhimento;
	}

	public void setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum situacaoGuiaRecolhimento) {
		this.situacaoGuiaRecolhimento = situacaoGuiaRecolhimento;
	}
}
