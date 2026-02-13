package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.identidade.Papel;

/**
 * Faz o controle do componente de cadastro de localizações de servidores
 * Esta classe é a controladora do componente: cadastroLocalizacaoServidor.xhtml
 */
@Name(CadastroLocalizacaoServidorAction.NAME)
@Scope(ScopeType.PAGE)
public class CadastroLocalizacaoServidorAction {
	public static final String NAME = "cadastroLocalizacaoServidorAction";
	
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;

	private Usuario usuario;
	private Integer idUsuarioLocalizacaoMagistradoServidor;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoInicial;
	private List<OrgaoJulgadorColegiado> orgaoJulgadorColegiadoList;
	private boolean orgaoJulgadorColegiadoObrigatorio;
	private boolean orgaoJulgadorColegiadoDesabilitado;
	private boolean orgaoJulgadorColegiadoFixo;
	
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgador orgaoJulgadorInicial;
	private List<OrgaoJulgador> orgaoJulgadorList;
	private boolean orgaoJulgadorObrigatorio;
	private boolean orgaoJulgadorDesabilitado;
	private boolean orgaoJulgadorFixo;
	
	private Localizacao localizacaoFisica;
	private Localizacao localizacaoFisicaInicial;
	private Localizacao localizacaoFisicaRoot;
	private boolean localizacaoFisicaFixa;
	
	private Localizacao localizacaoModelo;
	private Localizacao localizacaoModeloInicial;
	private Localizacao localizacaoModeloRoot;
	private boolean localizacaoModeloObrigatoria;
	private boolean localizacaoModeloDesabilitada;
	
	private Papel papel;
	private Papel papelInicial;
	private Papel papelRoot;

	private boolean cadastroValidado;
	
	@Create()
	public void init() {
		usuarioLocalizacaoMagistradoServidorManager = ComponentUtil.getComponent("usuarioLocalizacaoMagistradoServidorManager");
		this.atualizaConfiguracoesCampos();
		this.identificaValoresIniciais();
	}
	
	public void carregaValoresPreenchidos(Integer idUsuarioLocalizacaoMagistradoServidor, 
			OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgador, 
			Localizacao localizacaoFisica, Localizacao localizacaoModelo, Papel papel) {
		
		this.idUsuarioLocalizacaoMagistradoServidor = idUsuarioLocalizacaoMagistradoServidor;
		this.orgaoJulgadorColegiadoInicial = this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
		this.orgaoJulgadorInicial = this.orgaoJulgador = orgaoJulgador;
		this.localizacaoFisicaInicial = this.localizacaoFisica = localizacaoFisica;
		this.localizacaoModeloInicial = this.localizacaoModelo = localizacaoModelo;
		this.papelInicial = this.papel = papel;
		
		this.atualizaConfiguracoesCampos();

		this.cadastroValidado = Boolean.TRUE;
	}
	
	private void identificaValoresIniciais() {
		this.orgaoJulgadorColegiadoInicial = this.orgaoJulgadorColegiado;
		this.orgaoJulgadorInicial = this.orgaoJulgador;
		this.localizacaoFisicaInicial = this.localizacaoFisica;
		this.localizacaoModeloInicial = this.localizacaoModelo;
		this.papelInicial = this.papel;
	}
	
	public boolean isValoresIniciaisAlterados() {
		return 	   (
					(this.orgaoJulgadorColegiadoInicial == null && this.orgaoJulgadorColegiado != null)
					|| (this.orgaoJulgadorColegiadoInicial != null && !this.orgaoJulgadorColegiadoInicial.equals(this.orgaoJulgadorColegiado))
					)
				|| (
						(this.orgaoJulgadorInicial == null && this.orgaoJulgador != null)
						|| (this.orgaoJulgadorInicial != null && !this.orgaoJulgadorInicial.equals(this.orgaoJulgador))
					)
				|| (
						(this.localizacaoFisicaInicial == null && this.localizacaoFisica != null)
						|| (this.localizacaoFisicaInicial != null && !this.localizacaoFisicaInicial.equals(this.localizacaoFisica))
					)
				|| (
						(this.localizacaoModeloInicial == null && this.localizacaoModelo != null)
						|| (this.localizacaoModeloInicial != null && !this.localizacaoModeloInicial.equals(this.localizacaoModelo))
					)
				|| (
						(this.papelInicial == null && this.papel != null)
						|| (this.papelInicial != null && !this.papelInicial.equals(this.papel))
					);
	}
	
