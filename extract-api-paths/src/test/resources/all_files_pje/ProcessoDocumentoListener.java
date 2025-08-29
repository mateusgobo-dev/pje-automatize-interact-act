package br.jus.cnj.pje.entidades.listeners;

import java.util.Optional;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.util.StringUtil;

public class ProcessoDocumentoListener {
	
	public void prePersist(ProcessoDocumento processoDocumento) throws PJeBusinessException {
		validarDataJuntada(processoDocumento);
	}
	
	public void preUpdate(ProcessoDocumento processoDocumento) throws PJeBusinessException {
		validarDataJuntada(processoDocumento);
	}

    private void validarDataJuntada(ProcessoDocumento processoDocumento) throws PJeBusinessException {
        if(processoDocumento.getNomeUsuarioJuntada() == null && processoDocumento.getDataJuntada() != null) {
			if (processoDocumento.getDocumentoPrincipal() != null && StringUtil.isSet(processoDocumento.getDocumentoPrincipal().getNomeUsuarioJuntada())) {
				processoDocumento.setNomeUsuarioJuntada(processoDocumento.getDocumentoPrincipal().getNomeUsuarioJuntada());
			} 
			if (processoDocumento.getNomeUsuarioJuntada() == null) {
				processoDocumento.setNomeUsuarioJuntada(processoDocumento.getNomeUsuarioAlteracao());
			}
			if (processoDocumento.getNomeUsuarioJuntada() == null) {
				processoDocumento.setNomeUsuarioJuntada(processoDocumento.getNomeUsuarioInclusao());
			}			
			if (processoDocumento.getNomeUsuarioJuntada() == null && !processoDocumento.getProcessoDocumentoBin().getSignatarios().isEmpty()) {
				Optional<ProcessoDocumentoBinPessoaAssinatura> ultimaAssinatura = processoDocumento.getProcessoDocumentoBin().getSignatarios().stream()
						.sorted((a1, a2) -> a2.getDataAssinatura().compareTo(a1.getDataAssinatura()))
						.findFirst();
				if (ultimaAssinatura.isPresent()) {
					processoDocumento.setNomeUsuarioJuntada(ultimaAssinatura.get().getNomePessoa());
				}
			}
		}
    }
}