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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.editor.TipoTopicoEnum;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "tb_topico")
@org.hibernate.annotations.GenericGenerator(name = "gen_topico", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_topico"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Topico implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<Topico,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idTopico;
	private String tituloPadrao;
	private String conteudoPadrao;
	private boolean opcional;
	private boolean habilitado = true;
	private boolean somenteLeitura;
	private boolean exibirTitulo = true;
	private TipoTopicoEnum tipoTopico;

	@Id
	@GeneratedValue(generator = "gen_topico")
	@Column(name = "id_topico", unique = true, nullable = false)
	public Integer getIdTopico() {
		return idTopico;
	}

	public void setIdTopico(Integer idTopico) {
		this.idTopico = idTopico;
	}

	@Column(name = "cd_tipo_topico")
	@Enumerated(EnumType.STRING)
	public TipoTopicoEnum getTipoTopico() {
		return tipoTopico;
	}

	public void setTipoTopico(TipoTopicoEnum tipoTopico) {
		this.tipoTopico = tipoTopico;
	}

	@Column(name = "in_opcional", nullable = false)
	@NotNull
	public boolean isOpcional() {
		return opcional;
	}

	public void setOpcional(boolean opcional) {
		this.opcional = opcional;
	}

	@Column(name = "in_habilitado", nullable = false)
	@NotNull
	public boolean isHabilitado() {
		return habilitado;
	}

	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}

	@Column(name = "in_somente_leitura", nullable = false)
	@NotNull
	public boolean isSomenteLeitura() {
		return somenteLeitura;
	}

	public void setSomenteLeitura(boolean somenteLeitura) {
		this.somenteLeitura = somenteLeitura;
	}

	@Column(name = "in_exibir_titulo", nullable = false)
	@NotNull
	public boolean isExibirTitulo() {
		return exibirTitulo;
	}

	public void setExibirTitulo(boolean exibirTitulo) {
		this.exibirTitulo = exibirTitulo;
	}

	@Column(name = "ds_titulo_padrao", nullable = false)
	@NotNull
	public String getTituloPadrao() {
		return tituloPadrao;
	}

	public void setTituloPadrao(String tituloPadrao) {
		this.tituloPadrao = tituloPadrao;
	}

	@Column(name = "ds_conteudo_padrao")
	@NotNull
	public String getConteudoPadrao() {
		return conteudoPadrao;
	}

	public void setConteudoPadrao(String conteudoPadrao) {
		this.conteudoPadrao = conteudoPadrao;
	}

	@Transient
	public boolean isUsoUnico() {
		return false;
	}

	@Transient
	public Topico getItemTopico() {
		return null;
	}
	
	@Transient
	public boolean isItem() {
		return false;
	}
	
	@Transient
	public boolean isConclusao(){
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(tituloPadrao);
		sb.append(" (");
		sb.append(getTipoTopico().getLabel());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((idTopico == null) ? 0 : idTopico.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Topico other = (Topico) obj;
		if (idTopico == null) {
			if (other.idTopico != null)
				return false;
		} else if (!idTopico.equals(other.idTopico))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Topico> getEntityClass() {
		return Topico.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdTopico();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
