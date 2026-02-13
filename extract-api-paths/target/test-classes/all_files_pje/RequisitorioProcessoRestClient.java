package br.jus.cnj.pje.webservice.client;

import java.io.Serializable;

import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponse;

@Name(RequisitorioProcessoRestClient.NAME)
public class RequisitorioProcessoRestClient implements Serializable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RequisitorioProcessoRestClient.class);

	private static final String COD_SUCESSO = "200";
	private static final String API_REQUISITORIO = "/requisitorio/api";
	private static final String V1 = "/v1";
	private static final String REQUISITORIOS = "/requisitorios";
	
	private static final String ATUALIZA_SITUACAO_REQUISITORIO = API_REQUISITORIO + V1 + REQUISITORIOS + "/atualiza-situacao/";
	private static final String ENVIAR_DIVISAO_PAGAMENTO_REQUISITORIO = API_REQUISITORIO + V1 + REQUISITORIOS + "/enviar-divisao-pagamento/";
	private static final String MARCAR_SITUACAO_ATUAL_COMO_ANALISADO_SISTEMA = API_REQUISITORIO + V1 + REQUISITORIOS + "/situacoes/marcar-situacao-analisado-sistema/";
	private static final String COD_SITUACAO_ATUAL_REQUISITORIO = API_REQUISITORIO + V1 + REQUISITORIOS + "/situacoes/codigo/";
	
	private static final long serialVersionUID = -3354546414760260775L;

	public static final String NAME = "requisitorioProcessoRestClient";
	
	public boolean atualizaSituacaoRequisitorioAtual(String codSituacao) {
		return atualizaSituacaoRequisitorioAtual(codSituacao, "");
	}
	
	@SuppressWarnings({ "static-access", "unchecked" })
	public boolean atualizaSituacaoRequisitorioAtual(String codSituacao, String motivo) {
		
		PjeApiClient pjeApiClient = ComponentUtil.getComponent(PjeApiClient.NAME);
		AtualizarSituacaoRequisitorioRequestDto dto = new AtualizarSituacaoRequisitorioRequestDto();
		dto.setAutorSituacao(Authenticator.getUsuarioLogado().getNome());
		dto.setCodSituacao(codSituacao);
		dto.setLocalizacao(Authenticator.getLocalizacaoAtual().getLocalizacao());
		dto.setMotivoSituacao(motivo);
		dto.setPapel(Authenticator.getPapelAtual().getNome());
		PjeResponse<Object> respostaDTO = null;
		try {
			String idRequisitorio = JbpmUtil.getProcessVariable("idRequisitorio");
			respostaDTO = (PjeResponse<Object>) pjeApiClient.post(ATUALIZA_SITUACAO_REQUISITORIO + idRequisitorio, Object.class, dto);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return respostaDTO != null && respostaDTO.getCode().equals(COD_SUCESSO);
	}
	
	@SuppressWarnings({ "static-access", "unchecked" })
	public boolean marcarSituacaoAtualComoAnalisadoSistema() {
		PjeApiClient pjeApiClient = ComponentUtil.getComponent(PjeApiClient.NAME);
		PjeResponse<Object> respostaDTO = null;
		try {
			String idRequisitorio = JbpmUtil.getProcessVariable("idRequisitorio");
			respostaDTO = (PjeResponse<Object>) pjeApiClient.get(MARCAR_SITUACAO_ATUAL_COMO_ANALISADO_SISTEMA+idRequisitorio, Object.class);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return respostaDTO != null && respostaDTO.getCode().equals(COD_SUCESSO);
	}
	
	@SuppressWarnings({ "static-access", "unchecked" })
	public boolean enviarADivisaoDePagamento() {
		PjeApiClient pjeApiClient = ComponentUtil.getComponent(PjeApiClient.NAME);
		PjeResponse<Object> respostaDTO = null;
		try {
			String idRequisitorio = JbpmUtil.getProcessVariable("idRequisitorio");
			respostaDTO = (PjeResponse<Object>) pjeApiClient.get(ENVIAR_DIVISAO_PAGAMENTO_REQUISITORIO + idRequisitorio, Object.class);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return respostaDTO != null && respostaDTO.getCode().equals(COD_SUCESSO);
	}
}
