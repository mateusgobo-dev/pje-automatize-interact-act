package br.jus.cnj.pje.util;
import java.util.List;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

public class PJEPostgresSQLFullTextSQLFunction extends StandardSQLFunction implements SQLFunction{
	
	public PJEPostgresSQLFullTextSQLFunction(String name) {
		super(name);
	}
	
	public PJEPostgresSQLFullTextSQLFunction(String name, Type type) {
		super(name,type);
	}

	@Override
	public String render(Type firstArgumentType, List args, SessionFactoryImplementor factory) {
		if (args.size() != 2) {
			throw new IllegalArgumentException(
					"A função deve conter dois argumentos.");
		}

		String field = (String) args.get(0);
		String value = (String) args.get(1);
/**
 * Issue PJEII-24414
 *  adicionado a função to_ascii para ignorar acentuação em uma pesquisa.
 */
		String fragment = "to_tsvector('portuguese', to_ascii("+field+")) @@ plainto_tsquery('portuguese', to_ascii("+value+"))";
		return fragment;

	}

}