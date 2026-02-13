package br.jus.cnj.pje.util;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.nucleo.PJeBusinessException;


/**
 * Classe utilitária para formatar objetos que serão utilizados em modelos
 * de documentos. Operações disponíveis:
 * 
 * <ul>
 * 
 * <li>
 * Lista: Retorna uma String contendo o resultado, apresentando a informação de para cada um dos objetos da
 * coleção informada como parâmetro de entrada.
 * </li>
 * 
 * <li>
 * Tabela: Retorna uma String contendo uma tabela html com a informação de para cada um dos objetos da
 * coleção informada como parâmetro de entrada. 
 * </li>
 *  
 * </ul>
 * 
 * @author thiago.figueiredo@tse.jus.br
 *
 */
@Name(FormatadorUtils.NAME)
@Scope(ScopeType.EVENT)
public class FormatadorUtils {
	
	public static final String NAME = "formatadorUtils";
	
	private SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");
	
	private final String SEPARADOR_PADRAO = ", ";
	
	private final String CONECTOR_FINAL = " e ";
	
	/**
	 * Retorna uma String representando os elementos da lista informada como entrada.
	 * Para cada um dos objetos da lista dada, recupera-se as informaçãos a partir das propriedades 
	 * indicadas. A primeira propriedade é incluída como informação principal e as demais incluídas 
	 * como informações secundárias, dentro de parênteses e separadas por hífens. As informações de 
	 * cada item da lista são separadas pelo separador dado seguida de espaço e, caso seja verdadeiro 
	 * o parâmetro conectorFinal, incluindo a expressão " e " entre o penúltimo e o último item da lista.
	 * <br><br><br>
	 * Exemplo:
	 * <br><br>
	 * listar(processos, ';', false, "numeroProcesso", "orgaoJulgador", "dataAutuacao") em que há três 
	 * ProcessoTrf na lista processos:
	 * <br><br>
	 * "0000001-23.2012.2.00.0000 (1ª Vara Cível de Campina Grande - 01/01/2012); 
	 * 0000002-23.2012.2.00.0000 (2ª Vara Criminal de João Pessoa - 03/01/2012); 
	 * 0000002-23.2012.2.00.0000 (Vara Única de São José da Lagoa Tapada - 15/08/2012)"
	 * 
	 * 
	 * @param list - coleção de entrada
	 * @param separador - caracter que irá realizar a separação entre os itens da lista
	 * @param conectorFinal - indicador se será utilizado o "e" antes do ultimo item da lista
	 * @param propriedades - atributos dos objetos da coleção a serem recuperados, separados por ponto-e-vírgula
	 * 
	 * @return string representando os atributos da lista a ser impressa.
	 * @throws PJeBusinessException 
	 */
	public String lista(Collection<?> list, String separador, boolean conectorFinal, String propriedades) throws PJeBusinessException{
		
		StringBuilder retorno = new StringBuilder();
		
		if(list != null && !list.isEmpty()){
			Iterator<?> iterator = list.iterator();
			String[] props = parseProperties(propriedades);
			String conteudo = null;
			List<String> concreteData = new ArrayList<String>();
			while(iterator.hasNext()){
				if((conteudo = extrairInformacaoObjLista(iterator.next(), props)) == null){
					continue;
				}
				concreteData.add(conteudo);
			}
			for(int i = 0; i < concreteData.size(); i++){
				retorno.append(concreteData.get(i));
				if(i < (concreteData.size() - 2) || !conectorFinal){
					retorno.append(separador);
				} else if(i == (concreteData.size() - 2) && conectorFinal){
					retorno.append(CONECTOR_FINAL);
				} 
			}
				
		}
		
		return retorno.toString();
	}
	
	/**
	 * Retorna uma String representando os elementos da lista informada como entrada.
	 * Para cada um dos objetos da lista dada, recupera-se as informaçãos a partir do método toString()
	 * dos objetos da coleção. As informações de cada item da lista são separadas pelo separador dado 
	 * seguida de espaço e, caso seja verdadeiro o parâmetro conectorFinal, incluindo a expressão " e " 
	 * entre o penúltimo e o último item da lista.
	 * <br><br><br>
	 * Exemplo:
	 * <br><br> 
	 * listar(processos, ';', false) em que há três ProcessoTrf na lista processos
	 * <br><br>
	 * "0000001-23.2012.2.00.0000; 0000002-23.2012.2.00.0000; 0000002-23.2012.2.00.0000"
	 * 
	 * @param col - coleção de entrada
	 * @param separador - caracter que irá realizar a separação entre os itens da lista
	 * @param conectorFinal - indicador se será utilizado o "e" antes do ultimo item da lista
	 * 
	 * @return string representando os atributos da lista a ser impressa.
	 * @throws PJeBusinessException 
	 */
	public String lista(Collection<?> col, String separador, boolean conectorFinal) throws PJeBusinessException{
		String parametros = null;
		return lista(col, separador, conectorFinal, parametros);
	}
	
