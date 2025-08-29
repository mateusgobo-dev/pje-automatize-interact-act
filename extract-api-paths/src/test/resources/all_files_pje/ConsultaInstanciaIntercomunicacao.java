package br.com.infox.trf.webservice;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorService;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorServiceAbstract;
import br.jus.cnj.pje.ws.Jurisdicao;
import br.jus.cnj.pje.ws.client.ConsultaPJeClient;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Scope(value = ScopeType.EVENT)
@BypassInterceptors
@Name(ConsultaInstanciaIntercomunicacao.NAME)
/**
 * Classe responsável por consultar dados de processos em outras instâncias.
 * Ela permite a consulta 
 * @author Leonardo Inácio
 *
 */
public class ConsultaInstanciaIntercomunicacao {
	public final static String NAME = "consultaInstanciaIntercomunicacao";

	public static ConsultaInstanciaIntercomunicacao instance() {
		return ComponentUtil.getComponent(ConsultaInstanciaIntercomunicacao.NAME);
	}

	/**
	 * Método responsável por consultar um processo na instância especificada.
	 * A consulta é feita através do número do processo passado como parâmetro.
	 * @param endereco Dados do endereço da instância do PJE
	 * @param numeroProcesso Número do processo
	 * @param consultarMovimentos Informa se o sistema deve recuperar os movimentos do processo
	 * @param consultarDocumentos Informa se o sistema deve recuperar os documentos do processo
	 * @return Dados do processo consultado na instância especificada
	 * @throws Exception
	 */
	public ProcessoTrf consultarProcesso(EnderecoWsdl endereco, String numeroProcesso, Boolean consultarMovimentos, Boolean consultarDocumentos) 
			throws Exception {
		ProcessoTrf resultado = null;
		
		// Se o endereço da instância foi especificado...
		if (endereco != null && endereco.getWsdlIntercomunicacao() != null) {
			ConsultarProcessoRequisicaoDTO requisicao = new ConsultarProcessoRequisicaoDTO();
			requisicao.setNumeroProcesso(numeroProcesso);
			requisicao.setMovimentos(consultarMovimentos);
			requisicao.setIncluirDocumentos(consultarDocumentos);
			
			MNIMediatorService mediator = MNIMediatorServiceAbstract.instance(endereco);
			ConsultarProcessoRespostaDTO resposta = mediator.consultarProcesso(requisicao);
			
			resultado = resposta.getProcessoTrf();
		}
		
		return resultado;
	}

	/**
	 * Método responsável por consultar um documento de processo na instância especificada.
	 * A consulta é feita através do identificador do documento passado como parâmetro.
	 * @param endereco Dados do endereço da instância do PJE
	 * @param numeroProcesso Número do processo
	 * @param idDocumento Identificador do documento do processo
	 * @return Dados do processo consultado na instância especificada
	 * @throws Exception
	 */
	public ProcessoTrf consultarDocumentoProcesso(EnderecoWsdl endereco, String numeroProcesso, Integer idDocumento) 
			throws Exception {
		ProcessoTrf resultado = null;
		
		// Se o endereço da instância foi especificado...
		if (endereco != null && endereco.getWsdlIntercomunicacao() != null) {
			ConsultarProcessoRequisicaoDTO requisicao = new ConsultarProcessoRequisicaoDTO();
			requisicao.setNumeroProcesso(numeroProcesso);
			requisicao.setMovimentos(false);
			requisicao.setIncluirDocumentos(false);
			requisicao.getDocumento().add(String.valueOf(idDocumento));
			
			MNIMediatorService mediator = MNIMediatorServiceAbstract.instance(endereco);
			ConsultarProcessoRespostaDTO resposta = mediator.consultarProcesso(requisicao);
			
			resultado = resposta.getProcessoTrf();
		}
		
		return resultado;
	}
	
	/**
	 * Método responsável por recuperar os dados da Jurisdição Processual especificada
	 * @param endereco Dados do endereço da instância do PJE 
	 * @param numeroOrigemJurisdicao Número de origem da jurisdição
	 * @return Dados da Jurisdição Processual
	 * @throws Exception
	 */
	public Jurisdicao recuperarJurisdicaoProcessual(EnderecoWsdl endereco, String numeroOrigemJurisdicao) throws Exception {
		// Se o endereço da instância foi especificado...
		if (endereco != null && endereco.getWsdlConsulta() != null) {
			ConsultaPJeClient consultaPJeClient = new ConsultaPJeClient(endereco);
			List<Jurisdicao> jurisdicoes = consultaPJeClient.consultarJurisdicoes();
			
			for (Jurisdicao jurisdicao : jurisdicoes) {
				if (jurisdicao.getId().toString().equals(numeroOrigemJurisdicao)) {
					return jurisdicao;
				}
			}
		}
		
		return null;
	}
}