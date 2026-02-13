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

import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.entidades.identidade.Papel;

@Entity
@Table(name = ListaEmail.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_lista_email", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_lista_email"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ListaEmail implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ListaEmail,Integer> {

	public final static String TABLE_NAME = "tb_lista_email";

	private static final long serialVersionUID = 1L;

	private int idListaEmail;
	private int idGrupoEmail;
	private Localizacao localizacao;
	private Papel papel;
	private Localizacao estrutura;

	public ListaEmail() {
	}

	@Id
	@GeneratedValue(generator = "gen_lista_email")
	@Column(name = "id_lista_email", unique = true, nullable = false)
	public int getIdListaEmail() {
		return this.idListaEmail;
	}

	public void setIdListaEmail(int id) {
		this.idListaEmail = id;
	}

	@Column(name = "id_grupo_email", unique = true, nullable = false)
	public int getIdGrupoEmail() {
		return this.idGrupoEmail;
	}

	public void setIdGrupoEmail(int id) {
		this.idGrupoEmail = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao")
	public Localizacao getLocalizacao() {
		return this.localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_papel")
	public Papel getPapel() {
		return papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estrutura")
	public Localizacao getEstrutura() {
		return estrutura;
	}

	public void setEstrutura(Localizacao estrutura) {
		this.estrutura = estrutura;
	}

	@Override
	public String toString() {
		String est = "";
		if(estrutura != null) {
			est = estrutura + "/";
		}
		return MessageFormat.format("{0}:{1}-{2}{3}/{4}", idListaEmail, idGrupoEmail, est, localizacao, papel);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ListaEmail)) {
			return false;
		}
		ListaEmail other = (ListaEmail) obj;
		if (getIdListaEmail() != other.getIdListaEmail()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdListaEmail();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ListaEmail> getEntityClass() {
		return ListaEmail.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdListaEmail());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
