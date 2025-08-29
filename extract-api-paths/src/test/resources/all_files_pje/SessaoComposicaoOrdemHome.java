package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoComposicaoOrdemManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;

@Name(SessaoComposicaoOrdemHome.NAME)
@AutoCreate
public class SessaoComposicaoOrdemHome extends AbstractHome<SessaoComposicaoOrdem> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "sessaoComposicaoOrdemHome";
	private PessoaMagistrado magistradoSubstituto;
	private Boolean continuaSessaoDiv = Boolean.FALSE;
	private Boolean presidente = null;
	private SessaoProcessoDocumentoVoto sessaoVoto;
	private List<PessoaMagistrado> listaSubstituto = new ArrayList<PessoaMagistrado>();
	private OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoRevisor;
	private List<PessoaMagistrado> listaMagistradosDisponiveis = null;

	@In
	private SessaoComposicaoOrdemManager sessaoComposicaoOrdemManager;
	
	@In
	private PessoaMagistradoManager pessoaMagistradoManager;
	
	@In
	private FacesMessages facesMessages;
	
	private boolean primeiraChamada = true;

	public boolean isPrimeiraChamada() {
		return primeiraChamada;
	}

	public void setPrimeiraChamada(boolean primeiraChamada) {
		this.primeiraChamada = primeiraChamada;
	}

	@In
	private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;

	@In
	private SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;
	
	@In
	private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;
		
	@Logger
	private Log logger;
	
	public String continuaSessao() {
		continuaSessaoDiv = !continuaSessaoDiv;
		return "";
	}

	public void setSessaoComposicaoOrdemIdSessaoComposicaoOrdem(Integer id) {
		setId(id);
	}

	public Integer getSessaoComposicaoOrdemIdSessaoComposicaoOrdem() {
		return (Integer) getId();
	}

	public static SessaoComposicaoOrdemHome instance() {
		return ComponentUtil.getComponent(SessaoComposicaoOrdemHome.NAME);
	}

	public Boolean countRevisor(Sessao obj) {
		StringBuilder sb = new StringBuilder("select count(o) from SessaoComposicaoOrdem o ");
		sb.append("where o.sessao = :sessao and o.orgaoJulgadorRevisor is not null ");
		sb.append("and o.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgadorAtual ");

		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("sessao", obj);
		query.setParameter("idOrgaoJulgadorAtual", Authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador());

		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno >= 1;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}	
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> listaComboRevisoresDetalheProcesso() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct (o.orgaoJulgador) from SessaoComposicaoOrdem o ");
		sb.append("where o.orgaoJulgador != :ojProcesso ");
		sb.append("and o.sessao = :sessaoSurgerida ");
		sb.append("or ");
		sb.append("  (o.orgaoJulgador in (select rev.orgaoJulgadorRevisor from RevisorProcessoTrf rev ");
		sb.append(" 					  	where rev.processoTrf = :processoTrf ");
		sb.append("   						and rev.dataFinal = null)) ");
		sb.append("or ");
		sb.append("  (o.orgaoJulgador in (select sco.orgaoJulgadorRevisor from SessaoComposicaoOrdem sco ");
		sb.append(" 					  	where sco.sessao = :sessaoSurgerida ");
		sb.append("   						and sco.orgaoJulgadorRevisor != :ojProcesso ");
		sb.append("							and sco.orgaoJulgador = :ojProcesso))");
		Query q = getEntityManager().createQuery(sb.toString());
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		q.setParameter("ojProcesso", processoTrf.getOrgaoJulgador());
		q.setParameter("sessaoSurgerida", processoTrf.getSessaoSugerida());
		q.setParameter("processoTrf", processoTrf);
		RevisorProcessoTrfHome.instance().defineOrgaoJulgadorRevisor(processoTrf);
		return q.getResultList();
	}

	public List<OrgaoJulgador> orgaoJulgadorLivresParaRevisar(SessaoComposicaoOrdem sco) {
		List<SessaoComposicaoOrdem> listaSco = sessaoComposicaoOrdemManager.obterComposicaoSessao(sco.getSessao().getIdSessao(), sco.getIdSessaoComposicaoOrdem());

		List<OrgaoJulgador> listaRevisor = new ArrayList<OrgaoJulgador>();

		for (SessaoComposicaoOrdem ojcoj : listaSco) {
			if (ojcoj.getOrgaoJulgadorRevisor() != null) {
				listaRevisor.add(ojcoj.getOrgaoJulgadorRevisor());
			}
		}

		// pega a lista de todos os oj da composição mesnos o selecionado
		List<OrgaoJulgador> listaOJlivre = new ArrayList<OrgaoJulgador>();
		for (SessaoComposicaoOrdem scoItem : listaSco) {
			listaOJlivre.add(scoItem.getOrgaoJulgador());
		}

		// retira da lista os oj ja em uso como revisor dos gabinetes da sessão
		for (OrgaoJulgador ojRev : listaRevisor) {
			if (listaOJlivre.contains(ojRev)) {
				listaOJlivre.remove(ojRev);
			}
		}
		// retorna lista de oj livres para serem revisor
		return listaOJlivre;
	}

	public void updateComposicaoOrgaoJulgadorCombo(SessaoComposicaoOrdem row) {
		setInstance(row);
		super.update();
		Contexts.removeFromAllContexts("sessaoComposicaoOrdemList");
		getEntityManager().clear();
	}

	/**
	 * Metodo que obtem os magistrados substitutos para uma determinada sessao e orgao julgador.
	 * 
	 * @param sco Composicao da sessao
	 * @return Lista de Magistrados substitutos.
	 */
	public List<PessoaMagistrado> getMagistradoList(SessaoComposicaoOrdem sco) {
		listaSubstituto = pessoaMagistradoManager.obterSubstitutos(sco.getSessao().getIdSessao(), sco.getOrgaoJulgador().getIdOrgaoJulgador());
		return listaSubstituto;
	}

	/**
	 * Recupera todos os magistrados ativos.
	 * @return Lista de magistrados ativos.
	 */
	public List<PessoaMagistrado> getMagistradoItems() {
		listaSubstituto = pessoaMagistradoManager.magistradoList();
		return listaSubstituto;
	}

	public Integer posicaoItemSubstituto(PessoaMagistrado row) {
		if (row != null) {
			int i = 0;
			for (i = 0; i < listaSubstituto.size(); i++) {
				if (listaSubstituto.get(i).getIdUsuario().equals(row.getIdUsuario()))
					return i;
			}
		}
		return null;
	}
	
	public void gravaSubstituto(SessaoComposicaoOrdem sessaoComposicaoOrdem) {
		try {
			getSessaoComposicaoOrdemManager().alterarMagistradoSubstituto(sessaoComposicaoOrdem);
			
			FacesMessages.instance().add(Severity.INFO, "O magistrado substituto foi alterado com sucesso!");
		} 
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro mensagem interna, " + e.getMessage());
		}
	}

	private void removePresidenteSessao() {
		StringBuilder sb = new StringBuilder("select o from SessaoComposicaoOrdem o where o.sessao = :sessao");
		sb.append(" and o.presidente = true ");

		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("sessao", SessaoHome.instance().getInstance());
		SessaoComposicaoOrdem sco = EntityUtil.getSingleResult(query);
		if (sco != null) {
			sco.setPresidente(Boolean.FALSE);
			getEntityManager().merge(sco);
			getEntityManager().flush();
		}
	}

	public void atualizaPresidente(SessaoComposicaoOrdem row) {
		removePresidenteSessao();
		row.setPresidente(true);
		row.setMagistradoTitularPresenteSessao(Boolean.TRUE);
		setInstance(row);
		super.update();
	}

	public void alterarIndicadorMagistradoTitularPresente(SessaoComposicaoOrdem sessaoComposicaoOrdem) {
		try {
			sessaoComposicaoOrdem.setMagistradoTitularPresenteSessao(!sessaoComposicaoOrdem.getMagistradoTitularPresenteSessao());
			getSessaoComposicaoOrdemManager().persistAndFlush(sessaoComposicaoOrdem);
			
			FacesMessages.instance().add(Severity.INFO, "A presença do magistrado titular foi alterada com sucesso!");
		} 
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro mensagem interna, " + e.getMessage());
		}
	}

	public void updateRevisor() {
		StringBuilder sb = new StringBuilder("select o from SessaoComposicaoOrdem o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("and o.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgadorAtual ");

		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("sessao", SessaoHome.instance().getInstance());
		query.setParameter("idOrgaoJulgadorAtual", Authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador());

		SessaoComposicaoOrdem sessao = (SessaoComposicaoOrdem) query.getSingleResult();

		sessao.setOrgaoJulgadorRevisor(instance.getOrgaoJulgadorRevisor());

		getEntityManager().merge(sessao);
		getEntityManager().flush();
	}

	public void setMagistradoSubstituto(PessoaMagistrado magistradoSubstituto) {
		this.magistradoSubstituto = magistradoSubstituto;
	}

	public PessoaMagistrado getMagistradoSubstituto() {
		return magistradoSubstituto;
	}

	public void setContinuaSessaoDiv(Boolean continuaSessaoDiv) {
		this.continuaSessaoDiv = continuaSessaoDiv;
	}

	public Boolean getContinuaSessaoDiv() {
		return continuaSessaoDiv;
	}

	public void setPresidente(Boolean presidente) {
		this.presidente = presidente;
	}

	public Boolean getPresidente() {
		return presidente;
	}

	public SessaoProcessoDocumentoVoto getSessaoVoto() {
		return sessaoVoto;
	}

	public void setSessaoVoto(SessaoProcessoDocumentoVoto sessaoVoto) {
		this.sessaoVoto = sessaoVoto;
	}

	public Boolean impedido(SessaoComposicaoOrdem obj, ProcessoTrf processoTrf) {
		buscaVoto(obj, processoTrf);
		if (sessaoVoto != null) {
			return sessaoVoto.getImpedimentoSuspeicao();
		}
		return Boolean.FALSE;
	}

	public SessaoProcessoDocumentoVoto buscaVoto(SessaoComposicaoOrdem sco, ProcessoTrf processoTrf) {
		sessaoVoto = sessaoProcessoDocumentoVotoManager.recuperarVoto(sco.getSessao(), processoTrf, sco.getOrgaoJulgador());
		if(sessaoVoto != null){
		EntityUtil.getEntityManager().refresh(sessaoVoto);
		}
		return sessaoVoto;
	}

	public void setOrgaoJulgadorColegiadoRevisor(OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoRevisor) {
		this.orgaoJulgadorColegiadoRevisor = orgaoJulgadorColegiadoRevisor;
	}

	public OrgaoJulgadorColegiadoOrgaoJulgador getOrgaoJulgadorColegiadoRevisor() {
		return orgaoJulgadorColegiadoRevisor;
	}

	@SuppressWarnings("unchecked")
	public List<PessoaMagistrado> getMagistrados(OrgaoJulgador orgaoJulgador) {
		StringBuilder sb = new StringBuilder(
				"select distinct o.usuarioLocalizacao.usuario from UsuarioLocalizacaoMagistradoServidor o ");
		sb.append("where o.orgaoJulgadorCargo.orgaoJulgador = :orgaoJulgador ");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("orgaoJulgador", orgaoJulgador);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public String getMagistrado(int id) {
		StringBuilder sb = new StringBuilder("select o.magistradoPresenteSessao.nome from SessaoComposicaoOrdem o ");
		sb.append("where o.idSessaoComposicaoOrdem = :idSCO ");
		sb.append("and o.sessao = :sessao ");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("sessao", SessaoHome.instance().getInstance()).setParameter("idSCO", id);
		
		List<String> nomeMagistradoList = query.getResultList();
		
		if (nomeMagistradoList.size() >= 1) {
			return nomeMagistradoList.get(0);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<PessoaMagistrado> getMagistradosPresentesSessao(OrgaoJulgador orgaoJulgador) {
		StringBuilder sb = new StringBuilder("select o.magistradoPresenteSessao from SessaoComposicaoOrdem o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("and o.orgaoJulgador = :orgaoJulgador ");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("sessao", SessaoHome.instance().getInstance());
		query.setParameter("orgaoJulgador", orgaoJulgador);
		
		List<PessoaMagistrado> pessoaMagistradoList = query.getResultList();
		
		if (pessoaMagistradoList.size() >= 1) {
			return pessoaMagistradoList;
		} else {
			return null;
		}
	}

	public void gravarMagistrado(SessaoComposicaoOrdem sessaoComposicaoOrdem) {
		try {
			getSessaoComposicaoOrdemManager().alterarMagistradoPresente(sessaoComposicaoOrdem);
			
			FacesMessages.instance().add(Severity.INFO, "O magistrado presente foi alterado com sucesso!");
		} 
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro mensagem interna, " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> orgaoJulgadorList(SessaoComposicaoOrdem row) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoComposicaoOrdem o where ");
		sb.append("o.sessao.idSessao = :sessao ");
		sb.append("and o.idSessaoComposicaoOrdem != :row ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", row.getSessao().getIdSessao());
		q.setParameter("row", row.getIdSessaoComposicaoOrdem());

		List<SessaoComposicaoOrdem> listOJCOJ = q.getResultList();

		List<OrgaoJulgador> listaRevisor = new ArrayList<OrgaoJulgador>();
		for (SessaoComposicaoOrdem ojcoj : listOJCOJ) {
			if (ojcoj.getOrgaoJulgadorRevisor() != null) {
				listaRevisor.add(ojcoj.getOrgaoJulgadorRevisor());
			}
		}
		int i = 0;
		while (i < listOJCOJ.size()) {
			if (listaRevisor.contains(listOJCOJ.get(i).getOrgaoJulgador())) {
				listOJCOJ.remove(listOJCOJ.get(i));
				i = -1;
			}
			i++;
		}

		List<OrgaoJulgador> listaOJCOJ = new ArrayList<OrgaoJulgador>();
		for (SessaoComposicaoOrdem ojcoj : listOJCOJ) {
			listaOJCOJ.add(ojcoj.getOrgaoJulgador());
		}

		return listaOJCOJ;
	}

	public List<PessoaMagistrado> getListaMagistradosDisponiveis() {
		return listaMagistradosDisponiveis;
	}

	public void setListaMagistradosDisponiveis(List<PessoaMagistrado> listaMagistradosDisponiveis) {
		this.listaMagistradosDisponiveis = listaMagistradosDisponiveis;
	}

	public SessaoComposicaoOrdemManager getSessaoComposicaoOrdemManager() {
		return sessaoComposicaoOrdemManager;
	}	
	public void removerVotoSecretario(SessaoPautaProcessoComposicao sessaoPautaProcessoComposicao, Integer sessaoPauta){
		
		if(sessaoPauta != null){
			try {
				if (sessaoPautaProcessoComposicao != null) {
					SessaoProcessoDocumentoVoto spdv = getVotoProprio(sessaoPautaProcessoComposicao.getOrgaoJulgador(), sessaoPauta);
					if(spdv != null){
						if(spdv.getProcessoDocumento() != null){
							ProcessoDocumento pd = spdv.getProcessoDocumento();
							if(pd.getDataJuntada() != null){
								spdv.setProcessoDocumento(null);
							}
						}
						List<SessaoProcessoDocumentoVoto> votosAcompanhantes = sessaoProcessoDocumentoVotoManager.getVotosAcompanhantes(spdv, spdv.getOrgaoJulgador());
						for(SessaoProcessoDocumentoVoto vot : votosAcompanhantes){
							vot.setOjAcompanhado(vot.getOrgaoJulgador());
							sessaoProcessoDocumentoVotoManager.persist(vot);
						}
						sessaoProcessoDocumentoVotoManager.remove(spdv);
						sessaoProcessoDocumentoVotoManager.flush();		
						
						OrgaoJulgador ojMaioria = sessaoProcessoDocumentoVotoManager.contagemMaioriaVotacao(sessaoPautaProcessoComposicao.getSessaoPautaProcessoTrf().getSessao(), sessaoPautaProcessoComposicao.getSessaoPautaProcessoTrf().getProcessoTrf());
						sessaoPautaProcessoComposicao.getSessaoPautaProcessoTrf().setOrgaoJulgadorVencedor(ojMaioria != null ? ojMaioria : sessaoPautaProcessoComposicao.getSessaoPautaProcessoTrf().getProcessoTrf().getOrgaoJulgador());
		 				sessaoPautaProcessoTrfManager.alterar(sessaoPautaProcessoComposicao.getSessaoPautaProcessoTrf());					}
				}
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
				logger.error("Erro ao remover voto: {0}", e.getLocalizedMessage());
			} catch (Exception e) {
				facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
				logger.error("Erro ao alterar voto vencedor: {0}", e.getLocalizedMessage());
			}
			
		}else{
			logger.error("Erro ao remover voto: identificador do julgamento não encontrado.");
			facesMessages.add(Severity.ERROR, "Erro ao remover voto: identificador do julgamento não encontrado.");
		}
	}
	
	public SessaoProcessoDocumentoVoto getVotoProprio(OrgaoJulgador oj, Integer idJulg){
		try {
			SessaoPautaProcessoTrf julg = sessaoPautaProcessoTrfManager.findById(idJulg);
			return getVoto(julg.getSessao(), julg.getProcessoTrf(), oj);
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível recuperar o voto");
			e.printStackTrace();
			return null;
		}
	}
	
	private SessaoProcessoDocumentoVoto getVoto(Sessao sessao, ProcessoTrf processo, OrgaoJulgador julgador){
		return sessaoProcessoDocumentoVotoManager.recuperarVoto(sessao, processo, julgador);
	}
	
}
