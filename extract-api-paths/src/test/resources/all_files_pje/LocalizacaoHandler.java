package br.com.infox.ibpm.expression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

public class LocalizacaoHandler extends GenericExpression {

	private Set<String> localizacaoSet = new HashSet<String>();
	private Set<String> papelSet = new HashSet<String>();

	private final static String LOCALIZACAO_SQL = "select ds_localizacao from tb_localizacao where id_localizacao = ?";
	private final static String PAPEL_SQL = "select ds_identificador from tb_papel where id_papel = ?";

	@Override
	public void execute() {
		List<String> params = parseParameters();
		for (String s : params) {
			String local = s;
			if (s.contains(":")) {
				local = s.split(":")[0];
				papelSet.add(s.split(":")[1]);
			}
			localizacaoSet.add(local);
		}
	}

	@Override
	public Element getXml() {
		Element root = new Element("localizacaoAssignment");
		root.addContent(getLocalizacoes());
		root.addContent(getPapeis());
		return root;
	}

	private Element getPapeis() {
		Element papeis = new Element("papeis");
		getContent(papeis, "papel", papelSet, PAPEL_SQL);
		return papeis;
	}

	private Element getLocalizacoes() {
		Element localizacoes = new Element("localizacoes");
		getContent(localizacoes, "localizacao", localizacaoSet, LOCALIZACAO_SQL);
		return localizacoes;
	}

}
