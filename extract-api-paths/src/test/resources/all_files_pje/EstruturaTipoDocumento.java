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

import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Entity
@Table(name="tb_estrutura_tipo_documento")
@org.hibernate.annotations.GenericGenerator(name = "gen_estrutura_tipo_doc", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_estrutura_tipo_doc"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class EstruturaTipoDocumento implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<EstruturaTipoDocumento,Integer> {

	private static final long serialVersionUID = 1L;
	
	private int idEstruturaTipoDocumento;
	private EstruturaDocumento estruturaDocumento;
	private TipoProcessoDocumento tipoProcessoDocumento;
	
	@Id
	@GeneratedValue(generator = "gen_estrutura_tipo_doc")
	@Column(name = "id_estrutura_tipo_documento", unique = true, nullable = false)
	public int getIdEstruturaTipoDocumento() {
		return idEstruturaTipoDocumento;
	}
	public void setIdEstruturaTipoDocumento(int idEstruturaTipoDocumento) {
		this.idEstruturaTipoDocumento = idEstruturaTipoDocumento;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estrutura_documento")	
	public EstruturaDocumento getEstruturaDocumento() {
		return estruturaDocumento;
	}
	public void setEstruturaDocumento(EstruturaDocumento estruturaDocumento) {
		this.estruturaDocumento = estruturaDocumento;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_processo_documento")	
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}
	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EstruturaTipoDocumento> getEntityClass() {
		return EstruturaTipoDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEstruturaTipoDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
