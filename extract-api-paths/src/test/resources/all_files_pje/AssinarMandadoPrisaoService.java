package br.com.infox.pje.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.mandadoprisao.webservices.MPWSComprovante;
import br.jus.cnj.mandadoprisao.webservices.MPWSTipoSituacaoMandado;
import br.jus.cnj.pje.nucleo.BNMPException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.MandadoPrisaoManager;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.entidades.MandadoPrisaoComprovante;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.enums.SituacaoMandadoPrisaoEnum;

@Name("assinarMandadoPrisaoService")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AssinarMandadoPrisaoService extends AbstractAssinarExpedienteCriminalService<MandadoPrisao>{

	private static final long serialVersionUID = -6762181868029193112L;

	@In
	private MandadoPrisaoManager mandadoPrisaoManager;

	@In
	private RevogarMandadoPrisaoService revogarMandadoPrisaoService;

	@In
	private EntityManager entityManager;

	public static final String ENVIAR_MANDADO_BNMP_OBSERVER = "enviarMandadoBNMPObserver";

	@Override
	public MandadoPrisao assinarExpedienteCriminal(MandadoPrisao expediente, String assinatura, String encodedCertChain, Long jbpmTask) throws PJeBusinessException{
		expediente.setPessoaMagistrado(((PessoaFisica) Authenticator.getUsuarioLogado()).getPessoaMagistrado());
		
		//substituindo a marcacao ||MAGISTADO|| pelo nome do magistado
		String textoMagistrado = expediente.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento()
				.replace(MandadoPrisaoManager.MARCACAO_MAGISTRADO_REPLACE, expediente.getPessoaMagistrado().getNome());
		expediente.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(textoMagistrado);
		
		mandadoPrisaoManager.persist(expediente);
		expediente = super.assinarExpedienteCriminal(expediente, assinatura, encodedCertChain, jbpmTask);
		entityManager.flush();

		// Quando a publicação não for restrita, deve-se enviar para o BNMP
		if (expediente.getPublicacaoRestrita() != null && !expediente.getPublicacaoRestrita()){
			// Chamando o método enviarMandadoPrisaoBNMP()
			// Events.instance().raiseAsynchronousEvent(ENVIAR_MANDADO_BNMP_OBSERVER, expediente);
			enviarMandadoPrisaoBNMP(expediente);
		}

		return expediente;
	}

	// envio do mandado para o BNMP
	@Observer(ENVIAR_MANDADO_BNMP_OBSERVER)
	public MandadoPrisao enviarMandadoPrisaoBNMP(MandadoPrisao mandado) throws PJeBusinessException{
		// mandado = entityManager.find(MandadoPrisao.class, mandado.getId());

		/*
		 * permite o envio/reenvio se estiver com situação interna REVOGADO e situação no BNMP nula
		 */
		if (mandado.getSituacaoAtualBNMP() != null && mandado.getSituacaoAtualBNMP() != SituacaoMandadoPrisaoEnum.RV){
			throw new PJeBusinessException(
					"pje.assinarMandadoPrisaoService.error.mandadoPrisaoJaEnviadoBNMP",
					null, mandado.getNumeroExpediente(), mandado
							.getSituacaoAtualBNMP().getLabel());
		}

		if (mandado.getPublicacaoRestrita() == null || !mandado.getPublicacaoRestrita()){
			try{
				List<MandadoPrisao> mandados = new ArrayList<MandadoPrisao>();
				mandados.add(mandado);

				MPWSComprovante ticket = enviarParaBNMP(mandados, MPWSTipoSituacaoMandado.AGUARDANDO_CUMPRIMENTO);

				MandadoPrisaoComprovante comprovante = new MandadoPrisaoComprovante();
				comprovante.setMandadoPrisao(mandado);
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

				Date data = sdf.parse(ticket.getDataEnvio());

				comprovante.setDataRecebimento(data);
				comprovante.setNumeroProtocoloRecebimento(ticket.getNumeroProtocolo());
				comprovante.setSituacaoMandadoPrisao(SituacaoMandadoPrisaoEnum.AC);
				mandado.getComprovantes().add(comprovante);

				mandadoPrisaoManager.persist(mandado);
			} catch (PJeDAOException e){
				// revogando para permitir o reenvio, pois já foi gravado no BNMP
				revogarMandadoPrisaoService.revogarMandadoPrisao(mandado);
				throw new PJeBusinessException(e);
			} catch (BNMPException e){
				throw new PJeBusinessException(e);
			} catch (PJeBusinessException e){
				// revogando para permitir o reenvio, pois já foi gravado no BNMP
				revogarMandadoPrisaoService.revogarMandadoPrisao(mandado);
				throw new PJeBusinessException(e);
			} catch (ParseException e){
				// revogando para permitir o reenvio, pois já foi gravado no BNMP
				revogarMandadoPrisaoService.revogarMandadoPrisao(mandado);
				throw new PJeBusinessException("pje.abstractAssinarExpedienteCriminalService.error.formatoDataComprovanteInvalido");
			}
		}

		return mandado;
	}
}
