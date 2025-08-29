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
package br.jus.pje.nucleo.entidades.acesso;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.log.Ignore;

/**
 * Entidade que representa um token de sessão de upload de documentos.
 * Este token é repassado para a applet de assinatura com o objetivo de 
 * viabilizar que ela possa fazer upload de assinaturas e documentos para
 * o sistema de forma segura.
 * 
 */
@Entity
@Ignore
@Table(name = "tb_hash_session")
@org.hibernate.annotations.GenericGenerator(name = "gen_hash_session", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hash_session"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HashSession implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "gen_hash_session")
	@Column(name = "id_hash_session", nullable = false, updatable = false, unique = true)
	private int idHashSessao;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa", nullable = false)
	private Pessoa pessoa;
	
	@Column(name = "ds_hash", nullable = false, length = 40, unique = true)
	private String hash;
	
	@Column(name = "dt_expiracao", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date expirationDate;
	
	@Column(name = "ds_ip", nullable = false)
	private String ip;

	public void inicializar(){
		resetExpirationDate();
	}

	public int getIdHashSessao(){
		return idHashSessao;
	}

	public void setIdHashSessao(int idHashSessao){
		this.idHashSessao = idHashSessao;
	}

	public void resetExpirationDate(){
		long time = new Date().getTime();
		time += 1000 * 60 * 60;
		this.expirationDate = new Date(time);
	}

	public Pessoa getPessoa(){
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa){
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	public String getHash(){
		return hash;
	}

	public void setHash(String hash){
		this.hash = hash;
	}

	public Date getExpirationDate(){
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate){
		this.expirationDate = expirationDate;
	}

	public boolean isExpired(){
		return new Date().after(expirationDate);
	}

	public String getIp(){
		return ip;
	}

	public void setIp(String ip){
		this.ip = ip;
	}

	public boolean isIpValido(String ip){
		return this.ip.equals(ip);
	}

}
