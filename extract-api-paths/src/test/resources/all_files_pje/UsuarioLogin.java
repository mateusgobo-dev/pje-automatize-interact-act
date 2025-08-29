/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à  União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades.identidade;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Entity
@Table(name = "tb_usuario_login", uniqueConstraints = @UniqueConstraint(columnNames = "ds_login"))
@NamedQuery(name = "usuarioLogadoByLogin", query = "select u from UsuarioLogin u where u.login = :login")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_usuario_login", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_usuario_login"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@Cacheable
public class UsuarioLogin implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<UsuarioLogin,Integer>{

	private static final long serialVersionUID = -1106347367267119427L;
	private Integer idUsuario;
	private String senha;
	private String email;
	private String login;
	private String nome;
	private String assinatura;
	private String certChain;
	private Boolean ativo = true;
	private String hashAtivacaoSenha;
	private StatusSenhaEnum statusSenha;
	private Date dataValidadeSenha;
	private Set<Papel> papelSet = new TreeSet<Papel>();
	private UsuarioLocalizacao usuarioLocalizacaoInicial;
	private Integer falhasSucessivas = 0;
	private Boolean atualizaSso = Boolean.TRUE;
	private Boolean temCertificado = Boolean.FALSE;

	public UsuarioLogin() {}

	@Id
	@GeneratedValue(generator = "gen_usuario_login")
	@Column(name = "id_usuario", unique = true, nullable = false)
	public Integer getIdUsuario(){
		return this.idUsuario;
	}

	public void setIdUsuario(Integer idUsuario){
		this.idUsuario = idUsuario;
	}

	@Column(name = "ds_senha", length = 100)
	@Length(max = 100)
	public String getSenha(){
		return this.senha;
	}

	public void setSenha(String senha){
		this.senha = senha;
	}

	@Column(name = "ds_email", length = 100)
	@Length(max = 100)
	public String getEmail(){
		return this.email;
	}

	public void setEmail(String email){
		this.email = email;
	}

