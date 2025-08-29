package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import br.com.itx.exception.AplicationException;
import br.jus.pje.nucleo.dto.domicilioeletronico.PessoaDomicilioEletronicoDTO;

public class PessoaDomicilioEletronicoDtoBuilder {

	private static final String CNPJ = "CNPJ";
	private static final String COLUNA_DOCUMENTO = "DOCUMENTO";
	private static final int INDEX_CAMPO_DOCUMENTO = 0;
	private static final int INDEX_TIPO_DOCUMENTO = 1;
	private static final int INDEX_HABILITADO = 2;
	private static final int INDEX_PESSOA_JURIDICA_DIREITO_PUBLICO = 3;
	
	private static final String SEPARADOR = ";";

	/**
	 * Construtor.
	 */
	private PessoaDomicilioEletronicoDtoBuilder() {
		//Construtor.
	}

	public static List<PessoaDomicilioEletronicoDTO> create(List<String> linhasArquivo) {
		if (linhasArquivo == null) {
			return Collections.emptyList();
		}
		linhasArquivo = removerHeader(linhasArquivo);
		return linhasArquivo.stream().map(PessoaDomicilioEletronicoDtoBuilder::create).collect(Collectors.toList());
	}

	private static List<String> removerHeader(List<String> linhasArquivo) {
		String header = linhasArquivo.get(0);
		if (isHeader(header)) {
			linhasArquivo.remove(0);
		}
		return linhasArquivo;
	}

	private static boolean isHeader(String header) {
		if (header == null) {
			return false;
		}
		return header.toUpperCase().contains(COLUNA_DOCUMENTO);
	}

	public static PessoaDomicilioEletronicoDTO create(String linha) {
		try {
			if (linha == null) {
				return null;
			}
			boolean pessoaJuridicaDireitoPublico = false;
			String[] campos = linha.split(SEPARADOR);
			String documento = campos[INDEX_CAMPO_DOCUMENTO];
			String tipoDocumento = campos[INDEX_TIPO_DOCUMENTO];
			boolean habilitado = Boolean.parseBoolean(campos[INDEX_HABILITADO]);

			if (CNPJ.equals(tipoDocumento) && campos.length == 4) {
				pessoaJuridicaDireitoPublico = Boolean.parseBoolean(campos[INDEX_PESSOA_JURIDICA_DIREITO_PUBLICO]);
			}
			return new PessoaDomicilioEletronicoDTO(documento, tipoDocumento, habilitado, pessoaJuridicaDireitoPublico);
		} catch (Exception e) {
			throw new AplicationException("Erro ao converter linha do CSV em objeto PessoaDomicilioEletronicoDto. ", e);
		}

	}
}
