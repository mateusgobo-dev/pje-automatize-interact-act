package br.jus.pje.api.converters;

import java.util.List;

import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.CadastroOAB;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.DocumentoIdentificacao;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Endereco;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.ModalidadeDocumentoIdentificador;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.ModalidadeRepresentanteProcessual;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.RepresentanteProcessual;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;

public class RepresentanteProcessualConverter {
	
	public RepresentanteProcessual convertFrom(ProcessoParteRepresentante representante) {
		DocumentoIdentificacaoConverter documentoIdentificacaoConverter = new DocumentoIdentificacaoConverter();
		EnderecoConverter enderecoConverter = new EnderecoConverter();
		RepresentanteProcessual representanteProcessual = new RepresentanteProcessual();

		Pessoa pessoa = representante.getParteRepresentante().getPessoa();		
		List<Endereco> enderecos = enderecoConverter.convertFrom(representante.getParteRepresentante().getProcessoParteEnderecoList());

		CadastroOAB cadastroOAB = new CadastroOAB();		
		PessoaDocumentoIdentificacao identificacao = getOAB(pessoa);
		if(identificacao != null) {
			DocumentoIdentificacao documentoIdentificacao = documentoIdentificacaoConverter.convertFrom(identificacao);		
			cadastroOAB.setValue(documentoIdentificacao.getCodigoDocumento());
		}		
		
		representanteProcessual.setNome(pessoa.getNome());
		representanteProcessual.setEndereco(enderecos);
		representanteProcessual.setInscricao(cadastroOAB);
		representanteProcessual.setNumeroDocumentoPrincipal(pessoa.getDocumentoCpfCnpj());
		representanteProcessual.setTipoRepresentante(ModalidadeRepresentanteProcessual.A);
		
		return representanteProcessual;
	}

	private PessoaDocumentoIdentificacao getOAB(Pessoa pessoa) {
		return pessoa.getPessoaDocumentoIdentificacaoList().stream()
				.filter(x -> x.getTipoDocumento().getCodTipo().equals(ModalidadeDocumentoIdentificador.OAB.value()))
				.findFirst()
				.orElse(new PessoaDocumentoIdentificacao());
	}
}
