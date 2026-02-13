package br.jus.cnj.pje.nucleo.dto;

import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.identidade.Papel;

public class FinalizaDocumentoFiltroBuilder {
	FinalizaDocumentoFiltroDto filtro;
	
	public FinalizaDocumentoFiltroBuilder() {
		this.filtro = new FinalizaDocumentoFiltroDto();
	}

	public FinalizaDocumentoFiltroBuilder setPd(ProcessoDocumento pd) {
		this.filtro.setPd(pd);
		return this;
	}

	public FinalizaDocumentoFiltroBuilder setProcesso(ProcessoTrf processo) {
		this.filtro.setProcesso(processo);
		return this;
	}

	public FinalizaDocumentoFiltroBuilder setIdTaskInstance(Long idTaskInstance) {
		this.filtro.setIdTaskInstance(idTaskInstance);
		return this;
	}

	public FinalizaDocumentoFiltroBuilder setSubstituirNomeDocumento(boolean substituirNomeDocumento) {
		this.filtro.setSubstituirNomeDocumento(substituirNomeDocumento);
		return this;
	}

	public FinalizaDocumentoFiltroBuilder setUpdateBin(boolean updateBin) {
		this.filtro.setUpdateBin(updateBin);
		return this;
	}

	public FinalizaDocumentoFiltroBuilder setMarcarComoNaoLido(boolean marcarComoNaoLido) {
		this.filtro.setMarcarComoNaoLido(marcarComoNaoLido);
		return this;
	}

	public FinalizaDocumentoFiltroBuilder setPessoa(Pessoa pessoa) {
		this.filtro.setPessoa(pessoa);
		return this;
	}

	public FinalizaDocumentoFiltroBuilder setLocalizacaoUsuario(Localizacao localizacaoUsuario) {
		this.filtro.setLocalizacaoUsuario(localizacaoUsuario);
		return this;
	}

	public FinalizaDocumentoFiltroBuilder setPapelUsuario(Papel papelUsuario) {
		this.filtro.setPapelUsuario(papelUsuario);
		return this;
	}

	public FinalizaDocumentoFiltroBuilder setJuntar(boolean juntar) {
		this.filtro.setJuntar(juntar);
		return this;
	}
	
	public FinalizaDocumentoFiltroDto build() {
		return this.filtro;
	}
	
	
}
