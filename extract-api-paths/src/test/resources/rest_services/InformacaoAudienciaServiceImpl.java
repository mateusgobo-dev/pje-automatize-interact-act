package br.jus.cnj.pje.webservice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.pje.manager.ProcessoAudienciaManager;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.util.DateUtil;


@Name("informacaoAudienciaService")
@Path("informacaoAudiencia")
public class InformacaoAudienciaServiceImpl implements InformacaoAudienciaService{
	
	@Logger
	private Log log;

	@In
	private ProcessoAudienciaManager processoAudienciaManager;

	@GET
	@Override
	@Produces({ MediaType.APPLICATION_JSON + ";charset=UTF-8" })
	public Response recuperarPauta(
			@QueryParam("dataInicio") String dataInicioString, 
			@QueryParam("idOrgaoJulgador") Integer idOrgaoJulgador,
			@QueryParam("idSalaAudiencia") Integer idSalaAudiencia,
			@QueryParam("dataFim") String dataFimString){
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			
			Date dataInicio = new Date();
			Date dataFim = null;
			Sala salaAudiencia = null;
			OrgaoJulgador orgaoJulgador = null;
			
			if(dataInicioString != null && !dataInicioString.isEmpty()){
				dataInicio = sdf.parse(dataInicioString);
			}
			
			if(dataFimString != null && !dataFimString.isEmpty()){
				dataFim = sdf.parse(dataFimString);
			}
			
			
			if(idSalaAudiencia != null){
				salaAudiencia = new Sala();
				salaAudiencia.setIdSala(idSalaAudiencia);
			}		
			
			if(idOrgaoJulgador != null){
				orgaoJulgador = new OrgaoJulgador();
				orgaoJulgador.setIdOrgaoJulgador(idOrgaoJulgador);
			}
			 
			if(dataInicio != null && dataFim != null){
				if(DateUtil.diferencaEntreDias(dataFim, dataInicio) > 30){
					throw new IntercomunicacaoException("Diferença entre as datas inicio e fim não pode ser superior a 30 dias!");
				};
			}
			
			List<ProcessoAudiencia> list = processoAudienciaManager.procurarSalasComAudienciaMarcadaPorDia(orgaoJulgador, dataInicio, dataFim, salaAudiencia);
			
			List<InformacaoAudiencia> audiencias = new ArrayList<InformacaoAudiencia>(list.size()); 
			
			for(ProcessoAudiencia pa : list){
				InformacaoAudiencia audiencia = new InformacaoAudiencia();
				
				audiencia.setDtInicio(pa.getDtInicio());
				audiencia.setNomeConciliador(pa.getPessoaConciliador() != null ? 
						pa.getPessoaConciliador().getNome() : null);
				
				String nomeOrgaoJulgador = pa.getSalaAudiencia().getOrgaoJulgadorColegiado() != null ?
						pa.getSalaAudiencia().getOrgaoJulgadorColegiado().getOrgaoJulgadorColegiado() :
							pa.getSalaAudiencia().getOrgaoJulgador().getOrgaoJulgador();
				
				audiencia.setNomeOrgaoJulgador(nomeOrgaoJulgador);
				audiencia.setNomeRealizador(pa.getPessoaRealizador() != null ? 
						pa.getPessoaRealizador().getNome() : null);
				audiencia.setNumeroProcesso(pa.getProcessoTrf().getNumeroProcesso());
				audiencia.setTipoAudiencia(pa.getTipoAudiencia().getTipoAudiencia());
				audiencia.setIdSala(pa.getSalaAudiencia().getIdSala());
				audiencia.setNomeJurisdicao(pa.getProcessoTrf().getJurisdicao().getJurisdicao());
				
				List<ProcessoParte> partes =pa.getProcessoTrf().
						getListaPartePoloObj(false, ProcessoParteParticipacaoEnum.A, 
								ProcessoParteParticipacaoEnum.P); 
				
				for(ProcessoParte parte : partes){
					InformacaoAudienciaParte parteAudiencia = new InformacaoAudienciaParte();
					parteAudiencia.setNome(parte.getPessoa().getNome());
					parteAudiencia.setQualificacao(parte.getTipoParte().getTipoParte());

					for(PessoaDocumentoIdentificacao documento : parte.getPessoa().getPessoaDocumentoIdentificacaoList()){
						InformacaoAudienciaDocumentoParte documentoParte = new InformacaoAudienciaDocumentoParte();
						documentoParte.setNumeroDocumento(documento.getNumeroDocumento());
						documentoParte.setTipoDocumento(documento.getTipoDocumento().getTipoDocumento());
						parteAudiencia.getDocumentos().add(documentoParte);
					}
					
					audiencia.getPartes().add(parteAudiencia);
				}
				
				audiencias.add(audiencia);
			}
			
			
			InformacaoAudienciaResposta resposta = new InformacaoAudienciaResposta();
			resposta.setAudiencias(audiencias);
			
			return Response.ok(resposta).build();
			
		} catch (Exception e) {
			return reportarErro(e);
		}
	}

	
	private Response reportarErro(Exception e) {
		Response response;
		log.error("Erro no serviço de informações de audiência", e);
		response = Response.ok(e.getMessage())
				.status(Response.Status.BAD_REQUEST).build();
		return response;
	}
}
