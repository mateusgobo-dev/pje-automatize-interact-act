/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.je.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;

/**
 * Entidade representativa de informações complementares pertinentes a processos 
 * judiciais eleitorais.
 */
@Entity
@Table(name = "tb_complemento_processo_je")
@org.hibernate.annotations.GenericGenerator(name = "gen_comp_proc_je", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_complemento_processo_je"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ComplementoProcessoJE implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "gen_comp_proc_je")
	@Column(name = "id_complemento_processo_je", unique = true, nullable = false)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_eleicao", nullable = true)
	private Eleicao eleicao;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estado", nullable = true)
	private Estado estadoEleicao;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_municipio", nullable = true)
	private Municipio municipioEleicao;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false, updatable = false)
	@NotNull
	private ProcessoTrf processoTrf;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_vinc_depend_eleitoral", nullable = true)
	private VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_atualizacao")
	private Date dtAtualizacao;

	@Column(name = "is_paradigma")
	private Boolean paradigma;
	
	/**
	 * Recupera o identificador desta entidade.
	 * 
	 * @return o identificador
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 * Atribui a esta entidade um identificador. 
	 * Em razão do mapeamento JPA, não deve ser utilizado pelo desenvolvedor.
	 * 
	 * @param id o identificador a ser atribuído.
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	public Date getDtAtualizacao() {
		return dtAtualizacao;
	}

	public void setDtAtualizacao(Date dtAtualizacao) {
		this.dtAtualizacao = dtAtualizacao;
	}
	 	
	 	
	/**
	 * Recupera a {@link Eleicao} a que está vinculado este processo, se existente.
	 * 
	 * @return a {@link Eleicao} vinculada, ou null, se não se tratar de processo
	 * com eleição associada.
	 */
	public Eleicao getEleicao() {
		return this.eleicao;
	}

	/**
	 * Atribui a esta entidade uma {@link Eleicao} vinculada.
	 * 
	 * @param eleicao a {@link Eleicao} a ser vinculada.
	 */
	public void setEleicao(Eleicao eleicao) {
		this.eleicao = eleicao;
	}

	/**
	 * Recupera a unidade federativa ({@link Estado}) na qual houve a eleição 
	 * de que trata o processo judicial.
	 * 
	 * @return a unidade federativa, ou null, se não houver unidade associada
	 */
	public Estado getEstadoEleicao() {
		return this.estadoEleicao;
	}

	/**
	 * Atribui a esta entidade uma unidade federativa ({@link Estado}) no qual
	 * foi realizada a eleição.
	 * 
	 * @param estadoEleicao o {@link Estado} em que se realizou a eleição
	 */
	public void setEstadoEleicao(Estado estadoEleicao) {
		this.estadoEleicao = estadoEleicao;
	}

	/**
	 * Recupera o município ({@link Municipio}) no qual houve a eleição 
	 * de que trata o processo judicial.
	 * 
	 * @return o município, ou null, se não houver município associado
	 */
	public Municipio getMunicipioEleicao() {
		return this.municipioEleicao;
	}

	/**
	 * Atribui a esta entidade um {@link Municipio}) no qual
	 * foi realizada a eleição.
	 * 
	 * @param municipioEleicao o município a ser vinculado
	 */
	public void setMunicipioEleicao(Municipio municipioEleicao) {
		this.municipioEleicao = municipioEleicao;
	}

	/**
	 * Recupera o processo judicial a que está vinculado este complemento.
	 * 
	 * @return o processo judicial.
	 */
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	/**
	 * Vincula este complemento de informações a um processo judicial.
	 * 
	 * @param processoTrf o processo judicial a ser vinculado
	 */
	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	/**
	 * Recupera, quando existente, informação relativa à dependência eleitoral
	 * para apreciação de recursos pertinentes a uma dada eleição.
	 * 
	 * @return a informação relativa à dependência eleitoral
	 */
	public VinculacaoDependenciaEleitoral getVinculacaoDependenciaEleitoral() {
		return vinculacaoDependenciaEleitoral;
	}

	/**
	 * Atribui a este complemento informação relativa à dependência eleitoral para
	 * a apreciação de recursos pertinentes a uma dada eleição.
	 * 
	 * @param vinculacaoDependenciaEleitoral a informação a ser atribuída
	 */
	public void setVinculacaoDependenciaEleitoral(
			VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral) {
		this.vinculacaoDependenciaEleitoral = vinculacaoDependenciaEleitoral;
	}
	
	public Boolean getParadigma() {
		return paradigma;
	}

	public void setParadigma(Boolean paradigma) {
		this.paradigma = paradigma;
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
		if (!(obj instanceof ComplementoProcessoJE))
			return false;
		ComplementoProcessoJE other = (ComplementoProcessoJE) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ComplementoProcessoJE [id=" + id + ", eleicao=" + eleicao
				+ ", estadoEleicao=" + estadoEleicao + ", municipioEleicao="
				+ municipioEleicao + ", processoTrf=" + processoTrf + "]";
	}

}
