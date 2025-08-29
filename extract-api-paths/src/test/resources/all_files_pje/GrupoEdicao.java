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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author Rodrigo Cartaxo / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class GrupoEdicao
 * @description Classe que representa um grupo de obrigações que foram criadas
 *              como resultado de uma unica acao do usuario. As obrigacoes
 *              pertencentes ao grupo nao podem apenas ser editadas e/ou
 *              excluidas individualmente.
 */

@Entity
@Table(name = "tb_grupo_edicao")
@org.hibernate.annotations.GenericGenerator(name = "gen_grupo_edicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_grupo_edicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class GrupoEdicao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<GrupoEdicao,Long> {

	private static final long serialVersionUID = 1L;

	private Long id;

	private Boolean homologado = Boolean.FALSE;

	private List<ObrigacaoPagar> obrigacaoPagarList = new ArrayList<ObrigacaoPagar>(0);

	@Id
	@GeneratedValue(generator = "gen_grupo_edicao")
	@Column(name = "id_grupo_edicao", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "in_homologado")
	public Boolean getHomologado() {
		return homologado;
	}

	public void setHomologado(Boolean homologado) {
		this.homologado = homologado;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "grupoEdicao", cascade = { CascadeType.PERSIST, CascadeType.REFRESH })
	public List<ObrigacaoPagar> getObrigacaoPagarList() {
		return obrigacaoPagarList;
	}

	public void setObrigacaoPagarList(List<ObrigacaoPagar> obrigacaoPagarList) {
		this.obrigacaoPagarList = obrigacaoPagarList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GrupoEdicao))
			return false;
		GrupoEdicao other = (GrupoEdicao) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends GrupoEdicao> getEntityClass() {
		return GrupoEdicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
