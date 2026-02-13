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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_tipificacao_delito")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipificacao_delito", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipificacao_delito"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipificacaoDelito implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipificacaoDelito,Integer> {

	private static final long serialVersionUID = 1L;

	public enum TipoConsumacaoDelito {
		C("Consumado"), T("Tentado");

		private String label;

		private TipoConsumacaoDelito(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

	private Integer id;
	private InformacaoCriminalRelevante informacaoCriminalRelevante;
	private Date dataDelito;
	private Integer quantidadeIncidencia = 1;
	private Integer numeroReferencia;
	private TipoConsumacaoDelito tipoConsumacaoDelito = TipoConsumacaoDelito.C;
	private String observacao;
	private List<DispositivoNorma> delito = new ArrayList<DispositivoNorma>(0);
	private List<DispositivoNorma> combinacoes = new ArrayList<DispositivoNorma>(
			0);
	private List<ConcursoCrime> concursos = new ArrayList<ConcursoCrime>(0);
    private Boolean dataDesconhecida = false;

	@Id
	@GeneratedValue(generator = "gen_tipificacao_delito")
	@Column(name = "id_tipificacao_delito", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "id_icr")
	public InformacaoCriminalRelevante getInformacaoCriminalRelevante() {
		return informacaoCriminalRelevante;
	}

	public void setInformacaoCriminalRelevante(
			InformacaoCriminalRelevante informacaoCriminalRelevante) {
		this.informacaoCriminalRelevante = informacaoCriminalRelevante;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "dt_delito")
	public Date getDataDelito() {
		return dataDelito;
	}

	public void setDataDelito(Date dataDelito) {
		this.dataDelito = dataDelito;
	}

	@Column(name = "in_quantidade_incidencia")
	public Integer getQuantidadeIncidencia() {
		return quantidadeIncidencia;
	}

	public void setQuantidadeIncidencia(Integer quantidadeIncidencia) {
		this.quantidadeIncidencia = quantidadeIncidencia;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_tipo_consumacao_delito")
	public TipoConsumacaoDelito getTipoConsumacaoDelito() {
		return tipoConsumacaoDelito;
	}

	public void setTipoConsumacaoDelito(
			TipoConsumacaoDelito tipoConsumacaoDelito) {
		this.tipoConsumacaoDelito = tipoConsumacaoDelito;
	}

	@Column(name = "ds_observacao")
	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "tb_combncao_norma_extensao", joinColumns = { @JoinColumn(name = "id_tipificacao_delito") }, inverseJoinColumns = { @JoinColumn(name = "id_dispositivo_norma") })
	public List<DispositivoNorma> getCombinacoes() {
		return combinacoes;
	}

	public void setCombinacoes(List<DispositivoNorma> combinacoes) {
		this.combinacoes = combinacoes;
	}

	@ManyToMany(cascade = CascadeType.ALL, mappedBy = "tipificacoes")
	public List<ConcursoCrime> getConcursos() {
		return concursos;
	}

	public void setConcursos(List<ConcursoCrime> concursos) {
		this.concursos = concursos;
	}

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "tb_combinacao_delito", joinColumns = { @JoinColumn(name = "id_tipificacao_delito") }, inverseJoinColumns = { @JoinColumn(name = "id_dispositivo_norma") })
	public List<DispositivoNorma> getDelito() {
		return delito;
	}

	public void setDelito(List<DispositivoNorma> delito) {
		this.delito = delito;
	}

	@Column(name = "nr_referencia")
	public Integer getNumeroReferencia() {
		return numeroReferencia;
	}

	public void setNumeroReferencia(Integer numeroReferencia) {
		this.numeroReferencia = numeroReferencia;
	}

	@Column(name = "in_data_desconhecida", nullable = false)
	@NotNull
	public Boolean getDataDesconhecida() {
		return this.dataDesconhecida;
	}

	public void setDataDesconhecida(Boolean dataDesconhecida) {
		this.dataDesconhecida = dataDesconhecida;
	}
	
	@Transient
	public String getDelitoString() {
		String returnValue = "";
		String normaPenal = "";
		if (getDelito() != null) {
			for (DispositivoNorma dispositivoNorma : getDelito()) {
				
				returnValue += " e " + getDelitoString(dispositivoNorma);
				if (dispositivoNorma.getNormaPenal().getDsSigla() != null) {
					normaPenal = dispositivoNorma.getNormaPenal().getDsSigla();
				} else if (dispositivoNorma.getNormaPenal().getNormaPenal() != null) {
					normaPenal = dispositivoNorma.getNormaPenal()
							.getNormaPenal();
				} else {
					normaPenal = dispositivoNorma.getNormaPenal()
							.getTipoNormaPenal().getDescricao()
							+ " "
							+ dispositivoNorma.getNormaPenal().getNrNorma();
				}
				
			}
			returnValue = returnValue.replaceAll(", "+ normaPenal," ")+ normaPenal;
			if(returnValue.indexOf("") > -1){
				returnValue = returnValue.substring(returnValue.indexOf("e") + 1);
			}

		}
		return returnValue;
		
	}

	@Transient
	/*
	 * - No inicio deve ser apresentado os dados do dispositivo da norma, na
	 * seguinte ordem: Símbolo do Artigo, Identificador do artigo, Símbolo do
	 * Parágrafo, Identificador do Parágrafo, Inciso, Alínea, Parte, Item... -
	 * No final deve ser apresentado os dados da Norma, com as seguintes regras:
	 * -- Se tem Sigla:  apresenta só os dados da sigla; -- Se não tem Sigla,
	 * mas tem o Nome: apresenta só o nome; -- Se não tem Sigla e não tem o
	 * nome: apresenta o Tipo e o Número;
	 */
	public static String getDelitoString(DispositivoNorma dispositivoNorma) {
		String returnValue = "";
		String normaPenal = "";
		if (dispositivoNorma.getNormaPenal().getDsSigla() != null) {
			normaPenal = dispositivoNorma.getNormaPenal().getDsSigla();
		} else if (dispositivoNorma.getNormaPenal().getNormaPenal() != null) {
			normaPenal = dispositivoNorma.getNormaPenal().getNormaPenal();
		} else {
			normaPenal = dispositivoNorma.getNormaPenal().getTipoNormaPenal()
					.getDescricao()
					+ " " + dispositivoNorma.getNormaPenal().getNrNorma();
		}
		do {
			returnValue = (dispositivoNorma.getDsSimbolo() != null ? dispositivoNorma
					.getDsSimbolo() : "")
					+ dispositivoNorma.getDsIdentificador()
					+ ", "
					+ returnValue;
			dispositivoNorma = dispositivoNorma.getDispositivoNormaPai();
		} while (dispositivoNorma != null);

		returnValue = returnValue + normaPenal;

		return returnValue;
	}

	@Transient
	public String getCombinacoesString() {
		if (getCombinacoes() != null) {
			return getCombinacoesString(this);
		}
		return null;
	}

	public static String getCombinacoesString(
			TipificacaoDelito tipificacaoDelito) {
		StringBuilder returnValue = new StringBuilder();
		if (!tipificacaoDelito.getCombinacoes().isEmpty()) {
			List<DispositivoNorma> pais = new ArrayList<DispositivoNorma>();
			for (int i = 0; i < tipificacaoDelito.getCombinacoes().size(); i++) {
				DispositivoNorma dispositivo = tipificacaoDelito
						.getCombinacoes().get(i);
				// os dispositivos na mesma hierarquia sao apresentados como
				// "Inciso I, II, III"
				if (pais.contains(dispositivo.getDispositivoNormaPai())) {
					returnValue.append(",");
					returnValue.append(dispositivo.getDsIdentificador());
				} else {
					returnValue.append(i == 0 ? " <b>Combinado com </b> "
							: i > 0 ? " <b>e com </b> " : "");
					returnValue.append(TipificacaoDelito
									.getDelitoString(tipificacaoDelito
											.getCombinacoes().get(i)));
				}
				pais.add(dispositivo.getDispositivoNormaPai());
			}
		}
		return returnValue.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getDataDelito() == null) ? 0 : dataDelito.hashCode());
		result = prime
				* result
				+ ((getDataDesconhecida() == null) ? 0 : dataDesconhecida.hashCode());
		result = prime * result + ((getDelito() == null) ? 0 : delito.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TipificacaoDelito))
			return false;
		TipificacaoDelito other = (TipificacaoDelito) obj;
		if (getDataDelito() == null) {
			if (other.getDataDelito() != null) {
				return false;
			} else {
				if (getDataDesconhecida() == null) {
					if (other.getDataDesconhecida() != null) {
						return false;
					} else if (!getDataDesconhecida().equals(other.getDataDesconhecida())) {
						return false;
					}
				}
			}
		} else if (!dataDelito.equals(other.getDataDelito()))
			return false;
		if (getDelito() == null) {
			if (other.getDelito() != null)
				return false;
		} else if (!delito.containsAll(other.getDelito()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipificacaoDelito> getEntityClass() {
		return TipificacaoDelito.class;
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
