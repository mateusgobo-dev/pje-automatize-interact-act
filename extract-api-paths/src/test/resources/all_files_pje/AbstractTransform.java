/**
 * AbstractTransform.java.
 *
 * Data: 16/02/2018
 */
package br.jus.cnj.pje.business.dao.transform;

import java.util.ArrayList;
import java.util.List;

import br.com.infox.cliente.util.ProjetoUtil;

/**
 * Classe abstrata dos transformadores de consulta.
 *
 * @see Transform
 * @author Adriano Pamplona
 */
public abstract class AbstractTransform<T> implements Transform<T> {

	@Override
	public List<T> transformCollection(List<Object[]> lista) {
		List<T> resultado = new ArrayList<T>();
		
		if (ProjetoUtil.isNotVazio(lista)) {
			for (Object[] objeto : lista) {
				T objetoTransformado = transform(objeto);
				if (objetoTransformado != null) {
					resultado.add(objetoTransformado);
				}
			}
		}
		return resultado;
	}
	
	@Override
	public abstract T transform(Object[] objeto);
}
