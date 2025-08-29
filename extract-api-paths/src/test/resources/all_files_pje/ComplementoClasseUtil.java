/**
 * ComplementoClasseUtil.java
 * 
 * Data de criação: 12/02/2015
 */
package br.jus.cnj.pje.intercomunicacao.util;

import java.util.HashMap;
import java.util.Map;

import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;

import br.com.infox.exceptions.NegocioException;
import br.com.infox.validator.CnpjValidator;
import br.com.infox.validator.CpfValidator;
import br.com.infox.validator.DataddMMyyyyHHmmssValidator;
import br.com.infox.validator.DummyValidator;
import br.jus.pje.nucleo.entidades.ClasseAplicacao;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ComplementoClasse;
import br.jus.pje.nucleo.entidades.ComplementoClasseProcessoTrf;

/**
 * Classe utilitária para tratamento do objeto ComplementoClasse.
 * 
 * @author Adriano Pamplona
 */
public final class ComplementoClasseUtil {

	private static Map<String, Validator> validadores;
	
	/**
	 * Valida o valor do complemento de classe quanto à obrigatoriedade e o conteúdo.
	 * @param complementoProcesso
	 */
	public static void validar(ComplementoClasseProcessoTrf complementoProcesso) {
		
		if (complementoProcesso != null && complementoProcesso.getComplementoClasse() != null) {
			
			validarObrigatoriedade(complementoProcesso);
			validarConteudo(complementoProcesso);
		}
	}

	/**
	 * Valida se foi informado um valor para um ComplementoClasse obrigatório.
	 * 
	 * @param complementoProcesso
	 */
	private static void validarObrigatoriedade(ComplementoClasseProcessoTrf complementoProcesso) {
		ComplementoClasse complementoClasse = complementoProcesso.getComplementoClasse();
		ClasseAplicacao classeAplicacao = complementoClasse.getClasseAplicacao();
		ClasseJudicial classeJudicial = classeAplicacao.getClasseJudicial();
		
		String nome = complementoClasse.getComplementoClasse();
		String valor = complementoProcesso.getValorComplementoClasseProcessoTrf();
		
		if (complementoClasse.getObrigatorio() && StringUtils.isBlank(valor)) {
			String nomeClasse = classeJudicial.getClasseJudicial();
			String mensagem = "O complemento '%s' da classe '%s' exige um valor obrigatório.";
			throw new NegocioException(String.format(mensagem, nome, nomeClasse));
		}
	}

	/**
	 * Valida se o valor do ComplementoClasse segue a validação informada.
	 * 
	 * @param complementoProcesso
	 */
	private static void validarConteudo(ComplementoClasseProcessoTrf complementoProcesso) {
		ComplementoClasse complementoClasse = complementoProcesso.getComplementoClasse();
		ClasseAplicacao classeAplicacao = complementoClasse.getClasseAplicacao();
		ClasseJudicial classeJudicial = classeAplicacao.getClasseJudicial();
		
		String nome = complementoClasse.getComplementoClasse();
		String valor = complementoProcesso.getValorComplementoClasseProcessoTrf();
		String componenteValidacao = complementoClasse.getComponenteValidacao();
		
		Validator validator = getValidadores().get(componenteValidacao);
		try {
			/*
			 * Se o complemento da classe NÃO é obrigatório e o contéudo de 'valor' é NULL, 
			 * isso implica que não precisa validar esse contéudo.
			 */
			if (!complementoClasse.getObrigatorio() && StringUtils.isBlank(valor)) {
				return;
			} else{				
				validator.validate(null, null, valor);
			}
		} catch (ValidatorException e) {
			String nomeClasse = classeJudicial.getClasseJudicial();
			String mensagem = "O conteúdo do complemento '%s' da classe '%s' é inválido. Erro: %s";
			throw new NegocioException(String.format(mensagem, nome, nomeClasse, e.getMessage()));	
		}
	}
	
	/**
	 * Retorna o mapa de validadores de ComplementoClasse.
	 * 
	 * @return the validadores
	 */
	private static Map<String, Validator> getValidadores() {
		if (validadores == null) {
			validadores = new HashMap<String, Validator>();
			validadores.put("cpf", new CpfValidator());
			validadores.put("cnpj", new CnpjValidator());
			validadores.put("data", new DataddMMyyyyHHmmssValidator());
			validadores.put("default", new DummyValidator());	
		}
		return validadores;
	}
}
