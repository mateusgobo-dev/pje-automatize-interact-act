package br.jus.csjt.pje.commons.validator;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.hibernate.mapping.Property;
import org.hibernate.validator.PropertyConstraint;
import org.hibernate.validator.Validator;

import br.jus.pje.jt.util.JTDateUtil;

/**
 * Classe responsavel por fazer a validacao do campo anotado com @
 * <code>PastAndToday</code>.
 * 
 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
 * 
 * @category PJE-JT
 * @since 1.4.2
 * @created 29/09/2011
 * 
 */
public class PastAndTodayValidator implements Validator<PastAndToday>, PropertyConstraint, Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @category PJE-JT
	 * 
	 * @since 1.4.2
	 * 
	 * @created 29/09/2011
	 * 
	 * @param arg0
	 * 
	 * @see
	 * org.hibernate.validator.PropertyConstraint#apply(org.hibernate.mapping
	 * .Property)
	 */
	@Override
	public void apply(Property property) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @category PJE-JT
	 * 
	 * @since 1.4.2
	 * 
	 * @created 29/09/2011
	 * 
	 * @param arg0
	 * 
	 * @see
	 * org.hibernate.validator.Validator#initialize(java.lang.annotation.Annotation
	 * )
	 */
	@Override
	public void initialize(PastAndToday pastAndToday) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @category PJE-JT
	 * 
	 * @since 1.4.2
	 * 
	 * @created 29/09/2011
	 * 
	 * @param date for validation.
	 * 
	 * @return true caso a data esteja no passado ou hoje.
	 * 
	 * @see org.hibernate.validator.Validator#isValid(java.lang.Object)
	 */
	@Override
	public boolean isValid(Object date) {

		if (date == null)
			return true;
		if (date instanceof Date) {
			Date d = (Date) date;
			return JTDateUtil.beforeOrEquals(d, new Date());
		} else if (date instanceof Calendar) {
			Calendar cal = (Calendar) date;
			Date c = cal.getTime();
			return JTDateUtil.beforeOrEquals(c, new Date());
		} else {
			return false;
		}
	}

}
