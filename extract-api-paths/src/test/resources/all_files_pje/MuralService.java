package br.jus.cnj.pje.nucleo.manager;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.dao.GenericDAO;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoDAO;
import br.jus.cnj.pje.nucleo.MuralException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.service.BaseService;
import br.jus.cnj.pje.nucleo.service.JurisdicaoService;
import br.jus.cnj.pje.vo.AdvogadoParteMural;
import br.jus.cnj.pje.vo.MuralInfo;
import br.jus.cnj.pje.vo.ProcessoParteMural;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;

@Name(MuralService.NAME)
public class MuralService extends BaseService {
	public final static String NAME = "muralService";

	private static String PATH_MURAL_HEALTH = "/actuator/health";
	private static String PATH_MURAL_PUBLICAO = "/integracao/v1/publicar";
	
	@In
	private ProcessoDocumentoBinManager processoDocumentoBinManager;

	@Logger
	private Log logger;

	@In
	ProcessoDocumentoDAO processoDocumentoDAO;

	@In
	GenericDAO genericDAO;

	/**
	 * 
	 * @param numeroProcesso "0600006-90.2016.6.00.0000"
	 * 
	 * @return sucesso no envio é igual a true
	 */
	public void enviarDadosMural(String numeroProcesso, Integer idProcessoDocumento, ProcessoExpediente...processoExpediente) throws MuralException {
		enviarDadosMural(numeroProcesso, idProcessoDocumento, null, processoExpediente);
	}
	
	public void enviarDadosMural(String numeroProcesso, int idProcessoDocumento, List<ProcessoParte> partesDoExpediente, ProcessoExpediente...processoExpediente) throws MuralException {
		try {
			String parametroServicoMural = obterEnderecoServico();
			logger.info("Obtendo endereço do serviço " + parametroServicoMural);
			MuralInfo muralInfo = preencherMuralInfo(numeroProcesso, idProcessoDocumento, partesDoExpediente);
			if(processoExpediente != null) {
				muralInfo.setIdDecisaoPJE(processoExpediente[0].getIdProcessoExpediente());
			}
			enviarDadosMural(parametroServicoMural, muralInfo);
		} catch (ConnectException connectException) {
			logger.error("Não foi possível conectar no serviço do mural", connectException);
			throw new MuralException(connectException.getMessage());
		} catch (Exception e) {
			logger.info("Conectou no mural, mas houve erro ao enviar os dados para o mural",e);
			throw new MuralException(e.getMessage());
		}
	}

	private MuralInfo preencherMuralInfo(String numeroProcesso, Integer idProcessoDocumento, List<ProcessoParte> partesDoExpediente) {
		MuralInfo muralVO = new MuralInfo();
		ProcessoDocumento processoDocumento = processoDocumentoDAO.find(idProcessoDocumento);
		ProcessoTrf processoTrf = ComponentUtil.getProcessoTrfManager().getProcessoTrfByProcesso(processoDocumento.getProcesso());

		adicionandoDecisaoMuralInfo(processoDocumento, muralVO);
		adicionandoInformacoesMuralInfo(processoTrf, processoDocumento, numeroProcesso, muralVO, partesDoExpediente);

		logger.info("preenchendo pojo MuralInfo");
		return muralVO;
	}

