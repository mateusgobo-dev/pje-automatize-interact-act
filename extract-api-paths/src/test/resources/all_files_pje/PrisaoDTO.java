package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class PrisaoDTO implements Serializable{
	private static final long serialVersionUID = 1L;

	private String id;
	private TipoPrisaoDTO tipoPrisao;
	private MotivoPrisaoDTO motivoPrisao;
	private Date dtPrisao;
	private Integer prazoDias;
	private Integer prazoMeses;
	private Integer prazoAnos;
	private UnidadePrisionalDTO unidadePrisional;

	public PrisaoDTO() {
		super();
	}
	
	public PrisaoDTO(String id, TipoPrisaoDTO tipoPrisao, MotivoPrisaoDTO motivoPrisao,
			Date dtPrisao, Integer prazoDias, Integer prazoMeses, Integer prazoAnos,
			UnidadePrisionalDTO unidadePrisional) {
		super();
		if(id == null){
			this.id = UUID.randomUUID().toString();
		} else {
			this.id = id;			
		}
		this.tipoPrisao = tipoPrisao;
		this.motivoPrisao = motivoPrisao;
		this.dtPrisao = dtPrisao;
		this.prazoDias = prazoDias;
		this.prazoMeses = prazoMeses;
		this.prazoAnos = prazoAnos;
		this.unidadePrisional = unidadePrisional;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public TipoPrisaoDTO getTipoPrisao() {
		return tipoPrisao;
	}

	public void setTipoPrisao(TipoPrisaoDTO tipoPrisao) {
		this.tipoPrisao = tipoPrisao;
	}

	public MotivoPrisaoDTO getMotivoPrisao() {
		return motivoPrisao;
	}

	public void setMotivoPrisao(MotivoPrisaoDTO motivoPrisao) {
		this.motivoPrisao = motivoPrisao;
	}

	public Date getDtPrisao() {
		return dtPrisao;
	}

	public void setDtPrisao(Date dtPrisao) {
		this.dtPrisao = dtPrisao;
	}

	public UnidadePrisionalDTO getUnidadePrisional() {
		return unidadePrisional;
	}

	public void setUnidadePrisional(UnidadePrisionalDTO unidadePrisional) {
		this.unidadePrisional = unidadePrisional;
	}

	public Integer getPrazoDias() {
		return prazoDias;
	}

	public void setPrazoDias(Integer prazoDias) {
		this.prazoDias = prazoDias;
	}

	public Integer getPrazoMeses() {
		return prazoMeses;
	}

	public void setPrazoMeses(Integer prazoMeses) {
		this.prazoMeses = prazoMeses;
	}

	public Integer getPrazoAnos() {
		return prazoAnos;
	}

	public void setPrazoAnos(Integer prazoAnos) {
		this.prazoAnos = prazoAnos;
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
		PrisaoDTO other = (PrisaoDTO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}	
	
}
