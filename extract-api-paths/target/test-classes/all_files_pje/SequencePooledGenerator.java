/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.type.Type;

/***
 * Usando uma sequence de banco com um valor de incremento maior que 1 (um),
 * realiza um pool de sequences com o objetivo de acelerar a obtenção de 
 * sequences, uma vez que torna desnecessário o acesso ao banco para cada 
 * sequence requerida.
 * 
 * Se allocationSize for -1, utiliza o valor como definido no banco de dados.
 */
public class SequencePooledGenerator extends SequenceGenerator {

	public static final String PARAM_ALLOCATION_SIZE = "allocationSize";
	public static final String PARAM_DISABLE_ALTER_SEQUENCE = "disableAlterSequence";
	public static final String PARAM_DECREMENTAL = "decrementalSequence";
	private boolean decrementalSequence;

	static enum ReturnedClassTypeEnum {
		LONG, INTEGER, SHORT, STRING;
	}
	
	private volatile long idSequence = -1;
	private volatile int idMax = Integer.MAX_VALUE;
	private int allocationSize;
	private volatile boolean forcedOneByTime;
	private boolean disableAlterSequence;
	private ReturnedClassTypeEnum returnedClassType;
	private volatile boolean sequenceChecked;

	public SequencePooledGenerator() {
	}

	@Override
	public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
		this.allocationSize = ConfigurationHelper.getInt(PARAM_ALLOCATION_SIZE, params, -1);
		this.disableAlterSequence = ConfigurationHelper.getBoolean(PARAM_DISABLE_ALTER_SEQUENCE, params, 
				ConfigurationHelper.getBoolean(PARAM_DISABLE_ALTER_SEQUENCE, System.getProperties(), true));
		this.decrementalSequence = ConfigurationHelper.getBoolean(PARAM_DECREMENTAL, params, 
				ConfigurationHelper.getBoolean(PARAM_DECREMENTAL, System.getProperties(), false));
		
		String parameters = ConfigurationHelper.getString(PARAMETERS, params, null);
		Properties props = params;
		if (!disableAlterSequence && (parameters==null)) {
			props = new Properties();
			props.putAll(params);
			props.setProperty(PARAMETERS, getSequenceSettings());
		}
				
		super.configure(type, props, dialect);
		
