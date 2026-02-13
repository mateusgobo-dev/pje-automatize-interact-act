package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.Length;

@Entity
@Table(name = "tb_reserva_horario")
public class ReservaHorario implements Serializable {

	private static final long serialVersionUID = 1L;
	private int idReservaHorario;
	private String dsExpressaoCronInicio;
	private String dsExpressaoCronTermino;
	private String dsTraducaoExpressaoCron;
	private boolean ativo = true;
	private List<Sala> salas;
	
	@SequenceGenerator(name = "generator", sequenceName = "sq_tb_reserva_horario")
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id_reserva_horario", unique = true, nullable = false)
	public int getIdReservaHorario() {
		return idReservaHorario;
	}
	
	public void setIdReservaHorario(int idReservaHorario) {
		this.idReservaHorario = idReservaHorario;
	}
	
	@Column(name = "ds_expressao_cron_inicio", length = 200)
	@Length(max = 200)
	public String getDsExpressaoCronInicio() {
		return dsExpressaoCronInicio;
	}
	
	public void setDsExpressaoCronInicio(String dsExpressaoCronInicio) {
		this.dsExpressaoCronInicio = dsExpressaoCronInicio;
	}
	
	@Column(name = "ds_expressao_cron_termino", length = 200)
	@Length(max = 200)	
	public String getDsExpressaoCronTermino() {
		return dsExpressaoCronTermino;
	}

	public void setDsExpressaoCronTermino(String dsExpressaoCronTermino) {
		this.dsExpressaoCronTermino = dsExpressaoCronTermino;
	}	
	
	@Column(name = "ds_traducao_expressao_cron", length = 600)
	@Length(max = 600)
	public String getDsTraducaoExpressaoCron() {
		return dsTraducaoExpressaoCron;
	}
	
	public void setDsTraducaoExpressaoCron(String dsTraducaoExpressaoCron) {
		this.dsTraducaoExpressaoCron = dsTraducaoExpressaoCron;
	}
	
	@Column(name = "in_ativo")
	public boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(boolean inAtivo) {
		this.ativo = inAtivo;
	}

	@ManyToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_sala_reserva_horario", 
		joinColumns = @JoinColumn(name = "id_reserva_horario", nullable = false, updatable = false), 
		inverseJoinColumns = @JoinColumn(name = "id_sala", nullable = false, updatable = false)
	)
	public List<Sala> getSalas() {
		return salas;
	}

	public void setSalas(List<Sala> salas) {
		this.salas = salas;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ReservaHorario)) {
			return false;
		}
		ReservaHorario other = (ReservaHorario) obj;
		if (getIdReservaHorario() != other.getIdReservaHorario()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 61;
		int result = 1;
		result = prime * result + getIdReservaHorario();
		return result;
	}
	
	@Override
	public String toString(){
		return this.dsTraducaoExpressaoCron;
	}	
}
