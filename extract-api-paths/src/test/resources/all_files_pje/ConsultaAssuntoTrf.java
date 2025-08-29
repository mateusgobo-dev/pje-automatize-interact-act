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
package br.jus.pje.nucleo.entidades.ws.consulta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = ConsultaAssuntoTrf.TABLE_NAME)
public class ConsultaAssuntoTrf implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ConsultaAssuntoTrf,Integer> {

	public static final String TABLE_NAME = "tb_assunto_trf";
	private static final long serialVersionUID = 1L;

	private int idAssuntoTrf;
	private String codAssuntoTrf;
	private String assuntoTrf;

	public ConsultaAssuntoTrf() {
	}

	@Id
	@Column(name = "id_assunto_trf", unique = true, insertable = false)
	public int getIdAssuntoTrf() {
		return this.idAssuntoTrf;
	}

	public void setIdAssuntoTrf(int idAssuntoTrf) {
		this.idAssuntoTrf = idAssuntoTrf;
	}

	@Column(name = "cd_assunto_trf", insertable = false)
	public String getCodAssuntoTrf() {
		return this.codAssuntoTrf;
	}

	public void setCodAssuntoTrf(String codAssuntoTrf) {
		this.codAssuntoTrf = codAssuntoTrf;
	}

	@Column(name = "ds_assunto_trf", insertable = false)
	public String getAssuntoTrf() {
		return this.assuntoTrf;
	}

	public void setAssuntoTrf(String assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	@Override
	public String toString() {
		return codAssuntoTrf + " - " + assuntoTrf;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ConsultaAssuntoTrf> getEntityClass() {
		return ConsultaAssuntoTrf.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAssuntoTrf());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
