package br.jus.csjt.pje.commons.util.dejt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import br.jus.csjt.pje.commons.util.dejt.ItemMateriaType.Processos;
import br.jus.csjt.pje.commons.util.dejt.ItemMateriaType.Texto;
import br.jus.csjt.pje.commons.util.dejt.ParteType.Advogados;

public class MateriaDejt {

	private static ObjectFactory objectFactory = new ObjectFactory();

	public void main(String[] args) {

		criaMateria();

	}

	public static void criaMateria() {

		// Cria Materia
		MateriaType materia1 = MateriaDejt.criarMateria("1", "1", "Titulo", "UNIDADE");

		// Cria Item Materia Texto

		ItemMateriaType itemMateriaTexto1 = MateriaDejt.criarItemMateriaTexto(1, 1, "<>Texto a ser formado1");
		materia1.getItemMateria().add(itemMateriaTexto1);

		// Cria Item Materia Processo

		ItemMateriaType itemMateriaProcesso1 = MateriaDejt.criarItemMateriaProcessoNumeracaoAntiga("AIRR",
				new BigInteger("37"), new BigInteger("2006"), new BigInteger("013"), new BigInteger("08"),
				new BigInteger("40"), "9");
		materia1.getItemMateria().add(itemMateriaProcesso1);

		AdvogadoType criarAdvogadotype1 = MateriaDejt.criarAdvogadotype("1", "2", "DR");
		AdvogadoType criarAdvogadotype2 = MateriaDejt.criarAdvogadotype("3", "4", "JR");

		List<AdvogadoType> listaAdvogados = new ArrayList<AdvogadoType>();
		listaAdvogados.add(criarAdvogadotype1);
		listaAdvogados.add(criarAdvogadotype2);

		ParteType criarParteType = MateriaDejt.criarParteType("NomePArte", "DR", listaAdvogados);

		itemMateriaProcesso1.getProcessos().getProcesso().get(0).getParte().add(criarParteType);

		MateriasType createMateriasType = MateriaDejt.objectFactory.createMateriasType();
		createMateriasType.getMateria().add(materia1);

		try {
			JAXBElement<MateriasType> materias = MateriaDejt.objectFactory.createMaterias(createMateriasType);

			JAXBContext jc = JAXBContext.newInstance("br.jus.pje.jt.util.dejt");
			Marshaller m = jc.createMarshaller();
			m.marshal(materias, System.out);

		} catch (JAXBException jbe) {

			jbe.printStackTrace();
		}

	}

	public static JAXBElement<MateriasType> criarMateriasJaxbElement(MateriasType materiasType) {

		return MateriaDejt.objectFactory.createMaterias(materiasType);
	}

	public static MateriasType criarMaterias() {

		return MateriaDejt.objectFactory.createMateriasType();
	}

	public static MateriaType criarMateria(String codOrigemMateria, String tipo, String titulo,
			String unidadePublicadora) {

		MateriaType createMateriaType = objectFactory.createMateriaType();

		createMateriaType.setCodOrigemMateria(codOrigemMateria);
		createMateriaType.setTipo(tipo);
		createMateriaType.setTitulo(titulo);
		createMateriaType.setUnidadePublicadora(unidadePublicadora);

		return createMateriaType;

	}

	public static ItemMateriaType criarItemMateriaTexto(int alinhamento, int tipoConteudo, String conteudo) {

		ParagrafoType createParagrafoType = objectFactory.createParagrafoType();
		createParagrafoType.setAlinhamento(alinhamento);
		createParagrafoType.setTipoConteudo(tipoConteudo);
		createParagrafoType.setConteudo(conteudo);

		Texto createItemMateriaTypeTexto = objectFactory.createItemMateriaTypeTexto();
		createItemMateriaTypeTexto.getParagrafo().add(createParagrafoType);

		ItemMateriaType createItemMateriaType = objectFactory.createItemMateriaType();
		createItemMateriaType.setTexto(createItemMateriaTypeTexto);

		return createItemMateriaType;
	}

