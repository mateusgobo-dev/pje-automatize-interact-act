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
import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.editor.NivelVisibilidadeAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusAcolhidoAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusCienciaAnotacao;
import br.jus.pje.nucleo.enums.editor.TipoAnotacao;

@Entity
@Table(name = "tb_anotacao")
@org.hibernate.annotations.GenericGenerator(name = "AnotacaoGenerator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_anotacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Anotacao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "AnotacaoGenerator")
	@Column(name = "id_anotacao", unique = true, nullable = false)
	private Integer idAnotacao;

	@Column(name = "ds_conteudo", nullable = false)
	private String conteudo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_pessoa_criacao", nullable = false)
	private Usuario pessoaCriacao;

	@Column(name = "in_destaque", nullable = false)
	private Boolean destaque;

	@Column(name = "dt_criacao", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataCriacao;

	@Column(name = "dt_alteracao", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataAlteracao;

	@Column(name = "dt_alteracao_status", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataAlteracaoStatus;
	
	@Column(name = "cd_anotacao", nullable = false)
	@Enumerated(EnumType.STRING)
	private TipoAnotacao tipoAnotacao;

	@Column(name = "ds_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private StatusAnotacao statusAnotacao;

	@Column(name = "cd_visibilidade", nullable = false)
	@Enumerated(EnumType.STRING)
	private NivelVisibilidadeAnotacao nivelVisibilidadeAnotacao;

	@Column(name = "in_acolhido")
	@Enumerated(EnumType.STRING)
	private StatusAcolhidoAnotacao statusAcolhidoAnotacao;

	@Column(name = "in_ciencia")
	@Enumerated(EnumType.STRING)
	private StatusCienciaAnotacao statusCienciaAnotacao;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_proc_doc_est_topico", nullable = false)
	private ProcessoDocumentoEstruturadoTopico topico;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_proc_doc_estruturado", nullable = false)
	private ProcessoDocumentoEstruturado documento;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	private OrgaoJulgador orgaoJulgador;
	
	@Transient
	private Integer codigoIdentificador;

	public Integer getIdAnotacao() {
		return idAnotacao;
	}

	public void setIdAnotacao(Integer idAnotacao) {
		this.idAnotacao = idAnotacao;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	public Usuario getPessoaCriacao() {
		return pessoaCriacao;
	}

	public void setPessoaCriacao(Usuario pessoaCriacao) {
		this.pessoaCriacao = pessoaCriacao;
	}

	public Boolean getDestaque() {
		return destaque;
	}

	public void setDestaque(Boolean destaque) {
		this.destaque = destaque;
	}

	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	public TipoAnotacao getTipoAnotacao() {
		return tipoAnotacao;
	}

	public void setTipoAnotacao(TipoAnotacao tipoAnotacao) {
		this.tipoAnotacao = tipoAnotacao;
	}

	public StatusAnotacao getStatusAnotacao() {
		return statusAnotacao;
	}

	public void setStatusAnotacao(StatusAnotacao statusAnotacao) {
		this.statusAnotacao = statusAnotacao;
	}

	public NivelVisibilidadeAnotacao getNivelVisibilidadeAnotacao() {
		return nivelVisibilidadeAnotacao;
	}

	public void setNivelVisibilidadeAnotacao(NivelVisibilidadeAnotacao nivelVisibilidadeAnotacao) {
		this.nivelVisibilidadeAnotacao = nivelVisibilidadeAnotacao;
	}

	public ProcessoDocumentoEstruturado getDocumento() {
		return documento;
	}

	public void setDocumento(ProcessoDocumentoEstruturado documento) {
		this.documento = documento;
	}

	public ProcessoDocumentoEstruturadoTopico getTopico() {
		return topico;
	}

	public void setTopico(ProcessoDocumentoEstruturadoTopico topico) {
		this.topico = topico;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public Date getDataAlteracaoStatus() {
		return dataAlteracaoStatus;
	}
	
	public void setDataAlteracaoStatus(Date dataAlteracaoStatus) {
		this.dataAlteracaoStatus = dataAlteracaoStatus;
	}
	
	public StatusAcolhidoAnotacao getStatusAcolhidoAnotacao() {
		return statusAcolhidoAnotacao;
	}

	public void setStatusAcolhidoAnotacao(StatusAcolhidoAnotacao statusAcolhidoAnotacao) {
		this.statusAcolhidoAnotacao = statusAcolhidoAnotacao;
	}

	public StatusCienciaAnotacao getStatusCienciaAnotacao() {
		return statusCienciaAnotacao;
	}

	public void setStatusCienciaAnotacao(StatusCienciaAnotacao statusCienciaAnotacao) {
		this.statusCienciaAnotacao = statusCienciaAnotacao;
	}
	
	public Integer getCodigoIdentificador() {
		if (codigoIdentificador == null) {
			codigoIdentificador = idAnotacao != null ? idAnotacao : super.hashCode();
		}
		return codigoIdentificador;
	}
	
	@Override
	public int hashCode() {
		if (idAnotacao == null) {
			return super.hashCode();
		}
		final int prime = 31;
		int result = 1;
		result = prime * result + idAnotacao;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Anotacao))
			return false;
		Anotacao other = (Anotacao) obj;
		return other.getCodigoIdentificador().equals(getCodigoIdentificador());
	}
}