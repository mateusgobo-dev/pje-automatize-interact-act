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
package br.jus.pje.jt.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;


@Entity
@Table(name = AnotacaoVoto.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_anot_voto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_anotacao_voto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AnotacaoVoto implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AnotacaoVoto,Integer> {

	public static final String TABLE_NAME = "tb_anotacao_voto";
	private static final long serialVersionUID = 1L;

	private int idAnotacaoVoto;
	private ProcessoTrf processoTrf;
	private SessaoJT sessao;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private OrgaoJulgador orgaoJulgador;
	private String anotacao;
	private Date dataInclusao;
	private Usuario usuarioInclusao;
	private Date dataAlteracao;
	private Usuario usuarioAlteracao;
	
	public AnotacaoVoto() {
	}

	@Id
	@GeneratedValue(generator = "gen_anot_voto")
	@Column(name = "id_anotacao_voto", unique = true, nullable = false)
	public int getIdAnotacaoVoto() {
		return this.idAnotacaoVoto;
	}

	public void setIdAnotacaoVoto(int idAnotacaoVoto) {
		this.idAnotacaoVoto = idAnotacaoVoto;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao")
	public SessaoJT getSessao() {
		return sessao;
	}

	public void setSessao(SessaoJT sessao) {
		this.sessao = sessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado", nullable = false)
	@NotNull
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@Column(name = "ds_anotacao")
	public String getAnotacao() {
		return anotacao;
	}

	public void setAnotacao(String anotacao) {
		this.anotacao = anotacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao", nullable = false)
	@NotNull
	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_inclusao", nullable = false)
	@NotNull
	public Usuario getUsuarioInclusao() {
		return usuarioInclusao;
	}

	public void setUsuarioInclusao(Usuario usuarioInclusao) {
		this.usuarioInclusao = usuarioInclusao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_alteracao")
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_alteracao")
	public Usuario getUsuarioAlteracao() {
		return usuarioAlteracao;
	}

	public void setUsuarioAlteracao(Usuario usuarioAlteracao) {
		this.usuarioAlteracao = usuarioAlteracao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AnotacaoVoto> getEntityClass() {
		return AnotacaoVoto.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAnotacaoVoto());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
