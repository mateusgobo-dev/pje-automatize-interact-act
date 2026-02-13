package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = ProcessoParteExpedienteCaixaAdvogadoProcurador.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_parte_exp_caixa_adv_proc", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_parte_exp_caixa_adv_proc"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoParteExpedienteCaixaAdvogadoProcurador implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoParteExpedienteCaixaAdvogadoProcurador,Integer> {

	public static final String TABLE_NAME = "tb_proc_parte_exp_caixa_adv_proc";
	private static final long serialVersionUID = 1L;
	
	private Integer idProcessoParteExpedienteCaixaAdvogadoProcurador;
	private ProcessoParteExpediente processoParteExpediente;
	private CaixaAdvogadoProcurador caixaAdvogadoProcurador;
	
	@Id
	@GeneratedValue(generator = "gen_proc_parte_exp_caixa_adv_proc")
	@Column(name = "id_proc_parte_exp_caixa_adv_proc", unique = true, nullable = false)
	public Integer getIdProcessoParteExpedienteCaixaAdvogadoProcurador() {
		return idProcessoParteExpedienteCaixaAdvogadoProcurador;
	}
	
	public void setIdProcessoParteExpedienteCaixaAdvogadoProcurador(Integer idProcessoParteExpedienteCaixaAdvogadoProcurador) {
		this.idProcessoParteExpedienteCaixaAdvogadoProcurador = idProcessoParteExpedienteCaixaAdvogadoProcurador;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte_expediente")
	public ProcessoParteExpediente getProcessoParteExpediente() {
		return processoParteExpediente;
	}
	
	public void setProcessoParteExpediente(ProcessoParteExpediente processoParteExpediente) {
		this.processoParteExpediente = processoParteExpediente;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_caixa_adv_proc")
	public CaixaAdvogadoProcurador getCaixaAdvogadoProcurador() {
		return caixaAdvogadoProcurador;
	}
	
	public void setCaixaAdvogadoProcurador(CaixaAdvogadoProcurador caixaAdvogadoProcurador) {
		this.caixaAdvogadoProcurador = caixaAdvogadoProcurador;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ProcessoParteExpedienteCaixaAdvogadoProcurador))
			return false;
		ProcessoParteExpedienteCaixaAdvogadoProcurador other = (ProcessoParteExpedienteCaixaAdvogadoProcurador) obj;
		if (getCaixaAdvogadoProcurador() == null) {
			if (other.getCaixaAdvogadoProcurador() != null)
				return false;
		} else if (!caixaAdvogadoProcurador.equals(other.getCaixaAdvogadoProcurador()))
			return false;
		if (getIdProcessoParteExpedienteCaixaAdvogadoProcurador() == null) {
			if (other.getIdProcessoParteExpedienteCaixaAdvogadoProcurador() != null)
				return false;
		} else if (!idProcessoParteExpedienteCaixaAdvogadoProcurador.equals(other.getIdProcessoParteExpedienteCaixaAdvogadoProcurador()))
			return false;
		if (getProcessoParteExpediente() == null) {
			if (other.getProcessoParteExpediente() != null)
				return false;
		} else if (!processoParteExpediente.equals(other.getProcessoParteExpediente()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoParteExpedienteCaixaAdvogadoProcurador> getEntityClass() {
		return ProcessoParteExpedienteCaixaAdvogadoProcurador.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdProcessoParteExpedienteCaixaAdvogadoProcurador();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
