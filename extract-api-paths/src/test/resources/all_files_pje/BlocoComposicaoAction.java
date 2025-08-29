/**
 *
 **/
package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.pje.nucleo.entidades.BlocoComposicao;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;

/**
 * 
 * 
 */
@Name(BlocoComposicaoAction.NAME)
@Scope(ScopeType.PAGE)
public class BlocoComposicaoAction {
	
	public static final String NAME = "blocoComposicaoAction";

	@RequestParameter("idBloco")
	private Integer idBloco;
	
	private List<BlocoComposicao> listComposicaoBloco;
	private List<OrgaoJulgador> listOrgaoJulgadorComposicao;
	private BlocoComposicao participanteComposicao;
	private BlocoJulgamento bloco;
	
	private boolean alterar;

	private List<OrgaoJulgador> listOrgaoJulgadorColegiadoSessao;
	private List<OrgaoJulgador> listOrgaoJulgadorOutrosColegiados;
	private List<UsuarioLocalizacaoMagistradoServidor> listMagistrados;

	private UsuarioLocalizacaoMagistradoServidor magistradoSelecionado;
	private PessoaMagistrado magistradoPresente;
	private OrgaoJulgador orgaoJulgador;
	
	@Create
	public void init(){
		if(getListComposicaoBloco() == null || getListComposicaoBloco().size() == 0) {
			try {
				ComponentUtil.getBlocoComposicaoManager().gerarComposicaoBloco(getBloco());
				listComposicaoBloco = ComponentUtil.getBlocoComposicaoManager().findByBlocoPresentes(getBloco());
				listOrgaoJulgadorComposicao = ComponentUtil.getBlocoComposicaoManager().recuperarOrgaoJulgadorPorBloco(bloco);
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			}
		}
		setParticipanteComposicao(new BlocoComposicao());
	}
	
	public void incluir(){
		if(!estaNaComposicao(getParticipanteComposicao().getOrgaoJulgador())){
			getParticipanteComposicao().setBloco(getBloco());
			getParticipanteComposicao().setTipoAtuacaoMagistrado(TipoAtuacaoMagistradoEnum.VOGAL);
			getParticipanteComposicao().setMagistradoPresente(getMagistradoPresente());
			getParticipanteComposicao().setCargoAtuacao(getCargoAtuacao());
			getParticipanteComposicao().setImpedidoSuspeicao(Boolean.FALSE);
			getParticipanteComposicao().setDefinidoPorUsuario(Boolean.FALSE);
			setOrgaoJulgador(getParticipanteComposicao().getOrgaoJulgador());
			ComponentUtil.getBlocoComposicaoManager().mergeAndFlush(getParticipanteComposicao());
			limparFormulario();
			listComposicaoBloco = ComponentUtil.getBlocoComposicaoManager().findByBloco(getBloco());
			listOrgaoJulgadorComposicao = ComponentUtil.getBlocoComposicaoManager().recuperarOrgaoJulgadorPorBloco(bloco);
			setParticipanteComposicao(new BlocoComposicao());
			FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso!");
		}else{
			FacesMessages.instance().add(Severity.ERROR, "O órgão julgador selecionado já está na composição deste bloco!");
		}
	}

	private boolean estaNaComposicao(OrgaoJulgador orgaoJulgadorSelecionado) {
		boolean retorno = false;
		if(listComposicaoBloco != null) {
			for (BlocoComposicao blocoComposicao : listComposicaoBloco) {
				if(blocoComposicao.getOrgaoJulgador().equals(orgaoJulgadorSelecionado)){
					retorno = true;
					break;
				}
			}
		}
		return retorno;
	}

