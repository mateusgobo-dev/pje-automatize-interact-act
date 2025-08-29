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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;

@Entity
@Table(name = "tb_processo_expediente")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_expediente", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_expediente"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoExpediente implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoExpediente,Integer>{

	private static final long serialVersionUID = 1L;

	private int idProcessoExpediente;
	private ProcessoTrf processoTrf;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private Date dtCriacao;
	private Date dtExclusao;
	private ExpedicaoExpedienteEnum meioExpedicaoExpediente = ExpedicaoExpedienteEnum.E;
	private Boolean urgencia = Boolean.FALSE;
	private Boolean checkado;
	private ProcessoDocumento processoDocumentoVinculadoExpediente;
	private Boolean documentoExistente = Boolean.FALSE;
	private Boolean inTemporario;
	private Sessao sessao;
	private SessaoJT sessaoJT;
	private ProcessoDocumento processoDocumento;
	private OrgaoJulgador orgaoJulgador;
	private List<PessoaExpediente> pessoaExpedienteList = new ArrayList<PessoaExpediente>(0);
	private List<ProcessoExpedienteDiligencia> processoExpedienteDiligenciaList = new ArrayList<ProcessoExpedienteDiligencia>(0);
	private List<ProcessoDocumentoExpediente> processoDocumentoExpedienteList = new ArrayList<ProcessoDocumentoExpediente>(0);
	private List<ProcessoParteExpediente> processoParteExpedienteList = new ArrayList<ProcessoParteExpediente>(0);
	private List<ProcessoExpedienteCentralMandado> processoExpedienteCentralMandadoList = new ArrayList<ProcessoExpedienteCentralMandado>(0);

	public ProcessoExpediente(){
	}

	@Id
	@GeneratedValue(generator = "gen_processo_expediente")
	@Column(name = "id_processo_expediente", unique = true, nullable = false)
	public int getIdProcessoExpediente(){
		return this.idProcessoExpediente;
	}

	public void setIdProcessoExpediente(int idProcessoExpediente){
		this.idProcessoExpediente = idProcessoExpediente;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf(){
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf){
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_processo_documento")
	public TipoProcessoDocumento getTipoProcessoDocumento(){
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento){
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao_expediente")
	public Date getDtCriacao(){
		return dtCriacao;
	}

	public void setDtCriacao(Date dtCriacao){
		this.dtCriacao = dtCriacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_exclusao")
	public Date getDtExclusao(){
		return dtExclusao;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processoExpediente")
	public List<PessoaExpediente> getPessoaExpedienteList(){
		return pessoaExpedienteList;
	}

	public void setPessoaExpedienteList(List<PessoaExpediente> pessoaExpedienteList){
		this.pessoaExpedienteList = pessoaExpedienteList;
	}

	public void setDtExclusao(Date dtExclusao){
		this.dtExclusao = dtExclusao;
	}

	@Column(name = "in_meio_expedicao_expediente", length = 1)
	@Enumerated(EnumType.STRING)
	public ExpedicaoExpedienteEnum getMeioExpedicaoExpediente(){
		return meioExpedicaoExpediente;
	}

	public void setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum meioExpedicaoExpediente){
		this.meioExpedicaoExpediente = meioExpedicaoExpediente;
	}

	@Column(name = "in_urgencia")
	public Boolean getUrgencia(){
		return this.urgencia;
	}

	public void setUrgencia(Boolean urgencia){
		this.urgencia = urgencia;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_documento")
	public ProcessoDocumento getProcessoDocumento(){
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento){
		this.processoDocumento = processoDocumento;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}
	
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processoExpediente")
	public List<ProcessoExpedienteDiligencia> getProcessoExpedienteDiligenciaList(){
		return this.processoExpedienteDiligenciaList;
	}

	public void setProcessoExpedienteDiligenciaList(List<ProcessoExpedienteDiligencia> processoExpedienteDiligenciaList){
		this.processoExpedienteDiligenciaList = processoExpedienteDiligenciaList;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processoExpediente")
	public List<ProcessoDocumentoExpediente> getProcessoDocumentoExpedienteList(){
		return this.processoDocumentoExpedienteList;
	}

	public void setProcessoDocumentoExpedienteList(List<ProcessoDocumentoExpediente> processoDocumentoExpedienteList){
		this.processoDocumentoExpedienteList = processoDocumentoExpedienteList;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processoExpediente")
	public List<ProcessoParteExpediente> getProcessoParteExpedienteList(){
		return this.processoParteExpedienteList;
	}

	public void setProcessoParteExpedienteList(List<ProcessoParteExpediente> processoParteExpedienteList){
		this.processoParteExpedienteList = processoParteExpedienteList;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processoExpediente")
	public List<ProcessoExpedienteCentralMandado> getProcessoExpedienteCentralMandadoList(){
		return this.processoExpedienteCentralMandadoList;
	}

	public void setProcessoExpedienteCentralMandadoList(
			List<ProcessoExpedienteCentralMandado> processoExpedienteCentralMandadoList){
		this.processoExpedienteCentralMandadoList = processoExpedienteCentralMandadoList;
	}

	/**
	 * @return the documentoExistente
	 */
	@Column(name = "in_documento_existente", nullable = false)
	@NotNull
	public Boolean getDocumentoExistente(){
		return documentoExistente;
	}

	/**
	 * @param documentoExistente the documentoExistente to set
	 */
	public void setDocumentoExistente(Boolean documentoExistente){
		this.documentoExistente = documentoExistente;
	}

	@Transient
	public Boolean getCheckado(){
		return checkado;
	}

	public void setCheckado(Boolean checkado){
		this.checkado = checkado;
	}

	@Column(name = "in_temporario", nullable = false)
	@NotNull
	public Boolean getInTemporario(){
		return this.inTemporario;
	}

	public void setInTemporario(Boolean inTemporario){
		this.inTemporario = inTemporario;
	}

	public void setSessao(Sessao sessao){
		this.sessao = sessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao")
	public Sessao getSessao(){
		return sessao;
	}

	public void setSessaoJT(SessaoJT sessaoJT){
		this.sessaoJT = sessaoJT;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao_jt")
	public SessaoJT getSessaoJT(){
		return sessaoJT;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (!(obj instanceof ProcessoExpediente)){
			return false;
		}
		ProcessoExpediente other = (ProcessoExpediente) obj;
		if (getIdProcessoExpediente() != other.getIdProcessoExpediente()){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoExpediente();
		return result;
	}
	
	/**
	 * [PJEII-1253] Método que retorna os endereços das partes do expediente
	 * [PJEII-1372] Correção: colocando teste se o endereço de correspondencia vem nulo.
	 * [PJEII-1600] Correção no retorno do método para evitar acessos fora de indice (erros) do array quando o endereço tiver comprimento menor que 2
	 * @return endereços das partes
	 */
	@Transient
	public String getEnderecosProcessoExpediente() {
		StringBuilder enderecos = new StringBuilder("");
		
		for (ProcessoParteExpediente processoParteExpediente : this.getProcessoParteExpedienteList()) {
			for (Endereco endereco : processoParteExpediente.getEnderecos()) {
				if (endereco.getCorrespondencia() != null && endereco.getCorrespondencia()) {
					return endereco.getEnderecoCompleto();
				}
				enderecos.append("\n\n");
				enderecos.append(endereco.getEnderecoCompleto());
			}
		}
		
		String enderecosStr = enderecos.toString();
		
		return enderecosStr.length() > 2 ?  enderecosStr.substring(2) : enderecosStr;
	}
	
	/**
	 * [PJEII-1794] PJE-JT: Ronny Paterson : PJE-1.4.4
	 * Método que quebra a lista de destinatários do expediente separados pela 
	 * tag <br/> do HTML. Caso seja utilizado em um componente wi:dataTable, é
	 * necessário utilizá-lo em uma wi:columnOutputText com atributo escape
	 * setado para false.
	 * @return endereços das partes
	 */
	@Transient
	public String getListEnderecosProcessoExpediente() {
		StringBuilder enderecos = new StringBuilder("");
		
		for (ProcessoParteExpediente processoParteExpediente : this.getProcessoParteExpedienteList()) {
			boolean primeiraLinha = true;
			for (Endereco endereco : processoParteExpediente.getEnderecos()) {
				if (endereco.getCorrespondencia() != null && endereco.getCorrespondencia()) {
					return endereco.getEnderecoCompleto();
				}
				if(!primeiraLinha){
					enderecos.append("<br/><br/>");
				}
				enderecos.append(endereco.getEnderecoCompleto());
				primeiraLinha = false;
			}
		}
		
		String enderecosStr = enderecos.toString();
		
		return enderecosStr;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_documento_vinculado")
	public ProcessoDocumento getProcessoDocumentoVinculadoExpediente(){
		return processoDocumentoVinculadoExpediente;
	}

	public void setProcessoDocumentoVinculadoExpediente(ProcessoDocumento processoDocumentoVinculadoExpediente){
		this.processoDocumentoVinculadoExpediente = processoDocumentoVinculadoExpediente;
	}


	/**
	 * Retorna a data de ciência mais tarde.
	 * @return
	 */
	@Transient
	public ProcessoParteExpediente getUltimoQueTomouCiencia() {
		ProcessoParteExpediente ultimoPPE = null;
		
		if(processoParteExpedienteList != null) {
			for(ProcessoParteExpediente ppe : processoParteExpedienteList) {
				if(ultimoPPE == null && ppe.getDtCienciaParte() != null) {
					ultimoPPE = ppe;
				}
				
				if(ppe.getDtCienciaParte() != null && ppe.getDtCienciaParte().after(ultimoPPE.getDtCienciaParte())) {
					ultimoPPE = ppe;
				}
			}
		}
		
		return ultimoPPE;
	}
	
	/**
	 * Retorna o objeto com o prazo legal mais tarde.
	 * @return
	 */
	@Transient
	public ProcessoParteExpediente getUltimoPrazoLegal() {
		ProcessoParteExpediente ultimoPPE = null;
		
		if(processoParteExpedienteList != null) {
			for(ProcessoParteExpediente ppe : processoParteExpedienteList) {
				if(ultimoPPE == null && ppe.getDtPrazoLegal() != null) {
					ultimoPPE = ppe;
				}
				
				if(ppe.getDtPrazoLegal() != null && ppe.getDtPrazoLegal().after(ultimoPPE.getDtPrazoLegal())) {
					ultimoPPE = ppe;
				}
			}
		}
		
		return ultimoPPE;
	}
	
	@Transient
	public String getPrimeiroEndereco(String tipoOrdenacaoEndereco) {
		if (getProcessoParteExpedienteList() != null && getProcessoParteExpedienteList().size() > 0) {
			ProcessoParteExpediente ppe = getProcessoParteExpedienteList().get(0);
			
			return ppe.getOrdenacao(tipoOrdenacaoEndereco);
		} 
		return null;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoExpediente> getEntityClass() {
		return ProcessoExpediente.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoExpediente());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
