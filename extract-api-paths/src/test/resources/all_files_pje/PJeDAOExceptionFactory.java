/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.PersistenceException;

import org.hibernate.LazyInitializationException;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.PersistentObjectException;
import org.hibernate.PropertyAccessException;
import org.hibernate.PropertyValueException;
import org.hibernate.TypeMismatchException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.jdbc.TooManyRowsAffectedException;

import br.jus.cnj.pje.nucleo.PJeDAOException;

public class PJeDAOExceptionFactory{

	private static final String DAO_SBGD_CODE_DEFAULT = "dao.sgbd.error.codeDefault";
	private static final String DAO_SBGD_PRIMARY_KEY = "dao.sgbd.error.primaryKey";
	private static final String DAO_SBGD_CONSTRAINT_VIOLATION = "dao.sgbd.error.constraintViolation";
	private static final String DAO_SBGD_NON_UNIQUE_RESULT = "dao.sgbd.error.nonUniqueResult";
	private static final String DAO_SBGD_LAZY_INITIALIZATION = "dao.sgbd.error.lazyInitialization";
	private static final String DAO_SBGD_SQL_GRAMMAR = "dao.sgbd.error.sqlGrammar";
	private static final String DAO_SGBD_IDENTIFIER_GENERATION = "dao.sgbd.error.identifierGeneration";
	private static final String DAO_SGBD_DETACHED_OBJECT = "dao.sgbd.error.detachedObject";
	private static final String DAO_SGBD_PROPERTY_ACCESS = "dao.sgbd.error.propertyAccess";
	private static final String DAO_SGBD_PROPERTY_VALUE = "dao.sgbd.error.propertyValue";
	private static final String DAO_SGBD_TYPE_MISMATCH = "dao.sgbd.error.typeMismatch";
	private static final String DAO_SGBD_TOO_MANY_ROWS_AFFECTED = "dao.sgbd.error.tooManyRowsAffected";

	private static Class<?>[] exceptionClasses = {NonUniqueObjectException.class,
			ConstraintViolationException.class, NonUniqueResultException.class,
			LazyInitializationException.class, SQLGrammarException.class,
			IdentifierGenerationException.class,
			PersistentObjectException.class,
			PropertyAccessException.class,
			PropertyValueException.class,
			TooManyRowsAffectedException.class,
			TypeMismatchException.class};

	private static Throwable getFirstCause(Throwable e){
		List<Class<?>> exceptions = Arrays.asList(exceptionClasses);
		if (e.getCause() != null){
			if (exceptions.contains(e.getClass())){
				return e;
			}
			else{
				return getFirstCause(e.getCause());
			}
		}
		else{
			return e;
		}
	}

	public static PJeDAOException getDaoException(Exception e){
		List<Object> params = new ArrayList<Object>();
		if (e instanceof PersistenceException){
			Throwable erro = getFirstCause(e);

			if (erro instanceof NonUniqueObjectException){
				params.add(((java.sql.BatchUpdateException) erro.getCause()).getNextException().toString());
				return new PJeDAOException(DAO_SBGD_PRIMARY_KEY, e, params.toArray());
			}
			else if (erro instanceof NonUniqueResultException){
				return new PJeDAOException(DAO_SBGD_NON_UNIQUE_RESULT, e);
			}
			else if (erro instanceof ConstraintViolationException){
				params.add(((java.sql.BatchUpdateException) erro.getCause()).getNextException().toString());
				return new PJeDAOException(DAO_SBGD_CONSTRAINT_VIOLATION, e, params.toArray());
			}
			else if (erro instanceof LazyInitializationException){
				return new PJeDAOException(DAO_SBGD_LAZY_INITIALIZATION, e);
			}
			else if (erro instanceof SQLGrammarException){
				Throwable causa = erro.getCause();
				if (causa instanceof BatchUpdateException) {
					params.add(((BatchUpdateException) causa).getNextException().toString());
				} else {
					params.add(erro);
				}
				return new PJeDAOException(DAO_SBGD_SQL_GRAMMAR, e, params.toArray());
			}
			else if (erro instanceof IdentifierGenerationException){
				return new PJeDAOException(DAO_SGBD_IDENTIFIER_GENERATION, e);
			}
			else if (erro instanceof PersistentObjectException){
				if (e.getMessage().toUpperCase().contains("DETACHED")){
					return new PJeDAOException(DAO_SGBD_DETACHED_OBJECT, e);
				}
			}
			else if (erro instanceof PropertyAccessException){
				return new PJeDAOException(DAO_SGBD_PROPERTY_ACCESS, e);
			}
			else if (erro instanceof PropertyValueException){
				return new PJeDAOException(DAO_SGBD_PROPERTY_VALUE, e);
			}
			else if (erro instanceof TooManyRowsAffectedException){
				params.add(((java.sql.BatchUpdateException) erro.getCause()).getNextException().toString());
				return new PJeDAOException(DAO_SGBD_TOO_MANY_ROWS_AFFECTED, e, params.toArray());
			}
			else if (erro instanceof TypeMismatchException){
				return new PJeDAOException(DAO_SGBD_TYPE_MISMATCH, e);
			}

			e.printStackTrace();
			params.add(e);
			return new PJeDAOException(DAO_SBGD_CODE_DEFAULT, e, params.toArray());
		}

		e.printStackTrace();
		params.add(e);
		return new PJeDAOException(DAO_SBGD_CODE_DEFAULT, e, params.toArray());
	}

}
