package br.jus.cnj.pje.util;

import java.lang.reflect.Array;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanPredicate;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.collections.functors.EqualPredicate;
import org.apache.commons.collections.iterators.ReverseListIterator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.nucleo.PJeRuntimeException;

/**
 * Classe utilitária para disponibilizar no contexto os métodos da classe
 * {@link CollectionUtils}
 * 
 */
@Scope(ScopeType.EVENT)
@Name("collectionUtilsPje")
public class CollectionUtilsPje extends CollectionUtils {

	/** logger */
	private static Log log = LogFactory.getLog(CollectionUtilsPje.class);

	/**
	 * Returns the Equal Predicate based on beanPropertyName and value
	 * 
	 * @param beanPropertyName
	 *            ( Property of ValueObject which not null (or) empty)
	 * @param value
	 * @return
	 */
	private static BeanPredicate getEqualPredicate(String beanPropertyName,	Object value) {
		BeanPredicate beanPredicate = null;
		if (value != null) {
			EqualPredicate nameEqlPredicate = new EqualPredicate(value);
			beanPredicate = new BeanPredicate(beanPropertyName, nameEqlPredicate);
		} else {
			beanPredicate = new BeanPredicate(beanPropertyName, PredicateUtils.nullPredicate());
		}
		return beanPredicate;
	}

	/**
	 * Converts the valueObject to Map whose key is propertyName and value as
	 * corresponding value for PropertyName
	 * 
	 * @param beanObject
	 * @return
	 */
	public static Map<String, Object> beanToMap(Object beanObject) {
		return new BeanMap(beanObject);
	}
	
	/**
	 * Converte uma String no formato
	 * 'variavel1=valor1,variavel=valor2;variavel3=valor3' para um mapa de
	 * <String,String> ou <String,Long>, quando o valor for possível ser
	 * convertido num valor númerico
	 * 
	 * @param variaveis
	 * 		Uma string no formato 'variavel1:valor1,variavel:valor2,variavel3:valor3'
	 * 
	 * @return
	 */
	public static Map<String, Object> stringToMap(String variaveis) {

		Map<String, Object> mapaVariaveis = new HashMap<String, Object>();
		 
		String[] pairs = variaveis.split(",");
		for (int i = 0; i < pairs.length; i++) {
			String pair = pairs[i];
			String[] keyValue = pair.split("=");
			
			String nomeVariavel = StringUtils.trim(keyValue[0]);
			Object valorVariavel = null;
			keyValue[1] = StringUtils.trim(keyValue[1]);
			try {
				valorVariavel = Long.valueOf(keyValue[1]);
			} catch (NumberFormatException e) {
				valorVariavel = keyValue[1]; 
			}			
			mapaVariaveis.put(nomeVariavel, valorVariavel);
		}
		
		return mapaVariaveis;
	}
	
	/**
	 * Operacao que extrai os tokens de uma origem que contenha numeros no formato: "1, 2, 3, 4"
	 * @param origem
	 * @return
	 */
	public static List<Integer> convertStringToIntegerList(String origem){
		List<Integer> retorno = new ArrayList<Integer>();
		if (origem != null && !origem.isEmpty()) {
			StringTokenizer tokens = new StringTokenizer(origem, ",");
			while (tokens.hasMoreTokens()) {
				retorno.add(Integer.valueOf(tokens.nextToken().trim()));
			}
		}
		return retorno;
	}

	
	/**
	 * Operacao que extrai os tokens de uma origem que contenha numeros no formato: "1, 2, 3, 4"
	 * @param origem
	 * @return
	 */
	public static List<Long> convertStringToLongList(String origem){
		List<Long> retorno = new ArrayList<Long>();
		if (origem != null && !origem.isEmpty()) {
			StringTokenizer tokens = new StringTokenizer(origem, ",");
			while (tokens.hasMoreTokens()) {
				retorno.add(Long.valueOf(tokens.nextToken().trim()));
			}
		}
		return retorno;
	}

	public static List<Long> convertListIntegerToListLong(List<Integer> listInteger){
		List<Long> listLong = new ArrayList<>();
		if(isNotEmpty(listInteger)) {
			for (Integer numInt : listInteger) {
				listLong.add(numInt.longValue());
			}
		}
		
		return listLong;
	}
	
