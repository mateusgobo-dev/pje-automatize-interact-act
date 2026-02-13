
/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justi�a
 *
 * A propriedade intelectual deste programa, como c�digo-fonte
 * e como sua deriva��o compilada, pertence � Uni�o Federal,
 * dependendo o uso parcial ou total de autoriza��o expressa do
 * Conselho Nacional de Justi�a.
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = "tb_pessoa_procuradoria")
@org.hibernate.annotations.GenericGenerator(name = "gen_pessoa_procuradoria", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pessoa_procuradoria"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaProcuradoria implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaProcuradoria,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPessoaProcuradoria;
	private Procuradoria procuradoria;
	private PessoaProcurador pessoa;
	private boolean chefeProcuradoria = false;
	private Boolean acompanhaSessao = false;
	private RepresentanteProcessualTipoAtuacaoEnum atuacao = RepresentanteProcessualTipoAtuacaoEnum.P;
	
	private List<PessoaProcuradoriaJurisdicao> pessoaProcuradoriaJurisdicaoList = new ArrayList<PessoaProcuradoriaJurisdicao>(0);
	
		public PessoaProcuradoria() {
	}

	@Id
	@GeneratedValue(generator = "gen_pessoa_procuradoria")
	@Column(name = "id_pessoa_procuradoria", unique = true, nullable = false)
	public int getIdPessoaProcuradoria() {
		return idPessoaProcuradoria;
	}

	public void setIdPessoaProcuradoria(int idPessoaProcuradoria) {
		this.idPessoaProcuradoria = idPessoaProcuradoria;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_procuradoria")
	public Procuradoria getProcuradoria() {
		return procuradoria;
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
	public PessoaProcurador getPessoa() {
		return pessoa;
	}

	public void setPessoa(PessoaProcurador pessoa) {
		this.pessoa = pessoa;
	}


	@Override
	public String toString() {
		return pessoa.getNome();
	}

	@Column(name = "in_chefe_procuradoria")
	public boolean getChefeProcuradoria() {
		return chefeProcuradoria;
	}

	public void setChefeProcuradoria(boolean chefeProcuradoria) {
		this.chefeProcuradoria = chefeProcuradoria;
	}
	
	@Column(name = "in_acompanha_sessao")
	public Boolean getAcompanhaSessao() {
		return acompanhaSessao;
	}
	
	public void setAcompanhaSessao(Boolean acompanhaSessao) {
		this.acompanhaSessao = acompanhaSessao;
	}
	
	@OneToMany(mappedBy = "pessoaProcuradoria", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	public List<PessoaProcuradoriaJurisdicao> getPessoaProcuradoriaJurisdicaoList() {
		return pessoaProcuradoriaJurisdicaoList;
	}

	public void setPessoaProcuradoriaJurisdicaoList(
			List<PessoaProcuradoriaJurisdicao> pessoaProcuradoriaJurisdicaoList) {
		this.pessoaProcuradoriaJurisdicaoList = pessoaProcuradoriaJurisdicaoList;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaProcuradoria)) {
			return false;
		}
		PessoaProcuradoria other = (PessoaProcuradoria) obj;
		if (getIdPessoaProcuradoria() != other.getIdPessoaProcuradoria()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaProcuradoria();
		return result;
	}
	
	@Transient
	public RepresentanteProcessualTipoAtuacaoEnum getAtuacao() {
		return this.atuacao;
	}
	
	public void setAtuacao(RepresentanteProcessualTipoAtuacaoEnum tipo){
		this.atuacao = tipo;		
	}
	
	@Transient
	public RepresentanteProcessualTipoAtuacaoEnum getAtuacaoReal(){
		if(this.chefeProcuradoria)
			return RepresentanteProcessualTipoAtuacaoEnum.G;
		
		if(this.pessoaProcuradoriaJurisdicaoList.size() > 0)
			return RepresentanteProcessualTipoAtuacaoEnum.D;
		
		return RepresentanteProcessualTipoAtuacaoEnum.P;
	}
	
	/**
 	 * Método utilizado para listar a descrição das jurisdições na grid "procuradorGrid"
 	 * Retorna a descrição(nome) das jurisdições
 	 * 
 	 * @return List<String>
 	 */
 	@Transient
 	public List<String> getJurisdicoesLabel(){
 		List<String> jurisdicoes = new ArrayList<String>();
 		
 		if(this.chefeProcuradoria){
 			jurisdicoes.add(" Todas");
 		}else{
 			for(PessoaProcuradoriaJurisdicao pessoaProcuradoriaJurisdicao: pessoaProcuradoriaJurisdicaoList){
 				jurisdicoes.add(pessoaProcuradoriaJurisdicao.getJurisdicao().getJurisdicao());
 			}
 		}
 		
 		return jurisdicoes;
	}
 	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaProcuradoria> getEntityClass() {
		return PessoaProcuradoria.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaProcuradoria());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
