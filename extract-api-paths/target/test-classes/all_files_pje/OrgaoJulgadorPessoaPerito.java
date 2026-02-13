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

@Entity
@Table(name = "tb_org_julg_pessoa_perito")
@org.hibernate.annotations.GenericGenerator(name = "gen_org_julg_pessoa_perito", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_org_julg_pessoa_perito"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoJulgadorPessoaPerito implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoJulgadorPessoaPerito,Integer> {

	private static final long serialVersionUID = 1L;

	private int idOrgaoJulgadorPessoaPerito;
	private OrgaoJulgador orgaoJulgador;
	private PessoaPerito pessoaPerito;

	@Id
	@GeneratedValue(generator = "gen_org_julg_pessoa_perito")
	@Column(name = "id_org_julgador_pessoa_perito", unique = true, nullable = false)
	public int getIdOrgaoJulgadorPessoaPerito() {
		return idOrgaoJulgadorPessoaPerito;
	}

	public void setIdOrgaoJulgadorPessoaPerito(int idOrgaoJulgadorPessoaPerito) {
		this.idOrgaoJulgadorPessoaPerito = idOrgaoJulgadorPessoaPerito;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa_perito")
	public PessoaPerito getPessoaPerito() {
		return pessoaPerito;
	}

	public void setPessoaPerito(PessoaPerito pessoaPerito) {
		this.pessoaPerito = pessoaPerito;
	}
	
	@Override
	public String toString() {
		if(orgaoJulgador == null){
			return pessoaPerito.getNome();
		}
		else{
			return orgaoJulgador.getOrgaoJulgador();			
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OrgaoJulgadorPessoaPerito)) {
			return false;
		}
		OrgaoJulgadorPessoaPerito other = (OrgaoJulgadorPessoaPerito) obj;
		if (getIdOrgaoJulgadorPessoaPerito() != other.getIdOrgaoJulgadorPessoaPerito()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdOrgaoJulgadorPessoaPerito();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoJulgadorPessoaPerito> getEntityClass() {
		return OrgaoJulgadorPessoaPerito.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdOrgaoJulgadorPessoaPerito());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
