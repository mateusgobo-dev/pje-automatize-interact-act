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
package br.jus.pje.je.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entidade representativa dos tipos de eleição possíveis.
 * 
 * @author TSE
 *
 */
@Entity
@Table(name = "tb_tp_eleicao")
public class TipoEleicao implements Serializable {
	
	private static final long serialVersionUID = 8011373415869697139L;
	
	@Id
	@Column(name = "id_tp_eleicao", nullable = false, unique = true)
	private Integer codObjeto;
	
	@Column(name = "descricao", length = 255)
	private String descricao;
	
	/**
	 * Recupera o identificador do tipo de eleição.
	 * 
	 * @return o identificador
	 */
	public Integer getCodObjeto() {
		return codObjeto;
	}
	
	/**
	 * Atribui a este tipo de eleição um identificador.
	 * 
	 * @param codObjeto o identificador a ser atribuído.
	 */
	public void setCodObjeto(Integer codObjeto) {
		this.codObjeto = codObjeto;
	}
	
	/**
	 * Recupera a descrição deste tipo de eleição.
	 * 
	 * @return a descrição.
	 */
	public String getDescricao() {
		return descricao;
	}
	
	/**
	 * Atribui a este tipo de eleição uma descrição.
	 * 
	 * @param descricao a descrição a ser atribuída
	 */
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codObjeto == null) ? 0 : codObjeto.hashCode());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TipoEleicao))
			return false;
		TipoEleicao other = (TipoEleicao) obj;
		if (codObjeto == null) {
			if (other.codObjeto != null)
				return false;
		} else if (!codObjeto.equals(other.codObjeto))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return descricao;
	}

}
