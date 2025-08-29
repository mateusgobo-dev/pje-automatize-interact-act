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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.filters.ProcessoParteExpedienteFilter;
import br.jus.pje.nucleo.enums.TipoCalculoMeioComunicacaoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;

@Entity
@Table(name = "tb_proc_parte_expediente")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_parte_expediente", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_parte_expediente"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@FilterDefs(value = {		
	@FilterDef(name = ProcessoParteExpedienteFilter.FILTER_ORGAO_JULGADOR_CARGO, 
		parameters = {			
			@ParamDef(type = ProcessoParteExpedienteFilter.TYPE_INT, name="idUsuarioLocalizacao"),
			@ParamDef(type = ProcessoParteExpedienteFilter.TYPE_DATE, name="dataAtual")
		})	
	})
@Filters(value = {		
	@Filter(name = ProcessoParteExpedienteFilter.FILTER_ORGAO_JULGADOR_CARGO, condition = ProcessoParteExpedienteFilter.CONDITION_ORGAO_JULGADOR_CARGO)
})
public class ProcessoParteExpediente implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoParteExpediente,Integer>{

	private static final long serialVersionUID = 1L;

	private int idProcessoParteExpediente;
	private ProcessoExpediente processoExpediente;
	private Pessoa pessoaParte;
	private ProcessoTrf processoJudicial;
	private ConsultaProcessoTrfSemFiltro cabecalhoProcesso;
	private Pessoa pessoaCiencia;
	private Integer prazoLegal;
	private Integer prazoProcessual;
	private Date dtPrazoLegal;
	private Date dtPrazoProcessual;
	private Date dtCienciaParte;
	private Boolean cienciaSistema;
	private Boolean check = Boolean.FALSE;
	private String nomePessoaParte;
	private String nomePessoaCiencia;
	private Boolean pendenteManifestacao = Boolean.FALSE;
	private RespostaExpediente resposta;
	private TipoPrazoEnum tipoPrazo = TipoPrazoEnum.D;
	private String pendencia;
	private Boolean fechado = Boolean.FALSE;
	private Boolean enviadoCancelamento = Boolean.FALSE;
	private Boolean cancelado = Boolean.FALSE;
	private Procuradoria procuradoria;
	private TipoCalculoMeioComunicacaoEnum tipoCalculoMeioComunicacao;

	private List<ProcessoParteExpedienteVisita> processoParteExpedienteVisitaList = new ArrayList<ProcessoParteExpedienteVisita>(0);
	private List<ProcessoParteExpedienteEndereco> processoParteExpedienteEnderecoList = new ArrayList<ProcessoParteExpedienteEndereco>(0) ;
	private List<RegistroIntimacao> registroIntimacaoList = new ArrayList<RegistroIntimacao>(0);
	private List<PublicacaoDiarioEletronico> publicacaoDiarioEletronicoList = new ArrayList<PublicacaoDiarioEletronico>(0);
	private List<ProcessoParteExpedienteDestinatario> destinatarioList = new ArrayList<ProcessoParteExpedienteDestinatario>(0);
	private List<ProcessoParteExpedienteCaixaAdvogadoProcurador> caixasRepresentantes =new ArrayList<ProcessoParteExpedienteCaixaAdvogadoProcurador>(0);
	private Boolean intimacaoPessoal = Boolean.FALSE;
	private Boolean destaque = Boolean.TRUE;
	private Integer idProcessoJudicial;
	private Pessoa pessoaEncerramentoManual;
	private Date dtEncerramentoManual;	
	private Boolean enviadoDomicilio = Boolean.FALSE;

	// Utilizada na unificacao e desunificacao de pessoas.
	private List<ProcessoParteExpedienteHistorico> processoParteExpedienteHistoricoList = new ArrayList<ProcessoParteExpedienteHistorico>(0);

	public ProcessoParteExpediente(){
	}

	@Id
	@GeneratedValue(generator = "gen_proc_parte_expediente")
	@Column(name = "id_processo_parte_expediente", unique = true, nullable = false)
	public int getIdProcessoParteExpediente(){
		return this.idProcessoParteExpediente;
	}

