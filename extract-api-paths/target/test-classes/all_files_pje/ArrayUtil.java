/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.util;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.lang3.math.NumberUtils;

public final class ArrayUtil {

	private static final String DEFAULT_SPLITTER = ",";

	private ArrayUtil() {
	}

	public static <E> E[] copyOf(E[] array) {
		if (array != null) {
			return Arrays.copyOf(array, array.length);
		}
		return null;
	}

	public static byte[] copyOf(byte[] array) {
		if (array != null) {
			return Arrays.copyOf(array, array.length);
		}
		return null;
	}

	/**
	 * Faz um split na string com o separador passado por parâmetro e recupera o valor do índice 
	 * do array criado.
	 * @param string String que será aplicado o split.
	 * @param separador Separador.
	 * @param indice Índice do array criado que será recuperado.
	 * @return String.
	 */
	public static String get(String string, String separador, int indice) {
		
		if (StringUtil.isNotEmpty(string) && StringUtil.isNotEmpty(separador)) {
			String[] array = string.split(separador);
			
			if (indice < array.length) {
				string = array[indice];
			} else {
				string = null;
			}
		}
		return string;
	}
	
	/**
     * Retorna o primeiro valor não nulo.
     *
     * <pre>
     * ArrayUtil.firstNonNull(null, null)      = null
     * ArrayUtil.firstNonNull(null, "")        = ""
     * ArrayUtil.firstNonNull(null, null, "")  = ""
     * ArrayUtil.firstNonNull(null, "zz")      = "zz"
     * ArrayUtil.firstNonNull("abc", *)        = "abc"
     * ArrayUtil.firstNonNull(null, "xyz", *)  = "xyz"
     * ArrayUtil.firstNonNull(Boolean.TRUE, *) = Boolean.TRUE
     * ArrayUtil.firstNonNull()                = null
     * </pre>
     *
     * @param <T> Tipo do objeto retornado.
     * @param valores Objetos que serão validados.
     * @return primeiro objeto não nulo.
     * @see ObjectUtils.firstNonNull(T... values)
     */
    public static <T> T firstNonNull(T... values) {
        if (values != null) {
            for (T val : values) {
                if 	(val != null) {
                    return val;
                }
            }
        }
        return null;
    }
    
    /**
     * Metodo responsavel por gerar um array de inteiros com o valor -1.
     * utilizado em algumas criterias para evitar nullpointerException em filtros sem resultados.
     * @return Integer[] com inteiro valor = -1
    */
    public static Integer[] getArrayInteiroNegativo() {
    	Integer[] retorno = new Integer[1];
    	retorno[0] = -1;
    	return retorno;
    }
    
    /**
	 * Faz um split na string com o separador passado por parâmetro e recupera o array.
	 * 
	 * @param string String que será aplicado o split.
	 * @param separador Separador.
	 * @return String[].
	 */
	public static String[] split(String string, String separador) {
		String[] resultado = null;
		
		if (StringUtil.isNotEmpty(string) && StringUtil.isNotEmpty(separador)) {
			resultado = string.split(separador);
		}
		return resultado;
	}
	
    /**
     * Retorna o objeto do índice do array ou null se o índice não existir.
     * 
     * @param array Array de objetos
     * @param index Índice do objeto que será retornado.
     * @return Objeto do array
     */
    public static <T> T get(T[] array, int index) {
    	return (array != null && array.length > index ? array[index] : null);
    }
    
    /**
	 * Retorna o objeto do índice do array ou null se o índice não existir.
     * 
     * @param array Array de objetos
     * @param index Índice do objeto que será retornado.
     * @return Long do array
	 */
	public static Long getLong(String[] array, int indice) {
		
		String valor = get(array, indice);
		return (NumberUtils.isCreatable(valor) ? NumberUtils.createLong(valor) : null);
	}
    
    /**
	 * Retorna o objeto do índice do array ou null se o índice não existir.
     * 
     * @param array Array de objetos
     * @param index Índice do objeto que será retornado.
     * @return Long do array
	 */
	public static Integer getInteger(String[] array, int indice) {
		
		String valor = get(array, indice);
		return (NumberUtils.isCreatable(valor) ? NumberUtils.createInteger(valor) : null);
	}