	public static List<Integer> convertListStringToListInteger(List<String> listString){
		List<Integer> listInteger = new ArrayList<>();
		if(isNotEmpty(listString)) {
			for (String numString : listString) {
				listInteger.add(Integer.valueOf(numString));
			}
		}
		
		return listInteger;
	}	

	/**
	 * Converts the List of Maps based on beanPropertyName <br>
	 * For example if passed List of User's and intials as beanPropertyName it
	 * returns the map whose key is intial and corresponding value is
	 * ValueObject <br>
	 * <code>listToMap(collection,"intials");</code>
	 * 
	 * @param <T>
	 * @param collection
	 *            - Collection of ValueObject
	 * @param beanPropertyName
	 *            - property of valueObject
	 * @return
	 * @throws ImproperUsageException
	 */
	public static <T> Map<String, T> listToMap(Collection<T> inputCollection,
			String beanPropertyName) {
		Map<String, T> map = new HashMap<String, T>(inputCollection.size());
		for (T t : inputCollection) {
			try {
				map.put(BeanUtils.getSimpleProperty(t, beanPropertyName), t);
			} catch (Exception e) {
				log.error("Exception in CollectionsUtilExt.listToMap", e);
				throw new PJeRuntimeException(
						"Exception in CollectionsUtilExt.listToMap", e);
			}
		}
		return map;
	}

	/**
	 * Returns the ValueObject whose beanPropertyName is least <br>
	 * For Ex : <code>min(list,"salary")</code> ==> Returns the valueObject
	 * whose salary is least <br>
	 * If more than one valueObject having least salary then <code>min()</code>
	 * will return first matching valueObject from colelction passed @
	 * 
	 * @param <T>
	 * @param collection
	 *            - List of ValueObjects
	 * @param beanPropertyName
	 * @param nullsAreHigh
	 *            a <code>true</code> value indicates that <code>null</code>
	 *            should be compared as higher than a non-<code>null</code>
	 *            object. A <code>false</code> value indicates that
	 *            <code>null</code> should be compared as lower than a non-
	 *            <code>null</code> object.
	 * @return
	 */
	public static <T> T min(Collection<T> inputCollection,
			String beanPropertyName, boolean nullsAreHigh) {
		Comparator<T> nullComparator = new NullComparator(nullsAreHigh);
		Comparator<T> beanComparator = new BeanComparator(beanPropertyName,
				nullComparator);
		T selectedObject = Collections.min(inputCollection, beanComparator);
		return selectedObject;
	}

	/**
	 * Returns the ValueObject whose beanPropertyName is greatest For Ex :
	 * max(list,"salary") ==> Returns the valueObject whose salary is greater
	 * 
	 * @param <T>
	 * @param collection
	 * @param beanPropertyName
	 * @param nullsAreHigh
	 *            a <code>true</code> value indicates that <code>null</code>
	 *            should be compared as higher than a non-<code>null</code>
	 *            object. A <code>false</code> value indicates that
	 *            <code>null</code> should be compared as lower than a non-
	 *            <code>null</code> object.
	 * @return
	 */
	public static <T> T max(Collection<T> inputCollection,
			String beanPropertyName, boolean nullsAreHigh) {
		Comparator<T> nullComparator = new NullComparator(nullsAreHigh);
		Comparator<T> beanComparator = new BeanComparator(beanPropertyName,
				nullComparator);
		T selectedObject = Collections.max(inputCollection, beanComparator);
		return selectedObject;
	}

	/**
	 * Sorts the List of ValueObjects based on beanPropertyName array
	 * 
	 * @param <T>
	 * @param list
	 * @param beanPropertyName
	 * @param nullsAreHigh
	 *            a <code>true</code> value indicates that <code>null</code>
	 *            should be compared as higher than a non-<code>null</code>
	 *            object. A <code>false</code> value indicates that
	 *            <code>null</code> should be compared as lower than a non-
	 *            <code>null</code> object.
	 * @param isAscending
	 *            - sorts in Ascending if true , else desending
	 */
	public static <T>List<T> sortCollection(List<T> list, boolean isAscending,
			String... beanPropertyName) {
		/*
		 * NullComparator should be passed as isAscending ,Since null values
		 * needs to handled in dif way
		 */
		Comparator<T> nullComparator = new NullComparator(isAscending);
		Collection<Comparator<T>> beanComparatorCollection = new ArrayList<Comparator<T>>(
				beanPropertyName.length);
		for (int i = 0; i < beanPropertyName.length; i++) {
			beanComparatorCollection.add(new BeanComparator(
					beanPropertyName[i], nullComparator));
		}
		Comparator<T> finalComparator = ComparatorUtils
				.chainedComparator(beanComparatorCollection);
		if (!isAscending) {
			finalComparator = new ReverseComparator(finalComparator);
		}
		Collections.sort(list, finalComparator);
		
		return list;
	}

