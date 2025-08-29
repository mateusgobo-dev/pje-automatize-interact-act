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

import br.jus.pje.nucleo.entidades.PessoaMagistrado;

@Entity
@Table(name="tb_estru_doc_top_mag")
@org.hibernate.annotations.GenericGenerator(name = "gen_est_doc_top_magistrado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_est_doc_top_magistrado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class EstruturaDocumentoTopicoMagistrado implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<EstruturaDocumentoTopicoMagistrado,Integer> {
	
	private static final long serialVersionUID = 1L;

	private int idEstruturaDocumentoTopicoMagistrado;
	private PessoaMagistrado pessoaMagistrado;
	private EstruturaDocumentoTopico estruturaDocumentoTopico;
	private String conteudo;
	
	@Id
	@GeneratedValue(generator = "gen_est_doc_top_magistrado")
	@Column(name = "id_estru_doc_top_mag", unique = true, nullable = false)
	public int getIdEstruturaDocumentoTopicoMagistrado() {
		return idEstruturaDocumentoTopicoMagistrado;
	}
	
	public void setIdEstruturaDocumentoTopicoMagistrado(
			int idEstruturaDocumentoTopicoMagistrado) {
		this.idEstruturaDocumentoTopicoMagistrado = idEstruturaDocumentoTopicoMagistrado;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_magistrado", nullable = false)
	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}
	
	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estrutura_documento_topico", nullable = false)
	public EstruturaDocumentoTopico getEstruturaDocumentoTopico() {
		return estruturaDocumentoTopico;
	}
	
	public void setEstruturaDocumentoTopico(EstruturaDocumentoTopico estruturaDocumentoTopico) {
		this.estruturaDocumentoTopico = estruturaDocumentoTopico;
	}
	
	@Column(name = "ds_conteudo_topico")
	public String getConteudo() {
		return conteudo;
	}
	
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EstruturaDocumentoTopicoMagistrado> getEntityClass() {
		return EstruturaDocumentoTopicoMagistrado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEstruturaDocumentoTopicoMagistrado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
