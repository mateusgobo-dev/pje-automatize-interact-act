package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import javax.persistence.TypedQuery;

import org.apache.commons.lang.ArrayUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.jus.pje.nucleo.entidades.LogIntegracao;

/**
 * Classe com as consultas a entidade de LogIntegracao.
 * 
 * @author Adriano Pamplona
 */
@Name(LogIntegracaoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class LogIntegracaoDAO extends GenericDAO implements Serializable {

	public static final String NAME = "logIntegracaoDAO";

	/**
	 * Consulta os Log's pelo prefixo das URL's passadas por parâmetro.
	 * 
	 * @param urls Prefixo das URL's.
	 * @return Lista de LogIntegracao.
	 */
	public List<LogIntegracao> consultarURL(String... urls) {
		List<LogIntegracao> resultado = new ArrayList<>();

		if (ArrayUtils.isNotEmpty(urls)) {
			StringJoiner sql = new StringJoiner(" or ", "SELECT o FROM LogIntegracao o WHERE ", "");

			for (int i = 0; i < urls.length; i++) {
				sql.add("requestUrl LIKE :url" + i);
			}

			TypedQuery<LogIntegracao> query = getEntityManager().createQuery(sql.toString(), LogIntegracao.class);

			for (int i = 0; i < urls.length; i++) {
				query.setParameter("url" + i, urls[i] + "%");
			}

			resultado.addAll(query.getResultList());
		}

		return resultado;
	}
}
