package br.com.infox.trf.webservice;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.MeasureTime;
import br.com.itx.util.ComponentUtil;
import br.gov.cjf.pj.webservice.CNPJ;
import br.gov.cjf.pj.webservice.WsConsultaCNPJ;
import br.gov.cjf.pj.webservice.WsConsultaCNPJ_Service;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaJuridica;

/**
 * 
 * @author Rodrigo Menezes
 * 
 */
@Name(ConsultaClienteReceitaPJCJF.NAME)
@BypassInterceptors
public class ConsultaClienteReceitaPJCJF extends ConsultaClienteReceitaPJ {

	private static final LogProvider log = Logging.getLogProvider(ConsultaClienteReceitaPJCJF.class);
	public static final String NAME = "consultaClienteReceitaPJCJF";
	private URL wsdl;
	private String pesqCNPJ;
	private int timeOut = 30;
	private String pNomeOrgao;
	private String pLoginUsuario;
	private String pNomeAplicacao;

	@Override
	public DadosReceitaPessoaJuridica consultaDados(String inscricao, String inscricaoConsulente, boolean forceUpdate) throws Exception {
		throw new UnsupportedOperationException("O método consultaDados(String, String, forceUpdate) ainda não foi implementado para esta classe.");
	}
	
	@Override
	public DadosReceitaPessoaJuridica consultaDados(String numeroCNPJ, boolean forceUpdate) throws Exception {
		String urlString = ParametroUtil.getFromContext("urlWsdlReceitaCnpj", true);
		pNomeOrgao = ParametroUtil.getFromContext("pNomeOrgaoWsReceita", true);
		pLoginUsuario = ParametroUtil.getFromContext("pLoginUsuarioWsReceita", true);
		pNomeAplicacao = ParametroUtil.getFromContext("pNomeAplicacaoWsReceita", true);

		if (Strings.isEmpty(urlString) || Strings.isEmpty(pNomeOrgao) || Strings.isEmpty(pLoginUsuario)
				|| Strings.isEmpty(pNomeAplicacao)) {
			throw new Exception("Parâmetro do Webservice não definido.");
		}
		wsdl = new URL(urlString);
		DadosReceitaPessoaJuridica dadosReceitaPessoaJuridica = null;
		this.pesqCNPJ = numeroCNPJ.replaceAll("\\.", "").replaceAll("-", "").replaceAll("/", "");
		if (forceUpdate) {
			try {
				dadosReceitaPessoaJuridica = consultaWebService(pesqCNPJ);
				atualizarDados(dadosReceitaPessoaJuridica, pesqCNPJ);
			} catch (Exception e) {
				dadosReceitaPessoaJuridica = consultaDadosBase(pesqCNPJ);
				if (dadosReceitaPessoaJuridica == null) {
					throw new WebserviceReceitaException(e.getMessage(), e);
				}
			}
		} else {
			dadosReceitaPessoaJuridica = consultaDadosBase(pesqCNPJ);
			if (dadosReceitaPessoaJuridica == null) {
				dadosReceitaPessoaJuridica = consultaWebService(pesqCNPJ);
				atualizarDados(dadosReceitaPessoaJuridica, pesqCNPJ);
			}
		}
		return dadosReceitaPessoaJuridica;
	}

	private DadosReceitaPessoaJuridica consultaWebService(String pesqCNPJ) throws Exception {
		if(!Authenticator.isLogouComCertificado()){
			throw new Exception("Não foi possível recuperar os dados de '"+pesqCNPJ+"' junto à Receita Federal.\n"+
		                        "Funcionalidade permitida apenas para usuários com certificado digital.");
		}		

		ExecutaServicoPj es = new ExecutaServicoPj();
		Thread t = new Thread(es);
		t.start();
		MeasureTime mt = new MeasureTime(true);
		while (t.isAlive()) {
			if ((mt.getTime() / 1000) > timeOut) {
				throw new WebserviceReceitaException("Problema de comunicação com a Receita Federal (timeout)");
			}
			Thread.sleep(200);
		}
		if (es.exception != null) {
			log.error(es.exception.getMessage(), es.exception);
			throw new WebserviceReceitaException("Problema de comunicação com a Receita Federal", es.exception);
		}
		CNPJ objCNPJSecurity = es.objCNPJSecurity;
		if (objCNPJSecurity == null) {
			throw new WebserviceReceitaException("Pessoa Jurídica não encontrada.");
		}
		return processaRespostaOBJ(objCNPJSecurity);
	}

