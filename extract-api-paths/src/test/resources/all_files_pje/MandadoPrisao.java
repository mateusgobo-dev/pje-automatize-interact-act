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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.RegimePenaEnum;
import br.jus.pje.nucleo.enums.SituacaoMandadoPrisaoEnum;
import br.jus.pje.nucleo.enums.TipoPrisaoEnum;

@Entity
@Table(name = "tb_mandado_prisao")
@PrimaryKeyJoinColumn(name = "id_mandado_prisao")
public class MandadoPrisao extends MandadoAlvara {

	private static final long serialVersionUID = 1L;

	private Date dataRecaptura;
	private Date dataValidade;
	private Boolean publicacaoRestrita;
	private Boolean recaptura;
	private TipoPrisaoEnum tipoPrisao;
	private Double valorFianca;
	private Integer prazoPrisao;// em dias
	private Integer diasPrisaoTemporaria;
	private Boolean prisaoFlagrante;
	private MandadoPrisao mandadoPrisaoOrigemRecaptura;

	private TipoPena tipoPena;
	private RegimePenaEnum regimePena;
	private Integer anosPena = 0;
	private Integer mesesPena = 0;
	private Integer diasPena = 0;
	private Integer horasPena = 0;
	
	private EstabelecimentoPrisional estabelecimentoPrisionalCumprimento;

	private List<MandadoPrisao> mandadosSolicitantesRecaptura;
	private List<AlvaraSoltura> alvarasSolicitantes = new ArrayList<AlvaraSoltura>();
	private List<ContraMandado> contraMandados = new ArrayList<ContraMandado>();
	private List<MandadoPrisaoComprovante> comprovantes = new ArrayList<MandadoPrisaoComprovante>();

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_recaptura", nullable = true)
	public Date getDataRecaptura() {
		return dataRecaptura;
	}

