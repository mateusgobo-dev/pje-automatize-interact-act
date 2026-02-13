package br.jus.cnj.pje.webservice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.providers.jaxb.IgnoreMediaTypes;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.utils.Constantes;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.SessaoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.vo.PlacarSessaoVO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name("informacaoSessaoService")
@Path("informacaoSessao")
public class InformacaoSessaoServiceImpl implements InformacaoSessaoService {

	@In
	private SessaoJudicialManager sessaoJudicialManager;

	@In
	private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;
	
	@In
	private SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;
	
	@In
	private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;

	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ParametroService parametroService;
	
	@In
	private SecurityTokenControler securityTokenControler;

	@Logger
	private Log log;
	
	private static final String SESSAO_RESUMO = "pje:sessaojulgamento:informacaosessao:";
	private static final String SESSAO_RESUMO_ULTIMA_ATUALIZACAO = "pje:sessaojulgamento:informacaosessao:lastUpdate";
	private static final String SESSAO_DETALHE = "pje:sessaojulgamento:informacaosessao:detalhe:";
	private static final String SESSAO_DETALHE_ULTIMA_ATUALIZACAO = "pje:sessaojulgamento:informacaosessao:detalhe:lastUpdate";

	@Override
	@GET
	@Path("/")
	@Produces({ MediaType.APPLICATION_JSON })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	public Response recuperarInformacoes(
			@QueryParam("id_sessao") Integer idSessao) {
		Response response = null;
		try {
			response = Response.ok(montarResposta(idSessao, false), MediaType.APPLICATION_JSON_TYPE).status(Response.Status.OK.getStatusCode()).build();
		} catch (Exception e) {
			response = reportarErro(e);
		}

		return response;
	}

	private Response reportarErro(Exception e) {
		Response response;
		log.error("Erro no serviço de informações de sessão", e);
		response = Response.serverError()
				.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		return response;
	}
	
	@Override
	@GET
	@Path("/todas")
	@Produces({ MediaType.APPLICATION_JSON })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	public Response recuperarTodasSessoes(
			@QueryParam("ano") Integer ano,
			@QueryParam("id_sessao") Integer idSessao,
			@QueryParam("virtuais") Boolean somenteVirtuais,
			@QueryParam("sessoes_futuras") Boolean sessoesFuturas){
		
		Response response = null;
		
		try{
			List<Sessao> sessoes = new ArrayList<Sessao>(0);
			if(idSessao != null){
				Sessao s = sessaoJudicialManager.findById(idSessao);
				if(s != null){
					sessoes.add(s);
				}
			}else{
				if(ano == null){
					ano = Calendar.getInstance().get(Calendar.YEAR);
					
				}
				if(somenteVirtuais == null){
					somenteVirtuais = Boolean.FALSE;
				}
				if(sessoesFuturas == null){
					sessoesFuturas = Boolean.FALSE;
				}
				sessoes = sessaoJudicialManager.findByAno(ano,somenteVirtuais,sessoesFuturas);
			}
			
			InformacaoSessoesResposta sessoesResposta = new InformacaoSessoesResposta();
			
			if(sessoes != null){
				for(Sessao sessao : sessoes){
					sessoesResposta.getSessoes().add(getDadosSessao_(sessao.getIdSessao(), true, true));
				}
			}
			
			response = Response.ok(sessoesResposta, MediaType.APPLICATION_JSON_TYPE).
					status(Response.Status.OK.getStatusCode()).build();
		}
		catch(Exception e){
			response = reportarErro(e);
		}
		
		return response;
		
	}
	
	@Override
	@GET
	@Path("/voto")
	@Produces({ MediaType.APPLICATION_JSON })
	@IgnoreMediaTypes(value = "text/plain;charset=UTF-8")
	public Response recuperarVoto(@QueryParam("idJulgamento") Integer idJulgamento, @QueryParam("idOrgaoJulgador") Integer idOrgaoJulgador) {
		Response response = null;
		try {
			response = Response.ok(carregarVoto(idJulgamento, idOrgaoJulgador), MediaType.APPLICATION_JSON_TYPE).status(Response.Status.OK.getStatusCode()).build();
		} catch (Exception e) {
			response = reportarErro(e);
		}

		return response;
	}
	
