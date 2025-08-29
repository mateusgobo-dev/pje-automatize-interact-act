package br.com.infox.pje.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.mandadoprisao.webservices.MPWSComprovante;
import br.jus.cnj.mandadoprisao.webservices.MPWSSituacaoMandado;
import br.jus.cnj.mandadoprisao.webservices.MPWSTipoSituacaoMandado;
import br.jus.cnj.pje.nucleo.BNMPException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.MandadoPrisaoManager;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.entidades.MandadoPrisaoComprovante;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;
import br.jus.pje.nucleo.enums.SituacaoMandadoPrisaoEnum;

@Name("cumprirMandadoPrisaoService")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class CumprirMandadoPrisaoService extends BNMPService{

	private static final long serialVersionUID = 6768056875087436699L;

	@In
	private MandadoPrisaoManager mandadoPrisaoManager;

	@In
	private AssinarMandadoPrisaoService assinarMandadoPrisaoService;

	public static final String ENVIAR_CUMPRIMENTO_BNMP_OBSERVER = "enviarCumprimentoBNMPObserver";
	public static final String ENVIAR_ASSINATURA_CUMPRIMENTO_BNMP_OBSERVER = "enviarAssinaturaCumprimentoBNMPObserver";

	@Observer(ENVIAR_CUMPRIMENTO_BNMP_OBSERVER)
	public MandadoPrisao enviarCumprimentoBNMP(MandadoPrisao mandado) throws PJeBusinessException{
		try{
			//mandado = entityManager.find(MandadoPrisao.class, mandado.getId());
			MPWSComprovante ticket = null;

			/*
			 * Se publicidade restrita, envia o mandado inicialmente com situação CUMPRIDO, se não, apenas altera a situação do mandado para
			 * 'CUMPRIDO'
			 */
			if (mandado.getPublicacaoRestrita() != null && mandado.getPublicacaoRestrita()){
				List<MandadoPrisao> mandados = new ArrayList<MandadoPrisao>(0);
				mandados.add(mandado);
				ticket = enviarParaBNMP(mandados, MPWSTipoSituacaoMandado.CUMPRIDO);
			}
			else{
				MPWSSituacaoMandado situacao = new MPWSSituacaoMandado();
				situacao.setNumeroMandado(mandado.getNumeroExpediente());
				situacao.setNumeroProcessoR65(mandado.getProcessoTrf().getNumeroProcesso());
				situacao.setTipoSituacao(MPWSTipoSituacaoMandado.CUMPRIDO);
				ticket = atualizarSituacaoBNMP(situacao);
			}

			MandadoPrisaoComprovante comprovante = new MandadoPrisaoComprovante();
			comprovante.setMandadoPrisao(mandado);
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

			Date data = sdf.parse(ticket.getDataEnvio());

			comprovante.setDataRecebimento(data);
			comprovante.setNumeroProtocoloRecebimento(ticket.getNumeroProtocolo());
			comprovante.setSituacaoMandadoPrisao(SituacaoMandadoPrisaoEnum.CP);
			mandado.getComprovantes().add(comprovante);

			mandadoPrisaoManager.persist(mandado);
			
			return mandado;
		} catch (PJeDAOException e){
			throw new PJeBusinessException(e);
		} catch (BNMPException e){
			throw new PJeBusinessException(e);
		} catch (PJeBusinessException e){
			throw new PJeBusinessException(e);
		} catch (ParseException e){
			throw new PJeBusinessException("pje.abstractAssinarExpedienteCriminalService.error.formatoDataComprovanteInvalido");
		}
	}
	
	@Observer(ENVIAR_ASSINATURA_CUMPRIMENTO_BNMP_OBSERVER)
	public MandadoPrisao enviarAssinaturaCumprimentoMandadoPrisao(MandadoPrisao mandado) throws PJeBusinessException{
		assinarMandadoPrisaoService.enviarMandadoPrisaoBNMP(mandado);		
		enviarCumprimentoBNMP(mandado);
		return mandado;
	}

	public MandadoPrisao cumprirMandadoPrisao(MandadoPrisao mandado) throws PJeBusinessException{
		if (mandado.getSituacaoExpedienteCriminal() == null || mandado.getSituacaoExpedienteCriminal() != SituacaoExpedienteCriminalEnum.PC){
			throw new PJeBusinessException(
					"pje.cumprirMandadoPrisaoService.error.situacaoDiferentePendenteCumprimento",
					null, mandado.getSituacaoExpedienteCriminal());
		}

		if (mandado.getSituacaoExpedienteCriminal() == SituacaoExpedienteCriminalEnum.CP){
			throw new PJeBusinessException("pje.cumprirMandadoPrisaoService.error.mandadoJaCumprido");
		}

		mandadoPrisaoManager.gravarCumprimento(mandado);
		//mandado = entityManager.find(MandadoPrisao.class, mandado.getId());

		if (mandado.getSituacaoAtualBNMP() == null){
			// enviando a assinatura e depois o cumprimento do mandado de prisão para o BNMP
			//Events.instance().raiseAsynchronousEvent(ENVIAR_ASSINATURA_CUMPRIMENTO_BNMP_OBSERVER, mandado);
			enviarAssinaturaCumprimentoMandadoPrisao(mandado);
		}
		else if (mandado.getSituacaoAtualBNMP() == SituacaoMandadoPrisaoEnum.AC){
			// enviando o cumprimento do mandado de prisão para o BNMP
			//Events.instance().raiseAsynchronousEvent(ENVIAR_CUMPRIMENTO_BNMP_OBSERVER, mandado);
			enviarCumprimentoBNMP(mandado);
		}

		return mandado;
	}
}
