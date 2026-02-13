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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.editor.Hierarchical;

@Entity
@Table(name = "tb_estrutura_documento_topico")
@org.hibernate.annotations.GenericGenerator(name = "gen_estrutura_doc_topico", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_estrutura_doc_topico"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class EstruturaDocumentoTopico implements Hierarchical, br.jus.pje.nucleo.entidades.IEntidade<EstruturaDocumentoTopico,Integer> {

	private static final long serialVersionUID = 1L;

	private int idEstruturaDocumentoTopico;
	private EstruturaDocumento estruturaDocumento;
	private Topico topico;
	private Integer ordem;
	private Integer nivel;
	private Integer numeracao;
	private boolean numerado = true;
	
	private EstruturaDocumentoTopicoMagistrado estruturaDocumentoTopicoMagistrado;

	@Id
	@GeneratedValue(generator = "gen_estrutura_doc_topico")
	@Column(name = "id_estrutura_documento_topico", unique = true, nullable = false)
	public int getIdEstruturaDocumentoTopico() {
		return idEstruturaDocumentoTopico;
	}

	public void setIdEstruturaDocumentoTopico(int idModeloDocumentoEstruturadoTopico) {
		this.idEstruturaDocumentoTopico = idModeloDocumentoEstruturadoTopico;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_topico", nullable = false)
	public Topico getTopico() {
		return topico;
	}

	public void setTopico(Topico topico) {
		this.topico = topico;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estrutura_documento", nullable = false)
	public EstruturaDocumento getEstruturaDocumento() {
		return estruturaDocumento;
	}

	public void setEstruturaDocumento(EstruturaDocumento estruturaDocumento) {
		this.estruturaDocumento = estruturaDocumento;
	}

	@Override
	@Column(name = "nr_ordem", nullable = false)
	public Integer getOrdem() {
		return ordem;
	}

	@Override
	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	@Override
	@Column(name = "nr_nivel", nullable = false)
	public Integer getNivel() {
		return nivel;
	}

	@Override
	public void setNivel(Integer nivel) {
		this.nivel = nivel;
	}

	@Override
	@Column(name = "nr_numeracao")
	public Integer getNumeracao() {
		return numeracao;
	}

	@Override
	public void setNumeracao(Integer numeracao) {
		this.numeracao = numeracao;
	}

	@Override
	@Column(name = "in_numerado", nullable = false)
	@NotNull
	public boolean isNumerado() {
		return numerado;
	}

	public void setNumerado(boolean numerado) {
		this.numerado = numerado;
	}

	@Transient
	public EstruturaDocumentoTopicoMagistrado getEstruturaDocumentoTopicoMagistrado() {
		return estruturaDocumentoTopicoMagistrado;
	}
	
	public void setEstruturaDocumentoTopicoMagistrado(
			EstruturaDocumentoTopicoMagistrado estruturaDocumentoTopicoMagistrado) {
		this.estruturaDocumentoTopicoMagistrado = estruturaDocumentoTopicoMagistrado;
	}
	
	@Override
	public String toString() {
		return topico != null ? topico.toString() : super.toString();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EstruturaDocumentoTopico> getEntityClass() {
		return EstruturaDocumentoTopico.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEstruturaDocumentoTopico());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
