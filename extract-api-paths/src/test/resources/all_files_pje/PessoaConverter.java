package br.jus.pje.api.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.infox.pje.manager.PessoaProcuradoriaEntidadeManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.CadastroIdentificador;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.DocumentoIdentificacao;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.ModalidadeGeneroPessoa;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;

public class PessoaConverter {

	public Pessoa convertFrom(br.jus.pje.nucleo.entidades.Pessoa pessoa) {
		Pessoa ps = new Pessoa();
		if (pessoa != null) {
			PessoaSimplesConverter psc = new PessoaSimplesConverter();

			ps.setDadosBasicos(psc.convertFrom(pessoa));
			
			if(!pessoa.getDocumentoPessoaList().isEmpty())
				ps.setDocumento(convertFromDocumentoIdentificacao(pessoa.getPessoaDocumentoIdentificacaoList()));
			
			if (pessoa instanceof PessoaFisica) {
				PessoaFisica pf = (PessoaFisica) pessoa;
				ps = this.convertFromPessoaFisica(ps, pf);
			} else if (pessoa instanceof PessoaJuridica) {
				PessoaJuridica pj = (PessoaJuridica) pessoa;
				ps = this.convertFromPessoaJuridica(ps, pj);
			} else if (pessoa instanceof PessoaAutoridade) {
				PessoaAutoridade pa = (PessoaAutoridade) pessoa;
				ps = this.convertFromPessoaAutoridade(ps, pa);
			}
		}

		return ps;
	}

	public Pessoa convertFromPessoaFisica(Pessoa pessoa, PessoaFisica pessoaFisica) {
		if (pessoa != null && pessoaFisica != null) {
			pessoa.setDataNascimento(pessoaFisica.getDataNascimento());
			pessoa.setDataObito(pessoaFisica.getDataObito());
			
			if(pessoaFisica.getSexo() != null) {
				pessoa.setSexo(ModalidadeGeneroPessoa.fromValue(pessoaFisica.getSexo().name()));
			}
			
			pessoa.setNomeGenitor(pessoaFisica.getNomeGenitor());
			pessoa.setNomeGenitora(pessoaFisica.getNomeGenitora());
			if (pessoaFisica.getPaisNascimento() != null) {
				pessoa.setNacionalidade(pessoaFisica.getPaisNascimento().getDescricao());
			}
			CadastroIdentificador ci = new CadastroIdentificador();
			ci.setValue(pessoaFisica.getDocumentoCpfCnpj());
			pessoa.setNumeroDocumentoPrincipal(ci);

			CidadeConverter cidadeConverter = new CidadeConverter();
			pessoa.setCidadeNatural(cidadeConverter.convertFrom(pessoaFisica.getMunicipioNascimento()));

			if (pessoaFisica.getPaisNascimento() != null) {
				pessoa.setNacionalidade(pessoaFisica.getPaisNascimento().getDescricao());
			}

			if (pessoaFisica.getEscolaridade() != null) {
				pessoa.setEscolaridade(pessoaFisica.getEscolaridade().getEscolaridade());
			}

		}

		return pessoa;
	}

	public Pessoa convertFromPessoaJuridica(Pessoa pessoa, PessoaJuridica pessoaJuridica) {
		if (pessoa != null && pessoaJuridica != null) {
			pessoa.setDataNascimento(pessoaJuridica.getDataAbertura());
			pessoa.setDataObito(pessoaJuridica.getDataFimAtividade());
			List<String> nomes = new ArrayList<>();
			nomes.add(pessoaJuridica.getNomeFantasia());
			pessoa.setOutroNome(nomes);
			
			PessoaProcuradoriaEntidadeManager ppm = ComponentUtil.getComponent(PessoaProcuradoriaEntidadeManager.NAME);
			if (ppm.getProcuradoriaPadraoPessoa(pessoaJuridica) != null) {
				if(pessoa.getAny() == null) {					
					pessoa.setAny(new ArrayList<>());
				}
				pessoa.getAny().add("OBSPessoaEntidade");				
			}
		}
		return pessoa;
	}

	public Pessoa convertFromPessoaAutoridade(Pessoa pessoa, PessoaAutoridade pessoaAutoridade) {
		if (pessoa != null && pessoaAutoridade != null && pessoaAutoridade.getOrgaoVinculacao() != null) {
			PessoaJuridica pessoaJuridica = pessoaAutoridade.getOrgaoVinculacao();
			
			CadastroIdentificador ci = new CadastroIdentificador();
			ci.setValue(pessoaJuridica.getDocumentoCpfCnpj());
			pessoa.setNumeroDocumentoPrincipal(ci);
			pessoa.setDataNascimento(pessoaJuridica.getDataAbertura());
			pessoa.setDataObito(pessoaJuridica.getDataFimAtividade());
			
			List<String> nomes = new ArrayList<>();
			nomes.add(pessoaJuridica.getNomeFantasia());
			pessoa.setOutroNome(nomes);
		}
		return pessoa;
	}

	private List<DocumentoIdentificacao> convertFromDocumentoIdentificacao(Set<PessoaDocumentoIdentificacao> documentos) {
		DocumentoIdentificacaoConverter documentoIdentificacaoConverter = new DocumentoIdentificacaoConverter();		
		return documentos.stream()
				.map(documentoIdentificacaoConverter::convertFrom)
				.collect(Collectors.toList());
	}

}
