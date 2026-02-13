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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.ExigibilidadeAssinaturaEnum;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_tipo_proc_doc_papel")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_proc_doc_papel", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_proc_doc_papel"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoProcessoDocumentoPapel implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoProcessoDocumentoPapel,Integer> {
	private static final long serialVersionUID = -4852948326884850967L;

	private Integer idTipoProcessoDocumentoPapel;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private Papel papel;
	private Boolean obrigatorio = Boolean.TRUE;
	private ExigibilidadeAssinaturaEnum exigibilidade;

	public TipoProcessoDocumentoPapel() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_proc_doc_papel")
	@Column(name = "id_tp_processo_documento_papel", nullable = false, unique = true)
	public Integer getIdTipoProcessoDocumentoPapel() {
		return idTipoProcessoDocumentoPapel;
	}

	public void setIdTipoProcessoDocumentoPapel(Integer idTipoProcessoDocumentoPapel) {
		this.idTipoProcessoDocumentoPapel = idTipoProcessoDocumentoPapel;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_tipo_processo_documento", nullable = false)
	@NotNull
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_papel", nullable = false)
	@NotNull
	public Papel getPapel() {
		return papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	@Column(name = "in_obrigatorio", nullable = false)
	@NotNull
	public Boolean getObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(Boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	/**
	 * Retorna a indicação relativa à exigibilidade da assinatura de um papel no
	 * tipo de documento vinculado. As exigibilidades podem ser:
	 * 
	 * {@link Exigibilidade#S} suficiente: o documento deste tipo é válido se
	 * existe uma assinatura de pessoa que detenha o papel referido.
	 * 
	 * {@link Exigibilidade#O} obrigatória: o documento deste tipo só é válido
	 * se pessoas que tenham todos os papeis tidos como obrigatórios tenham
	 * assinado
	 * 
	 * {@link Exigibilidade#F} facultativo: o documento deste tipo pode ser
	 * assinado por pessoa que tenha esse papel. Não havendo relacionamento
	 * suficiente ou obrigatório, esta assinatura já torna válido o documento.
	 * 
	 * @return o grau de exigibilidade de assinatura de um determinado papel
	 *         para ele ser considerado válido.
	 */
	@Column(name = "in_exigibilidade")
	@Enumerated(EnumType.STRING)
	public ExigibilidadeAssinaturaEnum getExigibilidade() {
		return exigibilidade;
	}

	/**
	 * Atribui um grau de exigibilidade de assinatura de um papel a um
	 * determinado tipo de documento a fim de que ele possa ser considerado
	 * válido. As exigibilidades podem ser:
	 * 
	 * {@link Exigibilidade#S} suficiente: o documento deste tipo é válido se
	 * existe uma assinatura de pessoa que detenha o papel referido.
	 * 
	 * {@link Exigibilidade#O} obrigatória: o documento deste tipo só é válido
	 * se pessoas que tenham todos os papeis tidos como obrigatórios tenham
	 * assinado
	 * 
	 * {@link Exigibilidade#F} facultativo: o documento deste tipo pode ser
	 * assinado por pessoa que tenha esse papel. Não havendo relacionamento
	 * suficiente ou obrigatório, esta assinatura já torna válido o documento.
	 * 
	 * @param exigibilidade
	 *            o grau de {@link Exigibilidade} a ser atribuído
	 */
	public void setExigibilidade(ExigibilidadeAssinaturaEnum exigibilidade) {
		this.exigibilidade = exigibilidade;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getIdTipoProcessoDocumentoPapel() == null) {
			return false;
		}
		if (!(obj instanceof TipoProcessoDocumentoPapel)) {
			return false;
		}
		TipoProcessoDocumentoPapel other = (TipoProcessoDocumentoPapel) obj;
		if (other.getIdTipoProcessoDocumentoPapel() == null || 
				(getIdTipoProcessoDocumentoPapel().intValue() != other.getIdTipoProcessoDocumentoPapel().intValue())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoProcessoDocumentoPapel();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoProcessoDocumentoPapel> getEntityClass() {
		return TipoProcessoDocumentoPapel.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdTipoProcessoDocumentoPapel();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
