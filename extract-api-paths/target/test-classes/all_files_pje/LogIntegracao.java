package br.jus.pje.nucleo.entidades;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "tb_log_integracao")
public class LogIntegracao implements java.io.Serializable {

	@Id
	@GeneratedValue(generator = "generator")
	@org.hibernate.annotations.GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
			@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_log_integracao"),
			@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1") })
	@Column(name = "id_log_integracao", unique = true, nullable = false)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data")
	private Date data;

	@Column(name = "request_url")
	private String requestUrl;

	@Column(name = "request_payload")
	private String requestPayload;

	@Column(name = "request_payload_class")
	private String requestPayloadClass;

	@Column(name = "request_method")
	private String requestMethod;

	@Column(name = "request_token")
	private String requestToken;

	@Column(name = "response_status")
	private Integer responseStatus;

	@Column(name = "response_payload")
	private String responsePayload;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultima_alteracao")
	private Date dataUltimaAlteracao;

	@Basic(optional = true)
	@Column(name = "numero_processo")
	private String numeroProcesso;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = true)
	private ProcessoTrf processoTrf;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte_expediente", nullable = true)
	private ProcessoParteExpediente processoParteExpediente;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_processo_documento", nullable = true)
	private TipoProcessoDocumento tipoProcessoDocumento;

	public LogIntegracao() {
		// constructor empty
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getRequestPayload() {
		return requestPayload;
	}

	public void setRequestPayload(String requestPayload) {
		this.requestPayload = requestPayload;
	}

	public String getRequestPayloadClass() {
		return requestPayloadClass;
	}

	public void setRequestPayloadClass(String requestPayloadClass) {
		this.requestPayloadClass = requestPayloadClass;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getRequestToken() {
		return requestToken;
	}

	public void setRequestToken(String requestToken) {
		this.requestToken = requestToken;
	}

	public Integer getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(Integer responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getResponsePayload() {
		return responsePayload;
	}

	public void setResponsePayload(String responsePayload) {
		this.responsePayload = responsePayload;
	}

	public Date getDataUltimaAlteracao() {
		return dataUltimaAlteracao;
	}

	public void setDataUltimaAlteracao(Date dataUltimaAlteracao) {
		this.dataUltimaAlteracao = dataUltimaAlteracao;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		try {
			if (numeroProcesso != null && !numeroProcesso.isEmpty()) {
				this.numeroProcesso = numeroProcesso;
			} else {
				String extractedNumeroProcesso = extractNumeroProcessoFromUrl();
				this.numeroProcesso = (extractedNumeroProcesso != null && !extractedNumeroProcesso.isEmpty())
						? extractedNumeroProcesso
						: null;
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
			this.numeroProcesso = numeroProcesso;
		}
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public ProcessoParteExpediente getProcessoParteExpediente() {
		return processoParteExpediente;
	}

	public void setProcessoParteExpediente(ProcessoParteExpediente processoParteExpediente) {
		this.processoParteExpediente = processoParteExpediente;
	}

	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@Transient
	private String extractNumeroProcessoFromUrl() throws URISyntaxException {
		if (requestUrl == null || requestUrl.isEmpty()) {
			return null;
		}

		URI uri = new URI(requestUrl);
		String[] segments = uri.getPath().split("/");
		return segments[segments.length - 1];
	}

	@Transient
	public Integer getNumeroComunicacao() {
		if (requestPayload == null || requestPayload.isEmpty()) {
			return null;
		}

		ObjectMapper objectMapper = new ObjectMapper();

		try {
			JsonNode rootNode = objectMapper.readTree(requestPayload);

			JsonNode numeroComunicacaoNode = !rootNode.path("numeroComunicacao").isMissingNode() ? 
					rootNode.path("numeroComunicacao") : 
						rootNode.path("idProcessoParteExpediente");

			if (numeroComunicacaoNode.isMissingNode()) {
				return null;
			}

			return numeroComunicacaoNode.asInt();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
