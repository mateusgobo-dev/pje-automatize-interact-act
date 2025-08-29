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

import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.type.StandardBasicTypes;

/**
 * Customização do dialeto para permitir
 * comparações bitwise AND com o hibernate
 * @author Rodrigo Santos Menezes
 *
 */
public class PJEOracleDialect extends Oracle10gDialect{
	
	public PJEOracleDialect() {
		super();
		registerFunction("bitwise_and", new PJEOracleBitwiseAndSQLFunction("bitwise_and", StandardBasicTypes.INTEGER));
	}

}
