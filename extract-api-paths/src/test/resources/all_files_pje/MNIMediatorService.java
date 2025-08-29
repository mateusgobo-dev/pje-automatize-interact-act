/*
 * MNIMediatorService.java
 *
 * Data: 28/07/2020
 */
package br.jus.cnj.pje.intercomunicacao.service;

import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;

/**
 * Classe mediator responsável pelo encapsulamento da intercomunicacão entre
 * sistemas sem expor a dependência com a biblioteca cnj-interop.
 * 
 * @author Adriano Pamplona
 */
public interface MNIMediatorService {
	
	/**
	 * Entrega de manifestação processual.
	 * 
	 * @param requisicao ManifestacaoProcessualRequisicaoDTO
	 * @return ManifestacaoProcessualRespostaDTO
	 * @throws IntercomunicacaoException
	 */
	public ManifestacaoProcessualRespostaDTO entregarManifestacaoProcessual(ManifestacaoProcessualRequisicaoDTO requisicao) throws IntercomunicacaoException;
	
	/**
	 * Consulta processual.
	 * 
	 * @param requisicao ConsultarProcessoRequisicaoDTO
	 * @return ConsultarProcessoRespostaDTO
	 * @throws IntercomunicacaoException
	 */
	public ConsultarProcessoRespostaDTO consultarProcesso(ConsultarProcessoRequisicaoDTO requisicao) throws IntercomunicacaoException;
	
	/**
	 * Verifica se o login com o endpoint está correto.
	 * 
	 * @return ConsultarProcessoRespostaDTO
	 * @throws IntercomunicacaoException
	 */
	public ConsultarProcessoRespostaDTO login() throws IntercomunicacaoException;
	
	/**
	 * Verifica se o login com o endpoint está correto.
	 * 
	 * @return login
	 * @return senha
	 * @throws IntercomunicacaoException
	 */
	public ConsultarProcessoRespostaDTO login(String login, String senha) throws IntercomunicacaoException;
}
