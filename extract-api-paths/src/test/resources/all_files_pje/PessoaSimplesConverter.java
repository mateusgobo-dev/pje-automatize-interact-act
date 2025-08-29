package br.jus.pje.api.converters;

import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.CadastroIdentificador;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.ModalidadeQualificacaoPessoa;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.PessoaSimples;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;

public class PessoaSimplesConverter {
	
	public PessoaSimples convertFrom(Pessoa pessoa) {
		PessoaSimples ps = new PessoaSimples();
		
		if(pessoa instanceof PessoaFisica) {
			PessoaFisica pf = (PessoaFisica) pessoa;
			ps = this.convertFromPessoaFisica(pf);
		} else if (pessoa instanceof PessoaJuridica) {
			PessoaJuridica pj = (PessoaJuridica) pessoa;
			ps = this.convertFromPessoaJuridica(pj);
		} else if (pessoa instanceof PessoaAutoridade) {
			PessoaAutoridade pa = (PessoaAutoridade) pessoa;
			ps = this.convertFromPessoaAutoridade(pa);
		}
		
		return ps;
	}
	
	public PessoaSimples convertFromPessoaFisica(PessoaFisica pessoaFisica) {
		PessoaSimples ps = new PessoaSimples();
		
		ps.setNome(pessoaFisica.getNomeParte());
		ps.setQualificacaoPessoa(ModalidadeQualificacaoPessoa.FIS);
		
		CadastroIdentificador ci = new CadastroIdentificador();
		ci.setValue(pessoaFisica.getDocumentoCpfCnpj());
		ps.setNumeroDocumentoPrincipal(ci);
		
		return ps;
	}
	
	public PessoaSimples convertFromPessoaJuridica(PessoaJuridica pessoaJuridica) {
		PessoaSimples ps = new PessoaSimples();
		
		ps.setNome(pessoaJuridica.getNomeParte());
		ps.setQualificacaoPessoa(ModalidadeQualificacaoPessoa.JUR);
		
		CadastroIdentificador ci = new CadastroIdentificador();
		ci.setValue(pessoaJuridica.getDocumentoCpfCnpj());
		
		ps.setNumeroDocumentoPrincipal(ci);
		
		return ps;
	}
	
	public PessoaSimples convertFromPessoaAutoridade(PessoaAutoridade pessoaAutoridade) {
		PessoaSimples ps = new PessoaSimples();
		
		ps.setNome(pessoaAutoridade.getNomeParte());
		ps.setQualificacaoPessoa(ModalidadeQualificacaoPessoa.AUT);
		
		PessoaJuridica pessoaJuridica = pessoaAutoridade.getOrgaoVinculacao();
		if (pessoaJuridica != null) {
			CadastroIdentificador ci = new CadastroIdentificador();
			ci.setValue(pessoaJuridica.getDocumentoCpfCnpj());
			
			ps.setNumeroDocumentoPrincipal(ci);
		}
		
		return ps;
	}
}
