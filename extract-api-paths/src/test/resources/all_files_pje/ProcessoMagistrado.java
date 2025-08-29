package br.jus.pje.nucleo.entidades;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.TipoAtuacaoDetalhadaMagistradoEnum;
import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;
import br.jus.pje.nucleo.enums.TipoRelacaoProcessoMagistradoEnum;

@Entity
@Table(name = ProcessoMagistrado.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "sequence_generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_processo_magistrado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoMagistrado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoMagistrado,Integer> {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_processo_magistrado";
	
	private Integer idProcessoMagistrado;
	private ProcessoTrf processo;
	private PessoaMagistrado magistrado;
	private OrgaoJulgadorCargo orgaoJulgadorCargo;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private Date dataVinculacao;
	private TipoRelacaoProcessoMagistradoEnum tipoRelacaoProcessoMagistrado;
	private TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado;
	private Boolean magistradoTitular;
	private SubstituicaoMagistrado substituicaoMagistradoVigente;
	private Boolean ativo;
	private String observacao;

	/**
	 * Recupera o identificador da entidade.
	 * @return o identificador da entidade.
	 */
	@Id
	@Column(name = "id_processo_magistrado", unique = true, nullable = false, updatable = false)
	@NotNull
	@GeneratedValue(generator = "sequence_generator")
	public Integer getIdProcessoMagistrado() {
		return idProcessoMagistrado;
	}
	
	/**
	 * Atribui um identificador para a entidade.
	 * @param idProcessoMagistrado o identificador a ser atribuído.
	 */
	public void setIdProcessoMagistrado(Integer idProcessoMagistrado) {
		this.idProcessoMagistrado = idProcessoMagistrado;
	}
	
	/**
	 * Recupera o processo referente a essa associação.
	 * @return o processo em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcesso() {
		return processo;
	}

	/**
	 * Atribui o processo referente a essa associação.
	 * @param processo o processo em questão.
	 */
	public void setProcesso(ProcessoTrf processo) {
		this.processo = processo;
	}
	
	/**
	 * Recupera o magistrado referente a essa associação.
	 * @return o magistrado em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_magistrado", nullable = false)
	@NotNull
	public PessoaMagistrado getMagistrado() {
		return magistrado;
	}

	/**
	 * Atribui o magistrado referente a essa associação.
	 * @param magistrado o magistrado em questão.
	 */
	public void setMagistrado(PessoaMagistrado magistrado) {
		this.magistrado = magistrado;
	}
	
	/**
	 * Recupera a o cargo judicial referente a essa associação.
	 * @return o cargo judicial em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_cargo", nullable = false)
	@NotNull
	public OrgaoJulgadorCargo getOrgaoJulgadorCargo() {
		return orgaoJulgadorCargo;
	}
	
	/**
	 * Atribui o cargo judicial referente a essa associação.
	 * @param orgaoJulgadorCargo o cargo judicial em questão
	 */
	public void setOrgaoJulgadorCargo(OrgaoJulgadorCargo orgaoJulgadorCargo) {
		this.orgaoJulgadorCargo = orgaoJulgadorCargo;
	}

	/**
	 * Recupera a o órgão julgador referente a essa associação.
	 * @return o órgão julgador em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}
	
	/**
	 * Atribui o órgão julgador referente a essa associação.
	 * @param orgaoJulgador o órgão julgador em questão.
	 */
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	/**
	 * Recupera a o órgão julgador colegiado referente a essa associação.
	 * @return o órgão julgador colegiado em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado", nullable = false)
	@NotNull
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}
	
	/**
	 * Atribui o órgão julgador colegiado referente a essa associação.
	 * @param orgaoJulgadorColegiado o órgão julgador colegiado em questão.
	 */
	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}
	
	/**
	 * Recupera a data em que ocorreu a vinculação entre o magistrado e o processo.
	 * @return a data em questão.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_vinculacao", nullable = false)
	@NotNull
	public Date getDataVinculacao() {
		return dataVinculacao;
	}

	/**
	 * Atribui a data em que ocorreu essa vinculação entre o magistrado e o processo.
	 * @param dataVinculacao a data a ser atribuída.
	 */
	public void setDataVinculacao(Date dataVinculacao) {
		this.dataVinculacao = dataVinculacao;
	}
	
	/**
	 * Recupera o tipo da relação entre o processo e o magistrado. 
	 * @return o tipo em questão. 
	 */
	@Column(name = "tp_relacao_processo_magistrado", length = 5, nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull
	public TipoRelacaoProcessoMagistradoEnum getTipoRelacaoProcessoMagistrado() {
		return tipoRelacaoProcessoMagistrado;
	}

	/**
	 * Atribui o tipo de relação entre o processo e o magistrado.
	 * @param tipoRelacaoProcessoMagistrado o tipo em questão.
	 */
	public void setTipoRelacaoProcessoMagistrado(TipoRelacaoProcessoMagistradoEnum tipoVinculacaoProcessoMagistrado) {
		this.tipoRelacaoProcessoMagistrado = tipoVinculacaoProcessoMagistrado;
	}
	
	/**
	 * Recupera o tipo de atuação do magistrado nesse processo. 
	 * @return o tipo de atuação em questão. 
	 */
	@Column(name = "tp_atuacao_magistrado", length = 5, nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull
	public TipoAtuacaoMagistradoEnum getTipoAtuacaoMagistrado() {
		return tipoAtuacaoMagistrado;
	}
	
	/**
	 * Atribui o tipo de atuação do magistrado nesse processo.
	 * @param tipoAtuacaoMagistrado o tipo em questão.
	 */
	public void setTipoAtuacaoMagistrado(TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado) {
		this.tipoAtuacaoMagistrado = tipoAtuacaoMagistrado;
	}
	
	/**
	 * Indica se é um  magistrado atuando de forma titular no processo.
	 * Sendo falso, subentende-se que se trata de uma atuação de magistrado substitituto/auxiliar.  
	 * @return true se for magistrado titular, caso contrário,  retorna false 
	 */
	@Column(name = "in_magistrado_titular", nullable = false)
	@NotNull
	public Boolean getMagistradoTitular() {
		return magistradoTitular;
	}

	/**
	 * Atribui indicador de atuação de magistrado titular.
	 * @param magistradoTitular o indicador em questão.
	 */
	public void setMagistradoTitular(Boolean magistradoTitular) {
		this.magistradoTitular = magistradoTitular;
	}
	
	/**
	 * Caso a vinculação entre o magistrado e o processo tenha se originada por meio da atuação de um magistrado substituto, 
	 * retorna a substituição de magistrado vigente em questão.
	 * Utilizado para fins de controle de meta de quantitativo de vinculações durante uma substituição de magistrado.
	 * @return uma possível substituição de magistrado em questão. 
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_substituicao_magistrado_vigente")
	public SubstituicaoMagistrado getSubstituicaoMagistradoVigente() {
		return substituicaoMagistradoVigente;
	}
	
	/**
	 * Atribui a substituição de magistrado vigente.
	 * @param substituicaoMagistradoVigente substituição de magistrado em questão.
	 */
	public void setSubstituicaoMagistradoVigente(SubstituicaoMagistrado substituicaoMagistradoVigente) {
		this.substituicaoMagistradoVigente = substituicaoMagistradoVigente;
	}
	
	/**
	 * Indica se essa associação entre magistrado e processo está ativa. 
	 * Somente será falso, se ela for desfeita (por redistribuição de processo, por exemplo). 
	 * @return true caso esteja ativa.
	 */
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}
	
	/**
	 * Atribui a situação (ativa ou não) dessa associação.
	 * @param ativo situação a ser atribuida.
	 */
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	/**
	 * Recupera uma possível observação referente a essa associação entre o magistrado e o processo.
	 * @return observação em questão.
	 */
	@Column(name = "ds_observacao")
	public String getObservacao(){
		return observacao;
	}
	
	/**
	 * Atribui uma possível observação referente a essa associação entre o magistrado e o processo.
	 * @param observacao
	 */
	public void setObservacao(String observacao){
		this.observacao = observacao;
	}
	
	@Transient
	public TipoAtuacaoDetalhadaMagistradoEnum getTipoAtuacaoDetalhadaMagistrado() {
		return TipoAtuacaoDetalhadaMagistradoEnum.valueOf(this.tipoRelacaoProcessoMagistrado, this.tipoAtuacaoMagistrado, this.magistradoTitular);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoMagistrado)) {
			return false;
		}
		
		ProcessoMagistrado other = (ProcessoMagistrado) obj;
		return (getIdProcessoMagistrado() == other.getIdProcessoMagistrado());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getIdProcessoMagistrado() == null ? 0 : getIdProcessoMagistrado());
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoMagistrado> getEntityClass() {
		return ProcessoMagistrado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdProcessoMagistrado();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
