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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.validator.constraints.Length;


@Entity
@Table(name = "tb_icr")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_icr", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_icr"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class InformacaoCriminalRelevante implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<InformacaoCriminalRelevante,Integer>, Comparable<InformacaoCriminalRelevante>{

	private static final long serialVersionUID = -3932817762714609624L;
	private Integer id;
	private Date data;
	private ProcessoParte processoParte;
	private Pessoa pessoaComoReu;
	private String nomeReu;
	private TipoInformacaoCriminalRelevante tipo;
	private String observacao;
	private Boolean ativo;
	private List<IcrProcessoEvento> icrProcessoEventoList = new ArrayList<IcrProcessoEvento>();
	private List<ProcessoEvento> processoEventoList = new ArrayList<ProcessoEvento>();
	private List<TransitoEmJulgado> transitoEmJulgadoList = new ArrayList<TransitoEmJulgado>();
	private TransitoEmJulgado transitoEmJulgadoTemp;
	private List<TipificacaoDelito> tipificacoes = new ArrayList<TipificacaoDelito>(0);

	// ----------------------------- CONSTRUTOR ---------------------------

	public InformacaoCriminalRelevante(){
		//
	}

	public InformacaoCriminalRelevante(Integer id){
		this.id = id;
	}

	public InformacaoCriminalRelevante(InformacaoCriminalRelevante icr){
		copiarPropriedades(icr);
	}

	private void copiarPropriedades(InformacaoCriminalRelevante icr){
		try{
			BeanUtils.copyProperties(this, icr);
			icr.setIcrProcessoEventoList(null);
		} catch (IllegalAccessException e){
			e.printStackTrace();
		} catch (InvocationTargetException e){
			e.printStackTrace();
		}
	}

	// ----------------------------- ID -----------------------------------
	@Id
	@GeneratedValue(generator = "gen_icr")
	@Column(name = "id_icr", unique = true, nullable = false)
	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	// ------------------------------ DEMAIS ------------------------------
	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_icr", nullable = false)
	public Date getData(){
		return data;
	}

	public void setData(Date data){
		this.data = data;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte", nullable = true)
	public ProcessoParte getProcessoParte(){
		return processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte){
		this.processoParte = processoParte;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "icr")
	public List<TransitoEmJulgado> getTransitoEmJulgadoList(){
		return this.transitoEmJulgadoList;
	}

	public void setTransitoEmJulgadoList(List<TransitoEmJulgado> transitoEmJulgadoList){
		this.transitoEmJulgadoList = transitoEmJulgadoList;
	}

	public void setTransitoEmJulgadoTemp(TransitoEmJulgado transitoEmJulgadoTemp){
		this.transitoEmJulgadoTemp = transitoEmJulgadoTemp;
	}

	@Transient
	public TransitoEmJulgado getTransitoEmJulgadoTemp(){
		if (transitoEmJulgadoTemp == null)
			transitoEmJulgadoTemp = new TransitoEmJulgado();
		return transitoEmJulgadoTemp;
	}

	@Transient
	public Pessoa getPessoaComoReu(){
		if (processoParte != null && processoParte.getPessoa() != null){
			this.pessoaComoReu = processoParte.getPessoa();
			return this.pessoaComoReu;
		}
		return null;
	}

	public void setPessoaComoReu(Pessoa pessoaComoReu){
		this.pessoaComoReu = pessoaComoReu;
		if (this.processoParte != null){
			this.processoParte.setPessoa(pessoaComoReu);
		}
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaComoReu(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaComoReu(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaComoReu(pessoa.getPessoa());
		} else {
			setPessoaComoReu((Pessoa)null);
		}
	}

	@Transient
	public String getNomeReu(){
		getPessoaComoReu();
		if (pessoaComoReu != null){
			nomeReu = pessoaComoReu.getNome();
		}
		return nomeReu;
	}

	public void setNomeReu(String nomeReu){
		this.nomeReu = nomeReu;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cd_icr", nullable = false)
	@NotNull
	public TipoInformacaoCriminalRelevante getTipo(){
		return tipo;
	}

	public void setTipo(TipoInformacaoCriminalRelevante tipo){
		this.tipo = tipo;
	}

	@Column(name = "ds_observacao", nullable = true, length = 400)
	@Length(min = 0, max = 400)
	public String getObservacao(){
		return observacao;
	}

	public void setObservacao(String observacao){
		this.observacao = observacao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo(){
		return ativo;
	}

	public void setAtivo(Boolean ativo){
		this.ativo = ativo;
	}

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, mappedBy = "icr")
	public List<IcrProcessoEvento> getIcrProcessoEventoList(){
		return icrProcessoEventoList;
	}

	public void setIcrProcessoEventoList(List<IcrProcessoEvento> icrProcessoEventoList){
		this.icrProcessoEventoList = icrProcessoEventoList;
	}

	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_icr_processo_evento", joinColumns = {@JoinColumn(name = "id_icr", nullable = false, updatable = false)}, inverseJoinColumns = {@JoinColumn(name = "id_processo_evento", nullable = false, updatable = false)})
	public List<ProcessoEvento> getProcessoEventoList(){
		return processoEventoList;
	}

	public void setProcessoEventoList(List<ProcessoEvento> processoEventoList){
		this.processoEventoList = processoEventoList;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "informacaoCriminalRelevante")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@OrderBy("numeroReferencia")
	public List<TipificacaoDelito> getTipificacoes(){
		return tipificacoes;
	}

	public void setTipificacoes(List<TipificacaoDelito> tipificacoes){
		this.tipificacoes = tipificacoes;
	}

	@Transient
	public List<ConcursoCrime> getAllConcursos(){
		List<ConcursoCrime> concursos = new ArrayList<ConcursoCrime>(0);
		for (TipificacaoDelito t : getTipificacoes()){
			for (ConcursoCrime c : t.getConcursos()){
				if (!concursos.contains(c)){
					concursos.add(c);
				}
			}
		}

		return concursos;
	}

	@Transient
	public List<TipificacaoDelito> getTipificacoesNaoAgrupadas(){
		List<TipificacaoDelito> result = new ArrayList<TipificacaoDelito>(0);
		for (TipificacaoDelito t : getTipificacoes()){
			if (t.getConcursos() == null || t.getConcursos().isEmpty()){
				result.add(t);
			}
		}
		return result;
	}

	@Transient
	public String getTipificacaoCompletaString(){
		StringBuilder sb = new StringBuilder();
		int linhas = 0;
		if (getAllConcursos() != null){
			for (ConcursoCrime concursoCrime : getAllConcursos()){
				for (TipificacaoDelito tipificacaoDelito : concursoCrime.getTipificacoes()){
					sb.append("<b>(" + tipificacaoDelito.getNumeroReferencia() + ")</b>");
					sb.append(tipificacaoDelito.getDelitoString() + ", ");
					sb.append("<b>" + tipificacaoDelito.getQuantidadeIncidencia() + " "
						+ (tipificacaoDelito.getQuantidadeIncidencia() == 1 ? "vez" : "vezes") + "<b>("
						+ tipificacaoDelito.getTipoConsumacaoDelito().getLabel() + ")</b>");
					sb.append(tipificacaoDelito.getCombinacoesString());
					sb.append(";<br/>");
					linhas++;
				}
				if (concursoCrime.getTipoAgrupamento() == ConcursoCrime.TipoAgrupamento.C){
					sb.append("<b>em continuidade delitiva</b><br/><br/>");
				}
				else{
					sb.append("<b>em concurso formal</b><br/><br/>");
				}
			}
		}

		if (getTipificacoesNaoAgrupadas() != null){
			for (TipificacaoDelito tipificacaoDelito : getTipificacoesNaoAgrupadas()){
				sb.append("<b>(" + tipificacaoDelito.getNumeroReferencia() + ")</b>");
				sb.append(tipificacaoDelito.getDelitoString() + ", ");
				sb.append("<b>" + tipificacaoDelito.getQuantidadeIncidencia() + " "
					+ (tipificacaoDelito.getQuantidadeIncidencia() == 1 ? "vez" : "vezes") + "<b>("
					+ tipificacaoDelito.getTipoConsumacaoDelito().getLabel() + ")</b>");
				sb.append(tipificacaoDelito.getCombinacoesString());
				sb.append(";<br/>");
				linhas++;
			}
		}

		if (linhas > 1){
			sb.append("<b>todos em concurso material</b><br/><br/>");
		}
		else if (linhas != 0){
			sb.append("<b>em concurso material</b><br/><br/>");
		}

		return sb.toString();
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAtivo() == null) ? 0 : ativo.hashCode());
		result = prime * result + ((getData() == null) ? 0 : data.hashCode());
		result = prime * result + ((getIcrProcessoEventoList() == null) ? 0 : icrProcessoEventoList.hashCode());
		result = prime * result + ((getId() == null) ? 0 : id.hashCode());
		result = prime * result + ((getNomeReu() == null) ? 0 : nomeReu.hashCode());
		result = prime * result + ((getPessoaComoReu() == null) ? 0 : pessoaComoReu.hashCode());
		result = prime * result + ((getProcessoEventoList() == null) ? 0 : processoEventoList.hashCode());
		result = prime * result + ((getProcessoParte() == null) ? 0 : processoParte.hashCode());
		result = prime * result + ((getTipo() == null) ? 0 : tipo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof InformacaoCriminalRelevante))
			return false;
		InformacaoCriminalRelevante other = (InformacaoCriminalRelevante) obj;
		if (getAtivo() == null){
			if (other.getAtivo() != null)
				return false;
		}
		else if (!ativo.equals(other.getAtivo()))
			return false;
		if (getData() == null){
			if (other.getData() != null)
				return false;
		}
		else if (!data.equals(other.getData()))
			return false;
		if (getIcrProcessoEventoList() == null){
			if (other.getIcrProcessoEventoList() != null)
				return false;
		}
		else if (!icrProcessoEventoList.equals(other.getIcrProcessoEventoList()))
			return false;
		if (getId() == null){
			if (other.getId() != null)
				return false;
		}
		else if (!id.equals(other.getId()))
			return false;
		if (getNomeReu() == null){
			if (other.getNomeReu() != null)
				return false;
		}
		else if (!nomeReu.equals(other.getNomeReu()))
			return false;
		if (getPessoaComoReu() == null){
			if (other.getPessoaComoReu() != null)
				return false;
		}
		else if (!pessoaComoReu.equals(other.getPessoaComoReu()))
			return false;
		if (getProcessoEventoList() == null){
			if (other.getProcessoEventoList() != null)
				return false;
		}
		else if (!processoEventoList.equals(other.getProcessoEventoList()))
			return false;
		if (getProcessoParte() == null){
			if (other.getProcessoParte() != null)
				return false;
		}
		else if (!processoParte.equals(other.getProcessoParte()))
			return false;
		if (getTipo() == null){
			if (other.getTipo() != null)
				return false;
		}
		else if (!tipo.equals(other.getTipo()))
			return false;
		return true;
	}

	@Override
	public int compareTo(InformacaoCriminalRelevante other){
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;

		if (this.getData() != null && other.getData() != null){
			if (this.getData().equals(other.getData())){
				if (this.getId() < other.getId()){
					return BEFORE;
				}
				return AFTER;
			}
			else if (this.getData().before(other.getData())){
				return BEFORE;
			}
			else if (this.getData().after(other.getData())){
				return AFTER;
			}
		}

		return EQUAL;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return InformacaoCriminalRelevante.class;
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
