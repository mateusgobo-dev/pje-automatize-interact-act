package br.com.itx.component.grid;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
*
* A propriedade intelectual deste programa, como código-fonte
* e como sua derivação compilada, pertence à União Federal,
* dependendo o uso parcial ou total de autorização expressa do
* Conselho Nacional de Justiça.
* 
* Foi criada essa classe porque o método EntityQuery.getCoutEjbql() apresenta
* problemas na execução de query com subquery dentro de ORDER BY. A query 
* executada vem do arquivo alertaGrid.component.xml
* 
* @author waleska.barros
*
*/
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class AlertaGridQuery extends GridQuery {
	private static final long serialVersionUID = 8669748631058947141L;
	private static final Pattern SUBJECT_PATTERN = Pattern.compile("^select\\s+(\\w+(?:\\s*\\.\\s*\\w+)*?)(?:\\s*,\\s*(\\w+(?:\\s*\\.\\s*\\w+)*?))*?\\s+from",Pattern.CASE_INSENSITIVE);
	private static final Pattern FROM_PATTERN    = Pattern.compile("(^|\\s)(from)\\s", Pattern.CASE_INSENSITIVE);
	private static final Pattern WHERE_PATTERN   = Pattern.compile("\\s(where)\\s", Pattern.CASE_INSENSITIVE);
	private static final Pattern ORDER_PATTERN   = Pattern.compile("\\s(order)(\\s)+by\\s", Pattern.CASE_INSENSITIVE);
	private static final Pattern GROUP_PATTERN   = Pattern.compile("\\s(group)(\\s)+by\\s", Pattern.CASE_INSENSITIVE);
	private boolean useWildcardAsCountQuerySubject = true;
	
	
	/**
	 * Este método retorna a query(jpql) que retorna a quantidade de registros restuldados da pesquisa da GridQuery.
	 * 
	 * [PJEII-18551] O componente GridQuery não suporta que a cláusula "order by" possua subselects.
	 *               Para contornar esse problema, foi necessário sobrescrever o método "getCountEjbql".
	 * 		         Para criar a "countEjbqp" foi retirada o order by, pois não é necessário na consulta count(*).
	 * 
	 */
	public String getCountEjbql()
	{
	   String ejbql = getRenderedEjbql();
	   
	   Matcher fromMatcher = FROM_PATTERN.matcher(ejbql);
	   if ( !fromMatcher.find() )
	   {
	      throw new IllegalArgumentException("Nenhuma cláusula from encontrado na consulta");
	   }
	   int fromLoc = fromMatcher.start(2);
	   
	   Matcher orderMatcher = ORDER_PATTERN.matcher(ejbql);
	   int orderLoc = orderMatcher.find() ? orderMatcher.start(1) : ejbql.length();

	   Matcher groupMatcher = GROUP_PATTERN.matcher(ejbql);
	   int groupLoc = groupMatcher.find() ? groupMatcher.start(1) : orderLoc;
	   
	   ejbql = ejbql.substring(0, groupLoc); // Retira o order by, pois não é necessário na consulta do order by.
	   
	   Matcher whereMatcher = WHERE_PATTERN.matcher(ejbql);
	   int whereLoc = whereMatcher.find() ? whereMatcher.start(1) : groupLoc;

	   String subject;
	   if (getGroupBy() != null) {
	      subject = "distinct " + getGroupBy();
	   }
	   else if (useWildcardAsCountQuerySubject) {
	      subject = "*";
	   }else {
	       Matcher subjectMatcher = SUBJECT_PATTERN.matcher(ejbql);
	       if ( subjectMatcher.find() )
	       {
	          subject = subjectMatcher.group(1);
	       }
	       else
	       {
	          throw new IllegalStateException("Nenhuma cláusula from encontrado na consulta");
	       }
	   }
	   
	   String query = new StringBuilder(ejbql.length() + 15).append("select count(").append(subject).append(") ").
	      append(ejbql.substring(fromLoc, whereLoc).replace("join fetch", "join")).
	      append(ejbql.substring(whereLoc, groupLoc)).toString().trim();
	   
	   return query;
	}
	
}
