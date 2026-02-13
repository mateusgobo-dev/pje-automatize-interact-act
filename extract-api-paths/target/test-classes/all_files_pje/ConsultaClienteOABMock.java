package br.jus.cnj.pje.nucleo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.trf.webservice.ConsultaClienteOAB;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;

@Name("consultaClienteOAB")
@Install(precedence = Install.FRAMEWORK)
@BypassInterceptors
public class ConsultaClienteOABMock extends ConsultaClienteOAB {

	private static final LogProvider log = Logging.getLogProvider(ConsultaClienteOAB.class);

	private static final Random rnd = new Random();

	private List<DadosAdvogadoOAB> dadosAdvogadoList;
	
	private static String[] ORGANIZACAO = { "Extra", "Carrefour", "Ford", "General Motors", "Wallmart", "Advogados Mineiros S.A.", "J & J Advocacia", "Universal Consultoria Juridica",
		"Oficina de Advogados", "Sindicato dos Advogados", "B. C. Consultoria Juridica", "M & N Advogados Trabalhistas", "Advocacia Ágil", "Justiça Juridica" };
	
	private static String[] CIDADES = { "Brasília", "Rio de Janeiro", "São Paulo", "Belo Horizonte", "Goiânia", "Salvador", "Campo Grande", "Cuiabá",
		"Palmas", "Vitória", "Manaus", "Porto Velhor", "Florianópolis", "Curitiba" };
	
	private static String[] UF = { "DF", "RJ", "SP", "MG", "GO", "BA", "MT", "MS",
		"TO", "ES", "AM", "SC", "RO", "CE" };
	
	private static String[] BAIRROS = { "Sol", "Mercúrio", "Vênus", "Terra", "Marte", "Júpiter", "Saturno", "Netuno",
		"Urano", "Plutão", "Ceres", "Éris", "Makemake", "Haumea" };

	private static String[] LOGRADOUROS = { "Sirius", "Canopus", "Arcturus", "Rigil Kentaurus", "Vega", "Rigel",
		"Procyon", "Betelgeuse", "Achernar", "Hadar", "Capela", "Altair", "Aldebaran", "Capela B", "Spica",
		"Antares", "Polux", "Fomalhaut", "Deneb", "Mimosa", "Regulus", "Acrux", "Adara", "Shaula", "Gacrux",
		"Belatrix", "El Nath", "Miaplacidus", "Alnilam", "Alnitak" };
	
	private static String[] CEPS = { "70450-55", "71010-043", "70680-500", "70070-600", "70097-900", "70070-900"};

	private static String[] NOMES = { "Canis", "Cephei", "Monocerotis", "Mu Cephei", "Cygni", "Sagittarli",
		"Betelgeuse", "Carinae", "Pegasi", "Hydrae", "Orionis", "Cassiopeiae", "Leporis", "Hercullis", "Tauri",
		"Mira", "Doradus", "Aquilae", "Titicans", "Pistol", "Coronae", "Puppis", "Lyrae", "Aurigae", "Omicron",
		"Gamma", "Deneb", "Superba", "Delta", "Zeta", "Velorum", "Persei", "Geminorum", "Mebsuta", "Enif", "Wezen",
		"Venaticorum", "Sadir", "Boütis", "Andromedae", "Scorpii", "Crucis", "Arae", "Peony", "Australis",
		"Camelopardalis", "Scuti" };

	private static String[] TIPO_INSCRICAO = { "ADVOGADO", "SUPLEMENTAR", "ESTAGIARIO"};
	
	private static String[] SITUACAO_INSCRICAO = { "CANCELADO","REGULAR"};

	@Override
	public void consultaDados(String cpf, boolean atualizar) throws Exception {
		cpf = StringUtil.removeNaoNumericos(cpf);
		if (atualizar) {
			try {
				dadosAdvogadoList = this.recuperaDadosFabricados(cpf);
				atualizarDados(dadosAdvogadoList, cpf);
			} catch (Exception e) {
				String msg = "Erro ao consultar no web service: " + e.getMessage();
				log.warn(msg, e);
				dadosAdvogadoList = consultaDadosBase(cpf);
				throw new Exception(e);
			}
		} else {
			dadosAdvogadoList = consultaDadosBase(cpf);
			if (dadosAdvogadoList == null || dadosAdvogadoList.size() == 0) {
				dadosAdvogadoList = this.recuperaDadosFabricados(cpf);
				atualizarDados(dadosAdvogadoList, cpf);
			}
		}
	}

	private void atualizarDados(List<DadosAdvogadoOAB> listAdvNovo, String pesqCPF) {
		EntityManager em = EntityUtil.getEntityManager();
		List<DadosAdvogadoOAB> listAdv = consultaDadosBase(pesqCPF);
		for (DadosAdvogadoOAB adv : listAdv) {
			em.remove(adv);
			EntityUtil.flush(em);
		}

		listAdv.clear();
		Date dataCadastro = new Date();
		for (DadosAdvogadoOAB adv : listAdvNovo) {
			adv.setDataCadastro(dataCadastro);
			em.persist(adv);
			EntityUtil.flush(em);
		}
	}

	@Override
	public List<DadosAdvogadoOAB> getDadosAdvogadoList() {
		return dadosAdvogadoList;
	}

	private List<DadosAdvogadoOAB> recuperaDadosFabricados(String cpf) {
		List<DadosAdvogadoOAB> ret = new ArrayList<DadosAdvogadoOAB>();
		Integer rndInt = rnd.nextInt(100000);		
		
		String nomePai = obtemNome();
		String nomeMae = obtemNome();
		String nomeAdv = obtemNome();
		
		for (int i = 0; i < rnd.nextInt(4) + 1; i++) {
			
			rndInt = rnd.nextInt(100000);
			DadosAdvogadoOAB dados = new DadosAdvogadoOAB();
			dados.setBairro(getRandomValue(BAIRROS));
			dados.setCep(getRandomValue(CEPS));
			dados.setCidade(getRandomValue(CIDADES));
			dados.setDataCadastro((new GregorianCalendar(2000, 0, 1)).getTime());
			dados.setDdd("61");			
			dados.setEmail("advmail@oab.com.br");
			dados.setLogadouro(getRandomValue(LOGRADOUROS));			
			dados.setNome(nomeAdv);
			dados.setNomeMae(nomePai);
			dados.setNomePai(nomeMae);			
			dados.setNumCPF(cpf);
			dados.setTelefone(Integer.toString(rnd.nextInt(10000)));
			dados.setNumSeguranca(rndInt.toString());
			dados.setOrganizacao(getRandomValue(ORGANIZACAO));
						
			dados.setNumInscricao(rndInt.toString());
			dados.setSituacaoInscricao("REGULAR");		
			dados.setTipoInscricao(getRandomValue(TIPO_INSCRICAO));
			dados.setUf(getRandomValue(UF));
			
			ret.add(dados);
		}
			
		return ret;
	}

	public static ConsultaClienteOAB instance() {
		return ComponentUtil.getComponent("consultaClienteOAB");
	}
	
	private String obtemNome() {
		StringBuilder sb = new StringBuilder();
		sb.append(getRandomValue(NOMES));
		sb.append(" ");
		sb.append(getRandomValue(NOMES));
		return sb.toString();
	}
	
	public String getRandomValue(String[] values) {
		return values[rnd.nextInt(values.length - 1)];
	}

}
