package br.com.infox.cliente.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.RpvUnidadeGestora;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.CondicaoPssEnum;

public class RpvParte implements Serializable{

	private static final long serialVersionUID = 1L;

	private Pessoa parte;
	private TipoParte tipoParte;
	private Double valorHonorario;
	private Double valorPagoPessoa;
	private List<RpvParte> representanteList = new ArrayList<RpvParte>(0);
	private List<RpvParteAdvogado> advogadoList = new ArrayList<RpvParteAdvogado>(0);
	private Boolean check = false;
	private Boolean inIsentoPss = Boolean.FALSE;
	private Double valorPss;
	private CondicaoPssEnum condicaoPss = CondicaoPssEnum.A;
	private RpvUnidadeGestora unidadeExecutadaPss;
	private Boolean inPrincipal;

	public Pessoa getParte(){
		return parte;
	}

	public void setParte(Pessoa parte){
		this.parte = parte;
	}

	public TipoParte getTipoParte(){
		return tipoParte;
	}

	public void setTipoParte(TipoParte tipoParte){
		this.tipoParte = tipoParte;
	}

	public Double getValorHonorario(){
		return valorHonorario;
	}

	public void setValorHonorario(Double valorHonorario){
		this.valorHonorario = valorHonorario;
	}

	public Double getValorPagoPessoa(){
		return valorPagoPessoa;
	}

	public void setValorPagoPessoa(Double valorPagoPessoa){
		this.valorPagoPessoa = valorPagoPessoa;
	}

	public List<RpvParte> getRepresentanteList(){
		return representanteList;
	}

	public void setRepresentanteList(List<RpvParte> representanteList){
		this.representanteList = representanteList;
	}

	public List<RpvParteAdvogado> getAdvogadoList(){
		return advogadoList;
	}

	public void setAdvogadoList(List<RpvParteAdvogado> advogadoList){
		this.advogadoList = advogadoList;
	}

	public void setCheck(Boolean check){
		this.check = check;
	}

	public Boolean getCheck(){
		return check;
	}

	public void setInIsentoPss(Boolean inIsentoPss){
		this.inIsentoPss = inIsentoPss;
	}

	public Boolean getInIsentoPss(){
		return inIsentoPss;
	}

	public void setValorPss(Double valorPss){
		this.valorPss = valorPss;
	}

	public Double getValorPss(){
		return valorPss;
	}

	public void setCondicaoPss(CondicaoPssEnum condicaoPss){
		this.condicaoPss = condicaoPss;
	}

	public CondicaoPssEnum getCondicaoPss(){
		return condicaoPss;
	}

	public void setUnidadeExecutadaPss(RpvUnidadeGestora unidadeExecutadaPss){
		this.unidadeExecutadaPss = unidadeExecutadaPss;
	}

	public RpvUnidadeGestora getUnidadeExecutadaPss(){
		return unidadeExecutadaPss;
	}

	public void setInPrincipal(Boolean inPrincipal){
		this.inPrincipal = inPrincipal;
	}

	public Boolean getInPrincipal(){
		return inPrincipal;
	}

}