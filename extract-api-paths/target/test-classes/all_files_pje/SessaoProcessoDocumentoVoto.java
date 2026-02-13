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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;

@Entity
@Table(name = SessaoProcessoDocumentoVoto.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_sessao_proc_documento_voto")
@IndexedEntity(id="idSessaoProcessoDocumento", value="voto",
	mappings={
		@Mapping(beanPath="impedimentoSuspeicao", mappedPath="impedimento"),
		@Mapping(beanPath="tipoVoto", mappedPath="tipovoto"),
		@Mapping(beanPath="destaqueSessao", mappedPath="solicitadodestaque"),
		@Mapping(beanPath="processoTrf.idProcessoTrf", mappedPath="idprocesso"),
		@Mapping(beanPath="processoTrf.processo.numeroProcesso", mappedPath="numeroprocesso"),
		@Mapping(beanPath="ojAcompanhado.idOrgaoJulgador", mappedPath="acompanhado"),
		@Mapping(beanPath="sessao", mappedPath="sessao"),
		@Mapping(beanPath="orgaoJulgador.idOrgaoJulgador", mappedPath="orgaoprolator")
})
public class SessaoProcessoDocumentoVoto extends SessaoProcessoDocumento implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 303557067266725334L;

	public static final String TABLE_NAME = "tb_sessao_proc_doc_voto";

	private TipoVoto tipoVoto;
	private OrgaoJulgador ojAcompanhado;
	private boolean impedimentoSuspeicao = false;
	private boolean destaqueSessao = false;
	private boolean checkAcompanhaRelator = false;
	private ProcessoTrf processoTrf;
	private Date dtVoto = new Date();

	private String textoProclamacaoJulgamento;
	private List<SessaoProcessoMultDocsVoto> sessaoProcessoMultDocsVoto = new ArrayList<SessaoProcessoMultDocsVoto>(0);

	public SessaoProcessoDocumentoVoto() {
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj_acompanhado")
	public OrgaoJulgador getOjAcompanhado() {
		return ojAcompanhado;
	}

	public void setOjAcompanhado(OrgaoJulgador ojAcompanhado) {
		this.ojAcompanhado = ojAcompanhado;
	}

	@Column(name = "in_impedimento_suspeicao", nullable = false)
	@NotNull
	public boolean getImpedimentoSuspeicao() {
		return impedimentoSuspeicao;
	}

	public void setImpedimentoSuspeicao(boolean impedimentoSuspeicao) {
		this.impedimentoSuspeicao = impedimentoSuspeicao;
	}

	@Column(name = "in_destaque_sessao", nullable = false)
	@NotNull
	public boolean getDestaqueSessao() {
		return destaqueSessao;
	}

	public void setDestaqueSessao(boolean destaqueSessao) {
		this.destaqueSessao = destaqueSessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_voto")
	public TipoVoto getTipoVoto() {
		return tipoVoto;
	}

	public void setTipoVoto(TipoVoto tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

	@Transient
	public boolean getCheckAcompanhaRelator() {
		return checkAcompanhaRelator;
	}

	public void setCheckAcompanhaRelator(boolean checkAcompanhaRelator) {
		this.checkAcompanhaRelator = checkAcompanhaRelator;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_voto")
	public Date getDtVoto() {
		return dtVoto;
	}

	public void setDtVoto(Date dtVoto) {
		this.dtVoto = dtVoto;
	}

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_texto_proclamacao_julgamento")
	public String getTextoProclamacaoJulgamento() {
		return textoProclamacaoJulgamento;
	}

	public void setTextoProclamacaoJulgamento(String textoProclamacaoJulgamento) {
		this.textoProclamacaoJulgamento = textoProclamacaoJulgamento;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sessaoProcessoDocumentoVoto", cascade=CascadeType.ALL)
	public List<SessaoProcessoMultDocsVoto> getSessaoProcessoMultDocsVoto() {
		return sessaoProcessoMultDocsVoto;
	}
	
	public void setSessaoProcessoMultDocsVoto(
			List<SessaoProcessoMultDocsVoto> sessaoProcessoMultDocsVoto) {
		this.sessaoProcessoMultDocsVoto = sessaoProcessoMultDocsVoto;
	}

	@Transient
	@Override
	public Class<? extends SessaoProcessoDocumento> getEntityClass() {
		return SessaoProcessoDocumentoVoto.class;
	}	
}