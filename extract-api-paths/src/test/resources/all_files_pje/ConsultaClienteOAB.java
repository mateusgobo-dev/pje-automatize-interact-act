package br.com.infox.trf.webservice;

import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.exception.AdvogadoNaoEncontradoException;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.webservices.consultaoab.ConsultaAdvogado;
import br.com.infox.pje.webservices.consultaoab.ConsultaAdvogadoPorCpf;
import br.com.infox.pje.webservices.consultaoab.ConsultaAdvogadoPorCpfResponse;
import br.com.infox.pje.webservices.consultaoab.ConsultaAdvogadoResponse;
import br.com.infox.pje.webservices.consultaoab.ObjectFactory;
import br.com.itx.component.MeasureTime;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.DadosAdvogadoOABManager;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.nucleo.util.Utf8ParaIso88591Util;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;
import br.org.oab.www5.cnaws.Authentication;
import br.org.oab.www5.cnaws.Service;
import br.org.oab.www5.cnaws.ServiceSoap;
import br.org.oab.www5.cnaws.xmlobj.AdvogadoData;
import br.org.oab.www5.cnaws.xmlobj.ArrayOfAdvogadoData;

/**
 * 
 * @author Rodrigo Menezes
 * 
 */
@Name("consultaClienteOAB")
@BypassInterceptors
public class ConsultaClienteOAB {

	private static final LogProvider log = Logging.getLogProvider(ConsultaClienteOAB.class);
	private List<DadosAdvogadoOAB> dadosAdvogadoList;
	private URL wsdl;
	private String pesqCPF;
	private String nrOAB;
	private String ufOAB;
	private int timeOut = 100;
	private String paramOABKey;
	
	
	public void consultaDados(String pesqCPF, boolean forceUpdate, Boolean isLogouComCertificado) throws Exception {
		consultaDados(pesqCPF, null, null, forceUpdate, isLogouComCertificado);
	}
	
	public void consultaDados(String pesqCPF, boolean forceUpdate) throws Exception {
		consultaDados(pesqCPF, null, null, forceUpdate);
	}
	
	public List<DadosAdvogadoOAB> consultaDados(String pesqCPF, String nrOAB, String ufOAB, boolean forceUpdate) throws Exception {
		Boolean isLogouComCertificado = Authenticator.isLogouComCertificado();
		return consultaDados(pesqCPF, nrOAB, ufOAB, forceUpdate, isLogouComCertificado);
	}
		
	public List<DadosAdvogadoOAB> consultaDados(String pesqCPF, String nrOAB, String ufOAB, boolean forceUpdate, Boolean isLogouComCertificado) throws Exception {
		this.pesqCPF = null;
		this.nrOAB = null;
		this.ufOAB = null;
		List<DadosAdvogadoOAB> retorno = new ArrayList<DadosAdvogadoOAB>(0);
		
		String stringUrl = ParametroUtil.getFromContext("urlWsdlConsultaOab", true);
		if (Strings.isEmpty(stringUrl)) {
			throw new Exception("Parâmetro do Webservice não definido.");
		}
		wsdl = new URL(stringUrl);
		if(pesqCPF != null) {
			this.pesqCPF = StringUtil.removeNaoNumericos(pesqCPF);
		}
		else {
			this.nrOAB = StringUtil.retiraZerosEsquerda(nrOAB);
			this.ufOAB = ufOAB.toUpperCase();
		}
		if (forceUpdate) {
			try {
				dadosAdvogadoList = consultaWebService(isLogouComCertificado);
				atualizarDados(dadosAdvogadoList);
			} catch (Exception e) {
				String msg = "Erro ao consultar no web service: " + e.getMessage();
				FacesMessages.instance().addToControl("errosPreCadastro", msg);
				log.warn(msg, e);
				dadosAdvogadoList = consultaDadosBase();
				throw new Exception(e.getMessage());
			}
		} else {
			dadosAdvogadoList = new ArrayList<DadosAdvogadoOAB>(0);
			dadosAdvogadoList.addAll(consultaDadosBase());
			
			if (this.dadosAdvogadoList.size() == 0) {
				this.dadosAdvogadoList = consultaWebService(isLogouComCertificado);
				atualizarDados(dadosAdvogadoList);
			} 
		}
		
		if(dadosAdvogadoList.size() > 1) {
			retorno.addAll(organizaListPorTipoInscricao(dadosAdvogadoList));
		} else {
			retorno.addAll(dadosAdvogadoList);
		}
		return retorno;
	}

