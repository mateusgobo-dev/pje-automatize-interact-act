package br.jus.pje.api.filter;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

public class ApiFilterUtil {

	public ApiCriteria findCriteriaByField(ApiFilter filter, String field) {
		ApiCriteria criteria = filter.getCriterias()
									 .stream()
									 .filter(crit -> crit.getField().equalsIgnoreCase(field))
									 .findFirst()
									 .orElse(null);
		
		return criteria;
	}
	
	public ApiCondition findConditionByOperator(ApiCriteria criteria, ApiConditionOperatorEnum operator) {
		ApiCondition condition = criteria.getConditions()
										.stream()
										.filter(cond -> cond.getOperator().equals(operator))
										.findFirst()
										.orElse(null);
		return condition;
	}
	
	public ApiFilter convertToApiFilter(JsonNode rootNode) {
		ApiFilter filter = new ApiFilter();
		Iterator<Entry<String, JsonNode>> argumentos = rootNode.fields();
		while(argumentos.hasNext()) {
			Map.Entry<String, JsonNode> argumento = (Map.Entry<String, JsonNode>) argumentos.next();
			ApiCriteria criteria = new ApiCriteria();
			criteria.setField(argumento.getKey());
			if(argumento.getValue().isArray()) {
				Iterator<Entry<String, JsonNode>> condicoes = argumento.getValue().fields();
				while(condicoes.hasNext()) {
					Map.Entry<String, JsonNode> condicao = (Map.Entry<String, JsonNode>) condicoes.next();
					ApiCondition condition = new ApiCondition();
					condition.setValue(condicao.getValue().toString());
					condition.setOperator(ApiConditionOperatorEnum.valueOf(condicao.getKey()));
					criteria.addCondition(condition);
				}
			} else {
				Map.Entry<String, JsonNode> condicao = (Map.Entry<String, JsonNode>) argumento.getValue().fields().next();
				ApiCondition condition = new ApiCondition();
				condition.setValue(condicao.getValue().toString());
				condition.setOperator(ApiConditionOperatorEnum.valueOf(condicao.getKey().toUpperCase()));
				criteria.addCondition(condition);
			}
			filter.addCriteria(criteria);
		}
		
		return filter;
	}
	
}