	public void atualizaConfiguracoesCampos() {
		this.configurarOJC();
		this.configurarOJ();
		this.configurarLocalizacaoFisica();
		this.configurarLocalizacaoModelo();
		this.configurarPapel();
		this.validarDadosServidor();
	}
	
	public void atualizaConfiguracoesCamposPosOJCSelecionado() {
		this.configurarOJ();
		this.configurarLocalizacaoFisica();
		this.configurarLocalizacaoModelo();
		this.validarDadosServidor();
	}
	
	public void atualizaConfiguracoesCamposPosOJSelecionado() {
		this.configurarLocalizacaoFisica();
		this.configurarLocalizacaoModelo();
		this.validarDadosServidor();
	}
	
	public void atualizaConfiguracoesCamposPosLocalizacaoFisicaSelecionada() {
		this.configurarLocalizacaoModelo();
		this.validarDadosServidor();
	}
	
	/**
	 * Com base nos dados do usuário logado identifica os dados disponíveis para o campo OJC
	 * 1. Se estivermos em um ambiente de 1o grau, o campo OJC deve ficar desabilitado
	 * 2. O campo será obrigatório se o usuário não for administrador e já estiver em um OJC
	 * 3. A lista de OJCs disponíveis terá todos os OJCs ativos da instalação ou se o usuário pertencer a um OJC, terá apenas o OJC do usuário logado
	 * 4. O OJC selecionado será o do usuário logado (se este não for administrador)
	 */
	private void configurarOJC() {
		if(!this.getIsAmbienteColegiado()) {
			this.orgaoJulgadorColegiadoDesabilitado = Boolean.TRUE;
		}else {
			this.orgaoJulgadorColegiadoDesabilitado = Boolean.FALSE;
			this.orgaoJulgadorColegiadoFixo = Boolean.FALSE;
			this.orgaoJulgadorColegiadoObrigatorio = usuarioLocalizacaoMagistradoServidorManager.isOrgaoJulgadorColegiadoObrigatorio();
			if(this.isValoresIniciaisAlterados()) {
				this.orgaoJulgadorColegiado = null;
			}

			if(Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
				this.orgaoJulgadorColegiadoList = new ArrayList<OrgaoJulgadorColegiado>();
				this.orgaoJulgadorColegiadoList.add(Authenticator.getOrgaoJulgadorColegiadoAtual());
				this.orgaoJulgadorColegiado = Authenticator.getOrgaoJulgadorColegiadoAtual();
				this.orgaoJulgadorColegiadoFixo = Boolean.TRUE;
			}else {
				OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager = ComponentUtil.getComponent("orgaoJulgadorColegiadoManager");
				this.orgaoJulgadorColegiadoList = orgaoJulgadorColegiadoManager.getColegiadosByLocalizacao(Authenticator.getLocalizacaoFisicaAtual());
			}
			if(this.orgaoJulgadorColegiadoList == null || this.orgaoJulgadorColegiadoList.size() == 0) {
				this.orgaoJulgadorColegiadoDesabilitado = Boolean.TRUE;
			}
		}
	}
	
