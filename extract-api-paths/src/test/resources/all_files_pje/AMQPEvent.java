package br.jus.pje.nucleo.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.pje.nucleo.type.CloudEventType;
import javax.persistence.GenerationType;
import javax.persistence.Transient;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = AMQPEvent.TABLE_NAME)
@TypeDefs({ @TypeDef(name = "CloudEventType", typeClass = CloudEventType.class) })
@javax.persistence.Cacheable(false)
public class AMQPEvent implements IEntidade<AMQPEvent, Long> {

	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_amqp_event";

	private Long id;
	private String routingKey;
	private CloudEvent cloudEvent;
	private String payloadHash;
	private Date dataCadastro;
	private Integer quantidadeTentativas = 0;
	private String errorMessage;

	@Id
	@GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@Parameter(name = "sequence", value = "sq_tb_amqp_event")
		, @Parameter(name = "allocationSize", value = "-1")})
	@GeneratedValue(generator = "generator", strategy = GenerationType.AUTO)
	@Column(name = "id_amqp_event", nullable = false, updatable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "routing_key", nullable = false, updatable = false)
	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}

	@Column(name = "json_cloud_event", nullable = false, updatable = false)
	@Type(type = "CloudEventType")
	public CloudEvent getCloudEvent() {
		return cloudEvent;
	}

	public void setCloudEvent(CloudEvent cloudEvent) {
		this.cloudEvent = cloudEvent;
	}

	@Column(name = "ds_payload_hash", nullable = false, updatable = false)
	public String getPayloadHash() {
		return payloadHash;
	}
	
	public void setPayloadHash(String payloadHash) {
		this.payloadHash = payloadHash;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cadastro", nullable = false, updatable = false)
	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	@Column(name = "qtd_tentativas", nullable = false, updatable = true)
	public int getQuantidadeTentativas() {
		return quantidadeTentativas;
	}

	public void setQuantidadeTentativas(int quantidadeTentativas) {
		this.quantidadeTentativas = quantidadeTentativas;
	}

	@Column(name = "error_message", length = 255, updatable = true, nullable = true)
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cloudEvent == null) ? 0 : cloudEvent.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((routingKey == null) ? 0 : routingKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AMQPEvent other = (AMQPEvent) obj;
		if (cloudEvent == null) {
			if (other.cloudEvent != null) {
				return false;
			}
		} else if (!cloudEvent.equals(other.cloudEvent)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (routingKey == null) {
			if (other.routingKey != null) {
				return false;
			}
		} else if (!routingKey.equals(other.routingKey)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AMQPEvent [id=" + id + ", routingKey=" + routingKey + ", dataCadastro=" + dataCadastro
				+ ", quantidadeTentativas=" + quantidadeTentativas + "]";
	}

	@Override
	@Transient
	public Class<? extends AMQPEvent> getEntityClass() {
		return AMQPEvent.class;
	}

	@Override
	@Transient
	public Long getEntityIdObject() {
		return getId();
	}

	@Override
	@Transient
	public boolean isLoggable() {
		return false;
	}
}
