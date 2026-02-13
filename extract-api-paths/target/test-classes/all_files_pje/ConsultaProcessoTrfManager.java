/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.ConsultaProcessoTrfDAO;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.search.Criteria;


@Name("consultaProcessoTrfManager")
public class ConsultaProcessoTrfManager extends BaseManager<ConsultaProcessoTrf>{

	private static final String NUMERO_ORGAO_JUSTICA = "numeroOrgaoJustica";

	@In
	private ConsultaProcessoTrfDAO consultaProcessoTrfDAO;
	
	@In
	private ProcessoVisibilidadeSegredoManager processoVisibilidadeSegredoManager;
	
	@In
	private ParametroService parametroService;

	@Override
	protected ConsultaProcessoTrfDAO getDAO(){
		return this.consultaProcessoTrfDAO;
	}
	
	public ConsultaProcessoVO consultaProcessoVO(ProcessoTrf processoJudicial, String nomeTarefa){
		
		return this.consultaProcessoTrfDAO.consultaProcessoVO(processoJudicial, nomeTarefa);
	}
	
	public ConsultaProcessoVO consultaProcessoVO(ProcessoTrf processoJudicial, Integer idTarefa){
		
		return this.consultaProcessoTrfDAO.consultaProcessoVO(processoJudicial, idTarefa);
	}
	
	public Long countConsultaProcessoSituacao(ProcessoTrf processoJudicial, Integer idTarefa){
		
		return this.consultaProcessoTrfDAO.countConsultaProcessoSituacao(processoJudicial, idTarefa);
	}
	
	public Long countConsultaProcessoSituacao(ProcessoTrf processoJudicial, String nmTarefa){
		
		return this.consultaProcessoTrfDAO.countConsultaProcessoSituacao(processoJudicial, nmTarefa);
	}

	/**
	 * Método responsável por retornar uma Critéria contendo todos as condições 
	 * para consulta de processo de acordo com as regras de negócio do sistema
	 * @param isAdmin 
	 * @param isProcurador 
	 * @param isMagistrado 
	 * @param isNumeroProcessoIncompleto 
	 *  
	 * @return uma <b>lista</b> de critérios negociais
	 */
	public List<Criteria> getCriteriosNegociais(boolean isProcurador, boolean isAdmin,
			boolean isMagistrado, boolean isNumeroProcessoIncompleto){
		
		List<Criteria> criterios = new ArrayList<>(0);
		criterios.add(Criteria.equals("processoStatus", ProcessoStatusEnum.D));

		Criteria parteAtiva = Criteria.equals("processoParteList.inSituacao", ProcessoParteSituacaoEnum.A);
	    Criteria parteBaixada = Criteria.equals("processoParteList.inSituacao", ProcessoParteSituacaoEnum.B);
	    criterios.add(Criteria.or(parteAtiva, parteBaixada));

		if(isNumeroProcessoIncompleto){
			Criteria visibilidadeAtribuida = processoVisibilidadeSegredoManager.consultaCriteriosVisibilidadeAtribuidaProcesso();
			Criteria visibilidadeProcurador = getCriteriosVisualizacaoProcurador(isProcurador, isAdmin);
			Criteria visibilidadeParte = Criteria.contains("processoParteList.pessoa.nome",Authenticator.getPessoaLogada().getNomeParte());
			Criteria visibilidadeMagistrado = isMagistrado ? getCriteriosLocalizacaoUsuario() : null;
			criterios.add(Criteria.or(visibilidadeAtribuida,visibilidadeMagistrado,visibilidadeProcurador,visibilidadeParte));
		}
		return criterios;
	}
	
