/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;



import java.util.List;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import br.com.itx.util.ComponentUtil;

import br.jus.cnj.pje.business.dao.BlocoComposicaoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.BlocoComposicao;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.VotoBloco;
import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(BlocoComposicaoManager.NAME)
public class BlocoComposicaoManager extends BaseManager<BlocoComposicao> {
		
	public static final String NAME = "blocoComposicaoManager";
	
	@Logger
	private Log logger;
	
	public List<BlocoComposicao> findByBloco(BlocoJulgamento bloco) {
		return getDAO().findByBloco(bloco);
	}
	
	public List<BlocoComposicao> findByBlocoPresentes(BlocoJulgamento bloco) {
		return getDAO().findByBlocoPresentes(bloco);
	}
	
	@Override
	protected BlocoComposicaoDAO getDAO() {
		BlocoComposicaoDAO blocoComposicaoDAO = ComponentUtil.getComponent(BlocoComposicaoDAO.class);
		return blocoComposicaoDAO;
	}
	
	public void gerarComposicaoBloco(BlocoJulgamento bloco) throws PJeBusinessException {
		List<SessaoComposicaoOrdem> composicaoSessao = null;
		if(bloco!= null) {
			composicaoSessao = ComponentUtil.getSessaoComposicaoOrdemManager().obterComposicaoSessao(bloco.getSessao().getIdSessao(), 0);
			gerarComposicaoBloco(bloco, composicaoSessao);
		}
	}
	
	public void gerarComposicaoBloco(BlocoJulgamento bloco, List<SessaoComposicaoOrdem> composicaoSessao ) throws PJeBusinessException {
		for (SessaoComposicaoOrdem composicao : composicaoSessao){
			UsuarioLocalizacaoMagistradoServidor lotacaoMagistradoTitular = ComponentUtil.getUsuarioLocalizacaoMagistradoServidorManager().obterLocalizacaoMagistradoPrincipal(composicao.getOrgaoJulgador(), bloco.getSessao().getOrgaoJulgadorColegiado());
			if (lotacaoMagistradoTitular != null) {
				this.persistAndFlush(recuperarNovaComposicaoBloco(bloco, composicao, lotacaoMagistradoTitular.getOrgaoJulgadorCargo()));
			}
		} 
	}
		
	public BlocoComposicao recuperarNovaComposicaoBloco(BlocoJulgamento bloco, SessaoComposicaoOrdem composicao, OrgaoJulgadorCargo cargo) {
		BlocoComposicao retorno = new BlocoComposicao();
		retorno.setBloco(bloco);
		retorno.setTipoAtuacaoMagistrado(TipoAtuacaoMagistradoEnum.VOGAL);
		retorno.setMagistradoPresente(composicao.getMagistradoPresenteSessao());
		retorno.setCargoAtuacao(cargo);
		retorno.setImpedidoSuspeicao(Boolean.FALSE);
		retorno.setDefinidoPorUsuario(Boolean.FALSE);
		retorno.setOrgaoJulgador(composicao.getOrgaoJulgador());
		retorno.setPresente(composicao.getPresenteSessao());
		return retorno;
	}
	
	public List<OrgaoJulgador> recuperarOrgaoJulgadorPorBloco(BlocoJulgamento bloco){
		return ComponentUtil.getBlocoComposicaoDAO().recuperarOrgaoJulgadorPorBloco(bloco);
	}
	
	public boolean verificarPresenca(BlocoJulgamento bloco, OrgaoJulgador orgao){
		List<BlocoComposicao> listComposicaoBloco = ComponentUtil.getBlocoComposicaoManager().findByBlocoPresentes(bloco);
	
		if(listComposicaoBloco == null || listComposicaoBloco.size() == 0) {
			try {
				ComponentUtil.getBlocoComposicaoManager().gerarComposicaoBloco(bloco);
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			}
		}
		Search s = new Search(BlocoComposicao.class);
		addCriteria(s,
				Criteria.equals("bloco", bloco),
				Criteria.equals("orgaoJulgador", orgao),
				Criteria.equals("presente", true));
		List<VotoBloco> ret = list(s);
		
		return ret.isEmpty() ? false : true;
	}
}