	@Column(name = "ds_login", unique = true, nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getLogin(){
		return this.login;
	}

	public void setLogin(String login){
		this.login = login;
	}

	@Column(name = "ds_nome", nullable = false, length = 255)
	@NotNull
	@Length(max = 255)
	public String getNome(){
		return this.nome;
	}

	public void setNome(String nome){
		this.nome = nome;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_assinatura_usuario")
	public String getAssinatura(){
		return assinatura;
	}

	public void setAssinatura(String assinatura){
		this.assinatura = assinatura;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_cert_chain_usuario")
	public String getCertChain(){
		return certChain;
	}

	public void setCertChain(String certChain){
		this.certChain = certChain;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo(){
		return this.ativo;
	}

	public void setAtivo(Boolean ativo){
		this.ativo = ativo;
	}
	
	@Column(name="hash_ativacao_senha")
	public String getHashAtivacaoSenha() {
		return hashAtivacaoSenha;
	}
	
	public void setHashAtivacaoSenha(String hashAtivacaoSenha) {
		this.hashAtivacaoSenha = hashAtivacaoSenha;
	}	

	@Column(name = "in_status_senha", length = 1)
	@Enumerated(EnumType.STRING)
	public StatusSenhaEnum getStatusSenha() {
		return statusSenha;
	}
	
	public void setStatusSenha(StatusSenhaEnum statusSenha) {
		this.statusSenha = statusSenha;
	}
	
	@Column(name="dt_validade_senha")
	public Date getDataValidadeSenha() {
		return dataValidadeSenha;
	}
	
	public void setDataValidadeSenha(Date dataValidadeSenha) {
		this.dataValidadeSenha = dataValidadeSenha;
	}

	@ManyToMany
	@JoinTable(name = "tb_usuario_papel", joinColumns = @JoinColumn(name = "id_usuario"), inverseJoinColumns = @JoinColumn(name = "id_papel"))
	@ForeignKey(name = "tb_usuario_papel_usuario_fk", inverseName = "tb_usuario_papel_papel_fk")
	public Set<Papel> getPapelSet(){
		return this.papelSet;
	}

	public void setPapelSet(Set<Papel> papelSet){
		this.papelSet = papelSet;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_usuario_localizacao_inicial")
	public UsuarioLocalizacao getUsuarioLocalizacaoInicial() {
		return usuarioLocalizacaoInicial;
	}

	public void setUsuarioLocalizacaoInicial(UsuarioLocalizacao usuarioLocalizacaoInicial) {
		this.usuarioLocalizacaoInicial = usuarioLocalizacaoInicial;
	}

	@Column(name = "qtd_falhas_sucessivas")
	public Integer getFalhasSucessivas() {
		return falhasSucessivas;
	}

	public void setFalhasSucessivas(Integer falhasSucessivas) {
		this.falhasSucessivas = falhasSucessivas;
	}
	
	@Column(name = "in_atualiza_sso", nullable = false)
	@NotNull
	public Boolean getAtualizaSso() {
		return atualizaSso;
	}
	
	public void setAtualizaSso(Boolean atualizaSso) {
		this.atualizaSso = atualizaSso;
	}

	@Override
	public String toString(){
		return nome;
	}

	@Transient
	public boolean checkCertChain(String certChain){
		if (certChain == null){
			throw new IllegalArgumentException("O parâmetro não deve ser nulo");
		}
		return StringUtil.replaceQuebraLinha(certChain).equals(StringUtil.replaceQuebraLinha(this.certChain));
	}
	
	@Transient
	public int getDiasExpirarSenha(){
		if(this.getDataValidadeSenha() != null){
			Date d1 = null;
			Date d2 = null;
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			
			String sD1 = sdf.format(this.getDataValidadeSenha());
			String sD2 = sdf.format(new Date());
			try {
				 d1 = sdf.parse(sD1);
				 d2 = sdf.parse(sD2);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			if(d1.after(d2) || d2.equals(d1)){
				return (int)((d1.getTime() - d2.getTime()) / (24*60*60*1000));
			}
		}
		
		return -1;
	}
	
	@Transient
	public String getUrlAtivacaoSenha(String urlSistema){
		if(getIdUsuario() != null && getHashAtivacaoSenha() != null && getLogin() != null && urlSistema != null){
			return urlSistema + "/Senha/ativacaoSenha.seam?hashCodigoAtivacao="+getHashAtivacaoSenha()+"&login="+getLogin();
		}
		
		return null;
	}

	@Transient
	public String[] getEmails() {
		if (this.email != null) {
			final String SEPARADOR = ",";
			return this.email.split(SEPARADOR);
		}
		return new String[0];
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getIdUsuario() == null){
			return false;
		}
		Integer idOther = null;
		if(UsuarioLogin.class.isAssignableFrom(obj.getClass())){
			idOther = ((UsuarioLogin) obj).getIdUsuario();
		}else if(PessoaFisicaEspecializada.class.isAssignableFrom(obj.getClass())){
			idOther = ((PessoaFisicaEspecializada) obj).getIdUsuario();
		}else{
			return false;
		}
		if (idOther == null){
			return false;
		}
		return idOther.equals(getIdUsuario());
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdUsuario() == null) ? 0 : getIdUsuario().hashCode());
		return result;
	}
	
	@Transient
	public String getNomeSobrenome(){
		String primeiroNome = "";
		String sobrenome = "";
		if(this.nome.split("\\w+").length > 1){
			primeiroNome = this.nome.substring(0, this.nome.indexOf(' ')).toLowerCase();
			sobrenome = this.nome.substring(this.nome.lastIndexOf(' ')+1).toLowerCase();
		}
		
		return primeiroNome + " " + sobrenome;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends UsuarioLogin> getEntityClass() {
		return UsuarioLogin.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdUsuario();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@Column(name = "in_tem_certificado", nullable = false)
	@NotNull
	public Boolean getTemCertificado() {
		return temCertificado;
	}

	public void setTemCertificado(Boolean temCertificado) {
		this.temCertificado = temCertificado;
	}

}
