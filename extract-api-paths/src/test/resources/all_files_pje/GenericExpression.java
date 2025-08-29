package br.com.infox.ibpm.expression;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jdom.Element;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;

@Name(GenericExpression.NAME)
@AutoCreate
public class GenericExpression implements ExpressionHandler {

	public static final String NAME = "genericExpression";

	private String expression;

	private Set<String> idSet = new HashSet<String>();

	private List<String> warningList = new ArrayList<String>();

	private boolean validate;

	@Override
	public void execute() {
		for (String s : parseParameters()) {
			idSet.add(s);
		}

		//  O warning nao esta documentado
		//if (validate && !idSet.isEmpty()) {
		//	warningList.add(expression);
		//}
	}

	public String getExpression() {
		return expression;
	}

	protected List<String> parseParameters() {
		try {
			List<String> ret = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(expression, "(',)");
			st.nextToken();
			while (st.hasMoreTokens()) {
				String trim = st.nextToken().trim();
				if (trim.matches("\\d+|\\d+:?\\d+")) {
					ret.add(trim);
				}
			}
			return ret;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Element getXml() {
		if (getRootName() == null) {
			return null;
		}
		Element root = new Element(getRootName());
		getContent(root, getElementName(), idSet, getSql());
		return root;
	}

	protected String getSql() {
		return null;
	}

	protected String getElementName() {
		return null;
	}

	protected String getRootName() {
		return null;
	}

	protected void getContent(Element root, String name, Set<String> set, String sql) {
		GenericDAO genericDAO = (GenericDAO) Component.getInstance(GenericDAO.NAME);
		Query q = genericDAO.getEntityManager().createNativeQuery(sql);
		for (String id : set) {
			int i = 0;
			try {
				i = Integer.parseInt(id);
			} catch (NumberFormatException e) {
				continue;
			}
			Element e = new Element(name);
			e.setAttribute("id", id);
			q.setParameter(1, i);
			Object result = EntityUtil.getSingleResult(q);
			if (result != null) {
				String text = result.toString();
				e.addContent(text);
			} else {
				warningList.add(MessageFormat.format("{0}={1}", name, id));
			}
			root.addContent(e);
		}
	}

	@Override
	public List<String> getWarningList() {
		return warningList;
	}

	public void validateOn() {
		this.validate = true;
	}

	@Override
	public void setExpression(String expression) {
		this.expression = expression;
	}

}
