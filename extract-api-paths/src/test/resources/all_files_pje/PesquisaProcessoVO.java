package br.jus.cnj.pje.entidades.vo;

import br.jus.pje.nucleo.util.StringUtil;

public class PesquisaProcessoVO {
	
	private String numeroProcesso;
	private String classe;
	private String[] tags;
	private String tagsString;
	
	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
	public String getClasse() {
		return classe;
	}
	public void setClasse(String classe) {
		this.classe = classe;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	public String getTagsString() {
		return tagsString;
	}
	public void setTagsString(String tagsString) {
		if(StringUtil.isEmpty(tagsString)){
			tagsString = null;
			tags = null;
		}
		else{
			this.tags = tagsString.split(",");
		}
		this.tagsString = tagsString;
	}	
}

