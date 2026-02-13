package br.jus.cnj.pje.nucleo.dto;

import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.identidade.Papel;


public class FinalizaDocumentoFiltroDto {
	
	ProcessoDocumento pd;
	
	ProcessoTrf processo; 
	
	Long idTaskInstance;
	
	boolean substituirNomeDocumento;
	
	boolean updateBin;
	
	boolean marcarComoNaoLido;
	
	Pessoa pessoa;
	
	Localizacao localizacaoUsuario;
	
	Papel papelUsuario;
	
	boolean juntar;
	


	public ProcessoDocumento getPd() {
		return pd;
	}

	public void setPd(ProcessoDocumento pd) {
		this.pd = pd;
	}

	public ProcessoTrf getProcesso() {
		return processo;
	}

	public void setProcesso(ProcessoTrf processo) {
		this.processo = processo;
	}

	public Long getIdTaskInstance() {
		return idTaskInstance;
	}

	public void setIdTaskInstance(Long idTaskInstance) {
		this.idTaskInstance = idTaskInstance;
	}

	public boolean isSubstituirNomeDocumento() {
		return substituirNomeDocumento;
	}

	public void setSubstituirNomeDocumento(boolean substituirNomeDocumento) {
		this.substituirNomeDocumento = substituirNomeDocumento;
	}

	public boolean isUpdateBin() {
		return updateBin;
	}

	public void setUpdateBin(boolean updateBin) {
		this.updateBin = updateBin;
	}

	public boolean isMarcarComoNaoLido() {
		return marcarComoNaoLido;
	}

	public void setMarcarComoNaoLido(boolean marcarComoNaoLido) {
		this.marcarComoNaoLido = marcarComoNaoLido;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public Localizacao getLocalizacaoUsuario() {
		return localizacaoUsuario;
	}

	public void setLocalizacaoUsuario(Localizacao localizacaoUsuario) {
		this.localizacaoUsuario = localizacaoUsuario;
	}

	public Papel getPapelUsuario() {
		return papelUsuario;
	}

	public void setPapelUsuario(Papel papelUsuario) {
		this.papelUsuario = papelUsuario;
	}

	public boolean isJuntar() {
		return juntar;
	}

	public void setJuntar(boolean juntar) {
		this.juntar = juntar;
	}
	
	
	
}
