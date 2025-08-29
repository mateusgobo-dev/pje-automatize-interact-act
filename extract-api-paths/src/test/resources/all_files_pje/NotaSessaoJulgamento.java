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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = NotaSessaoJulgamento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_nota_sessao_julgamento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_nota_sessao_julgamento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class NotaSessaoJulgamento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<NotaSessaoJulgamento,Integer> {

	public static final String TABLE_NAME = "tb_nota_sessao_julgamento";
	private static final long serialVersionUID = 1L;

	private int idNotaSessaoJulgamento;
	private Sessao sessao;
	private ProcessoTrf processoTrf;
	private OrgaoJulgador orgaoJulgador;
	private Usuario usuarioCadastro;
	private Date dataCadastro;
	private String notaSessaoJulgamento;
	private Boolean ativo = Boolean.TRUE;

	public NotaSessaoJulgamento() {
	}

	@Id
	@GeneratedValue(generator = "gen_nota_sessao_julgamento")
	@Column(name = "id_nota_sessao_julgamento", unique = true, nullable = false)
	public int getIdNotaSessaoJulgamento() {
		return this.idNotaSessaoJulgamento;
	}

	public void setIdNotaSessaoJulgamento(int idNotaSessaoJulgamento) {
		this.idNotaSessaoJulgamento = idNotaSessaoJulgamento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao", nullable = false)
	@NotNull
	public Sessao getSessao() {
		return this.sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
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
	@Column(name = "ds_nota_sessao_julgamento")
	@NotNull
	public String getNotaSessaoJulgamento() {
		return notaSessaoJulgamento;
	}

	public void setNotaSessaoJulgamento(String notaSessaoJulgamento) {
		this.notaSessaoJulgamento = notaSessaoJulgamento;
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
		if (!(obj instanceof NotaSessaoJulgamento)) {
			return false;
		}
		NotaSessaoJulgamento other = (NotaSessaoJulgamento) obj;
		if (getIdNotaSessaoJulgamento() != other.getIdNotaSessaoJulgamento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdNotaSessaoJulgamento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends NotaSessaoJulgamento> getEntityClass() {
		return NotaSessaoJulgamento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdNotaSessaoJulgamento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
