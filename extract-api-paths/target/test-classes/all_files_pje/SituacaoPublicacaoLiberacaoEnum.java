package br.jus.pje.nucleo.enums;

import java.util.ArrayList;
import java.util.List;

public enum SituacaoPublicacaoLiberacaoEnum implements PJeEnum {
	CRIADA("Criada"), 
	PENDENTE_DE_PUBLICACAO("Pendente de Publicação"),
	PUBLICADA("Publicada"),
	LIBERADO_PARA_PUBLICACAO("Liberada para Publicação"),
	INATIVA("Excluida/Inativa");
	
	private String label;

	SituacaoPublicacaoLiberacaoEnum(String label) {
		this.label = label;
	}
	
	public boolean isCriada(){
		return this.equals(CRIADA);
	}
	
	public boolean isPendentePublicacao(){
		return this.equals(PENDENTE_DE_PUBLICACAO);
	}
	
	public boolean isPublicada(){
		return this.equals(PUBLICADA);
	}
	
	public boolean isLiberadaPublicacao(){
		return this.equals(LIBERADO_PARA_PUBLICACAO);
	}
	
	public static List<SituacaoPublicacaoLiberacaoEnum> getListSituacaoPerfilGravarPublicacao(){
		List<SituacaoPublicacaoLiberacaoEnum> retorno = new ArrayList<SituacaoPublicacaoLiberacaoEnum>();
		retorno.add(CRIADA);
		retorno.add(LIBERADO_PARA_PUBLICACAO);
		return retorno;
	}
	
	public static List<SituacaoPublicacaoLiberacaoEnum> getListSituacaoPerfilPublicar(){
		List<SituacaoPublicacaoLiberacaoEnum> retorno = new ArrayList<SituacaoPublicacaoLiberacaoEnum>();
		retorno.add(LIBERADO_PARA_PUBLICACAO);
		return retorno;
	}
	
	public static List<SituacaoPublicacaoLiberacaoEnum> getListSituacaoPerfilPublicada(){
		List<SituacaoPublicacaoLiberacaoEnum> retorno = new ArrayList<SituacaoPublicacaoLiberacaoEnum>();
		retorno.add(PUBLICADA);
		return retorno;
	}
	
	@Override
	public String getLabel() {
		return this.label;
	}
}