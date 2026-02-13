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
@Table(name = SubstituicaoMagistrado.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "sequence_generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_substituicao_magistrado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SubstituicaoMagistrado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SubstituicaoMagistrado,Integer> {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_substituicao_magistrado";

	private Integer idSubstituicaoMagistrado;
	
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private OrgaoJulgadorCargo cargoDistribuicao;
	private PessoaMagistrado magistradoAfastado;
	
	private PessoaMagistrado magistradoSubstituto;
	private OrgaoJulgadorCargo cargoMagistradoSubstituto;
	
	private Date dataInicio;
	private Date dataFim;
	private Boolean estruturaGabineteCedida;
	private String observacao;
	private String avisosVisibilidade;
	
	private Date dataCriacao;
	private Date dataUltimaAtualizacao;
	
	
	/**
	 * Recupera o identificador da substituição
	 * @return
	 */
	@Id
	@Column(name = "id_substituicao_magistrado", unique = true, nullable = false, updatable = false)
	@NotNull
	@GeneratedValue(generator = "sequence_generator")
	public Integer getIdSubstituicaoMagistrado() {
		return idSubstituicaoMagistrado;
	}
	
	/**
	 * Atribui um identificador para a substituição.
	 * @param idSubstituicaoMagistrado
	 */
	public void setIdSubstituicaoMagistrado(Integer idSubstituicaoMagistrado) {
		this.idSubstituicaoMagistrado = idSubstituicaoMagistrado;
	}

	
	/**
	 * Recupera o órgão julgador onde ocorre a substituição.
	 * @return o órgão julgador em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	/**
	 * Atribui o órgão julgador onde ocorre a substituição
	 * @param orgaoJulgador a ser atribuído.
	 */
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	

	/**
	 * Recupera o órgão julgador colegiado onde ocorre a substituição.
	 * @return o órgão julgador colegiado em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado", nullable = false)
	@NotNull
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	/**
	 * Atribui o órgão julgador colegiado onde ocorre a substituição
	 * @param orgaoJulgadorColegiado a ser atribuído.
	 */
	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	
	/**
	 * Recupera o cargo de distribuição que o substituto atuará durante a substituição.
	 * @return o cargo judicial em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo_distribuicao", nullable = false)
	@NotNull
	public OrgaoJulgadorCargo getCargoDistribuicao() {
		return cargoDistribuicao;
	}

	/**
	 * Atribui o cargo de distribuição que o substituto atuará durante a substituição.
	 * @param cargoDistribuicao a ser atribuído.
	 */
	public void setCargoDistribuicao(OrgaoJulgadorCargo cargoDistribuicao) {
		this.cargoDistribuicao = cargoDistribuicao;
	}

	/**
	 * Recupera o magistrado substituto que atua nessa substituição.
	 * @return o magistrado substituto em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_magistrado_substituto", nullable = false)
	@NotNull
	public PessoaMagistrado getMagistradoSubstituto() {
		return magistradoSubstituto;
	}

	/**
	 * Atribui o magistrado substituto que atua nessa substituição.
	 * @param magistradoSubstituto a ser atribuído.
	 */
	public void setMagistradoSubstituto(PessoaMagistrado magistradoSubstituto) {
		this.magistradoSubstituto = magistradoSubstituto;
	}
	
	
	/**
	 * Recupera o magistrado afastado referente a substituição..
	 * @return o magistrado afastado em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_magistrado_afastado")
	public PessoaMagistrado getMagistradoAfastado() {
		return magistradoAfastado;
	}

	/**
	 * Atribui o magistrado afastado referente a essa substituição.
	 * @param magistradoSubstituto a ser atribuído.
	 */
	public void setMagistradoAfastado(PessoaMagistrado magistradoAfastado) {
		this.magistradoAfastado = magistradoAfastado;
	}
	
	/**
	 * Recupera o cargo judicial pessoal do magistrado substituto no OJ em que ocorre a substituição.
	 * @return o cargo em questão
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo_substituto", nullable = false)
	@NotNull
	public OrgaoJulgadorCargo getCargoMagistradoSubstituto() {
		return cargoMagistradoSubstituto;
	}

	/**
	 * Atribui o cargo judicial pessoal do magistrado substituto no OJ em que ocorre a substituição.
	 * @param cargoMagistradoSubstituto a ser atribuído.
	 */
	public void setCargoMagistradoSubstituto(OrgaoJulgadorCargo cargoMagistradoSubstituto) {
		this.cargoMagistradoSubstituto = cargoMagistradoSubstituto;
	}

	/**
	 * Recupera a data inicial do período de substituição.
	 * @return
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio_substituicao", nullable = false)
	@NotNull
	public Date getDataInicio() {
		return dataInicio;
	}
	
	/**
	 * Atribui a data inicial do período de substituição.
	 * @param dataInicio
	 */
	public void setDataInicio(Date dataInicio){
		this.dataInicio = dataInicio;
	}
	
	/**
	 * Recupera a data final do período de substituição.
	 * @return
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim_substituicao", nullable = false)
	@NotNull
	public Date getDataFim() {
		return dataFim;
	}
	
	/**
	 * Atribui a data final do período de substituição.
	 * @param dataFim
	 */
	public void setDataFim(Date dataFim){
		this.dataFim = dataFim;
	}
	
	/**
	 * Indica se o magistrado substituto recebeu estrutura de gabinete do magistrado afastado.
	 * @return true caso a estrutura de gabinete tenha sido cedida.
	 */
	@Column(name = "in_mag_subst_rec_est_gabinete", nullable = false)
	@NotNull
	public Boolean getEstruturaGabineteCedida(){
		return estruturaGabineteCedida;
	}
	
	/**
	 * Atribui a informção referente a estrutura de gabinete do magistrado afastado ter sido cedida ou não. 
	 * @param estruturaGabineteCedida
	 */
	public void setEstruturaGabineteCedida(Boolean estruturaGabineteCedida ){
		this.estruturaGabineteCedida = estruturaGabineteCedida;
	}
	
	/**
	 * Recupera uma possível observação referente a substituição
	 * @return observação em questão.
	 */
	@Column(name = "ds_observacao")
	public String getObservacao(){
		return observacao;
	}
	
	/**
	 * Atribui uma possível observação a substituição
	 * @param observacao
	 */
	public void setObservacao(String observacao){
		this.observacao = observacao;
	}
	
	
	/**
	 * Recupera possíveis avisos de visibilidade
	 * que foram geradas na última tentativa de gravação 
	 * da substituição 
	 * @return avisos em questão
	 */
	@Column(name = "ds_avisos_visibilidade")
	public String getAvisosVisibilidade(){
		return avisosVisibilidade;
	}
	
	/**
	 * Atribui avisos de visibilidade
	 * @param avisoVisibilidade a ser atribuído
	 */
	public void setAvisosVisibilidade(String avisosVisibilidade){
		this.avisosVisibilidade = avisosVisibilidade;
	}
	
	
	/**
	 * Recupera a data de criação da substituição.
	 * @return a referida data de criação.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao", nullable = false)
	@NotNull
	public Date getDataCriacao() {
		return dataCriacao;
	}
	
	/**
	 * Atribui a data de criação da substituição
	 * @param dataCriacao
	 */
	public void setDataCriacao(Date dataCriacao){
		this.dataCriacao = dataCriacao;
	}
	
	/**
	 * Recupera a data da última atualização da substituição.
	 * @return a referida data de última atualização.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultima_atualizacao", nullable = false)
	@NotNull
	public Date getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}
	
	/**
	 * Atribui a data da última atualização da substituição
	 * @param dataUltimaAtualizacao
	 */
	public void setDataUltimaAtualizacao(Date dataUltimaAtualizacao){
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SubstituicaoMagistrado)) {
			return false;
		}
		
		SubstituicaoMagistrado other = (SubstituicaoMagistrado) obj;
		return (getIdSubstituicaoMagistrado() == other.getIdSubstituicaoMagistrado());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdSubstituicaoMagistrado();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SubstituicaoMagistrado> getEntityClass() {
		return SubstituicaoMagistrado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdSubstituicaoMagistrado();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
