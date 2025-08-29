/**
 * BeanSerializer.java.
 *
 * Data: 13 de nov de 2017
 */
package br.com.infox.cliente.util;

import java.io.IOException;
import java.lang.reflect.Field;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import br.com.itx.util.ReflectionsUtil;

/**
 * Classe sobrescrita para filtrar os atributos primitivos nulos e assim evitar NullPointerException.
 * 
 * @author Adriano Pamplona
 * @see com.fasterxml.jackson.databind.ser.BeanSerializer
 */
public class BeanSerializer extends com.fasterxml.jackson.databind.ser.BeanSerializer {

	protected BeanSerializer(BeanSerializerBase src) {
		super(src);
	}

	protected void serializeFields(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
		final BeanPropertyWriter[] props;
		if (_filteredProps != null && provider.getActiveView() != null) {
			props = _filteredProps;
		} else {
			props = _props;
		}
		int i = 0;
		try {
			for (final int len = props.length; i < len; ++i) {
				BeanPropertyWriter prop = props[i];
				if (prop != null && isPrimitiveNotNull(prop, bean)) { // can have nulls in filtered list
					prop.serializeAsField(bean, gen, provider);
				}
			}
			if (_anyGetterWriter != null) {
				_anyGetterWriter.getAndSerialize(bean, gen, provider);
			}
		} catch (Exception e) {
			String name = (i == props.length) ? "[anySetter]" : props[i].getName();
			wrapAndThrow(provider, e, bean, name);
		} catch (StackOverflowError e) {
			JsonMappingException mapE = new JsonMappingException(gen, "Infinite recursion (StackOverflowError)", e);

			String name = (i == props.length) ? "[anySetter]" : props[i].getName();
			mapE.prependPath(new JsonMappingException.Reference(bean, name));
			throw mapE;
		}
	}

	/**
	 * Retorna true se o atributo primitivo for nulo.
	 * 
	 * @param prop
	 * @param bean
	 * @return Booleano.
	 */
	private boolean isPrimitiveNotNull(BeanPropertyWriter prop, Object bean) {
		Boolean resultado = Boolean.TRUE;

		if (prop != null && prop.getType().isPrimitive()) {
			try {
				Field field = ReflectionsUtil.getField(bean, prop.getName());
				if (field != null) {
					Object value = field.get(bean);
					resultado = (value != null);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
		}
		return resultado;
	}
}
