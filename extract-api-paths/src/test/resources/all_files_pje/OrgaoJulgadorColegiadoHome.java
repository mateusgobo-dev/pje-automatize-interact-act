package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.DAO.EntityList;
import br.com.infox.cliente.component.tree.OrgaoJulgadorColegiadoTreeHandler;
import br.com.infox.ibpm.component.tree.LocalizacaoTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.com.jt.pje.manager.OrgaoJulgadorColegiadoOrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.enums.RelatorRevisorEnum;

/**
 * Classe para operações com "Orgao Julgador Colegiado"
 * 
 */
@Name("orgaoJulgadorColegiadoHome")
@BypassInterceptors
public class OrgaoJulgadorColegiadoHome extends AbstractOrgaoJulgadorColegiadoHome<OrgaoJulgadorColegiado> {

	private static final long serialVersionUID = 1L;

	public void limparTrees() {
		OrgaoJulgadorColegiadoTreeHandler ath = getComponent(OrgaoJulgadorColegiadoTreeHandler.NAME);
		ath.clearTree();
	}

	public static OrgaoJulgadorColegiadoHome instance() {
		return ComponentUtil.getComponent("orgaoJulgadorColegiadoHome");
	}

	@Override
	public void newInstance() {
		limparTrees();
		super.newInstance();
	}

	public Localizacao getLocalizacaoAtual() {
		return Authenticator.getLocalizacaoAtual();
	}

	public String inactiveRecursive(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		if (orgaoJulgadorColegiado.getOrgaoJulgadorColegiadoList().size() > 0) {
			inativarFilhos(orgaoJulgadorColegiado);
		}
		orgaoJulgadorColegiado.setAtivo(Boolean.FALSE);
		String ret = super.update();
		limparTrees();
		return ret;
	}

	private void inativarFilhos(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		orgaoJulgadorColegiado.setAtivo(Boolean.FALSE);
		Integer quantidadeFilhos = orgaoJulgadorColegiado.getOrgaoJulgadorColegiadoList().size();
		for (int i = 0; i < quantidadeFilhos; i++) {
			inativarFilhos(orgaoJulgadorColegiado.getOrgaoJulgadorColegiadoList().get(i));
		}
	}

	@SuppressWarnings("unchecked")
	public List<AplicacaoClasse> getAplicacaoClasseItems() {
		StringBuffer ejbql = new StringBuffer();
		ejbql.append("select o from AplicacaoClasse o ");
		ejbql.append("where o.ativo = true ");
		return EntityUtil.createQuery(ejbql.toString()).getResultList();
	}

	/**
	 * Obtem a lista de orgaos julgadores elegiveis para ser o presidente do orgao julgador colegiado
	 * @return	retorna a lista de orgaos elegiveis a presidente do Colegiado
	 */
	public List<OrgaoJulgador> getOrgaosJulgadoresElegiveisParaPresidenteDoColegiado(){
		OrgaoJulgadorColegiadoOrgaoJulgadorManager ojcOjManager = 
				ComponentUtil.getComponent("orgaoJulgadorColegiadoOrgaoJulgadorManager");
		
		List<OrgaoJulgador> resultado = new ArrayList<OrgaoJulgador>();

		List<OrgaoJulgadorColegiadoOrgaoJulgador> orgaoJulgadorColegiadoOrgaoJulgador = getInstance().getOrgaoJulgadorColegiadoOrgaoJulgadorList();
		for (OrgaoJulgadorColegiadoOrgaoJulgador ojcOrgaoJulgador : orgaoJulgadorColegiadoOrgaoJulgador) {
			if(ojcOjManager.isVinculoOJSingularComColegiadoAtivo(ojcOrgaoJulgador)){
				resultado.add(ojcOrgaoJulgador.getOrgaoJulgador());
			}
		}
		return resultado;
	}
		
	/**
	 * Verifica se HÁ um OJ já relacionado à localização indicada
	 * 
	 * @param OJC
	 * @param localizacao
	 * @return false se não houver nada diferente, true se houver pelo menos um OJ vinculado à localização
	 */
	private boolean isLocalizacaoPossuiOJDistinto(Localizacao localizacao) {
  		OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent("orgaoJulgadorManager");
  		
  		if(orgaoJulgadorManager.getOrgaoJulgadorByLocalizacaoExata(localizacao) == null) {
			return false;
  		}
  		
  		return true;
	}
	
