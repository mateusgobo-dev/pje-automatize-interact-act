package br.jus.pje.nucleo.dto.domicilioeletronico;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class ComunicacaoRecebida {

	@JsonProperty("messageType")
	private String tipoComunicacao;

	@JsonProperty(value="numeroComunicacao",required = true)
	@JsonDeserialize(using = CustomNumberDeserializer.class)
	private Integer idProcessoParteExpediente;

	@JsonProperty("numeroProcesso")
	private String numeroProcesso;	

	@JsonProperty("foiTribunal")
	private boolean foiTribunal;

	@JsonProperty("foiCienciaAutomatica")
	private boolean cienciaAutomatica;

	public String getTipoComunicacao() {
		return tipoComunicacao;
	}

	public void setTipoComunicacao(String tipoComunicacao) {
		this.tipoComunicacao = tipoComunicacao;
	}

	public Number getIdProcessoParteExpediente() {
		return idProcessoParteExpediente;
	}

	public void setIdProcessoParteExpediente(Integer idProcessoParteExoediente) {
		this.idProcessoParteExpediente = idProcessoParteExoediente;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public boolean isFoiTribunal() {
		return foiTribunal;
	}

	public void setFoiTribunal(boolean foiTribunal) {
		this.foiTribunal = foiTribunal;
	}

	public boolean isCienciaAutomatica() {
		return cienciaAutomatica;
	}

	public void setCienciaAutomatica(boolean cienciaAutomatica) {
		this.cienciaAutomatica = cienciaAutomatica;
	}

	/**
	 * CustomNumberDeserializer é uma implementação personalizada de {@link JsonDeserializer}
	 * para desserializar valores numéricos de uma string JSON para um tipo {@link Number}.
	 * <p>
	 * Esta classe tenta primeiro converter a string em um {@link Integer}.
	 * Se a conversão para {@link Integer} falhar, ela tenta converter para {@link Long}.
	 * Se ambas as conversões falharem, ela retorna {@code null}.
	 * </p>
	 * <p>
	 * Esta implementação utiliza a biblioteca Guava, que fornece métodos utilitários
	 * para realizar conversões seguras de strings em tipos numéricos, evitando
	 * a necessidade de capturar exceções.
	 * </p>
	 *
	 * @author Jônatas Pereira da Silva
	 * @version 1.0
	 */
	public static class CustomNumberDeserializer extends JsonDeserializer<Number> {

	    /**
	     * Desserializa uma string JSON em um objeto {@link Number}, tentando converter
	     * a string para {@link Integer} primeiro, e depois para {@link Long} se a primeira
	     * conversão falhar.
	     * <p>
	     * A string é analisada usando os métodos {@link Ints#tryParse(String)} e {@link Longs#tryParse(String)}
	     * da biblioteca Guava. Esses métodos retornam {@code null} se a string não puder ser
	     * convertida, garantindo que não seja lançada uma {@link NumberFormatException}.
	     * </p>
	     * <p>
	     * Verifica-se se o parser JSON {@link JsonParser} e o valor de string obtido não são {@code null}
	     * antes de proceder com a desserialização para evitar possíveis {@link NullPointerException}.
	     * </p>
	     *
	     * @param p     o parser JSON usado para ler o conteúdo JSON, não deve ser {@code null}
	     * @param ctxt  o contexto de desserialização, não deve ser {@code null}
	     * @return um objeto {@link Number} contendo o valor numérico, ou {@code null} se
	     * a string não puder ser convertida para {@link Integer} ou {@link Long}
	     * @throws IOException se ocorrer um erro de leitura durante a desserialização
	     * @throws JsonProcessingException se ocorrer um erro de processamento durante a desserialização
	     * @throws IllegalArgumentException se o parser JSON ou o valor de string for {@code null}
	     */
		@Override
		public Number deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {

			if (p == null || p.getText() == null) {
				throw new IllegalArgumentException("JsonParser e o texto não podem ser nulos");
			}

			String value = p.getText();

			// Tenta converter para Integer
			Integer intValue = Ints.tryParse(value);
			if (intValue != null) {
				return intValue;
			}

			// Se não puder ser convertido, retorna null
			return null;
		}
	}
}
