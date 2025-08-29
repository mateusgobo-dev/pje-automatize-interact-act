package br.com.infox.pje.webservices.processoprevento;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Query;
import javax.xml.namespace.QName;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.MeasureTime;
import br.com.itx.component.UrlUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.PrevencaoHostsWebservice;

@Name("consultaWSProcessoPrevento")
@BypassInterceptors
public class ConsultaWSProcessoPrevento implements Serializable{

	private static final long serialVersionUID = 1L;

	private static final String FINAL_URL_PREVENCAO = "ProcessoPrevento?wsdl";
	private static final LogProvider log = Logging.getLogProvider(ConsultaWSProcessoPrevento.class);
	private URL wsdl;
	private String pesqCPFCNPJ;
	private int timeOut = 30;
	private List<String> erros = new ArrayList<String>();
	private ArrayList<PrevencaoHostsWebservice> listValidos;

	@SuppressWarnings("unchecked")
	private void getHosts(){
		String sb = "select o from PrevencaoHostsWebservice o ";
		Query query = EntityUtil.getEntityManager().createQuery(sb);
		List<PrevencaoHostsWebservice> resultList = query.getResultList();
		listValidos = new ArrayList<PrevencaoHostsWebservice>();
		for (PrevencaoHostsWebservice phw : resultList){
			if (isUrlValida(phw)){
				listValidos.add(phw);
			}
		}
	}

	private boolean isUrlValida(PrevencaoHostsWebservice phw){
		InputStream inputStreamURL = null;
		try{
			inputStreamURL = UrlUtil.getInputStreamUrl(phw.getUrlServidor().concat(FINAL_URL_PREVENCAO));
		} catch (IOException e){
			String msgErro = MessageFormat.format("URL do Webservice não esta acessivel no estado {0}: {1}",
					phw.getHost(), e.getMessage());
			log.error(msgErro);
			erros.add(msgErro);
		}
		return inputStreamURL != null;
	}

	public void consultaDados(String numeroCPFCNPJ) throws Exception{

		pesqCPFCNPJ = numeroCPFCNPJ;
		URL baseUrl;
		baseUrl = br.com.infox.pje.webservices.processoprevento.ProcessoPreventoService.class.getResource(".");
		List<DadosProcessoPrevento> processo = null;
		List<PrevencaoHostsWebservice> list = getHostsValidos();
		if (list != null){
			for (PrevencaoHostsWebservice pWS : list){
				wsdl = new URL(baseUrl, pWS.getUrlServidor().concat(FINAL_URL_PREVENCAO));
				try{
					processo = consultaWebService();
					atualizarDados(processo);
				} catch (Exception e){
					String msg = "Erro ao consultar no web service: " + e.getMessage();
					FacesMessages.instance().add(Severity.WARN, msg);
					log.warn(msg, e);
				}
			}
		}
	}

	private List<PrevencaoHostsWebservice> getHostsValidos(){
		if (listValidos == null){
			getHosts();
		}
		return listValidos;
	}

	private void atualizarDados(List<DadosProcessoPrevento> dadosProcessoPrevento){
		ParametroUtil.instance();
		String secao = ParametroUtil.getFromContext(Parametros.NOME_SECAO_JUDICIARIA, true);

		for (int i = 0; i < dadosProcessoPrevento.size(); i++){

			if (!dadosProcessoPrevento.get(i).getSessaoJudiciaria().equalsIgnoreCase(secao)){
				String numProc = dadosProcessoPrevento.get(i).getNumeroProcesso();
				int idProc = ProcessoTrfHome.instance().getInstance().getIdProcessoTrf();
				String classe = dadosProcessoPrevento.get(i).getClasseJudicial();
				String sessao = dadosProcessoPrevento.get(i).getSessaoJudiciaria();
				String orgao = dadosProcessoPrevento.get(i).getOrgaoJulgador();
				String link = dadosProcessoPrevento.get(i).getLink();
				String hash = dadosProcessoPrevento.get(i).getHash();
				Date data = new Date();
				String query = "insert into tb_processo_trf_conexao (id_processo_trf, ds_sessao_judiciaria, ds_orgao_julgador, "
						+ "nr_processo, ds_link_sessao_judiciaria, ds_validacao_hash, ds_classe_judicial, dt_possivel_prevencao, dt_registro ) "
						+ "values ("
						+ idProc
						+ ", '"
						+ sessao
						+ "', '"
						+ orgao
						+ "', '"
						+ numProc
						+ "', '"
						+ link
						+ "', '" + hash + "', '" + classe + "', '" + data + "', '" + data + "')";
				HibernateUtil.getSession().createSQLQuery(query)
						.addSynchronizedQuerySpace("tb_processo_trf_conexao")
						.executeUpdate();
			}
		}
	}

	private List<DadosProcessoPrevento> consultaWebService() throws Exception{
		ExecutaServicoPf es = new ExecutaServicoPf();
		Thread t = new Thread(es);
		t.start();
		MeasureTime mt = new MeasureTime(true);
		while (t.isAlive()){
			if ((mt.getTime() / 1000) > timeOut){
				throw new Exception("Timeout");
			}
			Thread.sleep(200);
		}
		List<DadosProcessoPrevento> dadosCPFSecurity = es.dadosCPFSecurity;
		if (dadosCPFSecurity == null || dadosCPFSecurity.size() == 0){
			throw new Exception("Não foi encontrado processo para este documento.");
		}
		return dadosCPFSecurity;
	}

	public static void main(String[] args) throws Exception{
		String cpf = "033.782.735-00";
		ConsultaWSProcessoPrevento consulta = new ConsultaWSProcessoPrevento();
		consulta.pesqCPFCNPJ = cpf;
		List<DadosProcessoPrevento> prevento = consulta.consultaWebService();
		for (int i = 0; i < prevento.size(); i++){
			System.out.println(prevento.get(i).getNumeroProcesso());
		}
	}

	public static ConsultaWSProcessoPrevento instance(){
		return ComponentUtil.getComponent("consultaWSProcessoPrevento");
	}

	private class ExecutaServicoPf implements Runnable{

		private List<DadosProcessoPrevento> dadosCPFSecurity;

		@Override
		public void run(){
			ProcessoPreventoService service1 = new ProcessoPreventoService(wsdl, new QName(
					"http://webservices.pje.infox.com.br/", "ProcessoPreventoService"));
			ProcessoPrevento port1 = service1.getProcessoPreventoPort();
			try{
				this.dadosCPFSecurity = port1.getProcessosPreventos(pesqCPFCNPJ);
			} catch (NoSuchAlgorithmException_Exception e){
				e.printStackTrace();
			} catch (UnsupportedEncodingException_Exception e){
				e.printStackTrace();
			}

		}

	}

	public List<String> getErros(){
		return erros;
	}

}