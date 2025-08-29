/**
 * ParametroUtil.java
 * 
 * Data de criação: 29/08/2014
 */
package br.jus.cnj.pje.intercomunicacao.v222.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import br.com.infox.cliente.util.JSONUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.cnj.intercomunicacao.v222.beans.CabecalhoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.Parametro;

/**
 * Classe utilitária para tratar o objeto Parametro do MNI.
 * 
 * @author Adriano Pamplona
 */
public final class MNIParametroUtil {

	/**
	 * Retorna true se o parâmetro informado existe.
	 * 
	 * @param manifestacao
	 *            ManifestacaoProcessual.
	 * @param nome
	 *            Nome do parâmetro que será localizado.
	 * @return booleano
	 */
	public static Boolean hasParametro(ManifestacaoProcessual manifestacao, String nome) {
		
		return manifestacao != null && hasParametro(manifestacao.getParametros(), nome);
	}

	/**
	 * Retorna true se o parâmetro informado existe.
	 * 
	 * @param parametros
	 *            Lista de parâmetros.
	 * @param nome
	 *            Nome do parâmetro que será localizado.
	 * @return booleano
	 */
	public static Boolean hasParametro(Collection<Parametro> parametros, String nome) {
		return (obter(parametros, nome) != null);
	}
	
	/**
	 * Retorna true se o valor para o parâmetro informado existe.
	 * 
	 * @param manifestacao
	 *            ManifestacaoProcessual.
	 * @param nome
	 *            Nome do parâmetro que será localizado.
	 * @return booleano
	 */
	public static Boolean hasValor(ManifestacaoProcessual manifestacao, String nome) {
		String valor = obterValor(manifestacao, nome);
		return StringUtils.isNotBlank(valor);
	}

	/**
	 * Retorna o parâmetro informado ou nulo se o mesmo não existir.
	 * 
	 * @param parametros
	 *            Lista de parâmetros.
	 * @param nome
	 *            Nome do parâmetro que será localizado.
	 * @return parâmetro
	 */
	public static Parametro obter(Collection<Parametro> parametros, String nome) {
		Parametro resultado = null;

		if (ProjetoUtil.isNotVazio(parametros) && StringUtils.isNotBlank(nome)) {
			resultado = (Parametro) CollectionUtils.find(parametros,
					novoFiltroParametroPeloNome(nome));
		}
		return resultado;
	}

	/**
	 * Retorna o parâmetro informado ou nulo se o mesmo não existir.
	 * 
	 * @param parametro
	 *            Lista de parâmetros.
	 * @param nome
	 *            Nome do parâmetro que será localizado.
	 * @return parâmetro
	 */
	public static Parametro obter(ManifestacaoProcessual manifestacao, String nome) {
		return (manifestacao != null ? obter(manifestacao.getParametros(), nome) : null);
	}
	
	/**
	 * Retorna o valor parâmetro informado ou nulo se o mesmo não existir.
	 * 
	 * @param manifestacaoProcessual
	 *            Objeto da onde será obtido a lista de parâmetros.
	 * @param nome
	 *            Nome do parâmetro que será localizado.
	 * @return valor do parâmetro.
	 */
	public static String obterValor(ManifestacaoProcessual manifestacaoProcessual, String nome) {
		return obterValor(manifestacaoProcessual.getParametros(), nome);
	}

	/**
	 * Retorna o valor parâmetro informado ou nulo se o mesmo não existir.
	 * 
	 * @param documentoProcessual
	 *            Objeto da onde será obtido a lista de parâmetros.
	 * @param nome
	 *            Nome do parâmetro que será localizado.
	 * @return valor do parâmetro.
	 */
	public static String obterValor(DocumentoProcessual documentoProcessual, String nome) {
		return obterValor(documentoProcessual.getOutroParametro(), nome);
	}

	/**
	 * Retorna o valor parâmetro informado ou nulo se o mesmo não existir.
	 * 
	 * @param parametros
	 *            Lista de parâmetros.
	 * @param nome
	 *            Nome do parâmetro que será localizado.
	 * @return valor do parâmetro.
	 */
	public static String obterValor(Collection<Parametro> parametros, String nome) {
		String resultado = null;

		Parametro parametro = obter(parametros, nome);
		if (parametro != null) {
			resultado = parametro.getValor();
		}
		return (StringUtils.isNotBlank(resultado) ? resultado : null);
	}
	
