package br.jus.csjt.pje.commons.model.dto;

import java.io.Serializable;

import br.jus.pje.jt.entidades.ProcessoAudienciaJT;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;

public class ProcessoImportadoAudDTO implements Serializable {

	private ProcessoAudienciaJT pajt;
	private SituacaoProcesso sp;
	private boolean ativo = true;
	
	public ProcessoImportadoAudDTO(){
	}
	
	public ProcessoImportadoAudDTO(ProcessoAudienciaJT processoAudienciaJT, SituacaoProcesso situacaoProcesso){
		this.pajt = processoAudienciaJT;
		this.sp = situacaoProcesso;
	}

	public ProcessoAudienciaJT getPajt() {
		return pajt;
	}

	public void setPajt(ProcessoAudienciaJT pajt) {
		this.pajt = pajt;
	}

	public SituacaoProcesso getSp() {
		return sp;
	}

	public void setSp(SituacaoProcesso sp) {
		this.sp = sp;
	}

	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
	
	
	
}