    /**
     * Método responsável por receber uma lista de strings e adicionar o caractere % 
     * no ínicio e no fim de cada item da lista
     * @param stringsSemCaractereDePercentoNoInicioENoFim
     * @return
     */
    public static List<String> concatenarCaracterePercentualNoComecoENoFimDeCadaItem(List<String> stringsSemCaractereDePercentoNoInicioENoFim) {
		List<String> tagsWithPercent = new ArrayList<>();
		for (String tag : stringsSemCaractereDePercentoNoInicioENoFim) {
			tagsWithPercent.add("%" + tag + "%");
		}
		return tagsWithPercent;
    }

    /**
	 * Transforma uma string separadas por vírgulas em um Set de Integers.
	 * @param value: String de números separados por vírgulas. Exemplo: "1,2,3".
	 * @return Set de Inteiros se conteúdo válido. Null se string nula ou vazia.
	 * @exception NumberFormatException quando há elementos não numéricos
	 */
	public static Set<Integer> getAsListOfIntegers(String value) {
		return getAsListOfIntegers(value, DEFAULT_SPLITTER);
	}
	
	/**
	 * Transforma uma string separadas por vírgulas em um Set de Longs.
	 * @param value: String de números separados por vírgulas. Exemplo: "1,2,3".
	 * @return Set de Inteiros se conteúdo válido. Null se string nula ou vazia.
	 * @exception NumberFormatException quando há elementos não numéricos
	 */
	public static Set<Long> getAsListOfLongs(String value) {
		return getAsListOfLongs(value, DEFAULT_SPLITTER);
	}

	/**
	 * Transforma uma string separadas por vírgulas em um Set de Integers.
	 * @param value: String de números separados por um splitter: Exemplo: "1|2|3".
	 * @param splitter: Separador específico.
	 * @return Set de Inteiros se conteúdo válido. Null se string nula ou vazia.
	 * @exception NumberFormatException quando há elementos não numéricos
	 */
    public static Set<Integer> getAsListOfIntegers(String value, String splitter) {
		Set<Integer> list = null;
		if (StringUtil.isNotEmpty(value) && StringUtil.isNotEmpty(splitter)) {
			list = new HashSet<>();

			if (splitter.equals("|")) {
				splitter = DEFAULT_SPLITTER;
				value = value.replace("|", splitter);
			}

			Set<String> tempList = new HashSet<>(Arrays.asList(value.trim().split(splitter)));
			for (String item: tempList) {
				item = item.trim();
				if (!item.equals("")) {
					list.add(Integer.valueOf(item));
				}
			}
		}
		return list;
	}
    
    /**
	 * Transforma uma string separadas por vírgulas em um Set de Integers.
	 * @param value: String de números separados por um splitter: Exemplo: "1|2|3".
	 * @param splitter: Separador específico.
	 * @return Set de Inteiros se conteúdo válido. Null se string nula ou vazia.
	 * @exception NumberFormatException quando há elementos não numéricos
	 */
    public static Set<Long> getAsListOfLongs(String value, String splitter) {
		Set<Long> list = null;
		if (StringUtil.isNotEmpty(value) && StringUtil.isNotEmpty(splitter)) {
			list = new HashSet<>();

			if (splitter.equals("|")) {
				splitter = DEFAULT_SPLITTER;
				value = value.replace("|", splitter);
			}

			Set<String> tempList = new HashSet<>(Arrays.asList(value.trim().split(splitter)));
			for (String item: tempList) {
				item = item.trim();
				if (!item.equals("")) {
					list.add(Long.valueOf(item));
				}
			}
		}
		return list;
	}
    
	public static boolean isListEmpty(List<?> lista) {
		return lista == null || lista.isEmpty();
	}

	public static boolean isListNotEmpty(List<?> lista) {
		return !isListEmpty(lista);
	}	
	
	public static boolean isSetEmpty(Set<?> set) {
		return set == null || set.isEmpty();
	}

	public static boolean isSetNotEmpty(Set<?> set) {
		return !isSetEmpty(set);
	}
}