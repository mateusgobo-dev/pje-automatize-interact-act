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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.UnidadeMultaEnum;

@Entity
@Table(name = "tb_pena")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_pena", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pena"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Pena implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<Pena,Integer>{

	private static final long serialVersionUID = -5707643259783985799L;

	private Integer id;
	private TipoPena tipoPena;
	private String observacoes;
	private UnidadeMultaEnum unidadeMulta;
	private List<Pena> penasSubstitutivas = new ArrayList<Pena>(0);
	private List<Pena> penasOriginais = new ArrayList<Pena>(0);
	private IcrSentencaCondenatoria icrSentencaCondenatoria;

	// --------- genero reclusão --------
	private Integer anosPenaInicial;
	private Integer mesesPenaInicial;
	private Integer diasPenaInicial;
	private Integer horasPenaInicial;
	// --------- genero multa:dias/multa ----
	private Integer diasMulta;
	private Double valorFracaoDiaMultaSalarioMinimo;
	private Double multiplicadorPena;
	private Double valorHistoricoPrevisto;
	// --------- genero multa:valor ---------
	private UnidadeMonetaria unidadeMonetaria;
	private Double valorMulta;
	// --------- restrição de direito -------
	private String descricaoLocal;
	private String descricaoBem;

	@Id
	@GeneratedValue(generator = "gen_pena")
	@Column(name = "id_pena")
	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_tipo_pena", nullable = false)
	public TipoPena getTipoPena(){
		return tipoPena;
	}

	public void setTipoPena(TipoPena tipoPena){
		this.tipoPena = tipoPena;
	}

	@Max(9999)
	@Column(name = "qd_anos_pena_inicial")
	public Integer getAnosPenaInicial(){
		return anosPenaInicial;
	}

	public void setAnosPenaInicial(Integer anosPenaInicial){
		this.anosPenaInicial = anosPenaInicial;
	}

	@Max(11)
	@Column(name = "qd_meses_pena_inicial")
	public Integer getMesesPenaInicial(){
		return mesesPenaInicial;
	}

	public void setMesesPenaInicial(Integer mesesPenaInicial){
		this.mesesPenaInicial = mesesPenaInicial;
	}

	@Max(29)
	@Column(name = "qd_dias_pena_inicial")
	public Integer getDiasPenaInicial(){
		return diasPenaInicial;
	}

	public void setDiasPenaInicial(Integer diasPenaInicial){
		this.diasPenaInicial = diasPenaInicial;
	}

	@Max(23)
	@Column(name = "qd_horas_pena_inicial")
	public Integer getHorasPenaInicial(){
		return horasPenaInicial;
	}

	public void setHorasPenaInicial(Integer horasPenaInicial){
		this.horasPenaInicial = horasPenaInicial;
	}

	@Column(name = "in_unidade_multa")
	@Enumerated(EnumType.STRING)
	public UnidadeMultaEnum getUnidadeMulta(){
		return unidadeMulta;
	}

	public void setUnidadeMulta(UnidadeMultaEnum unidadeMulta){
		this.unidadeMulta = unidadeMulta;
	}

	@Column(name = "qd_dias_multa")
	public Integer getDiasMulta(){
		return diasMulta;
	}

	public void setDiasMulta(Integer diasMulta){
		this.diasMulta = diasMulta;
	}

	@Column(name = "vl_frco_dia_multa_slrio_minimo")
	public Double getValorFracaoDiaMultaSalarioMinimo(){
		return valorFracaoDiaMultaSalarioMinimo;
	}

	public void setValorFracaoDiaMultaSalarioMinimo(Double valorFracaoDiaMultaSalarioMinimo){
		this.valorFracaoDiaMultaSalarioMinimo = valorFracaoDiaMultaSalarioMinimo;
	}

	@Column(name = "multiplicador_pena")
	public Double getMultiplicadorPena(){
		return multiplicadorPena;
	}

	public void setMultiplicadorPena(Double multiplicadorPena){
		this.multiplicadorPena = multiplicadorPena;
	}

	@Column(name = "vl_historico_previsto")
	public Double getValorHistoricoPrevisto(){
		return valorHistoricoPrevisto;
	}

	public void setValorHistoricoPrevisto(Double valorHistoricoPrevisto){
		this.valorHistoricoPrevisto = valorHistoricoPrevisto;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_unidade_monetaria")
	public UnidadeMonetaria getUnidadeMonetaria(){
		return unidadeMonetaria;
	}

	public void setUnidadeMonetaria(UnidadeMonetaria unidadeMonetaria){
		this.unidadeMonetaria = unidadeMonetaria;
	}

	@Column(name = "vl_pena")
	public Double getValorMulta(){
		return valorMulta;
	}

	public void setValorMulta(Double valorMulta){
		this.valorMulta = valorMulta;
	}

	@Length(max = 255)
	@Column(name = "ds_bem")
	public String getDescricaoBem(){
		return descricaoBem;
	}

	public void setDescricaoBem(String descricaoBem){
		this.descricaoBem = descricaoBem;
	}

	@Length(max = 300)
	@Column(name = "ds_local")
	public String getDescricaoLocal(){
		return descricaoLocal;
	}

	public void setDescricaoLocal(String descricaoLocal){
		this.descricaoLocal = descricaoLocal;
	}

	@Length(max = 400)
	@Column(name = "ds_obs")
	public String getObservacoes(){
		return observacoes;
	}

	public void setObservacoes(String observacoes){
		this.observacoes = observacoes;
	}

	@Transient
	public int getPenaReclusaoEmHoras(){
		int penaReclusaoEmHoras = 0;
		int anos = getAnosPenaInicial() != null ? getAnosPenaInicial() : 0;
		int meses = getMesesPenaInicial() != null ? getMesesPenaInicial() : 0;
		int dias = getDiasPenaInicial() != null ? getDiasPenaInicial() : 0;
		int horas = getHorasPenaInicial() != null ? getHorasPenaInicial() : 0;

		penaReclusaoEmHoras = ((anos * 12) * 365) * 24;
		penaReclusaoEmHoras = penaReclusaoEmHoras + ((meses * 365) * 24);
		penaReclusaoEmHoras = penaReclusaoEmHoras + (dias * 24);
		penaReclusaoEmHoras = penaReclusaoEmHoras + horas;

		return penaReclusaoEmHoras;
	}

	@Transient
	public String getDescricaoTotalPena(){
		String returnValue = "";

		if (getAnosPenaInicial() != null){
			returnValue += getAnosPenaInicial() + " Ano(s)";
		}

		if (getMesesPenaInicial() != null){
			returnValue += (returnValue.isEmpty() ? "" : ", ") + getMesesPenaInicial() + " Mese(s)";
		}

		if (getDiasPenaInicial() != null){
			returnValue += (returnValue.isEmpty() ? "" : ", ") + getDiasPenaInicial() + " Dia(s)";
		}

		if (getHorasPenaInicial() != null){
			returnValue += (returnValue.isEmpty() ? "" : ", ") + getHorasPenaInicial() + " Hora(s)";
		}

		return returnValue;
	}

	@Transient
	public String getDetalhesPenaOriginal(){
		String result = "";
		if (getTipoPena() != null){
			if (getDiasMulta() != null && getDiasMulta() != 0){
				result += "Qd. dias: " + getDiasMulta() + " - ";
			}

			if (getValorFracaoDiaMultaSalarioMinimo() != null && getValorFracaoDiaMultaSalarioMinimo() != 0){
				result += "Vl. dia multa sal. minimo: " + getValorFracaoDiaMultaSalarioMinimo() + " - ";
			}

			if (getMultiplicadorPena() != null && getMultiplicadorPena() != 0){
				result += "Multiplicador: " + getMultiplicadorPena() + " - ";
			}

			if (getValorHistoricoPrevisto() != null && getValorHistoricoPrevisto() != 0){
				result += "Vl. Hist. Previsto: " + getValorHistoricoPrevisto() + " - ";
			}

			if (getUnidadeMonetaria() != null && getValorMulta() != null && getValorMulta() > 0){
				result += "Vl. condição: " + getUnidadeMonetaria().getSimbolo() + getValorMulta() + " - ";
			}

			if (getDescricaoLocal() != null){
				result += "Local: " + getDescricaoLocal() + " - ";
			}

			if (getTipoPena() != null && getTipoPena().isExigeDadosPrivativaLiberdade()){
				result += getDescricaoTotalPena() + " - ";
			}

			if (result != null && !result.trim().equals("")){
				result = result.substring(0, result.lastIndexOf(" - "));
				return result;
			}

			return null;
		}

		return null;
	}

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "tb_pena_substituicao",
			joinColumns = {@JoinColumn(name = "id_pena_substitutiva")},
			inverseJoinColumns = {@JoinColumn(name = "id_pena_original")})
	@Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<Pena> getPenasSubstitutivas(){
		return penasSubstitutivas;
	}

	public void setPenasSubstitutivas(List<Pena> penasSubstitutivas){
		this.penasSubstitutivas = penasSubstitutivas;
	}

	@ManyToMany(cascade ={CascadeType.ALL}, mappedBy = "penasSubstitutivas")
	public List<Pena> getPenasOriginais(){
		return penasOriginais;
	}

	public void setPenasOriginais(List<Pena> penasOriginais){
		this.penasOriginais = penasOriginais;
	}

	@ManyToOne
	@JoinColumn(name = "id_icr_sentenca_condenatoria")
	public IcrSentencaCondenatoria getIcrSentencaCondenatoria(){
		return icrSentencaCondenatoria;
	}

	public void setIcrSentencaCondenatoria(IcrSentencaCondenatoria icrSentencaCondenatoria){
		this.icrSentencaCondenatoria = icrSentencaCondenatoria;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Pena> getEntityClass() {
		return Pena.class;
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
