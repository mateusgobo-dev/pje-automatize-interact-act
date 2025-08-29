package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.UsuarioLocalizacaoHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.CentralMandadoManager;
import br.jus.cnj.pje.nucleo.manager.OficialJusticaCentralMandadoManager;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.csjt.pje.commons.exception.BusinessException;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OficialJusticaCentralMandado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name(OficialJusticaCentralMandadoHome.NAME)
@BypassInterceptors
public class OficialJusticaCentralMandadoHome extends AbstractHome<OficialJusticaCentralMandado> {

	public static final String NAME = "oficialJusticaCentralMandadoHome";
	private static final long serialVersionUID = 1L;
	private List<CentralMandado> centralMandadoList = null;
	private List<Papel> papeisOJ = new ArrayList<Papel>(2);
	
	private CentralMandado centralMandado;
	private List<Localizacao> localizacaoFisicaRootList;
	private Localizacao localizacaoFisica;
	private Papel papel;
	private Pessoa pessoa;

	public void setOficialJusticaCentralMandadoIdOficialJusticaCentralMandado(Integer id) {
		setId(id);
	}

	public Integer getOficialJusticaCentralMandadoIdOficialJusticaCentralMandado() {
		return (Integer) getId();
	}

	public static OficialJusticaCentralMandadoHome instance() {
		return (OficialJusticaCentralMandadoHome) ComponentUtil.getComponent(OficialJusticaCentralMandadoHome.NAME);
	}
	
	private UsuarioLocalizacaoManager getUsuarioLocalizacaoManager() {
		return (UsuarioLocalizacaoManager) ComponentUtil.getComponent(UsuarioLocalizacaoManager.NAME);
	}
	
	private OficialJusticaCentralMandadoManager getOficialJusticaCentralMandadoManager() {
		return (OficialJusticaCentralMandadoManager) ComponentUtil.getComponent(OficialJusticaCentralMandadoManager.NAME);
	}
	
	private PapelManager getPapelManager() {
		return (PapelManager) ComponentUtil.getComponent(PapelManager.NAME);
	}

	private CentralMandadoManager getCentralMandadoManager() {
		return (CentralMandadoManager) ComponentUtil.getComponent(CentralMandadoManager.NAME);
	}

	@Override
	public void setId(Object id) {
		super.setId(id);
		if (isManaged()) {
			centralMandado = instance.getCentralMandado();
			this.initLocalizacoesFisicasRootList();
			localizacaoFisica = instance.getUsuarioLocalizacao().getLocalizacaoFisica();
			papel = instance.getUsuarioLocalizacao().getPapel();

			instance.setCentralMandado(centralMandado);
			instance.setLocalizacao(localizacaoFisica);
			instance.setPapel(papel);
		}
	}
	
	/**
	 * Reinicia a instância do objeto cadastro OJ.
	 */
	public void newInstance() {
		super.clearInstance();
		super.newInstance();

		this.centralMandado = null;
		this.localizacaoFisica = null;
		this.papel = null;
		this.pessoa = PessoaOficialJusticaHome.instance().getInstance().getPessoa();
		this.initCentraisMandadoList();
		this.initPapelOficialJusticaList();
		this.initLocalizacoesFisicasRootList();
	}
	
	public void atualizaConfiguracoesCamposPosCemanSelecionada() {
		this.initLocalizacoesFisicasRootList();
	}

	/**
	 * Remove o cadastro de OJ selecionado na tela.
	 */
	@Override
	public String remove(OficialJusticaCentralMandado obj) {
		try {
			this.getOficialJusticaCentralMandadoManager().remove(obj);
		} catch (Exception e) {
			throw new BusinessException(Severity.ERROR, "oficialJusticaCentralMandado.erro.acessoAoBanco", e.getCause());
		}
		newInstance();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Informação removida");
		return new String();
	}
	
	/**
	 * Persiste um novo registro de oficial de justiça em uma localização, com papel determinado e ligado a uma Central de Mandados específica.
	 */
	public String inserir() {
		if (isCadastroExistente()) {
			return new String("erro");
		}
		UsuarioLocalizacaoHome ulh = UsuarioLocalizacaoHome.instance();
		UsuarioLocalizacao ul = criarNovoUsuarioLocalizacao(ulh);
		this.instance.setUsuarioLocalizacao(ul);
		this.instance.setCentralMandado(this.centralMandado);
		this.instance.setLocalizacao(this.localizacaoFisica);
		this.instance.setPapel(this.papel);
		getEntityManager().persist(this.instance);
		EntityUtil.flush();
		ulh.newInstance();
		newInstance();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Informação inserida");
		return new String();
	}