	/**
	 * Com base nos dados do usuário e do OJC selecionado, configura o campo OJ
	 * 1. Verifica se o campo estará habilitado ou não, de acordo com o ambiente (singular/colegiado) e se o OJC está ou não preenchido
	 * 2. Verifica se o campo será obrigatório se o usuário não for admin e pertencer a um OJ
	 * 3. Verifica a lista de OJs disponível de acordo com o ambiente e/ou o OJC selecionado
	 * 4. o OJ selecionado será o do usuário logado (se este não for administrador) 
	 */
	private void configurarOJ() {
		if(!this.orgaoJulgadorColegiadoDesabilitado && this.getOrgaoJulgadorColegiado() == null) {
			this.orgaoJulgadorDesabilitado = Boolean.TRUE;
			this.orgaoJulgador = null;
		}else {
			this.orgaoJulgadorDesabilitado = Boolean.FALSE;
			this.orgaoJulgadorFixo = Boolean.FALSE;
			this.orgaoJulgadorObrigatorio = usuarioLocalizacaoMagistradoServidorManager.isOrgaoJulgadorObrigatorio();
			if(this.isValoresIniciaisAlterados()) {
				this.orgaoJulgador = null;
			}

			if(Authenticator.getOrgaoJulgadorAtual() != null) {
				this.orgaoJulgadorList = new ArrayList<OrgaoJulgador>();
				this.orgaoJulgadorList.add(Authenticator.getOrgaoJulgadorAtual());
				this.orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
				this.orgaoJulgadorFixo = Boolean.TRUE;
			}else {
				OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent("orgaoJulgadorManager");
				this.orgaoJulgadorList = orgaoJulgadorManager.obterOrgaosJulgadoresPorColegiadoEhPorLocalizacao(orgaoJulgadorColegiado, Authenticator.getLocalizacaoFisicaAtual());
			}
			if(this.orgaoJulgadorList == null || this.orgaoJulgadorList.size() == 0) {
				this.orgaoJulgadorDesabilitado = Boolean.TRUE;
			}
		}
	}
	
	/**
	 * A localização física será dada de acordo com o capmo identificado:
	 * 1. pela localização do OJ (se selecionado)
	 * 2. pela localização do OJC (se selecionado)
	 * 3. selecionável a partir da localização do usuário logado (se não for administrador)
	 * 4. selecionável a partir da localizacao raiz do tribunal (se for administrador)
	 */
	private void configurarLocalizacaoFisica() {
		this.localizacaoFisicaRoot = null;
		if(this.isValoresIniciaisAlterados()) {
			this.localizacaoFisica = null;
		}
		this.localizacaoFisicaFixa = Boolean.FALSE;
		if(this.getOrgaoJulgador() != null) {
			this.localizacaoFisica = this.getOrgaoJulgador().getLocalizacao();
			this.localizacaoFisicaRoot = this.localizacaoFisica;
		}else if(!this.orgaoJulgadorColegiadoDesabilitado && this.getOrgaoJulgadorColegiado() != null) {
				this.localizacaoFisica = this.getOrgaoJulgadorColegiado().getLocalizacao();
				this.localizacaoFisicaRoot = this.localizacaoFisica;
		}else {
			// busca todas as localizações físicas a partir da atual do usuário
			this.localizacaoFisicaRoot = Authenticator.getLocalizacaoFisicaAtual();
		}
		
		if(this.localizacaoFisicaRoot != null) {
			this.localizacaoFisicaFixa = this.localizacaoFisicaRoot.isLocalizacaoFolha();
		}
	}
	
	/**
	 * Se ainda não houver localizações físicas selecionadas, ou se a localização física não possuir modelo relacionado,
	 * o campo de localizaçãoModelo estará desabilitado e será não obrigatório
	 * - a partir do momento em que uma localização física for selecionada e houver localização modelo relacionada, este campo ficará
	 * habilitado e obrigatório
	 */
	private void configurarLocalizacaoModelo() {
		this.localizacaoModeloDesabilitada = Boolean.TRUE;
		this.localizacaoModeloRoot = null;
		if(this.isValoresIniciaisAlterados()) {
			this.localizacaoModelo = null;
		}
		this.localizacaoModeloObrigatoria = usuarioLocalizacaoMagistradoServidorManager.isLocalizacaoModeloObrigatoria(this.localizacaoFisica);
		if(this.localizacaoFisica != null) {
			if(this.localizacaoFisica.getEstruturaFilho() != null) {
				this.localizacaoModeloDesabilitada = Boolean.FALSE;
				this.localizacaoModeloRoot = this.localizacaoFisica.getEstruturaFilho();
			}
		}
	}
	