	/**
	 * organiza a lista passada em parametro em ordem alfabetica e retorna a lista reorganizada
	 * @param listaDesorganizada
	 * @return List<DadosAdvogadoOAB> listaOrganizada
	 */
	private List<DadosAdvogadoOAB> organizaListPorTipoInscricao(List<DadosAdvogadoOAB> listaDesorganizada) {
		List<DadosAdvogadoOAB> listaOrganizada = new ArrayList<DadosAdvogadoOAB>(0);
				if(listaDesorganizada != null && listaDesorganizada.size() > 0) {
					Collections.sort(listaDesorganizada, new Comparator<DadosAdvogadoOAB>() {
						@Override
						public int compare(DadosAdvogadoOAB doc1, DadosAdvogadoOAB doc2) {
							int valor = doc1.getTipoInscricao().toString().compareTo(doc2.getTipoInscricao().toString());
							return valor;
						}
					});
					listaOrganizada = listaDesorganizada;
				}
				return listaOrganizada;
	}

	private List<DadosAdvogadoOAB> consultaWebService(Boolean isLogouCertificado) throws Exception {
		if(!isLogouCertificado){
			throw new Exception("Não foi possível consultar a inscrição "+pesqCPF+" na OAB."+
					"Operação permitida apenas para usuários com certificado digital.");
		}
		setParamOABKey(ParametroUtil.instance().getParametroOAB());
		ExecutaServico es = new ExecutaServico();
		Thread t = new Thread(es);
		t.start();
		MeasureTime mt = new MeasureTime(true);
		while (t.isAlive()) {
			if ((mt.getTime() / 1000) > timeOut) {
				throw new Exception("O servidor da OAB não está respondendo. Tempo limite de espera esgotado.");
			}
			Thread.sleep(200);
		}
		
		if (es.errorMsg != null){
			AdvogadoNaoEncontradoException exception = new AdvogadoNaoEncontradoException();
			exception.setErrorMsg(es.errorMsg);
			throw exception;
		}
		
		Object response = pesqCPF != null ? es.consultaAdvogadoPorCpfResponse : es.consultaAdvogadoResponse;
		return processaXML(response);
	}

	private List<DadosAdvogadoOAB> consultaDadosBase() {
		List<DadosAdvogadoOAB> resultado = new ArrayList<DadosAdvogadoOAB>();
		DadosAdvogadoOABManager manager = obterDadosAdvogadoOABManager();
		
		if (pesqCPF != null) {
			resultado = manager.consultar(pesqCPF); 
		} else {
			resultado = manager.consultar(nrOAB, ufOAB);
		}
		return resultado;
	}

	public List<DadosAdvogadoOAB> consultaDadosBase(String pesqCPF) {
		pesqCPF = StringUtils.leftPad(pesqCPF, 11, "0");
		
		return obterDadosAdvogadoOABManager().consultar(pesqCPF);
	}

	private void atualizarDados(List<DadosAdvogadoOAB> listAdvNovo) {
		obterDadosAdvogadoOABManager().atualizar(listAdvNovo);
	}

	public List<DadosAdvogadoOAB> getDadosAdvogadoList() {
		return dadosAdvogadoList;
	}