	/**
	 * Returns the Value Object from List based on beanPropertyName and
	 * beanPropertyValue For Example
	 * selectObjectFromCollection(list,"firstName","yahoo") ==> Returns Value
	 * Object whose firstname is Yahoo If more than one valueObject exist for
	 * given criteria , it picks first matching one
	 * 
	 * @param <T>
	 * @param list
	 * @param beanPropertyName
	 * @param beanPropertyValue
	 * @return
	 */
	public static <T> T selectFilteredObject(Collection<T> inputCollection,
			String beanPropertyName, Object beanPropertyValue) {
		BeanPredicate beanPredicate = getEqualPredicate(beanPropertyName, beanPropertyValue);
		T selectedObject = (T) find(inputCollection, beanPredicate);
		return selectedObject;
	}

	/**
	 * Returns the list of valueObjects whose valueObjects property having value
	 * value Example if beanPropertyName is "intials" and value is "GAGO" it
	 * returns the list of ValueObjects whose intials are "GAGO"
	 * 
	 * @param <T>
	 * @param list
	 * @param beanPropertyName
	 *            - Property Name of Value Object
	 * @param beanPropertyValue
	 *            - Value corresponding to beanPropertyName
	 * @return
	 */
	public static <T> Collection<T> selectFilteredCollection(
			Collection<T> inputCollection, String beanPropertyName,
			Object beanPropertyValue) {
		BeanPredicate beanPredicate = getEqualPredicate(beanPropertyName, beanPropertyValue);
		Collection<T> selectedList = select(inputCollection, beanPredicate);
		return selectedList;
	}

	/**
	 * Convient method which takes custom Predicate
	 * 
	 * @param <T>
	 * @param list
	 * @param customPredicate
	 * @return
	 */
	public static <T> Collection<T> selectFilteredCollection(
			Collection<T> inputCollection, Predicate customPredicate) {
		Collection<T> selectedList = select(inputCollection, customPredicate);
		return selectedList;
	}

	/**
	 * Select's from the collection whose beanPropertyName and beanPropertyValue
	 * For Ex we can select User whose firstname = 'smith' anf lastname='john'
	 * we pass firstname and lastname in beanPropertyName[] we pass smith and
	 * john in beanPropertyNamevalue[]
	 * 
	 * @param <T>
	 * @param list
	 * @param beanPropertyName
	 *            - String array of bean properties names
	 * @param beanPropertyValue
	 *            - Object array of values corresponding to bean properties in
	 *            beanPropertyName
	 * @return
	 */
	public static <T> Collection<T> selectFilteredCollection(
			Collection<T> inputCollection, String[] beanPropertyName,
			Object[] beanPropertyValue) {
		Collection<T> selectedList = inputCollection;
		if (!ArrayUtils.isEmpty(beanPropertyName)
				&& !ArrayUtils.isEmpty(beanPropertyValue)) {
			Predicate[] predicateArray = new Predicate[beanPropertyName.length];
			for (int i = 0; i < beanPropertyName.length; i++) {
				predicateArray[i] = getEqualPredicate(beanPropertyName[i], beanPropertyValue[i]);
			}
			selectedList = select(inputCollection, PredicateUtils.allPredicate(predicateArray));
		}
		return selectedList;
	}
	
	
	public static <T> List<T> toList(T... elementos) {
		return new ArrayList<>(Arrays.asList(elementos));			
	}