	/**
	 *Retorna uma condição Equals de procuradoria.
	 *<br /><br />
	 *Ex.:  processoParteList.procuradoria.idProcuradoria = 568
	 *<br />
	 *Onde 568 é a procuradoria que o usuário faz parte.
	 *<br /><br />
	 *Obs.: A critéria somente será retornada se o usuário for um "procurador"
	 *<br /><br />
	 * @param isAdmin 
	 * @param isProcurador 
	 * @return Uma <b>Criteria</b> contendo a condição equals de procuradoria
	 */
	private Criteria getCriteriosVisualizacaoProcurador(boolean isProcurador, boolean isAdmin){
		
		if (isProcurador && !isAdmin){
			try {
				Integer idProcuradoria = Authenticator.getIdProcuradoriaAtualUsuarioLogado();
				return Criteria.equals("processoParteList.procuradoria.idProcuradoria",idProcuradoria);
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	/**
	 * Responsável por montar uma criteria contendo as localizações do usuário para consulta.
	 * <br /><br />
	 * Obs.: Só é retornado se o usuário logado possuir uma localização.
	 * <br />
	 * Ex.:  orgaoJulgadorCargo.idOrgaoJulgadorCargo = 568
	 * <br />Onde 568 é a localização do usuário.
	 * <br /><br />
	 * @return Uma <b>criteria</b> Equals contendo as localizações do usuário  
	 */
	private Criteria getCriteriosLocalizacaoUsuario(){
		Criteria orgao = null;

		UsuarioLocalizacaoMagistradoServidor usuarioLocMagServidor = Authenticator.getUsuarioLocalizacaoAtual().getUsuarioLocalizacaoMagistradoServidor();
		if(usuarioLocMagServidor != null){
			if(usuarioLocMagServidor.getOrgaoJulgadorCargo() != null && !usuarioLocMagServidor.getOrgaoJulgadorCargo().getAuxiliar()){
				orgao = Criteria.equals("orgaoJulgadorCargo.idOrgaoJulgadorCargo", usuarioLocMagServidor.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo());
			}else if(usuarioLocMagServidor.getOrgaoJulgador() != null){
				orgao = Criteria.equals("orgaoJulgador.idOrgaoJulgador", usuarioLocMagServidor.getOrgaoJulgador().getIdOrgaoJulgador());
			}else if(usuarioLocMagServidor.getOrgaoJulgadorColegiado() != null){
				orgao = Criteria.equals("orgaoJulgadorColegiado.idOrgaoJulgadorColegiado", usuarioLocMagServidor.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado());
			}
		}
		
		return orgao;
		
	}

	/**
	 * PJEII-18556 
	 * Retorna os valores preenchidos na tela de pesquisa fazendo as devidas verificações
	 * se o dado está preenchido ou não. 
	 * <br /><br />
	 * 
	 * @param respectivoTribunal 
	 * @param numeroOrigem 
	 * @param ano 
	 * @param digitoVerificador 
	 * @param numeroSequencia 
	 * @return <b>List<Criteria></b> contendo os critério para consulta do processo.
	 */
	public List<Criteria> getCriteriosTelaPesquisa(Integer numeroSequencia, 
			Integer digitoVerificador, Integer ano, Integer numeroOrigem, String respectivoTribunal){ 
		
			List<Criteria> criterios = new ArrayList<>(0);
			String segmentoJudiciario = parametroService.valueOf(NUMERO_ORGAO_JUSTICA);
				
			if (numeroSequencia != null && numeroSequencia > 0){
				criterios.add(Criteria.equals("numeroSequencia",numeroSequencia));
			}
			
			if (digitoVerificador != null && digitoVerificador > 0){
				criterios.add(Criteria.equals("numeroDigitoVerificador",digitoVerificador));
			}
			
			if (ano != null && ano > 0){
				criterios.add(Criteria.equals("ano",ano));
			}
			
			if (numeroOrigem != null && numeroOrigem > 0){
				criterios.add(Criteria.equals("numeroOrigem",numeroOrigem));
			}
			
			if(respectivoTribunal != null && !respectivoTribunal.isEmpty()){
				if(segmentoJudiciario != null){
					segmentoJudiciario = segmentoJudiciario.substring(0,1);
					criterios.add(Criteria.equals(NUMERO_ORGAO_JUSTICA,Integer.parseInt(segmentoJudiciario + respectivoTribunal)));
				}
				else{
					criterios.add(Criteria.endsWith(NUMERO_ORGAO_JUSTICA,respectivoTribunal));
				}
			}
		return criterios;
	}
	
	/**
	 * Método responsável por retornar um EntityDataModel para listagem
	 * de processos que o usuário tem permissão
	 * 
	 * 
	 * @param model
	 * @param numeroSequencia
	 * @param digitoVerificador
	 * @param ano
	 * @param numeroOrigem
	 * @param respectivoTribunal
	 * @param isLogouComCertificado
	 * @param isProcurador
	 * @param isAdmin
	 * @param isMagistrado
	 * @param isNumeroProcessoIncompleto
	 * @return EntityDataModel
	 */
	public List<Criteria> obtemModelListagemProcessos(Integer numeroSequencia,
			Integer digitoVerificador, Integer ano, Integer numeroOrigem,
			String respectivoTribunal,
			boolean isProcurador, boolean isAdmin, boolean isMagistrado,
			boolean isNumeroProcessoIncompleto) {

		List<Criteria> criterios = new ArrayList<Criteria>(0);
		criterios.addAll(getCriteriosTelaPesquisa(numeroSequencia,digitoVerificador, ano, 
				numeroOrigem, respectivoTribunal));
		criterios.addAll(getCriteriosNegociais(isProcurador, isAdmin, 
				isMagistrado,isNumeroProcessoIncompleto));
		
		return criterios;
	}
	

}
