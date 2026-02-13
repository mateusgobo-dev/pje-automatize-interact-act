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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = TipoMateriaDiarioEletronico.TABLE_NAME)
public class TipoMateriaDiarioEletronico implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_tp_mtra_drio_eletronico";

	private int idTipoMateria;
	private String descricaoTipoMateria;

	@Id
	@Column(name = "id_tipo_materia", unique = true, nullable = false)
	public int getIdTipoMateria(){
		return idTipoMateria;
	}

	public void setIdTipoMateria(int idTipoMateria){
		this.idTipoMateria = idTipoMateria;
	}

	@Column(name = "ds_tipo_materia", nullable = false)
	@NotNull
	public String getDescricaoTipoMateria(){
		return descricaoTipoMateria;
	}

	public void setDescricaoTipoMateria(String descricaoTipoMateria){
		this.descricaoTipoMateria = descricaoTipoMateria;
	}
	
//	@Override
//	public String toString(){
//		
//		return idTipoMateria + "-" + descricaoTipoMateria;
//	}

}
