package br.jus.pje.nucleo.beans.criminal;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.jus.pje.nucleo.dto.MotivoSolturaDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SolturaBean extends BaseBean{
	
	private static final long serialVersionUID = 1L;

	private Date dtSoltura;
	private MotivoSolturaDTO motivo;
	private UnidadePrisionalBean unidadePrisional;

	public SolturaBean() {
		super();
	}

	public SolturaBean(String id, Date dtSoltura, MotivoSolturaDTO motivoSoltura, UnidadePrisionalBean unidadePrisional) {
		super(id);
		this.dtSoltura = dtSoltura;
		this.motivo = motivoSoltura;
		this.unidadePrisional = unidadePrisional;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public Date getDtSoltura() {
		return dtSoltura;
	}

	public void setDtSoltura(Date dtSoltura) {
		this.dtSoltura = dtSoltura;
	}


	public MotivoSolturaDTO getMotivo() {
		return motivo;
	}

	public void setMotivo(MotivoSolturaDTO motivo) {
		this.motivo = motivo;
	}

	public UnidadePrisionalBean getUnidadePrisional() {
		return unidadePrisional;
	}

	public void setUnidadePrisional(UnidadePrisionalBean unidadePrisional) {
		this.unidadePrisional = unidadePrisional;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		SolturaBean other = (SolturaBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
