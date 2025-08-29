package br.com.infox.editor.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.editor.dao.PreferenciaDao;
import br.com.infox.editor.exception.VariavelException;
import br.com.infox.editor.interpretadorDocumento.LinguagemFormalException;
import br.com.infox.editor.list.AutoTextoList;
import br.com.infox.editor.service.ProcessaModeloService;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.view.GenericCrudAction;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.Variavel;
import br.jus.pje.nucleo.entidades.editor.AutoTexto;
import br.jus.pje.nucleo.entidades.editor.Preferencia;
import br.jus.pje.nucleo.enums.editor.OrigemAutoTextoEnum;
import br.jus.pje.nucleo.enums.editor.PreferenciaEditorEnum;

@Name(AutoTextoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class AutoTextoAction extends GenericCrudAction<AutoTexto> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "autoTextoAction";

	@In
	private GenericManager genericManager;

	@In
	private PreferenciaDao preferenciaDao;

	@In
	private ProcessaModeloService processaModeloService;
	
	private OrigemAutoTextoEnum origemAutoTexto;
	private Variavel variavel;
	private Preferencia preferenciaOrigemAutoTexto;
	private Map<String, String> dadosProcessamentoAutoTexto;
	private boolean autoTextoModificado = false;
	private String mainMenu;
	
	public void persist() {
		AutoTexto autoTexto = getInstance();
		autoTexto.setLocalizacao(getLocalizacaoAtual());
		setDadosOrigemAutoTexto(autoTexto);
		if (!validaAutoTexto(autoTexto))
			return;
		super.persist(autoTexto);
		autoTextoModificado = false;
	}

	public String getMainMenu() {
		return mainMenu;
	}

	public void setMainMenu(String mainMenu) {
		this.mainMenu = mainMenu;
	}

	public void update() {
		AutoTexto autoTexto = getInstance();
		setDadosOrigemAutoTexto(autoTexto);
		if (!validaAutoTexto(autoTexto))
			return;
		super.update(autoTexto);
		autoTextoModificado = false;
	}
	
	private Localizacao getLocalizacaoAtual(){
		if (Authenticator.isUsuarioLocalizacaoMagistadoServidor()){ 
			UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor = Authenticator.getUsuarioLocalizacaoMagistradoServidorAtual();
			if (usuarioLocalizacaoMagistradoServidor != null) {
				if (usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador() != null && usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador().getLocalizacao() != null) {
					return usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador().getLocalizacao();
				}
				else if (usuarioLocalizacaoMagistradoServidor.getUsuarioLocalizacao() != null && usuarioLocalizacaoMagistradoServidor.getUsuarioLocalizacao().getLocalizacaoFisica() != null) {
					return usuarioLocalizacaoMagistradoServidor.getUsuarioLocalizacao().getLocalizacaoFisica();
				} 
			}
		}
		else{
			Localizacao localizacaoFisica = Authenticator.getLocalizacaoFisicaAtual();
			if (localizacaoFisica != null) {
				return localizacaoFisica;
			}
		}
		return null;
	}
	
	private boolean validaAutoTexto(AutoTexto autoTexto) {
		String erro = processaModeloService.validaModelo(autoTexto.getConteudo());
		if (erro != null) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao avaliar a linguagem formal. " + erro);
			return false;
		}

		//Não podem existir dois auto-textos com o mesmo nome para a mesma localização, nem dois auto-textos com o mesmo nome para o mesmo usuário.
		boolean existeAutoTextoComDescricao = EntityUtil.getSingleResult(EntityUtil.createQuery("select o.idAutoTexto from AutoTexto o where " +
				" lower(o.descricao) = lower(:descricao) and o.idAutoTexto != :idAutoTexto " + 
				" and o.publico = :publico " +
				" and ((o.localizacao = :localizacao and o.publico = true) or (o.usuario = :usuario and o.publico = false)) ")
				.setParameter("descricao", autoTexto.getDescricao())
				.setParameter("idAutoTexto", autoTexto.getIdAutoTexto() != null ? autoTexto.getIdAutoTexto() : 0)
				.setParameter("publico", autoTexto.getPublico())
				.setParameter("localizacao", autoTexto.getLocalizacao())
				.setParameter("usuario", autoTexto.getUsuario()))!= null;

		if (existeAutoTextoComDescricao) {
			FacesMessages.instance().add(Severity.ERROR, "Já existe um autotexto com o nome informado.");
			return false;
		}
		return true;
	}

	public void remove() {
		super.remove(getInstance());
		newInstance();
	}

	public void mudarOrigemAutoTexto() {
		preferenciaOrigemAutoTexto.setValor(origemAutoTexto.toString());
		genericManager.update(preferenciaOrigemAutoTexto);
	}

	public void inicializar() {
		preferenciaOrigemAutoTexto = preferenciaDao.getPreferenciaPorUsuario(Authenticator.getUsuarioLogado(), PreferenciaEditorEnum.OA);
		if (preferenciaOrigemAutoTexto == null) {
			preferenciaOrigemAutoTexto = new Preferencia();
			preferenciaOrigemAutoTexto.setPreferenciaEditor(PreferenciaEditorEnum.OA);
			preferenciaOrigemAutoTexto.setUsuario(Authenticator.getUsuarioLogado());
			preferenciaOrigemAutoTexto.setValor(OrigemAutoTextoEnum.L.toString());
			genericManager.persist(preferenciaOrigemAutoTexto);
		}
		origemAutoTexto = OrigemAutoTextoEnum.valueOf(preferenciaOrigemAutoTexto.getValor());
		AutoTextoList autoTextoList = ComponentUtil.getComponent(AutoTextoList.NAME);
		if (origemAutoTexto.equals(OrigemAutoTextoEnum.L)) {
			autoTextoList.getEntity().setLocalizacao(getLocalizacaoAtual());
			autoTextoList.getEntity().setUsuario(null);
			autoTextoList.getEntity().setPublico(true);
		} else {
			autoTextoList.getEntity().setLocalizacao(null);
			autoTextoList.getEntity().setUsuario(Authenticator.getUsuarioLogado());
			autoTextoList.getEntity().setPublico(false);
		}
	}

	public void processarAutoTexto(String conteudo) {
		dadosProcessamentoAutoTexto = new HashMap<String, String>();
		try {
			dadosProcessamentoAutoTexto.put("autoTexto", processaModeloService.processaVariaveisModelo(conteudo));
		} catch (LinguagemFormalException e) {
			e.printStackTrace();
			if (e.getCause() instanceof VariavelException) {
				dadosProcessamentoAutoTexto.put("erro", "Variável inválida encontrada.");
			} else {
				dadosProcessamentoAutoTexto.put("erro", "Erro ao avaliar a linguagem formal.");
			}
		}
	}

	private void setDadosOrigemAutoTexto(AutoTexto autoTexto) {
		if (origemAutoTexto.equals(OrigemAutoTextoEnum.P)) {
			autoTexto.setPublico(false);
			autoTexto.setUsuario(Authenticator.getUsuarioLogado());
		} else {
			autoTexto.setPublico(true);
			autoTexto.setUsuario(null);
		}
	}

	@Override
	public void setId(Integer id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (changed) {
			setIdInstance(id);
			autoTextoModificado = false;
		}
	}

	public OrigemAutoTextoEnum getOrigemAutoTexto() {
		return origemAutoTexto;
	}

	public void setOrigemAutoTexto(OrigemAutoTextoEnum origemAutoTexto) {
		this.origemAutoTexto = origemAutoTexto;
	}

	public Variavel getVariavel() {
		return variavel;
	}

	public void setVariavel(Variavel variavel) {
		this.variavel = variavel;
	}

	public Map<String, String> getAutoTextoProcessado() {
		return dadosProcessamentoAutoTexto;
	}

	@Override
	public boolean isManaged() {
		return super.isManaged() && getInstance().getIdAutoTexto() != null;
	}

	public boolean isAutoTextoModificado() {
		return autoTextoModificado;
	}

	public void setAutoTextoModificado(boolean autoTextoModificado) {
		this.autoTextoModificado = autoTextoModificado;
	}

	public String getHomeName() {
		return getName();
	}
}
