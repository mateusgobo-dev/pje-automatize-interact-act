package br.com.infox.ibpm.validator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.jboss.seam.Component;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;

public abstract class AbstractValidator {

	private Map<Integer, String> idMap = new HashMap<Integer, String>();
	private Map<String, String> oldToNewIdMap = new HashMap<String, String>();
	private List<String> warningList = new ArrayList<String>();

	public void put(int id, String name) {
		idMap.put(id, name);
	}

	public String validate(String xmlFluxo) {
		for (Entry<Integer, String> e : idMap.entrySet()) {
			GenericDAO genericDAO = (GenericDAO) Component.getInstance(GenericDAO.NAME);
			Query q = genericDAO.getEntityManager().createNativeQuery(getSql());
			q.setParameter(1, e.getValue());
			Integer result = EntityUtil.getSingleResult(q);
			boolean found = false;
			if (result != null && result.intValue() == e.getKey().intValue()) {
				found = true;
				break;
			}
			if (!found) {
				oldToNewIdMap.put(e.getKey() + "", result + "");
			}
			if (result == null) {
				warningList.add(MessageFormat.format("{0}: {1}", getClass().getSimpleName(), e));
			}
		}
		if (!oldToNewIdMap.isEmpty()) {
			xmlFluxo = changeIds(xmlFluxo);
		}
		return xmlFluxo;
	}

	protected Map<String, String> getOldToNewIdMap() {
		return oldToNewIdMap;
	}

	/**
	 * Ajusta os ids do fluxo
	 * 
	 * @param xmlFluxo
	 *            é o fluxo original
	 * @return fluxo com os ids ajustados
	 */
	private String changeIds(String xmlFluxo) {
		for (Entry<String, String> e : getOldToNewIdMap().entrySet()) {
			xmlFluxo = changeId(xmlFluxo, e.getKey(), e.getValue());
		}
		return xmlFluxo;
	}

	private String changeId(String xmlFluxo, String oldId, String newId) {
		StringBuilder sb = new StringBuilder();
		int i = xmlFluxo.indexOf(getExpression());
		int j = 0;
		while (i > -1) {
			sb.append(xmlFluxo.substring(j, i));
			j = xmlFluxo.indexOf(")", i);
			String sub = xmlFluxo.substring(i, j);
			String oldIdPattern = getOldIdPattern(oldId);
			String newIdPattern = getNewIdPattern(newId);
			sub = sub.replaceAll(oldIdPattern, newIdPattern);
			sb.append(sub);
			i = xmlFluxo.indexOf(getExpression(), j);
		}
		sb.append(xmlFluxo.substring(j));
		return sb.toString();
	}

	protected String getNewIdPattern(String newId) {
		return newId;
	}

	protected String getOldIdPattern(String oldId) {
		return "\\b" + oldId + "\\b";
	}

	public List<String> getWarningList() {
		return warningList;
	}

	/**
	 * Expressão sql que deve selecionar o id da tabela partindo da descrição
	 * 
	 * @return
	 */
	protected abstract String getSql();

	/**
	 * Expressão a ser buscada para trocar os ids
	 * 
	 * @return
	 */
	protected abstract String getExpression();
}
