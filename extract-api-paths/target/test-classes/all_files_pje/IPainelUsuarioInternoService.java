package br.jus.cnj.pje.webservice.api;

import java.util.List;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CabecalhoProcesso;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.PagedQueryResult;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.SaidaTarefa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TarefaPendente;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TarefaPendenteAssinatura;

public interface IPainelUsuarioInternoService {
	
    public List<TarefaPendente> listarTarefasUsuario(JsonObject pesquisa);
    
    public List<TarefaPendente> listarTarefasFavoritasUsuario(JsonObject pesquisa);
    
    public List<TarefaPendenteAssinatura> listarQuantidadeMinutasEmElaboracaoPorTipoDocumento();
    
    public PagedQueryResult<CabecalhoProcesso> recuperarProcessos(Integer tipoDocumento, CriterioPesquisa criterios);    
    
    public PagedQueryResult<CabecalhoProcesso> recuperarProcessos(String tarefa, Boolean somenteFavoritas, CriterioPesquisa crit);
    
    public CabecalhoProcesso recuperarProcesso(Long idTaskInstance, Boolean assinatura);
    
    public List<SaidaTarefa> retornaTransicoes(Long idTarefa);
    
    public Response conferirProcesso(Long idTarefa, Long idProcesso);
    
    public Response desconferirProcesso(Long idTarefa, Long idProcesso);
    
    public Response limparResponsavel(Long idTarefa);
    
    public Response movimentarProcesso(Long idTarefa, String nomeTransicao);
    
    public Response obterTodosOrgaosJulgadores();
    
    public Response obterEleicoesAtivas();
    
    public Response obterEstadosAtivos();
    
    public Response retornaMunicipioAtivosPorIdEstado(Integer idEstado);
    
    public Response obterValorParametro(String nomeParametro);
    
    public Response recuperarPrioridades();
    
    public Response gerarChaveAcessoProcesso(Long idProcesso, HttpServletRequest contexto);
    
    public Response uploadArquivoAssinado(String id, String hash, String codIni , String hashAssinado, String certChain, String idTarefa);    
}
