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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe de mapeamento para as entidades antes derivadas de pessoa física que
 * foram dela desvinculadas. Trata-se de solução "temporária" para o problema da
 * excessiva especialização de pessoa no PJe.
 * 
 * @author Antônio Augusto Silva Martins
 * @author Paulo Cristovão de Araújo Silva Filho
 * @author Thiago de Andrade Vieira
 * 
 */
@MappedSuperclass
@EntityListeners(PessoaFisicaEspecializadaListener.class)
@SecondaryTables({ @SecondaryTable(name = "tb_usuario_login", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
		@SecondaryTable(name = "tb_usuario", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
		@SecondaryTable(name = "tb_pessoa", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa", referencedColumnName = "id") }),
		@SecondaryTable(name = "tb_pessoa_fisica", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa_fisica", referencedColumnName = "id") }) })
public class PessoaFisicaEspecializada implements IEntidade<PessoaFisicaEspecializada, Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 391138444144226795L;

	private Integer idUsuario;

	private PessoaFisica pessoa = new PessoaFisica();

	// Atributos oriundos de UsuarioLogin

	private String senha;

	private String email;

	private String login;

	private String nome;

	private String assinatura;

	private String certChain;

	private Boolean ativo = Boolean.TRUE;
	
	private String hashAtivacaoSenha;
	
	private StatusSenhaEnum statusSenha;
	
	private Date dataValidadeSenha;	

	private Set<Papel> papelSet = new TreeSet<Papel>();

	// Atributos oriundos de Usuario

	private Boolean bloqueio;

	private Boolean provisorio;

	private List<UsuarioLocalizacao> usuarioLocalizacaoList = new ArrayList<UsuarioLocalizacao>(0);

	private List<BloqueioUsuario> bloqueioUsuarioList = new ArrayList<BloqueioUsuario>(0);

	private List<Endereco> enderecoList = new ArrayList<Endereco>(0);

	// Atributos oriundos de Pessoa.java

	private TipoPessoa tipoPessoa;

	private TipoPessoaEnum inTipoPessoa = TipoPessoaEnum.F;

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

	// Atributos oriundos de PessoaFisica.java

	private Etnia etnia;

	private EstadoCivil estadoCivil;

	private Profissao profissao;

	private Escolaridade escolaridade;

	private SexoEnum sexo;

	private Date dataNascimento;

	private String nomeGenitor;

	private String nomeGenitora;
	
	private String nomeSocial;

	private Municipio municipioNascimento;

	private Date dataObito;

	private String dddCelular;

	private String numeroCelular;

	private String dddResidencial;

	private String numeroResidencial;

	private String dddComercial;

	private String numeroComercial;

	private Boolean validado;

	private Date dataValidacao;

	private String nomeAlcunha;

	private boolean incapaz = false;

	private Pais paisNascimento;

	private String outrasCaracteristicasPessoais;

	private List<CaracteristicaFisica> caracteristicasFisicas = new ArrayList<CaracteristicaFisica>(0);

	private Boolean brasileiro = Boolean.TRUE;
	
	public PessoaFisicaEspecializada(){
	}

	/**
	 * Recupera o identificador da pessoa, que é igual àquele existente em
	 * {@link #getPessoa()}.
	 * 
	 * @return o identificador
	 */
	@Id
	@Column(name = "id")
	public Integer getIdUsuario() {
		return idUsuario;
	}

	/**
	 * Atribui a esta pessoa um identificador para gravação em banco de dados.
	 * Não deve ser chamado diretamente em razão da existência de um evento de
	 * prepersist previsto em {@link PessoaFisicaEspecializadaListener#prePersist(PessoaGato)}
	 * 
	 * @param idUsuario
	 *            o identificador a ser atribuído.
	 */
	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	/**
	 * Recupera a pessoa vinculada a esta entidade.
	 * 
	 * @return a pessoa física vinculada a esta entidade.
	 */
	@OneToOne(optional = false, fetch=FetchType.LAZY)
	@JoinColumn(name="id")
	public PessoaFisica getPessoa() {
		return pessoa;
	}

	/**
	 * Atribui a esta pessoa uma pessoa física vinculada.
	 * 
	 * @param pessoa
	 *            a pessoa a ser vinculada.
	 */
	public void setPessoa(PessoaFisica pessoa) {
		this.pessoa = pessoa;
	}

	/**
	 * Recupera a senha, encriptada, da pessoa.
	 * 
	 * @return a senha em formato encriptado
	 */
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Column(table = "tb_usuario_login", name = "ds_senha", length = 100)
	@Length(max = 100)
	public String getSenha() {
		return senha;
	}

	/**
	 * Atribui a esta pessoa uma senha. A atribuição deve ser feita sob a forma
	 * encriptada.
	 * 
	 * @param senha
	 *            a senha a ser atribuída.
	 */
	public void setSenha(String senha) {
		
		this.senha = senha;
	}

	/**
	 * Recupera o email vinculado a esta pessoa.
	 * 
	 * @return o email cadastrado
	 */
	@Column(table = "tb_usuario_login", name = "ds_email", length = 100)
	@Length(max = 100)
	public String getEmail() {
		return email;
	}

	/**
	 * Atribui a esta pessoa um email.
	 * 
	 * @param email
	 *            o email a ser atribuído
	 */
	public void setEmail(String email) {
		
		this.email = email;
	}

	/**
	 * Recupera o login do usuário vinculado a esta pessoa.
	 * 
	 * @return o login do usuário vinculado
	 */
	@Column(table = "tb_usuario_login", name = "ds_login", unique = true, length = 100)
	@Length(max = 100)
	public String getLogin() {
		return login;
	}

	/**
	 * Atribui ao usuário vinculado a esta pessoa um determinado login.
	 * 
	 * @param login o login a ser atribuído
	 */
	public void setLogin(String login) {
		
		this.login = login;
	}

	/**
	 * Recupera o nome padrão da pessoa.
	 * 
	 * @return o nome da pessoa
	 */
	@Column(table = "tb_usuario_login", name = "ds_nome", length = 255)
	@Length(max = 255)
	public String getNome() {
		return nome;
	}

	/**
	 * Atribui a esta pessoa um nome.
	 * 
	 * @param nome o nome a ser atribuído
	 */
	public void setNome(String nome) {
		
		this.nome = nome;
	}

	/**
	 * Recupera a assinatura 
	 * @return
	 */
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(table = "tb_usuario_login", name = "ds_assinatura_usuario")
	public String getAssinatura() {
		return assinatura;
	}

	public void setAssinatura(String assinatura) {
		
		this.assinatura = assinatura;
	}

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(table = "tb_usuario_login", name = "ds_cert_chain_usuario")
	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		
		this.certChain = certChain;
	}

	@Column(table = "tb_usuario_login", name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {		
		this.ativo = ativo;
	}
	
	@Column(table = "tb_usuario_login", name="hash_ativacao_senha")
	public String getHashAtivacaoSenha() {
		return hashAtivacaoSenha;
	}
	
	public void setHashAtivacaoSenha(String hashAtivacaoSenha) {
		this.hashAtivacaoSenha = hashAtivacaoSenha;
	}
	
	@Column(table = "tb_usuario_login", name="in_status_senha", length = 1)
	@Enumerated(EnumType.STRING)
	public StatusSenhaEnum getStatusSenha() {
		return statusSenha;
	}
	
	public void setStatusSenha(StatusSenhaEnum statusSenha) {
		this.statusSenha = statusSenha;
	}
	
	@Column(table = "tb_usuario_login", name="dt_validade_senha")
	public Date getDataValidadeSenha() {
		return dataValidadeSenha;
	}
	
	public void setDataValidadeSenha(Date dataValidadeSenha) {
		this.dataValidadeSenha = dataValidadeSenha;
	}

	@ManyToMany(cascade={CascadeType.REFRESH})
	@JoinTable(name = "tb_usuario_papel", joinColumns = @JoinColumn(name = "id_usuario"), inverseJoinColumns = @JoinColumn(name = "id_papel"))
	@ForeignKey(name = "tb_usuario_papel_usuario_fk", inverseName = "tb_usuario_papel_papel_fk")
	public Set<Papel> getPapelSet() {
		return papelSet;
	}

	public void setPapelSet(Set<Papel> papelSet) {
		
		this.papelSet = papelSet;
	}

	public boolean checkCertChain(String certChain) {
		return getPessoa().checkCertChain(certChain);
	}

	@Column(table = "tb_usuario", name = "in_bloqueio")
	public Boolean getBloqueio() {
		return bloqueio;
	}

	public void setBloqueio(Boolean bloqueio) {
		
		this.bloqueio = bloqueio;
	}

	@Column(table = "tb_usuario", name = "in_provisorio")
	public Boolean getProvisorio() {
		return provisorio;
	}

	public void setProvisorio(Boolean provisorio) {
		
		this.provisorio = provisorio;
	}

	@OneToMany(cascade = {CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name="id_usuario")
	public List<UsuarioLocalizacao> getUsuarioLocalizacaoList() {
		return usuarioLocalizacaoList;
	}

	public void setUsuarioLocalizacaoList(List<UsuarioLocalizacao> usuarioLocalizacaoList) {
		
		this.usuarioLocalizacaoList = usuarioLocalizacaoList;
	}

	@OneToMany(cascade = {CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name="id_usuario")
	public List<BloqueioUsuario> getBloqueioUsuarioList() {
		return bloqueioUsuarioList;
	}

	public void setBloqueioUsuarioList(List<BloqueioUsuario> bloqueioUsuarioList) {
		
		this.bloqueioUsuarioList = bloqueioUsuarioList;
	}

	@OneToMany(cascade = {CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name="id_usuario")
	public List<Endereco> getEnderecoList() {
		return enderecoList;
	}

	public void setEnderecoList(List<Endereco> enderecoList) {
		
		this.enderecoList = enderecoList;
	}

	@Transient
	public Localizacao[] getLocalizacoes() {
		return getPessoa().getLocalizacoes();
	}

	@Transient
	public List<UsuarioLocalizacaoVisibilidade> getUsuarioLocalizacoesVisibilidades() {
		return getPessoa().getUsuarioLocalizacoesVisibilidades(this.usuarioLocalizacaoList);
	}

	@ManyToOne(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(table="tb_pessoa", name = "id_tipo_pessoa")
	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		
		this.tipoPessoa = tipoPessoa;
	}

	@Column(table="tb_pessoa", name = "in_tipo_pessoa", length = 1)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoPessoaType")
	public TipoPessoaEnum getInTipoPessoa() {
		return inTipoPessoa;
	}

	public void setInTipoPessoa(TipoPessoaEnum inTipoPessoa) {
		
		this.inTipoPessoa = inTipoPessoa;
	}

	@Column(table="tb_pessoa", name = "in_atrai_competencia")
	public Boolean getAtraiCompetencia() {
		return atraiCompetencia;
	}

	public void setAtraiCompetencia(Boolean atraiCompetencia) {
		
		this.atraiCompetencia = atraiCompetencia;
	}

	@Column(table="tb_pessoa", name = "in_classificado")
	public Boolean getClassificado() {
		return classificado;
	}

	public void setClassificado(Boolean classificado) {
		
		this.classificado = classificado;
	}

	@Column(table="tb_pessoa", name = "in_pessoa_individualizada")
	public Boolean getPessoaIndividualizada() {
		return pessoaIndividualizada;
	}

	public void setPessoaIndividualizada(Boolean pessoaIndividualizada) {
		
		this.pessoaIndividualizada = pessoaIndividualizada;
	}

	@Column(table="tb_pessoa", name = "in_estrangeiro")
	public Boolean getEstrangeiro() {
		return estrangeiro;
	}

	public void setEstrangeiro(Boolean estrangeiro) {
		
		this.estrangeiro = estrangeiro;
	}

	@ManyToOne(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(table="tb_pessoa", name = "id_oj_inclusao")
	public OrgaoJulgador getOrgaoJulgadorInclusao() {
		return orgaoJulgadorInclusao;
	}

	public void setOrgaoJulgadorInclusao(OrgaoJulgador orgaoJulgadorInclusao) {
		
		this.orgaoJulgadorInclusao = orgaoJulgadorInclusao;
	}

	@OneToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name="id_pessoa")
	public List<PessoaProcuradoriaEntidade> getPessoaProcuradoriaEntidadeList() {
		return pessoaProcuradoriaEntidadeList;
	}

	public void setPessoaProcuradoriaEntidadeList(List<PessoaProcuradoriaEntidade> pessoaProcuradoriaEntidadeList) {
		
		this.pessoaProcuradoriaEntidadeList = pessoaProcuradoriaEntidadeList;
	}

	@OneToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
	public List<PessoaQualificacao> getPessoaQualificacaoList() {
		return pessoaQualificacaoList;
	}

	public void setPessoaQualificacaoList(List<PessoaQualificacao> pessoaQualificacaoList) {
		
		this.pessoaQualificacaoList = pessoaQualificacaoList;
	}

	@OneToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
	public List<ProcessoParte> getProcessoParteList() {
		return processoParteList;
	}

	public void setProcessoParteList(List<ProcessoParte> processoParteList) {
		
		this.processoParteList = processoParteList;
	}

	@OneToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name="id_pessoa")
	public List<ProcessoAudienciaPessoa> getProcessoAudienciaPessoaList() {
		return processoAudienciaPessoaList;
	}

	public void setProcessoAudienciaPessoaList(List<ProcessoAudienciaPessoa> processoAudienciaPessoaList) {
		
		this.processoAudienciaPessoaList = processoAudienciaPessoaList;
	}

	@OneToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@OrderBy(value = "dataInclusao")
	@JoinColumn(name="id_pessoa")
	public List<DocumentoPessoa> getDocumentoPessoaList() {
		return documentoPessoaList;
	}

	public void setDocumentoPessoaList(List<DocumentoPessoa> documentoPessoaList) {
		
		this.documentoPessoaList = documentoPessoaList;
	}

	@OneToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name="id_pessoa")
	public List<MeioContato> getMeioContatoList() {
		return meioContatoList;
	}

	public void setMeioContatoList(List<MeioContato> meioContatoList) {
		
		this.meioContatoList = meioContatoList;
	}

	@OneToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name="id_pessoa")
	public List<PessoaNomeAlternativo> getPessoaNomeAlternativoList() {
		return pessoaNomeAlternativoList;
	}

	public void setPessoaNomeAlternativoList(List<PessoaNomeAlternativo> pessoaNomeAlternativoList) {
		
		this.pessoaNomeAlternativoList = pessoaNomeAlternativoList;
	}

	@OneToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name="id_pessoa")
	public Set<PessoaDocumentoIdentificacao> getPessoaDocumentoIdentificacaoList() {
		return pessoaDocumentoIdentificacaoList;
	}

	public void setPessoaDocumentoIdentificacaoList(Set<PessoaDocumentoIdentificacao> pessoaDocumentoIdentificacaoList) {
		
		this.pessoaDocumentoIdentificacaoList = pessoaDocumentoIdentificacaoList;
	}

	@OneToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name="id_representado")
	public List<RelacaoPessoal> getRelacaoPessoalList() {
		return relacaoPessoalList;
	}

	public void setRelacaoPessoalList(List<RelacaoPessoal> relacaoPessoalList) {
		
		this.relacaoPessoalList = relacaoPessoalList;
	}
	
	@Transient
	public String getUltimoPainelSelecionado() {
		return getPessoa().getUltimoPainelSelecionado();
	}

	/**
	 * Devolve o documento da pessoa pelo tipo dela (Fisica/Juridica)
	 * 
	 * @return
	 */
	@Transient
	public String getDocumentoCpfCnpj(){
		return getPessoa().getDocumentoCpfCnpj();
	}
	
	/*
	 * TODO - Verificar entendimento abaixo - PESSOANEW Abrevia o nome da parte quando for menor de Idade Se não tiver a data de nascimento, CONSIDERA
	 * COMO MAIOR DE IDADE Pode acontecer em casos como advogados cadastrados pelo WS da OAB ou quando a pessoa foi cadastrada como não
	 * individualizada
	 */
	@Transient
	public String getNomeParte(){
		return getPessoa().getNomeParte();
	}
	
	protected String buscaNumeroDocumentoIdentificacao(String codigoDocumento){
		return getPessoa().buscaNumeroDocumentoIdentificacao(codigoDocumento);
	}
	
	protected String buscaNumeroDocumentoIdentificacaoAtivo(String codigoDocumento){
		return getPessoa().buscaNumeroDocumentoIdentificacaoAtivo(codigoDocumento);
	}

	protected PessoaDocumentoIdentificacao buscaDocumentoIdentificacao(String codigoDocumento){
		return getPessoa().buscaDocumentoIdentificacao(codigoDocumento);
	}
	
	protected PessoaDocumentoIdentificacao buscaDocumentoIdentificacao(String codigoDocumento, boolean buscaSomenteAtivo){
		return getPessoa().buscaDocumentoIdentificacao(codigoDocumento, buscaSomenteAtivo);
	}

	@Transient
	public Boolean getRecebeIntimacao(){
		return getPessoa().getRecebeIntimacao();
	}

	/// Pessoa Física
	
	@ManyToOne(cascade={CascadeType.REFRESH})
	@JoinColumn(table="tb_pessoa_fisica", name = "id_etnia")
	public Etnia getEtnia() {
		return etnia;
	}

	public void setEtnia(Etnia etnia) {
		
		this.etnia = etnia;
	}

	@ManyToOne(cascade={CascadeType.REFRESH})
	@JoinColumn(table="tb_pessoa_fisica", name = "id_estado_civil")
	public EstadoCivil getEstadoCivil() {
		return estadoCivil;
	}

	public void setEstadoCivil(EstadoCivil estadoCivil) {
		
		this.estadoCivil = estadoCivil;
	}

	@ManyToOne(cascade={CascadeType.REFRESH})
	@JoinColumn(table="tb_pessoa_fisica", name = "id_profissao")
	public Profissao getProfissao() {
		return profissao;
	}

	public void setProfissao(Profissao profissao) {
		
		this.profissao = profissao;
	}

	@ManyToOne(cascade={CascadeType.REFRESH})
	@JoinColumn(table="tb_pessoa_fisica", name = "id_escolaridade")
	public Escolaridade getEscolaridade() {
		return escolaridade;
	}

	public void setEscolaridade(Escolaridade escolaridade) {
		
		this.escolaridade = escolaridade;
	}

	@Transient
	public String getNumeroCPF() {
		return this.pessoa.getNumeroCPF();
	}

	public void setNumeroCPF(String numeroCPF) {
		getPessoa().setNumeroCPF(numeroCPF);
	}
	
	@Transient
	public String getNumeroCPFAtivo() {
		return getPessoa().getNumeroCPFAtivo();
	}

	public void setNumeroCPFAtivo(String numeroCPF) {
		getPessoa().setNumeroCPFAtivo(numeroCPF);
	}

	@Transient
	public String getNumeroPassaporte() {
		return getPessoa().getNumeroPassaporte();
	}

	public void setNumeroPassaporte(String numeroPassaporte) {
		getPessoa().setNumeroPassaporte(numeroPassaporte);
	}

	@Column(table="tb_pessoa_fisica", name = "in_sexo")
	@Enumerated(EnumType.STRING)
	public SexoEnum getSexo() {
		return sexo;
	}

	public void setSexo(SexoEnum sexo) {
		
		this.sexo = sexo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(table="tb_pessoa_fisica", name = "dt_nascimento")
	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		
		this.dataNascimento = dataNascimento;
	}

	@Column(table="tb_pessoa_fisica", name = "ds_nome_social", length = 255)
	@Length(max = 255)
	public String getNomeSocial() {
		return nomeSocial;
	}

	public void setNomeSocial(String nomeSocial) {
		
		this.nomeSocial= nomeSocial;
	}
	
	@Column(table="tb_pessoa_fisica", name = "nm_genitor", length = 200)
	@Length(max = 200)
	public String getNomeGenitor() {
		return nomeGenitor;
	}

	public void setNomeGenitor(String nomeGenitor) {
		
		this.nomeGenitor = nomeGenitor;
	}

	@Column(table="tb_pessoa_fisica", name = "nm_genitora", length = 200)
	@Length(max = 200)
	public String getNomeGenitora() {
		return nomeGenitora;
	}

	public void setNomeGenitora(String nomeGenitora) {
		
		this.nomeGenitora = nomeGenitora;
	}

	@ManyToOne(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(table="tb_pessoa_fisica", name = "id_municipio_nascimento")
	public Municipio getMunicipioNascimento() {
		return municipioNascimento;
	}

	public void setMunicipioNascimento(Municipio municipioNascimento) {
		
		this.municipioNascimento = municipioNascimento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(table="tb_pessoa_fisica", name = "dt_obito")
	public Date getDataObito() {
		return dataObito;
	}

	public void setDataObito(Date dataObito) {
		
		this.dataObito = dataObito;
	}

	@Column(table="tb_pessoa_fisica", name = "nr_ddd_celular", length = 2)
	@Length(max = 2)
	public String getDddCelular() {
		return dddCelular;
	}

	public void setDddCelular(String dddCelular) {
		
		this.dddCelular = dddCelular;
	}

	@Column(table="tb_pessoa_fisica", name = "nr_celular", length = 15)
	@Length(max = 15)
	public String getNumeroCelular() {
		return numeroCelular;
	}

	public void setNumeroCelular(String numeroCelular) {
		
		this.numeroCelular = numeroCelular;
	}

	@Column(table="tb_pessoa_fisica", name = "nr_ddd_tel_residencial", length = 2)
	@Length(max = 2)
	public String getDddResidencial() {
		return dddResidencial;
	}

	public void setDddResidencial(String dddResidencial) {
		
		this.dddResidencial = dddResidencial;
	}

	@Column(table="tb_pessoa_fisica", name = "nr_tel_residencial", length = 15)
	@Length(max = 15)
	public String getNumeroResidencial() {
		return numeroResidencial;
	}

	public void setNumeroResidencial(String numeroResidencial) {
		
		this.numeroResidencial = numeroResidencial;
	}

	@Column(table="tb_pessoa_fisica", name = "nr_ddd_tel_comercial", length = 2)
	@Length(max = 2)
	public String getDddComercial() {
		return dddComercial;
	}

	public void setDddComercial(String dddComercial) {
		
		this.dddComercial = dddComercial;
	}

	@Column(table="tb_pessoa_fisica", name = "nr_tel_comercial", length = 15)
	@Length(max = 15)
	public String getNumeroComercial() {
		return numeroComercial;
	}

	public void setNumeroComercial(String numeroComercial) {
		
		this.numeroComercial = numeroComercial;
	}

	@Column(table="tb_pessoa_fisica", name = "in_validado")
	public Boolean getValidado() {
		return validado;
	}

	public void setValidado(Boolean validado) {
		
		this.validado = validado;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(table="tb_pessoa_fisica", name = "dt_validacao")
	public Date getDataValidacao() {
		return dataValidacao;
	}

	public void setDataValidacao(Date dataValidacao) {
		
		this.dataValidacao = dataValidacao;
	}

	@Transient
	@Deprecated
	public String getNomeAlcunha() {
		return nomeAlcunha;
	}

	@Deprecated
	public void setNomeAlcunha(String nomeAlcunha) {
		this.nomeAlcunha = nomeAlcunha;
	}

	@Column(table="tb_pessoa_fisica", name = "in_incapaz")
	public boolean isIncapaz() {
		return incapaz;
	}

	public void setIncapaz(boolean incapaz) {
		
		this.incapaz = incapaz;
	}

	@ManyToOne(cascade={CascadeType.REFRESH})
	@JoinColumn(table="tb_pessoa_fisica", name = "id_pais")	
	public Pais getPaisNascimento() {
		return paisNascimento;
	}

	public void setPaisNascimento(Pais paisNascimento) {
		
		this.paisNascimento = paisNascimento;
	}

	@Length(max = 1000)
	@Column(table="tb_pessoa_fisica", name = "ds_outras_caracteristicas", length = 1000)	
	public String getOutrasCaracteristicasPessoais() {
		return outrasCaracteristicasPessoais;
	}

	public void setOutrasCaracteristicasPessoais(String outrasCaracteristicasPessoais) {
		
		this.outrasCaracteristicasPessoais = outrasCaracteristicasPessoais;
	}

	@OneToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name="id_pessoa_fisica")
	public List<CaracteristicaFisica> getCaracteristicasFisicas() {
		return caracteristicasFisicas;
	}

	public void setCaracteristicasFisicas(List<CaracteristicaFisica> caracteristicasFisicas) {
		
		this.caracteristicasFisicas = caracteristicasFisicas;
	}

	@Column(table="tb_pessoa_fisica", name = "in_brasileiro")
	public Boolean getBrasileiro() {
		return brasileiro;
	}

	public void setBrasileiro(Boolean brasileiro) {
		
		this.brasileiro = brasileiro;
	}
	
	@Override
	public String toString(){
		String retorno = getPessoa().getNome();
		if(this.getNomeSocial() != null && !StringUtil.isEmpty(this.getNomeSocial())) {
			retorno = StringUtil.retornarNomeExibicao(nome, this.getNomeSocial());
		}
		return retorno;
	}
	
	@Transient
	public String getDataNascimentoFormatada() {
		return getPessoa().getDataNascimentoFormatada();
	}
	
	@Transient
	public String getPessoaStr() {
		return getPessoa().getPessoaStr();
	}
	
	public String getUrlAtivacaoSenha(String urlSistema){
		if(getIdUsuario() != null && getHashAtivacaoSenha() != null && getLogin() != null && urlSistema != null){
			System.out.println("[URL EMAIL para login "+getIdUsuario()+": "+urlSistema + "/Senha/ativacaoSenha.seam?hashCodigoAtivacao="+getHashAtivacaoSenha()+"&login="+getLogin()+"]");
			return urlSistema + "/Senha/ativacaoSenha.seam?hashCodigoAtivacao="+getHashAtivacaoSenha()+"&login="+getLogin();
		}
		
		System.out.println("[URL EMAIL para login "+getIdUsuario()+": nao foi gerada URL");
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		Integer idObj = null;
		if(PessoaFisicaEspecializada.class.isAssignableFrom(obj.getClass())){
			PessoaFisicaEspecializada p = (PessoaFisicaEspecializada) obj;
			idObj = p.getIdUsuario();
		}else if(UsuarioLogin.class.isAssignableFrom(obj.getClass())){
			return this.equals((UsuarioLogin) obj);
		}else{
			return false;
		}
		if(idObj == null){
			return false;
		}
		return idObj.equals(getIdUsuario());
	}
	
	public boolean equals(UsuarioLogin u) {
		Integer idObj = null;
		if(u.getIdUsuario() == 0) {
			idObj = null;
		} else {
			idObj = Integer.valueOf(u.getIdUsuario());
		}
		if(idObj == null){
			return false;
		}
		return idObj.equals(getIdUsuario());
	}	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdUsuario() == null) ? 0 : getIdUsuario().hashCode());
		return result;
	}

	/**
	 * Método responsável por retornar se o perfil de uma determinada Pessoa
	 * Especializada encontra-se ativo (<code>true</code>) ou inativo (
	 * <code>false</code>).
	 * 
	 * @return <code>Boolean</code>, <code>true</code> para perfil ativo e
	 *         <code>false</code> para perfil inativo.
	 */
	@Transient
	public Boolean isPerfilAtivo() {
		if (this instanceof PessoaServidor) {
			return ((PessoaServidor) this).getServidorAtivo();
		} else if (this instanceof PessoaMagistrado) {
			return ((PessoaMagistrado) this).getMagistradoAtivo();
		} else if (this instanceof PessoaProcurador) {
			return ((PessoaProcurador) this).getProcuradorAtivo();
		} else if (this instanceof PessoaAdvogado) {
			return ((PessoaAdvogado) this).getAdvogadoAtivo();
		} else if (this instanceof PessoaPerito) {
			return ((PessoaPerito) this).getPeritoAtivo();
		} else if (this instanceof PessoaOficialJustica) {
			return ((PessoaOficialJustica) this).getOficialJusticaAtivo();
		} else if (this instanceof PessoaAssistenteAdvogado) {
			return((PessoaAssistenteAdvogado) this).getAssistenteAdvogadoAtivo();
		} else if (this instanceof PessoaAssistenteProcuradoria) {
			return ((PessoaAssistenteProcuradoria) this).getAssistenteProcuradoriaAtivo();
		}
		return true;
	}

	@Override
	@Transient
	public Class<? extends PessoaFisicaEspecializada> getEntityClass() {
		return PessoaFisicaEspecializada.class;
	}

	@Override
	@Transient
	public Integer getEntityIdObject() {
		return idUsuario;
	}

	@Override
	@Transient
	public boolean isLoggable() {
		return true;
	}
}
