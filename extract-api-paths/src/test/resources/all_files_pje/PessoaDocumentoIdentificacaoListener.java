package br.jus.cnj.pje.entidades.listeners;

import br.com.infox.pje.manager.ProcessoTrfManager;
import org.jboss.seam.core.Events;

import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.service.AutomacaoTagService;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;

public class PessoaDocumentoIdentificacaoListener {

	public void prePersist(PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao) {
		pessoaDocumentoIdentificacao.setNomeUsuarioLogin(pessoaDocumentoIdentificacao.getPessoa().getNome());
		pessoaDocumentoIdentificacao.setNumeroDocumento(adicionarMascara(pessoaDocumentoIdentificacao));
	}
	
	public void preUpdate(PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao) {
		pessoaDocumentoIdentificacao.setNumeroDocumento(adicionarMascara(pessoaDocumentoIdentificacao));
	}
	
	public void postInsert(PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao){
		processarTags(pessoaDocumentoIdentificacao);
	}

	public void postUpdate(PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao){
		processarTags(pessoaDocumentoIdentificacao);
	}
	
	private void processarTags(PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao) {
		if (pessoaDocumentoIdentificacao.getIdPessoa() != null) {
			Events.instance().raiseAsynchronousEvent(AutomacaoTagService.EVENTO_AUTOMACAO_TAG_IDPESSOA, pessoaDocumentoIdentificacao.getIdPessoa().intValue());
		}
	}
	
	private String adicionarMascara(PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao) {
		if (pessoaDocumentoIdentificacao.getTipoDocumento().getCodTipo().equals("CPF") || 
				pessoaDocumentoIdentificacao.getTipoDocumento().getCodTipo().equals("CPJ")) {
			
			return InscricaoMFUtil.acrescentaMascaraMF(pessoaDocumentoIdentificacao.getNumeroDocumento());
		}
		
		return pessoaDocumentoIdentificacao.getNumeroDocumento();
	}
	
}
