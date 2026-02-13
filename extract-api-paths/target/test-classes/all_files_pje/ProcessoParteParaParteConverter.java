/**
 * ProcessoParteParaParteConverter.java
 * 
 * Data de criação: 16/02/2016
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.intercomunicacao.v222.beans.Endereco;
import br.jus.cnj.intercomunicacao.v222.beans.Parte;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Conversor de ProcessoParte para Parte.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name(ProcessoParteParaParteConverter.NAME)
public class ProcessoParteParaParteConverter
		extends
		IntercomunicacaoConverterAbstrato<ProcessoParte, Parte> {

	public static final String NAME = "v222.processoParteParaParteConverter";
	
	@In (value = PessoaParaParteConverter.NAME)
	private PessoaParaParteConverter pessoaParaParteConverter;
	
	@In (value = EnderecoParaIntercomunicacaoEnderecoConverter.NAME)
	private EnderecoParaIntercomunicacaoEnderecoConverter enderecoParaIntercomunicacaoEnderecoConverter; 
	
	/**
	 * Use o método converter(Pessoa, Boolean).
	 * 
	 * @see br.jus.cnj.pje.intercomunicacao.converter.IntercomunicacaoConverterAbstrato#converter(java.lang.Object)
	 */
	@Override
	public Parte converter(ProcessoParte processoParte) {
		Parte resultado = null;
		
		if (isNotNull(processoParte) && 
			isNotNull(processoParte.getPessoa()) && 
			isNotNull(processoParte.getProcessoTrf())) {
			
			Pessoa pessoa = processoParte.getPessoa();
			ProcessoTrf processo = processoParte.getProcessoTrf();
			
			resultado = getPessoaParaParteConverter().converter(pessoa, processo.getJusticaGratuita());
			br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoaResultado = resultado.getPessoa();
			pessoaResultado.getEndereco().clear();
			pessoaResultado.getEndereco().addAll(consultarColecaoEndereco(processoParte));
			
		}
		return resultado;
	}


	/**
	 * @param processoParte
	 * @return coleção de endereços da parte do processo.
	 */
	protected Collection<Endereco> consultarColecaoEndereco(ProcessoParte processoParte) {
		processoParte = EntityUtil.refreshEntity(processoParte);
		List<br.jus.pje.nucleo.entidades.Endereco> enderecos = processoParte.getEnderecos();
		//Código abaixo inserido para evitar listas de endereço com objetos de mesma instância.
		List<br.jus.pje.nucleo.entidades.Endereco> enderecosNaoDuplicados =
			    new ArrayList<br.jus.pje.nucleo.entidades.Endereco>(new LinkedHashSet<br.jus.pje.nucleo.entidades.Endereco>(enderecos));
		return getEnderecoParaIntercomunicacaoEnderecoConverter().converterColecao(enderecosNaoDuplicados);
	}


	/**
	 * @return the pessoaParaParteConverter
	 */
	protected PessoaParaParteConverter getPessoaParaParteConverter() {
		return pessoaParaParteConverter;
	}


	/**
	 * @return the enderecoParaIntercomunicacaoEnderecoConverter
	 */
	protected EnderecoParaIntercomunicacaoEnderecoConverter getEnderecoParaIntercomunicacaoEnderecoConverter() {
		return enderecoParaIntercomunicacaoEnderecoConverter;
	}
}
