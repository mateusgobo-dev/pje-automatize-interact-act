package br.jus.csjt.pje.view.action;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.csjt.pje.commons.model.dto.ObrigacaoFazerParteDTO;
import br.jus.csjt.pje.view.action.component.list.ObrigacaoFazerList;
import br.jus.pje.jt.entidades.ObrigacaoFazer;
import br.jus.pje.jt.enums.CredorDevedorEnum;
import br.jus.pje.jt.enums.ObrigacaoFazerEnum;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name(ObrigacaoFazerHome.NAME)
@BypassInterceptors
public class ObrigacaoFazerHome extends AbstractHome<ObrigacaoFazer> {

	public static final String NAME = "obrigacaoFazerHome";
	private static final long serialVersionUID = 1L;
	private List<ObrigacaoFazerParteDTO> listaObrigacaoFazerParteDTO;
	
	//
	private boolean showModal;

	private ProcessoTrfHome getProcessoTrfHome() {
		return ComponentUtil.getComponent("processoTrfHome");
	}

	public void setarProcessoTrfNoList() {
		ProcessoTrfHome processoTrfHome = getProcessoTrfHome();
		ObrigacaoFazerList obrigacaoFazerList = ComponentUtil.getComponent("obrigacaoFazerList");

		if (processoTrfHome.getInstance().getIdProcessoTrf() != 0) {
			obrigacaoFazerList.setEjbql("select o from ObrigacaoFazer o where o.processoTrf.idProcessoTrf = "
					+ processoTrfHome.getInstance().getIdProcessoTrf());
		}

	}

	public void popularListaObrigacaoFazerParteDTO() {
		if (listaObrigacaoFazerParteDTO == null) {
			criarListaObrigacaoFazerParteDTO();
		}
	}

	public void inicializarModal() {
		if (getProcessoTrfHome().isManaged()) {
			/* Correção para [PJEII-2032] Listagem errada das partes - Associar partes no Registro de Obrigações de Fazer e Não Fazer (Importação AUD) 
			 */
			
			listaObrigacaoFazerParteDTO=null;
			setarProcessoTrfNoList();
			popularListaObrigacaoFazerParteDTO();
			//[PJEII-3410] Inclusão do atributo para apresentar a modal quando o botão for selecionado. 
			showModal = true;
		}
	}

	@Override
	public void newInstance() {
		super.newInstance();

		criarListaObrigacaoFazerParteDTO();
	}

	private void criarListaObrigacaoFazerParteDTO() {
		listaObrigacaoFazerParteDTO = new ArrayList<ObrigacaoFazerParteDTO>();

		List<ProcessoParte> listaPartes = getProcessoTrfHome().getInstance().getProcessoParteList();

		for (ProcessoParte pp : listaPartes) {
			ObrigacaoFazerParteDTO obrigacaoFazerParteDTO = new ObrigacaoFazerParteDTO();
			obrigacaoFazerParteDTO.setParte(pp);
			obrigacaoFazerParteDTO
					.setCredorDevedor(pp.getInParticipacao() == ProcessoParteParticipacaoEnum.A ? CredorDevedorEnum.C
							: CredorDevedorEnum.D);
			listaObrigacaoFazerParteDTO.add(obrigacaoFazerParteDTO);
		}
	}

	public ObrigacaoFazerEnum[] getObrigacaoFazerValues() {
		return ObrigacaoFazerEnum.values();
	}

	public CredorDevedorEnum[] getCredorDevedorValues() {
		return CredorDevedorEnum.values();
	}

	private String validarParticipacoes() {
		String retorno = "";
		int quantidadeCredor = 0;
		int quantidadeDevedor = 0;

		for (ObrigacaoFazerParteDTO obrigacaoFazerParteDTO : listaObrigacaoFazerParteDTO) {
			if (obrigacaoFazerParteDTO.getSelecionado()
					&& obrigacaoFazerParteDTO.getCredorDevedor() == CredorDevedorEnum.C)
				quantidadeCredor++;
			else {
				if (obrigacaoFazerParteDTO.getSelecionado()) {
					quantidadeDevedor++;
				}
			}

		}

		if (quantidadeCredor == 0) {
			retorno += "Um credor deve ser selecionado!";
		} else {
			if (quantidadeCredor > 1) {
				retorno += "Apenas um credor pode ser selecionado!";
			}
		}

		if (quantidadeDevedor == 0)
			retorno += "\nPelo menos um devedor deve ser selecionado!";

		return retorno;
	}

	private ObrigacaoFazerParteDTO buscarCredor() {
		for (ObrigacaoFazerParteDTO obrigacaoFazerParteDTO : listaObrigacaoFazerParteDTO) {
			if (obrigacaoFazerParteDTO.getCredorDevedor() == CredorDevedorEnum.C
					&& obrigacaoFazerParteDTO.getSelecionado()) {
				return obrigacaoFazerParteDTO;
			}
		}
		return null;
	}