	private InformacaoSessaoResposta montarResposta(Integer idSessao, boolean omitirListas, boolean reload) throws PJeBusinessException{
		InformacaoSessaoResposta resp = getDadosSessao(idSessao, reload, false);
		if(!reload){
			if(!validaTTL(false)){
				resp = getDadosSessao(idSessao, true, false);
			}
		}
		return resp;
	}
	
	private Boolean validaTTL(boolean detalhado){
		Date last = (Date) Contexts.getApplicationContext().get(getNomeVariavelUltimaAtualizacao(detalhado));
		String sttl = parametroService.valueOf("pje:sessaojulgamento:informacaosessao:ttl");
		Integer ttl = 180;
		if(sttl != null){
			ttl = Integer.parseInt(sttl);
		}
		Calendar limit = new GregorianCalendar();
		if(last != null){
			limit.setTime(last);
			limit.add(Calendar.SECOND, ttl);
		}
		if(last == null || new Date().after(limit.getTime())){
			return false;
		}
		return true;
			
	}

	private InformacaoSessaoResposta montarResposta(Integer idSessao, boolean omitirListas) throws PJeBusinessException {
		return montarResposta(idSessao, omitirListas, false);
	}
	
	
	
	private InformacaoSessaoResposta getDadosSessao(Integer idSessao, boolean reload, boolean detalhado) throws PJeBusinessException{
		InformacaoSessaoResposta isr = (InformacaoSessaoResposta) Contexts.getApplicationContext().get(getNomeVariavelCache(idSessao, detalhado));
		if(isr == null || reload){
			isr = getDadosSessao(idSessao, detalhado);
			Contexts.getApplicationContext().set(getNomeVariavelCache(idSessao, detalhado), isr);
			Contexts.getApplicationContext().set(getNomeVariavelUltimaAtualizacao(detalhado), new Date());
		}
		return isr;
	}
	
	
	private InformacaoSessaoResposta getDadosSessao_(Integer idSessao, boolean formatoSimples, boolean detalhado) throws PJeBusinessException{
		Sessao sessao = sessaoJudicialManager.findById(idSessao);
		if(formatoSimples){
			InformacaoSessaoResposta isr1 = null;
			if(validaTTL(detalhado)){
				isr1= (InformacaoSessaoResposta) Contexts.getApplicationContext().get(getNomeVariavelCache(idSessao, detalhado));
			}
			
			if(isr1 != null){
				for(InformacaoSessaoResumo res : isr1.getResumo()){
					if(res.getTipo().equals("pautados")){
						if(isr1.getProcessos().size() != res.getQuantidade()){
							carregarDadosJulgamento(isr1,sessao,null);
						}
					}
				}
				return isr1;
			}
		}
		InformacaoSessaoResposta isr = new InformacaoSessaoResposta();
		if(sessao != null){
			isr.setData(DateUtil.dateToString(sessao.getDataSessao(), "dd/MM/yyyy"));
			if(sessao.getDataFimSessao() != null){
				isr.setDataFim(DateUtil.dateToString(sessao.getDataFimSessao(), "dd/MM/yyyy"));
			}
			isr.setStatus(SessaoHome.instance().getStatus(sessao));
			isr.setIniciar(sessao.getIniciar());
            isr.setDataRealizacaoSessao(DateUtil.dateToString(sessao.getDataRealizacaoSessao(), "dd/MM/yyyy"));
			isr.setHorarioInicio(obterHorarioInicioSessao(sessao));
			isr.setHorarioFim(obterHorarioFinalSessao(sessao));
			
			if(sessao.getTipoSessao() != null) {
				isr.setTipoSessao(sessao.getTipoSessao().getTipoSessao());
			}
			
			isr.setSessao(sessao.getApelido());
			isr.setId(sessao.getIdSessao());
			isr.setVirtual(sessao.getContinua());
			Long totalPautados = sessaoPautaProcessoTrfManager.totalIncluidos(sessao);
			InformacaoSessaoResumo resumoTotalPautados = new InformacaoSessaoResumo();
			resumoTotalPautados.setQuantidade(totalPautados.intValue());
			resumoTotalPautados.setTipo("pautados");
			isr.getResumo().add(resumoTotalPautados);

			if(!formatoSimples){
				Long totalEmJulgamento = sessaoPautaProcessoTrfManager.totalEmSituacao(sessao, TipoSituacaoPautaEnum.EJ);
				InformacaoSessaoResumo resumoTotalEmJulgamento = new InformacaoSessaoResumo();
				resumoTotalEmJulgamento.setQuantidade(totalEmJulgamento.intValue());
				resumoTotalEmJulgamento.setTipo("emJulgamento");
				isr.getResumo().add(resumoTotalEmJulgamento);
		
				Long totalPendentes = sessaoPautaProcessoTrfManager.totalEmSituacao(sessao, TipoSituacaoPautaEnum.AJ);
				InformacaoSessaoResumo resumoTotalPendentes = new InformacaoSessaoResumo();
				resumoTotalPendentes.setQuantidade(totalPendentes.intValue());
				resumoTotalPendentes.setTipo("pendentesJulgamento");
				isr.getResumo().add(resumoTotalPendentes);
		
				Long totalJulgados = sessaoPautaProcessoTrfManager.totalJulgados(sessao);
				InformacaoSessaoResumo resumoTotalJulgados = new InformacaoSessaoResumo();
				resumoTotalJulgados.setQuantidade(totalJulgados.intValue());
				resumoTotalJulgados.setTipo("julgados");
				isr.getResumo().add(resumoTotalJulgados);
		
				Long totalPedidosDeVista = sessaoPautaProcessoTrfManager.totalComVista(sessao);
				InformacaoSessaoResumo resumoTotalPedidoDeVista = new InformacaoSessaoResumo();
				resumoTotalPedidoDeVista.setQuantidade(totalPedidosDeVista.intValue());
				resumoTotalPedidoDeVista.setTipo("pedidoVista");
				isr.getResumo().add(resumoTotalPedidoDeVista);
		
				Long totalRetirado = sessaoPautaProcessoTrfManager.totalAdiados(sessao) + sessaoPautaProcessoTrfManager.totalRetirados(sessao);
				InformacaoSessaoResumo resumoTotalRetirado = new InformacaoSessaoResumo();
				resumoTotalRetirado.setQuantidade(totalRetirado.intValue());
				resumoTotalRetirado.setTipo("retiradoPautaAdiadoConvertidoDiligencia");
				isr.getResumo().add(resumoTotalRetirado);
			}
		}
		if(formatoSimples){
			carregarDadosJulgamento(isr, sessao,null);
			Contexts.getApplicationContext().set(getNomeVariavelCache(idSessao, detalhado),isr);
			Contexts.getApplicationContext().set(getNomeVariavelUltimaAtualizacao(detalhado), new Date());
		}
		else{
			carregarDadosJulgamento(isr, sessao);
		}
		return isr;
	}

