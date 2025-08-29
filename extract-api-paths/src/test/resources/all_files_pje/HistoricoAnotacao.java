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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.log.Ignore;
import br.jus.pje.nucleo.enums.editor.NivelVisibilidadeAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusAcolhidoAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusCienciaAnotacao;
import br.jus.pje.nucleo.enums.editor.TipoAnotacao;
import br.jus.pje.nucleo.enums.editor.TipoOperacaoTopicoEnum;

@Entity
@Ignore
@Table(name=HistoricoAnotacao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_anotacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_anotacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoAnotacao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoAnotacao,Integer> {
	
	public static final String TABLE_NAME = "tb_hist_anotacao";

	private static final long serialVersionUID = 1L;
	
	private int idHistoricoAnotacao;
	private Anotacao anotacao;
	private String conteudo;
	private Date dataAlteracao;
	private Usuario usuario;
	private boolean destaque;
	private TipoAnotacao tipoAnotacao;
	private StatusAnotacao statusAnotacao;
	private StatusAcolhidoAnotacao statusAcolhidoAnotacao;
	private StatusCienciaAnotacao statusCienciaAnotacao;
	private NivelVisibilidadeAnotacao nivelVisibilidadeAnotacao;
	private TipoOperacaoTopicoEnum tipoOperacaoTopico;
	
	@Id
	@GeneratedValue(generator = "gen_hist_anotacao")
	@Column(name = "id_hist_anotacao", unique = true, nullable = false)	
	public int getIdHistoricoAnotacao() {
		return idHistoricoAnotacao;
	}
	
	public void setIdHistoricoAnotacao(int idHistoricoAnotacao) {
		this.idHistoricoAnotacao = idHistoricoAnotacao;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_anotacao", nullable = false)
	public Anotacao getAnotacao() {
		return anotacao;
	}
	
	public void setAnotacao(Anotacao anotacao) {
		this.anotacao = anotacao;
	}

	@Column(name="ds_conteudo", nullable=false)
	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	@Column(name="dt_alteracao", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}
	
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_pessoa")	
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	@Column(name = "in_tipo_operacao", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	public TipoOperacaoTopicoEnum getTipoOperacaoTopico() {
		return tipoOperacaoTopico;
	}
	
	public void setTipoOperacaoTopico(TipoOperacaoTopicoEnum tipoOperacaoTopico) {
		this.tipoOperacaoTopico = tipoOperacaoTopico;
	}

	@Column(name = "in_destaque", nullable = false)
	public boolean isDestaque() {
		return destaque;
	}

	public void setDestaque(boolean destaque) {
		this.destaque = destaque;
	}

	@Column(name = "cd_anotacao", nullable = false)
	@Enumerated(EnumType.STRING)
	public TipoAnotacao getTipoAnotacao() {
		return tipoAnotacao;
	}

	public void setTipoAnotacao(TipoAnotacao tipoAnotacao) {
		this.tipoAnotacao = tipoAnotacao;
	}

	@Column(name = "ds_status", nullable = false)
	@Enumerated(EnumType.STRING)
	public StatusAnotacao getStatusAnotacao() {
		return statusAnotacao;
	}

	public void setStatusAnotacao(StatusAnotacao statusAnotacao) {
		this.statusAnotacao = statusAnotacao;
	}

	@Column(name = "in_acolhido")
	@Enumerated(EnumType.STRING)
	public StatusAcolhidoAnotacao getStatusAcolhidoAnotacao() {
		return statusAcolhidoAnotacao;
	}

	public void setStatusAcolhidoAnotacao(StatusAcolhidoAnotacao statusAcolhidoAnotacao) {
		this.statusAcolhidoAnotacao = statusAcolhidoAnotacao;
	}

	@Column(name = "in_ciencia")
	@Enumerated(EnumType.STRING)
	public StatusCienciaAnotacao getStatusCienciaAnotacao() {
		return statusCienciaAnotacao;
	}

	public void setStatusCienciaAnotacao(StatusCienciaAnotacao statusCienciaAnotacao) {
		this.statusCienciaAnotacao = statusCienciaAnotacao;
	}
	
	@Column(name = "cd_visibilidade", nullable = false)
	@Enumerated(EnumType.STRING)
	public NivelVisibilidadeAnotacao getNivelVisibilidadeAnotacao() {
		return nivelVisibilidadeAnotacao;
	}
	
	public void setNivelVisibilidadeAnotacao(NivelVisibilidadeAnotacao nivelVisibilidadeAnotacao) {
		this.nivelVisibilidadeAnotacao = nivelVisibilidadeAnotacao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdHistoricoAnotacao();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HistoricoAnotacao other = (HistoricoAnotacao) obj;
		if (getIdHistoricoAnotacao() != other.getIdHistoricoAnotacao())
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoAnotacao> getEntityClass() {
		return HistoricoAnotacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdHistoricoAnotacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return false;
	}

}