	/**
	 * Adiciona um parâmetro no CabecalhoProcessual
	 * 
	 * @param manifestacaoProcessual
	 *            ManifestacaoProcessual.
	 * @param nome
	 *            Nome do parâmetro que será adicionado.
	 * @param valor 
	 * 			  Valor do parâmetro.
	 */
	public static void adicionar(ManifestacaoProcessual manifestacaoProcessual, String nome, String valor) {
		
		if (manifestacaoProcessual != null && StringUtils.isNotBlank(nome)) {
			
			Parametro parametro = novoParametro(nome, valor);
			manifestacaoProcessual.getParametros().add(parametro);
		}
	}
	
	/**
	 * Adiciona um parâmetro no CabecalhoProcessual
	 * 
	 * @param cabecalhoProcessual
	 *            CabecalhoProcessual.
	 * @param nome
	 *            Nome do parâmetro que será adicionado.
	 * @param valor 
	 * 			  Valor do parâmetro.
	 */
	public static void adicionar(CabecalhoProcessual cabecalhoProcessual, String nome, String valor) {
		
		if (cabecalhoProcessual != null && StringUtils.isNotBlank(nome)) {
			
			Parametro parametro = novoParametro(nome, valor);
			cabecalhoProcessual.getOutroParametro().add(parametro);
		}
	}

	/**
	 * Converte uma lista de parâmetros para Map.
	 * 
	 * @param parametros
	 * @return Map
	 */
	public static Map<String, Object> converterParaMap(List<Parametro> parametros) {
		Map<String, Object> resultado = new HashMap<>();
		
		if (ProjetoUtil.isNotVazio(parametros)) {
			for (Parametro parametro : parametros) {
				
				resultado.put(parametro.getNome(), parametro.getValor());
			}
		}
		return resultado;
	}
	
	/**
	 * Converte uma string json para lista de Parametro.
	 * 
	 * @param json
	 * @return List.
	 */
	@SuppressWarnings("unchecked")
	public static List<Parametro> converterParaParametro(String json) {
		List<Parametro> resultado = new ArrayList<>();
		
		if (StringUtils.isNotBlank(json)) {
			resultado = (List<Parametro>) JSONUtil.converterStringParaObjeto(json, Parametro.class);
		}
		return resultado;
	}
	
	/**
	 * Converte um objeto JSONObject para lista de Parametro.
	 * 
	 * @param json
	 * @return List.
	 */
	@SuppressWarnings("unchecked")
	public static List<Parametro> converterParaParametro(JSONObject json) {
		List<Parametro> resultado = new ArrayList<>();
		
		if (json != null) {
			json.keySet().forEach(key -> {
				Parametro parametro = new Parametro();
				parametro.setNome(key.toString());
				parametro.setValor(String.valueOf(json.get(key.toString())));
				
				resultado.add(parametro);
			});
		}
		return resultado;
	}
	
	/**
	 * Converte um Map em lista de Parametro.
	 * 
	 * @param mapa
	 * @return List.
	 */
	public static List<Parametro> converterParaParametro(Map<String, Object> mapa) {
		List<Parametro> resultado = new ArrayList<>();
		
		if (mapa != null) {
			mapa.keySet().forEach(key -> {
				Parametro parametro = new Parametro();
				parametro.setNome(key);
				parametro.setValor(String.valueOf(mapa.get(key)));
				
				resultado.add(parametro);
			});
		}
		return resultado;
	}
	
	/**
	 * Novo Parametro.
	 * 
	 * @param nome
	 * @param valor
	 * @return Parametro.
	 */
	private static Parametro novoParametro(String nome, String valor) {
		Parametro resultado = null;
		
		if (StringUtils.isNotBlank(nome)) {
			resultado = new Parametro();
			resultado.setNome(nome);
			resultado.setValor(valor);
		}
			
		return resultado;
	}

	/**
	 * Retorna o filtro de busca de parâmetro pelo nome.
	 * 
	 * @param nome
	 *            Nome do parâmetro.
	 * @return filtro
	 */
	private static Predicate novoFiltroParametroPeloNome(final String nome) {
		return new Predicate() {

			@Override
			public boolean evaluate(Object objeto) {
				Parametro parametro = (Parametro) objeto;
				return StringUtils.equals((parametro != null ? parametro.getNome() : ""), nome);
			}
		};
	}
}
