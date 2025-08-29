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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.TipoSolturaEnum;

@Entity
@Table(name = "tb_alvara_soltura")
@PrimaryKeyJoinColumn(name = "id_alvara_soltura")
public class AlvaraSoltura extends MandadoAlvara {

	private static final long serialVersionUID = 1L;

	private TipoSolturaEnum tipoSoltura;
	private String numerosProcessosAlcancados;
	private Boolean cumpridoComSultura;
	private PessoaMagistrado pessoaMagistradoCumprimento;
	private List<MandadoPrisao> mandadosAlcancados = new ArrayList<MandadoPrisao>();

	@NotNull
	@Column(name = "in_tipo_soltura", length = 3)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoSolturaType")
	public TipoSolturaEnum getTipoSoltura() {
		return tipoSoltura;
	}

	public void setTipoSoltura(TipoSolturaEnum tipoSoltura) {
		this.tipoSoltura = tipoSoltura;
	}

	@Length(max = 300)
	@Column(name = "nrs_processos_alcancados", nullable = true, length = 300)
	public String getNumerosProcessosAlcancados() {
		return numerosProcessosAlcancados;
	}

	public void setNumerosProcessosAlcancados(String numerosProcessosAlcancados) {
		this.numerosProcessosAlcancados = numerosProcessosAlcancados;
	}
	
	@Column(name = "in_cumprido_soltura")
	public Boolean getCumpridoComSultura() {
		return cumpridoComSultura;
	}

	public void setCumpridoComSultura(Boolean cumpridoComSultura) {
		this.cumpridoComSultura = cumpridoComSultura;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa_magistrado")
	public PessoaMagistrado getPessoaMagistradoCumprimento() {
		return pessoaMagistradoCumprimento;
	}

	public void setPessoaMagistradoCumprimento(PessoaMagistrado pessoaMagistradoCumprimento) {
		this.pessoaMagistradoCumprimento = pessoaMagistradoCumprimento;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_mandados_alcancados", joinColumns = { @JoinColumn(name = "id_alvara_soltura", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_mandado_prisao", nullable = false, updatable = false) })
	public List<MandadoPrisao> getMandadosAlcancados() {
		return mandadosAlcancados;
	}

	public void setMandadosAlcancados(List<MandadoPrisao> mandadosAlcancados) {
		this.mandadosAlcancados = mandadosAlcancados;
	}

	@Override
	@Transient
	public Class<? extends ProcessoExpedienteCriminal> getEntityClass() {
		return AlvaraSoltura.class;
	}
}
