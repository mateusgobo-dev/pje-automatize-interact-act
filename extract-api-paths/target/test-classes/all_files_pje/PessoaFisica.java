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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

/**
 * Entidade destinada a representar uma pessoa física no regime jurídico brasileiro.
 * 
 * @author infox
 *
 */
@Entity
@Table(name = PessoaFisica.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_pessoa_fisica")
@Cacheable
public class PessoaFisica extends Pessoa implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pessoa_fisica";
	private static final long serialVersionUID = 1L;
	
	/**
	 * Flag indicativa de que esta pessoa é advogado. Deve ser comparada com o campo {@link #especializacoes}
	 */
	public static final int ADV = 1;

	/**
	 * Flag indicativa de que esta pessoa é assistente de advogado. Deve ser comparada com o campo {@link #especializacoes}
	 */
	public static final int ASA = 2;

	/**
	 * Flag indicativa de que esta pessoa é assistente de procuradoria. Deve ser comparada com o campo {@link #especializacoes}
	 */
	public static final int ASP = 4;

	/**
	 * Flag indicativa de que esta pessoa é magistrado. Deve ser comparada com o campo {@link #especializacoes}
	 */
	public static final int MAG = 8;

	/**
	 * Flag indicativa de que esta pessoa é oficial de justiça. Deve ser comparada com o campo {@link #especializacoes}
	 */
	public static final int OFJ = 16;

	/**
	 * Flag indicativa de que esta pessoa é perito. Deve ser comparada com o campo {@link #especializacoes}
	 */
	public static final int PER = 32;

	/**
	 * Flag indicativa de que esta pessoa é procurador. Deve ser comparada com o campo {@link #especializacoes}
	 */
	public static final int PRO = 64;

	/**
	 * Flag indicativa de que esta pessoa é servidor. Deve ser comparada com o campo {@link #especializacoes}
	 */
	public static final int SER = 128;

	private Etnia etnia;
	private EstadoCivil estadoCivil;
	private Profissao profissao;
	private Escolaridade escolaridade;
	private String numeroCPF;
	private Date dataCPF;
	private String numeroPassaporte;
	private SexoEnum sexo;
	private Date dataNascimento;
	private String nomeGenitor;
	private String nomeGenitora;
	private String nomeSocial;
	private String numeroTituloEleitor;
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
	private List<CaixaRepresentante> caixaRepresentanteList = new ArrayList<CaixaRepresentante>(0);
	private Boolean brasileiro = Boolean.TRUE;
	private int especializacoes = 0;
	
	private PessoaAdvogado pessoaAdvogado;
	
	private PessoaAssistenteAdvogado pessoaAssistenteAdvogado;
	
	private PessoaAssistenteProcuradoria pessoaAssistenteProcuradoria;
	
	private PessoaMagistrado pessoaMagistrado;
	
	private PessoaPerito pessoaPerito;
	
	private PessoaProcurador pessoaProcurador;
	
	private PessoaServidor pessoaServidor;
	
	private PessoaOficialJustica pessoaOficialJustica;
	
	public PessoaFisica() {
		setInTipoPessoa(TipoPessoaEnum.F);
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_etnia")
	public Etnia getEtnia() {
		return etnia;
	}

	public void setEtnia(Etnia etnia) {
		this.etnia = etnia;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_estado_civil")
	public EstadoCivil getEstadoCivil() {
		return estadoCivil;
	}

	public void setEstadoCivil(EstadoCivil estadoCivil) {
		this.estadoCivil = estadoCivil;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_profissao")
	public Profissao getProfissao() {
		return profissao;
	}

	public void setProfissao(Profissao profissao) {
		this.profissao = profissao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_escolaridade")
	public Escolaridade getEscolaridade() {
		return escolaridade;
	}

	public void setEscolaridade(Escolaridade escolaridade) {
		this.escolaridade = escolaridade;
	}

	@Transient
	public String getNumeroCPF() {
		return this.numeroCPF;
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	@Transient
	public String getNumeroCPFAtivo() {
		return this.numeroCPF;
	}

	public void setNumeroCPFAtivo(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}
	
	@Transient
	public String getNumeroPassaporte() {
		return this.numeroPassaporte;
	}

	public void setNumeroPassaporte(String numeroPassaporte) {
		this.numeroPassaporte = numeroPassaporte;
	}

	@Column(name = "in_sexo")
	@Enumerated(EnumType.STRING)
	public SexoEnum getSexo() {
		return sexo;
	}

	public void setSexo(SexoEnum sexo) {
		this.sexo = sexo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_nascimento", nullable = true)
	public Date getDataNascimento() {
		return this.dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	@Column(name = "nm_genitor", length = 200)
	@Length(max = 200)
	public String getNomeGenitor() {
		return this.nomeGenitor;
	}

	public void setNomeGenitor(String nomeGenitor) {
		this.nomeGenitor = nomeGenitor;
	}

	@Column(name = "nm_genitora", length = 200)
	@Length(max = 200)
	public String getNomeGenitora() {
		return this.nomeGenitora;
	}

	public void setNomeGenitora(String nomeGenitora) {
		this.nomeGenitora = nomeGenitora;
	}

	@Transient
	public String getNumeroTituloEleitor() {
		return this.numeroTituloEleitor;
	}

	public void setNumeroTituloEleitor(String numeroTituloEleitor) {
		this.numeroTituloEleitor = numeroTituloEleitor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_municipio_nascimento")
	public Municipio getMunicipioNascimento() {
		return municipioNascimento;
	}

	public void setMunicipioNascimento(Municipio municipioNascimento) {
		this.municipioNascimento = municipioNascimento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_obito")
	public Date getDataObito() {
		return this.dataObito;
	}

	public void setDataObito(Date dataObito) {
		this.dataObito = dataObito;
	}

	@Column(name = "nr_ddd_celular", length = 2)
	@Length(max = 2)
	public String getDddCelular() {
		return this.dddCelular;
	}

	public void setDddCelular(String dddCelular) {
		this.dddCelular = dddCelular;
	}

	@Column(name = "nr_celular", length = 15)
	@Length(max = 15)
	public String getNumeroCelular() {
		return this.numeroCelular;
	}

	public void setNumeroCelular(String numeroCelular) {
		this.numeroCelular = numeroCelular;
	}

	@Column(name = "nr_ddd_tel_residencial", length = 2)
	@Length(max = 2)
	public String getDddResidencial() {
		return this.dddResidencial;
	}

	public void setDddResidencial(String dddResidencial) {
		this.dddResidencial = dddResidencial;
	}

	@Column(name = "nr_tel_residencial", length = 15)
	@Length(max = 15)
	public String getNumeroResidencial() {
		return this.numeroResidencial;
	}

	public void setNumeroResidencial(String numeroResidencial) {
		this.numeroResidencial = numeroResidencial;
	}

	@Column(name = "nr_ddd_tel_comercial", length = 2)
	@Length(max = 2)
	public String getDddComercial() {
		return this.dddComercial;
	}

	public void setDddComercial(String dddComercial) {
		this.dddComercial = dddComercial;
	}

	@Column(name = "nr_tel_comercial", length = 15)
	@Length(max = 15)
	public String getNumeroComercial() {
		return this.numeroComercial;
	}

	public void setNumeroComercial(String numeroComercial) {
		this.numeroComercial = numeroComercial;
	}

	@Column(name = "in_validado")
	public Boolean getValidado() {
		return validado;
	}

	public void setValidado(Boolean validado) {
		this.validado = validado;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_validacao")
	public Date getDataValidacao() {
		return dataValidacao;
	}

	public void setDataValidacao(Date dataValidacao) {
		this.dataValidacao = dataValidacao;
	}

	@Transient
	public String getDataNascimentoFormatada() {
		SimpleDateFormat frmt = new SimpleDateFormat("dd/MM/yyyy");
		if (this.dataNascimento != null) {
			return frmt.format(this.dataNascimento);
		}
		return "01/12/2000";
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

	@Column(name = "in_incapaz", nullable = false)
	@NotNull
	public boolean getIncapaz() {
		return incapaz;
	}

	public void setIncapaz(boolean incapaz) {
		this.incapaz = incapaz;
	}
	
	@ManyToOne
	@JoinColumn(name = "id_pais")	
	public Pais getPaisNascimento() {
		return paisNascimento;
	}
	
	public void setPaisNascimento(Pais paisNascimento) {
		this.paisNascimento = paisNascimento;
	}
	
	@Length(max = 1000)
	@Column(name = "ds_outras_caracteristicas", length = 1000)	
	public String getOutrasCaracteristicasPessoais() {
		return outrasCaracteristicasPessoais;
	}
	
	public void setOutrasCaracteristicasPessoais(
			String outrasCaracteristicasPessoais) {
		this.outrasCaracteristicasPessoais = outrasCaracteristicasPessoais;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pessoaFisica")
	public List<CaracteristicaFisica> getCaracteristicasFisicas() {
		return caracteristicasFisicas;
	}
	
	public void setCaracteristicasFisicas(
			List<CaracteristicaFisica> caracteristicasFisicas) {
		this.caracteristicasFisicas = caracteristicasFisicas;
	}
	
	@Transient
	public String getPessoaStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(getNome() == null || getNome().isEmpty() ? "" : "Nome: " + getNome().toUpperCase() + "\n");
		sb.append(getProfissao() == null ? "" : "Profissão: " + getProfissao().getProfissao().toUpperCase() + "\n");
		sb.append(getEstadoCivil() == null ? "" : "Estado civil: " + getEstadoCivil().toString().toUpperCase() + "\n");
		sb.append(getNumeroCPF() == null || getNumeroCPF().isEmpty() ? "" : "CPF: " + getNumeroCPF());
		String rg = buscaNumeroDocumentoIdentificacao("RG");
		if (StringUtils.isNotBlank(rg)) {
			sb.append("\n" + rg);
			PessoaDocumentoIdentificacao doc = buscaDocumentoIdentificacao("RG");
			sb.append(doc.getOrgaoExpedidor() != null && doc.getOrgaoExpedidor().isEmpty() ? "" : " - " + doc.getOrgaoExpedidor());
			sb.append(doc.getEstado() == null ? "" : "/" + doc.getEstado().getCodEstado());
		}
		return sb.toString();
	}

	@Column(name = "in_brasileiro")
	public Boolean getBrasileiro() {
		return brasileiro;
	}

	public void setBrasileiro(Boolean brasileiro) {
		this.brasileiro = brasileiro;
	}
	
	@Column(name = "ds_nome_social", length = 255)
	@Length(max = 255)
	public String getNomeSocial() {
		return nomeSocial;
	}

	public void setNomeSocial(String nomeSocial) {
		this.nomeSocial = nomeSocial;
	}

	@OneToOne(mappedBy="pessoa", fetch=FetchType.LAZY)
	public PessoaAdvogado getPessoaAdvogado() {
		return pessoaAdvogado;
	}

	public void setPessoaAdvogado(PessoaAdvogado pessoaAdvogado) {
		this.pessoaAdvogado = pessoaAdvogado;
	}

	@OneToOne(mappedBy="pessoa", fetch=FetchType.LAZY)
	public PessoaAssistenteAdvogado getPessoaAssistenteAdvogado() {
		return pessoaAssistenteAdvogado;
	}

	public void setPessoaAssistenteAdvogado(PessoaAssistenteAdvogado pessoaAssistenteAdvogado) {
		this.pessoaAssistenteAdvogado = pessoaAssistenteAdvogado;
	}

	@OneToOne(mappedBy="pessoa", fetch=FetchType.LAZY)
	public PessoaAssistenteProcuradoria getPessoaAssistenteProcuradoria() {
		return pessoaAssistenteProcuradoria;
	}

	public void setPessoaAssistenteProcuradoria(PessoaAssistenteProcuradoria pessoaAssistenteProcuradoria) {
		this.pessoaAssistenteProcuradoria = pessoaAssistenteProcuradoria;
	}

	@OneToOne(mappedBy="pessoa", fetch=FetchType.LAZY)
	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}

	@OneToOne(mappedBy="pessoa", fetch=FetchType.LAZY)
	public PessoaPerito getPessoaPerito() {
		return pessoaPerito;
	}

	public void setPessoaPerito(PessoaPerito pessoaPerito) {
		this.pessoaPerito = pessoaPerito;
	}

	@OneToOne(mappedBy="pessoa", fetch=FetchType.LAZY)
	public PessoaProcurador getPessoaProcurador() {
		return pessoaProcurador;
	}

	public void setPessoaProcurador(PessoaProcurador pessoaProcurador) {
		this.pessoaProcurador = pessoaProcurador;
	}

	@OneToOne(mappedBy="pessoa", fetch=FetchType.LAZY)
	public PessoaServidor getPessoaServidor() {
		return pessoaServidor;
	}

	public void setPessoaServidor(PessoaServidor pessoaServidor) {
		this.pessoaServidor = pessoaServidor;
	}

	@OneToOne(mappedBy="pessoa", fetch=FetchType.LAZY)
	public PessoaOficialJustica getPessoaOficialJustica() {
		return pessoaOficialJustica;
	}

	public void setPessoaOficialJustica(PessoaOficialJustica pessoaOficialJustica) {
		this.pessoaOficialJustica = pessoaOficialJustica;
	}
	
	@OneToMany(mappedBy = "representante", fetch = FetchType.LAZY)
	public List<CaixaRepresentante> getCaixaRepresentanteList() {
		return caixaRepresentanteList;
	}
	
	public void setCaixaRepresentanteList(
			List<CaixaRepresentante> caixaRepresentanteList) {
		this.caixaRepresentanteList = caixaRepresentanteList;
	}
	
	/**
	 * Atribui a esta pessoa um perfil especializado.
	 * 
	 * @param especializada a pessoa a ser especializada.
	 */
	public <T extends PessoaFisicaEspecializada> void setPessoaEspecializada(T especializada){
		if(especializada instanceof PessoaAdvogado){
			setPessoaAdvogado((PessoaAdvogado) especializada);
			setEspecializacoes(especializacoes | ADV);
		}else if(especializada instanceof PessoaAssistenteAdvogado){
			setPessoaAssistenteAdvogado((PessoaAssistenteAdvogado) especializada);
			setEspecializacoes(especializacoes | ASA);
		}else if(especializada instanceof PessoaAssistenteProcuradoria){
			setPessoaAssistenteProcuradoria((PessoaAssistenteProcuradoria) especializada);
			setEspecializacoes(especializacoes | ASP);
		}else if(especializada instanceof PessoaMagistrado){
			setPessoaMagistrado((PessoaMagistrado) especializada);
			setEspecializacoes(especializacoes | MAG);
		}else if(especializada instanceof PessoaOficialJustica){
			setPessoaOficialJustica((PessoaOficialJustica) especializada);
			setEspecializacoes(especializacoes | OFJ);
		}else if(especializada instanceof PessoaPerito){
			setPessoaPerito((PessoaPerito) especializada);
			setEspecializacoes(especializacoes | PER);
		}else if(especializada instanceof PessoaProcurador){
			setPessoaProcurador((PessoaProcurador) especializada);
			setEspecializacoes(especializacoes | PRO);
		}else if(especializada instanceof PessoaServidor){
			setPessoaServidor((PessoaServidor) especializada);
			setEspecializacoes(especializacoes | SER);
		}else{
			throw new IllegalArgumentException("Repassado tipo de pessoa especializada ainda não tratado.");
		}
	}
	
	public <T extends PessoaFisicaEspecializada> void suprimePessoaEspecializada(T especializada){
		if(especializada instanceof PessoaAdvogado && ((especializacoes & ADV) == ADV)){
			setEspecializacoes(especializacoes ^ ADV);
		}else if(especializada instanceof PessoaAssistenteAdvogado && ((especializacoes & ASA) == ASA)){
			setEspecializacoes(especializacoes ^ ASA);
		}else if(especializada instanceof PessoaAssistenteProcuradoria && ((especializacoes & ASP) == ASP)){
			setEspecializacoes(especializacoes ^ ASP);
		}else if(especializada instanceof PessoaMagistrado && ((especializacoes & MAG) == MAG)){
			setEspecializacoes(especializacoes ^ MAG);
		}else if(especializada instanceof PessoaOficialJustica && ((especializacoes & OFJ) == OFJ)){
			setEspecializacoes(especializacoes ^ OFJ);
		}else if(especializada instanceof PessoaPerito && ((especializacoes & PER) == PER)){
			setEspecializacoes(especializacoes ^ PER);
		}else if(especializada instanceof PessoaProcurador && ((especializacoes & PRO) == PRO)){
			setEspecializacoes(especializacoes ^ PRO);
		}else if(especializada instanceof PessoaServidor && ((especializacoes & SER) == SER)){
			setEspecializacoes(especializacoes ^ SER);
		}
	}
	
	@Basic(fetch=FetchType.EAGER, optional=false)
	@Column(name="in_especializacoes", nullable=false)
	public int getEspecializacoes(){
		return especializacoes;
	}
	
	public void setEspecializacoes(int especializacoes){
		this.especializacoes = especializacoes;
	}
	
	@Transient
	public Boolean getOficialJusticaPessoaAtivo() {
		if((getEspecializacoes() & OFJ) == OFJ && getAtivo()){
			return true;
		}else{
			return false;
		}
	}

	public void setDataCPF(Date dataCPF) {
		this.dataCPF = dataCPF;
	}	
	
	@Transient
	public Date getDataCPF() {
		return dataCPF;
	}

	@Transient
	@Override
	public Class<? extends UsuarioLogin> getEntityClass() {
		return PessoaFisica.class;
	}
}