	private void adicionandoDecisaoMuralInfo(
			ProcessoDocumento processoDocumento, MuralInfo muralVO) {
		byte[] data = null;
		try {
			data = processoDocumentoBinManager.getBinaryData(processoDocumento.getProcessoDocumentoBin());
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		if( data != null ) {
			muralVO.setDecisaoBinaria(data);
		}
		muralVO.setDataDecisao(processoDocumento.getDataJuntada());
		muralVO.setDataPublicacao(new Date());
		muralVO.setCaminhoDecisaoIsilon(processoDocumento.getProcessoDocumentoBin().getNumeroDocumentoStorage());
	}

	private void adicionandoInformacoesMuralInfo(ProcessoTrf processoTrf, ProcessoDocumento processoDocumento, String numeroProcesso, MuralInfo muralVO, List<ProcessoParte> partesDoExpediente) {
		if(ParametroUtil.instance().isPrimeiroGrau()) {
			muralVO.setSiglaTribunal(processoTrf.getOrgaoJulgador().getOrgaoJulgador());
		} else {
			muralVO.setSiglaTribunal(ParametroUtil.instance().getSiglaTribunal());
		}
		muralVO.setSiglaClasse(processoTrf.getClasseJudicial().getClasseJudicialSigla());
		muralVO.setNumeroProcesso(numeroProcesso);
		muralVO.setTextoAssuntoProcessual(obterAssuntoProcesso(processoTrf.getAssuntoTrfListStr()));
		muralVO.setCargoUsuario(processoDocumento.getPapel().getNome());
		muralVO.setTipoDecisao(processoDocumento.getTipoProcessoDocumento().getTipoProcessoDocumento());
		muralVO.setDescricaoTipoDocumento(processoDocumento.getTipoProcessoDocumento().getTipoProcessoDocumento());
		muralVO.setDescricaoClasse(processoTrf.getClasseJudicial().getClasseJudicial());
		muralVO.setSegredoJustica(processoTrf.getSegredoJustica());
		muralVO.setUfOrigem(processoTrf.getComplementoJE().getEstadoEleicao().getCodEstado());
		muralVO.setMunicipioOrigem(processoTrf.getComplementoJE().getMunicipioEleicao().getMunicipio());
		muralVO.setNomeUsuario(processoDocumento.getNomeUsuario());
		muralVO.setLoginUsuario(processoDocumento.getUsuarioInclusao().getLogin());
		muralVO.setUnidadeUsuario(Authenticator.getLocalizacaoAtual().getLocalizacao());

		if(processoTrf.getPessoaRelator() != null) {
			muralVO.setNomeRelator(processoTrf.getPessoaRelator().getNome());
		} else {
			muralVO.setNomeRelator(ComponentUtil.getUsuarioLocalizacaoMagistradoServidorManager().getRelator(processoTrf.getOrgaoJulgador()));
		}
		
		if(ParametroUtil.instance().isPrimeiroGrau()) {
			muralVO.setIdZonaEleitoral(Integer.valueOf(processoTrf.getOrgaoJulgador().getIdOrgaoJulgador()));
		} else { 
			muralVO.setIdZonaEleitoral(0);
		}
		muralVO.setFontePublicacao("PJe");
		muralVO.setOrigemDecisao("E");
		logger.info("Adicionando outras informações ao pojo muralInfo");
		obterPartesProcesso(muralVO, processoTrf, partesDoExpediente);
	}

	private void obterPartesProcesso(MuralInfo muralVO, ProcessoTrf processoTrf, List<ProcessoParte> processoPartes) {
		if(processoPartes == null || processoPartes.isEmpty()){
			processoPartes = processoTrf.getListaPartePrincipal(false, ProcessoParteParticipacaoEnum.A, ProcessoParteParticipacaoEnum.P, ProcessoParteParticipacaoEnum.T);
		}
		List<ProcessoParteMural> processoPartesMural = new ArrayList<ProcessoParteMural>();
		for (ProcessoParte parte : processoPartes) {
			Pessoa MP = ComponentUtil.getComponent(PessoaManager.class).getFiscalLei(processoTrf.getJurisdicao());
			if(MP != null && parte.getPessoa().equals(MP)) {
				muralVO.setPrazoMp(Boolean.TRUE);
			}
			ProcessoParteMural processoParteMural = new ProcessoParteMural();
			processoParteMural.setNomeParte(parte.getNomeParte());
			processoParteMural.setDescricaoTipoParte(parte.getTipoParte().getTipoParte());
			processoParteMural.setOrdemEnvioParte(parte.getOrdem());
			obterAdvogadosParte(processoParteMural, parte);
			processoPartesMural.add(processoParteMural);
		}
		muralVO.setProcessoPartesMural(processoPartesMural);
		logger.info("Obtendo partes do processo");
	}

	private void obterAdvogadosParte(ProcessoParteMural processoParteMural,	ProcessoParte parte) {
		TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
		for (ProcessoParteRepresentante representante : parte.getProcessoParteRepresentanteList()) {
			if(representante.getInSituacao().equals(ProcessoParteSituacaoEnum.A) && representante.getParteRepresentante().getTipoParte().equals(tipoParteAdvogado)) {
				AdvogadoParteMural advogadoParteMural = new AdvogadoParteMural();
				String numero = ((PessoaFisica)representante.getRepresentante()).getPessoaAdvogado().getNumeroOAB();
				if(((PessoaFisica)representante.getRepresentante()).getPessoaAdvogado().getLetraOAB() != null ) {
					numero = numero + "-" + ((PessoaFisica)representante.getRepresentante()).getPessoaAdvogado().getLetraOAB(); 
				}
				advogadoParteMural.setNumeroOAB(numero);
				if (((PessoaFisica)representante.getRepresentante()).getPessoaAdvogado().getUfOAB() != null) {
					advogadoParteMural.setUfOAB(((PessoaFisica)representante.getRepresentante()).getPessoaAdvogado().getUfOAB().getCodEstado());
				}
				advogadoParteMural.setNomeAdvogado(representante.getRepresentante().getNome());
				processoParteMural.getAdvogadoParteMural().add(advogadoParteMural);
			}
		}
		logger.info("Obtendo advogado das partes");
	}
	
	private String obterAssuntoProcesso(List<String> assuntoTrfListStr) {
		String listaAssuntoStr = assuntoTrfListStr.stream().collect(Collectors.joining(", "));
		logger.info("Obtendo assunto(s) do processo");
		return listaAssuntoStr;
	}

	private String obterEnderecoServico() {
		logger.info("Obtendo URL do serviço do conector com o Mural");
		return ParametroUtil.getParametro(Parametros.URL_SERVICO_MURAL);
	}

	private void enviarDadosMural(String parametroServicoMural, MuralInfo muralVO) throws Exception {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		String muralJson = gson.toJson(muralVO, MuralInfo.class);

		Response response = Jsoup.connect(parametroServicoMural+PATH_MURAL_PUBLICAO)
				.timeout(600000).header("Content-Type", "application/json")
				.ignoreContentType(true).ignoreHttpErrors(true)
				.method(Method.POST).requestBody(muralJson).execute();

		if (response.statusCode() != 200) {
			logger.info("Erro ao enviar para o Mural {0}", response.statusCode());
			throw new Exception("Erro " + response.parse());
		}
		logger.info("Dados persistidos com sucesso ");
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
    public boolean verificarServicoDisponivel(int timeoutEspera, String url) {
        boolean retorno = false;
        try {
            HttpClient client = null;
            HttpParams httpParams = new org.apache.http.params.BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, timeoutEspera);
            client = new DefaultHttpClient(httpParams);

            HttpResponse response = client.execute(new HttpGet(url + PATH_MURAL_HEALTH));
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");

            if (response.getStatusLine().getStatusCode() != 200) {
                logger.info("Erro ao consultar o serviço do Mural {0}", response.getStatusLine().getStatusCode());
                throw new Exception("Erro No Mural" + response.getStatusLine().getStatusCode());
            } else if(responseString != null && responseString.contains("UP")) {
                retorno = true;
            }

        } catch (Exception e) {
            logger.info("O serviço do Mural está indisponível");
        }
        return retorno;
    }
}