	private List<ObrigacaoFazerParteDTO> buscarListaDevedor() {
		List<ObrigacaoFazerParteDTO> devedorList = new ArrayList<ObrigacaoFazerParteDTO>();

		for (ObrigacaoFazerParteDTO obrigacaoFazerParteDTO : listaObrigacaoFazerParteDTO) {
			if (obrigacaoFazerParteDTO.getCredorDevedor() == CredorDevedorEnum.D
					&& obrigacaoFazerParteDTO.getSelecionado()) {
				devedorList.add(obrigacaoFazerParteDTO);
			}
		}
		return devedorList;
	}

	private void limparParticipantesSelecionados() {
		for (ObrigacaoFazerParteDTO obrigacaoFazerParteDTO : listaObrigacaoFazerParteDTO) {
			obrigacaoFazerParteDTO.setSelecionado(false);
		}
	}

	@Override
	public String persist() {
		// Capturando dados do formulario
		ObrigacaoFazerEnum obrigacao = this.instance.getObrigacao();
		Boolean multaDescumprimento = this.instance.getMultaDescumprimento();
		String descricao = this.instance.getDescricao();
		Integer prazo = this.instance.getPrazo();

		String validacao = validarParticipacoes();
		
		if(prazo == 0){
			validacao += " O prazo tem que ser maior que 0!";
		}

		if (!validacao.equals("")) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, validacao);
			return validacao;
		}

		ObrigacaoFazerParteDTO credor = buscarCredor();
		List<ObrigacaoFazerParteDTO> devedorList = buscarListaDevedor();

		for (ObrigacaoFazerParteDTO obrigacaoFazerParteDTO : devedorList) {
			/*
			 * Preenchendo o instance pq o relacionamento deve ser criado varias
			 * vezes e uma nova entidade é instanciada logo após ser persistida.
			 */
			this.instance.setObrigacao(obrigacao);
			this.instance.setMultaDescumprimento(multaDescumprimento);
			this.instance.setDescricao(descricao);
			this.instance.setPrazo(prazo);
			this.instance.setProcessoTrf(getProcessoTrfHome().getInstance());
			this.instance.setCredor(credor.getParte());
			this.instance.setDevedor(obrigacaoFazerParteDTO.getParte());

			super.persist();
			// Instanciando uma nova entidade para persistir o novo
			// relacionamento
			this.newInstance();
		}

		limparParticipantesSelecionados();

		return "persisted";
	}

	public List<ObrigacaoFazerParteDTO> getListaObrigacaoFazerParteDTO() {
		if (listaObrigacaoFazerParteDTO == null) {
			popularListaObrigacaoFazerParteDTO();
		}
		return listaObrigacaoFazerParteDTO;
	}

	public List<ObrigacaoFazerParteDTO> getListaObrigacaoFazerParteDTOAtivos() {
		List<ObrigacaoFazerParteDTO> listaObrigacaoFazerParteDTOAtivos = new ArrayList<ObrigacaoFazerParteDTO>();

		for (ObrigacaoFazerParteDTO obrigacaoFazerParteDTO : listaObrigacaoFazerParteDTO) {
			if (obrigacaoFazerParteDTO.getSelecionado()) {
				listaObrigacaoFazerParteDTOAtivos.add(obrigacaoFazerParteDTO);
			}
		}

		return listaObrigacaoFazerParteDTOAtivos;
	}

	public void associarPartes() {	}

	public void removerParte(ObrigacaoFazerParteDTO obrigacaoFazerParteDTORemover) {
		for (ObrigacaoFazerParteDTO obrigacaoFazerParteDTO : listaObrigacaoFazerParteDTO) {
			if (obrigacaoFazerParteDTO.equals(obrigacaoFazerParteDTORemover)) {
				obrigacaoFazerParteDTO.setSelecionado(false);
			}
		}
	}

	public boolean isNovaInstancia() {
		return this.instance == null || this.instance.getIdObrigacaoFazer() == 0;
	}

	@Override
	public String remove(ObrigacaoFazer obj) {
		String retorno = "";
		try {
			retorno = super.remove(obj);
		} catch (IllegalArgumentException e) {
			// Esta exception é lançada quando o usuário tenta remover o mesmo
			// registro mais de uma vez.
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "Registro excluído com sucesso");
		}
		return retorno;
	}

	public boolean possuiObrigacaoFazer(ProcessoTrf processoTrf) {
		Query query = getEntityManager().createQuery(
				"select count(o) from ObrigacaoFazer o where o.processoTrf = :processo");
		query.setParameter("processo", processoTrf);
		Number count = (Number) query.getSingleResult();

		return count.intValue() >= 1;
	}

	public boolean carregarCampoDescricao() {
		if (this.instance != null && this.instance.getObrigacao() != null)
			return this.instance.getObrigacao() == ObrigacaoFazerEnum.OUT;

		return false;
	}

	public boolean isShowModal() {
		return showModal;
	}

	public void setShowModal(boolean showModal) {
		this.showModal = showModal;
	}

}
