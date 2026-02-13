package br.jus.cnj.pje.intercomunicacao.v223.servico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.jus.cnj.intercomunicacao.v223.beans.CadastroIdentificador;
import br.jus.cnj.intercomunicacao.v223.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v223.beans.ModalidadePoloProcessual;
import br.jus.cnj.intercomunicacao.v223.beans.Parte;
import br.jus.cnj.intercomunicacao.v223.beans.Pessoa;
import br.jus.cnj.intercomunicacao.v223.beans.PoloProcessual;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.util.StringUtil;

public class CorrelacaoParteCriminalPartePjeService {
	private List<CorrelacaoParteCriminalPartePje> listaCorrelacao = new ArrayList<CorrelacaoParteCriminalPartePje>();

	private ManifestacaoProcessual manifestacaoProcessual;

	private static final Logger logger = LoggerFactory.getLogger(CorrelacaoParteCriminalPartePjeService.class);

	public CorrelacaoParteCriminalPartePjeService(ManifestacaoProcessual manifestacaoProcessual) {
		this.manifestacaoProcessual = manifestacaoProcessual;
	}

	public List<CorrelacaoParteCriminalPartePje> getListaCorrelacao() {
		return listaCorrelacao;
	}

	public void setListaCorrelacao(List<CorrelacaoParteCriminalPartePje> listaCorrelacao) {
		this.listaCorrelacao = listaCorrelacao;
	}

	public void adicionarCorrelacao(PoloProcessual polo, Parte parteMni, ProcessoParte processoParte,
			br.jus.cnj.intercomunicacao.v223.criminal.Parte parteCriminal) {
		if (ModalidadePoloProcessual.PA.equals(polo.getPolo()) && processoParte.getPartePrincipal()) {
			CorrelacaoParteCriminalPartePje correlacao = new CorrelacaoParteCriminalPartePje();
			correlacao.setPartePje(processoParte);
			correlacao.setParteMni(parteMni);
			correlacao.setParteCriminal(parteCriminal);
			this.listaCorrelacao.add(correlacao);
		}
	}

	public void adicionarPartesCriminaisNaCorrelacaoPelaPosicao(
			List<br.jus.cnj.intercomunicacao.v223.criminal.Parte> partesCriminal) {
		if (listaCorrelacao.size() != partesCriminal.size()) {
			throw new RuntimeException(
					"A lista de partes do polo passivo do cabeçalho do processo não corresponde à lista dentro das informações criminais.");
		}

		int indice = 0;
		for (br.jus.cnj.intercomunicacao.v223.criminal.Parte parteCriminal : partesCriminal) {
			CorrelacaoParteCriminalPartePje correlacao = listaCorrelacao.get(indice);
			correlacao.setParteCriminal(parteCriminal);
			indice++;
		}

	}

	public void adicionarPartesCriminaisNaCorrelacaoPorNomeOuDocumento(
			List<br.jus.cnj.intercomunicacao.v223.criminal.Parte> partesCriminal) {
		if (listaCorrelacao.size() != partesCriminal.size()) {
			throw new RuntimeException(
					"A lista de partes do polo passivo do cabeçalho do processo não corresponde à lista dentro das informações criminais.");
		}

		Map<String, CorrelacaoParteCriminalPartePje> mapaCpfCnpjParaProcessoParte = novoMapaDocumentoPrincipalParaProcessoParte();
		Integer contador = 0;
		for (br.jus.cnj.intercomunicacao.v223.criminal.Parte parteCriminal : partesCriminal) {

			String documentoPrincipal = corrigirMascaraMF(parteCriminal.getNumeroDocumentoPrincipal());
			CorrelacaoParteCriminalPartePje correlacaoEncontrada = null;

			// Tentar localizar pelo CPF primeiramente.
			if (documentoPrincipal != null) {
				correlacaoEncontrada = mapaCpfCnpjParaProcessoParte.get(documentoPrincipal);
			}

			// tentar localizar pelo nome sem CPF ou CNPJ. Caso possua mais de
			// um com o mesmo nome, localizar por posição.
			if (correlacaoEncontrada == null) {
				correlacaoEncontrada = localizarPorNome(parteCriminal, contador);
			}

			if (correlacaoEncontrada != null) {
				if (correlacaoEncontrada.isParteCriminalJaCorrelacionada()) {
					throw new RuntimeException(
							"Houve falha na correlação nos dados das partes criminais e partes do MNI. Favor conferir.");
				}
				correlacaoEncontrada.setParteCriminal(parteCriminal);
			}
			contador++;
		}

		validarAposCorrelacionarPartesDoCriminal();

	}

