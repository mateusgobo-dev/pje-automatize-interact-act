package br.jus.cnj.pje.servicos.prazos;

import java.util.HashMap;
import java.util.Map;

import br.jus.pje.nucleo.enums.CategoriaPrazoEnum;
import br.jus.pje.servicos.prazos.ICalculadorPrazo;

public class GerenciadorCache {
	
	/**
	 * Armazena em cache o calculadores de prazo que serao reutilizado quando o cache estiver habilitado.
	 * Pelo hash da lista feriados  
	 */
	private Map<Integer,Map<CategoriaPrazoEnum,ICalculadorPrazo>> cache = new HashMap<Integer,Map<CategoriaPrazoEnum,ICalculadorPrazo>>();

	/**
	 * Limpa toda as entradas do cache
	 */
	public void limpar() {
		this.cache.clear();		
	}

	/**
	 * Obtem o calculador de prazo para a categoria de prazo se existir entrada no cache ou cria uma nova instancia e insere a entrada no cache para posterior utilizacao.
	 * @param categoriaPrazo A categoria de prazo
	 * @param calendario O calendario com uma lista de eventos do orgao julgador
	 * @return Uma instancia de calculador de prazo para a categoria informada
	 */
	public ICalculadorPrazo obtemCalculadorPrazo(CategoriaPrazoEnum categoriaPrazo,	Calendario calendario) {

		Integer orgaoJulgadorId = calendario.getOrgaoJulgador().getIdOrgaoJulgador();
		
		ICalculadorPrazo calculadorPrazo = recuperarEntrada(orgaoJulgadorId, categoriaPrazo);
		
		// Caso nao exista uma entrada no cache sera criada uma nova instancia e inserida uma nova entrada
		if (calculadorPrazo == null) {
			
			calculadorPrazo = CalculadorPrazoFactory.novoCalculadorPrazo(categoriaPrazo, calendario);
			
			Map<CategoriaPrazoEnum,ICalculadorPrazo> cacheEntrada; 
			
			if (!this.cache.containsKey(orgaoJulgadorId)) {
				cacheEntrada = new HashMap<CategoriaPrazoEnum,ICalculadorPrazo>();
				this.cache.put(orgaoJulgadorId, cacheEntrada);
			}
			else {
				cacheEntrada = this.cache.get(orgaoJulgadorId);
			}
			
			cacheEntrada.put(categoriaPrazo, calculadorPrazo);
		}
		
		return calculadorPrazo;
	}

	/**
	 * Recupera uma instancia do calculador de prazo se existir no cache senao retorna nulo. 
	 * 
	 * @param orgaoJulgadorId O identificador do orgao julgador
	 * @param categoriaPrazo A categoria de prazo 
	 * @return Retorna uma instancia do calculador de prazo se existir no cache senao retorna nulo
	 */
	private ICalculadorPrazo recuperarEntrada(Integer orgaoJulgadorId, CategoriaPrazoEnum categoriaPrazo) {
		
		ICalculadorPrazo calculadorPrazo = null;
		
		if (this.cache.containsKey(orgaoJulgadorId) && this.cache.get(orgaoJulgadorId).containsKey(categoriaPrazo)) {
			calculadorPrazo = this.cache.get(orgaoJulgadorId).get(categoriaPrazo);
		}
		
		return calculadorPrazo;
	}	
}