	public static ItemMateriaType criarItemMateriaProcessoNumeracaoAntiga(String classe, BigInteger num,
			BigInteger ano, BigInteger vara, BigInteger tribunal, BigInteger sequecial, String digito) {

		ProcessoType createProcessoType = objectFactory.createProcessoType();
		createProcessoType.setClasse(classe);
		createProcessoType.setNum(num);
		createProcessoType.setAno(ano);
		createProcessoType.setVara(vara);
		createProcessoType.setTribunal(tribunal);
		createProcessoType.setSequencial(sequecial);
		createProcessoType.setDigito(digito);

		Processos createItemMateriaTypeProcessos = objectFactory.createItemMateriaTypeProcessos();
		createItemMateriaTypeProcessos.getProcesso().add(createProcessoType);

		ItemMateriaType createItemMateriaType = objectFactory.createItemMateriaType();
		createItemMateriaType.setProcessos(createItemMateriaTypeProcessos);

		return createItemMateriaType;
	}

	public static ItemMateriaType criarItemMateriaProcessoNumeracaoUnica(String classe, BigInteger num, BigInteger ano,
			BigInteger vara, BigInteger tribunal, BigInteger numeroUnico, BigInteger digitoUnico, String relator) {

		ProcessoType createProcessoType = objectFactory.createProcessoType();
		createProcessoType.setClasse(classe);
		createProcessoType.setNum(num);
		createProcessoType.setAno(ano);
		createProcessoType.setVara(vara);
		createProcessoType.setTribunal(tribunal);
		createProcessoType.setSequencial(new BigInteger("0"));
		createProcessoType.setDigito("0");
		createProcessoType.setNumUnico(numeroUnico);
		createProcessoType.setDigitoUnico(digitoUnico);
		createProcessoType.setAnoUnico(ano);
		createProcessoType.setTribunalUnico(tribunal);
		createProcessoType.setOrigemUnico(vara);
		if (relator != null && !relator.isEmpty()) {
			createProcessoType.setRelator(relator);
		}

		Processos createItemMateriaTypeProcessos = objectFactory.createItemMateriaTypeProcessos();
		createItemMateriaTypeProcessos.getProcesso().add(createProcessoType);

		ItemMateriaType createItemMateriaType = objectFactory.createItemMateriaType();
		createItemMateriaType.setProcessos(createItemMateriaTypeProcessos);

		return createItemMateriaType;
	}

	public static ItemMateriaType criarItemMateriaProcessoNumeracaoUnica(String classe, BigInteger ano,
			BigInteger vara, BigInteger tribunalUnico, BigInteger numeroUnico, BigInteger digitoUnico, String relator) {

		ProcessoType createProcessoType = objectFactory.createProcessoType();
		createProcessoType.setClasse(classe);
		createProcessoType.setNumUnico(numeroUnico);
		createProcessoType.setDigitoUnico(digitoUnico);
		createProcessoType.setAnoUnico(ano);
		createProcessoType.setTribunalUnico(tribunalUnico);
		createProcessoType.setOrigemUnico(vara);

		if (relator != null && !relator.isEmpty()) {
			createProcessoType.setRelator(relator);
		}

		Processos createItemMateriaTypeProcessos = objectFactory.createItemMateriaTypeProcessos();
		createItemMateriaTypeProcessos.getProcesso().add(createProcessoType);

		ItemMateriaType createItemMateriaType = objectFactory.createItemMateriaType();
		createItemMateriaType.setProcessos(createItemMateriaTypeProcessos);

		return createItemMateriaType;
	}

	public static AdvogadoType criarAdvogadotype(String numOAB, String nome, String titulo) {

		AdvogadoType createAdvogadoType = objectFactory.createAdvogadoType();

		createAdvogadoType.setNumOAB(numOAB);
		createAdvogadoType.setNome(nome);
		createAdvogadoType.setTitulo(titulo);

		return createAdvogadoType;

	}

	public static ParteType criarParteType(String nome, String titulo, List<AdvogadoType> listaAdvogados) {

		ParteType createParteType = objectFactory.createParteType();

		/*
		 * PJE-JT: Ricardo Scholz : PJEII-3426 - 2012-12-04 Alteracoes feitas pela JT.
		 * Inclusão de '<![CDATA[...]]>' para isolar caracteres especiais nos nomes
		 * das partes e dos advogados.
		 */
		createParteType.setNome("<![CDATA[" + nome + "]]>");
		createParteType.setTitulo("<![CDATA[" + titulo + "]]>");
		/*
		 * PJE-JT: Fim.
		 */

		if (listaAdvogados != null && listaAdvogados.size() > 0) {

			Advogados createParteTypeAdvogados = objectFactory.createParteTypeAdvogados();

			createParteTypeAdvogados.getAdvogado().addAll(listaAdvogados);

			createParteType.setAdvogados(createParteTypeAdvogados);

		}

		return createParteType;

	}

}