	/**
	 * Obtem o horario de inicio da sessao.
	 * @param sessao - Sessao de julgamento.
	 * @return String contendo o horario no formato hh:mm
	 */
	private String obterHorarioInicioSessao(Sessao sessao) {
		String horaInicio = null;
		if(sessao.getHorarioInicio() != null){
			horaInicio = DateUtil.dateToHour(sessao.getHorarioInicio());
		} else {
			horaInicio = DateUtil.dateToHour(sessao.getOrgaoJulgadorColegiadoSalaHorario().getHoraInicial());
		}
		return horaInicio;
	}
	
	/**
	 * Obtem o horario final da sessao.
	 * @param sessao - Sessao de julgamento.
	 * @return String contendo o horario no formato hh:mm
	 */
	private String obterHorarioFinalSessao(Sessao sessao) {
		String horaFim = null;
		if(sessao.getDataFechamentoSessao() != null){
			horaFim = DateUtil.dateToHour(sessao.getDataFechamentoSessao());
		} else if(sessao.getOrgaoJulgadorColegiadoSalaHorario() != null) {
			horaFim = DateUtil.dateToHour(sessao.getOrgaoJulgadorColegiadoSalaHorario().getHoraFinal());
		}
		return horaFim;
	}
	
	private InformacaoSessaoResposta getDadosSessao(Integer idSessao, boolean detalhado) throws PJeBusinessException{
		return getDadosSessao_(idSessao, false, detalhado);	
	}