	/**
	 * Retorna uma String representando os elementos da lista informada como entrada.
	 * Para cada um dos objetos da lista dada, do método toString(), separada pelo separador dado 
	 * seguida de espaço e, entre o penúltimo e o último item da lista, com a expressão " e ".
	 * <br><br><br>
	 * Exemplo:
	 * <br><br> 
	 * listar(processos, ';') em que há três ProcessoTrf na lista processos
	 * <br><br>
	 * "0000001-23.2012.2.00.0000; 0000002-23.2012.2.00.0000 e 0000002-23.2012.2.00.0000"
	 * 
	 * @param col - coleção de entrada
	 * @param separador - caracter que irá realizar a separação entre os itens da lista
	 * 
	 * @return string representando os atributos da lista a ser impressa.
	 * @throws PJeBusinessException 
	 */
	public String lista(Collection<?> col, String separador) throws PJeBusinessException{
		return lista(col, separador, true);
	}
	
	/**
	 * Retorna uma String representando os elementos da lista informada como entrada.
	 * Para cada um dos objetos da lista dada, do método toString(), separada por vírgula seguida de espaço 
	 * e, entre o penúltimo e o último item da lista, com a expressão " e ".
	 * <br><br><br>
	 * Exemplo:
	 * <br><br> 
	 * listar(processos) em que há três ProcessoTrf na lista processos
	 * <br><br>
	 * "0000001-23.2012.2.00.0000, 0000002-23.2012.2.00.0000 e 0000002-23.2012.2.00.0000"
	 * 
	 * @param col - coleção de entrada
	 * 
	 * @return string representando os atributos da lista a ser impressa.
	 * @throws PJeBusinessException 
	 */
	public String lista(Collection<?> col) throws PJeBusinessException{
		return lista(col, SEPARADOR_PADRAO);
		
	}
	
	/**
	 * Retorna uma String contendo uma tabela html com uma única coluna caso o parâmetro colunaUnica seja 
	 * verdadeiro ou tantas colunas quanto forem as propriedades exibíveis e tantas linhas quantos forem 
	 * os elementos da lista, contendo, cada uma das linhas o resultado do método toString das propriedades 
	 * listadas. Caso o parâmetro colunaUnica seja verdadeiro, a primeira propriedade será considerada 
	 * informação principal e as demais como informações secundárias objeto contido na lista, sendo, nesse caso, 
	 * exibidas dentro de parênteses e separadas por hífens.
	 * <br><br><br>
	 * Exemplo:
	 * <br><br>
	 * tabela(processos, false, "numeroProcesso", "orgaoJulgador"), em que há três ProcessoTrf na lista processos
	 * <br><br>
	 * <table>
	 *    <tr>
	 *       <td>0000001-23.2012.2.00.0000</td>
	 *       <td>1ª Vara Cível de Campina Grande</td>
	 *    </tr>
	 *    <tr>
	 *       <td>0000002-23.2012.2.00.0000</td>
	 *       <td>2ª Vara Criminal de João Pessoa</td>
	 *    </tr>
	 *    <tr>
	 *       <td>0000002-23.2012.2.00.0000</td>
	 *       <td>Vara Única de São José da Lagoa Tapada</td>
	 *    </tr>
	 * </table>
	 * <br><br><br>
	 * tabela(processos, true, "numeroProcesso", "orgaoJulgador") em que há três ProcessoTrf na lista processos
	 * <br><br>
	 * <table>
	 *    <tr>
	 *       <td>0000001-23.2012.2.00.0000 (1ª Vara Cível de Campina Grande)</td>
	 *    </tr>
	 *    <tr>
	 *       <td>0000002-23.2012.2.00.0000 (2ª Vara Criminal de João Pessoa)</td>
	 *    </tr>
	 *    <tr>
	 *       <td>0000002-23.2012.2.00.0000 (Vara Única de São José da Lagoa Tapada)</td>
	 *    </tr>
	 * </table>
	 * 
	 * @param col - coleção de entrada
	 * @param colunaUnica - indicador de apresentação das informações em coluna única
	 * @param propriedades - atributos dos objetos da coleção a serem recuperados, separados por ponto-e-vírgula
	 * 
	 * @return tabela HTML com as informações solicitadas
	 * @throws PJeBusinessException 
	 */
	public String tabela(Collection<?> col, boolean colunaUnica, String propriedades) throws PJeBusinessException{
		
		StringBuilder retorno = new StringBuilder();
		
		if(col != null && !col.isEmpty()){
			
			String[] props = parseProperties(propriedades);
			retorno.append("<table>");
			
			for (Object obj : col) {
				if(obj == null){
					continue;
				}
				
				retorno.append("<tr>");
				
				if(colunaUnica || props == null || props.length == 0){
					retorno.append("<td>");
					retorno.append(extrairInformacaoObjLista(obj, props));
					retorno.append("</td>");
				} else {
					for (String propriedade : props) {
						retorno.append("<td>");
						retorno.append(getProperty(obj, propriedade));
						retorno.append("</td>");
					}
				}
				
				retorno.append("</tr>");
			}
			
			retorno.append("</table>");
		}
		
		return retorno.toString();
	}
	
