package br.com.itx.util;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Init;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;


/**
 * Varre os componentes Seam e busca métodos anotados com {@link Cached}.
 * Ao encontrá-los cria fábricas Seam para eles.
 * 
 * @author David Vieira
 *
 */
@Name("createCachedFactories")
@Scope(ScopeType.APPLICATION)
public class CreateCachedFactories {

	public static final String CACHED_PREFIX = "cached.";

	@Observer(value = { "org.jboss.seam.postInitialization" }, create = true)
	public void createCachedFactorys() {
		Context context = Contexts.getApplicationContext();
		// Percorre os componentes Seam
		try {
			for (String name : context.getNames()) {
				Object object = context.get(name);
				if (object instanceof org.jboss.seam.Component) {
					Component component = (Component) object;
					initMembers(component, component.getBeanClass(), context);
				}
			}
		} catch (Exception e) {
			// swallow
		}
	}

	private void initMembers(Component component, Class<?> clazz, Context applicationContext) {
		Map<Method, Annotation> selectionSetters = new HashMap<Method, Annotation>();
		Set<String> dataModelNames = new HashSet<String>();

		for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
			// Para cada método do componente Seam, procurar pela annotation @Cached
			for (Method method : clazz.getDeclaredMethods()) {
				scanMethod(component, applicationContext, selectionSetters,
						dataModelNames, method);
			}

		}
	}

	private void scanMethod(Component component, Context applicationContext,
			Map<Method, Annotation> selectionSetters,
			Set<String> dataModelNames, Method method) {
		// Se encontrar a annotation @Cached, criar fábrica
		if (method.isAnnotationPresent(Cached.class)) {
			Init init = Init.instance();
			String contextVariable = CACHED_PREFIX + component.getName()   // "cached.componente"
					+ "." + method.getName().substring(3, 4).toLowerCase() // ".p"
					+ method.getName().substring(4);                       // "ropriedade"
			String methodExpression = "#{" + component.getName() + "." + method.getName() + "()}"; // #{componente.getPropriedade()}

			init.addFactoryMethodExpression(contextVariable, methodExpression, method.getAnnotation(Cached.class).scope());
			// cached.componente.propriedade -> #{componente.getPropriedade()}
			LogProvider logProvider = Logging.getLogProvider(CreateCachedFactories.class);
			logProvider.info("Criado fábrica '" + contextVariable + "' para o método '" + methodExpression + "'");
		}
	}
}