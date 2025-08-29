package br.jus.cnj.pje.vo;

import java.io.Serializable;
import java.util.Calendar;

import br.jus.pje.nucleo.entidades.ProcessoPericia;

public class DesignarPericia implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean ativo;
	private Calendar dataHora;
	private ProcessoPericia processoPericia;
	private ProcessoPericia processoPericiaAntigo;
	private boolean redesignarPericia = false;

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public ProcessoPericia getProcessoPericia() {
		return processoPericia;
	}

	public void setProcessoPericia(ProcessoPericia processoPericia) {
		this.processoPericia = processoPericia;
	}

	public ProcessoPericia getProcessoPericiaAntigo() {
		return processoPericiaAntigo;
	}

	public void setProcessoPericiaAntigo(ProcessoPericia processoPericiaAntigo) {
		this.processoPericiaAntigo = processoPericiaAntigo;
	}

	public boolean isRedesignarPericia() {
		return redesignarPericia;
	}

	public void setRedesignarPericia(boolean redesignarPericia) {
		this.redesignarPericia = redesignarPericia;
	}

	public Calendar getDataHora() {
		return dataHora;
	}

	public void setDataHora(Calendar dataHora) {
		this.dataHora = dataHora;
	}
}