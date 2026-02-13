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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Entity
@Table(name = Pessoa.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_pessoa")
@IndexedEntity(id="idPessoa", value="pessoa", owners={"processoParteList"},
	mappings={
		@Mapping(beanPath="nome", mappedPath="nome"),
		@Mapping(beanPath="pessoaNomeAlternativoList", mappedPath="outrosnomes"),
		@Mapping(beanPath="pessoaDocumentoIdentificacaoList", mappedPath="documentosidentificacao")
})
@Cacheable
public class Pessoa extends Usuario implements java.io.Serializable{

	public static final String TABLE_NAME = "tb_pessoa";
	private static final long serialVersionUID = 1L;
	
	private Integer idPessoa;
	private TipoPessoa tipoPessoa;
	private TipoPessoaEnum inTipoPessoa = TipoPessoaEnum.F;
	private boolean unificada = Boolean.FALSE;
	private Boolean atraiCompetencia = Boolean.FALSE;
	private Boolean classificado = Boolean.FALSE;
	private Boolean pessoaIndividualizada = Boolean.TRUE;
	private Boolean estrangeiro = Boolean.FALSE;
	private OrgaoJulgador orgaoJulgadorInclusao;
	private List<PessoaProcuradoriaEntidade> pessoaProcuradoriaEntidadeList = new ArrayList<PessoaProcuradoriaEntidade>(0);
	private List<PessoaQualificacao> pessoaQualificacaoList = new ArrayList<PessoaQualificacao>(0);
	private List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>(0);
	private List<ProcessoAudienciaPessoa> processoAudienciaPessoaList = new ArrayList<ProcessoAudienciaPessoa>(0);
	private List<DocumentoPessoa> documentoPessoaList = new ArrayList<DocumentoPessoa>(0);
	private List<MeioContato> meioContatoList = new ArrayList<MeioContato>(0);
	private List<PessoaNomeAlternativo> pessoaNomeAlternativoList = new ArrayList<PessoaNomeAlternativo>(0);
	private Set<PessoaDocumentoIdentificacao> pessoaDocumentoIdentificacaoList = new HashSet<PessoaDocumentoIdentificacao>();
	private List<RelacaoPessoal> relacaoPessoalList = new ArrayList<RelacaoPessoal>();
	private String ultimoPainelSelecionado = "";
	
	private transient List<NomePessoa> nomesPessoa = new ArrayList<>();
	
	private Boolean selDocumentoAtivoInativo = Boolean.FALSE;
	
	public Pessoa(){}
	
	@Basic(optional=true)
	@Column(name="id_pessoa", insertable=false, updatable=false)
	public Integer getIdPessoa() {
		return idPessoa;
	}
	
	public void setIdPessoa(Integer idPessoa) {
		this.idPessoa = idPessoa;
	}

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_pessoa")
	public TipoPessoa getTipoPessoa(){
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa){
		this.tipoPessoa = tipoPessoa;
	}

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, mappedBy = "pessoa")
	public List<PessoaQualificacao> getPessoaQualificacaoList(){
		return this.pessoaQualificacaoList;
	}

	public void setPessoaQualificacaoList(List<PessoaQualificacao> pessoaQualificacaoList){
		this.pessoaQualificacaoList = pessoaQualificacaoList;
	}

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, mappedBy = "pessoa")
	public List<ProcessoParte> getProcessoParteList(){
		return this.processoParteList;
	}

	public void setProcessoParteList(List<ProcessoParte> processoParteList){
		this.processoParteList = processoParteList;
	}

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, mappedBy = "pessoa")
	public List<ProcessoAudienciaPessoa> getProcessoAudienciaPessoaList(){
		return this.processoAudienciaPessoaList;
	}

	public void setProcessoAudienciaPessoaList(List<ProcessoAudienciaPessoa> processoAudienciaPessoaList){
		this.processoAudienciaPessoaList = processoAudienciaPessoaList;
	}

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, mappedBy = "pessoa")
	@OrderBy(value = "dataInclusao")
	public List<DocumentoPessoa> getDocumentoPessoaList(){
		return documentoPessoaList;
	}

	public void setDocumentoPessoaList(List<DocumentoPessoa> documentoPessoaList){
		this.documentoPessoaList = documentoPessoaList;
	}

	@Transient
	public List<Integer> getListIdProcesso(){
		Map<ProcessoTrf, Integer> processoTrfMap = new HashMap<ProcessoTrf, Integer>();
		List<ProcessoParte> processoParteList = this.getProcessoParteList();
		List<Integer> processosList = new ArrayList<Integer>();
		for (ProcessoParte processoParte : processoParteList){
			processoTrfMap.put(processoParte.getProcessoTrf(), processoParte.getProcessoTrf().getIdProcessoTrf());
			processosList.add(processoParte.getProcessoTrf().getIdProcessoTrf());
		}
		return processosList;
	}

	@Column(name = "in_tipo_pessoa", length = 1)
	@Enumerated(EnumType.STRING)
	public TipoPessoaEnum getInTipoPessoa(){
		return this.inTipoPessoa;
	}

	public void setInTipoPessoa(TipoPessoaEnum inTipoPessoa){
		this.inTipoPessoa = inTipoPessoa;
	}
	
	@Column(name = "in_unificado")
	public boolean getUnificada(){
		return unificada;
	}

	public void setUnificada(boolean unificado){
		this.unificada = unificado;
	}

	@Column(name = "in_atrai_competencia")
	public Boolean getAtraiCompetencia(){
		return atraiCompetencia;
	}

	public void setAtraiCompetencia(Boolean atraiCompetencia){
		this.atraiCompetencia = atraiCompetencia;
	}

	@Column(name = "in_classificado")
	public Boolean getClassificado(){
		return classificado;
	}

	public void setClassificado(Boolean classificado){
		this.classificado = classificado;
	}

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "pessoa")
	public List<MeioContato> getMeioContatoList(){
		return this.meioContatoList;
	}

	public void setMeioContatoList(List<MeioContato> meioContatoList){
		this.meioContatoList = meioContatoList;
	}

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, mappedBy = "pessoa")
	public List<PessoaNomeAlternativo> getPessoaNomeAlternativoList(){
		return pessoaNomeAlternativoList;
	}

	public void setPessoaNomeAlternativoList(List<PessoaNomeAlternativo> pessoaNomeAlternativoList){
		this.pessoaNomeAlternativoList = pessoaNomeAlternativoList;
	}

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "pessoa")
	@Cascade(value = {org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	public Set<PessoaDocumentoIdentificacao> getPessoaDocumentoIdentificacaoList(){
		return pessoaDocumentoIdentificacaoList;
	}

	public void setPessoaDocumentoIdentificacaoList(Set<PessoaDocumentoIdentificacao> pessoaDocumentoIdentificacaoList){
		this.pessoaDocumentoIdentificacaoList = pessoaDocumentoIdentificacaoList;
	}

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "pessoaRepresentada")
	public List<RelacaoPessoal> getRelacaoPessoalList(){
		return this.relacaoPessoalList;
	}

	public void setRelacaoPessoalList(List<RelacaoPessoal> relacaoPessoalList){
		this.relacaoPessoalList = relacaoPessoalList;
	}

	/**
	 * Devolve o documento da pessoa pelo tipo dela (Fisica/Juridica)
	 * Caso a pessoa nao tenha um documento válido, retorna null;
	 * 
	 * @param concatenarTipo indica se retornará o documento concatenado ao seu tipo
	 * @return o documento da pessoa
	 */
	public String getDocumentoCpfCnpj(Boolean concatenarTipo){
		if (this instanceof PessoaFisica){
			PessoaFisica pf = (PessoaFisica) this;
			if(pf.getNumeroCPF() != null) {
				return (concatenarTipo ? "CPF: "+pf.getNumeroCPF() : pf.getNumeroCPF());
			}
		}
		else if (this instanceof PessoaJuridica){
			PessoaJuridica pj = (PessoaJuridica) this;
			if(pj.getNumeroCNPJ() != null) {
				return (concatenarTipo ? "CNPJ: "+pj.getNumeroCNPJ() : pj.getNumeroCNPJ());	
			}
		}
		
		return null;
	}
	
	/**
	 * Devolve o documento da pessoa pelo tipo dela (Fisica/Juridica)
	 * @return o documento da pessoa
	 */
	@Transient
	public String getDocumentoCpfCnpj(){
		return getDocumentoCpfCnpj(false);
	}
	

	/**
	 * Devolve o tipo de documento da pessoa pelo tipo dela (Fisica/Juridica)
	 * @return o tipo de documento da pessoa
	 */
	@Transient
	public String getModalidadeDocumentoCpfCnpj(){
		if (this instanceof PessoaFisica){
			return "CPF";
		}
		else if (this instanceof PessoaJuridica){
			return "CNPJ";
		}
		
		return null;
	}
	
	/**
	 * Retorna informações referente a pessoa no padrão "Nome da Pessoa - CPF|CNPJ : 999999999"
	 * Se a pessoa nao tiver um documento CPF|CNPJ retorna apenas "Nome da Pessoa"
	 * 
	 * @return informações da pessoa conforme descritivo acima.
	 */
	public String obterInformacoes(){
		String ret = this.getNomeParte();

		String documento = this.getDocumentoCpfCnpj(true);
		if (documento != null){
			ret = ret.concat(" - ").concat(documento);				
		}
		return ret;		
	}
	
	

	/*
	 * TODO - Verificar entendimento abaixo - PESSOANEW Abrevia o nome da parte quando for menor de Idade Se não tiver a data de nascimento, CONSIDERA
	 * COMO MAIOR DE IDADE Pode acontecer em casos como advogados cadastrados pelo WS da OAB ou quando a pessoa foi cadastrada como não
	 * individualizada
	 */
	@Transient
	public String getNomeParte(){
		String nome = this.getNome();
		if (this instanceof PessoaFisica){
			PessoaFisica pessoaFisica = (PessoaFisica) Pessoa.this;
			if(pessoaFisica.getNomeSocial() != null && !StringUtil.isEmpty(pessoaFisica.getNomeSocial())) {
				nome = StringUtil.retornarNomeExibicao(nome, pessoaFisica.getNomeSocial());
			}
		}
		if (StringUtil.isSet(nome) && isMenor()){
			nome = StringUtil.obtemIniciais(nome);
		}
		return nome;
	}
	
	@Transient
	public boolean isMenor() {
		int idade = 0;
		boolean isMenor = false;
		if (this instanceof PessoaFisica){
			PessoaFisica pessoaFisica = (PessoaFisica) Pessoa.this;
			Calendar c = Calendar.getInstance();

			isMenor = false;
			if (pessoaFisica.getDataNascimento() != null){
				c.setTimeInMillis(new Date().getTime() - pessoaFisica.getDataNascimento().getTime());
				int ano = c.get(Calendar.YEAR);
				idade = ano - 1970;
				isMenor = idade < 18;
			}
		}
		return isMenor;
	}

	protected String buscaNumeroDocumentoIdentificacao(String codigoDocumento){
		PessoaDocumentoIdentificacao pdi = buscaDocumentoIdentificacao(codigoDocumento);
		if (pdi != null)
			return pdi.getNumeroDocumento();
		else
			return null;
	}

	public String buscaNumeroDocumentoIdentificacaoAtivo(String codigoDocumento){
		PessoaDocumentoIdentificacao pdi = buscaDocumentoIdentificacao(codigoDocumento, true);
		if (pdi != null)
			return pdi.getNumeroDocumento();
		else
			return null;
	}
	
	protected PessoaDocumentoIdentificacao buscaDocumentoIdentificacao(String codigoDocumento){
		return buscaDocumentoIdentificacao(codigoDocumento, false);
	}

	protected PessoaDocumentoIdentificacao buscaDocumentoIdentificacao(String codigoDocumento, boolean buscaSomenteAtivo){
		if (getPessoaDocumentoIdentificacaoList() != null && getPessoaDocumentoIdentificacaoList().size() > 0){
			for (PessoaDocumentoIdentificacao pdi : getPessoaDocumentoIdentificacaoList()){
				if(buscaSomenteAtivo && (pdi.getAtivo() != null && !pdi.getAtivo())) {
					continue;
				}
				if (pdi.getTipoDocumento().getCodTipo().equals(codigoDocumento) && pdi.getDocumentoPrincipal()){
					return pdi;
				}
			}
			for (PessoaDocumentoIdentificacao pdi : getPessoaDocumentoIdentificacaoList()){
				if(buscaSomenteAtivo && (pdi.getAtivo() != null && !pdi.getAtivo())) {
					continue;
				}
				if (pdi.getTipoDocumento().getCodTipo().equals(codigoDocumento)){
					return pdi;
				}
			}
		}
		return null;
	}
	
	public void setPessoaIndividualizada(Boolean pessoaIndividualizada){
		this.pessoaIndividualizada = pessoaIndividualizada;
	}

	/**
	 * Adicionado - PESSOANEW
	 * 
	 * @return
	 */
	@Column(name = "in_pessoa_individualizada")
	public Boolean getPessoaIndividualizada(){
		return pessoaIndividualizada;
	}

	@Column(name = "in_estrangeiro")
	public Boolean getEstrangeiro(){
		return estrangeiro;
	}

	public void setEstrangeiro(Boolean estrangeiro){
		this.estrangeiro = estrangeiro;
	}

	public void setOrgaoJulgadorInclusao(OrgaoJulgador orgaoJulgadorInclusao){
		this.orgaoJulgadorInclusao = orgaoJulgadorInclusao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj_inclusao")
	public OrgaoJulgador getOrgaoJulgadorInclusao(){
		return orgaoJulgadorInclusao;
	}

	public void setPessoaProcuradoriaEntidadeList(List<PessoaProcuradoriaEntidade> pessoaProcuradoriaEntidadeList){
		this.pessoaProcuradoriaEntidadeList = pessoaProcuradoriaEntidadeList;
	}

	@OneToMany(mappedBy = "pessoa", fetch = FetchType.LAZY)
	public List<PessoaProcuradoriaEntidade> getPessoaProcuradoriaEntidadeList(){
		return pessoaProcuradoriaEntidadeList;
	}

	@Override
	public String toString(){
		return super.getNome();
	}

	@Transient
	public Boolean getRecebeIntimacao(){
		return this.getCertChain() != null && (!this.getCertChain().isEmpty());
	}

	@Transient
	public String getUltimoPainelSelecionado() {
		if(ultimoPainelSelecionado == null || ultimoPainelSelecionado.isEmpty()){
			ultimoPainelSelecionado = "painel_usuario/list.seam";
			return "painel_usuario/list.seam";
		}else{
			return ultimoPainelSelecionado;
		}
	}

	public void setUltimoPainelSelecionado(String ultimoPainelSelecionado) {
		this.ultimoPainelSelecionado = ultimoPainelSelecionado;
	}
	
	/**
	 * Identifica se o usuário indicado tem uma das especializações possíveis.
	 *  
	 * @param p a pessoa a ser verificada
	 * @param clazz a classe a ser investigada
	 * @return true, se houver a especialização
	 */
	public static <T extends PessoaFisicaEspecializada> boolean instanceOf(UsuarioLogin p, Class<T> clazz){
		/**
		 * PJEII-5080 PJE-JT Antonio Lucas
		 * Verifica se o objeto passado como parametro não é nulo
		 * para evitar NullPointerException
		 */
		if (p != null){
			if(!(PessoaFisica.class.isAssignableFrom(p.getClass()))){
				return false;
			}
			PessoaFisica pessoa = (PessoaFisica) p;
			if(clazz.isAssignableFrom(PessoaAdvogado.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.ADV) == PessoaFisica.ADV;
			}else if(clazz.isAssignableFrom(PessoaAssistenteAdvogado.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.ASA) == PessoaFisica.ASA;
			}else if(clazz.isAssignableFrom(PessoaAssistenteProcuradoria.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.ASP) == PessoaFisica.ASP;
			}else if(clazz.isAssignableFrom(PessoaMagistrado.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.MAG) == PessoaFisica.MAG;
			}else if(clazz.isAssignableFrom(PessoaOficialJustica.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.OFJ) == PessoaFisica.OFJ;
			}else if(clazz.isAssignableFrom(PessoaPerito.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.PER) == PessoaFisica.PER;
			}else if(clazz.isAssignableFrom(PessoaProcurador.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.PRO) == PessoaFisica.PRO;
			}else if(clazz.isAssignableFrom(PessoaServidor.class)){
				return (pessoa.getEspecializacoes() & PessoaFisica.SER) == PessoaFisica.SER;
			}
		} 
		return false;
	}

	/* [PJEII-17703] - Alteração de RG e Título de Eleitor nos formulários que envolvem Pessoa
	 * Função que retorna o último documento ativo conforme o tipo de documento
	 * para auxiliar nas alterações realizadas nos formulários que envolvem Pessoa.
	 */
	public PessoaDocumentoIdentificacao getUltimoDocumentoIdentificacaoAtivoByTipoDocumento(String tipoDoc){
		PessoaDocumentoIdentificacao documentoRetorno = null;
		for (PessoaDocumentoIdentificacao documento : getPessoaDocumentoIdentificacaoList()) {
		 if(documento.getTipoDocumento().getCodTipo().trim().equals(tipoDoc)
				 && documento.getAtivo()){
			 if (documentoRetorno == null)
				 documentoRetorno = documento;
			 else{
				 if (documento.getIdDocumentoIdentificacao() > documentoRetorno.getIdDocumentoIdentificacao())
				 	documentoRetorno = documento;
			 }
		 }
		}
		return documentoRetorno;
	 }

	/**
	 * Metodo responsavel por analisar e retornar uma STRING com o tipo de pessoa, baseado se é uma pessoa fisica, juridica ou 'outras', 
	 * englobado como ente ou autoridade
	 * @return String tipo de pessoa
	 */
	@Transient
	public String getTipoPessoaResumidoAsString() {
		String retorno = null;
		if(this.inTipoPessoa != null) {
			if(this.inTipoPessoa == TipoPessoaEnum.A) {
				retorno = "Ente ou Autoridade";
			} else if (this.inTipoPessoa == TipoPessoaEnum.F) {
				retorno = "Pessoa Física";
			} else if (this.inTipoPessoa == TipoPessoaEnum.J){
				retorno = "Pessoa Jurídica";
			}
		}
		return retorno;
	}
	
	@Transient
	public Boolean getSelDocumentoAtivoInativo() {
		return selDocumentoAtivoInativo;
	}
	
	public void setSelDocumentoAtivoInativo(Boolean valor) {
		selDocumentoAtivoInativo = valor;
	}

	@Transient
	@Override
	public Class<? extends UsuarioLogin> getEntityClass() {
		return Pessoa.class;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pessoa")
	public List<NomePessoa> getNomesPessoa() {
		return nomesPessoa;
	}

	public void setNomesPessoa(List<NomePessoa> nomesPessoa) {
		this.nomesPessoa = nomesPessoa;
	}
}
