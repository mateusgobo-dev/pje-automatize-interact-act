/**
 * IntercomunicacaoEnderecoParaEnderecoConverter.java
 * 
 * Data de criação: 09/09/2015
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.List;

import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;

import br.com.infox.exceptions.NegocioException;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.CepManager;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Conversor de Endereço da intercomunicação para Endereço.
 * 
 * @author Adriano Pamplona
 */
public class IntercomunicacaoEnderecoParaEnderecoConverter
		extends
		IntercomunicacaoConverterAbstrato<br.jus.cnj.intercomunicacao.v222.beans.Endereco, Endereco> {

	/**
	 * Use o método converter(br.jus.cnj.intercomunicacao.v222.beans.Endereco, Pessoa).
	 */
	@Override
	@Deprecated
	public Endereco converter(br.jus.cnj.intercomunicacao.v222.beans.Endereco enderecoMNI) {
		return null;
	}
	
	/**
	 * Use o método converterColecao(br.jus.cnj.intercomunicacao.v222.beans.Endereco, Pessoa).
	 */
	@Override
	@Deprecated
	public List<Endereco> converterColecao(
			List<br.jus.cnj.intercomunicacao.v222.beans.Endereco> colecaoObjeto) {
		return null;
	}
	
	/**
	 * Converte uma coleção de EndereçoMNI para Endereço.
	 *  
	 * @param colecaoObjeto
	 * @param pessoa
	 * @return coleção de Endereço.
	 */
	public List<Endereco> converterColecao(
			List<br.jus.cnj.intercomunicacao.v222.beans.Endereco> colecaoObjeto, Pessoa pessoa) {
		Transformer transformer = novoTransformador(pessoa);
		return aplicarTransformador(colecaoObjeto, transformer);
	}
	
	
	
	/**
	 * Converte um EndereçoMNI para Endereço.
	 * 
	 * @param enderecoMNI
	 * @param pessoa
	 * @return Endereço.
	 */
	public Endereco converter(br.jus.cnj.intercomunicacao.v222.beans.Endereco enderecoMNI, Pessoa pessoa) {
		Endereco resultado = null;
		
		if (isNotNull(enderecoMNI)) {
			resultado = new Endereco();
			resultado.setCep(obterCep(enderecoMNI.getCep()));
			resultado.setComplemento(enderecoMNI.getComplemento());
			resultado.setCorrespondencia(Boolean.TRUE);
			resultado.setNomeBairro(enderecoMNI.getBairro());
			resultado.setNomeCidade(enderecoMNI.getCidade());
			resultado.setNomeEstado(enderecoMNI.getEstado());
			resultado.setNomeLogradouro(enderecoMNI.getLogradouro());
			resultado.setNumeroEndereco(enderecoMNI.getNumero());
			resultado.setUsuario(pessoa);
			resultado.setUsuarioCadastrador(obterUsuarioLogado());
		}
		return resultado;
	}

	/**
	 * Retorna o objeto Cep do número passado por parâmetro.
	 * 
	 * @param numeroCep
	 * @return CEP
	 */
	protected Cep obterCep(String numeroCep) {
		Cep resultado = null;
		
		numeroCep = StringUtil.removeNaoNumericos(numeroCep);
		/*
		 * Início - PJEII-18270 - Luis Sergio B. Machado 15/09/2014
		 * se formatação estiver errada ou não for encontrado será lançada exceção
		 */
	    if(StringUtils.length(numeroCep)  != 8){
			throw new NegocioException("Informe o CEP com 8 posições.");
		}
		
		resultado = getCepManager().findByCep(numeroCep);
		if(isNull(resultado)) {
			throw new NegocioException("CEP "+ numeroCep +" não encontrado.");
		}		
		
		return resultado;
	}

	/**
	 * Retorna um novo transformador de objetos que é usado pela conversão de
	 * coleção.
	 * 
	 * @return novo transformador de objeto.
	 */
	protected Transformer novoTransformador(final Pessoa pessoa) {
		return new Transformer() {
			@Override
			public Endereco transform(Object input) {
				return converter((br.jus.cnj.intercomunicacao.v222.beans.Endereco) input, pessoa);
			}
		};
	}
	
	/**
	 * @return CepManager.
	 */
	protected CepManager getCepManager() {
		return ComponentUtil.getComponent(CepManager.class);
	}
}