	/**
	 * Returns the list of valueObjects whose valueObjects property having value
	 * value Example if beanPropertyName is "intials" and value is "GAGO" it
	 * returns the list of ValueObjects whose intials are <b>NOT</b> "GAGO"
	 * 
	 * @param <T>
	 * @param list
	 * @param beanPropertyName
	 *            - Property Name of Value Object
	 * @param beanPropertyValue
	 *            - Value of
	 * @return
	 */
	public static <T> Collection<T> selectRejectedCollection(
			Collection<T> inputCollection, String beanPropertyName,
			Object beanPropertyValue) {
		BeanPredicate beanPredicate = getEqualPredicate(beanPropertyName, beanPropertyValue);
		Collection<T> selectedList = selectRejected(inputCollection, beanPredicate);
		return selectedList;
	}

	/**
	 * Modifies ValueObject in Collection
	 * 
	 * @param <T>
	 * @param list
	 *            List of ValueObjects
	 * @param beanPropertyName
	 *            PropertyName of ValueObject
	 * @param originalValue
	 *            Original Value
	 * @param valueToBeChanged
	 * @throws ImproperUsageException
	 * @return
	 */
	public static <T> Collection<T> modifyObjectsFromCollection(
			Collection<T> inputCollection, String beanPropertyName,
			Object originalValue, Object valueToBeChanged) {
		BeanPredicate beanPredicate = getEqualPredicate(beanPropertyName, originalValue);
		Collection<T> selectedList = select(inputCollection, beanPredicate);
		try {
			for (T t : selectedList) {
				BeanUtils.setProperty(t, beanPropertyName, valueToBeChanged);
			}
		} catch (Exception e) {
			log.error("Exception in CollectionsUtilExt.modifyCollection ", e);
			throw new PJeRuntimeException(
					"Exception in CollectionsUtilExt.modifyCollection", e);
		}
		return selectedList;
	}

	/**
	 * Counts the number of elements in the input collection that match the
	 * beanPropertyName with value specified in beanPropertyValue.
	 * <p>
	 * A <code>null</code> collection or predicate matches no elements.
	 * 
	 * @param inputCollection
	 *            the collection to get the input from, may be null
	 * @param beanPropertyName
	 *            - String array of bean properties names
	 * @param beanPropertyValue
	 *            - Object array of values corresponding to bean properties in
	 *            beanPropertyName
	 * @return the number of matches for the predicate in the collection
	 */
	public static int countMatches(Collection inputCollection,
			String[] beanPropertyName, String[] beanPropertyValue) {
		int matches = 0;
		if (!ArrayUtils.isEmpty(beanPropertyName)
				&& !ArrayUtils.isEmpty(beanPropertyValue)) {
			Predicate[] predicateArray = new Predicate[beanPropertyName.length];
			for (int i = 0; i < beanPropertyName.length; i++) {
				predicateArray[i] = getEqualPredicate(beanPropertyName[i], beanPropertyValue[i]);
			}
			matches = countMatches(inputCollection, PredicateUtils.allPredicate(predicateArray));
		}
		return matches;
	}

	/**
	 * Debug the collection and appends output to Log with debug level
	 * 
	 * @param <T>
	 * @param inputCollection
	 */
	<T> void debugCollection(Collection<T> inputCollection) {
		if (CollectionUtils.isNotEmpty(inputCollection)) {
			for (T t : inputCollection) {
				log.debug("[ " + ToStringBuilder.reflectionToString(t) + " ]");
			}
		}
	}

	/**
	 * Returns the reverse iterator based on {@link List} passed Order of
	 * elements in orginal collections wont get changed after the calling of
	 * reverseIterator(...)
	 * 
	 * @param <T>
	 * @param inputCollection
	 * @return
	 */
	<T> Iterator<T> reverseIterator(List<T> inputCollection) {
		ReverseListIterator reverseListIterator = new ReverseListIterator(inputCollection);
		return reverseListIterator;
	}
	
    /**
     * Método extraído da superclasse devido a ocorrências
     * de erro a depender da JDK utilizada
     * Null-safe check if the specified collection is empty.
     * <p>
     * Null returns true.
     * 
     * @param coll  the collection to check, may be null
     * @return true if empty or null
     * @since Commons Collections 3.2
     */
    public static boolean isEmpty(Collection coll) {
        return (coll == null || coll.isEmpty());
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Map mapa) {
        return (mapa == null || mapa.isEmpty());
    }
    
