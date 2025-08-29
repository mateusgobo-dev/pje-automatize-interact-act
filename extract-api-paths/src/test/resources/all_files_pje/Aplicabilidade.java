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
package br.jus.pje.nucleo.entidades.lancadormovimento;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.nucleo.entidades.AplicacaoClasse;

/**
 * Classe que representa a aplicabilidade de algum elemento em relação ao um
 * orgão da justiça, um sujeito ativo e uma classe.
 */
@Entity
@javax.persistence.Cacheable(true)
@Table(name = Aplicabilidade.TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_aplicabilidade", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_aplicabilidade"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Aplicabilidade implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Aplicabilidade,Long> {

	private static final long serialVersionUID = 4055307053148464886L;

	public static final String TABLE_NAME = "tb_aplicabilidade";

	private Long idAplicabilidade;
	private List<OrgaoJustica> orgaoJusticaList;
	private List<AplicacaoClasse> aplicacaoClasseList;
	private List<SujeitoAtivo> sujeitoAtivoList;
	private Boolean ativo;

	public Aplicabilidade() {
		this.orgaoJusticaList = new ArrayList<OrgaoJustica>();
		this.aplicacaoClasseList = new ArrayList<AplicacaoClasse>();
		this.sujeitoAtivoList = new ArrayList<SujeitoAtivo>();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_aplicabilidade")
	@Column(name = "id_aplicabilidade", unique = true, nullable = false)
	public Long getIdAplicabilidade() {
		return idAplicabilidade;
	}

	public void setIdAplicabilidade(Long idAplicabilidade) {
		this.idAplicabilidade = idAplicabilidade;
	}

	@ManyToMany(cascade = javax.persistence.CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_aplicabilidade_orgao", joinColumns = { @JoinColumn(name = "id_aplicabilidade", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_orgao_justica", nullable = false, updatable = false) })
	@ForeignKey(inverseName = "tb_orgao_justica_fkey", name = "tb_aplicabilidade_orgao_fkey")
	public List<OrgaoJustica> getOrgaoJusticaList() {
		return orgaoJusticaList;
	}

	public void setOrgaoJusticaList(List<OrgaoJustica> orgaoJusticaList) {
		this.orgaoJusticaList = orgaoJusticaList;
	}

	@ManyToMany(cascade = javax.persistence.CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_aplicabilidade_classe", joinColumns = { @JoinColumn(name = "id_aplicabilidade", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_aplicacao_classe", nullable = false, updatable = false) })
	@ForeignKey(inverseName = "tb_aplicacao_classe_fkey", name = "tb_aplicabilidade_classe_fkey")
	public List<AplicacaoClasse> getAplicacaoClasseList() {
		return aplicacaoClasseList;
	}

	public void setAplicacaoClasseList(List<AplicacaoClasse> aplicacaoClasseList) {
		this.aplicacaoClasseList = aplicacaoClasseList;
	}

	@ManyToMany(cascade = javax.persistence.CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_aplicabilidade_sujeito", joinColumns = { @JoinColumn(name = "id_aplicabilidade", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_sujeito_ativo", nullable = false, updatable = false) })
	@ForeignKey(inverseName = "tb_sujeito_ativo_fkey", name = "tb_aplicabilidade_sujeito_fkey")
	public List<SujeitoAtivo> getSujeitoAtivoList() {
		return sujeitoAtivoList;
	}

	public void setSujeitoAtivoList(List<SujeitoAtivo> sujeitoAtivoList) {
		this.sujeitoAtivoList = sujeitoAtivoList;
	}
	
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	/**
	 * @return Retorna a lista de orgaos da justica separados por virgula
	 */
	@Transient
	public String getOrgaos() {
		List<OrgaoJustica> orgaos = this.getOrgaoJusticaList();

		StringBuilder strOrgaos = new StringBuilder();
		boolean primeiro = true;
		for(OrgaoJustica orgao: orgaos) {
			if(primeiro) {
				strOrgaos.append(orgao.getNome());
				primeiro = false;
			} else {
				strOrgaos.append(", ");
				strOrgaos.append(orgao.getNome());
			}
		}
		return strOrgaos.toString();
	}
	
	/**
	 * @return Retorna a lista de instancias separados por virgula
	 */
	@Transient
	public String getInstancias() {
		List<AplicacaoClasse> aplicacoesClasse = this.getAplicacaoClasseList();

		StringBuilder strAplicacaoClasse = new StringBuilder();
		boolean primeiro = true;
		for(AplicacaoClasse aplicacaoClasse: aplicacoesClasse) {
			if(primeiro) {
				strAplicacaoClasse.append(aplicacaoClasse.getAplicacaoClasse());
				primeiro = false;
			} else {
				strAplicacaoClasse.append(", ");
				strAplicacaoClasse.append(aplicacaoClasse.getAplicacaoClasse());
			}
		}
		return strAplicacaoClasse.toString();
	}
	
	/**
	 * @return Retorna a lista de sujeitos ativos separado por virgula
	 */
	@Transient
	public String getSujeitosAtivos() {
		List<SujeitoAtivo> sujeitosAtivos = this.getSujeitoAtivoList();

		StringBuilder strSujeitoAtivo = new StringBuilder();
		boolean primeiro = true;
		for(SujeitoAtivo sujeitoAtivo: sujeitosAtivos) {
			if(primeiro) {
				strSujeitoAtivo.append(sujeitoAtivo.getNome());
				primeiro = false;
			} else {
				strSujeitoAtivo.append(", ");
				strSujeitoAtivo.append(sujeitoAtivo.getNome());
			}
		}
		return strSujeitoAtivo.toString();
	}
	
	@Override
	public String toString() {
		return "Órgãos da Justiça: "+getOrgaos()+"; Instâncias: "+getInstancias()+"; Sujeitos Ativos: "+getSujeitosAtivos();
	}

	@Transient
	public String getDescricaoAplicabilidade() {
		final String SEPARADOR = " - ";
		return getIdAplicabilidade() + SEPARADOR + getOrgaos() + SEPARADOR + getInstancias();
	}	
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Aplicabilidade> getEntityClass() {
		return Aplicabilidade.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdAplicabilidade();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
