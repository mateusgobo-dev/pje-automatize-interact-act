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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.OcorrenciaLembreteEnum;
import br.jus.pje.nucleo.enums.SituacaoAcompanhamentoIcrTransacaoPenalEnum;
import br.jus.pje.nucleo.enums.UnidadeMultaEnum;

@Entity
@Table(name = "tb_cond_icr_transcao_penal")
@org.hibernate.annotations.GenericGenerator(name = "gen_condcao_icr_transacao_penal", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_condcao_icr_transacao_penal"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CondicaoIcrTransacaoPenal implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<CondicaoIcrTransacaoPenal,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private IcrTransacaoPenal icrTransacaoPenal;
	private TipoPena tipoPena;
	private String observacoes;
	private UnidadeMultaEnum unidadeMulta;
	private Integer diasMulta;
	private Double valorFracaoDiaMultaSalarioMinimo;
	private Double multiplicadorPena;
	private Double valorHistoricoPrevisto;
	private UnidadeMonetaria unidadeMonetaria;
	private Double valorMulta;
	private String descricaoLocal;
	private String descricaoBem;

	private Integer quantidadeAnoPena;
	private Integer quantidadeMesPena;
	private Integer quantidadeDiasPena;
	private Integer quantidadeHorasPena;
	private Date dataInicioAcompanhamento;
	private Date dataTerminoAcompanhamento;
	private Date dataTerminoPrevistoAcompanhamento;
	private Integer quantidadeTarefasCumprir;
	private SituacaoAcompanhamentoIcrTransacaoPenalEnum situacaoAcompanhamentoIcrTransacao;
	private OcorrenciaLembreteEnum ocorrenciaLembrete;
	private String observacaoAcompanhamento;

	private List<AcompanhamentoCondicaoTransacaoPenal> acompanhamentos = new ArrayList<AcompanhamentoCondicaoTransacaoPenal>(
			0);

	@Id
	@GeneratedValue(generator = "gen_condcao_icr_transacao_penal")
	@Column(name = "id_condcao_icr_transacao_penal", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_icr_transacao_penal", nullable = false)
	@NotNull
	public IcrTransacaoPenal getIcrTransacaoPenal() {
		return icrTransacaoPenal;
	}

	public void setIcrTransacaoPenal(IcrTransacaoPenal icrTransacaoPenal) {
		this.icrTransacaoPenal = icrTransacaoPenal;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_tipo_pena", nullable = false)
	@NotNull
	public TipoPena getTipoPena() {
		return tipoPena;
	}

	public void setTipoPena(TipoPena tipoPena) {
		this.tipoPena = tipoPena;
	}

	@Column(name = "qd_dia_calculo_multa")
	public Integer getDiasMulta() {
		return diasMulta;
	}

	public void setDiasMulta(Integer diasMulta) {
		this.diasMulta = diasMulta;
	}

	@Column(name = "vl_frco_dia_multa_slrio_minimo")
	public Double getValorFracaoDiaMultaSalarioMinimo() {
		return valorFracaoDiaMultaSalarioMinimo;
	}

	public void setValorFracaoDiaMultaSalarioMinimo(Double valorFracaoDiaMultaSalarioMinimo) {
		this.valorFracaoDiaMultaSalarioMinimo = valorFracaoDiaMultaSalarioMinimo;
	}

	@Column(name = "multiplicador_multa")
	public Double getMultiplicadorPena() {
		return multiplicadorPena;
	}

	public void setMultiplicadorPena(Double multiplicadorPena) {
		this.multiplicadorPena = multiplicadorPena;
	}

	@Column(name = "vl_historico_previsto")
	public Double getValorHistoricoPrevisto() {
		return valorHistoricoPrevisto;
	}

	public void setValorHistoricoPrevisto(Double valorHistoricoPrevisto) {
		this.valorHistoricoPrevisto = valorHistoricoPrevisto;
	}

	@Column(name = "vl_condicao")
	public Double getValorMulta() {
		return valorMulta;
	}

	public void setValorMulta(Double valorMulta) {
		this.valorMulta = valorMulta;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_unidade_monetaria")
	public UnidadeMonetaria getUnidadeMonetaria() {
		return unidadeMonetaria;
	}

	public void setUnidadeMonetaria(UnidadeMonetaria unidadeMonetaria) {
		this.unidadeMonetaria = unidadeMonetaria;
	}

	@Column(name = "ano")
	@Max(9999)
	public Integer getQuantidadeAnoPena() {
		return quantidadeAnoPena;
	}

	public void setQuantidadeAnoPena(Integer quantidadeAnoPena) {
		this.quantidadeAnoPena = quantidadeAnoPena;
	}

	@Column(name = "mes")
	@Max(11)
	public Integer getQuantidadeMesPena() {
		return quantidadeMesPena;
	}

	public void setQuantidadeMesPena(Integer quantidadeMesPena) {
		this.quantidadeMesPena = quantidadeMesPena;
	}

	@Column(name = "qd_dia_pena")
	@Max(29)
	public Integer getQuantidadeDiasPena() {
		return quantidadeDiasPena;
	}

	public void setQuantidadeDiasPena(Integer quantidadeDiasPena) {
		this.quantidadeDiasPena = quantidadeDiasPena;
	}

	@Column(name = "qd_hora_pena")
	@Max(23)
	public Integer getQuantidadeHorasPena() {
		return quantidadeHorasPena;
	}

	public void setQuantidadeHorasPena(Integer quantidadeHorasPena) {
		this.quantidadeHorasPena = quantidadeHorasPena;
	}

	@Column(name = "ds_bem", length = 255)
	@Length(min = 0, max = 255)
	public String getDescricaoBem() {
		return descricaoBem;
	}

	public void setDescricaoBem(String descricaoBem) {
		this.descricaoBem = descricaoBem;
	}

	@Column(name = "ds_local", length = 300)
	@Length(min = 0, max = 300)
	public String getDescricaoLocal() {
		return descricaoLocal;
	}

	public void setDescricaoLocal(String descricaoLocal) {
		this.descricaoLocal = descricaoLocal;
	}

	@Column(name = "ds_observacao_condicao", length = 400)
	@Length(min = 0, max = 400)
	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	@Column(name = "dt_inicio")
	public Date getDataInicioAcompanhamento() {
		return dataInicioAcompanhamento;
	}

	public void setDataInicioAcompanhamento(Date dataInicioAcompanhamento) {
		this.dataInicioAcompanhamento = dataInicioAcompanhamento;
	}

	@Column(name = "dt_termino")
	public Date getDataTerminoAcompanhamento() {
		return dataTerminoAcompanhamento;
	}

	public void setDataTerminoAcompanhamento(Date dataTerminoAcompanhamento) {
		this.dataTerminoAcompanhamento = dataTerminoAcompanhamento;
	}

	@Column(name = "dt_termino_previsto")
	public Date getDataTerminoPrevistoAcompanhamento() {
		return dataTerminoPrevistoAcompanhamento;
	}

	public void setDataTerminoPrevistoAcompanhamento(Date dataTerminoPrevistoAcompanhamento) {
		this.dataTerminoPrevistoAcompanhamento = dataTerminoPrevistoAcompanhamento;
	}

	@Column(name = "qd_tarefa_cumprir")
	public Integer getQuantidadeTarefasCumprir() {
		return quantidadeTarefasCumprir;
	}

	public void setQuantidadeTarefasCumprir(Integer quantidadeTarefasCumprir) {
		this.quantidadeTarefasCumprir = quantidadeTarefasCumprir;
	}

	@Column(name = "cd_situacao_acompanhamento")
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.SituacaoAcompanhamentoIcrTransacaoPenalType")
	public SituacaoAcompanhamentoIcrTransacaoPenalEnum getSituacaoAcompanhamentoIcrTransacao() {
		return situacaoAcompanhamentoIcrTransacao;
	}

	public void setSituacaoAcompanhamentoIcrTransacao(
			SituacaoAcompanhamentoIcrTransacaoPenalEnum situacaoAcompanhamentoIcrTransacao) {
		this.situacaoAcompanhamentoIcrTransacao = situacaoAcompanhamentoIcrTransacao;
	}

	@Column(name = "cd_ocorrencia_lembrete")
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.OcorrenciaLembreteType")
	public OcorrenciaLembreteEnum getOcorrenciaLembrete() {
		return ocorrenciaLembrete;
	}

	public void setOcorrenciaLembrete(OcorrenciaLembreteEnum ocorrenciaLembrete) {
		this.ocorrenciaLembrete = ocorrenciaLembrete;
	}

	@Column(name = "ds_observacao_acompanhamento", length = 300)
	@Length(min = 0, max = 300)
	public String getObservacaoAcompanhamento() {
		return observacaoAcompanhamento;
	}

	public void setObservacaoAcompanhamento(String observacaoAcompanhamento) {
		this.observacaoAcompanhamento = observacaoAcompanhamento;
	}

	@Column(name = "in_unidade_multa")
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.UnidadeMultaType")
	public UnidadeMultaEnum getUnidadeMulta() {
		return unidadeMulta;
	}

	public void setUnidadeMulta(UnidadeMultaEnum unidadeMulta) {
		this.unidadeMulta = unidadeMulta;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "condicaoIcrTransacaoPenal")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@OrderBy("numeroSequencia")
	public List<AcompanhamentoCondicaoTransacaoPenal> getAcompanhamentos() {
		return acompanhamentos;
	}

	public void setAcompanhamentos(List<AcompanhamentoCondicaoTransacaoPenal> acompanhamentos) {
		this.acompanhamentos = acompanhamentos;
	}

	@Transient
	public String getDetalhes() {
		String result = "";
		if (getTipoPena() != null) {
			if (getDiasMulta() != null) {
				result += "Qd. dias: " + getDiasMulta() + " - ";
			}

			if (getValorFracaoDiaMultaSalarioMinimo() != null) {
				result += "Vlr. dia multa sal. minimo: " + getValorFracaoDiaMultaSalarioMinimo() + " - ";
			}

			if (getMultiplicadorPena() != null) {
				result += "Multiplicador: " + getMultiplicadorPena() + " - ";
			}

			if (getValorHistoricoPrevisto() != null) {
				result += "Vlr. Hist. Previsto: " + getValorHistoricoPrevisto() + " - ";
			}

			if (getUnidadeMonetaria() != null) {
				result += "Vlr. condição: " + getUnidadeMonetaria().getSimbolo() + getValorMulta() + " - ";
			}

			if (getDescricaoBem() != null) {
				result += "Bem: " + getDescricaoBem() + " - ";
			}

			if (getDescricaoLocal() != null) {
				result += "Local: " + getDescricaoLocal() + " - ";
			}

			if (getQuantidadeAnoPena() != null) {
				result += "Anos: " + getQuantidadeAnoPena() + " - ";
			}

			if (getQuantidadeMesPena() != null) {
				result += "Meses: " + getQuantidadeMesPena() + " - ";
			}

			if (getQuantidadeDiasPena() != null) {
				result += "Dias: " + getQuantidadeDiasPena() + " - ";
			}

			if (getQuantidadeHorasPena() != null) {
				result += "Horas: " + getQuantidadeHorasPena() + " - ";
			}

			if (result != null && !result.trim().equals("")) {
				result = result.substring(0, result.lastIndexOf(" - "));
				return result;
			}

			return null;
		}

		return null;
	}

	@Override
	public String toString() {
		return getTipoPena().getGeneroPena().getLabel() + " - " + getTipoPena();
	}

	@Override
	public int hashCode() {
		if (this.getId() != null) {
			return getId().hashCode();
		}
		
		int hash = 7;

		hash = 31 * hash + getIcrTransacaoPenal().hashCode();
		hash = 31 * hash + getTipoPena().hashCode();

		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		CondicaoIcrTransacaoPenal other = (CondicaoIcrTransacaoPenal) obj;

		if (this == other) {
			return true;
		}

		if ((obj == null) || !(obj instanceof CondicaoIcrTransacaoPenal)) {
			return false;
		}

		if (this.getId() == null || other.getId() == null) {

			if (other.getIcrTransacaoPenal() != null && other.getIcrTransacaoPenal() != null) {

				if (this.getTipoPena() != null && other.getTipoPena() != null) {
					return other.getTipoPena().equals(this.getTipoPena());
				} else {
					return false;
				}

			} else {
				return false;
			}

		} else {
			return getId().equals(other.getId());
		}
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CondicaoIcrTransacaoPenal> getEntityClass() {
		return CondicaoIcrTransacaoPenal.class;
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
