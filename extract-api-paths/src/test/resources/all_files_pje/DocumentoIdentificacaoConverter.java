package br.jus.pje.api.converters;

import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.DocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;

public class DocumentoIdentificacaoConverter {

	public DocumentoIdentificacao convertFrom(PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao) {
		DocumentoIdentificacao documentoIdentificacao = new DocumentoIdentificacao();
		TipoDocumentoIdentificacaoParaModalidadeDocumentoIdentificadorConverter tipoConverter = new TipoDocumentoIdentificacaoParaModalidadeDocumentoIdentificadorConverter(); 
		
		if(pessoaDocumentoIdentificacao != null) {
			documentoIdentificacao.setCodigoDocumento(pessoaDocumentoIdentificacao.getNumeroDocumento());
			documentoIdentificacao.setTipoDocumento(
				tipoConverter.converter(pessoaDocumentoIdentificacao.getTipoDocumento())
			);
			documentoIdentificacao.setEmissorDocumento(pessoaDocumentoIdentificacao.getOrgaoExpedidor());
		}
		return documentoIdentificacao;
	}
}
