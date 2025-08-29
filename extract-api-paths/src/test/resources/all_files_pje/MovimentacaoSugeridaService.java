package br.jus.cnj.pje.nucleo.service;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRestException;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.webservice.client.SinapsesClient;
import br.jus.pje.nucleo.dto.sinapses.Mensagem;
import br.jus.pje.nucleo.dto.sinapses.MovimentacaoSugeridaRequest;
import br.jus.pje.nucleo.dto.sinapses.MovimentacaoSugeridaResponse;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.StringUtil;

@Name(MovimentacaoSugeridaService.NAME)
@Scope(ScopeType.PAGE)
public class MovimentacaoSugeridaService implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "movimentacaoSugeridaService";
	
	private static final Logger logger = LoggerFactory.getLogger(MovimentacaoSugeridaService.class);
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@In(create = true, required = true)
	private TramitacaoProcessualService tramitacaoProcessualService;
	
	@In(create = true)
	private SinapsesClient sinapsesClient;
	
	@Create
	public void init() {
		
	}
	
	public MovimentacaoSugeridaResponse recuperarMovimentacaoSugerida(Integer idProcessoDocumento, String caminhoModelo, Integer qtdClasses) throws PJeBusinessException, PJeRestException {
		
		MovimentacaoSugeridaResponse response = new MovimentacaoSugeridaResponse();
		ProcessoDocumento pd = this.processoDocumentoManager.findById(idProcessoDocumento);
		
		if(pd != null && pd.getProcessoDocumentoBin() != null && !pd.getProcessoDocumentoBin().getModeloDocumento().isEmpty() ) {
			String conteudoHtml = pd.getProcessoDocumentoBin().getModeloDocumento();
			String conteudoSemTagHtml = StringUtil.removeHtmlTags(conteudoHtml);
			String encodedText = Base64.encodeBytes(conteudoSemTagHtml.getBytes());
			MovimentacaoSugeridaRequest request = this.montarRequisicaoMovimentacaoSugerida(encodedText, "TEXTO");
			Mensagem mensagem = new Mensagem();
			mensagem.setMensagem(request);
			mensagem.setQuantidadeClasses(qtdClasses);
			try {
				response = this.sinapsesClient.recuperarMovimetacoesSugeridas(mensagem, caminhoModelo);
			}catch (PJeRestException e) {
				response = null;
				logger.error("Erro ao tentar conectar ao servico Sinapses");
				logger.error(e.getMessage());
				logger.error("Fim do log do erro");
				throw e;
			}
		}
		
		return response;
	}
	
	private MovimentacaoSugeridaRequest montarRequisicaoMovimentacaoSugerida(String encodedHtml, String tipo) {
		return new MovimentacaoSugeridaRequest(tipo, encodedHtml);
	}
	
}