	private List<DadosAdvogadoOAB> processaXML(Object response) {
		List<DadosAdvogadoOAB> dados = new ArrayList<DadosAdvogadoOAB>();
		DadosAdvogadoOAB dadosAdvogado = null;;
		JAXBContext jaxbContext = null;
		ArrayOfAdvogadoData aofadv = null;
		List<AdvogadoData> advList = null;
		StringReader strr = null;
		Unmarshaller unn = null;

		try {
			jaxbContext = JAXBContext.newInstance("br.org.oab.www5.cnaws.xmlobj");
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		try {
			unn = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		// Consulta por CPF
		String responseString = null;
		if (response instanceof ConsultaAdvogadoPorCpfResponse) {
			responseString = ((ConsultaAdvogadoPorCpfResponse) response).getConsultaAdvogadoPorCpfResult();
		}
		// Consulta por OAB
		else {
			responseString = ((ConsultaAdvogadoResponse) response).getConsultaAdvogadoResult();
		}
		
		if(responseString == null){
			return dados;
		}
		
		strr = new StringReader(responseString);
		try {
			aofadv = (ArrayOfAdvogadoData) unn.unmarshal(strr);
			advList = aofadv.getAdvogadoData();
		} catch (JAXBException e) {
			e.printStackTrace();
			log.warn(e.getMessage(), e);

		}
		for (AdvogadoData adv : advList) {
			if (adv != null) {
				dadosAdvogado = new DadosAdvogadoOAB();
				dadosAdvogado.setNome(Utf8ParaIso88591Util.converter(adv.getNome()));
				dadosAdvogado.setNumCPF(Utf8ParaIso88591Util.converter(adv.getCpf()));
				dadosAdvogado.setNumSeguranca(Utf8ParaIso88591Util.converter(adv.getNumeroSeguranca()));
				dadosAdvogado.setUf(Utf8ParaIso88591Util.converter(adv.getUf()));
				dadosAdvogado.setBairro(Utf8ParaIso88591Util.converter(adv.getBairro()));
				dadosAdvogado.setCep(Utf8ParaIso88591Util.converter(adv.getCep()));
				dadosAdvogado.setCidade(Utf8ParaIso88591Util.converter(adv.getCidade()));
				dadosAdvogado.setDdd(Utf8ParaIso88591Util.converter(adv.getDDD()));
				dadosAdvogado.setEmail(Utf8ParaIso88591Util.converter(adv.getEmail()));
				dadosAdvogado.setLogadouro(Utf8ParaIso88591Util.converter(adv.getLogradouro()));
				dadosAdvogado.setNomeMae(Utf8ParaIso88591Util.converter(adv.getNomeMae()));
				dadosAdvogado.setNomePai(Utf8ParaIso88591Util.converter(adv.getNomePai()));
				String numInscricao = adv.getInscricao();
				if(numInscricao != null){
					numInscricao = numInscricao.trim();
				}
				dadosAdvogado.setNumInscricao(Utf8ParaIso88591Util.converter(numInscricao));
				dadosAdvogado.setOrganizacao(Utf8ParaIso88591Util.converter(adv.getOrganizacao()));
				dadosAdvogado.setSituacaoInscricao(Utf8ParaIso88591Util.converter(adv.getSituacao()));
				dadosAdvogado.setTelefone(Utf8ParaIso88591Util.converter(adv.getTelefone().toString()));
				dadosAdvogado.setTipoInscricao(Utf8ParaIso88591Util.converter(adv.getTipoInscricao()));

				tratarIntegridadeDados(dados, dadosAdvogado);
				
				dados.add(dadosAdvogado);
			}
		}

		return dados;
	}
	
	/**
	 * Método utilizado para verificar a integridade dos dados que serão persistidos na tabela "tb_dado_oab_pess_advogado".
	 * De acordo com a restrição de integridade, não deve haver dois registros com o mesmo valor de CPF e UF.
	 * 
	 * @param dadosAdvogadoOABList Lista que contém as informações dos advogados.
	 * @param dadosAdvogadoOAB Objeto que representa a informação de um advogado.
	 */
	private void tratarIntegridadeDados(List<DadosAdvogadoOAB> dadosAdvogadoOABList, DadosAdvogadoOAB dadosAdvogadoOAB) {
		Iterator<DadosAdvogadoOAB> iterator = dadosAdvogadoOABList.iterator();
		while (iterator.hasNext()) {
			DadosAdvogadoOAB obj = iterator.next();
			if (obj.getNumCPF().equals(dadosAdvogadoOAB.getNumCPF()) && obj.getUf().equals(dadosAdvogadoOAB.getUf())) {
				iterator.remove();
				break;
			}
		}
	}

	public static ConsultaClienteOAB instance() {
		return ComponentUtil.getComponent("consultaClienteOAB");
	}

	public void setParamOABKey(String key) {
		this.paramOABKey = key;
	}

	public String getParamOABKey() {
		return paramOABKey;
	}

	/**
	 * para gerar o ws da OAB wsimport.bat -keep -extension -p
	 * br.com.infox.pje.webservices.consultaoab -target 2.1 -Xendorsed
	 * -Xnocompile -XadditionalHeaders
	 * http://www5.oab.org.br/cnaws/service.asmx?WSDL e foi usado o
	 * http://jax-ws.java.net/ v 2.2.1
	 * 
	 * @author Alan
	 * 
	 */
	private class ExecutaServico implements Runnable {

		private String xml;
		private ObjectFactory objFac;
		private Service svc;
		private ServiceSoap port;
		private Authentication auth;
		protected ConsultaAdvogadoPorCpf consultaAdvogadoPorCpf;
		protected ConsultaAdvogado consultaAdvogado;
		protected ConsultaAdvogadoPorCpfResponse consultaAdvogadoPorCpfResponse;
		protected ConsultaAdvogadoResponse consultaAdvogadoResponse;
		
		protected String errorMsg = null;
		

		@Override
		public void run() {
			objFac = new ObjectFactory();
			svc = new Service(wsdl);
			port = svc.getServiceSoap();
			auth = new Authentication();
			auth.setKey(getParamOABKey());

			if(pesqCPF != null) {
				executaConsultaCPF();
			}
			else {
				executaConsultaOAB();
			}

		}

		private void executaConsultaCPF() {
			consultaAdvogadoPorCpf = objFac.createConsultaAdvogadoPorCpf();
			consultaAdvogadoPorCpfResponse = objFac.createConsultaAdvogadoPorCpfResponse();
			consultaAdvogadoPorCpf.setCpf(pesqCPF);

			Authentication auth = new Authentication();
			// Obtém chave de acesso (parâmetro "chaveOAB" do sistema) para autenticação no WS da OAB 
			String key = getParamOABKey();
			auth.setKey(key);
			

			try {
				xml = port.consultaAdvogadoPorCpf(pesqCPF, auth);
				consultaAdvogadoPorCpfResponse.setConsultaAdvogadoPorCpfResult(xml);
			} catch (Exception e) {
				log.warn("Não foi possível contactar Webservice da OAB", e);
				errorMsg = "Não foi possível contactar os serviços OAB no momento. Tente novamente.";
			}
		}

		private void executaConsultaOAB() {
			consultaAdvogado = objFac.createConsultaAdvogado();
			consultaAdvogadoResponse = objFac.createConsultaAdvogadoResponse();
			consultaAdvogado.setInscricao(nrOAB);
			consultaAdvogado.setUf(ufOAB);
			
			
			try {
				xml = port.consultaAdvogado(nrOAB,ufOAB, null, auth);
				consultaAdvogadoResponse.setConsultaAdvogadoResult(xml);
			} catch (Exception e) {
				log.warn("Não foi possível contactar Webservice da OAB", e);
				errorMsg = "Não foi possível contactar os serviços da OAB no momento. Tente novamente.";
			}
			
		}

	}
	
	/**
	 * Retorna uma instância de DadosAdvogadoOABManager.
	 * 
	 * @return Instância de DadosAdvogadoOABManager.
	 */
	protected DadosAdvogadoOABManager obterDadosAdvogadoOABManager() {
		return (DadosAdvogadoOABManager) Component.getInstance(DadosAdvogadoOABManager.class, true);
	}

}
