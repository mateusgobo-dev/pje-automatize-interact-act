package br.com.infox.cliente.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

import br.jus.pje.nucleo.enums.SemanaEnum;

public class SemanaType implements UserType {

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return deepCopy(cached);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object deepCopy(Object value) throws HibernateException {
		List<SemanaEnum> returnValue = new ArrayList<SemanaEnum>((List<SemanaEnum>) value);
		return returnValue;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		// TODO Auto-generated method stub
		return (Serializable) deepCopy(value);
	}

	@Override
	public boolean equals(Object arg0, Object arg1) throws HibernateException {
		return arg0.equals(arg1);
	}

	@Override
	public int hashCode(Object arg0) throws HibernateException {
		return arg0.hashCode();
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
		String value = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names[0], session);
		List<SemanaEnum> returnValue = new ArrayList<SemanaEnum>(0);

		if (value != null) {
			String[] values = value.split(",");
			for (String item : values) {
				returnValue.add(SemanaEnum.valueOf(item.trim()));
			}
			Collections.sort(returnValue, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return new Integer(((SemanaEnum) o1).ordinal()).compareTo(((SemanaEnum) o2).ordinal());
				}
			});
		}

		return returnValue;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
		List<SemanaEnum> objectValue = (List<SemanaEnum>) value;

		StringBuilder dataBaseValue = new StringBuilder();
		if (objectValue != null) {
			Collections.sort(objectValue, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return new Integer(((SemanaEnum) o1).ordinal()).compareTo(((SemanaEnum) o2).ordinal());
				}
			});
			for (SemanaEnum item : objectValue) {
				dataBaseValue.append(item.toString());
				dataBaseValue.append(",");
			}
			dataBaseValue = new StringBuilder(dataBaseValue.substring(0, dataBaseValue.lastIndexOf(",")));
		}
		StandardBasicTypes.STRING.nullSafeSet(st, (dataBaseValue != null) ? dataBaseValue.toString() : null, index, session);

	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return deepCopy(original);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class returnedClass() {
		return List.class;
	}

	@Override
	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}

}
