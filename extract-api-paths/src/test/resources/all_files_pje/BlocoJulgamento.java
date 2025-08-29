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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.JulgamentoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaBlocoEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Entity
@Table(name = "tb_bloco_julgamento")
@SequenceGenerator(allocationSize = 1, name = "gen_bloco_julgamento", sequenceName = "sq_tb_bloco_julgamento")
public class BlocoJulgamento implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private int idBlocoJulgamento;
	private String blocoJulgamento;
	private String propostaVoto;
	private Date dataCriacao;
	private Date dataAlteracao;
	private Sessao sessao;
	private Boolean ativo = Boolean.TRUE;
	private Boolean agruparOrgaoJulgador = Boolean.TRUE;
	private OrgaoJulgador orgaoJulgadorRelator = null;
	private OrgaoJulgador orgaoJulgadorVencedor = null;
	private TipoSituacaoPautaBlocoEnum situacaoJulgamento = TipoSituacaoPautaBlocoEnum.AJ;
	private TipoVoto votoRelator;
	private String proclamacaoJulgamento;
	private boolean certidaoPresente;
	private boolean certidaoAssinada;
	private String certidaoJulgamento;
	private boolean votacaoRegistrada;
	private JulgamentoEnum julgamentoEnum = JulgamentoEnum.M;

	public BlocoJulgamento() { }

	@Id
	@GeneratedValue(generator = "gen_bloco_julgamento")
	@Column(name = "id_bloco_julgamento ", nullable = false)
	public int getIdBlocoJulgamento() {
		return idBlocoJulgamento;
	}

	public void setIdBlocoJulgamento(int idBlocoJulgamento) {
		this.idBlocoJulgamento = idBlocoJulgamento;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao", nullable = false, updatable = false)
	@NotNull
	public Sessao getSessao() {
		return sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	@Column(name = "ds_bloco_julgamento", length = 50)
	@Length(max = 50)
	public String getBlocoJulgamento() {
		return blocoJulgamento;
	}

	public void setBlocoJulgamento(String blocoJulgamento) {
		this.blocoJulgamento = blocoJulgamento;
	}

	@Column(name = "ds_proposta_voto", length = 50)
	@Length(max = 50)
	public String getPropostaVoto() {
		return propostaVoto;
	}

	public void setPropostaVoto(String propostaVoto) {
		this.propostaVoto = propostaVoto;
	}


	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao")
	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_alteracao")
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}
	
	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Column(name = "tp_situacao_julgamento", length = 2)
	@Enumerated(EnumType.STRING)
	public TipoSituacaoPautaBlocoEnum getSituacaoJulgamento() {
		return situacaoJulgamento;
	}

	public void setSituacaoJulgamento(TipoSituacaoPautaBlocoEnum situacaoJulgamento) {
		this.situacaoJulgamento = situacaoJulgamento;
	}

	@Column(name = "in_agrupar_orgao_julgador")
	public Boolean getAgruparOrgaoJulgador() {
		return agruparOrgaoJulgador;
	}

	public void setAgruparOrgaoJulgador(Boolean agruparOrgaoJulgador) {
		this.agruparOrgaoJulgador = agruparOrgaoJulgador;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_relator", nullable = true, updatable = true)
	public OrgaoJulgador getOrgaoJulgadorRelator() {
		return orgaoJulgadorRelator;
	}

	public void setOrgaoJulgadorRelator(OrgaoJulgador orgaoJulgadorRelator) {
		this.orgaoJulgadorRelator = orgaoJulgadorRelator;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_vencedor", nullable = true, updatable = true)
	public OrgaoJulgador getOrgaoJulgadorVencedor() {
		return orgaoJulgadorVencedor;
	}

	public void setOrgaoJulgadorVencedor(OrgaoJulgador orgaoJulgadorVencedor) {
		this.orgaoJulgadorVencedor = orgaoJulgadorVencedor;
	}


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_voto_relator", nullable = true, updatable = true)
	public TipoVoto getVotoRelator() {
		return votoRelator;
	}

	public void setVotoRelator(TipoVoto votoRelator) {
		this.votoRelator = votoRelator;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_proclamacao_julgamento")
	public String getProclamacaoJulgamento() {
		return proclamacaoJulgamento;
	}
	
	@Column(name = "in_certidao_presente")
	public boolean isCertidaoPresente() {
		return certidaoPresente;
	}

	public void setCertidaoPresente(boolean certidaoPresente) {
		this.certidaoPresente = certidaoPresente;
	}

	@Column(name = "in_certidao_assinada")
	public boolean isCertidaoAssinada() {
		return certidaoAssinada;
	}

	public void setCertidaoAssinada(boolean certidaoAssinada) {
		this.certidaoAssinada = certidaoAssinada;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_certidao_julgamento")
	public String getCertidaoJulgamento() {
		return certidaoJulgamento;
	}

	public void setCertidaoJulgamento(String certidaoJulgamento) {
		this.certidaoJulgamento = certidaoJulgamento;
	}

	@Column(name = "in_votacao_registrada")
	public boolean isVotacaoRegistrada() {
		return votacaoRegistrada;
	}

	public void setVotacaoRegistrada(boolean votacaoRegistrada) {
		this.votacaoRegistrada = votacaoRegistrada;
	}

	@Transient
	public String getProclamacaoJulgamentoFormatada() {
		return StringUtil.replace(getProclamacaoJulgamento(), "\n","<br/>");
	}

	public void setProclamacaoJulgamento(String proclamacaoJulgamento) {
		this.proclamacaoJulgamento = proclamacaoJulgamento;
	}

	@Column(name = "in_julgamento")
	@Enumerated(EnumType.STRING)
	public JulgamentoEnum getJulgamentoEnum() {
		return julgamentoEnum;
	}

	public void setJulgamentoEnum(JulgamentoEnum julgamentoEnum) {
		this.julgamentoEnum = julgamentoEnum;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BlocoJulgamento)) {
			return false;
		}
		BlocoJulgamento other = (BlocoJulgamento) obj;
		if (getIdBlocoJulgamento() != other.getIdBlocoJulgamento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdBlocoJulgamento();
		return result;
	}

	@Override
	public String toString() {
		if (blocoJulgamento != null) {
			return blocoJulgamento;
		}
		return super.toString();
	}
}