	/**
	 * Busca entre as localiações folha do tribunal ou folhas abaixo da localização física do usuário logado
	 * - eliminando as localizacoes fisicas já utilizadas por OJs ou OJCs
	 * 
	 * @return
	 * @throws PJeBusinessException
	 */
	public List <Localizacao> getLocalizacoesFisicasPossiveis(){
		LocalizacaoManager localizacaoManager = ComponentUtil.getComponent(LocalizacaoManager.class);
		List<Localizacao> localizacoesList = new ArrayList<>();

		localizacoesList = localizacaoManager.getArvoreDescendente(Authenticator.getIdLocalizacaoFisicaAtual(), true);

		// filtra as localizacoes fisicas que nao possuem nem OJ, nem OJC
		List<Localizacao> localizacoesSemOJouOJCList = new ArrayList<>();
		if(localizacoesList.size() > 0) {
			for (Localizacao localizacao : localizacoesList) {
				if(!this.isLocalizacaoPossuiOJDistinto(localizacao)) {
					localizacoesSemOJouOJCList.add(localizacao);
				}
			}
		}
		

		Collections.sort(localizacoesSemOJouOJCList, (Localizacao p1, Localizacao p2)->p1.getLocalizacao().compareTo(p2.getLocalizacao()));		

		return localizacoesSemOJouOJCList;
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoPaiItems() {
		StringBuffer ejbql = new StringBuffer();
		ejbql.append("select o from OrgaoJulgadorColegiado o ");
		ejbql.append("where orgaoJulgadorColegiadoPai is null and ");
		ejbql.append("o.instancia != '1'");
		return EntityUtil.createQuery(ejbql.toString()).getResultList();
	}
	
	public List<OrgaoJulgadorColegiado> getListaOJC(){
		EntityList<OrgaoJulgadorColegiado> lista = ComponentUtil.getComponent("orgaoJulgadorColegiadoItemsList");
		return lista.getResultList();
	}

	private Boolean verificarPrazos() {
		if (instance.getMinimoParticipante() != null && (instance.getMinimoParticipante() <= 0)) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"A Quantidade Mínima de Participantes do Julgamento deve ser maior que 0.");
			return Boolean.FALSE;
		} else {
			if (instance.getMaximoProcessoPauta() != null && (instance.getMaximoProcessoPauta() <= 0)) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						"A Quantidade Máxima de Processos em Pauta deve ser maior que 0.");
				return Boolean.FALSE;
			} else {
				if (instance.getDiaCienciaInclusaoPauta() != null && (instance.getDiaCienciaInclusaoPauta() <= 0)) {
					FacesMessages.instance().add(StatusMessage.Severity.ERROR,
							"O Prazo para Ciência e Inclusão em Pauta deve ser maior que 0.");
					return Boolean.FALSE;
				} else {
					if (instance.getDiaRetiradaAdiada() != null && (instance.getDiaRetiradaAdiada() <= 0)) {
						FacesMessages.instance().add(StatusMessage.Severity.ERROR,
								"O Prazo para Retirar da Lista de Adiados deve ser maior que 0.");
						return Boolean.FALSE;
					} else {
						if (instance.getPrazoTermino() != null && (instance.getPrazoTermino() <= 0)) {
							FacesMessages.instance().add(StatusMessage.Severity.ERROR,
									"O Aviso Fechamento da Pauta deve ser maior que 0.");
							return Boolean.FALSE;
						} else {
							if (instance.getPrazoDisponibilizaJulgamento() != null
									&& (instance.getPrazoDisponibilizaJulgamento() <= 0)) {
								FacesMessages.instance().add(StatusMessage.Severity.ERROR,
										"O Prazo Disponibilização da Relação de Julgamento deve ser maior que 0.");
								return Boolean.FALSE;
							} else {
								return Boolean.TRUE;
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Método responsável por validar os valores dos campos de composição reduzida e integral.
	 * 
	 * @return
	 */
	private boolean validaComposicoes(){
		Boolean retorno = false;
		int qtdOj = 0;
		
		if(this.isManaged()){
			qtdOj = getQuantidadeOrgaoJulgadoresOJC(instance);
			if(validaComposicaoIntegral(qtdOj) && validaComposicaoReduzida(qtdOj)){
				retorno = true;
			}
		}
		return retorno;
	}
	
	/**
	 * Método responsável por validar a composição integral.
	 * 
	 * @param qtdOj Quantidade de órgões julgadores.
	 * @return Verdadeiro se a composição integral for maior que zero e menor ou 
	 * igual a quantidade de órgãos julgadores com período ativo. Falso, caso contrário.
	 */
	private boolean validaComposicaoIntegral(int qtdOj){
		if (instance.getQuantidadeJulgadoresComposicaoIntegral() > 0 &&
				instance.getQuantidadeJulgadoresComposicaoIntegral() <= qtdOj) {
			
			return true;
		}
		FacesMessages.instance().add(StatusMessage.Severity.ERROR,
			"A composição integral deve ter valor maior que zero e menor ou igual a quantidade de órgãos julgadores com período ativo.");
		
		return false;
	}
	
	/**
	 * Método responsável por validar a composição reduzida.
	 * 
	 * @param qtdOj Quantidade de órgões julgadores.
	 * @return Verdadeiro se:
	 * 	<ul>
	 * 		<li>A composição reduzida não pode ter valor nulo.</li>
	 * 		<li>A composição reduzida deve ter valor menor ou igual a composição integral com período ativo.</li>
	 * 		<li>A composição reduzida não pode ter valor maior que a quantidade de órgãos julgadores com período ativo.</li>
	 * 	</ul>
	 * Falso, caso contrário.
	 */
	private Boolean validaComposicaoReduzida(int qtdOj){
		Boolean retorno = true;
		
		if(instance.getQuantidadeJulgadoresComposicaoReduzida() == 0){
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "A composição reduzida não pode ter valor nulo.");
			retorno = false;
		}
		if(instance.getQuantidadeJulgadoresComposicaoReduzida() > instance.getQuantidadeJulgadoresComposicaoIntegral()){
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, 
				"A composição reduzida deve ter valor menor ou igual a composição integral com período ativo.");
			
			retorno = false;
		}
		if (instance.getQuantidadeJulgadoresComposicaoReduzida() > qtdOj) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, 
				"A composição reduzida não pode ter valor maior que a quantidade de órgãos julgadores com período ativo.");
			
			retorno = false;
		}

		return retorno;
	}
	
	private int getQuantidadeOrgaoJulgadoresOJC(OrgaoJulgadorColegiado ojc){
		int retorno = 0;
		
		OrgaoJulgadorColegiadoOrgaoJulgadorManager ojcojm = ComponentUtil.getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorManager.NAME); 
		
		if(ojcojm != null){
			retorno = ojcojm.countOrgaoJulgadorPorOrgaoJulgadorColegiado(instance).intValue();
		}
		
		return retorno;
	}

	@Override
	public String persist() {
		String ret = null;
		
		if(this.isLocalizacaoPossuiOJDistinto(getInstance().getLocalizacao())) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					FacesUtil.getMessage("entity_messages", "localizacao.erro.relacionamentoOJ"));
			return "false";
		}
		
		if(!validaComposicoes()){
			this.instance.setAtivo(false);
		}
		
		if (verificarPrazos()) {
			ret = super.persist();
		}
		return ret;
	}

	@Override
	public String update() {
		String ret = null;
		
		if(this.isLocalizacaoPossuiOJDistinto(getInstance().getLocalizacao())) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					FacesUtil.getMessage("entity_messages", "localizacao.erro.relacionamentoOJ"));
			return "false";
		}
		
		if(!validaComposicoes()){
			this.instance.setAtivo(false);
		}
		
		if (verificarPrazos()) {
			ret = super.update();
		}
		return ret;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);

		if (isManaged() && changed) {
			LocalizacaoTreeHandler localizacaoTreeHandler = (LocalizacaoTreeHandler) getComponent("localizacaoTree");
			localizacaoTreeHandler.setSelected(getInstance().getLocalizacao());
		}
	}

	public RelatorRevisorEnum[] getRelatorRevisorEnumValues() {
		return RelatorRevisorEnum.values();
	}

	public void resetarRelatorPedirPauta() {
		if (getInstance().getRelatorRevisor() == RelatorRevisorEnum.REV) {
			getInstance().setPautaAntecRevisao(Boolean.FALSE);
		}
	}
}