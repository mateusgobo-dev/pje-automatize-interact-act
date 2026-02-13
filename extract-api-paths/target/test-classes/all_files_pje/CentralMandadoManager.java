package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.CentralMandadoDAO;
import br.jus.cnj.pje.business.dao.GrupoOficialJusticaDAO;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.Localizacao;

/**
 * Classe responsavel por manipular consultas e regras para a entidade de CentralMandado
 */
@Name(CentralMandadoManager.NAME)
public class CentralMandadoManager extends BaseManager<CentralMandado>{
	
	public static final String NAME  = "centralMandadoManager";
	
	@In
	private GrupoOficialJusticaDAO grupoOficialJusticaDAO;
		
	@In
	private LocalizacaoManager localizacaoManager;
	
	@In
	private CentralMandadoDAO centralMandadoDAO;

	public static CentralMandadoManager instance() {
		return ComponentUtil.getComponent(NAME);
	}

	/**
	 * Retorna as centrais de mandados de acordo com o perfil que esta selecionado.
	 * se for Oficial de Justica retorna pelo usuario, para os demais perfis retorna pela localizacao(Ancestral) 
	 * 
	 * @return Lista de Centrais de Mandado por perfil
	 */
	public List<CentralMandado> obterCentraisMandadosPorPerfil() {
		List<CentralMandado> centrais = new ArrayList<CentralMandado>();
		if(Authenticator.isPapelOficialJustica()){
			centrais = centralMandadoDAO.obterCentraisMandadosPorUsuario(Authenticator.getUsuarioLogado().getIdUsuario());
		} else {
			centrais = obterPorLocalizacaoAncestral();
		}
		return centrais;
	}

	/**
	 * Retorna as centrais de mandados de acordo com as localizacoes ancestrais, a partir da localizacao atual.
	 * 
	 * @return List de Central de Mandado de mesmo nivel ou acima das localizacoes.
	 */
	public List<CentralMandado> obterPorLocalizacaoAncestral(){
		Integer idLocAtual = Authenticator.getLocalizacaoAtual().getIdLocalizacao();
		List<Localizacao> localizacoesAncestrais = localizacaoManager.getArvoreAscendente(idLocAtual, true);
		return this.obterPorLocalizacoes(localizacoesAncestrais);
	}
	
	public List<CentralMandado> obterPorLocalizacoes(List<Localizacao> localizacoesList){
		return centralMandadoDAO.obterPorLocalizacao(localizacoesList);
	}
	
	public List<Localizacao> obterLocalizaoes(CentralMandado centralMandado, List<Localizacao> localizacaoUsuarioList){
		return centralMandadoDAO.obterLocalizacoes(centralMandado, localizacaoUsuarioList);
	}

	@Override
	protected BaseDAO<CentralMandado> getDAO() {
		return centralMandadoDAO;
	}
}
