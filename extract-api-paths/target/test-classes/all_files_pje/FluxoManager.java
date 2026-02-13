package br.jus.cnj.pje.nucleo.manager;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.Actor;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.FluxoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TarefaDTO;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.pje.nucleo.dto.VisaoGeralTarefaLocalizacaoDTO;
import br.jus.pje.nucleo.dto.VisaoGeralTarefasDTO;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;

/**
 * @author cristof
 *
 */
@Name("fluxoManager")
public class FluxoManager extends BaseManager<Fluxo> {

	public static final String NAME = "fluxoManager";
	
	@In
	private FluxoDAO fluxoDAO;

	@In
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;
	
	@In
	private LocalizacaoManager localizacaoManager;
	
	@Override
	protected BaseDAO<Fluxo> getDAO() {
		return fluxoDAO;
	}
	
	public void changeNomeFluxo(Fluxo fluxo, String oldNomeFluxo) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document document;
			try {
				document = builder.build(new StringReader(fluxo.getXml()));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			Element processDefinition = document.getRootElement();
			Attribute attribute = processDefinition.getAttribute("name");
			attribute.setValue(fluxo.getFluxo());
			XMLOutputter xmlOutputter = new XMLOutputter();
			Format f = Format.getRawFormat();
			f.setEncoding("ISO-8859-1");
			xmlOutputter.setFormat(f);
			fluxo.setXml(xmlOutputter.outputString(document));
			fluxoDAO.changeFluxoName(oldNomeFluxo, fluxo.getFluxo());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Fluxo findByCodigo(String codigo) {
		return fluxoDAO.findByCodigo(codigo);
	}
	
	/**
	 * Verifica se existe algum fluxo processual ativo para um processo dado que tenha a variável 
	 * informada com valor não nulo.
	 * 
	 * @param processoJudicial o processo sob análise
	 * @param nomeVariavel o nome da variável a verificar
	 * @return true, se houver ao menos um fluxo ativo com a variável informada definida
	 * @throws PJeBusinessException
	 */
	public boolean existeFluxoComVariavel(ProcessoTrf processoJudicial, String nomeVariavel) throws PJeBusinessException{
		return fluxoDAO.existeFluxoComVariavel(processoJudicial.getIdProcessoTrf(), nomeVariavel);
	}
	
	public List<Long> recuperaFluxosComVariavel(ProcessoTrf processoJudicial, String nomeVariavel) throws PJeBusinessException{
		return fluxoDAO.recuperaFluxosComVariavel(processoJudicial.getIdProcessoTrf(), nomeVariavel);
	}

	/**
	 * Verifica se o processo está, ou esteve, em um fluxo com o nome especificado
	 * @param idProcesso Identificador do processo
	 * @param nomeFluxo Nome do fluxo
	 * @return true caso o processo exista no fluxo, falso caso nao exista
	 */
	public boolean existeProcessoNoFluxo(Integer idProcesso, String nomeFluxo) {
		return fluxoDAO.existeProcessoNoFluxo(idProcesso, nomeFluxo);
	}
	
	/**
	 * Verifica se o processo está, ou esteve, em um fluxo com o nome especificado
	 * e se o mesmo está em execução.
	 * 
	 * @param idProcesso Identificador do processo
	 * @param nomeFluxo Nome do fluxo
	 * @param idLocalizacao Identificador da localizacao
	 * @return true caso o processo exista no fluxo, falso caso nao exista
	 */
	public boolean existeProcessoNoFluxoEmExecucao(Integer idProcesso, List<Integer> idsLocalizacoes, String nomeFluxo) {
		return fluxoDAO.existeProcessoNoFluxoEmExecucao(idProcesso, idsLocalizacoes, nomeFluxo);
	}
	
	/**
	 * Verifica se o processo está, ou esteve, em um fluxo com o nome especificado
	 * e se o mesmo está em execução.
	 * 
	 * @param idProcesso Identificador do processo
	 * @param nomeFluxo Nome do fluxo
	 * @return true caso o processo exista no fluxo, falso caso nao exista
	 */
	public boolean existeProcessoNoFluxoEmExecucao(Integer idProcesso, String nomeFluxo) {
		return fluxoDAO.existeProcessoNoFluxoEmExecucao(idProcesso, nomeFluxo);
	}
	
	/**
	 * Verifica se o processo está em um fluxo ativo com o sigla especificado
	 * @param idProcesso Id do Processo
	 * @param nomeFluxo Nome do Fluxo
	 * @param idOrgaoJulgador id do OrgaoJulgador
	 * @return True se existe
	 */
	public boolean existeProcessoNoFluxoSigla(Integer idProcesso, String idsLocalizacoes, String nomeFluxo) {
		return fluxoDAO.existeProcessoNoFluxoSigla(idProcesso, nomeFluxo, idsLocalizacoes);
	}
	
	public void iniciarFluxoProcesso(Processo processo, Fluxo fluxo, Map<String, Object> parametros) {
		fluxoDAO.iniciarFluxoProcesso(processo, fluxo, parametros);
	}

	/**
	 * Com a correta implementação da utilização da árvore de localizações do tribunal a solução que utilizava os papeis de papelNaoFiltravel passaram a ser descontinuadas
	 * o tribunal deve configurar corretamente seus servidores na árvore de localizações do tribunal
	 * 
	 * @param idOrgaoJulgador
	 * @param idOrgaoJulgadorColegiado
	 * @param idsOrgaoJulgadorCargo
	 * @param logouComCertificado
	 * @param idUsuario
	 * @param idLocalizacaoFisica
	 * @param idLocalizacaoModelo
	 * @param idPapel
	 * @param visualizaSigiloso
	 * @param somenteFavoritas
	 * @param numeroProcesso
	 * @param competencia
	 * @param etiquetasList
	 * @param cargoAuxiliar
	 * @param papelNaoFiltravel
	 * @return
	 */
	@Deprecated
    public Map<String, Long> carregarListaTarefasUsuario(
        Integer idOrgaoJulgador,
        Integer idOrgaoJulgadorColegiado,
        List<Integer> idsOrgaoJulgadorCargo,
        Boolean logouComCertificado,
        Integer idUsuario,
        Integer idLocalizacaoFisica,
        Integer idLocalizacaoModelo,
        Integer idPapel,
        Boolean visualizaSigiloso,
        Integer nivelAcessoSigilo,
        Boolean somenteFavoritas,
        String numeroProcesso,
        String competencia,
        List<String> etiquetasList,
		Boolean cargoAuxiliar,
		Boolean papelNaoFiltravel) {

    	return this.carregarListaTarefasUsuario(idOrgaoJulgador, idOrgaoJulgadorColegiado, idsOrgaoJulgadorCargo, 
    			logouComCertificado, idUsuario, idLocalizacaoFisica, idLocalizacaoModelo, idPapel, 
    			visualizaSigiloso, nivelAcessoSigilo, somenteFavoritas, numeroProcesso, competencia, etiquetasList, cargoAuxiliar);
    }

	/**
	 * Utilizar a função carregarListaTarefasUsuario que não pede idOrgaoJulgador, mas pede idsLocalizacoesFisicas
	 * 
	 * @param idOrgaoJulgador
	 * @param idOrgaoJulgadorColegiado
	 * @param idsOrgaoJulgadorCargo
	 * @param logouComCertificado
	 * @param idUsuario
	 * @param idLocalizacaoFisica
	 * @param idLocalizacaoModelo
	 * @param idPapel
	 * @param visualizaSigiloso
	 * @param somenteFavoritas
	 * @param numeroProcesso
	 * @param competencia
	 * @param etiquetasList
	 * @param cargoAuxiliar
	 * @return
	 */
	@Deprecated
    public Map<String, Long> carregarListaTarefasUsuario(
            Integer idOrgaoJulgador,
            Integer idOrgaoJulgadorColegiado,
            List<Integer> idsOrgaoJulgadorCargo,
            Boolean logouComCertificado,
            Integer idUsuario,
            Integer idLocalizacaoFisica,
            Integer idLocalizacaoModelo,
            Integer idPapel,
            Boolean visualizaSigiloso,
            Integer nivelAcessoSigilo,
            Boolean somenteFavoritas,
            String numeroProcesso,
            String competencia,
            List<String> etiquetasList,
    		Boolean cargoAuxiliar) {
		
			List<Integer> idsLocalizacoesFisicasList = new ArrayList<>();
			idsLocalizacoesFisicasList.add(idLocalizacaoFisica);
			
			boolean isServidorExclusivoOJC = (idOrgaoJulgador == null && idOrgaoJulgadorColegiado != null);
    	
    		return this.carregarListaTarefasUsuario(idOrgaoJulgadorColegiado, isServidorExclusivoOJC, idsOrgaoJulgadorCargo, 
    				idUsuario, idsLocalizacoesFisicasList, idLocalizacaoFisica, idLocalizacaoModelo, idPapel, 
    				visualizaSigiloso, nivelAcessoSigilo, somenteFavoritas, numeroProcesso, competencia, etiquetasList, cargoAuxiliar);
        }

    public Map<String, Long> carregarListaTarefasUsuario(
            Integer idOrgaoJulgadorColegiado,
            boolean isServidorExclusivoOJC,
            List<Integer> idsOrgaoJulgadorCargo,
            Integer idUsuario,
            List<Integer> idsLocalizacoesFisicas,
            Integer idLocalizacaoFisica,
            Integer idLocalizacaoModelo,
            Integer idPapel,
            Boolean visualizaSigiloso,
            Integer nivelAcessoSigilo,
            Boolean somenteFavoritas,
            String numeroProcesso,
            String competencia,
            List<String> etiquetasList,
    		Boolean cargoAuxiliar) {
    	
    	return fluxoDAO.carregarListaTarefasUsuario(idOrgaoJulgadorColegiado, isServidorExclusivoOJC, idsOrgaoJulgadorCargo, idUsuario, 
    			idsLocalizacoesFisicas, idLocalizacaoFisica, idLocalizacaoModelo, idPapel, visualizaSigiloso, nivelAcessoSigilo,
    			somenteFavoritas, numeroProcesso, competencia, etiquetasList, cargoAuxiliar);
    }
    
    public List<TarefaDTO> carregarListaTarefasLocalizacao(Integer idLocalizacaoModelo) {
    	return fluxoDAO.carregarListaTarefasLocalizacao(idLocalizacaoModelo);
    }
    
    public List<String> carregarListaTarefas(
            Integer idLocalizacaoModelo,
            Integer idPapel) {
            return fluxoDAO.carregarListaTarefas(idLocalizacaoModelo, idPapel);
        }
    
	
    private  Map<String, Long> carregarListaTarefasUsuario(InformacaoUsuarioSessao informacaoUsuarioSessao){
    	return carregarListaTarefasUsuario(
			informacaoUsuarioSessao.getIdOrgaoJulgadorColegiado(),
			informacaoUsuarioSessao.isServidorExclusivoOJC(),
			informacaoUsuarioSessao.getIdsOrgaoJulgadorCargoVisibilidade(),
			informacaoUsuarioSessao.getIdUsuario(),
			informacaoUsuarioSessao.getIdsLocalizacoesFisicasFilhas(),
			informacaoUsuarioSessao.getIdLocalizacaoFisica(),
			informacaoUsuarioSessao.getIdLocalizacaoModelo(),
			informacaoUsuarioSessao.getIdPapel(),
			informacaoUsuarioSessao.getVisualizaSigiloso(),
			informacaoUsuarioSessao.getNivelAcessoSigilo(),
			false,
			null,
			null,
			null,
			false);
    			
    }
    
    public List<VisaoGeralTarefasDTO> obterVisaoGeralTarefasUsuario(){
    	List<VisaoGeralTarefasDTO> visaoGeralTarefasUsuario = new ArrayList<VisaoGeralTarefasDTO>();
    	
    	List<InformacaoUsuarioSessao> possiveisPerfisUsuario = obterPossiveisPerfisUsuarioLogado();
    	
    	for (InformacaoUsuarioSessao possivelPerfilUsuario : possiveisPerfisUsuario){
     		Map<String, Long> mapResumoTarefasPerfil = carregarListaTarefasUsuario(possivelPerfilUsuario);
     		     		
     		for (String nomeTarefa : mapResumoTarefasPerfil.keySet() ){
     			VisaoGeralTarefasDTO visaoGeralTarefasDTO = null;
     			
     			for (VisaoGeralTarefasDTO vgt : visaoGeralTarefasUsuario){
     				if (vgt.getNomeTarefa().equals(nomeTarefa)){
     					visaoGeralTarefasDTO = vgt;
     				}
     			}
     			
     			if (visaoGeralTarefasDTO == null){
     				visaoGeralTarefasDTO = new VisaoGeralTarefasDTO();
     				visaoGeralTarefasDTO.setNomeTarefa(nomeTarefa);
     				visaoGeralTarefasUsuario.add(visaoGeralTarefasDTO);
     			}
     			
     			VisaoGeralTarefaLocalizacaoDTO vgtl = new VisaoGeralTarefaLocalizacaoDTO();
     			vgtl.setIdUsuarioLocalizacaoMagistradoServidor(possivelPerfilUsuario.getIdUsuarioLocalizacaoMagistradoServidor());
     			vgtl.setIdOrgaoJulgador(possivelPerfilUsuario.getIdOrgaoJulgador() );
     			vgtl.setIdOrgaoJulgadorColegiado(possivelPerfilUsuario.getIdOrgaoJulgadorColegiado() );
     			
     			vgtl.setDescricaoOrgaoJulgador(possivelPerfilUsuario.getDescricaoOrgaoJulgador());
     			vgtl.setDescricaoOrgaoJulgadorColegiado(possivelPerfilUsuario.getDescricaoOrgaoJulgadorColegiado());
     			vgtl.setTotalPendencias(mapResumoTarefasPerfil.get(nomeTarefa).intValue());
     			
     			visaoGeralTarefasDTO.setTotalPendencias(visaoGeralTarefasDTO.getTotalPendencias() + vgtl.getTotalPendencias());
     			visaoGeralTarefasDTO.getLocaisTarefas().add(vgtl);
     		}
    	}
    	
    	return visaoGeralTarefasUsuario;
    }
    

	private List<InformacaoUsuarioSessao> obterPossiveisPerfisUsuarioLogado() {
		
		List<InformacaoUsuarioSessao> possiveisPerfisUsuarioLogado = new ArrayList<InformacaoUsuarioSessao>();
		
		Usuario usuario = Authenticator.getUsuarioLogado();
		Integer idUsuario = Authenticator.getIdUsuarioLogado();
		Integer idPapel = Authenticator.getIdPapelAtual();
		Boolean visualizaSigiloso = Authenticator.isVisualizaSigiloso();
		Integer nivelAcessoSigilo = Authenticator.recuperarNivelAcessoUsuarioLogado();
		
		List<UsuarioLocalizacaoMagistradoServidor> localizacoesUsuario = usuarioLocalizacaoMagistradoServidorManager.obterLocalizacoesUsuario(usuario);
		
		for (UsuarioLocalizacaoMagistradoServidor ulms :  localizacoesUsuario){
			Date agora = new Date();
			
			if (ulms.getDtInicio().before(agora) && (ulms.getDtFinal() == null || ulms.getDtFinal().after(agora))){
				InformacaoUsuarioSessao possivelPerfil = new InformacaoUsuarioSessao();
				possivelPerfil.setIdUsuarioLocalizacaoMagistradoServidor(ulms.getIdUsuarioLocalizacaoMagistradoServidor());
				Integer idLocalizacaoFisica = ulms.getUsuarioLocalizacao().getLocalizacaoFisica().getIdLocalizacao();
				List<Localizacao> localizacaoFisicaList = localizacaoManager.getArvoreDescendente(idLocalizacaoFisica, true);
				String idsLocalizacoesFisicas = LocalizacaoUtil.converteLocalizacoesList(localizacaoFisicaList);
				
				possivelPerfil.setIdsLocalizacoesFisicasFilhas(CollectionUtilsPje.convertStringToIntegerList(idsLocalizacoesFisicas));
				possivelPerfil.setIdLocalizacaoFisica(idLocalizacaoFisica);
				possivelPerfil.setIdLocalizacaoModelo(ulms.getUsuarioLocalizacao().getLocalizacaoModelo().getIdLocalizacao());
				possivelPerfil.setIdOrgaoJulgador(ulms.getOrgaoJulgador().getIdOrgaoJulgador());
				possivelPerfil.setIdOrgaoJulgadorColegiado(ulms.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado());
				
				List<Integer> idsOrgaoJulgadorCargo = new ArrayList<Integer>();
				
				for(UsuarioLocalizacaoVisibilidade vis : ulms.getUsuarioLocalizacaoVisibilidadeList()){
					if(vis.getDtInicio().after(agora) || (vis.getDtFinal() != null && vis.getDtFinal().before(agora))){
						continue;
					}
	
					if(vis.getOrgaoJulgadorCargo() == null){
						idsOrgaoJulgadorCargo.clear();
						break;
					}
					else{
						idsOrgaoJulgadorCargo.add(vis.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo());
					}
				}
	
				possivelPerfil.setIdsOrgaoJulgadorCargoVisibilidade(idsOrgaoJulgadorCargo);
				possivelPerfil.setIdUsuario(idUsuario);
				possivelPerfil.setIdPapel(idPapel);
				possivelPerfil.setVisualizaSigiloso(visualizaSigiloso);
				possivelPerfil.setNivelAcessoSigilo(nivelAcessoSigilo);
				possivelPerfil.setDescricaoOrgaoJulgador(ulms.getOrgaoJulgador().getOrgaoJulgador());
				possivelPerfil.setDescricaoOrgaoJulgadorColegiado(ulms.getOrgaoJulgadorColegiado().getOrgaoJulgadorColegiado());
				
				possiveisPerfisUsuarioLogado.add(possivelPerfil);
			}
		}

		return possiveisPerfisUsuarioLogado;
	}

	public Map<String,Long> recuperarQuantidadeMinutasEmElaboracaoPorTipoDocumento(
			Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC, List<Integer> idsOrgaoJulgadorCargo, Boolean logouComCertificado,
			Integer idUsuario, List<Integer> idsLocalizacoesFisicas, Integer idLocalizacaoModelo, Integer idPapel, 
			Boolean visualizaSigiloso, Integer nivelAcessoSigilo, List<String> tag,Boolean cargoAuxiliar){
		return fluxoDAO.recuperarQuantidadeMinutasEmElaboracaoPorTipoDocumento(idOrgaoJulgadorColegiado, isServidorExclusivoOJC,
				idsOrgaoJulgadorCargo,idUsuario, idsLocalizacoesFisicas, idLocalizacaoModelo, idPapel, visualizaSigiloso, nivelAcessoSigilo, 
				false, tag,cargoAuxiliar);
	}
	
	public Map<String,Long> recuperarQuantidadeMinutasEmElaboracaoPorTipoDocumentoService(
			Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC, List<Integer> idsOrgaoJulgadorCargo, Boolean logouComCertificado,
			Integer idUsuario, List<Integer> idsLocalizacoesFisicas, Integer idLocalizacaoModelo, Integer idPapel, 
			Boolean visualizaSigiloso, Integer nivelAcessoSigilo, List<String> tag, Boolean cargoAuxiliar){
		return fluxoDAO.recuperarQuantidadeMinutasEmElaboracaoPorTipoDocumento(idOrgaoJulgadorColegiado, isServidorExclusivoOJC, 
				idsOrgaoJulgadorCargo,idUsuario,idsLocalizacoesFisicas,idLocalizacaoModelo, idPapel, visualizaSigiloso, nivelAcessoSigilo,
				true,tag,cargoAuxiliar);
	}
	
	public void adicionarFluxoSemActorId(Fluxo fluxo, Map<String, Object> variaveis) {
		String actorIdAtual = null;
		if(Actor.instance() != null) {
			actorIdAtual = Actor.instance().getId();
			Actor.instance().setId(null);
		}
		ProcessoHome.instance().adicionarFluxo(fluxo, variaveis);
		if(actorIdAtual != null) {
			Actor.instance().setId(actorIdAtual);
		}
	}
	
	public Fluxo obterFluxoDoProcesso(Long idProcesso) {
		return this.fluxoDAO.obterFluxoDoProcesso(idProcesso);
	}
	
	public Long recuperarTaskInstancePorVariavel(String nomeVariavel, String valorVariavel) {
		return this.fluxoDAO.recuperarTaskInstancePorVariavel(nomeVariavel, valorVariavel);
	}
	
	public void finalizaFluxoManualmente(Long idTarefa ) {
		this.fluxoDAO.finalizaFluxoManualmente(idTarefa);
	}

}
