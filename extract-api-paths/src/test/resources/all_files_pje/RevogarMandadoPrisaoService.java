package br.com.infox.pje.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import br.jus.cnj.mandadoprisao.webservices.MPWSComprovante;
import br.jus.cnj.mandadoprisao.webservices.MPWSSituacaoMandado;
import br.jus.cnj.mandadoprisao.webservices.MPWSTipoSituacaoMandado;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.MandadoPrisaoManager;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.entidades.MandadoPrisaoComprovante;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;
import br.jus.pje.nucleo.enums.SituacaoMandadoPrisaoEnum;

@Name("revogarMandadoPrisaoService")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class RevogarMandadoPrisaoService extends BNMPService{

	private static final long serialVersionUID = -3499230388828814118L;

	public static final String REVOGAR_MANDADO_PRISAO_BNMP_OBSERVER = "revogarMandadoPrisaoBNMPObserver";

	@In
	private MandadoPrisaoManager mandadoPrisaoManager;

	@In
	private AssinarMandadoPrisaoService assinarMandadoPrisaoService;

	public MandadoPrisao revogarMandadoPrisao(MandadoPrisao mandado) throws PJeBusinessException{
		if (mandado.getSituacaoExpedienteCriminal() == null || mandado.getSituacaoExpedienteCriminal() == SituacaoExpedienteCriminalEnum.PA){
			throw new PJeBusinessException("O mandado de prisão ainda não foi assinado");
		}

		mandadoPrisaoManager.gravarRevogacao(mandado);

		Events.instance().raiseAsynchronousEvent(REVOGAR_MANDADO_PRISAO_BNMP_OBSERVER, mandado);
		
		return mandado;
	}

	@Observer(REVOGAR_MANDADO_PRISAO_BNMP_OBSERVER)
	public MandadoPrisao enviarRevogacaoBNMP(MandadoPrisao mandado) throws PJeBusinessException{
		try{
			//mandado = entityManager.find(MandadoPrisao.class, mandado.getId());
			
			// mandado ainda não enviado ao BNMP
			if (mandado.getSituacaoAtualBNMP() == null){
				assinarMandadoPrisaoService.enviarMandadoPrisaoBNMP(mandado);
			}

			MPWSSituacaoMandado situacao = new MPWSSituacaoMandado();
			situacao.setNumeroMandado(mandado.getNumeroExpediente());
			situacao.setNumeroProcessoR65(mandado.getProcessoTrf().getNumeroProcesso());
			situacao.setTipoSituacao(MPWSTipoSituacaoMandado.REVOGADO);

			MPWSComprovante ticket = atualizarSituacaoBNMP(situacao);

			MandadoPrisaoComprovante comprovante = new MandadoPrisaoComprovante();
			comprovante.setMandadoPrisao(mandado);
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

			Date data = sdf.parse(ticket.getDataEnvio());

			comprovante.setDataRecebimento(data);
			comprovante.setNumeroProtocoloRecebimento(ticket.getNumeroProtocolo());
			comprovante.setSituacaoMandadoPrisao(SituacaoMandadoPrisaoEnum.RV);
			mandado.getComprovantes().add(comprovante);

			mandadoPrisaoManager.persist(mandado);
			return mandado;
		} catch (PJeDAOException e){
			throw new PJeBusinessException(e);
		} catch (PJeBusinessException e){
			throw new PJeBusinessException(e);
		} catch (ParseException e){
			throw new PJeBusinessException("Erro na revogação do mandado de prisão ao tentar converter a data do comprovante com a data recebida do BNMP");
		}
	}
}
