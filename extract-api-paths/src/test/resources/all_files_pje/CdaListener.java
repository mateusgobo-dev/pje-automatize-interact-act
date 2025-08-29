/**
 *  pje-web
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.entidades.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.jus.pje.nucleo.entidades.Cda;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe responsável por monitorar eventos JPA relevantes para a recuperação de {@link a}.
 * 
 * A vinculação dessa classe com os eventos do ciclo de vida JPA deve ser feita por
 * meio do arquivo META-INF/orm.xml, que deverá conter a seguinte definição:
 * 
 * <pre>
 * {@code
 * 	<entity class="br.jus.pje.nucleo.entidades.Cda">
 * 		<entity-listeners>
 * 			<entity-listener class="br.jus.cnj.pje.entidades.listeners.CdaListener">
 * 				<post-load method-name="postLoad"/>
 *				<pre-update method-name="preUpdate"/>
 * 			</entity-listener>
 * 		</entity-listeners>
 * 	</entity>
 * }
 * </pre>
 * @author Adriano Pamplona
 */
public class CdaListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CdaListener.class);
	

	/**
	 * Evendo invocado antes de incluir a entidade.
	 * 
	 * @param cda
	 */
	public void prePersist(Cda cda) {
		cda.setNumero(StringUtil.removeNaoNumericos(cda.getNumero()));
	}
	
	/**
	 * Evendo invocado antes de alterar a entidade.
	 * 
	 * @param cda
	 */
	public void preUpdate(Cda cda) {
		cda.setNumero(StringUtil.removeNaoNumericos(cda.getNumero()));
	}
	
}
