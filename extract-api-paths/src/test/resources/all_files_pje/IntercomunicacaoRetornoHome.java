package br.com.infox.cliente.home;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.entidades.vo.StatusEnvioManifestacaoProcessualVO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.nucleo.manager.ManifestacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfManifestacaoProcessualManager;
import br.jus.cnj.pje.ws.client.ConsultaPJeClient;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


@Name("intercomunicacaoRetornoHome")
public class IntercomunicacaoRetornoHome extends AbstractIntercomunicacaoHome {
	
	private static final long serialVersionUID = 5267842336500942986L;
	
	public ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual;
 	
	@In
	private ManifestacaoProcessualManager manifestacaoProcessualManager;
	
	@In
	private ProcessoTrfManifestacaoProcessualManager processoTrfManifestacaoProcessualManager;
	
	/** Faz o retorno do processo para o primeiro grau
	 * @author Thiago Oliveira/Guilherme Bispo
	 * @since 1.4.5	 
	 */
	public void retornar() {
		try {
			Pessoa pessoaLogada = Authenticator.getPessoaLogada();
			ProcessoTrf processo = getInstance();
			Boolean logouComCertificado = Authenticator.isLogouComCertificado();
			br.jus.pje.nucleo.entidades.ManifestacaoProcessual mp = manifestacaoProcessualManager.buscaUltimoEntregue(getInstance());
			EnderecoWsdl enderecoWsdl = processoTrfManifestacaoProcessualManager.obterEnderecoWsdlDaManifestacao(processo);
			setEnderecoWsdl(enderecoWsdl);
			ConsultaPJeClient consultaPJeClient = new ConsultaPJeClient(enderecoWsdl);

			manifestacaoProcessual = new ManifestacaoProcessualRequisicaoDTO();
			manifestacaoProcessual.setProcessoTrf(processo);
			manifestacaoProcessual.setConsultaPJeClient(consultaPJeClient);
			manifestacaoProcessual.setCompetenciaConflito(null);
			manifestacaoProcessual.setIsRequisicaoPJE(Boolean.TRUE);
			manifestacaoProcessual.addParametro(MNIParametro.isPJE(), "true");
			manifestacaoProcessual.addParametro(MNIParametro.PARAM_LOGOU_COM_CERTIFICADO, logouComCertificado.toString());
			StringBuilder erros = validarExpedientes(processo);

			if(!this.validaMotivoRemessa())
                erros.append("Favor preencher o motivo da remessa.\n");
			
			if (erros.length() > 0) {
				throw new Exception(erros.toString());
			}

			setDocumentoNaoAssinadoList(getDocumentosNaoAssinadosUsuarioInterno(getInstance()));

			if ((getDocumentoNaoAssinadoList()==null) || (getDocumentoNaoAssinadoList().size()==0)) {
				enviarProcesso();
			}
		} catch (Exception e) {
			finalizarEnvioManifestacaoProcessual();
			FacesUtil.adicionarMensagemInfo(true, e); 
		}
	}
	
	public void enviarProcesso() {
	
		try {
			ProcessoTrf processo = getInstance();
			
			this.enviarProcesso(
					processo, 
					getEnderecoWsdl(), 
					"Processo enviado à instância inferior.", 
					MNIParametro.PARAM_RETORNO);
		} catch (Exception e) {
			finalizarEnvioManifestacaoProcessual();
			FacesUtil.adicionarMensagemInfo(true, e); 
		}
	}
	
	@Override
	public void posEnviarProcesso() {
		ProcessoTrf processo = ProcessoTrfHome.instance().getInstance();
		StatusEnvioManifestacaoProcessualVO status = getStatusEnvioManifestacaoProcessualVO(true);
				
		super.posEnviarProcesso(
					processo, 
					MNIParametro.PARAM_RETORNO, 
					status.getProtocoloRecebimento(), 
					"Processo enviado à instância inferior.",
					getEnderecoWsdl());
	}

	@Override
	protected ManifestacaoProcessualRequisicaoDTO getManifestacaoProcessual() {
		return manifestacaoProcessual;
	}
}
