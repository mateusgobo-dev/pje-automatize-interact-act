package br.jus.cnj.pje.vo;

import java.util.ArrayList;
import java.util.List;

public class ProcessoParteMural {
    private String nomeParte;
    private String descricaoTipoParte;
    private List<AdvogadoParteMural> advogadoParteMural;
    private Integer ordemEnvioParte;

    public String getNomeParte() {
	return nomeParte;
    }

    public void setNomeParte(String nomeParte) {
	this.nomeParte = nomeParte;
    }

    public String getDescricaoTipoParte() {
	return descricaoTipoParte;
    }

    public void setDescricaoTipoParte(String descricaoTipoParte) {
	this.descricaoTipoParte = descricaoTipoParte;
    }

    public List<AdvogadoParteMural> getAdvogadoParteMural() {
	if (advogadoParteMural == null) {
	    advogadoParteMural = new ArrayList<AdvogadoParteMural>();
	}

	return advogadoParteMural;
    }

    public void setAdvogadoParteMural(List<AdvogadoParteMural> advogadoParteMural) {
	this.advogadoParteMural = advogadoParteMural;
    }
    
    public Integer getOrdemEnvioParte() {
	return ordemEnvioParte;
    }

    public void setOrdemEnvioParte(Integer ordemEnvioParte) {
	this.ordemEnvioParte = ordemEnvioParte;
    }

}