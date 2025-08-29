package br.com.infox.trf.webservice;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.MeasureTime;
import br.com.itx.util.ComponentUtil;
import br.gov.cjf.webservice.WsConsultaCPF;
import br.gov.cjf.webservice.WsConsultaCPF_Service;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;

/**
 * 
 * @author Rodrigo Menezes
 * 
 */
@Name(ConsultaClienteReceitaPFCJF.NAME)
@BypassInterceptors
public class ConsultaClienteReceitaPFCJF extends ConsultaClienteReceitaPF {

	private static final LogProvider log = Logging.getLogProvider(ConsultaClienteReceitaPFCJF.class);
	public static final String NAME = "consultaClienteReceitaPFCJF";
	private URL wsdl;
	private String pesqCPF;
	private int timeOut = 30;
	private String pNomeOrgao;
	private String pLoginUsuario;
	private String pNomeAplicacao;
	
	@Override
	public DadosReceitaPessoaFisica consultaDados(String inscricao, String inscricaoConsulente, boolean forceUpdate) throws Exception {
		throw new UnsupportedOperationException("Esta implementação não suporta o método consultaDados(String, String, boolean).");
	}

	@Override
	public DadosReceitaPessoaFisica consultaDados(String numeroCPF, boolean forceUpdate) throws Exception {		
		pNomeOrgao = ParametroUtil.getFromContext("pNomeOrgaoWsReceita", true);
		pLoginUsuario = ParametroUtil.getFromContext("pLoginUsuarioWsReceita", true);
		pNomeAplicacao = ParametroUtil.getFromContext("pNomeAplicacaoWsReceita", true);

		String urlString = ParametroUtil.getFromContext("urlWsdlReceita", true);
		if (Strings.isEmpty(urlString) || Strings.isEmpty(pNomeOrgao) || Strings.isEmpty(pLoginUsuario)
				|| Strings.isEmpty(pNomeAplicacao)) {
			throw new Exception("Parâmetros do Webservice não definidos.");
		}
		wsdl = new URL(urlString);
		DadosReceitaPessoaFisica dadosReceitaPessoaFisica = null;
		this.pesqCPF = numeroCPF.replaceAll("\\.", "").replaceAll("-", "");
		if (forceUpdate) {
			try {
				dadosReceitaPessoaFisica = consultaWebService(pesqCPF);
				atualizarDados(dadosReceitaPessoaFisica, pesqCPF);
			} catch (Exception e) {
				dadosReceitaPessoaFisica = consultaDadosBase(pesqCPF);
				if (dadosReceitaPessoaFisica == null) {
					throw new WebserviceReceitaException(e.getMessage(), e);
				}
			}
		} else {
			dadosReceitaPessoaFisica = consultaDadosBase(pesqCPF);
			if (dadosReceitaPessoaFisica == null) {
				dadosReceitaPessoaFisica = consultaWebService(pesqCPF);
				atualizarDados(dadosReceitaPessoaFisica, pesqCPF);
			}
		}
		return dadosReceitaPessoaFisica;
	}

	private DadosReceitaPessoaFisica consultaWebService(String pesqCPF) throws Exception {
		if(!Authenticator.isLogouComCertificado()){
			throw new Exception("Não foi possível recuperar os dados de '"+pesqCPF+"' junto à Receita Federal.\n"+
		                        "Funcionalidade permitida apenas para usuários com certificado digital.");
		}

		ExecutaServicoPf es = new ExecutaServicoPf();
		Thread t = new Thread(es);
		t.start();
		MeasureTime mt = new MeasureTime(true);
		while (t.isAlive()) {
			if ((mt.getTime() / 1000) > timeOut) {
				throw new WebserviceReceitaException("Problema de comunicação com a Receita Federal (timeout)");
			}
			Thread.sleep(200);
		}
		String dadosCPFSecurity = es.dadosCPFSecurity;
		if (es.exception != null) {
			log.error(es.exception.getMessage(), es.exception);
			throw new WebserviceReceitaException("Problema de comunicação com a Receita Federal", es.exception);
		}
		if (Strings.isEmpty(dadosCPFSecurity)) {
			throw new Exception("Pessoa Fisica não encontrada na Receita Federal.");
		}
		return processaResposta(dadosCPFSecurity);
	}

	private String adicionarMascaraTitulo(String ret) {
		if (ret == null || ret.length() != 12)
			return ret;
		String p1 = ret.substring(0, 4);
		String p2 = ret.substring(4, 8);
		String p3 = ret.substring(8, 12);
		return p1 + "." + p2 + "." + p3;
	}

	private DadosReceitaPessoaFisica processaResposta(String response) throws Exception {
		DadosReceitaPessoaFisica dadoPessoaReceita = new DadosReceitaPessoaFisica();
		String[] split = response.split(";");
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		dadoPessoaReceita.setNumCPF(split[0]);
		dadoPessoaReceita.setNome(split[1]);
		try {
			dadoPessoaReceita.setDataNascimento(df.parse(split[2]));
		} catch (ParseException e) {
			dadoPessoaReceita.setDataNascimento(null);
		}
		dadoPessoaReceita.setSexo(split[3]);
		dadoPessoaReceita.setNomeMae(split[4]);
		if (split[5].length() > 1) {
			long numTitulo = Long.parseLong(split[5]);
			dadoPessoaReceita.setNumTituloEleitor(adicionarMascaraTitulo(NumeroProcessoUtil
					.completaZeros(numTitulo, 12)));
		}
		dadoPessoaReceita.setTipoLogradouro(split[6]);
		dadoPessoaReceita.setLogradouro(split[7]);
		dadoPessoaReceita.setNumLogradouro(split[8]);
		dadoPessoaReceita.setComplemento("null".equals(split[9]) ? null : split[9]);
		dadoPessoaReceita.setBairro(split[10]);
		dadoPessoaReceita.setMunicipio(split[11]);
		dadoPessoaReceita.setSiglaUF(split[12]);
		dadoPessoaReceita.setNumCEP(split[13]);
		dadoPessoaReceita.setSituacaoCadastral(split[14]);
		return dadoPessoaReceita;
	}

	public static void main(String[] args) throws Exception {
		String cpf = "01601973500";

		WsConsultaCPF_Service service1 = new WsConsultaCPF_Service(new URL(
				"http://172.31.3.215:7778/wsConsultaCPF/wsConsultaCPFSoapHttpPort?wsdl"));
		WsConsultaCPF port1 = service1.getWsConsultaCPFSoapHttpPort();
		String dadosCPFSecurity = port1.getDadosCPFSecurity(cpf, "TRF5", "TRF5", "PJE");
		System.out.println(dadosCPFSecurity);

	}

	public static ConsultaClienteReceitaPFCJF instance() {
		return ComponentUtil.getComponent(ConsultaClienteReceitaPFCJF.NAME);
	}

	private class ExecutaServicoPf implements Runnable {

		private String dadosCPFSecurity;
		private Exception exception;

		@Override
		public void run() {
			try {
				WsConsultaCPF_Service service1 = new WsConsultaCPF_Service(wsdl);
				WsConsultaCPF port1 = service1.getWsConsultaCPFSoapHttpPort();
				this.dadosCPFSecurity = port1.getDadosCPFSecurity(pesqCPF, pNomeOrgao, pLoginUsuario, pNomeAplicacao);
			} catch (Exception e) {
				exception = e;
			}
		}

	}

}