    /**
     * Método extraído da superclasse devido a ocorrências
     * de erro a depender da JDK utilizada
     * Null-safe check if the specified collection is not empty.
     * <p>
     * Null returns false.
     * 
     * @param coll  the collection to check, may be null
     * @return true if non-null and non-empty
     * @since Commons Collections 3.2
     */
    public static boolean isNotEmpty(Collection coll) {
    	return !isEmpty(coll);
    }
    
    /**
     * Método responsável por retornar o objeto recebido como um elemento de uma nova lista
     * 
     * @param object
     * @return
     */
    public static List<Object> getArrayFromObject(Object object){
    	List<Object> list = new ArrayList<Object>();
    	if(object != null) {
    		list.add(object);
    	}
    	return list;
    }
    
    /**
     * Método responsável por retornar uma lista vazia 
     * @return Lista vazia
     */
    public List<Object> getNewArrayList() {
    	return new ArrayList<Object>(0);
    }
    
    /**
     * Método responsável por incluir um objeto à lista especificada
     * @param list Lista na qual o objeto especificado será incluído
     * @param object Objeto que será incluído na lista
     * @return Lista de objetos passada como parâmetro
     */
    public List<Object> addElementToList(List<Object> list, Object object) {
    	list.add(object);
    	
    	return list;
    }

	/**
	 * Método responsável por remover um objeto da lista especificada
	 * @param list Lista na qual o objeto especificado será removido
	 * @param object Objeto que será removido da lista
	 * @return Lista de objetos passada como parâmetro
	 */
	public List<Object> removeElementFromList(List<Object> list, Object object) {
		list.remove(object);
		return list;
	}

	/**
	 * Método responsável por remover um item da lista especificada pelo índice
	 * @param list Lista na qual será removida o item
	 * @param index Índice a ser removido
	 * @return Lista de objetos passada como parâmetro
	 */
	public List<Object> removeElementFromList(List<Object> list, int index) {
		list.remove(index);
		return list;
	}

	/**
	 * Método responsável por remover objetos de uma lista
	 * @param list Lista na qual será removida os objetos
	 * @param removeList Lista de objetos a ser removidos
	 * @return Lista de objetos passada como parâmetro
	 */
	public List<Object> removeAllFromList(List<Object> list, Collection<?> removeList) {
		list.removeAll(removeList);
		return list;
	}

	/**
	 * Método responsável por converter um List em Array
	 * @param list
	 * @param <T>
	 * @return
	 */
	public static <T> T[] toArray(List<T> list) {
		T[] arr = null;
		if(CollectionUtils.isNotEmpty(list)) {
			arr = (T[]) Array.newInstance(list.get(0).getClass(), list.size());
			for (int i = 0; i < list.size(); ++i) {
				arr[i] = list.get(i);
			}
		}
		return arr;
	}

	/**
	 * Método responsável por converter um T em Array
	 * @param T
	 * @param <T>
	 * @return T[]
	 */
	public static <T> T[] objectToArray(T t) {
		return toArray(Arrays.asList(t));
	}

	public String listToString(Collection<?> c) {
		return c.toString().replaceAll("\\[", "").replaceAll("\\]", "");
	}
	
	public String listToStringWithGlue(Collection<?> c, String glue) {
		return String.join(glue, listToString(c).split(", "));
	}

	public static <T> List<T> ordenarLista(List<T> lista, String beanPropertyName) {
		Collator collator = Collator.getInstance();
		Collections.sort(lista, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				int result = 0;
				try {
					String property1 = BeanUtils.getSimpleProperty(o1, beanPropertyName);
					String property2 = BeanUtils.getSimpleProperty(o2, beanPropertyName);
					result = collator.compare(property1, property2);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return result;
			}
		});
		return lista;
	}

	/**
	 * Remove os objetos null da lista.
	 * 
	 * @param list Lista de objetos.
	 */
	public static <T> List<T> getNewListWithoutNull(List<T> list) {
		List<T> result = new ArrayList<>();
		
		if(CollectionUtils.isNotEmpty(list)) {
			for (T object : list) {
				if (object != null) {
					result.add(object);
				}
			}
		}
		
		return result;
	}
}