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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_pess_procrdor_prcrdoria")
@org.hibernate.annotations.GenericGenerator(name = "gen_pess_prcrdor_procradoria", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pess_prcrdor_procradoria"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaProcuradorProcuradoria implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaProcuradorProcuradoria,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idPessoaProcuradorProcuradoria;
	private PessoaProcurador pessoaProcurador;
	private PessoaProcuradoriaEntidade pessoaProcuradoriaEntidade;

	public PessoaProcuradorProcuradoria() {
	}

	@Id
	@GeneratedValue(generator = "gen_pess_prcrdor_procradoria")
	@Column(name = "id_pess_prcrdor_procuradoria", unique = true, nullable = false)
	public Integer getIdPessoaProcuradorProcuradoria() {
		return idPessoaProcuradorProcuradoria;
	}

	public void setIdPessoaProcuradorProcuradoria(Integer idPessoaProcuradorProcuradoria) {
		this.idPessoaProcuradorProcuradoria = idPessoaProcuradorProcuradoria;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_fisica", nullable = false)
	@NotNull
	public PessoaProcurador getPessoaProcurador() {
		return this.pessoaProcurador;
	}

	public void setPessoaProcurador(PessoaProcurador pessoaProcurador) {
		this.pessoaProcurador = pessoaProcurador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pess_procuradoria_entidade", nullable = false)
	@NotNull
	public PessoaProcuradoriaEntidade getPessoaProcuradoriaEntidade() {
		return this.pessoaProcuradoriaEntidade;
	}

	public void setPessoaProcuradoriaEntidade(PessoaProcuradoriaEntidade pessoaProcuradoriaEntidade) {
		this.pessoaProcuradoriaEntidade = pessoaProcuradoriaEntidade;
	}

	@Override
	public String toString() {
		return pessoaProcurador + " - " + pessoaProcuradoriaEntidade;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getIdPessoaProcuradorProcuradoria() == null) {
			return false;
		}
		if (!(obj instanceof PessoaProcuradorProcuradoria)) {
			return false;
		}
		PessoaProcuradorProcuradoria other = (PessoaProcuradorProcuradoria) obj;
		if (!idPessoaProcuradorProcuradoria.equals(other.getIdPessoaProcuradorProcuradoria())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaProcuradorProcuradoria();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaProcuradorProcuradoria> getEntityClass() {
		return PessoaProcuradorProcuradoria.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdPessoaProcuradorProcuradoria();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
