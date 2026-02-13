package br.jus.je.pje.entity.vo;

import br.jus.pje.nucleo.enums.TipoSituacaoExpedienteEnum;

public class SituacaoExpedienteVO {
	
	private TipoSituacaoExpedienteEnum situacaoExpediente;
	private Long contador;


	public SituacaoExpedienteVO() { }
	
	public SituacaoExpedienteVO(TipoSituacaoExpedienteEnum situacaoExpediente, Long contadorExpedientes) {
		this.situacaoExpediente = situacaoExpediente;
		this.contador = contadorExpedientes;
	}

	public String getIndice() {
		if(this.situacaoExpediente == null) {
			return "";
		}
		return this.situacaoExpediente.name();
	}
	

	public String getDescricao() {
		if(this.situacaoExpediente == null) {
			return "";
		}
		return this.situacaoExpediente.getLabel();
	}

	public Long getContador() {
		return contador;
	}
	
	public TipoSituacaoExpedienteEnum getSituacaoExpediente() {
		return situacaoExpediente;
	}
	
}
