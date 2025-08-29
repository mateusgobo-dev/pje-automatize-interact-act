package br.jus.cnj.pje.intercomunicacao.v222.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;

import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.home.ProcessoDocumentoBinPessoaAssinaturaHome;
import br.com.infox.cliente.home.ProcessoPrioridadeProcessoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.intercomunicacao.v222.beans.CabecalhoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadePoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.Parametro;
import br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.converter.ProcessoDocumentoParaDocumentoConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.ProcessoTrfParaCabecalhoProcessualConverter;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.ws.client.ConsultaPJeClient;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.csjt.pje.view.action.ProcessoJTHome;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

public class IntercomunicacaoUtil {
	
	public static ManifestacaoProcessual montarManifestacaoProcessual(Pessoa pessoaLogada, ManifestacaoProcessualRequisicaoDTO objeto, List<String> listaIdCarregarBinario) throws Exception {
		ManifestacaoProcessual ret = montarManifestacaoProcessual(pessoaLogada.getIdUsuario().toString() + "_ENVIO", 
				pessoaLogada.getIdUsuario().toString(), objeto, listaIdCarregarBinario);
		MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_CPF_CNPJ_USUARIO, pessoaLogada.getDocumentoCpfCnpj());
		MNIParametroUtil.adicionar(ret, MNIParametro.getIndiceParteSituacao(), MNIParametro.getListIndiceParteSituacao().toString());
		MNIParametroUtil.adicionar(ret, MNIParametro.getIndiceParteRepresentanteSituacao(), MNIParametro.getListIndiceParteRepresentateSituacaoValor().toString());
		MNIParametroUtil.adicionar(ret, MNIParametro.getIndiceParteSigilo(), MNIParametro.getListIndiceParteSigiloValor().toString());
		MNIParametroUtil.adicionar(ret, MNIParametro.getIndicePartePrincipal(), MNIParametro.getListIndicePartePrincipalValor().toString());
		MNIParametroUtil.adicionar(ret, MNIParametro.getIndiceParteRepresentanteSigilo(), MNIParametro.getListIndiceParteRepresentateSigiloValor().toString());
		return ret;

	}

	
	/**
	 * Atribui a necessidade de assistencia judiciaria ao polo ativo quando necessário
	 * @param polos convertidos ao MNI
	 * @param processo a ser enviado
	 */
	private static void setAssistenciaJudiciaria(List<PoloProcessual> polos, ProcessoTrf processo){
		if (processo.getJusticaGratuita()==null || !processo.getJusticaGratuita())
			return;
		for (PoloProcessual polo : polos) {
			if (polo.getPolo()==ModalidadePoloProcessual.AT){
				for (br.jus.cnj.intercomunicacao.v222.beans.Parte parte : polo.getParte()) {
					parte.setAssistenciaJudiciaria(true);
				}
			}
		}
	}
	
	@SuppressWarnings("java:S3776")
	public static ManifestacaoProcessual montarManifestacaoProcessual(String idManifestante, String senhaManifestante, ManifestacaoProcessualRequisicaoDTO objeto, List<String> listaIdCarregarBinario) throws Exception {
		ConsultaPJeClient consultaPJeClient = objeto.getConsultaPJeClient();
		Integer competenciaConflito = objeto.getCompetenciaConflito();
		boolean isRequisicaoPJe = objeto.getIsRequisicaoPJE();
		ProcessoTrf processoTrf = objeto.getProcessoTrf();

		EnderecoWsdl enderecoWsdl = ParametroUtil.instance().getEnderecoWsdlAplicacaoOrigem();
		
		if (enderecoWsdl == null || StringUtils.isBlank(enderecoWsdl.getWsdlIntercomunicacao())) {
			throw new Exception("Parâmetro 'idEnderecoWsdlAplicacaoOrigem' não foi definido com a referência ao registro do Endereço WSDL da aplicação de origem.");
		}
		
		
		validarAtributosProcessoTrf(processoTrf);

		ManifestacaoProcessual ret = new ManifestacaoProcessual();
		
		if (isRequisicaoPJe){
			MNIParametroUtil.adicionar(ret, MNIParametro.isPJE(), "true");
		}
		
		MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_NUM_PROC_1_GRAU, processoTrf.getNumeroProcesso());
		MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_DESCRICAO_INSTANCIA_PROCESSO_ORIGEM, 
				ParametroUtil.instance().getSiglaTribunal() + 
				ParametroUtil.instance().getInstancia());
		MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_INSTANCIA_PROCESSO_ORIGEM, ParametroUtil.instance().getInstancia());
		
		ret.setIdManifestante(idManifestante);
		ret.setSenhaManifestante(senhaManifestante);
		ret.setDadosBasicos(montarCabecalhoProcesso(processoTrf, consultaPJeClient, competenciaConflito));
		
		setAssistenciaJudiciaria(ret.getDadosBasicos().getPolo(),processoTrf);

		montarDocumentos(ret, processoTrf, listaIdCarregarBinario, consultaPJeClient);

		ProcessoJTHome processoJTHome = ComponentUtil.getComponent("processoJTHome");
		
		if(processoJTHome.getInstance().getAtividadeEconomica() != null){
			MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_ATIVIDADE_ECONOMICA, processoJTHome.getInstance().getAtividadeEconomica().getIdAtividadeEconomica()+"");
		}
		
		if(processoJTHome.getInstance().getMunicipioIBGE() != null){
			MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_MUNICIPIO_IBGE, processoJTHome.getInstance().getMunicipioIBGE().getIdMunicipio()+"");
		}
		
		
		MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_URL_ORIGEM_ENVIO, enderecoWsdl.getWsdlIntercomunicacao());

		MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_URL_ORIGEM_CONSULTA, enderecoWsdl.getWsdlConsulta());

        MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_LIMINAR_ANTECIPA_TUTELA, processoTrf.getTutelaLiminar().toString());
		
        if (ComponentUtil.getTramitacaoProcessualService().temSituacao(processoTrf, Variaveis.PJE_ATENDIMENTO_PLANTAO)) {
        	MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_ATENDIMENTO_PLANTAO, Boolean.TRUE.toString());	
        }
		
		if (processoJTHome.getInstance().getAtividadeEconomica() != null) {
			MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_ATIVIDADE_ECONOMICA, processoJTHome.getInstance().getAtividadeEconomica().getIdAtividadeEconomica() + "");
		}

		if (processoJTHome.getInstance().getMunicipioIBGE() != null) {
			MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_MUNICIPIO_IBGE, processoJTHome.getInstance().getMunicipioIBGE().getIdMunicipio() + "");
		}

		if (processoTrf.getComplementoJE() != null) {
			if (processoTrf.getComplementoJE().getEleicao() != null) {
				MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_JE_ANO_ELEICAO, processoTrf.getComplementoJE().getEleicao().getAno().toString());
				MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_JE_TIPO_ELEICAO, processoTrf.getComplementoJE().getEleicao().getTipoEleicao().getCodObjeto().toString());
			}

			MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_JE_ESTADO, processoTrf.getComplementoJE().getEstadoEleicao().getCodEstado());

			MunicipioManager municipioManager = (MunicipioManager) ComponentUtil.getComponent(MunicipioManager.NAME);
			Municipio municipio = municipioManager.getMunicipioByCodigoIBGE(processoTrf.getComplementoJE().getMunicipioEleicao().getCodigoIbge());

			MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_JE_MUNICIPIO, municipio.getCodigoIbge());
		}

		if(MNIParametro.getListIndiceParteSigiloValor() != null) {
			MNIParametroUtil.adicionar(ret, MNIParametro.getIndiceParteSigilo(), MNIParametro.getListIndiceParteSigiloValor().toString() );
		}
		
		if(MNIParametro.getListIndiceParteRepresentateSigiloValor() != null) {
			MNIParametroUtil.adicionar(ret, MNIParametro.getIndiceParteRepresentanteSigilo(), MNIParametro.getListIndiceParteRepresentateSigiloValor().toString() );
		}
		
		/*
		 * Início - PJEII-8136 - Registro de prioridade processual é perdido ao
		 * ser remetido ou baixado processintero. [CSJT] - Thiago Oliveira -
		 * 04/06/2013
		 */
		ProcessoPrioridadeProcessoHome processoPrioridadeProcessoHome = ComponentUtil.getComponent("processoPrioridadeProcessoHome");
		List<PrioridadeProcesso> listaPrioridades = processoPrioridadeProcessoHome.getInstance().getProcessoTrf().getPrioridadeProcessoList();
		
		
		String prioridades = "";

		if (listaPrioridades != null && listaPrioridades.size() > 0) {
			for (PrioridadeProcesso prioridadeProcesso : listaPrioridades) {
				prioridades += prioridadeProcesso.getIdPrioridadeProcesso() + ";";
			}
		}
		MNIParametroUtil.adicionar(ret, MNIParametro.PARAM_PRIORIDADE_PROCESSUAL, prioridades);

		/*
		 * Fim - PJEII-8136
		 */
		/*
		 * PJEII-25773
		 */

		MNIParametroUtil.adicionar(ret, MNIParametro.getIndiceParteSigilo(), MNIParametro.getListIndiceParteSigiloValor().toString());
		MNIParametroUtil.adicionar(ret, MNIParametro.getIndiceParteRepresentanteSigilo(),MNIParametro.getListIndiceParteRepresentateSigiloValor().toString());

		return ret;
	}

	private static CabecalhoProcessual montarCabecalhoProcesso(ProcessoTrf processo, ConsultaPJeClient consultaPJeClient, Integer competenciaConflito) {
		ProcessoTrfParaCabecalhoProcessualConverter converter = ProcessoTrfParaCabecalhoProcessualConverter.instance(consultaPJeClient, competenciaConflito);
		return converter.converter(processo);
	}

	private static void montarDocumentos(ManifestacaoProcessual manifestacaoProcessual, ProcessoTrf processoTrf, List<String> listaIdCarregarBinario, ConsultaPJeClient consultaPjeClient) throws Exception {
		ProcessoDocumentoManager pdm = ComponentUtil.getComponent(ProcessoDocumentoManager.NAME);
		List<ProcessoDocumento> docs = pdm.recuperaDocumentosJuntados(processoTrf, null);
		PessoaService pessoaService = (PessoaService) Component.getInstance(PessoaService.NAME);
		//associar vinculados ao principal
		List<br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual> docsVinculados = 
				new ArrayList<br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual>(0); 
		
		for (ProcessoDocumento doc : docs) {
			doc = pdm.findById(doc.getIdProcessoDocumento());
			validarAtributosProcessoDocumento(doc);
						
			if (Boolean.FALSE.equals(doc.getAtivo()) && doc.getDataJuntada() == null) {
				continue;
			}
			
			if (ProjetoUtil.isVazio(doc.getProcessoDocumentoBin().getSignatarios())) {
				continue;
			}

			String instanciaDestino = null;
			if(consultaPjeClient != null && consultaPjeClient.getEnderecoWsdl() != null) {
				instanciaDestino = consultaPjeClient.getEnderecoWsdl().getInstancia();
			} 
			
			Integer idDocumentoInstancia = Integer.parseInt(doc.getInstancia());
			Integer idAplicacaoSistema = ParametroUtil.instance().getAplicacaoSistema().getIdAplicacaoClasse();
			
			if((instanciaDestino != null && (instanciaDestino.compareTo(doc.getInstancia()) != 0)) || idDocumentoInstancia.equals(idAplicacaoSistema)) {
				ProcessoDocumentoParaDocumentoConverter conversor = (ProcessoDocumentoParaDocumentoConverter) Component.getInstance(ProcessoDocumentoParaDocumentoConverter.class);
				br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual mniDoc = null;
				if (MNIParametroUtil.obterValor(manifestacaoProcessual,  MNIParametro.isPJE()).equals("true")){
					mniDoc = conversor.converterParaRemessa(doc, false, listaIdCarregarBinario, true);
				}else{
					mniDoc = conversor.converter(doc, false, listaIdCarregarBinario, true);
				}
					
				docsVinculados.addAll(mniDoc.getDocumentoVinculado());				
				
				adicionarOutrosParametros(mniDoc, doc);
								
				manifestacaoProcessual.getDocumento().add(mniDoc);
			}
		}
		

//		Implementação necessária, pois a classe DocumentoProcessual do mni não possui implementação do método equals()
		List<br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual> docsVinculadosAux = 
				new ArrayList<br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual>(0); 
		for(br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual mniDoc : manifestacaoProcessual.getDocumento()){
			br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual mniDocAssociado = 
					getDocumentoById(mniDoc, docsVinculados);
			if (mniDocAssociado != null)
				docsVinculadosAux.add(mniDocAssociado);

		}
		
		manifestacaoProcessual.getDocumento().removeAll(docsVinculadosAux);
		
		for (Iterator<DocumentoProcessual> iteratorDocPrinc = manifestacaoProcessual.getDocumento().iterator(); iteratorDocPrinc.hasNext();) {
			DocumentoProcessual documentoProcessual = (DocumentoProcessual) iteratorDocPrinc.next();
			for (Iterator<DocumentoProcessual> iteratorDocVinc = documentoProcessual.getDocumentoVinculado().iterator(); iteratorDocVinc.hasNext();) {
				DocumentoProcessual documentoVinculado = (DocumentoProcessual) iteratorDocVinc.next();
				if (!documentoVinculado.isSetAssinatura()) {
					iteratorDocVinc.remove();
				}				
			}			
		}		
	}
	
	private static br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual getDocumentoById(
			br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual documentoAssociado, 
			List<br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual> docs){
		if(documentoAssociado.getIdDocumento() != null && docs != null && !docs.isEmpty()){
			for(br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual doc : docs){
				if(doc.getIdDocumento().equals(documentoAssociado.getIdDocumento())){
					doc.getOutroParametro().addAll(documentoAssociado.getOutroParametro());
					return documentoAssociado;
				}
			}
		}
		return null;
	}

	private static PessoaDocumentoIdentificacao getPessoaDocumentoIdentificacaoCriadorDocumento(
			PessoaService pessoaService, ProcessoDocumento doc)
			throws PJeBusinessException {
		Pessoa pessoaInclusao = pessoaService.findById(doc.getUsuarioInclusao().getIdUsuario());
		PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao = null;
		
		// Se a pessoa possui documentos de identificação...
		if (pessoaInclusao.getPessoaDocumentoIdentificacaoList() != null && pessoaInclusao.getPessoaDocumentoIdentificacaoList().size() > 0) {
			for (PessoaDocumentoIdentificacao pdi : pessoaInclusao.getPessoaDocumentoIdentificacaoList()) {
				if (pdi.getAtivo() && pdi.getDocumentoPrincipal()){
					pessoaDocumentoIdentificacao = pdi;
					break;
				}
			}
		}
		return pessoaDocumentoIdentificacao;
	}

	/**
	 * @author Gabriel Azevedo
	 * @issue [PJEII-1432] [PJEII-1547]
	 * @category PJE-JT
	 * @return retorna um objeto ProcessoParte se a parte já existir e null caso não exista.
	 */
	public static List<ProcessoParte> existeParte(ProcessoTrf processoTrf, Pessoa p, ProcessoParteParticipacaoEnum polo) {
		return existeParte(processoTrf, p, polo, null, null);
	}

	@SuppressWarnings("unchecked")
	public static List<ProcessoParte> existeParte(ProcessoTrf processoTrf, Pessoa p, ProcessoParteParticipacaoEnum polo, TipoParte tipoParte, ProcessoParteSituacaoEnum parteSituacao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ppa from ProcessoParte ppa ");
		sb.append("where ");
		sb.append("ppa.processoTrf = :processoTrf ");
		sb.append("and ppa.pessoa = :pessoa ");
		sb.append("and ppa.inParticipacao = :polo ");
		sb.append("and ppa.inSituacao = :situacao ");
		if (tipoParte != null) {
			sb.append("and ppa.tipoParte.idTipoParte = :tipoParte ");
		}

		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrf", processoTrf);
		q.setParameter("pessoa", p);
		q.setParameter("polo", polo);
		if (parteSituacao != null) {
			q.setParameter("situacao", parteSituacao);
		} else {
			q.setParameter("situacao", ProcessoParteSituacaoEnum.A);
		}
		if (tipoParte != null) {
			q.setParameter("tipoParte", tipoParte.getIdTipoParte());
		}
		return q.getResultList();
	}

	/**
	 * @author Gabriel Azevedo
	 * @issue [PJEII-1432] [PJEII-1547]
	 * @category PJE-JT
	 * @return retorna um objeto ProcessoParteRepresentante se a parte representante já existir e null caso não exista.
	 */
	public static ProcessoParteRepresentante existeParteRepresentante(ProcessoParte parte, ProcessoParte parteRepresentante) {

		String s = "select ppr from ProcessoParteRepresentante ppr where ppr.processoParte.idProcessoParte = :idParte "
				+ " and ppr.representante = :representante and ppr.tipoRepresentante = :tipoRepresentante ";
		Query q = EntityUtil.getEntityManager().createQuery(s);
		q.setParameter("idParte", parte.getIdProcessoParte()).setParameter("representante", parteRepresentante.getPessoa()).setParameter
			("tipoRepresentante", parteRepresentante.getTipoParte());

		if (q.getResultList().size() > 0) {
			return (ProcessoParteRepresentante) q.getResultList().get(0);
		}
		return null;
	}

	/**
	 * @author Gabriel Azevedo
	 * @issue [PJEII-1432] [PJEII-1547]
	 * @category PJE-JT
	 * @return retorna um objeto Pessoa se a pessoa já existir e null caso não exista.
	 */
	public static Pessoa existePessoa(String login) {

		String s = "select p from Pessoa p where p.login = :login ";
		Query q = EntityUtil.getEntityManager().createQuery(s);
		q.setParameter("login", login);

		if (q.getResultList().size() > 0) {
			return (Pessoa) q.getResultList().get(0);
		}
		return null;
	}

	/**
	 * @author Gabriel Azevedo
	 * @issue [PJEII-1432] [PJEII-1547]
	 * @category PJE-JT
	 * @return retorna um objeto PessoaDocumentoIdentificacao se o documento já existir e null caso não exista.
	 */
	public static PessoaDocumentoIdentificacao existeDocumento(Pessoa pessoa, TipoDocumentoIdentificacao tipoDocumentoIdentificacao) {

		StringBuilder hql = new StringBuilder();
		hql.append("select pdi ");
		hql.append("from PessoaDocumentoIdentificacao pdi ");
		hql.append("where ");
		hql.append("	pdi.tipoDocumento = :tipoDocumento and ");
		hql.append("	pdi.pessoa.idUsuario = :idpessoa ");
		
		Query q = EntityUtil.getEntityManager().createQuery(hql.toString());
		q.setParameter("tipoDocumento", tipoDocumentoIdentificacao).setParameter("idpessoa", pessoa.getIdUsuario());
		q.setMaxResults(1);
		try {
			return (PessoaDocumentoIdentificacao) q.getSingleResult();
		} catch (NoResultException no) {
			return null;
		}
	}
	public static PessoaDocumentoIdentificacao existeDocumento(Pessoa pessoa, TipoDocumentoIdentificacao tipoDocumentoIdentificacao, String numeroDocumento) {

		StringBuilder hql = new StringBuilder();
		hql.append("select pdi ");
		hql.append("from PessoaDocumentoIdentificacao pdi ");
		hql.append("where ");
		hql.append("	pdi.ativo = true and");
		hql.append("	pdi.tipoDocumento = :tipoDocumento and ");
		hql.append("	pdi.pessoa.idUsuario = :idpessoa and ");
		hql.append("	LTRIM(REGEXP_REPLACE(pdi.numeroDocumento, '\\D', '', 'gi'),'0') = LTRIM(REGEXP_REPLACE(:numeroDocumento, '\\D', '', 'gi'),'0')");
		
		Query q = EntityUtil.getEntityManager().createQuery(hql.toString());
		q.setParameter("tipoDocumento", tipoDocumentoIdentificacao);
		q.setParameter("idpessoa", pessoa.getIdUsuario());
		q.setParameter("numeroDocumento", numeroDocumento);
		q.setMaxResults(1);
		try {
			return (PessoaDocumentoIdentificacao) q.getSingleResult();
		} catch (NoResultException no) {
			return null;
		}

	}

	private static boolean existeProcessoEvento(ProcessoEvento movimentoProcesso) {
		boolean retorno = false;
		String s = "select pe from ProcessoEvento pe where pe.processo = :processo "
				+ " and pe.evento = :evento and pe.dataAtualizacao = :data ";
		Query q = EntityUtil.getEntityManager().createQuery(s);
		q.setParameter("processo", movimentoProcesso.getProcesso()).
			setParameter("evento", movimentoProcesso.getEvento()).setParameter("data", movimentoProcesso.getDataAtualizacao());
		if (q.getResultList().size() > 0) {
			retorno = true;
		}
		return retorno;
	}

	public static void montarListaMovimentoProcesso(ProcessoTrf processoTrf,
			List<br.jus.cnj.intercomunicacao.v222.beans.MovimentacaoProcessual> eventosProcessoInterop) throws Exception {
		for (br.jus.cnj.intercomunicacao.v222.beans.MovimentacaoProcessual movimentacao : eventosProcessoInterop) {
			ProcessoEvento movimentoProcesso = montarMovimentoProcesso(movimentacao);
			movimentoProcesso.setProcesso(processoTrf.getProcesso());

			if (!existeProcessoEvento(movimentoProcesso)) {
				// MovimentoProcessoManager
				processoTrf.getProcesso().getProcessoEventoList().add(movimentoProcesso);
			}
		}
	}

	private static ProcessoEvento montarMovimentoProcesso(
			br.jus.cnj.intercomunicacao.v222.beans.MovimentacaoProcessual eventoInterop) throws Exception {

		LancadorMovimentosService lancadorMovimentosService = ComponentUtil.getComponent(LancadorMovimentosService.NAME);
		Evento evento = lancadorMovimentosService.getEventoProcessualByCodigoCnj(eventoInterop.getMovimentoNacional().getCodigoNacional() + "");

		ProcessoEvento movimentoEvento = new ProcessoEvento();
		// movimentoEvento.setIdProcessoEvento(Integer.parseInt(eventoInterop.getIdentificadorMovimento()));
		movimentoEvento.setDataAtualizacao(DateUtil.stringToDate(
				eventoInterop.getDataHora().getValue(), "yyyyMMddHms"));
		movimentoEvento.setEvento(evento);
		movimentoEvento.setTextoFinalExterno(eventoInterop.getMovimentoNacional().getComplemento().get(0).getValue());
		movimentoEvento.setTextoFinalInterno(eventoInterop.getMovimentoNacional().getComplemento().get(0).getValue());
		movimentoEvento.setProcessoDocumento(null);

		return movimentoEvento;

	}
	


	/**
	 * Verifica os atributos do objeto ProcessoTrf para que a mensagem de erro seja
	 * exibida dentro de um contexto amigável para o usuário
	 * 
	 * @param processo
	 * @throws Exception 
	 */
	public static void validarAtributosProcessoTrf(ProcessoTrf processo) throws Exception{
		String erro = new String();
		
		if(processo.getTutelaLiminar() == null){
			erro = FacesUtil.getMessage("intercomunicacaoUtil.processoTrf.tutelaLiminarIsNull");
		}
		
		/*TODO
		 * Demais atributos que precisam ser verificados
		 */
		
		if (!erro.isEmpty()){
			throw new Exception(erro);
		}
	}
	
	/**
	 * Verifica os atributos do objeto ProcessoDocumento para que a mensagem de erro seja
	 * exibida dentro de um contexto amigável para o usuário
	 * 
	 * @param doc
	 * @throws Exception 
	 */
	public static void validarAtributosProcessoDocumento(ProcessoDocumento doc) throws Exception{
		String erro = new String();
		
		/*TODO
		 * Demais atributos que precisam ser verificados
		 */
		
		if(doc.getInstancia() == null){
			erro = FacesUtil.getMessage("entity_messages","intercomunicacaoUtil.processoDocumento.instanciaIsNull",doc);
		}
		
		
		if (!erro.isEmpty()){
			throw new Exception(erro);
		}
	}

	/**
	 * Adiciona os parâmetros necessários ao documento.
	 * 
	 * @param documentoMNI
	 * @param documentoPJE
	 * @throws Exception
	 */
	public static void adicionarOutrosParametros(DocumentoProcessual mniDoc, ProcessoDocumento doc) throws PJeBusinessException {
		PessoaService pessoaService = (PessoaService) Component.getInstance(PessoaService.NAME);
		
		Parametro documentoValido = new Parametro();
		documentoValido.setNome(MNIParametro.PARAM_DOCUMENTO_VALIDO);
		documentoValido.setValor(doc.getProcessoDocumentoBin().getValido().toString());

		Parametro idArquivoOrigem = new Parametro();
		idArquivoOrigem.setNome(MNIParametro.PARAM_ID_ARQUIVO_ORIGEM);
		idArquivoOrigem.setValor(Long.toString(doc.getIdProcessoDocumento()));

		Parametro nomeDocumento = new Parametro();
		nomeDocumento.setNome(MNIParametro.PARAM_NOME_DOCUMENTO);
		nomeDocumento.setValor(doc.getProcessoDocumento());

		Parametro instanciaDocumento = new Parametro();
		instanciaDocumento.setNome(MNIParametro.PARAM_INSTANCIA_DOCUMENTO);
		instanciaDocumento.setValor(doc.getInstancia());

		Parametro nomeArquivo = new Parametro();
		nomeArquivo.setNome(MNIParametro.PARAM_NOME_ARQUIVO);
		nomeArquivo.setValor(doc.getProcessoDocumentoBin().getNomeArquivo());

		Parametro criadorArquivo = new Parametro();
		criadorArquivo.setNome(MNIParametro.PARAM_CRIADOR_ARQUIVO);
		criadorArquivo.setValor(doc.getNomeUsuarioInclusao());
		
		Parametro pessoaJuntadaArquivo = new Parametro();
		pessoaJuntadaArquivo.setNome(MNIParametro.PARAM_PESSOA_JUNTADA_ARQUIVO);
		pessoaJuntadaArquivo.setValor(doc.getNomeUsuarioJuntada());
		
		/**
		 * Campo número, opcional na criação do documento, a ser passado na remessa RN547
		 */
		Parametro numeroDocumento = new Parametro();
		numeroDocumento.setNome(MNIParametro.PARAM_NUMERO_DOCUMENTO);
		numeroDocumento.setValor(doc.getNumeroDocumento());
		
		if (ParametroUtil.instance().isBaseBinariaUnificada()){
			Parametro storageId = new Parametro();
			storageId.setNome(MNIParametro.PARAM_STORAGE_ID);
			storageId.setValor(doc.getProcessoDocumentoBin().getNumeroDocumentoStorage());
			mniDoc.getOutroParametro().add(storageId);
		}
		
		Parametro dataJuntada = new Parametro();
		dataJuntada.setNome(MNIParametro.PARAM_DATA_JUNTADA);
		dataJuntada.setValor(Long.toString(doc.getDataJuntada().getTime()));
		
		Parametro dataInclusao = new Parametro();
		dataInclusao.setNome(MNIParametro.PARAM_DATA_INCLUSAO);
		dataInclusao.setValor(Long.toString(doc.getDataInclusao().getTime()));
		
		if(doc.getNumeroOrdem() != null) {
			Parametro numeroOrdem = new Parametro();
			numeroOrdem.setNome(MNIParametro.PARAM_NUMERO_ORDEM);
			numeroOrdem.setValor(String.valueOf(doc.getNumeroOrdem()));
			mniDoc.getOutroParametro().add(numeroOrdem);
		}
		
		// PJEII-19607 - Identificar o criador do documento pelo documento de identificação da pessoa
		Parametro documentoIdentificacaoCriadorArquivo = null; 
		Parametro tipoDocumentoIdentificacaoCriadorArquivo = null;
		PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao = getPessoaDocumentoIdentificacaoCriadorDocumento(
				pessoaService, doc);
		
		// Se o documento de identificação da pessoa não é nulo...
		// PJEII-19607 - Identificar o criador do documento pelo documento de identificação da pessoa		
		if (pessoaDocumentoIdentificacao != null) {
			tipoDocumentoIdentificacaoCriadorArquivo = new Parametro();
			tipoDocumentoIdentificacaoCriadorArquivo.setNome(MNIParametro.PARAM_TIPO_DOCUMENTO_IDENTIFICACAO_CRIADOR_ARQUIVO);
			tipoDocumentoIdentificacaoCriadorArquivo.setValor(pessoaDocumentoIdentificacao.getTipoDocumento().getCodTipo());
			
			documentoIdentificacaoCriadorArquivo = new Parametro();
			documentoIdentificacaoCriadorArquivo.setNome(MNIParametro.PARAM_DOCUMENTO_IDENTIFICACAO_CRIADOR_ARQUIVO);
			documentoIdentificacaoCriadorArquivo.setValor(pessoaDocumentoIdentificacao.getNumeroDocumento());	
			
			mniDoc.getOutroParametro().add(documentoIdentificacaoCriadorArquivo);	
			mniDoc.getOutroParametro().add(tipoDocumentoIdentificacaoCriadorArquivo);	
		}
		
		criarParametroPessoaJuntada(mniDoc, doc, pessoaService);

		Parametro linkValidacao = new Parametro();
		linkValidacao.setNome(MNIParametro.PARAM_LINK_VALIDACAO);
		linkValidacao.setValor(ValidacaoAssinaturaProcessoDocumento.instance().geraUrlValidacaoDocumento(doc.getProcessoDocumentoBin()));
		
		mniDoc.getOutroParametro().add(idArquivoOrigem);
		mniDoc.getOutroParametro().add(documentoValido);
		mniDoc.getOutroParametro().add(nomeDocumento);
		mniDoc.getOutroParametro().add(instanciaDocumento);
		mniDoc.getOutroParametro().add(nomeArquivo);
		mniDoc.getOutroParametro().add(criadorArquivo);
		mniDoc.getOutroParametro().add(numeroDocumento);
		mniDoc.getOutroParametro().add(dataInclusao);
		mniDoc.getOutroParametro().add(dataJuntada);		
		mniDoc.getOutroParametro().add(linkValidacao);
		mniDoc.getOutroParametro().add(pessoaJuntadaArquivo);
	}

	private static PessoaDocumentoIdentificacao getPessoaDocumentoIdentificacaoJuntadaDocumento(
			PessoaService pessoaService, ProcessoDocumento doc)
			throws PJeBusinessException {
		
		Pessoa pessoaJuntada = pessoaService.findById(doc.getUsuarioJuntada().getIdUsuario());
		PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao = null;
		
		// Se a pessoa possui documentos de identificao...
		if (pessoaJuntada.getPessoaDocumentoIdentificacaoList() != null && (!pessoaJuntada.getPessoaDocumentoIdentificacaoList().isEmpty())) {
			for (PessoaDocumentoIdentificacao pdi : pessoaJuntada.getPessoaDocumentoIdentificacaoList()) {
				if (pdi.getAtivo() && pdi.getDocumentoPrincipal()){
					pessoaDocumentoIdentificacao = pdi;
					break;
				}
			}
		}
		return pessoaDocumentoIdentificacao;
	}

	private static void criarParametroPessoaJuntada(DocumentoProcessual documentoMNI, ProcessoDocumento documentoPJE,
			PessoaService pessoaService )
			throws PJeBusinessException {
		PessoaDocumentoIdentificacao pessoaDocumentoIdentificacaoUsuarioJuntada = null;
		
		if(documentoPJE.getUsuarioJuntada() != null) {
			pessoaDocumentoIdentificacaoUsuarioJuntada = getPessoaDocumentoIdentificacaoJuntadaDocumento(
				pessoaService, documentoPJE);
		
			if (pessoaDocumentoIdentificacaoUsuarioJuntada != null) {
				Parametro documentoIdentificacaoUsuarioJuntadaArquivo = null;
				Parametro tipoDocumentoIdentificacaoUsuarioJuntadaArquivo = null;
	
				documentoIdentificacaoUsuarioJuntadaArquivo = new Parametro();
				documentoIdentificacaoUsuarioJuntadaArquivo.setNome(MNIParametro.PARAM_DOCUMENTO_IDENTIFICACAO_PESSOA_JUNTADA_ARQUIVO);
				documentoIdentificacaoUsuarioJuntadaArquivo.setValor(pessoaDocumentoIdentificacaoUsuarioJuntada.getNumeroDocumento());	
				
				tipoDocumentoIdentificacaoUsuarioJuntadaArquivo = new Parametro();
				tipoDocumentoIdentificacaoUsuarioJuntadaArquivo.setNome(MNIParametro.PARAM_TIPO_DOCUMENTO_IDENTIFICACAO_PESSOA_JUNTADA_ARQUIVO);
				tipoDocumentoIdentificacaoUsuarioJuntadaArquivo.setValor(pessoaDocumentoIdentificacaoUsuarioJuntada.getTipoDocumento().getCodTipo());
				
				documentoMNI.getOutroParametro().add(documentoIdentificacaoUsuarioJuntadaArquivo);	
				documentoMNI.getOutroParametro().add(tipoDocumentoIdentificacaoUsuarioJuntadaArquivo);	
			}
		}
	}	
}
