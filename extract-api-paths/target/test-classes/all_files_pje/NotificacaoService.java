package br.jus.cnj.pje.nucleo.service;

import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.model.dto.ProcessoParteExpedienteDomainEventMessage;
import br.jus.cnj.pje.amqp.model.dto.ProcessoTrfDomainEventMessage;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.pdpj.commons.builders.DomainEventMessageBuilder;
import br.jus.pdpj.commons.models.dtos.amqp.DomainEventMessage;
import br.jus.pdpj.commons.models.enums.InstanciaJusticaEnum;
import br.jus.pdpj.commons.models.enums.NivelSigiloEnum;
import br.jus.pdpj.commons.models.enums.TribunalEnum;
import br.jus.pdpj.commons.models.vo.DocumentoIdentificacao;
import br.jus.pdpj.commons.models.vo.PessoaSimples;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Camada de serviço para integração com o serviço de notificações. A classe é
 * responsável pelo envio de mensagens para o RabbitMQ. <br/>
 * A integração com o serviço de notificação é habilitada através do parâmetro
 * Parametros.PDPJ_INTEGRACAO_SERVICONOTIFICACAO.<br/>
 * As mensagens estão no padrão reconhecido pelo serviço de notificação e
 * poderão ser recebidas por um cliente inscrito no serviço registrado.
 * 
 * @author Adriano Pamplona
 */
@Name(NotificacaoService.NAME)
@Transactional
public class NotificacaoService {

	public static final String NAME = "notificacaoService";
	public static final String EVENTO_NOVO_PROCESSO = "NovoProcessoEvent";
	public static final String EVENTO_NOVO_EXPEDIENTE = "NovoExpedienteEvent";

	/**
	 * @return Instância da classe.
	 */
	public static NotificacaoService instance() {
		return ComponentUtil.getComponent(NotificacaoService.class);
	}

	/**
	 * Envio do evento "NovoProcesso" com os dados do processo.
	 * 
	 * @param processo
	 */
	public void enviarMensagem(ProcessoTrf processo) {
		if (processo != null) {
			ProcessoTrfDomainEventMessage payload = new ProcessoTrfDomainEventMessage(processo);

			// @formatter:off
    		DomainEventMessage message = newDomainEventMessage(
    				EVENTO_NOVO_PROCESSO, 
    				NivelSigiloEnum.PUBLICO, 
    				processo.getNumeroProcesso(), 
    				payload);
    		// @formatter:on

			this.enviarMensagem(message);
		}
	}

	/**
	 * Envio do evento "NovoExpediente" com os dados do expediente.
	 * 
	 * @param expedientes List<ProcessoExpediente>
	 */
	public void enviarMensagem(List<ProcessoExpediente> expedientes) {

		if (CollectionUtils.isNotEmpty(expedientes)) {
			for (ProcessoExpediente pe : expedientes) {
				for (ProcessoParteExpediente ppe : pe.getProcessoParteExpedienteList()) {
					enviarMensagem(ppe);
				}
			}
		}
	}

	/**
	 * Envio do evento "NovoExpediente" com os dados do expediente.
	 * 
	 * @param expediente
	 */
	public void enviarMensagem(ProcessoParteExpediente expediente) {
		if (expediente != null) {
			ProcessoTrf processo = expediente.getProcessoJudicial();

			ProcessoParteExpedienteDomainEventMessage payload = new ProcessoParteExpedienteDomainEventMessage(
					expediente);

			// @formatter:off
			DomainEventMessage message = newDomainEventMessage(
					EVENTO_NOVO_EXPEDIENTE, 
					NivelSigiloEnum.PUBLICO,
					processo.getNumeroProcesso(), 
					payload);
			// @formatter:on

			this.enviarMensagem(message);
		}
	}

	/**
	 * Envia a mensagem para o RabbitMQ no padrão definido pelo serviço de
	 * notificação.
	 * 
	 * @param message DomainEventMessage
	 */
	public void enviarMensagem(DomainEventMessage message) {
		// Implementar quando for para integrar com o serviço de notificações.
	}

	/**
	 * Novo DomainEventMessage para enviar ao RabbitMQ.
	 * 
	 * @param nomeEvento
	 * @param nivelSigilo
	 * @param numeroProcesso
	 * @param payload
	 * @return
	 */
	public DomainEventMessage newDomainEventMessage(String nomeEvento, NivelSigiloEnum nivelSigilo,
			String numeroProcesso, Object payload) {
		String orgaoJustica = ParametroUtil.instance().recuperarNumeroOrgaoJustica();
		String grau = ParametroUtil.instance().getCodigoInstanciaAtual().substring(0, 1);

		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		nivelSigilo = ObjectUtils.firstNonNull(nivelSigilo, NivelSigiloEnum.PUBLICO);

		DomainEventMessage message = null;
		try {
			// @formatter:off
			PessoaSimples autor = new PessoaSimples(
					UUID.randomUUID(), 
					usuarioLogado.getLogin(),
					new DocumentoIdentificacao(usuarioLogado.getLogin()));
			message = DomainEventMessageBuilder
					.instance("")
					.withAppName(ConfiguracaoIntegracaoCloud.getAppName())
					.withAppVersion(ConfiguracaoIntegracaoCloud.getAppVersion())
					.withPayload(payload)
					.withEventName(nomeEvento + ".POST")
					.withInstanciaTribunalAcao(
							TribunalEnum.findByJTR(orgaoJustica),
							InstanciaJusticaEnum.findByInstancia(grau), null)
					.withAutor(autor)
					.withNivelSigiloEvento(nivelSigilo)
					.withNumeroUnicoProcesso(numeroProcesso).build();
			// @formatter:on
		} catch (Exception e) {
			String mensagem = String.format("Não foi possível criar DomainEventMessage. Erro: %s",
					e.getLocalizedMessage());
			throw new PJeRuntimeException(mensagem);
		}
		return message;
	}
}
