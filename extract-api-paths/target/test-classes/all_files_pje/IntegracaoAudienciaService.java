package br.jus.csjt.pje.business.service;

import java.sql.Blob;
import java.util.Date;

public interface IntegracaoAudienciaService {
	/**
	 * Hash MD5 utilizado para verificar a integridade do xml recebido. No caso
	 * dos serviços "set" esse atributo não será utilizado, pois o hash_md5 deve
	 * ser passado como parâmetro de entrada, já que a verificação se fará no
	 * lado do servidor (PJe). Definição dos serviços que serão expostos pelo
	 * PJe via WebService para consumo pelo sistema Aud
	 * 
	 * /** Retorna o objeto contendo a lista das pautas do período solicitado
	 * através dos parâmetros de entrada dataHoraInicio e Date dataHoraFim O
	 * parâmetro "assinatura" é utilizado para "autenticação". Verificar
	 * comentários no cabeçalho para maiores informações.
	 */
	public RetornoPauta getListaPauta(String assinatura, Date dataHoraInicio, Date dataHoraFim);

	/**
	 * Retorna o objeto contendo a lista dos juízes cadastrados no PJe O
	 * parâmetro "assinatura" é utilizado para "autenticação".
	 */
	public RetornoAud getListaJuizes(String assinatura);

	/**
	 * Retorna o objeto contendo a lista dos peritos cadastrados no PJe O
	 * parâmetro "assinatura" é utilizado para "autenticação".
	 */
	public RetornoAud getListaPeritos(String assinatura);

	/**
	 * Retorna o objeto contendo a lista dos advogados cadastrados no PJe O
	 * parâmetro "assinatura" é utilizado para "autenticação".
	 */
	public RetornoAud getListaAdvogados(String assinatura);

	/**
	 * Retorna o objeto contendo lista das espécies (classes processuais)
	 * cadastrados no PJe O parâmetro "assinatura" é utilizado para
	 * "autenticação".
	 */
	public RetornoAud getListaEspecie(String assinatura);

	/**
	 * Retorna o objeto contendo lista de prepostos cadastrados no PJe O
	 * parâmetro "assinatura" é utilizado para "autenticação".
	 */
	public RetornoAud getListaPreposto(String assinatura, Date dataHoraInicio, Date dataHoraFim);

	/**
	 * Retorna o objeto contendo lista dos autores dos processos que têm pauta
	 * no período especificado nos parâmetros dataHoraInicio e dataHoraFim
	 * cadastrados no PJe O parâmetro "assinatura" é utilizado para
	 * "autenticação".
	 */
	public RetornoAud getListaAutor(String assinatura, Date dataHoraInicio, Date dataHoraFim);

	/**
	 * Retorna o objeto contendo lista dos réus dos processos que têm pauta no
	 * período especificado nos parâmetros dataHoraInicio e dataHoraFim
	 * cadastrados no PJe O parâmetro "assinatura" é utilizado para
	 * "autenticação".
	 */
	public RetornoAud getListaReus(String assinatura, Date dataHoraInicio, Date dataHoraFim);

	/**
	 * Envia o XML de resultado da audiência assim com o pdf da ata da audiência
	 * O parâmetro "assinatura" é utilizado para "autenticação".
	 */
	public RetornoAud setResultado(String assinatura, Object resultadoAud, String hash_md5_xml, Blob ataHtml,
			String hash_md5_ata);

}