	/**
	 * Altera o cadastro selecionado na tela.
	 */
	@Override
	public String update() {
		if (isCadastroExistente()) {
			return new String("erro");
		}
		this.instance.getUsuarioLocalizacao().setLocalizacaoFisica(this.getLocalizacaoFisica());
		this.instance.getUsuarioLocalizacao().setPapel(this.getPapel());
		this.instance.setCentralMandado(this.centralMandado);
		getEntityManager().persist(this.instance);
		EntityUtil.flush();
		newInstance();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Informação atualizada");
		return new String();
	}
	
	private boolean isCadastroExistente() {
		List<UsuarioLocalizacao> ull = getUsuarioLocalizacaoManager().getLocalizacoesAtuais(this.getPessoa(), this.getPapel(), this.getLocalizacaoFisica());
		if (ProjetoUtil.isNotVazio(ull)) {
			if (getOficialJusticaCentralMandadoManager().isExisteCadastro(this.instance.getCentralMandado(), ull)) {
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.WARN, "oficialJusticaCentralMandado.aviso.cadastroExiste");
				return true;
			}
		}
		return false;
	}
	
	private UsuarioLocalizacao criarNovoUsuarioLocalizacao(UsuarioLocalizacaoHome ulh) {
		UsuarioLocalizacao ul = new UsuarioLocalizacao();
		ul.setPapel(this.getPapel());
		ul.setUsuario(this.getPessoa());
		ul.setLocalizacaoFisica(this.getLocalizacaoFisica());
		ulh.setLocalizacaoFisica(ul.getLocalizacaoFisica());
		ulh.setLocalizacaoModelo(ul.getLocalizacaoModelo());
		ulh.setPapel(ul.getPapel());
		
		ulh.setInstance(ul);
		ulh.persist();
		return ul;
	}

	public void initCentraisMandadoList(){
		if(this.centralMandadoList == null) {
			this.centralMandadoList = getCentralMandadoManager().obterPorLocalizacoes(Authenticator.getLocalizacoesFilhasAtuais());
		}
	}
	
	/**
	 * Lista os papeis cadastrados como Oficial de justia, comum e distribuidor.
	 * @return Lista com papeis
	 */
	public void initPapelOficialJusticaList() {
		if(CollectionUtilsPje.isEmpty(papeisOJ)) {
			try {
				papeisOJ = new ArrayList<Papel>(2);
				papeisOJ.add(getPapelManager().findByCodeName(Papeis.OFICIAL_JUSTICA));
				papeisOJ.add(getPapelManager().findByCodeName(Papeis.OFICIAL_JUSTICA_DISTRIBUIDOR));
			} catch (PJeBusinessException e) {
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR,"grupoOficialJusticaCentralMandado.erro.recuperaPapeis");
				e.printStackTrace();
				papeisOJ = null;
			}		
		}
	}
	
	public void initLocalizacoesFisicasRootList() {
		localizacaoFisicaRootList = null;
		localizacaoFisica = null;
		if(centralMandado != null) {
			localizacaoFisicaRootList = getCentralMandadoManager().obterLocalizaoes(centralMandado, Authenticator.getLocalizacoesFilhasAtuais());
		}
	}
	
	public List<Papel> getPapeisOJ() {
		return papeisOJ;
	}

	public void setPapeisOJ(List<Papel> papeisOJ) {
		this.papeisOJ = papeisOJ;
	}

	public List<CentralMandado> getCentralMandadoList() {
		return centralMandadoList;
	}

	public void setCentralMandadoList(List<CentralMandado> centralMandadoList) {
		this.centralMandadoList = centralMandadoList;
	}

	public void setPapelOficialJustica(ValueChangeEvent evento) {
		this.setPapel((Papel) evento.getNewValue());
	}
	
	public List<Localizacao> getLocalizacaoFisicaRootList() {
		return localizacaoFisicaRootList;
	}

	public void setLocalizacaoFisicaRootList(List<Localizacao> localizacaoFisicaRootList) {
		this.localizacaoFisicaRootList = localizacaoFisicaRootList;
	}

	public Localizacao getLocalizacaoFisica() {
		return localizacaoFisica;
	}

	public void setLocalizacaoFisica(Localizacao localizacaoFisica) {
		this.localizacaoFisica = localizacaoFisica;
	}
	
	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	public Papel getPapel() {
		return papel;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public CentralMandado getCentralMandado() {
		return centralMandado;
	}

	public void setCentralMandado(CentralMandado centralMandado) {
		this.centralMandado = centralMandado;
		
	}
}