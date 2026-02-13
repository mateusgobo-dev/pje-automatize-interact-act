
/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.FluxoDAO;
import br.jus.cnj.pje.business.dao.PainelMagistradoSessaoBetaDAO;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.entidades.vo.PesquisaProcessoVO;

/**
 * PJEII-18813 Registra movimentações das Caixas pelos Procuradores	
 * 
 * @author Carlos Lisboa
 * @since 1.7.1
 *
 */
@Name(PainelMagistradoSessaoBetaManager.NAME)
public class PainelMagistradoSessaoBetaManager {
	
	@In
	private PainelMagistradoSessaoBetaDAO painelMagistradoSessaoBetaDAO;
	
	@In
	private FluxoDAO fluxoDAO;
	
	public static final String NAME = "painelMagistradoSessaoBetaManager";

    public Map<String, Long> carregarListaTarefasMagistrado(
        Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC, List<Integer> idsOrgaoJulgadorCargo,
        Integer idUsuario, List<Integer> idsLocalizacoesFisicas, Integer idLocalizacaoFisica, Integer idLocalizacaoModelo, 
        Integer idPapel, Boolean visualizaSigiloso, Integer nivelAcessoSigilo) {
    	
        return fluxoDAO.carregarListaTarefasUsuario(idOrgaoJulgadorColegiado, isServidorExclusivoOJC,
            idsOrgaoJulgadorCargo, idUsuario, idsLocalizacoesFisicas, idLocalizacaoFisica, idLocalizacaoModelo,
            idPapel, visualizaSigiloso, nivelAcessoSigilo,
            null, null, null, null, null);
    }
	
	public Map<String,Long> carregarListaDocumentoAssinatura(
			Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC, List<Integer> idsOrgaoJulgadorCargo,
			Integer idUsuario, List<Integer> idsLocalizacoesFisicas, Integer idLocalizacaoModelo, Integer idPapel, 
			Boolean visualizaSigiloso, Integer nivelAcessoSigilo, List<String> tag){
		return fluxoDAO.recuperarQuantidadeMinutasEmElaboracaoPorTipoDocumento(idOrgaoJulgadorColegiado, isServidorExclusivoOJC,
				idsOrgaoJulgadorCargo, idUsuario, idsLocalizacoesFisicas, idLocalizacaoModelo, idPapel, 
				visualizaSigiloso, nivelAcessoSigilo, false, tag,null);
	}
	
	public List<ConsultaProcessoVO> carregarListaProcessosAssinatura(List<Integer> idsLocalizacoesFisicas,
			Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC, Integer idOrgaoJulgadorCargo,
			Integer idUsuario, Integer idLocalizacaoFisica, Integer idLocalizacaoModelo, Integer idPapel, Boolean visualizaSigiloso,Integer idTipoDocumento,Integer idCaixa, PesquisaProcessoVO criteriosPesquisa){
		return painelMagistradoSessaoBetaDAO.carregarListaProcessosAssinatura(idsLocalizacoesFisicas,idOrgaoJulgadorColegiado, isServidorExclusivoOJC,
				idOrgaoJulgadorCargo,idUsuario, idLocalizacaoFisica, idLocalizacaoModelo, idPapel, visualizaSigiloso,idTipoDocumento,idCaixa,criteriosPesquisa);
	}
	
	public List<ConsultaProcessoVO> carregarListaProcessosTarefas(List<Integer> idsLocalizacoesFisicas,
			Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC, Integer idOrgaoJulgadorCargo,
			Integer idUsuario, Integer idLocalizacaoFisica, Integer idLocalizacaoModelo, Integer idPapel, Boolean visualizaSigiloso,String nomeTarefa, Integer idCaixa, PesquisaProcessoVO criteriosPesquisa){
		return painelMagistradoSessaoBetaDAO.carregarListaProcessosTarefas(idsLocalizacoesFisicas,idOrgaoJulgadorColegiado, isServidorExclusivoOJC,
				idOrgaoJulgadorCargo,idUsuario, idLocalizacaoFisica, idLocalizacaoModelo, idPapel, visualizaSigiloso,nomeTarefa,idCaixa,criteriosPesquisa);
	}
}
