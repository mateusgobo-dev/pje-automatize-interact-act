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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;


/**
 * 
 */
@Entity
@Table(name = VotoBloco.TABLE_NAME)
@SequenceGenerator(allocationSize = 1, name = "gen_voto_bloco", sequenceName = "sq_tb_voto_bloco")
public class VotoBloco implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_voto_bloco";
	private static final long serialVersionUID = 1L;

	private int idVotoBloco;
	private BlocoJulgamento bloco;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgador ojAcompanhado;
	private TipoVoto tipoVoto;
	private Date dataVoto = new Date();

	public VotoBloco() {
	}

	@Id
	@GeneratedValue(generator = "gen_voto_bloco")
	@Column(name = "id_voto_bloco", unique = true, nullable = false)
	public int getIdVotoBloco() {
		return this.idVotoBloco;
	}

	public void setIdVotoBloco(int idVotoBloco) {
		this.idVotoBloco = idVotoBloco;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_bloco_julgamento", nullable = false)
	@NotNull
	public BlocoJulgamento getBloco() {
		return this.bloco;
	}

	public void setBloco(BlocoJulgamento bloco) {
		this.bloco = bloco;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj_acompanhado", nullable = true)
	public OrgaoJulgador getOjAcompanhado() {
		return this.ojAcompanhado;
	}

	public void setOjAcompanhado(OrgaoJulgador ojAcompanhado) {
		this.ojAcompanhado = ojAcompanhado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_voto")
	public TipoVoto getTipoVoto() {
		return tipoVoto;
	}

	public void setTipoVoto(TipoVoto tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_voto", nullable = false)
	@NotNull
	public Date getDataVoto() {
		return this.dataVoto;
	}

	public void setDataVoto(Date dataVoto) {
		this.dataVoto = dataVoto;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof VotoBloco)) {
			return false;
		}
		VotoBloco other = (VotoBloco) obj;
		if (getIdVotoBloco() != other.getIdVotoBloco()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdVotoBloco();
		return result;
	}
}