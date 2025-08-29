package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name="tb_periodo_inativ_caixa_rep")
@org.hibernate.annotations.GenericGenerator(name = "gen_periodo_inativ_caixa_rep", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_periodo_inativ_caixa_rep"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PeriodoInativacaoCaixaRepresentante implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<PeriodoInativacaoCaixaRepresentante,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idPeriodoInativCaixaRep;
	private Date dataFim;
	private Date dataInicio;
	private CaixaAdvogadoProcurador caixaAdvogadoProcurador;

	public PeriodoInativacaoCaixaRepresentante() {
	}

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="gen_periodo_inativ_caixa_rep")
	@Column(name="id_periodo_inativ_caixa_rep")
	public Integer getIdPeriodoInativCaixaRep() {
		return this.idPeriodoInativCaixaRep;
	}

	public void setIdPeriodoInativCaixaRep(Integer idPeriodoInativCaixaRep) {
		this.idPeriodoInativCaixaRep = idPeriodoInativCaixaRep;
	}

	@Column(name="dt_fim")
	public Date getDataFim() {
		return this.dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	@Column(name="dt_inicio")
	public Date getDataInicio() {
		return this.dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_caixa_adv_proc")
	public CaixaAdvogadoProcurador getCaixaAdvogadoProcurador() {
		return this.caixaAdvogadoProcurador;
	}

	public void setCaixaAdvogadoProcurador(CaixaAdvogadoProcurador caixaAdvogadoProcurador) {
		this.caixaAdvogadoProcurador = caixaAdvogadoProcurador;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPeriodoInativCaixaRep();
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
		if (!(obj instanceof PeriodoInativacaoCaixaRepresentante)) {
			return false;
		}
		PeriodoInativacaoCaixaRepresentante other = (PeriodoInativacaoCaixaRepresentante) obj;
		if (getIdPeriodoInativCaixaRep() != other.getIdPeriodoInativCaixaRep()) {
			return false;
		}
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PeriodoInativacaoCaixaRepresentante> getEntityClass() {
		return PeriodoInativacaoCaixaRepresentante.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdPeriodoInativCaixaRep();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
