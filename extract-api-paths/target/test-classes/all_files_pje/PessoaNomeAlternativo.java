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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.enums.TipoNomeAlternativoEnum;


@Entity
@Table(name = "tb_pessoa_nome_alternativo")
@IndexedEntity(id="idPessoaNomeAlternativo", value="nomealternativo", owners={"pessoa"},
	mappings={
		@Mapping(beanPath="pessoaNomeAlternativo", mappedPath="nome")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_pessoa_nome_alternativo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pessoa_nome_alternativo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaNomeAlternativo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaNomeAlternativo,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPessoaNomeAlternativo;
	private String pessoaNomeAlternativo;
	private Pessoa pessoa;
	private Usuario usuarioCadastrador;
	private TipoNomeAlternativoEnum tipoNomeAlternativo = TipoNomeAlternativoEnum.O;
	
	// Lista auxiliar contendo os Outros Nomes que deverão exibir o botão excluir na grid correpondente.
	public List<String> listaExclusaoOutrosNomes = new ArrayList<String>();
	
	
	public PessoaNomeAlternativo() {
	}

	@Id
	@GeneratedValue(generator = "gen_pessoa_nome_alternativo")
	@Column(name = "id_pessoa_nome_alternativo", unique = true, nullable = false)
	public int getIdPessoaNomeAlternativo() {
		return this.idPessoaNomeAlternativo;
	}

	public void setIdPessoaNomeAlternativo(int idPessoaNomeAlternativo) {
		this.idPessoaNomeAlternativo = idPessoaNomeAlternativo;
	}

	@Column(name = "ds_pessoa_nome_alternativo", nullable = false, length = 255)
	@NotNull
	@Length(max = 255)
	public String getPessoaNomeAlternativo() {
		return this.pessoaNomeAlternativo;
	}

	public void setPessoaNomeAlternativo(String pessoaNomeAlternativo) {
		this.pessoaNomeAlternativo = pessoaNomeAlternativo;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	public Pessoa getPessoa() {
		return this.pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_cadastrador")
	public Usuario getUsuarioCadastrador() {
		return this.usuarioCadastrador;
	}

	public void setUsuarioCadastrador(Usuario usuarioCadastrador) {
		this.usuarioCadastrador = usuarioCadastrador;
	}
	
	@Column(name = "in_tipo_nome_alternativo", length = 1)
	@Enumerated(EnumType.STRING)
	public TipoNomeAlternativoEnum getTipoNomeAlternativo() {
		return tipoNomeAlternativo;
	}
	
	public void setTipoNomeAlternativo(TipoNomeAlternativoEnum tipoNomeAlternativo) {
		this.tipoNomeAlternativo = tipoNomeAlternativo;
	}

	@Override
	public String toString() {
		return pessoaNomeAlternativo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaNomeAlternativo)) {
			return false;
		}
		PessoaNomeAlternativo other = (PessoaNomeAlternativo) obj;
		if (getPessoaNomeAlternativo() == null) {
			if (other.getPessoaNomeAlternativo() != null) {
				return false;
			}
		} else if (!getPessoaNomeAlternativo().equals(other.getPessoaNomeAlternativo())) {
			return false;
		}
		if (getPessoa() == null) {
			if (other.getPessoa() != null) {
				return false;
			}
		} else {
			return pessoa.equals(other.getPessoa());
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getPessoaNomeAlternativo() == null) ? 0 : getPessoaNomeAlternativo().hashCode());
		result = prime * result + ((getPessoa() == null) ? 0 : pessoa.hashCode());
		return result;
	}

	@Transient
	public List<String> getListaExclusaoOutrosNomes() {
		return listaExclusaoOutrosNomes;
	}
	
	public void setListaExclusaoOutrosNomes(
			List<String> listaExclusaoOutrosNomes) {
		this.listaExclusaoOutrosNomes = listaExclusaoOutrosNomes;
	}
	
	public boolean exibirBotaoExclusaoOutrosNomes(String nomeItemLinha){
		return listaExclusaoOutrosNomes.contains(nomeItemLinha);
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaNomeAlternativo> getEntityClass() {
		return PessoaNomeAlternativo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaNomeAlternativo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