	private void carregarDadosJulgamento(InformacaoSessaoResposta isr, Sessao sessao){
		carregarDadosJulgamento(isr, sessao,TipoSituacaoPautaEnum.EJ);
	}
	
	private void carregarDadosJulgamento(InformacaoSessaoResposta isr, Sessao sessao,TipoSituacaoPautaEnum situacao){
		List<SessaoPautaProcessoTrf> emjulgamento = sessaoPautaProcessoTrfManager.getProcessoSessao(sessao, situacao);
		isr.getProcessos().clear();
		isr.getProcessosEmJulgamento().clear();
		for (SessaoPautaProcessoTrf sessaoPautaProcessoTrf : emjulgamento) {
			InformacaoSessaoProcesso informacaoSessaoProcesso = new InformacaoSessaoProcesso();
			informacaoSessaoProcesso.setNrProcesso(sessaoPautaProcessoTrf.getProcessoTrf().getNumeroProcesso());
			if(sessaoPautaProcessoTrf.getProcessoTrf().getSegredoJustica() != null &&
					!sessaoPautaProcessoTrf.getProcessoTrf().getSegredoJustica()){
				informacaoSessaoProcesso.setLink(montaLink(sessaoPautaProcessoTrf.getProcessoTrf().getIdProcessoTrf()));
			}
			informacaoSessaoProcesso.setIdJulgamento(sessaoPautaProcessoTrf.getIdSessaoPautaProcessoTrf());
			informacaoSessaoProcesso.setSequencial(sessaoPautaProcessoTrf.getNumeroOrdem());
			informacaoSessaoProcesso.setClasse(sessaoPautaProcessoTrf.getProcessoTrf().getClasseJudicial().getClasseJudicial());
			informacaoSessaoProcesso.setIdRelator(sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador().getIdOrgaoJulgador());
			informacaoSessaoProcesso.setRelator(sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador().getOrgaoJulgador());
			informacaoSessaoProcesso.setSituacaoJulgamento(getSituacaoJulgamento(sessaoPautaProcessoTrf));
			if(sessaoPautaProcessoTrf.getSituacaoJulgamento() != null && sessaoPautaProcessoTrf.getSituacaoJulgamento() == TipoSituacaoPautaEnum.EJ){
				List<InformacaoSessaoProcessoPlacar> placar = carregarPlacar(sessaoPautaProcessoTrf);
				informacaoSessaoProcesso.getPlacar().clear();
				informacaoSessaoProcesso.getPlacar().addAll(placar);
				isr.getProcessosEmJulgamento().add(informacaoSessaoProcesso);
			}
			if(sessaoPautaProcessoTrf.getSituacaoJulgamento() != null && sessaoPautaProcessoTrf.getSituacaoJulgamento() == TipoSituacaoPautaEnum.JG){
				List<InformacaoSessaoProcessoPlacar> placar = carregarPlacar(sessaoPautaProcessoTrf);
				informacaoSessaoProcesso.getPlacar().clear();
				informacaoSessaoProcesso.getPlacar().addAll(placar);
				informacaoSessaoProcesso.setProclamacao(sessaoPautaProcessoTrf.getProclamacaoDecisao());
				informacaoSessaoProcesso.setVencedor(sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor().getOrgaoJulgador());
			}
			if (sessaoPautaProcessoTrf.getProcessoTrf().getComplementoJE() != null) {
				StringBuilder sbOrigem = new StringBuilder();
				
				if(sessaoPautaProcessoTrf.getProcessoTrf().getComplementoJE().getEstadoEleicao()!=null) {
					sbOrigem.append(sessaoPautaProcessoTrf.getProcessoTrf().getComplementoJE().getEstadoEleicao().getCodEstado());
				}
				
				sbOrigem.append(" - ");
				
				if( sessaoPautaProcessoTrf.getProcessoTrf().getComplementoJE().getMunicipioEleicao()!=null) {
					sbOrigem.append(sessaoPautaProcessoTrf.getProcessoTrf().getComplementoJE().getMunicipioEleicao().getMunicipio());
				}
				informacaoSessaoProcesso.setOrigem(sbOrigem.toString());
			} 
			isr.getProcessos().add(informacaoSessaoProcesso);
		}
	}
	
