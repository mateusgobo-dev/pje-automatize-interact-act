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
package br.jus.pje.nucleo.entidades.editor;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Usuario;

@Entity
@Table(name = AutoTexto.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_auto_texto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_proc_doc_est_topico"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AutoTexto implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<AutoTexto,Integer> {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_autotexto";

	private Integer idAutoTexto;
	private String descricao;
	private String conteudo;
	private Localizacao localizacao;
	private Usuario usuario;
	private Boolean publico;

	@Id
	@GeneratedValue(generator = "gen_auto_texto")
	@Column(name = "id_autotexto", nullable = false, unique = true)
	public Integer getIdAutoTexto() {
		return idAutoTexto;
	}

	public void setIdAutoTexto(Integer idAutoTexto) {
		this.idAutoTexto = idAutoTexto;
	}

	@Column(name = "ds_autotexto", nullable = false, unique = true, length = 50)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "ds_conteudo_autotexto", nullable = false)
	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_localizacao", nullable = false)
	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "id_usuario", nullable = true)
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Column(name = "in_publico", nullable = false, length = 1)
	public Boolean getPublico() {
		return publico;
	}

	public void setPublico(Boolean publico) {
		this.publico = publico;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descricao == null) ? 0 : descricao.hashCode());
		result = prime * result + ((idAutoTexto == null) ? 0 : idAutoTexto.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AutoTexto)) {
			return false;
		}
		AutoTexto other = (AutoTexto) obj;
		if (descricao == null) {
			if (other.descricao != null) {
				return false;
			}
		} else if (!descricao.equals(other.descricao)) {
			return false;
		}
		if (idAutoTexto == null) {
			if (other.idAutoTexto != null) {
				return false;
			}
		} else if (!idAutoTexto.equals(other.idAutoTexto)) {
			return false;
		}
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AutoTexto> getEntityClass() {
		return AutoTexto.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdAutoTexto();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
