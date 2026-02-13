package br.jus.csjt.pje.view.action;

import br.jus.pje.jt.enums.ParticipacaoObrigacaoEnum;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoPericia;

public class ParticipanteObrigacaoVO {
	private ProcessoParte processoParte;
	private ProcessoPericia processoPericia;
	private ParticipacaoObrigacaoEnum participacao;
	private boolean selecionado;
	private boolean incluido;
	private String beneficioOrdem;
	private ParticipacaoObrigacaoEnum defaultParticipacao;

	public ParticipanteObrigacaoVO(ProcessoParte processoParte, ParticipacaoObrigacaoEnum participacao,
			boolean selecionado, boolean incluido, String beneficioOrdem) {
		super();
		this.processoParte = processoParte;
		this.participacao = participacao;
		this.selecionado = selecionado;
		this.incluido = incluido;
		this.beneficioOrdem = beneficioOrdem;
		this.defaultParticipacao = participacao;
	}

	public ParticipanteObrigacaoVO(ProcessoParte processoParte, ParticipacaoObrigacaoEnum participacao) {
		super();
		this.processoParte = processoParte;
		this.participacao = participacao;
		this.selecionado = false;
		this.incluido = false;
		this.beneficioOrdem = "1";
		this.defaultParticipacao = participacao;
	}

	public ParticipanteObrigacaoVO(ProcessoPericia processoPericia, ParticipacaoObrigacaoEnum participacao) {
		super();
		this.processoPericia = processoPericia;
		this.participacao = participacao;
		this.selecionado = false;
		this.incluido = false;
		this.beneficioOrdem = "1";
		this.defaultParticipacao = participacao;
	}

	public ProcessoParte getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}

	public ProcessoPericia getProcessoPericia() {
		return processoPericia;
	}

	public void setProcessoPericia(ProcessoPericia processoPericia) {
		this.processoPericia = processoPericia;
	}

	public ParticipacaoObrigacaoEnum getParticipacao() {
		return participacao;
	}

	public void setParticipacao(ParticipacaoObrigacaoEnum participacao) {
		this.participacao = participacao;
	}

	public boolean getSelecionado() {
		return selecionado;
	}

	public void setSelecionado(boolean selecionado) {
		this.selecionado = selecionado;
	}

	public boolean getIncluido() {
		return incluido;
	}

	public void setIncluido(boolean incluido) {
		this.incluido = incluido;
	}

	public String getBeneficioOrdem() {
		return beneficioOrdem;
	}

	public int getBeneficioOrdemNumerico() {
		int result = 1;
		try {
			result = Integer.parseInt(this.beneficioOrdem);
			if (result <= 0)
				result = 1;
		} catch (NumberFormatException e) {
			this.beneficioOrdem = "1";
		}
		return result;
	}

	public void setBeneficioOrdem(String beneficioOrdem) {
		if (beneficioOrdem == null || beneficioOrdem.equals("")) {
			beneficioOrdem = "1";
		}
		this.beneficioOrdem = beneficioOrdem;
	}

	public ParticipacaoObrigacaoEnum getDefaultParticipacao() {
		return defaultParticipacao;
	}

	public void reset() {
		setIncluido(false);
		setSelecionado(false);
		setParticipacao(defaultParticipacao);
	}

	public boolean isPerito() {
		return processoPericia != null;
	}

	public boolean isParte() {
		return processoParte != null;
	}

	public String getTipoParte() {
		if (isPerito()) {
			return getProcessoPericia().getEspecialidade().getEspecialidade();
		} else if (isParte()) {
			return getProcessoParte().getTipoParte().getTipoParte();
		}

		return null;
	}

}
