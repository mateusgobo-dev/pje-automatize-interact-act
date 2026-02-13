/**
 * ExtensionManagerStrategy.java.
 *
 * Data: 10/01/2019
 */
package br.jus.cnj.pje.intercomunicacao.v223.extensionmanager;

import java.util.HashMap;
import java.util.Map;

import br.jus.cnj.intercomunicacao.v223.cda.ColecaoCDA;
import br.jus.cnj.intercomunicacao.v223.criminal.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Estratégia responsável em recuperar a manager do respectivo tipo de objeto.
 * 
 * @author Adriano Pamplona
 */
public class ExtensionManagerStrategy {
	private static Map<Class, ExtensionManager> mapa = null;
	
	/**
	 * Construtor.
	 */
	private ExtensionManagerStrategy() {
		// Construtor.
	}
	
	/**
	 * Retorna a ExtensionManager do tipo de objeto selecionado.
	 * 
	 * @param objeto Tipo do objeto.
	 * @return ExtensionManager
	 */
	public static ExtensionManager get(Object objeto) {
		Class clazz = (objeto != null ? objeto.getClass() : null);
		ExtensionManager resultado = getMapa().get(clazz);
		if (resultado == null) resultado = novoDummyExtensionManager();
		
		return resultado;
	}
	
	/**
	 * @return Mapa de Classe para ExtensionManager.
	 */
	private static Map<Class, ExtensionManager> getMapa() {
		if (mapa == null) {
			mapa = new HashMap<>();
			mapa.put(null, novoDummyExtensionManager());
			mapa.put(Processo.class, new CriminalExtensionManager());
			mapa.put(ColecaoCDA.class, new CDAExtensionManager());
		}
		return mapa;
	}
	
	/**
	 * @return Dummy ExtensionManager
	 */
	private static ExtensionManager novoDummyExtensionManager() {
		return new ExtensionManager<Object>() {

			@Override
			public void execute(ProcessoTrf processo, Object extensao) {}
		};
	}
}
