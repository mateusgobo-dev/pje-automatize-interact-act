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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;


/**
 * 
 */
@Entity
@Table(name = NotaSessaoBloco.TABLE_NAME)
@SequenceGenerator(allocationSize = 1, name = "gen_nota_sessao_bloco", sequenceName = "sq_tb_nota_sessao_bloco")
public class NotaSessaoBloco implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_nota_sessao_bloco";
	private static final long serialVersionUID = 1L;

	private int idNotaSessaoBloco;
	private BlocoJulgamento bloco;
	private OrgaoJulgador orgaoJulgador;
	private Usuario usuarioCadastro;
	private Date dataCadastro;
	private String notaSessaoBloco;
	private Boolean ativo;

	public NotaSessaoBloco() {
	}

	@Id
	@GeneratedValue(generator = "gen_nota_sessao_bloco")
	@Column(name = "id_nota_sessao_bloco", unique = true, nullable = false)
	public int getIdNotaSessaoBloco() {
		return this.idNotaSessaoBloco;
	}

	public void setIdNotaSessaoBloco(int idNotaSessaoBloco) {
		this.idNotaSessaoBloco = idNotaSessaoBloco;
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
	@JoinColumn(name = "id_usuario_cadastro", nullable = false)
	@NotNull
	public Usuario getUsuarioCadastro() {
		return this.usuarioCadastro;
	}

	public void setUsuarioCadastro(Usuario usuarioCadastro) {
		this.usuarioCadastro = usuarioCadastro;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cadastro", nullable = false)
	@NotNull
	public Date getDataCadastro() {
		return this.dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_nota_sessao_bloco")
	@NotNull
	public String getNotaSessaoBloco() {
		return notaSessaoBloco;
	}

	public void setNotaSessaoBloco(String notaSessaoBloco) {
		this.notaSessaoBloco = notaSessaoBloco;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof NotaSessaoBloco)) {
			return false;
		}
		NotaSessaoBloco other = (NotaSessaoBloco) obj;
		if (getIdNotaSessaoBloco() != other.getIdNotaSessaoBloco()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdNotaSessaoBloco();
		return result;
	}
}