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
@Table(name = AplicacaoClasseTipoProcessoDocumento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_aplic_class_tipo_procdoc", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_aplic_cl_tp_proc_docmnto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AplicacaoClasseTipoProcessoDocumento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AplicacaoClasseTipoProcessoDocumento,Integer> {

	public static final String TABLE_NAME = "tb_ap_cl_tp_proc_documento";
	private static final long serialVersionUID = 1L;

	private Integer idAplicacaoClasseTipoProcessoDocumento;
	private AplicacaoClasse aplicacaoClasse;
	private TipoProcessoDocumento tipoProcessoDocumento;

	public AplicacaoClasseTipoProcessoDocumento() {
	}

	@Id
	@GeneratedValue(generator = "gen_aplic_class_tipo_procdoc")
	@Column(name = "id_aplic_cl_tp_proc_documento", unique = true, nullable = false)
	public Integer getIdAplicacaoClasseTipoProcessoDocumento() {
		return idAplicacaoClasseTipoProcessoDocumento;
	}

	public void setIdAplicacaoClasseTipoProcessoDocumento(Integer idAplicacaoClasseTipoProcessoDocumento) {
		this.idAplicacaoClasseTipoProcessoDocumento = idAplicacaoClasseTipoProcessoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aplicacao_classe", nullable = false)
	@NotNull
	public AplicacaoClasse getAplicacaoClasse() {
		return this.aplicacaoClasse;
	}

	public void setAplicacaoClasse(AplicacaoClasse aplicacaoClasse) {
		this.aplicacaoClasse = aplicacaoClasse;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_processo_documento", nullable = false)
	@NotNull
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return this.tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getIdAplicacaoClasseTipoProcessoDocumento() == null) {
			return false;
		}
		if (!(obj instanceof AplicacaoClasseTipoProcessoDocumento)) {
			return false;
		}
		AplicacaoClasseTipoProcessoDocumento other = (AplicacaoClasseTipoProcessoDocumento) obj;
		if (!idAplicacaoClasseTipoProcessoDocumento.equals(other.getIdAplicacaoClasseTipoProcessoDocumento())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdAplicacaoClasseTipoProcessoDocumento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AplicacaoClasseTipoProcessoDocumento> getEntityClass() {
		return AplicacaoClasseTipoProcessoDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdAplicacaoClasseTipoProcessoDocumento();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