	private void configurarPapel() {
		this.papelRoot = null;
		if(this.isValoresIniciaisAlterados()) {
			this.papel = null;
		}
		if(!this.getIsUsuarioAdministrador() && Authenticator.getPapelAtual() != null) {
			this.papelRoot = Authenticator.getPapelAtual();
		}else {
			this.papelRoot = ParametroUtil.instance().getPapelAdministrador();
		}
	}
	
	public void limparFormulario() {
		this.usuario = null;
		this.idUsuarioLocalizacaoMagistradoServidor = null;
		this.orgaoJulgadorColegiado = null;
		this.orgaoJulgadorColegiadoList = new ArrayList<OrgaoJulgadorColegiado>();
		this.orgaoJulgadorColegiadoDesabilitado = Boolean.FALSE;
		this.orgaoJulgadorColegiadoFixo = Boolean.FALSE;
		
		this.orgaoJulgador = null;
		this.orgaoJulgadorList = new ArrayList<OrgaoJulgador>();
		this.orgaoJulgadorDesabilitado = Boolean.FALSE;
		this.orgaoJulgadorFixo = Boolean.FALSE;
		
		this.localizacaoFisica = null;
		this.localizacaoFisicaRoot = null;
		this.localizacaoFisicaFixa = Boolean.FALSE;
		this.localizacaoModelo = null;
		this.localizacaoModeloRoot = null;
		this.localizacaoModeloDesabilitada = Boolean.FALSE;
		
		this.papel = null;
		this.papelRoot = null;
		
		this.orgaoJulgadorColegiadoInicial = null;
		this.orgaoJulgadorInicial = null;
		this.localizacaoFisicaInicial = null;
		this.localizacaoModeloInicial = null;
		this.papelInicial = null;

		this.cadastroValidado = Boolean.FALSE;
	}

