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
package br.jus.pje.jt.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Classe do tipo chave primária criada, pois antes quando se utilizava somente
 * o id da audiência existia problemas com o JPA. O problema era quando existia
 * mais de um advogado para a mesma parte, pois a view retornava duas linhas com
 * a mesma chave, na hora do JPA transformar em objetos como os ids eram os
 * mesmos era utilizado sempre o primeiro objeto criado, dando a impressão de
 * itens duplicados.
 * 
 */
@Embeddable
public class AudPartePK implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int idProcessoAudiencia;
	private int idProcessoParte;
	private int row;
	
	public AudPartePK() {
	}
	
	public AudPartePK(int idProcessoAudiencia, int idProcessoParte) {
		super();
		this.idProcessoAudiencia = idProcessoAudiencia;
		this.idProcessoParte = idProcessoParte;
	}

	@Column(name = "id_processo_audiencia", nullable = false, insertable = false, updatable = false)
	public int getIdProcessoAudiencia() {
		return idProcessoAudiencia;
	}

	public void setIdProcessoAudiencia(int idProcessoAudiencia) {
		this.idProcessoAudiencia = idProcessoAudiencia;
	}

	@Column(name = "id_processo_parte", nullable = false, insertable = false, updatable = false)
	public int getIdProcessoParte() {
		return idProcessoParte;
	}

	public void setIdProcessoParte(int idProcessoParte) {
		this.idProcessoParte = idProcessoParte;
	}
	
	@Column(name = "linha", insertable = false, updatable = false)
	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idProcessoAudiencia;
		result = prime * result + idProcessoParte;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AudPartePK other = (AudPartePK) obj;
		if (idProcessoAudiencia != other.idProcessoAudiencia)
			return false;
		if (idProcessoParte != other.idProcessoParte)
			return false;
		if (row != other.row)
			return false;
		return true;
	}
	
}
