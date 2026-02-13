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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;


@Entity
@Table(name = PessoaAssistenteProcuradoriaLocal.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_pessoa_assist_proc_local")
public class PessoaAssistenteProcuradoriaLocal extends UsuarioLocalizacao implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pess_assist_proc_local";
	private static final long serialVersionUID = 1L;

	private Date dataPosse;
	private boolean assinaDigitalmente = false;
	private Procuradoria procuradoria;
	private TipoProcuradoriaEnum tipo = TipoProcuradoriaEnum.P;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_posse")
	public Date getDataPosse() {
		return this.dataPosse;
	}

	public void setDataPosse(Date dataPosse) {
		this.dataPosse = dataPosse;
	}

	@Transient
	public boolean getAssinaDigitalmente() {
		return this.assinaDigitalmente;
	}

	public void setAssinaDigitalmente(boolean assinaDigitalmente) {
		this.assinaDigitalmente = assinaDigitalmente;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_procuradoria")
	public Procuradoria getProcuradoria() {
		return this.procuradoria;
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}
	
	@Column(name = "in_tipo_procuradoria", length = 1)
	@Enumerated(EnumType.STRING)
	@NotNull
	public TipoProcuradoriaEnum getTipo() {
		return tipo;
	}
	
	public void setTipo(TipoProcuradoriaEnum tipo) {
		this.tipo = tipo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(UsuarioLocalizacao.class.isAssignableFrom(obj.getClass()))) {
			return false;
		}
		Integer idObj = (Integer) ((UsuarioLocalizacao) obj).getIdUsuarioLocalizacao();
		if (idObj == null || !idObj.equals(getIdUsuarioLocalizacao())) {
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

	@Transient
	@Override
	public Class<? extends UsuarioLocalizacao> getEntityClass() {
		return PessoaAssistenteProcuradoriaLocal.class;
	}
}