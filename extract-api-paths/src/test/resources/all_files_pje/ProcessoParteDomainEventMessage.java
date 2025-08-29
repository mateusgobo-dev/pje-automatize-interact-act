package br.jus.cnj.pje.amqp.model.dto;

import java.util.ArrayList;
import java.util.List;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;

/**
 * Classe que representa os dados da parte que são enviados para o RabbitMQ.
 * 
 * @author Adriano Pamplona
 */
public class ProcessoParteDomainEventMessage implements CloudEventPayload<ProcessoParteDomainEventMessage, ProcessoParte>{

	private static final long serialVersionUID = 1L;

	private String participacao;
	private Boolean principal;
	private Boolean sigiloso;
	private PessoaDomainEventMessage pessoa;
	private List<ProcessoParteRepresentanteDomainEventMessage> representantes = new ArrayList<>();

	/**
	 * Construtor.
	 *
	 * @param processo
	 */
	public ProcessoParteDomainEventMessage(ProcessoParte processoParte) {
		super();
		if (processoParte != null) {
			setParticipacao(processoParte.getInParticipacao().getLabel());
			setPrincipal(processoParte.getPartePrincipal());
			setSigiloso(processoParte.getParteSigilosa());
			setPessoa(new PessoaDomainEventMessage(processoParte.getPessoa()));
			setRepresentantes(processoParte.getProcessoParteRepresentanteList());
		}
	}

	@Override
	public ProcessoParteDomainEventMessage convertEntityToPayload(ProcessoParte entity) {
		return new ProcessoParteDomainEventMessage(entity);
	}

	@Override
	public Long getId(ProcessoParte entity) {
		return (entity != null ? Long.valueOf(entity.getIdProcessoParte()) :  null);		
	}

	/**
	 * @return the participacao
	 */
	public String getParticipacao() {
		return participacao;
	}

	/**
	 * @param participacao the participacao to set
	 */
	public void setParticipacao(String participacao) {
		this.participacao = participacao;
	}

	/**
	 * @return the principal
	 */
	public Boolean getPrincipal() {
		return principal;
	}

	/**
	 * @param principal the principal to set
	 */
	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	/**
	 * @return the sigiloso
	 */
	public Boolean getSigiloso() {
		return sigiloso;
	}

	/**
	 * @param sigiloso the sigiloso to set
	 */
	public void setSigiloso(Boolean sigiloso) {
		this.sigiloso = sigiloso;
	}

	/**
	 * @return the pessoa
	 */
	public PessoaDomainEventMessage getPessoa() {
		return pessoa;
	}

	/**
	 * @param pessoa the pessoa to set
	 */
	public void setPessoa(PessoaDomainEventMessage pessoa) {
		this.pessoa = pessoa;
	}

	/**
	 * @return the representantes
	 */
	public List<ProcessoParteRepresentanteDomainEventMessage> getRepresentantes() {
		return representantes;
	}

	/**
	 * @param representantes the representantes to set
	 */
	public void setRepresentantesDomainEventMessage(List<ProcessoParteRepresentanteDomainEventMessage> representantes) {
		this.representantes = representantes;
	}
	
	/**
	 * @param assuntoTrfList the assuntoTrfList to set
	 */
	public void setRepresentantes(List<ProcessoParteRepresentante> representantes) {
		for (ProcessoParteRepresentante representante : representantes) {
			getRepresentantes().add(new ProcessoParteRepresentanteDomainEventMessage(representante));
		}
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