	public void validarDadosServidor() {
		if(this.isValoresIniciaisAlterados()) {
			try{
				usuarioLocalizacaoMagistradoServidorManager.validarDadosServidor(
						this.usuario, 
						this.orgaoJulgadorColegiado, 
						this.orgaoJulgador, 
						this.localizacaoFisica, 
						this.localizacaoModelo, 
						this.papel, 
						this.idUsuarioLocalizacaoMagistradoServidor);
				this.cadastroValidado = Boolean.TRUE;
			}catch (PJeBusinessException e) {
				this.cadastroValidado = Boolean.TRUE;
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, e.getCode(), e.getParams());
			}
		}
	}
	
	public boolean getIsAmbienteColegiado() {
		return !ComponentUtil.getComponent(ParametroUtil.class).isPrimeiroGrau();
	}
	
	public boolean getIsUsuarioAdministrador() {
		return Authenticator.isPermissaoCadastroTodosPapeis();
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Integer getIdUsuarioLocalizacaoMagistradoServidor() {
		return idUsuarioLocalizacaoMagistradoServidor;
	}

	public void setIdUsuarioLocalizacaoMagistradoServidor(Integer idUsuarioLocalizacaoMagistradoServidor) {
		this.idUsuarioLocalizacaoMagistradoServidor = idUsuarioLocalizacaoMagistradoServidor;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoList() {
		return orgaoJulgadorColegiadoList;
	}

	public void setOrgaoJulgadorColegiadoList(List<OrgaoJulgadorColegiado> orgaoJulgadorColegiadoList) {
		this.orgaoJulgadorColegiadoList = orgaoJulgadorColegiadoList;
	}

	public boolean isOrgaoJulgadorColegiadoObrigatorio() {
		return orgaoJulgadorColegiadoObrigatorio;
	}

	public void setOrgaoJulgadorColegiadoObrigatorio(boolean orgaoJulgadorColegiadoObrigatorio) {
		this.orgaoJulgadorColegiadoObrigatorio = orgaoJulgadorColegiadoObrigatorio;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public List<OrgaoJulgador> getOrgaoJulgadorList() {
		return orgaoJulgadorList;
	}

	public void setOrgaoJulgadorList(List<OrgaoJulgador> orgaoJulgadorList) {
		this.orgaoJulgadorList = orgaoJulgadorList;
	}

	public boolean isOrgaoJulgadorObrigatorio() {
		return orgaoJulgadorObrigatorio;
	}

	public void setOrgaoJulgadorObrigatorio(boolean orgaoJulgadorObrigatorio) {
		this.orgaoJulgadorObrigatorio = orgaoJulgadorObrigatorio;
	}

	public boolean isOrgaoJulgadorColegiadoFixo() {
		return orgaoJulgadorColegiadoFixo;
	}

	public void setOrgaoJulgadorColegiadoFixo(boolean orgaoJulgadorColegiadoFixo) {
		this.orgaoJulgadorColegiadoFixo = orgaoJulgadorColegiadoFixo;
	}

	public boolean isOrgaoJulgadorFixo() {
		return orgaoJulgadorFixo;
	}

	public void setOrgaoJulgadorFixo(boolean orgaoJulgadorFixo) {
		this.orgaoJulgadorFixo = orgaoJulgadorFixo;
	}

	public Localizacao getLocalizacaoFisica() {
		return localizacaoFisica;
	}

	public void setLocalizacaoFisica(Localizacao localizacaoFisica) {
		this.localizacaoFisica = localizacaoFisica;
	}

	public Localizacao getLocalizacaoFisicaRoot() {
		return localizacaoFisicaRoot;
	}

	public void setLocalizacaoFisicaRoot(Localizacao localizacaoFisicaRoot) {
		this.localizacaoFisicaRoot = localizacaoFisicaRoot;
	}

	public boolean isLocalizacaoFisicaFixa() {
		return localizacaoFisicaFixa;
	}

	public void setLocalizacaoFisicaFixa(boolean localizacaoFisicaFixa) {
		this.localizacaoFisicaFixa = localizacaoFisicaFixa;
	}

	public Localizacao getLocalizacaoModelo() {
		return localizacaoModelo;
	}

	public void setLocalizacaoModelo(Localizacao localizacaoModelo) {
		this.localizacaoModelo = localizacaoModelo;
	}

	public Localizacao getLocalizacaoModeloRoot() {
		return localizacaoModeloRoot;
	}

	public void setLocalizacaoModeloRoot(Localizacao localizacaoModeloRoot) {
		this.localizacaoModeloRoot = localizacaoModeloRoot;
	}

	public boolean isLocalizacaoModeloObrigatoria() {
		return localizacaoModeloObrigatoria;
	}

	public void setLocalizacaoModeloObrigatoria(boolean localizacaoModeloObrigatoria) {
		this.localizacaoModeloObrigatoria = localizacaoModeloObrigatoria;
	}

	public Papel getPapel() {
		return papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	public Papel getPapelRoot() {
		return papelRoot;
	}

	public void setPapelRoot(Papel papelRoot) {
		this.papelRoot = papelRoot;
	}

	public boolean isOrgaoJulgadorColegiadoDesabilitado() {
		return orgaoJulgadorColegiadoDesabilitado;
	}

	public void setOrgaoJulgadorColegiadoDesabilitado(boolean orgaoJulgadorColegiadoDesabilitado) {
		this.orgaoJulgadorColegiadoDesabilitado = orgaoJulgadorColegiadoDesabilitado;
	}

	public boolean isOrgaoJulgadorDesabilitado() {
		return orgaoJulgadorDesabilitado;
	}

	public void setOrgaoJulgadorDesabilitado(boolean orgaoJulgadorDesabilitado) {
		this.orgaoJulgadorDesabilitado = orgaoJulgadorDesabilitado;
	}

	public boolean isLocalizacaoModeloDesabilitada() {
		return localizacaoModeloDesabilitada;
	}

	public void setLocalizacaoModeloDesabilitada(boolean localizacaoModeloDesabilitada) {
		this.localizacaoModeloDesabilitada = localizacaoModeloDesabilitada;
	}

	public boolean isCadastroValidado() {
		return cadastroValidado;
	}

	public void setCadastroValidado(boolean cadastroValidado) {
		this.cadastroValidado = cadastroValidado;
	}
}