	public void setDataRecaptura(Date dataRecaptura) {
		this.dataRecaptura = dataRecaptura;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	@Column(name = "dt_validade", nullable = false)
	public Date getDataValidade() {
		return dataValidade;
	}

	public void setDataValidade(Date dataValidade) {
		this.dataValidade = dataValidade;
	}

	@NotNull
	@Column(name = "in_publicacao_restrira", nullable = false)
	public Boolean getPublicacaoRestrita() {
		return publicacaoRestrita;
	}

	public void setPublicacaoRestrita(Boolean publicacaoRestrita) {
		this.publicacaoRestrita = publicacaoRestrita;
	}

	@NotNull
	@Column(name = "in_recaptura", nullable = false)
	public Boolean getRecaptura() {
		return recaptura;
	}

	public void setRecaptura(Boolean recaptura) {
		this.recaptura = recaptura;
	}

	@NotNull
	@Column(name = "in_tipo_prisao", nullable = false)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoPrisaoType")
	public TipoPrisaoEnum getTipoPrisao() {
		return tipoPrisao;
	}

	public void setTipoPrisao(TipoPrisaoEnum tipoPrisao) {
		this.tipoPrisao = tipoPrisao;
	}

	@Column(name = "vl_fianca", nullable = true)
	public Double getValorFianca() {
		return valorFianca;
	}

	public void setValorFianca(Double valorFianca) {
		this.valorFianca = valorFianca;
	}

	@Column(name = "nr_dias_prisao", nullable = true)
	public Integer getPrazoPrisao() {
		return prazoPrisao;
	}

	public void setPrazoPrisao(Integer prazaoPrisao) {
		this.prazoPrisao = prazaoPrisao;
	}

	@Column(name = "nr_dias_prisao_temp", nullable = true)
	public Integer getDiasPrisaoTemporaria() {
		return diasPrisaoTemporaria;
	}

	public void setDiasPrisaoTemporaria(Integer diasPrisaoTemporaria) {
		this.diasPrisaoTemporaria = diasPrisaoTemporaria;
	}

	@NotNull
	@Column(name = "in_prisao_flagrante")
	public Boolean getPrisaoFlagrante() {
		return prisaoFlagrante;
	}

	public void setPrisaoFlagrante(Boolean prisaoFlagrante) {
		this.prisaoFlagrante = prisaoFlagrante;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_mnddo_prso_origem_recaptura")
	public MandadoPrisao getMandadoPrisaoOrigemRecaptura() {
		return mandadoPrisaoOrigemRecaptura;
	}

	public void setMandadoPrisaoOrigemRecaptura(MandadoPrisao mandadoPrisaoOrigemRecaptura) {
		this.mandadoPrisaoOrigemRecaptura = mandadoPrisaoOrigemRecaptura;
	}

	@ManyToOne
	@JoinColumn(name = "id_tipo_pena")
	public TipoPena getTipoPena() {
		return tipoPena;
	}

	public void setTipoPena(TipoPena tipoPena) {
		this.tipoPena = tipoPena;
	}

	@Column(name = "in_regime_pena")
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.RegimePenaType")
	public RegimePenaEnum getRegimePena() {
		return regimePena;
	}

	public void setRegimePena(RegimePenaEnum regimePena) {
		this.regimePena = regimePena;
	}

	@Max(9999)
	@Column(name = "qd_anos_pena")
	public Integer getAnosPena() {
		return anosPena;
	}

	public void setAnosPena(Integer anosPena) {
		this.anosPena = anosPena;
	}

	@Max(11)
	@Column(name = "qd_meses_pena")
	public Integer getMesesPena() {
		return mesesPena;
	}

	public void setMesesPena(Integer mesesPena) {
		this.mesesPena = mesesPena;
	}

	@Max(29)
	@Column(name = "qd_dias_pena")
	public Integer getDiasPena() {
		return diasPena;
	}

	public void setDiasPena(Integer diasPena) {
		this.diasPena = diasPena;
	}

	@Max(23)
	@Column(name = "qd_horas_pena")
	public Integer getHorasPena() {
		return horasPena;
	}

	public void setHorasPena(Integer horasPena) {
		this.horasPena = horasPena;
	}	
	
	@ManyToOne
	@JoinColumn(name="id_estabelecimento_prisional")
	public EstabelecimentoPrisional getEstabelecimentoPrisionalCumprimento(){
		return estabelecimentoPrisionalCumprimento;
	}	
	
	public void setEstabelecimentoPrisionalCumprimento(EstabelecimentoPrisional estabelecimentoPrisionalCumprimento){
		this.estabelecimentoPrisionalCumprimento = estabelecimentoPrisionalCumprimento;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "mandadoPrisaoOrigemRecaptura")
	public List<MandadoPrisao> getMandadosSolicitantesRecaptura() {
		return mandadosSolicitantesRecaptura;
	}

	public void setMandadosSolicitantesRecaptura(List<MandadoPrisao> mandadosSolicitantesRecaptura) {
		this.mandadosSolicitantesRecaptura = mandadosSolicitantesRecaptura;
	}

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "mandadosAlcancados")
	public List<AlvaraSoltura> getAlvarasSolicitantes() {
		return alvarasSolicitantes;
	}

	public void setAlvarasSolicitantes(List<AlvaraSoltura> alvarasSolicitantes) {
		this.alvarasSolicitantes = alvarasSolicitantes;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mandadoPrisao")
	public List<ContraMandado> getContraMandados() {
		return contraMandados;
	}

	public void setContraMandados(List<ContraMandado> contraMandados) {
		this.contraMandados = contraMandados;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "mandadoPrisao")
	public List<MandadoPrisaoComprovante> getComprovantes() {
		return comprovantes;
	}

	public void setComprovantes(List<MandadoPrisaoComprovante> comprovantes) {
		this.comprovantes = comprovantes;
	}

	@Transient
	public boolean isCamposPenaTotalOk() {
		if (getTipoPena() != null && getRegimePena() != null
				&& (getAnosPena() != null || getMesesPena() != null || getDiasPena() != null || getHorasPena() != null)) {
			return true;
		}

		return false;
	}

	@Transient
	public void copiarDadosPenalTotal(PenaTotal penaTotal) {
		if (penaTotal != null) {
			tipoPena = penaTotal.getTipoPena();
			regimePena = penaTotal.getRegimePena();
			anosPena = penaTotal.getAnosPenaInicial();
			mesesPena = penaTotal.getMesesPenaInicial();
			diasPena = penaTotal.getDiasPenaInicial();
			horasPena = penaTotal.getHorasPenaInicial();
		}
	}

	@Transient
	public String getDescricaoPena() {
		if(getAnosPena() != null || getMesesPena() != null || getDiasPena() != null || getHorasPena() != null){
			String returnValue = "";

			if (getAnosPena() != null){
				returnValue += getAnosPena() + " Ano(s)";
			}

			if (getMesesPena() != null){
				returnValue += (returnValue.isEmpty() ? "" : ", ") + getMesesPena() + " Mese(s)";
			}

			if (getDiasPena() != null){
				returnValue += (returnValue.isEmpty() ? "" : ", ") + getDiasPena() + " Dia(s)";
			}

			if (getHorasPena() != null){
				returnValue += (returnValue.isEmpty() ? "" : ", ") + getHorasPena() + " Hora(s)";
			}

			return returnValue;
		}
		
		return null;
	}
	
	@Transient
	public SituacaoMandadoPrisaoEnum getSituacaoAtualBNMP(){		
		if(getComprovantes() != null && !getComprovantes().isEmpty()){
			Collections.sort(getComprovantes());
			return getComprovantes().get(getComprovantes().size()-1).getSituacaoMandadoPrisao();
		}
		
		return null;
	}

	@Override
	@Transient
	public Class<? extends ProcessoExpedienteCriminal> getEntityClass() {
		return MandadoPrisao.class;
	}
}
