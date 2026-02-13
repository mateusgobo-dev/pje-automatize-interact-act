package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
@Table(name = "tb_sala_reserva_horario")
public class SalaReservaHorario implements Serializable {

	private static final long serialVersionUID = 1L;
	private int idSalaReservaHorario;
	private Sala sala;
	private ReservaHorario reservaHorario;
	private String identificadorReservaHorario;
	private boolean ativo = true;
	
	@SequenceGenerator(name = "generator", sequenceName = "sq_tb_sala_reserva_horario")
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id_sala_reserva_horario", unique = true, nullable = false)
	public int getIdSalaReservaHorario() {
		return idSalaReservaHorario;
	}
	
	public void setIdSalaReservaHorario(int idSalaReservaHorario) {
		this.idSalaReservaHorario = idSalaReservaHorario;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sala", nullable = false)
	@NotNull	
	public Sala getSala() {
		return sala;
	}
	
	public void setSala(Sala sala) {
		this.sala = sala;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_reserva_horario", nullable = false)
	@NotNull
	public ReservaHorario getReservaHorario() {
		return reservaHorario;
	}
	
	public void setReservaHorario(ReservaHorario reservaHorario) {
		this.reservaHorario = reservaHorario;
	}
	
	@Column(name = "ds_identificador_reserva_horario", length = 30)
	@Length(max = 30)
	public String getIdentificadorReservaHorario() {
		return identificadorReservaHorario;
	}

	public void setIdentificadorReservaHorario(String identificadorReservaHorario) {
		this.identificadorReservaHorario = identificadorReservaHorario;
	}	

	@Column(name = "in_ativo")
	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	public String toString(){
		return "Sala: " + getSala().getSala() + " - " + getReservaHorario().getDsTraducaoExpressaoCron();
	}
}
