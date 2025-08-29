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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = Jurisdicao.TABLE_NAME)
@IndexedEntity(id="idJurisdicao", value="jurisdicao", 
	mappings={
		@Mapping(beanPath="jurisdicao", mappedPath="descricao"),
		@Mapping(beanPath="numeroOrigem", mappedPath="codigoorigem"),
		@Mapping(beanPath="estado.codEstado", mappedPath="uf"),
		@Mapping(beanPath="orgaoJulgadorPlantao.idOrgaoJulgador", mappedPath="orgaoJulgadorPlantao"),
		@Mapping(beanPath="orgaoJulgadorColegiadoPlantao.idOrgaoJulgadorColegiado", mappedPath="orgaoJulgadorColegiadoPlantao")
	})
@org.hibernate.annotations.GenericGenerator(name = "gen_jurisdicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_jurisdicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Jurisdicao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Jurisdicao,Integer> {

	public static final String TABLE_NAME = "tb_jurisdicao";
	private static final long serialVersionUID = 1L;

	private int idJurisdicao;
	private String jurisdicao;
	private Integer numeroOrigem;
	private AplicacaoClasse aplicacao;
	private Boolean ativo;
	private Boolean isJuridicaoExterna;
	private List<JurisdicaoMunicipio> municipioList = new ArrayList<JurisdicaoMunicipio>(0);
	private List<PessoaProcuradoriaJurisdicao> pessoaProcuradoriaJurisdicaoList = new ArrayList<PessoaProcuradoriaJurisdicao>(0);
	private Estado estado;
	private Integer numeroOrgaoJustica;
	private OrgaoJulgador orgaoJulgadorPlantao;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao;
	private PessoaJuridica pessoaJuridicaSecao;

	public Jurisdicao() {
	}

	@Id
	@GeneratedValue(generator = "gen_jurisdicao")
	@Column(name = "id_jurisdicao", unique = true, nullable = false)
	public int getIdJurisdicao() {
		return this.idJurisdicao;
	}

	public void setIdJurisdicao(int idJurisdicao) {
		this.idJurisdicao = idJurisdicao;
	}

	@Column(name = "ds_jurisdicao", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getJurisdicao() {
		return this.jurisdicao;
	}

	public void setJurisdicao(String jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	@Column(name = "nr_origem")
	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="id_aplicacao")
	public AplicacaoClasse getAplicacao() {
		return aplicacao;
	}

	public void setAplicacao(AplicacaoClasse aplicacao) {
		this.aplicacao = aplicacao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Column(name = "in_usuario_externo_protocola", nullable = false)
	@NotNull
	public Boolean getIsJuridicaoExterna() {
		return this.isJuridicaoExterna;
	}
	
	public void setIsJuridicaoExterna(Boolean isJuridicaoExterna) {
		this.isJuridicaoExterna = isJuridicaoExterna;
	}

	@Override
	public String toString() {
		return jurisdicao;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "jurisdicao")
	public List<JurisdicaoMunicipio> getMunicipioList() {
		return municipioList;
	}

	public void setMunicipioList(List<JurisdicaoMunicipio> municipioList) {
		this.municipioList = municipioList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "jurisdicao")
	public List<PessoaProcuradoriaJurisdicao> getPessoaProcuradoriaJurisdicaoList() {
		return pessoaProcuradoriaJurisdicaoList;
	}

	public void setPessoaProcuradoriaJurisdicaoList(List<PessoaProcuradoriaJurisdicao> pessoaProcuradoriaJurisdicaoList) {
		this.pessoaProcuradoriaJurisdicaoList = pessoaProcuradoriaJurisdicaoList;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estado", nullable = false)
	@NotNull
	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}
	
	@Column(name = "nr_identificacao_orgao_justica")
	public Integer getNumeroOrgaoJustica() {
		return this.numeroOrgaoJustica;
	}

	public void setNumeroOrgaoJustica(Integer numeroOrgaoJustica) {
		this.numeroOrgaoJustica = numeroOrgaoJustica;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_juridica_secao")
	public PessoaJuridica getPessoaJuridicaSecao() {
		return pessoaJuridicaSecao;
	}
	
	public void setPessoaJuridicaSecao(PessoaJuridica pessoaJuridicaSecao) {
		this.pessoaJuridicaSecao = pessoaJuridicaSecao;
	}

	@Transient
	public Municipio getMunicipioSede() {
		for (JurisdicaoMunicipio aux : getMunicipioList()) {
			if (aux.getSede()) {
				return aux.getMunicipio();
			}
		}

		return null;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_plantao", nullable = true)
	public OrgaoJulgador getOrgaoJulgadorPlantao() {
		return orgaoJulgadorPlantao;
	}
	public void setOrgaoJulgadorPlantao(OrgaoJulgador orgaoJulgadorPlantao) {
		this.orgaoJulgadorPlantao = orgaoJulgadorPlantao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado_plantao", nullable = true)
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoPlantao() {
		return orgaoJulgadorColegiadoPlantao;
	}
	public void setOrgaoJulgadorColegiadoPlantao(OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao) {
		this.orgaoJulgadorColegiadoPlantao = orgaoJulgadorColegiadoPlantao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Jurisdicao)) {
			return false;
		}
		Jurisdicao other = (Jurisdicao) obj;
		if (getIdJurisdicao() != other.getIdJurisdicao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdJurisdicao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Jurisdicao> getEntityClass() {
		return Jurisdicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdJurisdicao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