	private DadosReceitaPessoaJuridica processaRespostaOBJ(CNPJ response) throws Exception {
		DadosReceitaPessoaJuridica dadoPessoaReceita = new DadosReceitaPessoaJuridica();

		System.out.println(response);

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		dadoPessoaReceita.setNumCNPJ(response.getCnpj());
		dadoPessoaReceita.setTipoMatrizFilial(response.getIdeMatrizFilial());
		dadoPessoaReceita.setRazaoSocial(response.getRazaoSocial());
		dadoPessoaReceita.setNomeFantasia(response.getNomeFantasia());
		dadoPessoaReceita.setTipoLogradouro(response.getTipoLogradouroPJ());
		dadoPessoaReceita.setDescricaoLogradouro(response.getLogradouroPJ());
		dadoPessoaReceita.setNumLogradouro(response.getNumLogradouroPJ());
		dadoPessoaReceita.setDescricaoComplemento(response.getComplementoPJ());
		dadoPessoaReceita.setDescricaoBairro(response.getBairroPJ());
		dadoPessoaReceita.setNumCep(response.getCepPJ());
		dadoPessoaReceita.setCodigoMunicipio(response.getCodMunicipioPJ());
		dadoPessoaReceita.setDescricaoMunicipio(response.getMunicipioPJ());
		dadoPessoaReceita.setSiglaUf(response.getSiglaUFPJ());
		dadoPessoaReceita.setNumDdd1(response.getDddTelefonePJ1());
		dadoPessoaReceita.setNumTelefone1(response.getNumTelefonePJ1());
		dadoPessoaReceita.setNumDdd2(response.getDddTelefonePJ2());
		dadoPessoaReceita.setNumTelefone2(response.getNumTelefonePJ2());
		dadoPessoaReceita.setNumDddFax(response.getDddTelefonePJFAX());
		dadoPessoaReceita.setNumFax(response.getNumTelefonePJFAX());
		dadoPessoaReceita.setCorreioEletronico(response.getCorreioEletronicoPJ());
		dadoPessoaReceita.setInSocio(response.getIndSocio());
		dadoPessoaReceita.setCodigoCnaeFiscal(response.getCnaeFiscal());
		dadoPessoaReceita.setDescricaoCnaeFiscal(response.getDesCnaeFiscal());
		dadoPessoaReceita.setCodigoNaturezaJuridica(response.getCodNaturezaJuridica());
		dadoPessoaReceita.setDescricaoNaturezaJuridica(response.getDesNaturezaJuridica());

		try {
			dadoPessoaReceita.setDataRegistro(df.parse(response.getDataAberturaPJ()));
		} catch (ParseException e) {
			dadoPessoaReceita.setDataRegistro(null);
		}

		try {
			dadoPessoaReceita.setDataSituacaoCnpj(df.parse(response.getDataSituacaoCNPJ()));
		} catch (ParseException e) {
			dadoPessoaReceita.setDataSituacaoCnpj(null);
		}
		dadoPessoaReceita.setStatusCadastralPessoaJuridica(response.getCodSituacaoCadastral());
		// dadoPessoaReceita.setDescricaoSituacaoCadastral(response.getDesSituacaoCadastral());
		dadoPessoaReceita.setNumNire(response.getNire());
		dadoPessoaReceita.setNumCpfResponsavel(response.getCpfResponsavel());
		dadoPessoaReceita.setNomeResponsavel(response.getNomeResponsavel());
		dadoPessoaReceita.setTipoLogradouroResponsavel(response.getTipoLogradouroResponsavel());
		dadoPessoaReceita.setDescricaoLogradouroResponsavel(response.getLogradouroResponsavel());
		dadoPessoaReceita.setNumLogradouroResponsavel(response.getNumLogradouroResponsavel());
		dadoPessoaReceita.setDescricaoComplementoResponsavel(response.getComplementoResponsavel());
		dadoPessoaReceita.setDescricaoBairroResponsavel(response.getBairroResponsavel());
		dadoPessoaReceita.setNumCepResponsavel(response.getCepResponsavel());
		dadoPessoaReceita.setCodigoMunicipioResponsavel(response.getCodMunicipioResponsavel());
		dadoPessoaReceita.setDescricaoMunicipioResponsavel(response.getMunicipioResponsavel());
		dadoPessoaReceita.setCodigoUfResponsavel(response.getSiglaUFResponsavel());
		dadoPessoaReceita.setNumDddTelefoneResponsavel(response.getDddTelefoneResponsavel());
		dadoPessoaReceita.setCorreioEletronicoResponsavel(response.getCorreioEletronicoResponsavel());
		dadoPessoaReceita.setCodigoQualificacaoResponsavel(response.getCodQualificacaoResponsavel());
		dadoPessoaReceita.setDescricaoQualificacaoResponsavel(response.getDesQualificacaoResponsavel());
		dadoPessoaReceita.setCodigoSituacaoAtualizacao(response.getSituacaoAtualizacao());
		return dadoPessoaReceita;
	}

	public static void main(String[] args) throws Exception {
		String cnpj = "15123946000112";
		WsConsultaCNPJ_Service service1 = new WsConsultaCNPJ_Service(new URL(
				"http://172.31.3.215:7778/wsConsultaCNPJ/wsConsultaCNPJSoapHttpPort?wsdl"));
		WsConsultaCNPJ port1 = service1.getWsConsultaCNPJSoapHttpPort();
		String dadosCNPJSecurity = port1.getDadosCNPJSecurity(cnpj, "TRF5", "TRF5", "PJE");
		System.out.println("Dados: " + dadosCNPJSecurity);
	}

	public static ConsultaClienteReceitaPJCJF instance() {
		return ComponentUtil.getComponent(ConsultaClienteReceitaPJCJF.NAME);
	}

	private class ExecutaServicoPj implements Runnable {

		private CNPJ objCNPJSecurity;
		private Exception exception;

		@Override
		public void run() {
			try {
				WsConsultaCNPJ_Service service1 = new WsConsultaCNPJ_Service(wsdl);
				WsConsultaCNPJ port1 = service1.getWsConsultaCNPJSoapHttpPort();
				this.objCNPJSecurity = port1.getObjectCNPJSecurity(pesqCNPJ, pNomeOrgao, pLoginUsuario, pNomeAplicacao);
			} catch (Exception e) {
				exception = e;
			}
		}

	}

}
