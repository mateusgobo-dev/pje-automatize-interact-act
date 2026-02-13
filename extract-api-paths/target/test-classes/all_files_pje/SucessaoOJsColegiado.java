package br.jus.pje.nucleo.entidades;


import java.util.Date;

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
import javax.validation.constraints.NotNull;

@Entity
@Table(name = SucessaoOJsColegiado.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "sequence_generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sucessao_ojs_colegiado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SucessaoOJsColegiado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SucessaoOJsColegiado,Integer> {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_sucessao_ojs_colegiado";

	private Integer idSucessaoOJsColegiado;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private OrgaoJulgador orgaoJulgadorSucedido;
	private OrgaoJulgador orgaoJulgadorSucessor;
	private Date dataSucessao;
	private String observacao;
	
	
	/**
	 * Recupera o identificador da sucessão
	 * @return
	 */
	@Id
	@Column(name = "id_sucessao_ojs_colegiado", unique = true, nullable = false, updatable = false)
	@NotNull
	@GeneratedValue(generator = "sequence_generator")
	public Integer getIdSucessaoOJsColegiado() {
		return idSucessaoOJsColegiado;
	}
	
	/**
	 * Atribui um identificador para a sucessão.
	 * @param idSucessaoOJsColegiado ID a ser atribuído
	 */
	public void setIdSucessaoOJsColegiado(Integer idSucessaoOJsColegiado) {
		this.idSucessaoOJsColegiado = idSucessaoOJsColegiado;
	}

	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado", nullable = false)
	@NotNull
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_sucedido", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgadorSucedido() {
		return orgaoJulgadorSucedido;
	}

	public void setOrgaoJulgadorSucedido(OrgaoJulgador orgaoJulgadorSucedido) {
		this.orgaoJulgadorSucedido = orgaoJulgadorSucedido;
	}


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_sucessor", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgadorSucessor() {
		return orgaoJulgadorSucessor;
	}

	public void setOrgaoJulgadorSucessor(OrgaoJulgador orgaoJulgadorSucessor) {
		this.orgaoJulgadorSucessor = orgaoJulgadorSucessor;
	}

	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_sucessao", nullable = false)
	@NotNull
	public Date getDataSucessao() {
		return dataSucessao;
	}

	public void setDataSucessao(Date dataSucessao) {
		this.dataSucessao = dataSucessao;
	}

	@Column(name = "ds_observacao")	
	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SucessaoOJsColegiado)) {
			return false;
		}
		
		SucessaoOJsColegiado other = (SucessaoOJsColegiado) obj;
		return (getIdSucessaoOJsColegiado() == other.getIdSucessaoOJsColegiado());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdSucessaoOJsColegiado();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SucessaoOJsColegiado> getEntityClass() {
		return SucessaoOJsColegiado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdSucessaoOJsColegiado();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
