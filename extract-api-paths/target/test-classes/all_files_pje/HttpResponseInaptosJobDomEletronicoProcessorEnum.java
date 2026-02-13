package br.jus.pje.nucleo.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum HttpResponseInaptosJobDomEletronicoProcessorEnum {

	COMUNICACAOCADASTRADA422(422, "COMUNICACAO_JA_CADASTRADA"),
	COMUNICACAOCIENTEOUEXPIRADA422(422, "PROCESSO_DAR_CIENCIA_COMUNICACAO_JA_CIENTE_OU_EXPIRADA");

	private int codigo;
	private String mensagem;

	HttpResponseInaptosJobDomEletronicoProcessorEnum(int codigo, String mensagem) {
		this.codigo = codigo;
		this.mensagem = mensagem;

	}

	public int getCodigo() {
		return codigo;
	}

	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public static Boolean existeHpttpResponse(int codigo, String mensagem) {

		List<HttpResponseInaptosJobDomEletronicoProcessorEnum> httpResponseDomEletr = Arrays
				.asList(HttpResponseInaptosJobDomEletronicoProcessorEnum.values());

		Optional<HttpResponseInaptosJobDomEletronicoProcessorEnum> enumHttpRespose = httpResponseDomEletr.stream()
				.filter(dom -> dom.getCodigo() == codigo && mensagem.contains(dom.getMensagem())).findFirst();

		if (enumHttpRespose.isPresent()) {
			return true;
		}

		return false;
	}

}
