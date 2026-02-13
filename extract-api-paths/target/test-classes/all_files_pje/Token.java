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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.jus.pje.nucleo.entidades.log.Ignore;

@Ignore
@Entity
@Table(name = "tb_token")
@NamedQueries(value = {@NamedQuery(name = TokenQuery.OBTER_TOKEN, query = TokenQuery.HQL_OBTER_TOKEN)})
public class Token{

	@Id
	@Column(name = "cd_token", nullable = false, updatable = false, length = 30)
	private String id;
	
	@Column(name = "ds_ip")
	private String ip;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao")
	private Date dataCriacao;

	public Token(){
	}

	public Token(String id, String ip){
		this.id = id;
		this.ip = ip;
		dataCriacao = new Date();
	}

	/**
	 * Recupera o identificador desta entidade.
	 * 
	 * @return o identificador
	 */
	public String getId(){
		return id;
	}

	/**
	 * Atribui a esta entidade um identificador.
	 * 
	 * @param id
	 */
	public void setId(String id){
		this.id = id;
	}

	/**
	 * Recupera endereço IP pertinente a este token.
	 *  
	 * @return o endereço IP.
	 */
	public String getIp(){
		return ip;
	}

	/**
	 * Atribui a este token um endereço IP.
	 * 
	 * @param ip o endereço a ser atribuído
	 */
	public void setIp(String ip){
		this.ip = ip;
	}

	/**
	 * Recupera a data de criação deste token.
	 * 
	 * @return a data de criação.
	 */
	public Date getDataCriacao(){
		return dataCriacao;
	}

	/**
	 * Atribui a este token uma data de criação.
	 * 
	 * @param dataCriacao a data a ser atribuída
	 */
	public void setDataCriacao(Date dataCriacao){
		this.dataCriacao = dataCriacao;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return id + ":" + ip;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof Token)
			return false;
		Token other = (Token) obj;
		if (getId() == null){
			if (other.getId() != null)
				return false;
		}
		else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

}