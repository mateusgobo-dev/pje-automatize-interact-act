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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OrderBy;

import br.jus.pje.nucleo.entidades.identidade.Papel;

@Entity
@Table(name = "tb_usuario_localizacao", uniqueConstraints = @UniqueConstraint(columnNames = {
		"id_usuario", "id_papel", "id_localizacao_fisica", "id_localizacao_modelo" }))
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_usuario_localizacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_usuario_localizacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class UsuarioLocalizacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<UsuarioLocalizacao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idUsuarioLocalizacao;
	private Usuario usuario;
	private Boolean responsavelLocalizacao;
	private Papel papel;
	private Localizacao localizacaoFisica;
	private Localizacao localizacaoModelo;
	private UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor;
	private List<Lembrete> lembretes;

	public UsuarioLocalizacao() {
		super();
	}

	@Id
	@GeneratedValue(generator = "gen_usuario_localizacao")
	@Column(name = "id_usuario_localizacao", unique = true, nullable = false)
	public int getIdUsuarioLocalizacao() {
		return this.idUsuarioLocalizacao;
	}

	public void setIdUsuarioLocalizacao(int idUsuarioLocalizacao) {
		this.idUsuarioLocalizacao = idUsuarioLocalizacao;
	}

	@ManyToOne
	@JoinColumn(name = "id_localizacao_fisica", nullable = false)
	@OrderBy(clause = "nr_faixa_inferior")
	@NotNull
	public Localizacao getLocalizacaoFisica() {
		return this.localizacaoFisica;
	}

	public void setLocalizacaoFisica(Localizacao localizacaoFisica) {
		this.localizacaoFisica = localizacaoFisica;
	}

	@ManyToOne
	@JoinColumn(name = "id_papel")
	public Papel getPapel() {
		return papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = false)
	@NotNull
	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Column(name = "in_responsavel_localizacao", nullable = false)
	@NotNull
	public Boolean getResponsavelLocalizacao() {
		return this.responsavelLocalizacao;
	}

	public void setResponsavelLocalizacao(Boolean responsavelLocalizacao) {
		this.responsavelLocalizacao = responsavelLocalizacao;
	}

	@ManyToOne
	@JoinColumn(name = "id_localizacao_modelo")
	public Localizacao getLocalizacaoModelo() {
		return localizacaoModelo;
	}

	public void setLocalizacaoModelo(Localizacao localizacaoModelo) {
		this.localizacaoModelo = localizacaoModelo;
	}
	
	@OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_usuario_localizacao")
	public UsuarioLocalizacaoMagistradoServidor getUsuarioLocalizacaoMagistradoServidor() {
		return usuarioLocalizacaoMagistradoServidor;
	}
	
	public void setUsuarioLocalizacaoMagistradoServidor(
			UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor) {
		this.usuarioLocalizacaoMagistradoServidor = usuarioLocalizacaoMagistradoServidor;
	}
	
	@OneToMany(mappedBy = "usuarioLocalizacao", cascade = CascadeType.ALL, orphanRemoval = true)
	public List<Lembrete> getLembretes() {
		return lembretes;
	}

	public void setLembretes(List<Lembrete> lembretes) {
		this.lembretes = lembretes;
	}

	@Override
	public String toString() {
		if(getUsuarioLocalizacaoMagistradoServidor() != null){
			return usuarioLocalizacaoMagistradoServidor.toString();
		}else{
			try {
				return localizacaoFisica + (localizacaoModelo == null ? "" : " / " + localizacaoModelo)  + " / " + papel;
			} catch (Exception e) {
				return super.toString();
			}
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
		if (!(obj instanceof UsuarioLocalizacao)) {
			return false;
		}
		UsuarioLocalizacao other = (UsuarioLocalizacao) obj;
		if (getIdUsuarioLocalizacao() != other.getIdUsuarioLocalizacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdUsuarioLocalizacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends UsuarioLocalizacao> getEntityClass() {
		return UsuarioLocalizacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdUsuarioLocalizacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