	private void validarAposCorrelacionarPartesDoCriminal() {
		for (CorrelacaoParteCriminalPartePje correlacao : listaCorrelacao) {
			if (!correlacao.todasAsCorrelacoesEstaoPreenchidas()) {
				throw new RuntimeException(
						"Houve falha na correlação nos dados das partes criminais e partes do MNI. Favor conferir.");
			}
		}

	}

	private CorrelacaoParteCriminalPartePje localizarPorNome(br.jus.cnj.intercomunicacao.v223.criminal.Parte parteCriminal,
			Integer indiceParteCriminal) {
		CorrelacaoParteCriminalPartePje resultado = null;
		
		Integer posicaoAtual = 0;
		Map<Integer, CorrelacaoParteCriminalPartePje> mapaCorrelacoesEncontradas = new HashMap<Integer, CorrelacaoParteCriminalPartePje>();
		for (CorrelacaoParteCriminalPartePje correlacao : listaCorrelacao) {
			if (!isCadastroIdentificadorPreenchido(correlacao.getParteMni().getPessoa())) {
				String nomePessoaMni = tratarDescricao(correlacao.getParteMni().getPessoa().getNome());
				String nomePessoaCriminal = tratarDescricao(parteCriminal.getNome());
				
				if(StringUtil.isEmpty(nomePessoaCriminal)){
					throw new RuntimeException(
							"Há partes inseridas no conteúdo criminal que estão sem nome ou sem CPF cadastrados.");
				}
				
				if (nomePessoaMni.equals(nomePessoaCriminal)) {
					mapaCorrelacoesEncontradas.put(posicaoAtual, correlacao);
				}
			}
			posicaoAtual++;
		}

		// Se existir mais de uma pessoa com o mesmo nome encontrada.
		if (mapaCorrelacoesEncontradas.size() == 0) {
			resultado = null;

		}

		if (mapaCorrelacoesEncontradas.size() == 1) {
			resultado = (CorrelacaoParteCriminalPartePje) mapaCorrelacoesEncontradas.values().toArray()[0];
		}

		if (mapaCorrelacoesEncontradas.size() > 1) {
			mapaCorrelacoesEncontradas.get(indiceParteCriminal);
		}

		return resultado;
	}

	public void validarCorrelacao() {
		for (CorrelacaoParteCriminalPartePje correlacao : listaCorrelacao) {
			if (correlacao.getParteMni() == null || correlacao.getPartePje() == null
					|| correlacao.getParteCriminal() == null) {
				throw new RuntimeException(
						"Houve falha ao obter a correlação entre as partes do Criminal e Pje. Favor verificar a ordem das partes.");
			}
			validarCpfOuCnpj(correlacao);
			validarNomeSeNaoExistirCpfOuCnpj(correlacao);
		}
	}

	private void validarNomeSeNaoExistirCpfOuCnpj(CorrelacaoParteCriminalPartePje correlacao) {
		if (!isCadastroIdentificadorPreenchido(correlacao.getParteMni().getPessoa())) {
			String nomeParteMni = tratarDescricao(correlacao.getParteMni().getPessoa().getNome());
			String nomeParteCriminal = tratarDescricao(correlacao.getParteCriminal().getNome());
			if (!nomeParteMni.equalsIgnoreCase(nomeParteCriminal)) {
				throw new RuntimeException(
						"Houve falha ao obter a correlação entre os 'NOMES' das partes do Criminal e Pje. Favor verificar a ordem das partes.");
			}
		}
	}