	private String getSituacaoJulgamento(SessaoPautaProcessoTrf sppt){
		String sitJulg = null;
		if(sppt.getSituacaoJulgamento() != TipoSituacaoPautaEnum.NJ){
			sitJulg = sppt.getSituacaoJulgamento().getLabel();
		}
		else{
			if(sppt.getRetiradaJulgamento()){
				sitJulg = "Retirado de julgamento";
			}
			else{
				if(sppt.getAdiadoVista() == null){
					sitJulg = "Não julgado";
				}
				else{
					sitJulg= sppt.getAdiadoVista().getLabel();
				}
			}
		}
		return sitJulg;
	}
	
	private List<InformacaoSessaoProcessoPlacar> carregarPlacar(SessaoPautaProcessoTrf julgamento){
			Map<Integer,String> orgaos = carregaOrgaos();
			SessaoProcessoDocumentoVotoManager votosManager = (SessaoProcessoDocumentoVotoManager)Component.getInstance(SessaoProcessoDocumentoVotoManager.class);
			OrgaoJulgadorManager orgaoJulgadorManager = (OrgaoJulgadorManager)Component.getInstance(OrgaoJulgadorManager.class);

			PlacarSessaoVO p = votosManager.getPlacarCondutores(julgamento.getSessao(), julgamento.getProcessoTrf(), true);

			Map<Integer, Set<Integer>> placarOjs = p.getMapaPlacar();
			List<InformacaoSessaoProcessoPlacar> placares = new ArrayList<InformacaoSessaoProcessoPlacar>();
			
			for(Map.Entry<Integer, Set<Integer>> entry: placarOjs.entrySet()){
				InformacaoSessaoProcessoPlacar placar = new InformacaoSessaoProcessoPlacar();
				OrgaoJulgador oj = null;
				SessaoProcessoDocumentoVoto voto = null;
				try {
					oj = orgaoJulgadorManager.findById(entry.getKey());
					voto = votosManager.recuperarVoto(julgamento.getSessao(), julgamento.getProcessoTrf(), oj);
				} catch (PJeBusinessException e) {
					// swallow
				}
				String tipoVoto = voto != null ? voto.getTipoVoto() != null ? voto.getTipoVoto().getTipoVoto() : orgaos.get(entry.getKey()):orgaos.get(entry.getKey());
				placar.setTipoVoto(tipoVoto );
				
				for(Integer idOJ : entry.getValue()){
					InformacaoSessaoProcessoVotante votante = new InformacaoSessaoProcessoVotante();
					votante.setIdOrgaoJulgador(idOJ);
					votante.setNomeOrgaoJulgador(orgaos.get(idOJ));
					SessaoProcessoDocumentoVoto votoOJ = sessaoProcessoDocumentoVotoManager.recuperarVoto(julgamento.getSessao(), julgamento.getProcessoTrf(), idOJ);
					if(podeExibirDocumentosSessao(votoOJ, julgamento.getProcessoTrf())) {
						votante.setVotoPossuiConteudo(verificarSeVotoPossuiConteudo(julgamento, votoOJ));

						votante.setAcompanhaRelator(votoOJ.getOjAcompanhado().getIdOrgaoJulgador() == julgamento.getProcessoTrf().getOrgaoJulgador().getIdOrgaoJulgador());
						placar.getVotantes().add(votante);
					}
				}
				placar.setQuantidade(entry.getValue().size());
				
				placares.add(placar);
			}
			
			Set<Integer> impedidos = votosManager.getImpedidos(julgamento.getSessao(), julgamento.getProcessoTrf(), true);
			InformacaoSessaoProcessoPlacar impedido = new InformacaoSessaoProcessoPlacar();
			impedido.setTipoVoto("Impedimentos");
			impedido.setQuantidade(impedidos.size());
			for(Integer idOJ : impedidos){
				InformacaoSessaoProcessoVotante votante = new InformacaoSessaoProcessoVotante();
				votante.setIdOrgaoJulgador(idOJ);
				votante.setNomeOrgaoJulgador(orgaos.get(idOJ));
				SessaoProcessoDocumentoVoto votoOJ = sessaoProcessoDocumentoVotoManager.recuperarVoto(julgamento.getSessao(), julgamento.getProcessoTrf(), idOJ);
				votante.setVotoPossuiConteudo(verificarSeVotoPossuiConteudo(julgamento, votoOJ));
				impedido.getVotantes().add(votante);
			}
			placares.add(impedido);
			
			Set<Integer> omissos = votosManager.getOmissos(julgamento.getSessao(),julgamento.getProcessoTrf(), true);
			InformacaoSessaoProcessoPlacar omisso = new InformacaoSessaoProcessoPlacar();
			omisso.setTipoVoto("Omissos");
			omisso.setQuantidade(omissos.size());
			for(Integer idOJ : omissos){
				InformacaoSessaoProcessoVotante votante = new InformacaoSessaoProcessoVotante();
				votante.setIdOrgaoJulgador(idOJ);
				votante.setNomeOrgaoJulgador(orgaos.get(idOJ));
				votante.setVotoPossuiConteudo(false);
				omisso.getVotantes().add(votante);
			}
			placares.add(omisso);
			
			return placares;
	}
	
