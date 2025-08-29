package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.GrupoOficialJusticaDAO;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.GrupoOficialJustica;

/**
 * Classe responsavel por manipular as regras de negocio para a entitade GrupoOficialJustica
 */
@Name(GrupoOficialJusticaManager.NAME)
public class GrupoOficialJusticaManager extends BaseManager<GrupoOficialJustica>{
	public static final String NAME  = "grupoOficialJusticaManager";

	@In
	private GrupoOficialJusticaDAO grupoOficialJusticaDAO;
		
	@In
	private CentralMandadoManager centralMandadoManager;
	
	@In
	private LocalizacaoManager localizacaoManager;
	
	/**
	 * Obter a lista de grupos de oficial de justica baseada na Central de Mandado.
	 * Caso nao seja passada uma central como parametro, o retorno sera feito baseado no papel de oficial de justica do usuario logado
	 * ou vinculados a centrais das localizacoes ancestrais. 
	 * 
	 * @param centralMandado Central de mandado selecionada, pode ser nulla
	 * @return List<GrupoOficialJustica> Lista de grupos vinculadas a central ou ao OfJus ou as localizacoes ancestrais.
	 */
	public List<GrupoOficialJustica> obter(CentralMandado centralMandado){
		List<GrupoOficialJustica> retornoGrupoOficiais = new ArrayList<GrupoOficialJustica>();
		
		if (Authenticator.isPapelOficialJustica()){
			retornoGrupoOficiais.addAll(grupoOficialJusticaDAO.obter(centralMandado, Authenticator.getUsuarioLogado().getIdUsuario(), Boolean.TRUE));
		} else if(centralMandado != null){
			retornoGrupoOficiais.addAll(grupoOficialJusticaDAO.obter(centralMandado, null, Boolean.TRUE));
		} else {
			retornoGrupoOficiais.addAll(obterPorLocalizacaoCentralMandado());
		}
		return retornoGrupoOficiais;
	}

	/**
	 * Metodo que retorna os Grupos de Oficiais que estao vinculados a centrais de mandado vinculadas
	 * a localizacao atual do usuario, essa consulta e feita usando as localizacoes ancestrais.
	 * 
	 * @return List de Grupos de Oficiais de Justica por central de mandado
	 */
	public List<GrupoOficialJustica> obterPorLocalizacaoCentralMandado(){
		List<GrupoOficialJustica> grupoRetorno = new ArrayList<GrupoOficialJustica>();
		List<CentralMandado> centrais = centralMandadoManager.obterPorLocalizacaoAncestral();
		if(!centrais.isEmpty()){
			grupoRetorno.addAll(grupoOficialJusticaDAO.obter(centrais, Boolean.TRUE));
		}
		return grupoRetorno;
	}
	
	@Override
	protected BaseDAO<GrupoOficialJustica> getDAO() {
		return grupoOficialJusticaDAO;
	}
	
	public static GrupoOficialJusticaManager instance() {
		return ComponentUtil.getComponent(NAME);
	}
}
