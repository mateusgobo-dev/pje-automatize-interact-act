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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = SessaoComposicaoOrdem.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_sessao_composicao_ordem", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sessao_composicao_ordem"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SessaoComposicaoOrdem implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SessaoComposicaoOrdem,Integer> {

	public static final String TABLE_NAME = "tb_sessao_composicao_ordem";
	private static final long serialVersionUID = 1L;

	private int idSessaoComposicaoOrdem;
	private Sessao sessao;
	private Boolean presidente = Boolean.FALSE;
	private PessoaMagistrado magistradoSubstitutoSessao;
	private PessoaMagistrado magistradoPresenteSessao;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgador orgaoJulgadorRevisor;
	private OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoOrgaoJulgador;
	private OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoRevisor;
	private Boolean magistradoTitularPresenteSessao = Boolean.TRUE;
	
	

	public SessaoComposicaoOrdem() {
	}

	@Id
	@GeneratedValue(generator = "gen_sessao_composicao_ordem")
	@Column(name = "id_sessao_composicao_ordem", unique = true, nullable = false)
	public int getIdSessaoComposicaoOrdem() {
		return this.idSessaoComposicaoOrdem;
	}

	public void setIdSessaoComposicaoOrdem(int idSessaoComposicaoOrdem) {
		this.idSessaoComposicaoOrdem = idSessaoComposicaoOrdem;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_sessao", nullable = false)
	@NotNull
	public Sessao getSessao() {
		return this.sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	@Column(name = "in_presidente", nullable = false)
	@NotNull
	public Boolean getPresidente() {
		return this.presidente;
	}

	public void setPresidente(Boolean presidente) {
		this.presidente = presidente;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_magistrado_subst_sessao")
	public PessoaMagistrado getMagistradoSubstitutoSessao() {
		return this.magistradoSubstitutoSessao;
	}

	public void setMagistradoSubstitutoSessao(PessoaMagistrado magistradoSubstitutoSessao) {
		this.magistradoSubstitutoSessao = magistradoSubstitutoSessao;
	}

	@Column(name = "in_mgto_titul_presente_sessao")
	public Boolean getMagistradoTitularPresenteSessao() {
		return this.magistradoTitularPresenteSessao;
	}

	public void setMagistradoTitularPresenteSessao(Boolean magistradoTitularPresenteSessao) {
		this.magistradoTitularPresenteSessao = magistradoTitularPresenteSessao;
	}
	
	@Transient
	public Boolean getPresenteSessao() {
		return (magistradoTitularPresenteSessao == Boolean.TRUE || magistradoSubstitutoSessao != null);
	}
	
	@Override
	public String toString() {
		return getOrgaoJulgador().getOrgaoJulgador();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_magistrado_presente_sessao")
	public PessoaMagistrado getMagistradoPresenteSessao() {
		return magistradoPresenteSessao;
	}

	public void setMagistradoPresenteSessao(PessoaMagistrado magistradoPresenteSessao) {
		this.magistradoPresenteSessao = magistradoPresenteSessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_revisor")
	public OrgaoJulgador getOrgaoJulgadorRevisor() {
		return orgaoJulgadorRevisor;
	}

	public void setOrgaoJulgadorRevisor(OrgaoJulgador orgaoJulgadorRevisor) {
		this.orgaoJulgadorRevisor = orgaoJulgadorRevisor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_org_julg_clgdo_org_julgador")
	public OrgaoJulgadorColegiadoOrgaoJulgador getOrgaoJulgadorColegiadoOrgaoJulgador() {
		return this.orgaoJulgadorColegiadoOrgaoJulgador;
	}

	public void setOrgaoJulgadorColegiadoOrgaoJulgador(
			OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoOrgaoJulgador) {
		this.orgaoJulgadorColegiadoOrgaoJulgador = orgaoJulgadorColegiadoOrgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_org_julg_colegiado_revisor")
	public OrgaoJulgadorColegiadoOrgaoJulgador getOrgaoJulgadorColegiadoRevisor() {
		return this.orgaoJulgadorColegiadoRevisor;
	}

	public void setOrgaoJulgadorColegiadoRevisor(OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoRevisor) {
		this.orgaoJulgadorColegiadoRevisor = orgaoJulgadorColegiadoRevisor;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SessaoComposicaoOrdem)) {
			return false;
		}
		SessaoComposicaoOrdem other = (SessaoComposicaoOrdem) obj;
		if (getIdSessaoComposicaoOrdem() != other.getIdSessaoComposicaoOrdem()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdSessaoComposicaoOrdem();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SessaoComposicaoOrdem> getEntityClass() {
		return SessaoComposicaoOrdem.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdSessaoComposicaoOrdem());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
