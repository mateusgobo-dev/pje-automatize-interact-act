/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.webservice.client.ConsultaClienteReceitaPJCNJ;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaJuridica;
import br.jus.pje.ws.externo.srfb.util.SituacaoCadastroPessoaJuridicaReceita;

/**
 * @author cristof
 *
 */
@Name(ConsultaClienteReceitaPJCNJ.NAME)
@Scope(ScopeType.EVENT)
@Install(precedence=Install.FRAMEWORK)
public class ConsultaClienteReceitaPJMock extends ConsultaClienteReceitaPJCNJ {
	
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
	public DadosReceitaPessoaJuridica consultaDados(String inscricao, String inscricaoConsulente, boolean forceUpdate) throws Exception {
		return consultaDados(inscricao, forceUpdate);
	}

	@Override
	public DadosReceitaPessoaJuridica consultaDados(String numeroCNPJ, boolean forceUpdate) throws Exception {
		DadosReceitaPessoaJuridica dados = new DadosReceitaPessoaJuridica();
		Calendar cal = new GregorianCalendar(1900 + random.nextInt(91), random.nextInt(11), random.nextInt(28));
		dados.setAtivo(true);
		dados.setDataAtualizacao(new Date());
		dados.setDataRegistro(cal.getTime());
		dados.setNomeFantasia(obtemNome());
		dados.setNomeResponsavel(obtemNome());
		dados.setDataSituacaoCnpj(new Date());
		dados.setNumCNPJ(numeroCNPJ);
		dados.setStatusCadastralPessoaJuridica(SituacaoCadastroPessoaJuridicaReceita.ATIVA);
		dados.setRazaoSocial(obtemNome());
		dados.setCodigoNaturezaJuridica(obtemCodigoNatureza());
		carregaEnderecoAleatorio(dados);
		return dados;
	}
	
	private String obtemCodigoNatureza(){
		String[] codigos = {"1015","1040","1074","1023","1058","1082","1031","1066","1104","1139","1163","1112",
				"1147","1171","1120","1155","1180","1198","1201","1210","2011","2038","2046","2054","2062","2070",
				"2089","2097","2127","2143","2151","2160","2224","2232","2240","2259","2267","2283","2291","2135",
				"2305","2313","2275","2178","2194","2216","3034","3069","3077","3085","3107","3115","3123","3131",
				"3220","3239","3247","3999","3204","3212","5010","5029","5037"};
		return codigos[random.nextInt(codigos.length)];
	}
	
	private void carregaEnderecoAleatorio(DadosReceitaPessoaJuridica dados) {
		Cep cep = null;
		while (cep == null){
			int idCep = random.nextInt(MAX_ID_CEP);
			cep = cepService.findById(idCep);
		}
		dados.setDescricaoBairro(cep.getNomeBairro());
		dados.setDescricaoComplemento(cep.getComplemento());
		dados.setDescricaoLogradouro(cep.getNomeLogradouro());
		dados.setDescricaoMunicipio(cep.getMunicipio().getMunicipio());
		dados.setNumCep(cep.getNumeroCep().replaceAll("-", ""));
		dados.setNumLogradouro(Integer.toString(random.nextInt(10000)));
		dados.setSiglaUf(cep.getMunicipio().getEstado().getCodEstado());
		dados.setTipoLogradouro(getRandomValue(TIPO_LOGRADOUROS));
		if (dados.getDescricaoBairro() == null) {
			dados.setDescricaoBairro(getRandomValue(BAIRROS));
		}
		if (dados.getDescricaoLogradouro() == null) {
			dados.setDescricaoLogradouro("Rua " + getRandomValue(LOGRADOUROS));
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

}
