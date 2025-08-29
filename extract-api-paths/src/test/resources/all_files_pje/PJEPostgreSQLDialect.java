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

import java.sql.Types;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

/**
 * Customização do dialeto para permitir
 * comparações bitwise AND com o hibernate
 * @author Rodrigo Santos Menezes
 *
 */
public class PJEPostgreSQLDialect extends PostgreSQLDialect {

	public PJEPostgreSQLDialect() {
		super();
		registerColumnType(Types.JAVA_OBJECT, "json");
		registerColumnType(Types.JAVA_OBJECT, "jsonb");
		registerFunction("bitwise_and", new PJEPostgresSQLBitwiseAndSQLFunction("bitwise_and", StandardBasicTypes.INTEGER));
		registerFunction("full_text", new PJEPostgresSQLFullTextSQLFunction("full_text", StandardBasicTypes.BOOLEAN));
		registerFunction("replace", new StandardSQLFunction("regexp_replace", StandardBasicTypes.STRING) );
		registerFunction( "to_ascii", new StandardSQLFunction("to_ascii") );
	}
	
}
