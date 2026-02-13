package br.jus.csjt.pje.commons.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Class que retorna uma String, em formato "['A', 'B']" (array de String em javascript).
 * 
 * Cada elemento do array representa uma possível chamada de método, ex: 'metodoA.metodoB(String)',
 * a raiz da árvore de chamadas é o componente Seam informado em getMethodsStringArrayForClass(CLASSE)
 * 
 * Classe usada para prover content assist em campos que requerem o preenchimento de ELs no browser.
 * 
 * @author David Vieira
 *
 */
@Name("ELResolverHelper")
@Scope(ScopeType.STATELESS)
public class ELResolverHelper {
	
	public static final String blackList = "toString,equals,getClass,hashCode,notify,notifyAll,wait";
	
	private static boolean inBlackList(Method method) {
		return blackList.contains(method.getName()) || method.getName().contains("$");
	}
	
	public String getMethodsStringArrayForClass(Object objeto) {
		return getMethodsStringArrayForClass(objeto.getClass());
	}
	
	public String getMethodsStringArrayForClass(Class<?> classeRaiz) {
		Set<String> retorno = new TreeSet<String>(); // TreeSet mantém chamadas em ordem alfabética
		boolean ehUmComponenteSeam = classeRaiz.isAnnotationPresent(Name.class);
		if (!ehUmComponenteSeam && classeRaiz.getSuperclass().isAnnotationPresent(Name.class)) {
			classeRaiz = classeRaiz.getSuperclass();
			ehUmComponenteSeam = true;
		}
		if (ehUmComponenteSeam) {
			String nomeDoComponente = classeRaiz.getAnnotation(Name.class).value();
			// nó raiz
			retorno.add("#{" + nomeDoComponente);
			Method[] declaredMethods = classeRaiz.getDeclaredMethods();
			for (Method method : declaredMethods) {
				if (inBlackList(method)) {
					continue;
				}
				if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
					String parametros = getParametros(method);
					retorno.add("#{" + nomeDoComponente + "." + method.getName() + "(" + parametros + ")");
				}
				List<Method> pilhaDeMetodos = new ArrayList<Method>();
				// Passando retorno para ser preenchido pelo método -> evitar criar árvores em recursão.
				getCadeiaDeMetodos(method, pilhaDeMetodos, retorno);
			}
		}
		
		return formatarArrayStringJavascript(retorno);
	}
	
	private static void getCadeiaDeMetodos(Method methodPercorrendo, List<Method> pilhaDeMetodos, Set<String> retorno) {
		Class<?> classe = methodPercorrendo.getReturnType();
		if (pilhaDeMetodos.contains(methodPercorrendo) || classe.equals(void.class)) {
			return;
		}
		pilhaDeMetodos.add(methodPercorrendo);
		
		Method[] declaredMethods = classe.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (inBlackList(method)) {
				continue;
			}
			if (Modifier.isPublic(method.getModifiers())) {
				String parametros = getParametros(method);
				retorno.add(methodPercorrendo.getName() + "." + method.getName() + "(" + parametros + ")");
			}
			getCadeiaDeMetodos(method, pilhaDeMetodos, retorno);
		}
	}

	private static String getParametros(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		StringBuilder parametros = new StringBuilder();
		String retorno = "";
		if (parameterTypes.length > 0) {
			for (Class<?> paramClass : parameterTypes) {
				parametros.append(paramClass.getSimpleName());
				parametros.append(",");
			}
			retorno = parametros.substring(0, parametros.length() - 1);
		}
		return retorno;
	}
	
	private static String formatarArrayStringJavascript(Set<String> retorno) {
		StringBuilder retornoJavascript = new StringBuilder("[");
		
		for (String string : retorno) {
			retornoJavascript.append("'");
			retornoJavascript.append(string);
			retornoJavascript.append("',");
		}
		
		retornoJavascript.delete(retornoJavascript.length() - 1, retornoJavascript.length());
		retornoJavascript.append("]");
		
		return retornoJavascript.toString();
	}

}
