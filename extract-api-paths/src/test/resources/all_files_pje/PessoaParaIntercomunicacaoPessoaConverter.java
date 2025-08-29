/**
 * PessoaParaIntercomunicacaoPessoaConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;

import br.jus.cnj.intercomunicacao.v222.beans.CadastroIdentificador;
import br.jus.cnj.intercomunicacao.v222.beans.Data;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao;
import br.jus.cnj.intercomunicacao.v222.beans.Endereco;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeDocumentoIdentificador;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeGeneroPessoa;
import br.jus.cnj.intercomunicacao.v222.beans.RelacionamentoPessoal;
import br.jus.cnj.intercomunicacao.v222.beans.TipoQualificacaoPessoa;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.Pais;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.enums.ServicosPJeMNIEnum;
import br.jus.pje.nucleo.enums.SexoEnum;

/**
 * Conversor de Pessoa para Pessoa da intercomunicação.
 * 
 * @author Adriano Pamplona
 */
public class PessoaParaIntercomunicacaoPessoaConverter
		extends
		IntercomunicacaoConverterAbstrato<Pessoa, br.jus.cnj.intercomunicacao.v222.beans.Pessoa> {
	
	private final String NACIONALIDADE_PADRAO = "BR";
	
	@In (br.jus.cnj.pje.intercomunicacao.v222.servico.IntercomunicacaoService.NAME)
	br.jus.cnj.pje.intercomunicacao.v222.servico.IntercomunicacaoService intercomunicacaoService;
	
	@SuppressWarnings("static-access")
	public PessoaParaIntercomunicacaoPessoaConverter() {
		intercomunicacaoService = intercomunicacaoService.getInstance();
	}
	
	@Override
	public br.jus.cnj.intercomunicacao.v222.beans.Pessoa converter(Pessoa pessoa) {
		br.jus.cnj.intercomunicacao.v222.beans.Pessoa resultado = null;
		
		if (isNotNull(pessoa)) {
			resultado = new br.jus.cnj.intercomunicacao.v222.beans.Pessoa();
			resultado.setNome(pessoa.getNome());;
			if (pessoa instanceof PessoaFisica) {
				PessoaFisica pessoaFisica = (PessoaFisica) pessoa;
			
				resultado.setTipoPessoa(TipoQualificacaoPessoa.FISICA);
				resultado.setCidadeNatural(obterCidadeNatural(pessoaFisica));
				resultado.setDataNascimento(obterDataNascimento(pessoaFisica));
				resultado.setDataObito(obterDataObito(pessoaFisica));
				resultado.setEstadoNatural(obterEstadoNatural(pessoaFisica));
				resultado.setNacionalidade(obterNacionalidade(pessoaFisica));
				resultado.setNomeGenitor(pessoaFisica.getNomeGenitor());
				resultado.setNomeGenitora(pessoaFisica.getNomeGenitora());
				resultado.setNumeroDocumentoPrincipal(obterNumeroDocumentoPrincipal(pessoaFisica));
				resultado.setSexo(obterSexo(pessoaFisica));
				
				if(intercomunicacaoService.getServicoAtual() == null || isServicoConsultarProcesso()){
					resultado.getOutroNome().addAll(consultarColecaoOutroNome(pessoa));				
				}
			} else if (pessoa instanceof PessoaJuridica) {
				PessoaJuridica pessoaJuridica = (PessoaJuridica) pessoa;
				
				resultado.setTipoPessoa(TipoQualificacaoPessoa.JURIDICA);
				resultado.setNumeroDocumentoPrincipal(obterNumeroDocumentoPrincipal(pessoaJuridica));
				resultado.setDataNascimento(obterDataNascimento(pessoaJuridica));
			} else if (pessoa instanceof PessoaAutoridade) {
				PessoaAutoridade pessoaAutoridade = (PessoaAutoridade)pessoa;
				
				resultado.setTipoPessoa(TipoQualificacaoPessoa.AUTORIDADE);
				resultado.setNumeroDocumentoPrincipal(obterNumeroDocumentoPrincipal(pessoaAutoridade));
				resultado.setPessoaVinculada(converter(pessoaAutoridade.getOrgaoVinculacao()));
			}
			
			if (!isServicoConsultarAvisosPendentes()) {
				resultado.getPessoaRelacionada().addAll(consultarColecaoPessoaRelacionada(pessoa));
			}
			
			if(intercomunicacaoService.getServicoAtual() == null || isServicoConsultarProcesso()){
				resultado.getDocumento().addAll(consultarColecaoDocumento(pessoa));
			}
		}
		return resultado;
	}

	/**
	 * @param pessoaFisica
	 * @return sexo.
	 */
	protected ModalidadeGeneroPessoa obterSexo(PessoaFisica pessoaFisica) {
		ModalidadeGeneroPessoa resultado = ModalidadeGeneroPessoa.D;
		
		SexoEnum sexo = pessoaFisica.getSexo();
		if (isNotNull(sexo)) {
			if (sexo.equals(SexoEnum.M)) {
				resultado = ModalidadeGeneroPessoa.M;
			} else {
				resultado = ModalidadeGeneroPessoa.F;
			}
		}
		return resultado;
	}

	/**
	 * @param pessoaFisica
	 * @return número do documento principal.
	 */
	protected CadastroIdentificador obterNumeroDocumentoPrincipal(
			PessoaFisica pessoaFisica) {
		CadastroIdentificador resultado = null;
		String cpf = InscricaoMFUtil.retiraMascara(pessoaFisica.getNumeroCPF());
		
		if (StringUtils.isNotBlank(cpf)) {
			resultado = MNIUtil.novoCadastroIdentificador(cpf);
		}
		return resultado;
	}

	/**
	 * @param pessoaFisica
	 * @return número do documento principal.
	 */
	protected CadastroIdentificador obterNumeroDocumentoPrincipal(
			PessoaJuridica pessoaJuridica) {
		CadastroIdentificador resultado = null;
		String cnpj = InscricaoMFUtil.retiraMascara(pessoaJuridica.getNumeroCNPJ());
		
		if (StringUtils.isNotBlank(cnpj)) {
			resultado = MNIUtil.novoCadastroIdentificador(cnpj);
		}
		return resultado;
	}

	/**
	 * @param pessoaAutoridade
	 * @return número do documento principal.
	 */
	protected CadastroIdentificador obterNumeroDocumentoPrincipal(
			PessoaAutoridade pessoaAutoridade) {
		CadastroIdentificador identificador = null;
		PessoaJuridica orgaoVinculacao = pessoaAutoridade.getOrgaoVinculacao();
		if (isNotNull(orgaoVinculacao)) {
			identificador = obterNumeroDocumentoPrincipal(orgaoVinculacao);
		}
		return identificador;
	}

	/**
	 * @param pessoaFisica
	 * @return nacionalidade
	 */
	protected String obterNacionalidade(PessoaFisica pessoaFisica) {
		String resultado = NACIONALIDADE_PADRAO;
		
		if (pessoaFisica != null && pessoaFisica.getPaisNascimento() != null) {
			Pais pais = pessoaFisica.getPaisNascimento();
			resultado = (StringUtils.isNotBlank(pais.getSigla()) ? pais.getSigla() : NACIONALIDADE_PADRAO);
		}
		return resultado;
	}

	/**
	 * @param pessoaFisica
	 * @return estado natural
	 */
	protected String obterEstadoNatural(PessoaFisica pessoaFisica) {
		Municipio municipio = pessoaFisica.getMunicipioNascimento();
		return (isNotNull(municipio)? municipio.getEstado().getCodEstado() : null);
	}

	/**
	 * @param pessoaFisica
	 * @return data de nascimento.
	 */
	protected Data obterDataNascimento(PessoaFisica pessoaFisica) {
		Date dataNascimento = pessoaFisica.getDataNascimento();
		return ConversorUtil.converterParaData(dataNascimento);
	}

	/**
	 * @param pessoaFisica
	 * @return data de óbito.
	 */
	protected Data obterDataObito(PessoaFisica pessoaFisica) {
		Date dataObito = pessoaFisica.getDataObito();
		return ConversorUtil.converterParaData(dataObito);
	}

	/**
	 * @param pessoaJuridica
	 * @return data de nascimento.
	 */
	protected Data obterDataNascimento(PessoaJuridica pessoaJuridica) {
		Date dataNascimento = pessoaJuridica.getDataAbertura();
		return ConversorUtil.converterParaData(dataNascimento);
	}

	/**
	 * @param pessoa
	 * @return cidade natural.
	 */
	protected String obterCidadeNatural(PessoaFisica pessoa) {
		Municipio municipio = pessoa.getMunicipioNascimento();
		return (isNotNull(municipio)? municipio.getMunicipio(): null);
	}

	/**
	 * @param pessoa
	 * @return coleção de nomes alternativos.
	 */
	protected List<String> consultarColecaoOutroNome(Pessoa pessoa) {
		List<String> resultado = new ArrayList<String>();
		
		List<PessoaNomeAlternativo> nomes = pessoa.getPessoaNomeAlternativoList();
		for (PessoaNomeAlternativo nome : nomes) {
			String alternativo = nome.getPessoaNomeAlternativo();
			if (!resultado.contains(alternativo)) {
				resultado.add(alternativo);
			}
		}
		return resultado;
	}

	/**
	 * @param pessoa
	 * @return coleção de relacionamentos pessoais.
	 */
	protected List<RelacionamentoPessoal> consultarColecaoPessoaRelacionada(Pessoa pessoa) {
		RelacaoPessoalParaRelacionamentoPessoalConverter converter = new RelacaoPessoalParaRelacionamentoPessoalConverter();
		return converter.converterColecao(pessoa.getRelacaoPessoalList());
	}

	/**
	 * @param pessoa
	 * @return coleção de endereços da pessoa.
	 */
	protected List<Endereco> consultarColecaoEndereco(Pessoa pessoa) {
		EnderecoParaIntercomunicacaoEnderecoConverter converter = new EnderecoParaIntercomunicacaoEnderecoConverter();
		return converter.converterColecao(pessoa.getEnderecoList());
	}

	/**
	 * @param pessoa
	 * @return coleção de documentos da pessoa.
	 */
	protected List<DocumentoIdentificacao> consultarColecaoDocumento(
			Pessoa pessoa) {
		List<DocumentoIdentificacao> resultado = new ArrayList<DocumentoIdentificacao>();
		
		Set<PessoaDocumentoIdentificacao> documentos = pessoa.getPessoaDocumentoIdentificacaoList();
		for (PessoaDocumentoIdentificacao documento : documentos) {
                        //os documentos de identificação inativos (excluídos) não são convertidos, pois o MNI não prevê
                        //campo para informar se um documento de identificação está ativo
                        if (documento.getAtivo()){
                                ModalidadeDocumentoIdentificador identificacao = converter(documento.getTipoDocumento());
                                if(isNotNull(identificacao)) {
                                        DocumentoIdentificacao docIdentificacao = new DocumentoIdentificacao();
                                        docIdentificacao.setTipoDocumento(identificacao);
                                        docIdentificacao.setCodigoDocumento(documento.getNumeroDocumento());
                                        docIdentificacao.setEmissorDocumento(documento.getOrgaoExpedidor());
                                        docIdentificacao.setNome(documento.getNome());
                                        resultado.add(docIdentificacao);
                                }
                        }
		}
		return resultado;
	}
	
	/**
	 * Converte TipoDocumentoIdentificacao para ModalidadeDocumentoIdentificador.
	 * @param identificacao
	 * @return ModalidadeDocumentoIdentificador
	 */
	protected ModalidadeDocumentoIdentificador converter(TipoDocumentoIdentificacao identificacao) {
		return novoTipoDocumentoIdentificacaoParaModalidadeDocumentoIdentificadorConverter().converter(identificacao);
	}
	
	/**
	 * @return TipoDocumentoIdentificacaoParaModalidadeDocumentoIdentificadorConverter.java
	 */
	protected TipoDocumentoIdentificacaoParaModalidadeDocumentoIdentificadorConverter novoTipoDocumentoIdentificacaoParaModalidadeDocumentoIdentificadorConverter() {
		return new TipoDocumentoIdentificacaoParaModalidadeDocumentoIdentificadorConverter();
	}
}