	public void setIdProcessoParteExpediente(int idProcessoParteExpediente){
		this.idProcessoParteExpediente = idProcessoParteExpediente;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_expediente", nullable = false)
	@NotNull
	public ProcessoExpediente getProcessoExpediente(){
		return this.processoExpediente;
	}

	public void setProcessoExpediente(ProcessoExpediente processoExpediente){
		this.processoExpediente = processoExpediente;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_parte", nullable = false)
	@NotNull
	public Pessoa getPessoaParte(){
		return this.pessoaParte;
	}

	public void setPessoaParte(Pessoa pessoaParte){
		this.pessoaParte = pessoaParte;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaParte(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaParte(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaParte(pessoa.getPessoa());
		} else {
			setPessoaParte((Pessoa)null);
		}
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_ciencia")
	public Pessoa getPessoaCiencia(){
		return pessoaCiencia;
	}

	public void setPessoaCiencia(Pessoa pessoaCiencia){
		this.pessoaCiencia = pessoaCiencia;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaCiencia(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaCiencia(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaCiencia(pessoa.getPessoa());
		} else {
			setPessoaCiencia((Pessoa)null);
		}
	}

	/**
	 * Obtém o tipo de prazo processual em análise.
	 * 
	 * @return o tipo do prazo, conforme domínio {@link TipoPrazoEnum}.
	 */
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "in_tipo_prazo", nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull
	public TipoPrazoEnum getTipoPrazo(){
		return tipoPrazo;
	}

	/**
	 * @param tipoPrazo the tipoPrazo to set
	 */
	public void setTipoPrazo(TipoPrazoEnum tipoPrazo){
		this.tipoPrazo = tipoPrazo;
	}

	@Column(name = "qt_prazo_legal_parte")
	public Integer getPrazoLegal(){
		return prazoLegal;
	}

	public void setPrazoLegal(Integer prazoLegal){
		this.prazoLegal = prazoLegal;
	}

	@Column(name = "qt_prazo_processual_parte")
	public Integer getPrazoProcessual(){
		return prazoProcessual;
	}

	public void setPrazoProcessual(Integer prazoProcessual){
		this.prazoProcessual = prazoProcessual;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_prazo_legal_parte")
	public Date getDtPrazoLegal(){
		return dtPrazoLegal;
	}

	public void setDtPrazoLegal(Date dtPrazoLegal){
		this.dtPrazoLegal = dtPrazoLegal;
	}
	
	/**
	 * PJE-JT: Ricardo Scholz : PJEII-3210 - 2012-11-07 Alteracoes feitas pela JT.
	 * Criação de método transiente para retornar o conteúdo da variável
	 * 'dtPrazoLegal', testando quando ela está guardando o valor do prazo legal
	 * ou o valor do prazo de graça. Quando o valor do prazo de graça estiver sendo
	 * guardado nesta variável, o retorno deverá ser nulo.
	 * Utilização nas seguintes grids, para evitar mostrar o prazo de graça nas 
	 * respectivas colunas:
	 * --------------------------------------------------------------------------
	 *   GRID (*.component.xml)					       | COLUNA
	 * ----------------------------------------------------------------------
	 *   processoParteExpedienteMenuGrid               | 'Fim do Prazo Legal'
	 *   intimacaoTrfInicialAdvogadoGrid			   | 'Prazo Final'
	 * ----------------------------------------------------------------------
	 */
	@Transient
	public Date getDtPrazoLegalNullSePrazoGraca(){
		if ((this.cienciaSistema != null && this.cienciaSistema)
				|| this.pessoaCiencia != null) {
			return getDtPrazoLegal();
		}
		return null;
	}

	/**
	 * Recupera a data em que o expediente foi disponibilizado para ciência pelo intimado.
	 * 
	 * @return data de disponibilização do expediente para o intimado
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_prazo_processual_parte")
	public Date getDtPrazoProcessual(){
		return dtPrazoProcessual;
	}

	/**
	 * Atribui a este expediente a data de sua disponibilização para ciência pelo intimado.
	 * 
	 * @param dtPrazoProcessual a data a ser atribuída
	 */
	public void setDtPrazoProcessual(Date dtPrazoProcessual){
		this.dtPrazoProcessual = dtPrazoProcessual;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ciencia_parte")
	public Date getDtCienciaParte(){
		return dtCienciaParte;
	}

	public void setDtCienciaParte(Date dtCienciaParte){
		this.dtCienciaParte = dtCienciaParte;
	}

	@Column(name = "in_ciencia_sistema")
	public Boolean getCienciaSistema(){
		return this.cienciaSistema;
	}

	public void setCienciaSistema(Boolean cienciaSistema){
		this.cienciaSistema = cienciaSistema;
	}

	@Transient
	public Date getDataDisponibilizacao(){
		if (this.processoExpediente != null){
			return this.processoExpediente.getDtCriacao();
		}
		return null;
	}

	@Transient
	public Boolean getCheck(){
		return check;
	}

	public void setCheck(Boolean check){
		this.check = check;
	}

	@Override
	public String toString(){
		if (pessoaParte != null)
			return pessoaParte.toString();
		else
			return nomePessoaParte;
	}

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, mappedBy = "processoParteExpediente")
	public List<ProcessoParteExpedienteVisita> getProcessoParteExpedienteVisitaList(){
		return this.processoParteExpedienteVisitaList;
	}

	public void setProcessoParteExpedienteVisitaList(
			List<ProcessoParteExpedienteVisita> processoParteExpedienteVisitaList){
		this.processoParteExpedienteVisitaList = processoParteExpedienteVisitaList;
	}
	
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, mappedBy = "processoParteExpediente")
	public List<ProcessoParteExpedienteHistorico> getProcessoParteExpedienteHistoricoList() {
		return processoParteExpedienteHistoricoList;
	}
   
	public void setProcessoParteExpedienteHistoricoList(List<ProcessoParteExpedienteHistorico> processoParteExpedienteHistoricoList) {
		this.processoParteExpedienteHistoricoList = processoParteExpedienteHistoricoList;
	}

	/**
	 * @return the processoJudicial
	 */
	@ManyToOne
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoJudicial(){
		return processoJudicial;
	}

	/**
	 * @param processoJudicial the processoJudicial to set
	 */
	public void setProcessoJudicial(ProcessoTrf processoJudicial){
		this.processoJudicial = processoJudicial;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", insertable = false, updatable = false)
	public ConsultaProcessoTrfSemFiltro getCabecalhoProcesso() {
		return cabecalhoProcesso;
	}
	
	public void setCabecalhoProcesso(ConsultaProcessoTrfSemFiltro cabecalhoProcesso) {
		this.cabecalhoProcesso = cabecalhoProcesso;
	}

	@Column(name = "nm_pessoa_parte", length = 255)
	@Length(max = 255)
	public String getNomePessoaParte(){
		if (pessoaParte != null)
			return pessoaParte.getNome();
		else
			return nomePessoaParte;
	}

	public void setNomePessoaParte(String nomePessoaParte){
		this.nomePessoaParte = nomePessoaParte;
	}

	@Column(name = "nm_pessoa_ciencia", length = 255)
	@Length(max = 255)
	public String getNomePessoaCiencia(){
		if (pessoaCiencia != null)
			return pessoaCiencia.getNome();
		else
			return nomePessoaCiencia;
	}

	public void setNomePessoaCiencia(String nomePessoaCiencia){
		this.nomePessoaCiencia = nomePessoaCiencia;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "processoParteExpediente")
	public List<RegistroIntimacao> getRegistroIntimacaoList(){
		return this.registroIntimacaoList;
	}

	public void setRegistroIntimacaoList(List<RegistroIntimacao> registroIntimacaoList){
		this.registroIntimacaoList = registroIntimacaoList;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "processoParteExpediente")
	public List<PublicacaoDiarioEletronico> getPublicacaoDiarioEletronicoList(){
		return this.publicacaoDiarioEletronicoList;
	}

	public void setPublicacaoDiarioEletronicoList(List<PublicacaoDiarioEletronico> publicacaoDiarioEletronicoList){
		this.publicacaoDiarioEletronicoList = publicacaoDiarioEletronicoList;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "processoParteExpediente")
	public List<ProcessoParteExpedienteDestinatario> getDestinatarioList(){
		return this.destinatarioList;
	}

	public void setDestinatarioList(List<ProcessoParteExpedienteDestinatario> destinatarioList){
		this.destinatarioList = destinatarioList;
	}

	@Transient
	public ProcessoDocumento getProcessoDocumento(){
		if (this.processoExpediente != null) {
			List<ProcessoDocumentoExpediente> documentoExpedienteList = getProcessoExpediente()
					.getProcessoDocumentoExpedienteList();
			for (ProcessoDocumentoExpediente processoDocumentoExpediente : documentoExpedienteList){
				if (processoDocumentoExpediente.getAnexo() != null && !processoDocumentoExpediente.getAnexo()){
					return processoDocumentoExpediente.getProcessoDocumento();
				}
			}
		}
		return null;
	}

	@Column(name = "in_pendente_manifestacao")
	@NotNull
	public Boolean getPendenteManifestacao(){
		return this.pendenteManifestacao;
	}

	public void setPendenteManifestacao(Boolean pendenteManifestacao){
		this.pendenteManifestacao = pendenteManifestacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_resposta")
	public RespostaExpediente getResposta(){
		return resposta;
	}

	public void setResposta(RespostaExpediente resposta){
		this.resposta = resposta;
	}

	@Transient
	public Boolean getRespostaIntempestiva(){
		if (getDtPrazoLegal() == null || getResposta() == null){
			return Boolean.FALSE;
		}

		return getResposta().getData().after(getDtPrazoLegal());
	}

	@Column(name = "in_fechado", length = 1, nullable = false)
	@NotNull
	public Boolean getFechado(){
		return fechado;
	}

	public void setFechado(Boolean fechado){
		this.fechado = fechado;
	}

	@Column(name = "ds_pendencia", length = 200)
	@Length(max = 200)
	public String getPendencia(){
		return pendencia;
	}

	public void setPendencia(String pendencia){
		this.pendencia = pendencia;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy="processoParteExpediente")
	public List<ProcessoParteExpedienteCaixaAdvogadoProcurador> getCaixasRepresentantes() {
		return caixasRepresentantes;
	}
	
	public void setCaixasRepresentantes(
			List<ProcessoParteExpedienteCaixaAdvogadoProcurador> caixasRepresentantes) {
		this.caixasRepresentantes = caixasRepresentantes;
	}
	
	/**
	 * Retorno de informações em lista para objetos
	 * 
	 * @return
	 */
	@Transient
	public String getEnderecoCorrespondencia() {
		if (getProcessoParteExpedienteEnderecoList() != null) {
			StringBuffer retorno = new StringBuffer();
			boolean appendConector = false;
			for(ProcessoParteExpedienteEndereco aux : getProcessoParteExpedienteEnderecoList()){
				if(appendConector){
					retorno.append("; ");
				}
				retorno.append(aux.getEndereco());
			}
			return retorno.toString();
		}
		return "";
	}
	
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, mappedBy = "processoParteExpediente")
	public List<ProcessoParteExpedienteEndereco> getProcessoParteExpedienteEnderecoList() {
		return processoParteExpedienteEnderecoList;
	}
	
	public void setProcessoParteExpedienteEnderecoList(
			List<ProcessoParteExpedienteEndereco> processoParteExpedienteEnderecoList) {
		this.processoParteExpedienteEnderecoList = processoParteExpedienteEnderecoList;
	}
	
	@Transient
	public List<Endereco> getEnderecos(){
		List<Endereco> result = new ArrayList<Endereco>(0);
		for(ProcessoParteExpedienteEndereco ppee : getProcessoParteExpedienteEnderecoList()){
			result.add(ppee.getEndereco());
		}
		
		return result;
	}
	
	@Transient
	public boolean isExisteAr(){
		for(ProcessoParteExpedienteEndereco ppee : getProcessoParteExpedienteEnderecoList()){
			if(ppee.getNumeroAr() != null){
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (!(obj instanceof ProcessoParteExpediente)){
			return false;
		}
		ProcessoParteExpediente other = (ProcessoParteExpediente) obj;
		if (getIdProcessoParteExpediente() != other.getIdProcessoParteExpediente()){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoParteExpediente();
		return result;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_procuradoria")
	public Procuradoria getProcuradoria() {
		return procuradoria;
	}
	
	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}
	
	@Column(name = "cod_tipo_calc_meio_comunicacao", length = 3)
	@Enumerated(EnumType.STRING)
	public TipoCalculoMeioComunicacaoEnum getTipoCalculoMeioComunicacao() {
		return tipoCalculoMeioComunicacao;
	}

	public void setTipoCalculoMeioComunicacao(TipoCalculoMeioComunicacaoEnum tipoCalculoMeioComunicacao) {
		this.tipoCalculoMeioComunicacao = tipoCalculoMeioComunicacao;
	}
	
	@Transient
	public String getOrdenacao(String tipoOrdenacaoEndereco) {
		
		Endereco primeiroEndereco = getEnderecos().get(0);
		if(tipoOrdenacaoEndereco == null)
			tipoOrdenacaoEndereco = "Cep";
		if (getEnderecos() != null && getEnderecos().size()>0) {
			if (tipoOrdenacaoEndereco.equals("Cep")) {
				return primeiroEndereco.getCep().getNumeroCep();
			} else if (tipoOrdenacaoEndereco.equals("Bairro")) {
				return primeiroEndereco.getNomeBairro();
			} else if (tipoOrdenacaoEndereco.equals("Cidade")) {
				return primeiroEndereco.getCep().getMunicipio().getMunicipio(); 
			} else if (tipoOrdenacaoEndereco.equals("Logradouro-Numero")) {
				return primeiroEndereco.getNomeLogradouro() +"-"+ primeiroEndereco.getNumeroEndereco() ;
			}
		}
		return null;
	}

	@Column(name = "in_intima_pessoal", length = 1, nullable = false)
	@NotNull
	public Boolean getIntimacaoPessoal() {
		return intimacaoPessoal;
	}

	public void setIntimacaoPessoal(Boolean intimacaoPessoal) {
		this.intimacaoPessoal = intimacaoPessoal;
	}

	@Column(name = "in_destaque", nullable = false)
	public Boolean getDestaque() {
		return destaque;
	}

	public void setDestaque(Boolean destaque) {
		this.destaque = destaque;
	}
	
	@Column(name = "id_processo_trf", insertable=false, updatable=false)
	public Integer getIdProcessoJudicial(){
		return this.idProcessoJudicial;
	}

	public void setIdProcessoJudicial(Integer idProcessoJudicial) {
		this.idProcessoJudicial = idProcessoJudicial;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoParteExpediente> getEntityClass() {
		return ProcessoParteExpediente.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoParteExpediente());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_encerrado_manualmente")
	public Pessoa getPessoaEncerramentoManual(){
		return pessoaEncerramentoManual;
	}

	public void setPessoaEncerramentoManual(Pessoa pessoaEncerramentoManual){
		this.pessoaEncerramentoManual = pessoaEncerramentoManual;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_encerrado_manualmente")
	public Date getDtEncerramentoManual() {
		return dtEncerramentoManual;
	}

	public void setDtEncerramentoManual(Date dtEncerramentoManual) {
		this.dtEncerramentoManual = dtEncerramentoManual;
	}

	@Column(name = "in_enviado_cancelamento", length = 1, nullable = false)
	@NotNull
	public Boolean getEnviadoCancelamento() {
		return enviadoCancelamento;
	}

	public void setEnviadoCancelamento(Boolean enviadoCancelamento) {
		this.enviadoCancelamento = enviadoCancelamento;
	}

	@Column(name = "in_cancelado", length = 1, nullable = false)
	@NotNull
	public Boolean getCancelado() {
		return cancelado;
	}

	public void setCancelado(Boolean cancelado) {
		this.cancelado = cancelado;
	}

	@Column(name = "in_enviado_domicilio", length = 1, nullable = false)
	@NotNull
	public Boolean isEnviadoDomicilio(){
		if (enviadoDomicilio == null) {
			enviadoDomicilio = Boolean.FALSE;
		}
		return enviadoDomicilio;
	}

	public void setEnviadoDomicilio(Boolean enviadoDomicilio){
		this.enviadoDomicilio = enviadoDomicilio;
	}
	
	/**
	 * Classe estática com as constantes dos atributos/métodos da classe.
	 *
	 */
	public static final class ATTR {
		
		/**
		 * Contrutor
		 * 
		 */
		private ATTR() {
			// Construtor.
		}
		
		public static final String PROCESSO_JUDICIAL = "processoJudicial";
	}
}
