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
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.je.entidades.Eleicao;

/**
 * Entidade destinada a permitir armazenar informações que podem ser utilizadas para o
 * reconhecimento de uma situação de dependência permanente segundo algum critério.
 * 
 * Exemplo disso acontece quando um recurso eleitoral é apresentado ao tribunal regional eleitoral
 * ou ao tribunal superior eleitoral em relação a uma específica eleição e uma unidade federativa,
 * conforme previsto no art. 260 do Código Eleitoral atual. 
 * 
 */
@Entity
@Table(name="tb_vinc_depend_eleitoral")
@org.hibernate.annotations.GenericGenerator(name = "gen_vinc_depend_eleitoral", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_vinc_depend_eleitoral"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class VinculacaoDependenciaEleitoral implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<VinculacaoDependenciaEleitoral,Integer> {
	
	private static final long serialVersionUID = -952623937833709284L;

	private Integer id;
	
	private OrgaoJulgadorCargo cargoJudicial;
	
	private Eleicao eleicao;
	
	private Estado estado;
	
	private Municipio municipio;
	
	private List<ComplementoProcessoJE> complementosProcessoJE;

	@Id
	@GeneratedValue(generator = "gen_vinc_depend_eleitoral")
	@Column(name = "id_vinc_depend_eleitoral", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne(optional=false)
	@JoinColumn(name="id_eleicao")
	public Eleicao getEleicao() {
		return eleicao;
	}

	public void setEleicao(Eleicao eleicao) {
		this.eleicao = eleicao;
	}

	@ManyToOne(optional=true)
	@JoinColumn(name="id_estado")
	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	@ManyToOne(optional=true)
	@JoinColumn(name="id_municipio")
	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	@ManyToOne(optional=true)
	@JoinColumn(name="id_orgao_julgador_cargo")
	public OrgaoJulgadorCargo getCargoJudicial() {
		return cargoJudicial;
	}
	
	public void setCargoJudicial(OrgaoJulgadorCargo cargoJudicial) {
		this.cargoJudicial = cargoJudicial;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "vinculacaoDependenciaEleitoral")
	public List<ComplementoProcessoJE> getComplementosProcessoJE() {
		return complementosProcessoJE;
	}

	public void setComplementosProcessoJE(
			List<ComplementoProcessoJE> complementosProcessoJE) {
		this.complementosProcessoJE = complementosProcessoJE;
	}
	
	@Transient
	public String getEstadoMunicipio(){
		String retorno;
		if(getEstado() != null){
			retorno = getEstado().getEstado();
		} else {
			retorno = getMunicipio().getMunicipio();
		}
		return retorno;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends VinculacaoDependenciaEleitoral> getEntityClass() {
		return VinculacaoDependenciaEleitoral.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
