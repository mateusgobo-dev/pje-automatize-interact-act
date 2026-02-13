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
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;


@Entity
@Table(name = Voto.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_voto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_voto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Voto implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Voto,Integer> {

	public static final String TABLE_NAME = "tb_voto";
	private static final long serialVersionUID = 1L;

	private int idVoto;
	private ProcessoTrf processoTrf;
	private OrgaoJulgador orgaoJulgador;
	private SessaoJT sessao;
	private TipoVotoJT tipoVoto;
	private Date dataTipoVoto;
	private Usuario usuarioTipoVoto;
	private Boolean liberacao;
	private Boolean destaque;
	private Boolean impedimentoSuspeicao;
	private Date dataInclusao;
	private Usuario usuarioInclusao;
	private Date dataAlteracao;
	private Usuario usuarioAlteracao;
	private Boolean marcacaoDestaque;
	private Boolean marcacaoDivergencia;
	private Boolean marcacaoObservacao;
	
	public Voto() {
	}

	@Id
	@GeneratedValue(generator = "gen_voto")
	@Column(name = "id_voto", unique = true, nullable = false)
	public int getIdVoto() {
		return this.idVoto;
	}

	public void setIdVoto(int idVoto) {
		this.idVoto = idVoto;
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
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}
	
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
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
	@JoinColumn(name =  "id_tipo_voto")
	public TipoVotoJT getTipoVoto(){
		return tipoVoto;
	}
	
	public void setTipoVoto(TipoVotoJT tipoVoto){
		this.tipoVoto = tipoVoto;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_tipo_voto")
	public Date getDataTipoVoto() {
		return dataTipoVoto;
	}

	public void setDataTipoVoto(Date dataTipoVoto) {
		this.dataTipoVoto = dataTipoVoto;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_tipo_voto")
	public Usuario getUsuarioTipoVoto() {
		return usuarioTipoVoto;
	}

	public void setUsuarioTipoVoto(Usuario usuarioTipoVoto) {
		this.usuarioTipoVoto = usuarioTipoVoto;
	}
	
	@Column(name = "in_liberacao", nullable = false)
	@NotNull
	public Boolean getLiberacao(){
		return liberacao;
	}
	
	public void setLiberacao(Boolean liberacao){
		this.liberacao = liberacao;
	}
	
	@Column(name = "in_destaque", nullable = false)
	@NotNull
	public Boolean getDestaque(){
		return destaque;
	}
	
	public void setDestaque(Boolean destaque){
		this.destaque = destaque;
	}
	
	@Column(name = "in_impedimento_suspeicao", nullable = false)
	@NotNull
	public Boolean getImpedimentoSuspeicao(){
		return impedimentoSuspeicao;
	}
	
	public void setImpedimentoSuspeicao(Boolean impedimentoSuspeicao){
		this.impedimentoSuspeicao = impedimentoSuspeicao;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdVoto();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Voto))
			return false;
		Voto other = (Voto) obj;
		if (getIdVoto() != other.getIdVoto())
			return false;
		return true;
	}
	
	@Column(name = "in_marcacao_destaque", nullable = false)
	@NotNull
	public Boolean getMarcacaoDestaque() {
		return marcacaoDestaque;
	}

	public void setMarcacaoDestaque(Boolean marcacaoDestaque) {
		this.marcacaoDestaque = marcacaoDestaque;
	}
	
	@Column(name = "in_marcacao_divergencia", nullable = false)
	@NotNull
	public Boolean getMarcacaoDivergencia() {
		return marcacaoDivergencia;
	}

	public void setMarcacaoDivergencia(Boolean marcacaoDivergencia) {
		this.marcacaoDivergencia = marcacaoDivergencia;
	}
	
	@Column(name = "in_marcacao_observacao", nullable = false)
	@NotNull
	public Boolean getMarcacaoObservacao() {
		return marcacaoObservacao;
	}

	public void setMarcacaoObservacao(Boolean marcacaoObservacao) {
		this.marcacaoObservacao = marcacaoObservacao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Voto> getEntityClass() {
		return Voto.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdVoto());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