		Class<?> clazz = type.getReturnedClass();
		if ( clazz == Long.class ) {
			returnedClassType = ReturnedClassTypeEnum.LONG;
		} else
		if ( clazz == Integer.class ) {
			returnedClassType = ReturnedClassTypeEnum.INTEGER;
		} else
		if ( clazz == Short.class ) {
			returnedClassType = ReturnedClassTypeEnum.SHORT;
		} else
		if ( clazz == String.class ) {
			returnedClassType = ReturnedClassTypeEnum.STRING;
		} else {
			throw new IdentifierGenerationException( "this id generator generates long, integer, short or string" );
		}
	}

	private Serializable generateDec(SessionImplementor sessionImplementor, Object entity) throws HibernateException {
		if (!sequenceChecked) {
			forcedOneByTime = !(disableAlterSequence || alterSequenceIncrementSize(sessionImplementor));
			sequenceChecked = true;
		}
		
		if (idMax<1) {
			idSequence = ((Number)super.generate(sessionImplementor, entity)).longValue();
			idMax = forcedOneByTime ? 1 : allocationSize;
		}
		
		switch (returnedClassType) {
			case LONG:
				return Long.valueOf(--idMax + idSequence);
			case INTEGER:
				return Integer.valueOf(--idMax + (int)idSequence);
			case SHORT:
				return Short.valueOf((short)(--idMax + idSequence));
			case STRING:
				return String.valueOf(--idMax + idSequence);
		}
		
		throw new IdentifierGenerationException( "this id generator generates long, integer, short or string" );
	}

	private Serializable generateInc(SessionImplementor sessionImplementor, Object entity) throws HibernateException {
		if (!sequenceChecked) {
			forcedOneByTime = !(disableAlterSequence || alterSequenceIncrementSize(sessionImplementor));
			checkSequenceAllocationSize(sessionImplementor);
			sequenceChecked = true;
		}
		
		if (idMax>=(forcedOneByTime ? 1 : allocationSize)) {
			idSequence = ((Number)super.generate(sessionImplementor, entity)).longValue();
			idMax = 0;
		}

		final int inc = idMax++;
		switch (returnedClassType) {
			case LONG:
				return Long.valueOf(inc + idSequence);
			case INTEGER:
				return Integer.valueOf(inc + (int)idSequence);
			case SHORT:
				return Short.valueOf((short)(inc + idSequence));
			case STRING:
				return String.valueOf(inc + idSequence);
		}
		
		throw new IdentifierGenerationException( "this id generator generates long, integer, short or string" );
	}

	@Override
	public synchronized Serializable generate(SessionImplementor sessionImplementor, Object entity) throws HibernateException {
		return decrementalSequence 
				? generateDec(sessionImplementor, entity) 
				: generateInc(sessionImplementor, entity);
	}
	
	private void checkSequenceAllocationSize(SessionImplementor session) throws HibernateException {
		StringBuilder sbSql = new StringBuilder(200)
				.append("SELECT increment ").append('\n')
				.append("FROM information_schema.sequences ").append('\n')
				.append("WHERE sequence_name ilike ? ");
		
		ResultSet rs = null;
		PreparedStatement stm = null;
		try {			
			stm =  session.connection().prepareStatement(sbSql.toString());
			try {
				stm.setString(1, getSequenceName());

				rs = stm.executeQuery();
				if (!rs.next())
					throw new HibernateException(String.format("Falha ao tentar obter a configuração da sequence %s", getSequenceName()));

				int allocSize = rs.getInt(1);
				if (allocSize!=allocationSize) {
					if (allocationSize!=-1)
						throw new HibernateException(String.format("O incremento da sequence %s deveria estar configurado para %d, mas está configurado para %d.", getSequenceName(), allocationSize, allocSize));
					allocationSize = allocSize;
					if (allocationSize!=1)
						Logger.getLogger(SequencePooledGenerator.class.getName()).log(Level.INFO, String.format("Usando o valor de incremento %d para a sequence %s, conforme definido na base de dados.", allocationSize, getSequenceName()));
				} else {
					Logger.getLogger(SequencePooledGenerator.class.getName()).log(Level.INFO, String.format("Verificamos que o incremento da sequence %s está adequadamente configurado para: %d", getSequenceName(), allocationSize));
				}

				if (rs.next())
					throw new HibernateException(String.format("Há mais de uma sequence com o nome: %s. Isto pode causar problemas. Defina um nome único para a sequence.", getSequenceName()));

			} finally {
				session.getTransactionCoordinator().getJdbcCoordinator().release(rs, stm);
			}

		} catch (SQLException ex) {
			throw new HibernateException(String.format("Erro SQL ao verificar se a sequence %s está adequadamente configurada.", getSequenceName()), ex);
		}
	}

	private boolean alterSequenceIncrementSize(SessionImplementor session) {
		StringBuilder sb = new StringBuilder(200)
				.append("alter sequence ").append(getSequenceName())
				.append(getSequenceSettings())
				.append(';');
		try {			
			PreparedStatement stm =  session.connection().prepareStatement(sb.toString());
			try {
				stm.execute();
			} finally {
				session.getTransactionCoordinator().getJdbcCoordinator().release(stm);
			}
			Logger.getLogger(SequencePooledGenerator.class.getName()).log(Level.INFO, "Sequence changed as:\n{0}.", sb);
			return true;
		} catch (SQLException ex) {
			Logger.getLogger(SequencePooledGenerator.class.getName()).log(Level.SEVERE, "Failed to alter sequence: {0}. Error: {1}", new Object[]{ex, sb});
		} catch (HibernateException ex) {
			Logger.getLogger(SequencePooledGenerator.class.getName()).log(Level.SEVERE, "Failed to alter sequence: {0}. Error: {1}", new Object[]{ex, sb});
		}
		return false;
	}
	
	private String getSequenceSettings() {
		return new StringBuilder(50)
				.append(" increment ").append(allocationSize)
				.append(" cache 1 ")
				.toString();
	}
	
}
