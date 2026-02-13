/**
 * pje-web
 * Copyright (C) 2009-2014 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.util;

import java.util.List;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * Implementação de função AND bit a bit para
 * comparações com finalidade de julgar perfil ativo
 * ou inantivo de pesssoa
 * @author Rodrigo Santos Menezes
 *
 */
public class PJEPostgresSQLBitwiseAndSQLFunction extends StandardSQLFunction implements SQLFunction{
	
	public PJEPostgresSQLBitwiseAndSQLFunction(String name) {
		// TODO Auto-generated constructor stub
		super(name);
	}
	
	public PJEPostgresSQLBitwiseAndSQLFunction(String name, Type type) {
		// TODO Auto-generated constructor stub
		super(name,type);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public String render(Type firstArgumentType, List args, SessionFactoryImplementor sessionFactory) {
		if(args.size() != 2){
			throw new IllegalArgumentException("A função deve conter dois argumentos.");
		}
		StringBuffer sb = new StringBuffer(args.get(0).toString());
		sb.append(" & ").append(args.get(1));

		return sb.toString();
	}
	
}
