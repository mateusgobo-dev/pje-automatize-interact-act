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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;

@Entity
@Table(name = "tb_icr_sent_condenatoria")
@PrimaryKeyJoinColumn(name="id_icr_sentenca_condenatoria")
public class IcrSentencaCondenatoria extends InformacaoCriminalRelevante{

	private static final long serialVersionUID = 1L;
	
	private Date dtPublicacao;
	private PessoaMagistrado pessoaMagistrado;
	
	private List<Pena> penas = new ArrayList<Pena>(0);
	
	@NotNull
	@Column(name = "dt_publicacao", nullable = false)
	public Date getDtPublicacao() {
		return dtPublicacao;
	}
	
	public void setDtPublicacao(Date dtPublicacao) {
		this.dtPublicacao = dtPublicacao;
	}
	
	@NotNull
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "id_pessoa_magistrado", nullable = false)
	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}
	
	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}
		
	@OneToMany(cascade=CascadeType.ALL, mappedBy="icrSentencaCondenatoria")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<Pena> getPenas() {
		return penas;
	}
	
	
	public void setPenas(List<Pena> penas){
		this.penas = penas;
	}
	
	@Transient
	public List<PenaTotal> getPenaTotalList(){
		List<PenaTotal> returnValue = new ArrayList<PenaTotal>();
		for(Pena pena : getPenas()){
			if(pena instanceof PenaTotal){
				returnValue.add((PenaTotal) pena);
			}
		}
		return returnValue;
	}
	

	@Transient
	public List<PenaIndividualizada> getPenaIndividualizadaList(){
		List<PenaIndividualizada> returnValue = new ArrayList<PenaIndividualizada>(0);
		for(Pena pena : getPenas()){
			if(pena instanceof PenaIndividualizada){
				returnValue.add((PenaIndividualizada) pena);
			}
		}
		return returnValue;
	}
	
	
	@Transient
	public List<PenaTotal> getPenaTotalOriginalList() {
		List<PenaTotal> returnValue = new ArrayList<PenaTotal>(0);
		for(PenaTotal penaTotal : getPenaTotalList()){
			if(penaTotal.getPenasOriginais() == null || penaTotal.getPenasOriginais().isEmpty()){
				returnValue.add(penaTotal);
			}
		}
		return returnValue;
	}
	
	
	@Transient
	public List<PenaIndividualizada> getPenaIndividualizadaOriginalList(){
		List<PenaIndividualizada> returnValue = new ArrayList<PenaIndividualizada>(0);
		for(PenaIndividualizada penaIndividualizada : getPenaIndividualizadaList()){
			if(penaIndividualizada.getPenasOriginais() == null || penaIndividualizada.getPenasOriginais().isEmpty()){
				returnValue.add(penaIndividualizada);
			}
		}
		return returnValue;
	}
	
	@Transient
	public List<Pena> getPenaTotalAposSubstituicaoList() {
		List<Pena> returnValue = new ArrayList<Pena>(0);
		for(PenaTotal penaTotal : getPenaTotalList()){
			if(penaTotal.getPenasSubstitutivas() != null && !penaTotal.getPenasSubstitutivas().isEmpty()){
				returnValue.addAll(penaTotal.getPenasSubstitutivas());
			}
			else{
				returnValue.add(penaTotal);
			}
		}
		return returnValue;
	}
	
	@Transient
	public List<Pena> getPenaIndividualizadaAposSubstituicaoList() {
		List<Pena> returnValue = new ArrayList<Pena>(0);
		for(PenaIndividualizada penaIndividualizada : getPenaIndividualizadaList()){
			if(penaIndividualizada.getPenasSubstitutivas() != null && !penaIndividualizada.getPenasSubstitutivas().isEmpty()){
				returnValue.addAll(penaIndividualizada.getPenasSubstitutivas());
			}
			else{
				returnValue.add(penaIndividualizada);
			}
		}
		return returnValue;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrSentencaCondenatoria.class;
	}
}
