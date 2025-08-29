package br.jus.cnj.pje.view;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.PessoaHome;
import br.com.infox.ibpm.component.tree.LocalizacaoTreeHandler;
import br.com.infox.ibpm.component.tree.PapelTreeHandler;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.cnj.pje.nucleo.manager.VinculacaoUsuarioManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.VinculacaoUsuario;
import br.jus.pje.nucleo.enums.TipoVinculacaoUsuarioEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Classe responsável por realizar os controles de tela do cadastro de assessores
 * do magistrado, realizando o vinculo entre os usuários do sistema
 */
@Name(AssessoriaMagistradoAction.NAME)
@AutoCreate
public class AssessoriaMagistradoAction extends BaseAction<VinculacaoUsuario> {

	public static final String NAME = "assessoriaMagistradoAction";
	private static final long serialVersionUID = -6323741560012238579L;
	
	@In
	private VinculacaoUsuarioManager vinculacaoUsuarioManager;	
	private EntityDataModel<VinculacaoUsuario> assessoresDoMagistrado;
	
	@RequestParameter(value="idVinculacaoUsuario")
	private Integer idVinculacaoUsuario; 
	
	/**
	 * Método responsável pela inicialização da classe.
	 */
	@Create
	public void init() {
		limparComponentesTela();
	}
	
	@Override
	public VinculacaoUsuario newInstance() {		
		VinculacaoUsuario vinculacaoUsuario = super.newInstance();
		vinculacaoUsuario.setTipoVinculacaoUsuario(TipoVinculacaoUsuarioEnum.EGA);
		limparComponentesTela();
		return vinculacaoUsuario;
	}

	@Override
	public void persist() {
		
		getInstance().setDataCriacao(new Date());
		getInstance().setUsuario(PessoaHome.instance().getInstance());
		
		super.persist();
		vinculacaoUsuarioManager.sincronizarLotacoes(getInstance());
		newInstance(); 		
	}
	
	public void remove(){
		VinculacaoUsuario vinculacaoUsuario;
		if (idVinculacaoUsuario == null) {
			facesMessages.add(Severity.ERROR, "Id da assessor não foi informado para remoção.");
		} else {		
			try {
				vinculacaoUsuario = vinculacaoUsuarioManager.findById(idVinculacaoUsuario);
				setInstance(vinculacaoUsuario);					
				vinculacaoUsuarioManager.sincronizarExclusaoVinculacaoUsuario(getInstance());
				super.removeAndFlush();
				newInstance();
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o assessor para remoção.", e.getLocalizedMessage());
				e.printStackTrace();
			}			
		}
	}
	
	private void limparComponentesTela(){
		((LocalizacaoTreeHandler) ComponentUtil.getComponent("localizacaoTree")).clearTree();
 		((PapelTreeHandler) ComponentUtil.getComponent("papelTree")).clearTree(); 		
 		atualizarPesquisa();
	}
	
	private void atualizarPesquisa() {
		DataRetriever<VinculacaoUsuario> dataRetriever = new AssessoriaMagistradoRetriever(vinculacaoUsuarioManager, this.facesMessages);
		assessoresDoMagistrado = new EntityDataModel<VinculacaoUsuario>(VinculacaoUsuario.class, this.facesContext, dataRetriever);		
	}
	
	@Override
	protected BaseManager<VinculacaoUsuario> getManager() {		
		return vinculacaoUsuarioManager;
	}

	@Override
	public EntityDataModel<VinculacaoUsuario> getModel() {
		return assessoresDoMagistrado;
	}

	/**
	 * A localização modeloRoot será a modeloRoot vinculada à primeira localização física de magistrado encontrada para o usuário em questão
	 * @return
	 */
	public Localizacao getLocalizacaoModeloRoot() {
		UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMgServManager = ComponentUtil.getComponent("usuarioLocalizacaoMagistradoServidorManager");
		List<UsuarioLocalizacaoMagistradoServidor> localizacoesUsuario = usuarioLocalizacaoMgServManager.obterLocalizacoesMagistrado(PessoaHome.instance().getInstance());
		if(CollectionUtilsPje.isNotEmpty(localizacoesUsuario)) {
			return localizacoesUsuario.get(0).getLocalizacaoFisica().getEstruturaFilho();
		}
		return null;
	}
		
	/**
	 * Classe privada para pesquisa de assessores dos magistrados
	 */
	private class AssessoriaMagistradoRetriever implements DataRetriever<VinculacaoUsuario> {
		
		private VinculacaoUsuarioManager vinculacaoUsuarioManager;
		private FacesMessages facesMessages;
		
		public AssessoriaMagistradoRetriever(VinculacaoUsuarioManager vinculacaoUsuarioManager, FacesMessages facesMessages) {
			this.vinculacaoUsuarioManager = vinculacaoUsuarioManager;
			this.facesMessages = facesMessages;
		}

		@Override
		public Object getId(VinculacaoUsuario obj) {
			return obj.getIdVinculacaoUsuario();
		}

		@Override
		public VinculacaoUsuario findById(Object id) throws Exception {
			return vinculacaoUsuarioManager.findById(id);
		}

		@Override
		public List<VinculacaoUsuario> list(Search search) {
			atualizarDadosPesquisa(search);			
			return vinculacaoUsuarioManager.list(search);
		}

		@Override
		public long count(Search search) {
			atualizarDadosPesquisa(search);
			return vinculacaoUsuarioManager.count(search);
		}
		
		private void atualizarDadosPesquisa(Search search) {			
			try {
				search.addCriteria(Criteria.equals("usuario", PessoaHome.instance().getInstance()));
				search.addOrder("usuarioVinculado.nome", Order.ASC);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar a lista de assessores do magistrado.");
				e.printStackTrace();
			}						
		}				
	}
}
