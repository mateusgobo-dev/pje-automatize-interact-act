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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;


@Entity
@Table(name = PessoaExpedienteDocIdentificacao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_pess_exp_doc_identfcacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pess_exp_doc_identfcacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaExpedienteDocIdentificacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaExpedienteDocIdentificacao,Integer> {

	public static final String TABLE_NAME = "tb_pess_exp_doc_ident";
	private static final long serialVersionUID = 1L;

	private int idPessoaExpedienteDocIdentificacao;
	private TipoDocumentoIdentificacao tipoDocumento;
	private PessoaExpediente pessoaExpediente;
	private Estado estado;
	private String numeroDocumento;
	private String nome;
	private Boolean usadoFalsamente;
	private Boolean ativo = Boolean.TRUE;
	private Boolean documentoPrincipal;
	private String orgaoExpedidor;
	private Date dataUsadoFalsamente;
	private Date dataExpedicao;

	@Id
	@GeneratedValue(generator = "gen_pess_exp_doc_identfcacao")
	@Column(name = "id_pess_exp_doc_identificacao")
	public int getIdPessoaExpedienteDocIdentificacao() {
		return idPessoaExpedienteDocIdentificacao;
	}

	public void setIdPessoaExpedienteDocIdentificacao(int idPessoaExpedienteDocIdentificacao) {
		this.idPessoaExpedienteDocIdentificacao = idPessoaExpedienteDocIdentificacao;
	}

	@ManyToOne
	@JoinColumn(name = "cd_tp_documento_identificacao")
	@NotNull
	public TipoDocumentoIdentificacao getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumentoIdentificacao tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_exp", nullable = false)
	@NotNull
	public PessoaExpediente getPessoaExpediente() {
		return this.pessoaExpediente;
	}

	public void setPessoaExpediente(PessoaExpediente pessoaExpediente) {
		this.pessoaExpediente = pessoaExpediente;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estado")
	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	@Column(name = "nr_documento_identificacao", length = 30)
	@Length(max = 30)
	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	@Column(name = "nm_pessoa", length = 150)
	@Length(max = 150)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "in_usado_falsamente")
	public Boolean getUsadoFalsamente() {
		return usadoFalsamente;
	}

	public void setUsadoFalsamente(Boolean usadoFalsamente) {
		this.usadoFalsamente = usadoFalsamente;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_principal")
	public Boolean getDocumentoPrincipal() {
		return documentoPrincipal;
	}

	public void setDocumentoPrincipal(Boolean documentoPrincipal) {
		this.documentoPrincipal = documentoPrincipal;
	}

	@Column(name = "ds_orgao_expedidor", length = 50)
	@Length(max = 50)
	public String getOrgaoExpedidor() {
		return orgaoExpedidor;
	}

	public void setOrgaoExpedidor(String orgaoExpedidor) {
		this.orgaoExpedidor = orgaoExpedidor;
	}

	@Column(name = "dt_usado_falsamente")
	public Date getDataUsadoFalsamente() {
		return dataUsadoFalsamente;
	}

	public void setDataUsadoFalsamente(Date dataUsadoFalsamente) {
		this.dataUsadoFalsamente = dataUsadoFalsamente;
	}

	@Column(name = "dt_expedicao")
	public Date getDataExpedicao() {
		return dataExpedicao;
	}

	public void setDataExpedicao(Date dataExpedicao) {
		this.dataExpedicao = dataExpedicao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getNumeroDocumento() == null) ? 0 : numeroDocumento.hashCode());
		result = prime * result + ((getPessoaExpediente() == null) ? 0 : pessoaExpediente.hashCode());
		result = prime * result + ((getTipoDocumento() == null) ? 0 : tipoDocumento.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaExpedienteDocIdentificacao)) {
			return false;
		}
		PessoaExpedienteDocIdentificacao other = (PessoaExpedienteDocIdentificacao) obj;
		if (getNumeroDocumento() == null) {
			if (other.getNumeroDocumento() != null) {
				return false;
			}
		} else if (!numeroDocumento.equals(other.getNumeroDocumento())) {
			return false;
		}
		if (getPessoaExpediente() == null) {
			if (other.getPessoaExpediente() != null) {
				return false;
			}
		} else if (!pessoaExpediente.equals(other.getPessoaExpediente())) {
			return false;
		}
		if (getTipoDocumento() == null) {
			if (other.getTipoDocumento() != null) {
				return false;
			}
		} else if (!tipoDocumento.equals(other.getTipoDocumento())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.nome + " - " + this.numeroDocumento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaExpedienteDocIdentificacao> getEntityClass() {
		return PessoaExpedienteDocIdentificacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaExpedienteDocIdentificacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