	private void validarCpfOuCnpj(CorrelacaoParteCriminalPartePje correlacao) {
		if (isCadastroIdentificadorPreenchido(correlacao.getParteMni().getPessoa())) {
			try {
				String cpfOuCnpjParteMni = corrigirMascaraMF(
						correlacao.getParteMni().getPessoa().getNumeroDocumentoPrincipal().getValue());
				String cpfOuCnpjPartePje = corrigirMascaraMF(
						correlacao.getPartePje().getPessoa().getDocumentoCpfCnpj());
				String cpfOuCnpjParteCriminal = corrigirMascaraMF(
						correlacao.getParteCriminal().getNumeroDocumentoPrincipal().getValue());

				if (!cpfOuCnpjParteMni.equals(cpfOuCnpjPartePje) || !cpfOuCnpjParteMni.equals(cpfOuCnpjParteCriminal)
						|| !cpfOuCnpjPartePje.equals(cpfOuCnpjParteCriminal)) {
					throw new RuntimeException(
							"Houve falha ao obter a correlação entre os 'CPF/CNPJ' das partes do Criminal e Pje. Favor verificar a ordem das partes.: "
									+ correlacao.getParteMni().getPessoa().getNome());
				}
			} catch (Exception e) {
				throw new RuntimeException(
						"Houve falha ao obter a correlação entre os 'CPF/CNPJ' das partes do Criminal e Pje. Favor verificar a ordem das partes.: "
								+ correlacao.getParteMni().getPessoa().getNome());
			}
		}
	}

	private String corrigirMascaraMF(String documento) {
		String documentoCorrigido = documento;
		try {
			documentoCorrigido = InscricaoMFUtil.acrescentaMascaraMF(documento);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return documentoCorrigido;
	}

	private String corrigirMascaraMF(CadastroIdentificador documento) {
		String documentoCorrigido = null;

		try {
			if (documento != null && documento.getValue() != null) {
				documentoCorrigido = corrigirMascaraMF(documento.getValue());
			}
		} catch (Exception e) {
			throw new RuntimeException("Houve falha ao obter o CPF: " + documento.getValue());
		}
		return documentoCorrigido;
	}

	private boolean isCadastroIdentificadorPreenchido(Pessoa pessoa) {
		return (pessoa != null && 
				pessoa.getNumeroDocumentoPrincipal() != null && 
				StringUtil.isNotEmpty(pessoa.getNumeroDocumentoPrincipal().getValue()));
	}

	private String tratarDescricao(String descricao) {
		if (descricao == null) {
			return null;
		}
		return StringUtil.substituiCaracteresAcentuados(descricao).trim().toUpperCase();
	}

	public ManifestacaoProcessual getManifestacaoProcessual() {
		return manifestacaoProcessual;
	}

	public void setManifestacaoProcessual(ManifestacaoProcessual manifestacaoProcessual) {
		this.manifestacaoProcessual = manifestacaoProcessual;
	}

	private Map<String, CorrelacaoParteCriminalPartePje> novoMapaDocumentoPrincipalParaProcessoParte() {
		Map<String, CorrelacaoParteCriminalPartePje> resultado = new HashMap<String, CorrelacaoParteCriminalPartePje>();
		for (CorrelacaoParteCriminalPartePje correlacao : listaCorrelacao) {
			if (isCadastroIdentificadorPreenchido(correlacao.getParteMni().getPessoa())) {
				String cpfOuCnpj = corrigirMascaraMF(
						correlacao.getParteMni().getPessoa().getNumeroDocumentoPrincipal().getValue());
				resultado.put(cpfOuCnpj, correlacao);
			}
		}
		return resultado;
	}

}
