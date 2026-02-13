package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "MotivoIsencaoGuia", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = MotivoIsencaoGuia.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_motivo_isencao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_motivo_isencao_guia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1") })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode",
		"persistenceContext" })
public class MotivoIsencaoGuia
		implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<MotivoIsencaoGuia, Integer> {

	private static final long serialVersionUID = -230230347482215165L;

	public static final String TABLE_NAME = "tb_motivo_isencao_guia";

	private Integer id;
	private String dsMotivoIsencao;
	private Boolean inControlaIsencao;
	private Boolean ativo;

	public MotivoIsencaoGuia() {
	}

	@SequenceGenerator(name = "gen_motivo_isencao", sequenceName = "sq_tb_motivo_isencao_guia")
	@Id
	@GeneratedValue(generator = "gen_motivo_isencao")
	@Column(name = "id_motivo_isencao", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "ds_motivo_isencao", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getDsMotivoIsencao() {
		return dsMotivoIsencao;
	}

	public void setDsMotivoIsencao(String dsMotivoIsencao) {
		this.dsMotivoIsencao = dsMotivoIsencao;
	}

	@Column(name = "in_controla_isencao", nullable = false)
	@NotNull
	public Boolean getInControlaIsencao() {
		return inControlaIsencao;
	}

	public void setInControlaIsencao(Boolean inControlaIsencao) {
		this.inControlaIsencao = inControlaIsencao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MotivoIsencaoGuia other = (MotivoIsencaoGuia) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return dsMotivoIsencao;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends MotivoIsencaoGuia> getEntityClass() {
		return MotivoIsencaoGuia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getId());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}