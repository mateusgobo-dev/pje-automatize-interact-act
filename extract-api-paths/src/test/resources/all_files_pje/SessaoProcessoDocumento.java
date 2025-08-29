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

import java.io.Serializable;

import javax.persistence.CascadeType;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import br.jus.pje.nucleo.enums.TipoInclusaoDocumentoEnum;


@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = "tb_sessao_proc_documento")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_sessao_proc_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sessao_proc_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SessaoProcessoDocumento implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<SessaoProcessoDocumento,Integer> {

	private static final long serialVersionUID = 1L;

	private int idSessaoProcessoDocumento;
	private ProcessoDocumento processoDocumento;
	private Sessao sessao;
	private OrgaoJulgador orgaoJulgador;
	private Boolean liberacao = Boolean.FALSE;
	private TipoInclusaoDocumentoEnum tipoInclusao = TipoInclusaoDocumentoEnum.S;

	@Id
	@GeneratedValue(generator = "gen_sessao_proc_documento")
	@Column(name = "id_sessao_processo_documento", unique = true, nullable = false, updatable = false)
	public int getIdSessaoProcessoDocumento() {
		return idSessaoProcessoDocumento;
	}

	public void setIdSessaoProcessoDocumento(int idSessaoProcessoDocumento) {
		this.idSessaoProcessoDocumento = idSessaoProcessoDocumento;
	}

	@OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_documento")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao")
	public Sessao getSessao() {
		return sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@Column(name = "in_liberacao", nullable = false)
	@NotNull
	public Boolean getLiberacao() {
		return liberacao;
	}

	public void setLiberacao(Boolean liberacao) {
		this.liberacao = liberacao;
	}

	@Column(name = "in_tipo_inclusao", nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull
	public TipoInclusaoDocumentoEnum getTipoInclusao() {
		return tipoInclusao;
	}

	public void setTipoInclusao(TipoInclusaoDocumentoEnum tipoInclusao) {
		this.tipoInclusao = tipoInclusao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SessaoProcessoDocumento)) {
			return false;
		}
		SessaoProcessoDocumento other = (SessaoProcessoDocumento) obj;
		if (getIdSessaoProcessoDocumento() != other.getIdSessaoProcessoDocumento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdSessaoProcessoDocumento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SessaoProcessoDocumento> getEntityClass() {
		return SessaoProcessoDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdSessaoProcessoDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(300)
			.append("SessaoProcessoDocumento(#").append(getIdSessaoProcessoDocumento()).append(' ').append(getTipoInclusao());
		
		if (getOrgaoJulgador()!=null) {
			sb.append(", ").append(getOrgaoJulgador().getIdOrgaoJulgador()).append('-').append(getOrgaoJulgador().getOrgaoJulgador());
		}
		
		if (getSessao()!=null) {
			sb.append(", ").append(getSessao());
		}
		
		if (getProcessoDocumento()!=null) {
			sb.append(", ").append(getProcessoDocumento().getIdProcessoDocumento()).append('-').append(getProcessoDocumento());
			if (getProcessoDocumento().getProcessoDocumentoBin()!=null) {
				sb.append(" ").append(getProcessoDocumento().getProcessoDocumentoBin());
			}
		}
		
		sb.append(')');
		return sb.toString();
	}
}