	private boolean verificarSeVotoPossuiConteudo(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, SessaoProcessoDocumentoVoto voto) {
		boolean retorno = true;
		if (voto.getProcessoDocumento() != null) {
			if (sessaoPautaProcessoTrf.getProcessoTrf().getSegredoJustica() != null && sessaoPautaProcessoTrf.getProcessoTrf().getSegredoJustica()
					|| voto.getProcessoDocumento().getDocumentoSigiloso()
						|| (!podeExibirDocumentosSessao(voto, sessaoPautaProcessoTrf.getProcessoTrf()))) {
				retorno = false;
			}
		} else {
			retorno = false;
		}
		return retorno;
	}

	private InformacaoVotosResposta carregarVoto(Integer idSessaoProcesso, Integer idOrgaoJulgador){
		try {
			SessaoPautaProcessoTrf sessaoProcesso = sessaoPautaProcessoTrfManager.findById(idSessaoProcesso);
			if(isCarregarVoto(sessaoProcesso, idOrgaoJulgador)){
				InformacaoVotosResposta resposta = new InformacaoVotosResposta();
				SessaoProcessoDocumentoVotoManager votoManager = (SessaoProcessoDocumentoVotoManager)Component.getInstance(SessaoProcessoDocumentoVotoManager.class);
				SessaoProcessoDocumentoVoto voto = votoManager.recuperarVoto(sessaoProcesso.getSessao(), sessaoProcesso.getProcessoTrf(), idOrgaoJulgador);
				if(podeExibirDocumentosSessao(voto, sessaoProcesso.getProcessoTrf()) ) {
					InformacaoSessaoProcessoDocumentoVoto informacaoVoto = new InformacaoSessaoProcessoDocumentoVoto();
					informacaoVoto.setIdSessaoProcessoDocumentoVoto(voto.getIdSessaoProcessoDocumento());
					informacaoVoto.setOrgaoJulgadorVoto(voto.getOrgaoJulgador().getOrgaoJulgador());
					informacaoVoto.setIdOrgaoJulgador(voto.getOrgaoJulgador().getIdOrgaoJulgador());
					if(ComponentUtil.getDocumentoJudicialService().podeExibirPublicamente(voto.getProcessoDocumento(), ParametroUtil.instance().isLiberaDocumentoSessaoAssinatura())){
						informacaoVoto.setConteudoVoto(voto.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
					}
					adicionarInformacaoOutrosDocumentos(voto, sessaoProcesso, informacaoVoto);
					resposta.setIdProcesso(sessaoProcesso.getProcessoTrf().getIdProcessoTrf());
					resposta.setIdSessao(sessaoProcesso.getSessao().getIdSessao());
					resposta.setVoto(informacaoVoto);
				}
				return resposta;
			}
			
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean isCarregarVoto(SessaoPautaProcessoTrf sessaoProcesso, Integer idOrgaoJulgador) {
		return sessaoProcesso != null 
				&& idOrgaoJulgador != null 
				&& Boolean.FALSE.equals(sessaoProcesso.getProcessoTrf().getSegredoJustica());
	}
	
	private void adicionarInformacaoOutrosDocumentos(SessaoProcessoDocumentoVoto voto, SessaoPautaProcessoTrf sessaoProcesso, 
			InformacaoSessaoProcessoDocumentoVoto informacaoVoto){
		try{
			if(voto.getTipoVoto().getRelator()){
				informacaoVoto.setListaInformacaoSessaoProcessoDocumento(new ArrayList<InformacaoSessaoProcessoDocumento>());
				SessaoProcessoDocumento relatorio = recuperarRelatorio(sessaoProcesso);
				if(podeExibirDocumentosSessao(relatorio, sessaoProcesso.getProcessoTrf()) ) {
					InformacaoSessaoProcessoDocumento informacaoRelatorio = new InformacaoSessaoProcessoDocumento();
					informacaoRelatorio.setIdSessaoProcessoDocumento(relatorio.getIdSessaoProcessoDocumento());
					informacaoRelatorio.setIdTipoProcessoDocumento(relatorio.getProcessoDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento());
					if(ComponentUtil.getDocumentoJudicialService().podeExibirPublicamente(relatorio.getProcessoDocumento(), ParametroUtil.instance().isLiberaDocumentoSessaoAssinatura())){
						informacaoRelatorio.setConteudo(relatorio.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
					}
					informacaoVoto.getListaInformacaoSessaoProcessoDocumento().add(informacaoRelatorio);
				}
				SessaoProcessoDocumento ementa = recuperarEmenta(sessaoProcesso);
				if(ComponentUtil.getDocumentoJudicialService().podeExibirPublicamente(ementa.getProcessoDocumento(), ParametroUtil.instance().isLiberaDocumentoSessaoAssinatura())){
					InformacaoSessaoProcessoDocumento informacaoEmenta = new InformacaoSessaoProcessoDocumento();
					informacaoEmenta.setIdSessaoProcessoDocumento(ementa.getIdSessaoProcessoDocumento());
					informacaoEmenta.setIdTipoProcessoDocumento(ementa.getProcessoDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento());
					if(ementa.getProcessoDocumento() != null){
						informacaoEmenta.setConteudo(ementa.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
					}
					informacaoVoto.getListaInformacaoSessaoProcessoDocumento().add(informacaoEmenta);
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}

	private boolean podeExibirDocumentosSessao(SessaoProcessoDocumento spd, ProcessoTrf processo) {
		boolean retorno = false;
		SessaoPautaProcessoTrf sessaoPauta = ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrf(processo, spd.getSessao());
		if(sessaoPauta != null && (sessaoPauta.isJulgamentoFinalizado() || spd.getSessao().getDataRealizacaoSessao() != null ) && TipoSituacaoPautaEnum.JG.equals(sessaoPauta.getSituacaoJulgamento()) && spd != null && spd.getLiberacao() && spd.getSessao().getDataAberturaSessao() != null && processo.getSegredoJustica().equals(Boolean.FALSE) ) {
			retorno = true;
		}
		return retorno;
	}

	private SessaoProcessoDocumento recuperarEmenta(SessaoPautaProcessoTrf sessaoProcesso) throws PJeBusinessException {
		SessaoProcessoDocumento ementa = null;
		if(sessaoProcesso != null) {
			TipoProcessoDocumento tipoEmenta = ParametroUtil.instance().getTipoProcessoDocumentoEmenta();
			ementa = recuperarDocumento(sessaoProcesso, tipoEmenta);
		}
		return ementa;
	}

	private SessaoProcessoDocumento recuperarRelatorio(SessaoPautaProcessoTrf sessaoProcesso) throws PJeBusinessException {
		SessaoProcessoDocumento relatorio = null;
		if(sessaoProcesso != null) {
			TipoProcessoDocumento tipoRelatorio = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
			relatorio = sessaoProcessoDocumentoManager.getSessaoProcessoDocumentoByTipoOj(
					sessaoProcesso.getSessao(), tipoRelatorio, sessaoProcesso.getProcessoTrf().getProcesso(), sessaoProcesso.getOrgaoJulgadorVencedor());
		}
		return relatorio;
	}
	
	private SessaoProcessoDocumento recuperarDocumento(SessaoPautaProcessoTrf sessaoProcesso, TipoProcessoDocumento tipoProcessoDocumento) {
		return sessaoProcessoDocumentoManager.getSessaoProcessoDocumentoByTipoOj(
				sessaoProcesso.getSessao(), tipoProcessoDocumento, sessaoProcesso.getProcessoTrf().getProcesso(), sessaoProcesso.getOrgaoJulgadorVencedor());
	}

	private Map<Integer, String> carregaOrgaos() {
		OrgaoJulgadorManager ojManager = (OrgaoJulgadorManager)Component.getInstance(OrgaoJulgadorManager.class);
		List<OrgaoJulgador> ojs = ojManager.findAll();
		Map<Integer,String> orgaos = new HashMap<Integer,String>();
		for(OrgaoJulgador oj : ojs){
			orgaos.put(oj.getIdOrgaoJulgador(),oj.getOrgaoJulgador());
		}
		return orgaos;
	}
	
	
	private String montaLink(Integer idProcesso) {
		return getURLWithContextPath()
				+ Constantes.URL_DETALHE_PROCESSO.CONSULTA_PUBLICA + "?ca="
				+ securityTokenControler.gerarChaveAcessoProcessoConsultaPublica(idProcesso);

	}
	
	public static String getURLWithContextPath() {
		return new Util().getUrlProject();
	}
	
	private String getNomeVariavelCache(Integer idSessao, boolean detalhado){
		StringBuilder name = new StringBuilder();
		if(detalhado){
			name.append(SESSAO_DETALHE);
		}else{
			name.append(SESSAO_RESUMO);
		}
		name.append(idSessao);
		return name.toString();
	}
	
	private String getNomeVariavelUltimaAtualizacao(boolean detalhado){
		StringBuilder name = new StringBuilder();
		if(detalhado){
			name.append(SESSAO_DETALHE_ULTIMA_ATUALIZACAO);
		}else{
			name.append(SESSAO_RESUMO_ULTIMA_ATUALIZACAO);
		}
		return name.toString();
	}
}
