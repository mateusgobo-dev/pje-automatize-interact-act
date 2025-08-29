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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_proc_trf_lcliz_mgstrado")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_trf_usu_loc_mgstrdo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_trf_usu_loc_mgstrdo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoTrfUsuarioLocalizacaoMagistradoServidor implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoTrfUsuarioLocalizacaoMagistradoServidor,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoTrfUsuarioLocalizacaoMagistradoServidor;
	private ProcessoTrf processoTrf;
	private UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor;

	public ProcessoTrfUsuarioLocalizacaoMagistradoServidor() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_trf_usu_loc_mgstrdo")
	@Column(name = "id_proc_trf_usu_loc_magistrado", unique = true, nullable = false)
	public int getIdProcessoTrfUsuarioLocalizacaoMagistradoServidor() {
		return idProcessoTrfUsuarioLocalizacaoMagistradoServidor;
	}

	public void setIdProcessoTrfUsuarioLocalizacaoMagistradoServidor(
			int idProcessoTrfUsuarioLocalizacaoMagistradoServidor) {
		this.idProcessoTrfUsuarioLocalizacaoMagistradoServidor = idProcessoTrfUsuarioLocalizacaoMagistradoServidor;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usu_loc_magistrado_servidor", nullable = false)
	@NotNull
	public UsuarioLocalizacaoMagistradoServidor getUsuarioLocalizacaoMagistradoServidor() {
		return usuarioLocalizacaoMagistradoServidor;
	}

	public void setUsuarioLocalizacaoMagistradoServidor(
			UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor) {
		this.usuarioLocalizacaoMagistradoServidor = usuarioLocalizacaoMagistradoServidor;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoTrfUsuarioLocalizacaoMagistradoServidor)) {
			return false;
		}
		ProcessoTrfUsuarioLocalizacaoMagistradoServidor other = (ProcessoTrfUsuarioLocalizacaoMagistradoServidor) obj;
		if (getIdProcessoTrfUsuarioLocalizacaoMagistradoServidor() != other
				.getIdProcessoTrfUsuarioLocalizacaoMagistradoServidor()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoTrfUsuarioLocalizacaoMagistradoServidor();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoTrfUsuarioLocalizacaoMagistradoServidor> getEntityClass() {
		return ProcessoTrfUsuarioLocalizacaoMagistradoServidor.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoTrfUsuarioLocalizacaoMagistradoServidor());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
