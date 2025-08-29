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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = PrevencaoHostsWebservice.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_prvncao_hosts_webservice", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_prvncao_hosts_webservice"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PrevencaoHostsWebservice implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PrevencaoHostsWebservice,Integer> {

	public static final String TABLE_NAME = "tb_prev_hosts_webservice";
	private static final long serialVersionUID = 1L;

	private int idPrevencaoHostsWebservice;
	private String urlServidor;
	private String host;
	private String hostInternet;

	public PrevencaoHostsWebservice() {
	}

	@Id
	@GeneratedValue(generator = "gen_prvncao_hosts_webservice")
	@Column(name = "id_prevencao_hosts_webservice", unique = true, nullable = false)
	public int getIdPrevencaoHostsWebservice() {
		return this.idPrevencaoHostsWebservice;
	}

	public void setIdPrevencaoHostsWebservice(int idPrevencaoHostsWebservice) {
		this.idPrevencaoHostsWebservice = idPrevencaoHostsWebservice;
	}

	@Column(name = "ds_url_servidor", length = 100, nullable = false)
	@Length(max = 100)
	@NotNull
	public String getUrlServidor() {
		return this.urlServidor;
	}

	public void setUrlServidor(String urlServidor) {
		this.urlServidor = urlServidor;
	}

	@Column(name = "ds_host", length = 100, nullable = false)
	@Length(max = 100)
	@NotNull
	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Column(name = "ds_host_internet", nullable = false, length = 100)
	@Length(max = 100)
	@NotNull
	public String getHostInternet() {
		return this.hostInternet;
	}

	public void setHostInternet(String hostInternet) {
		this.hostInternet = hostInternet;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PrevencaoHostsWebservice)) {
			return false;
		}
		PrevencaoHostsWebservice other = (PrevencaoHostsWebservice) obj;
		if (getIdPrevencaoHostsWebservice() != other.getIdPrevencaoHostsWebservice()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPrevencaoHostsWebservice();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PrevencaoHostsWebservice> getEntityClass() {
		return PrevencaoHostsWebservice.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPrevencaoHostsWebservice());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
