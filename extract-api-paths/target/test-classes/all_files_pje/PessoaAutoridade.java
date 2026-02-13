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

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Entity
@Table(name = PessoaAutoridade.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_pessoa_autoridade")
@Cacheable
public class PessoaAutoridade extends Pessoa implements java.io.Serializable {

	private static final long serialVersionUID = -3302528353968715529L;

	public static final String TABLE_NAME = "tb_pessoa_autoridade";

	private Integer idPessoaAutoridade;
	private List<AutoridadePublica> autoridadePublicaList = new ArrayList<AutoridadePublica>(0);
	private PessoaJuridica orgaoVinculacao;
		
	
	public PessoaAutoridade() {
		setInTipoPessoa(TipoPessoaEnum.A);
	}

	@Basic(optional=true)
	@Column(name="id_pessoa_autoridade", insertable=false, updatable=false)
	public Integer getIdPessoaAutoridade() {
		return idPessoaAutoridade;
	}
	
	public void setIdPessoaAutoridade(Integer idPessoaAutoridade) {
		this.idPessoaAutoridade = idPessoaAutoridade;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "autoridade")
	public List<AutoridadePublica> getAutoridadePublicaList() {
		return this.autoridadePublicaList;
	}

	public void setAutoridadePublicaList(List<AutoridadePublica> autoridadePublicaList) {
		this.autoridadePublicaList = autoridadePublicaList;
	}

	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_orgao_vinculacao")
	public PessoaJuridica getOrgaoVinculacao() {
		return orgaoVinculacao;
	}

	public void setOrgaoVinculacao(PessoaJuridica orgaoVinculacao) {
		this.orgaoVinculacao = orgaoVinculacao;
	}

	@Transient
	@Override
	public Class<? extends UsuarioLogin> getEntityClass() {
		return PessoaAutoridade.class;
	}
}
