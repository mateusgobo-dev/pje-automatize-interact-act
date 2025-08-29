package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_pess_proc_jurisdicao")
@org.hibernate.annotations.GenericGenerator(name = "gen_pess_proc_jurisdicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pess_proc_jurisdicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaProcuradoriaJurisdicao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaProcuradoriaJurisdicao,Integer>{

	private static final long serialVersionUID = 1L;
	private Integer idPessoaProcuradoriaJurisdicao;
	private Jurisdicao jurisdicao;
	private PessoaProcuradoria pessoaProcuradoria;
	private boolean ativo;
	
	@Id
	@GeneratedValue(generator = "gen_pess_proc_jurisdicao")
	@Column(name = "id_pess_proc_jurisdicao", unique = true, nullable = false)
	public Integer getIdPessoaProcuradoriaJurisdicao() {
		return idPessoaProcuradoriaJurisdicao;
	}
	
	public void setIdPessoaProcuradoriaJurisdicao(
			Integer idPessoaProcuradoriaJurisdicao) {
		this.idPessoaProcuradoriaJurisdicao = idPessoaProcuradoriaJurisdicao;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_jurisdicao")
	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}
	
	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_procuradoria")
	public PessoaProcuradoria getPessoaProcuradoria() {
		return pessoaProcuradoria;
	}
	
	public void setPessoaProcuradoria(PessoaProcuradoria pessoaProcuradoria) {
		this.pessoaProcuradoria = pessoaProcuradoria;
	}
	
	@Column(name = "in_ativo")
	public boolean isAtivo() {
		return ativo;
	}
	
	public void setAtivo(boolean ativo) {
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
		if (!(obj instanceof PessoaProcuradoriaJurisdicao)) {
			return false;
		}
		PessoaProcuradoriaJurisdicao other = (PessoaProcuradoriaJurisdicao) obj;
		if (getIdPessoaProcuradoriaJurisdicao() != other.getIdPessoaProcuradoriaJurisdicao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaProcuradoriaJurisdicao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaProcuradoriaJurisdicao> getEntityClass() {
		return PessoaProcuradoriaJurisdicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdPessoaProcuradoriaJurisdicao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
