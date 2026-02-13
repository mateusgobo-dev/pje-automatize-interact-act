/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.trf.webservice.WebserviceReceitaException;
import br.jus.cnj.pje.webservice.client.ConsultaClienteReceitaPFCNJ;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;

/**
 * @author cristof
 * 
 */
@Name(ConsultaClienteReceitaPFCNJ.NAME)
@Install(precedence = Install.FRAMEWORK)
@Scope(ScopeType.STATELESS)
public class ConsultaClienteReceitaPFMock extends ConsultaClienteReceitaPFCNJ {

	private static Random random = new Random();

	private static int MAX_ID_CEP = 740000;

	private static int MAX_NUM_TIT = 999999999;

	private static String[] BAIRROS = { "Sol", "Mercúrio", "Vênus", "Terra", "Marte", "Júpiter", "Saturno", "Netuno",
			"Urano", "Plutão", "Ceres", "Éris", "Makemake", "Haumea" };

	private static String[] LOGRADOUROS = { "Sirius", "Canopus", "Arcturus", "Rigil Kentaurus", "Vega", "Rigel",
			"Procyon", "Betelgeuse", "Achernar", "Hadar", "Capela", "Altair", "Aldebaran", "Capela B", "Spica",
			"Antares", "Polux", "Fomalhaut", "Deneb", "Mimosa", "Regulus", "Acrux", "Adara", "Shaula", "Gacrux",
			"Belatrix", "El Nath", "Miaplacidus", "Alnilam", "Alnitak" };

	private static String[] NOMES = { "Canis", "Cephei", "Monocerotis", "Mu Cephei", "Cygni", "Sagittarli",
			"Betelgeuse", "Carinae", "Pegasi", "Hydrae", "Orionis", "Cassiopeiae", "Leporis", "Hercullis", "Tauri",
			"Mira", "Doradus", "Aquilae", "Titicans", "Pistol", "Coronae", "Puppis", "Lyrae", "Aurigae", "Omicron",
			"Gamma", "Deneb", "Superba", "Delta", "Zeta", "Velorum", "Persei", "Geminorum", "Mebsuta", "Enif", "Wezen",
			"Venaticorum", "Sadir", "Boütis", "Andromedae", "Scorpii", "Crucis", "Arae", "Peony", "Australis",
			"Camelopardalis", "Scuti" };

	private static String[] TIPO_LOGRADOUROS = { "Rua", "Avenida", "Travessa", "Largo", "" };

	@In(create = true)
	private CepService cepService;
	
	@Override
	public DadosReceitaPessoaFisica consultaDados(String inscricao, String inscricaoConsulente, boolean forceUpdate) throws Exception {
		return consultaDados(inscricao, forceUpdate);
	}

	@Override
	public DadosReceitaPessoaFisica consultaDados(String cpf, boolean forceUpdate) {
		DadosReceitaPessoaFisica dados = new DadosReceitaPessoaFisica();
		Calendar cal = new GregorianCalendar(1900 + random.nextInt(91), random.nextInt(11), random.nextInt(28));
		dados.setDataNascimento(cal.getTime());
		cal.add(Calendar.YEAR, 15);
		cal.add(Calendar.DAY_OF_YEAR, random.nextInt(365));
		dados.setDataAtualizacao(cal.getTime());
		dados.setNome(obtemNome());
		dados.setNomeMae(obtemNome());
		dados.setNumCPF(cpf);
		dados.setNumTituloEleitor(String.format("%012d", random.nextInt(MAX_NUM_TIT)));
		dados.setSexo(random.nextInt(100) % 2 == 0 ? "2" : "1");
		dados.setSituacaoCadastral("0");
		carregaEnderecoAleatorio(dados);
		return dados;
	}

	private void carregaEnderecoAleatorio(DadosReceitaPessoaFisica dados) {
		Cep cep = null;
		while (cep == null){
			int idCep = random.nextInt(MAX_ID_CEP);
			cep = cepService.findById(idCep);
		}
		dados.setBairro(cep.getNomeBairro());
		dados.setComplemento(cep.getComplemento());
		dados.setLogradouro(cep.getNomeLogradouro());
		dados.setMunicipio(cep.getMunicipio().getMunicipio());
		dados.setNumCEP(cep.getNumeroCep().replaceAll("-", ""));
		dados.setNumLogradouro(Integer.toString(random.nextInt(10000)));
		dados.setSiglaUF(cep.getMunicipio().getEstado().getCodEstado());
		dados.setTipoLogradouro(getRandomValue(TIPO_LOGRADOUROS));
		if (dados.getBairro() == null) {
			dados.setBairro(getRandomValue(BAIRROS));
		}
		if (dados.getLogradouro() == null) {
			dados.setLogradouro("Rua " + getRandomValue(LOGRADOUROS));
		}
		return;
	}

	private String obtemNome() {
		StringBuilder sb = new StringBuilder();
		sb.append(getRandomValue(NOMES));
		sb.append(" ");
		sb.append(getRandomValue(NOMES));
		return sb.toString();
	}

	public String getRandomValue(String[] values) {
		return values[random.nextInt(values.length - 1)];
	}

	public DadosReceitaPessoaFisica getDadosReceitaPessoaFisicaSemAtualizarBaseDeDados(String cpf) throws WebserviceReceitaException, Exception {
		return consultaDados(cpf, true);
	}
}