	/**
	 * Retorna uma String contendo uma tabela html com uma única coluna e tantas linhas quantos forem os elementos 
	 * da lista, contendo, cada uma das linhas o resultado do método toString do objeto contido na lista.
	 * <br><br><br>
	 * Exemplo:
	 * <br><br>
	 * tabela(processos), em que há três ProcessoTrf na lista processos
	 * <br><br>
	 * <table>
	 *    <tr>
	 *       <td>0000001-23.2012.2.00.0000</td>
	 *    </tr>
	 *    <tr>
	 *       <td>0000002-23.2012.2.00.0000</td>
	 *    </tr>
	 *    <tr>
	 *       <td>0000002-23.2012.2.00.0000</td>
	 *    </tr>
	 * </table>
	 * 
	 * @param col - coleção de entrada
	 * 
	 * @return tabela HTML com as informações solicitadas
	 * @throws PJeBusinessException 
	 */
	public String tabela(Collection<?> col) throws PJeBusinessException{
		String parametros = null;
		return tabela(col, true, parametros);
	}
	
	private String[] parseProperties(String propriedades){
		String[] props = null;
		if(propriedades != null && !propriedades.isEmpty()){
			if(propriedades.indexOf(';') != -1){
				props = propriedades.split(";");
			}else{
				props = new String[]{propriedades};
			}
		}else{
			props = new String[0];
		}
		return props;
	}
	
	/*
	 * Operação que retorna informações que estão dentro do objeto a ser explorado.
	 * A operação avalia o objeto e extrai suas propriedades caso seja elas tenham sido
	 * informadas
	 *  
	 * @param retorno
	 * @param obj
	 * @param propriedades
	 */
	private String extrairInformacaoObjLista(Object obj, String... propriedades) throws PJeBusinessException{
		if(obj == null){
			return null;
		}
		Object aux = obj instanceof Entry<?, ?> ? ((Entry<?,?>) obj).getValue() : obj;
		if(aux instanceof Date){
			return dateFormater.format(aux);
		}else if(propriedades == null || propriedades.length == 0){
			return aux.toString();
		}else{
			StringBuilder retorno = new StringBuilder();
			boolean addHifen = false;
				
			//Recuperando informação pricipal
			retorno.append(getProperty(aux, propriedades[0]));
			
			//Recuperando as propriedades secundárias
			if(propriedades.length > 1){
				retorno.append(" (");
				for (int i = 1; i < propriedades.length; i++) {
					
					if(addHifen){
						retorno.append(" - ");
					}
					retorno.append(getProperty(aux, propriedades[i]));
					addHifen = true;
				}
				retorno.append(")");
			}
			
			return retorno.toString();
		}
		
	}
	
	private Object getProperty(Object o, String property){
		if(o == null || property == null || property.isEmpty()){
			return null;
		}
		if(property.indexOf('.') != -1){
			Object aux = getProperty(o, property.split("\\.")[0]);
			return getProperty(aux, property.split("\\.")[1]);
		}else{
			try {
				Object ret = PropertyUtils.getSimpleProperty(o, property);
				return ret == null ? null : (ret instanceof Date ? dateFormater.format(ret) : ret); 
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			} catch (InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
	
	/**
 	 * Converte o valor da variável para caixa alta.
 	 * 
 	 * @param String variavel
 	 * @return variavel.toUpperCase() 
 	 */
 	public String caixaAlta(String variavel){
 		String retorno = "";
 		if (variavel != null && !variavel.isEmpty()){
 			retorno = variavel.toUpperCase();
 		}
 		return retorno;
	}	
 	
	/**
 	 * Converte o valor da variável para caixa baixa.
 	 * 
 	 * @param String variavel
 	 * @return variavel.toLowerCase()
 	 */
 	public String caixaBaixa(String variavel){
 		String retorno = "";
 		if (variavel != null && !variavel.isEmpty()){
 			retorno = variavel.toLowerCase();
 		}
 		return retorno;
	} 	
}