	public void excluir(BlocoComposicao composicao){
		try {
			BlocoComposicao entidadeDelete = ComponentUtil.getBlocoComposicaoManager().findById(composicao.getIdBlocoComposicao());
			ComponentUtil.getBlocoComposicaoManager().remove(entidadeDelete);
			ComponentUtil.getBlocoComposicaoManager().flush();
			limparFormulario();
			listComposicaoBloco = ComponentUtil.getBlocoComposicaoManager().findByBloco(getBloco());
			listOrgaoJulgadorComposicao = ComponentUtil.getBlocoComposicaoManager().recuperarOrgaoJulgadorPorBloco(bloco);
			setParticipanteComposicao(new BlocoComposicao());
			FacesMessages.instance().add(Severity.INFO, "Registro excluído com sucesso!");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	public void alterar(){
		getParticipanteComposicao().setMagistradoPresente(getMagistradoPresente());
		getParticipanteComposicao().setCargoAtuacao(getCargoAtuacao());
		ComponentUtil.getBlocoComposicaoManager().mergeAndFlush(getParticipanteComposicao());
		limparFormulario();
		listComposicaoBloco = ComponentUtil.getBlocoComposicaoManager().findByBloco(getBloco());
		listOrgaoJulgadorComposicao = ComponentUtil.getBlocoComposicaoManager().recuperarOrgaoJulgadorPorBloco(bloco);
		setParticipanteComposicao(new BlocoComposicao());
		FacesMessages.instance().add(Severity.INFO, "Registro alterado com sucesso!");
		setAlterar(false);
	}
	
	
	
	public void initAlterar(BlocoComposicao composicao) throws PJeBusinessException{
		// Recuperar sessão já que  o objeto não está vindo sempre com o valor carregado
		bloco = ComponentUtil.getBlocoJulgamentoManager().findById(bloco.getIdBlocoJulgamento()); 
		setParticipanteComposicao(composicao);
		setMagistradoSelecionado(ComponentUtil.getUsuarioLocalizacaoMagistradoServidorManager().
				obterLocalizacaoAtivaPriorizandoColegiado(composicao.getMagistradoPresente().getIdUsuario(), composicao.getOrgaoJulgador(), bloco.getSessao().getOrgaoJulgadorColegiado() ));
		setAlterar(true);
	}
	
	public void limparFormulario(){
		setParticipanteComposicao(new BlocoComposicao());
		setMagistradoSelecionado(null);
		setAlterar(Boolean.FALSE);
	}
	
	public PessoaMagistrado getMagistradoPresente() {
		if(magistradoSelecionado != null){ 
			PessoaMagistrado magistrado = null;
			try {
				PessoaMagistradoManager pessoaMagistradoManager = ComponentUtil.getComponent(PessoaMagistradoManager.class);
				magistrado = pessoaMagistradoManager.findById(magistradoSelecionado.getUsuarioLocalizacao().getUsuario().getIdUsuario());
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			}
			this.magistradoPresente = magistrado;
		}
		return magistradoPresente;
	}
	
	public OrgaoJulgadorCargo getCargoAtuacao() {
		if (magistradoSelecionado != null){
			return magistradoSelecionado.getOrgaoJulgadorCargo();
		}
		return null;
	}
	
	

	public List<UsuarioLocalizacaoMagistradoServidor> getListMagistrados() {
		if(participanteComposicao.getOrgaoJulgador() != null){
			listMagistrados = ComponentUtil.getUsuarioLocalizacaoMagistradoServidorManager().obterLocalizacoesMagistrados(participanteComposicao.getOrgaoJulgador(), null, null, null, null);
		}
		return listMagistrados;
	}
	
	public void setListMagistrados(List<UsuarioLocalizacaoMagistradoServidor> listMagistrados) {
		this.listMagistrados = listMagistrados;
	}
	
	/**
	 * Retorna uma lista de orgãos julgadores, com os orgãos julgadores do colegiado da sessão primeiro.
	 */
	public List<OrgaoJulgador> getListOrgaosJulgadores() {
		List<OrgaoJulgador> list = new ArrayList<OrgaoJulgador>();
		List<OrgaoJulgador> tmp = new ArrayList<OrgaoJulgador>();
		list.addAll(getListOrgaoJulgadorColegiadoSessao());
		tmp.addAll(getListOrgaoJulgadorOutrosColegiados());
		tmp.removeAll(list);
		list.addAll(tmp);
		return list;
	}
	
	/**
	 * Retorna uma lista de Orgãos Julgadores de acordo com o colegiado da sessão.
	 */
	
	public List<OrgaoJulgador> getListOrgaoJulgadorColegiadoSessao() {
		if(listOrgaoJulgadorColegiadoSessao == null){
			if(getBloco() != null){
				listOrgaoJulgadorColegiadoSessao = ComponentUtil.getOrgaoJulgadorManager().orgaosPorColegiado(bloco.getSessao().getOrgaoJulgadorColegiado());
			}
		}
		return listOrgaoJulgadorColegiadoSessao;
	}
	
	
	public void setListOrgaoJulgadorColegiadoSessao(List<OrgaoJulgador> listOrgaoJulgadorColegiadoSessao) {
		this.listOrgaoJulgadorColegiadoSessao = listOrgaoJulgadorColegiadoSessao;
	}
	
	/**
	 * Retorna uma lista de todos os Orgãos Julgadores ativos.
	 */
	public List<OrgaoJulgador> getListOrgaoJulgadorOutrosColegiados() {
		if(listOrgaoJulgadorOutrosColegiados == null && getListOrgaoJulgadorColegiadoSessao() != null){
			listOrgaoJulgadorOutrosColegiados = ComponentUtil.getOrgaoJulgadorManager().findAll();
			listOrgaoJulgadorOutrosColegiados.removeAll(getListOrgaoJulgadorColegiadoSessao());
		}
		return listOrgaoJulgadorOutrosColegiados;
	}
	
	public void setListOrgaoJulgadorOutrosColegiados(List<OrgaoJulgador> listOrgaoJulgadorOutrosColegiados) {
		this.listOrgaoJulgadorOutrosColegiados = listOrgaoJulgadorOutrosColegiados;
	}
	
	public BlocoComposicao getParticipanteComposicao() {
		return participanteComposicao;
	}

	public void setParticipanteComposicao(BlocoComposicao participanteComposicao) {
		this.participanteComposicao = participanteComposicao;
	}

	public UsuarioLocalizacaoMagistradoServidor getMagistradoSelecionado() {
		return magistradoSelecionado;
	}

	public void setMagistradoSelecionado(UsuarioLocalizacaoMagistradoServidor magistradoSelecionado) {
		this.magistradoSelecionado = magistradoSelecionado;
	}

	public boolean isAlterar() {
		return alterar;
	}

	public void setAlterar(boolean alterar) {
		this.alterar = alterar;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}	
	
	public List<OrgaoJulgador> getListOrgaoJulgadorComposicao(BlocoJulgamento blocolocal) {
		if(listOrgaoJulgadorComposicao == null){
			listOrgaoJulgadorComposicao = ComponentUtil.getBlocoComposicaoManager().recuperarOrgaoJulgadorPorBloco(blocolocal);
		}
		return listOrgaoJulgadorComposicao;
	}


	public List<BlocoComposicao> getListComposicaoBloco() {
		if(listComposicaoBloco == null){
			if(getBloco() != null){ 
				listComposicaoBloco = ComponentUtil.getBlocoComposicaoManager().findByBloco(getBloco());
			}
		}
		return listComposicaoBloco;
	}

	public void setListComposicaoBloco(List<BlocoComposicao> listComposicaoBloco) {
		this.listComposicaoBloco = listComposicaoBloco;
	}

	public BlocoJulgamento getBloco() {
		if(bloco == null){
			if(idBloco != null){
				try {
					bloco = ComponentUtil.getBlocoJulgamentoManager().findById(idBloco);
				} catch (PJeBusinessException e) {
					FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
				}
			}
		}
		return bloco;
	}

	public void setBloco(BlocoJulgamento bloco) {
		this.bloco = bloco;
	}

	public List<OrgaoJulgador> getListOrgaoJulgadorComposicao() {
		if(listOrgaoJulgadorComposicao == null){
			if(getBloco() != null){ 
				listOrgaoJulgadorComposicao = ComponentUtil.getBlocoComposicaoManager().recuperarOrgaoJulgadorPorBloco(bloco);
			}
		}
		return listOrgaoJulgadorComposicao;
	}

	public void setListOrgaoJulgadorComposicao(List<OrgaoJulgador> listOrgaoJulgadorComposicao) {
		this.listOrgaoJulgadorComposicao = listOrgaoJulgadorComposicao;
	}
}
