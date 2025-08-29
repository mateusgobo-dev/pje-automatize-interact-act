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

import br.jus.pje.nucleo.entidades.Usuario;


@Entity
@Table(name = HistoricoTipoVoto.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_tipo_voto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_tipo_voto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoTipoVoto implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoTipoVoto,Integer> {

	public static final String TABLE_NAME = "tb_hist_tipo_voto";
	private static final long serialVersionUID = 1L;

	private int idHistoricoTipoVoto;
	private Voto voto;
	private TipoVotoJT tipoVoto;
	private Date dataTipoVoto;
	private Usuario usuarioTipoVoto;
	
	public HistoricoTipoVoto() {
	}

	@Id
	@GeneratedValue(generator = "gen_hist_tipo_voto")
	@Column(name = "id_hist_tipo_voto", unique = true, nullable = false)
	public int getIdHistoricoTipoVoto() {
		return this.idHistoricoTipoVoto;
	}

	public void setIdHistoricoTipoVoto(int idHistoricoTipoVoto) {
		this.idHistoricoTipoVoto = idHistoricoTipoVoto;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_voto", nullable = false)
	@NotNull
	public Voto getVoto() {
		return voto;
	}

	public void setVoto(Voto voto) {
		this.voto = voto;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_voto")
	public TipoVotoJT getTipoVoto() {
		return tipoVoto;
	}

	public void setTipoVoto(TipoVotoJT tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_tipo_voto", nullable = false)
	@NotNull
	public Date getDataTipoVoto() {
		return dataTipoVoto;
	}

	public void setDataTipoVoto(Date dataTipoVoto) {
		this.dataTipoVoto = dataTipoVoto;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_tipo_voto", nullable = false)
	@NotNull
	public Usuario getUsuarioTipoVoto() {
		return usuarioTipoVoto;
	}

	public void setUsuarioTipoVoto(Usuario usuarioTipoVoto) {
		this.usuarioTipoVoto = usuarioTipoVoto;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoTipoVoto> getEntityClass() {
		return HistoricoTipoVoto.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdHistoricoTipoVoto());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
