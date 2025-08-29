package br.com.infox.trf.distribuicao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ParametroHome;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

@Name("testeDistribuicao")
@BypassInterceptors
public class TesteDistribuicao {

	private static Integer ID_PROCESSO_BASE;
	private String numeroProcesso;
	private StringBuilder sb = new StringBuilder();
	private Map<VaraTitulacao, Integer> mapDistribuidos = new HashMap<VaraTitulacao, Integer>();
	private ProcessoTrf processoTrfBase;
	private StringBuilder sbDistribuidos = new StringBuilder();
	private int quantidade = 1;

	private void executarTeste() throws Exception {

		ProcessoTrfHome home = ProcessoTrfHome.instance();
		EntityManager em = EntityUtil.getEntityManager();
		ProcessoTrf processoBaseTeste = getProcessoBaseTeste();
		ClasseJudicial classeJudicial = processoBaseTeste.getClasseJudicial();
		Jurisdicao jurisdicao = processoBaseTeste.getJurisdicao();

		home.newInstance();
		ProcessoTrf processoTrf = home.getInstance();
		processoTrf.setClasseJudicial(classeJudicial);
		processoTrf.setJurisdicao(jurisdicao);
		processoTrf.setProcessoStatus(ProcessoStatusEnum.V);
		processoTrf.setDataAutuacao(new Date());

		ProcessoHome procHome = ProcessoHome.instance();
		Processo processo = procHome.criarProcesso();
		home.persistProcessClient(processo);

		String numeroOrgaoJustica = ParametroHome.getFromContext("numeroOrgaoJustica", true);
		Integer numeroOrigem = processoTrf.getJurisdicao().getNumeroOrigem();
		NumeroProcessoUtil.numerarProcesso(processoTrf, Integer.parseInt(numeroOrgaoJustica), numeroOrigem);
		AssuntoTrf assuntoTrf = processoBaseTeste.getAssuntoTrfList().get(0);
		processoTrf.getAssuntoTrfList().add(assuntoTrf);
		em.merge(processoTrf);
		try {
			em.flush();
		} catch (org.hibernate.AssertionFailure e) {
		}
		List<ProcessoParte> processoParteList = processoBaseTeste.getProcessoParteList();
		for (ProcessoParte processoParte : processoParteList) {
			ProcessoParte novo = EntityUtil.cloneEntity(processoParte, false);
			novo.setProcessoTrf(processoTrf);
			processoTrf.getProcessoParteList().add(novo);
		}
		em.merge(processoTrf);
		try {
			em.flush();
		} catch (org.hibernate.AssertionFailure e) {
		}

		criaCopiaProcessoDocumento(processoTrf);

//		Fluxo fluxo = processoTrf.getClasseJudicial().getFluxo();
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			ProcessoTrfHome.instance().inserirProcessoNoFluxo(processoTrf.getIdProcessoTrf(),
					ParametroUtil.instance().getFluxoDistribuicao());
		}
		System.out.println(processoTrf.getNumeroProcesso());
		if (sbDistribuidos.length() > 0) {
			sbDistribuidos.append(", ");
		}
		sbDistribuidos.append(processoTrf.getNumeroProcesso());
	}

	public void executarTesteDistribuicao() {
		sb = new StringBuilder();
		mapDistribuidos = new HashMap<VaraTitulacao, Integer>();
		for (int i = 0; i < quantidade; i++) {
			try {
				executarTeste();
			} catch (Exception e) {
				e.printStackTrace();
				FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			}
			if (i % 20 == 0) {
				EntityUtil.getEntityManager().clear();
			}
		}
	}

	private ProcessoTrf getProcessoBaseTeste() {
		if (processoTrfBase == null && numeroProcesso != null) {
			EntityManager em = EntityUtil.getEntityManager();
			try {
				ID_PROCESSO_BASE = Integer.parseInt(numeroProcesso);
				processoTrfBase = em.find(ProcessoTrf.class, ID_PROCESSO_BASE);
			} catch (Exception e) {
				Query q = em.createQuery("select o from ProcessoTrf o where o.processo.numeroProcesso = ?");
				q.setParameter(1, numeroProcesso);
				processoTrfBase = EntityUtil.getSingleResult(q);
				if (processoTrfBase != null) {
					ID_PROCESSO_BASE = processoTrfBase.getIdProcessoTrf();
				}
			}
		}
		return processoTrfBase;
	}

	@SuppressWarnings("unchecked")
	public List getEstatisticaDistribuidos() throws Exception {
		// ** ProcessoTrf processoTrf = getProcessoBaseTeste();
		// ** return processoTrf != null ?
		// DistribuicaoHome.obterListaParaSorteio(processoTrf, null) :
		// Collections.EMPTY_LIST;
		return null;
	}

	public ClasseJudicial getClasseTeste() {
		return getProcessoBaseTeste().getClasseJudicial();
	}

	public int getQuantidadeProcesso(VaraTitulacao varaTitulacao, boolean total) {
		return DistribuicaoHome.getQuantidadeProcesso(varaTitulacao.getOrgaoJulgador(),
				total ? null : varaTitulacao.getCargo(), total ? null : getProcessoBaseTeste().getClasseJudicial());
	}

	public int getQuantidadeProcessoTotalTitulacao(VaraTitulacao varaTitulacao) {
		return DistribuicaoHome.getQuantidadeProcesso(varaTitulacao.getOrgaoJulgador(), varaTitulacao.getCargo(), null);
	}

	public String getLog() {
		sb = new StringBuilder();
		Set<VaraTitulacao> keySet = mapDistribuidos.keySet();
		for (VaraTitulacao varaTitulacao : keySet) {
			Integer quantidade = mapDistribuidos.get(varaTitulacao);
			sb.append(String.format("Distribuidos %s processos para vara/titulacao " + varaTitulacao,
					quantidade.toString()));
			sb.append("<br/>");
		}
		return sb.toString();
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public String getNumerosDistribuidos() {
		return sbDistribuidos.toString();
	}

	public int getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(int quantidade) {
		this.quantidade = quantidade;
	}

	private void criaCopiaProcessoDocumento(ProcessoTrf processoTrf) {
		EntityManager em = EntityUtil.getEntityManager();

		String hql = "select o from ProcessoDocumento o where " + "o.processo = :processo order by o.dataInclusao";

		Query query = em.createQuery(hql);
		query.setParameter("processo", getProcessoBaseTeste().getProcesso());

		ProcessoDocumento pdBase = EntityUtil.getSingleResult(query);
		if (pdBase == null) {
			return;
		}

		ProcessoDocumentoBin pdBinBase = pdBase.getProcessoDocumentoBin();

		try {
			ProcessoDocumento pd = (ProcessoDocumento) EntityUtil.cloneObject(pdBase, false);
			ProcessoDocumentoBin pdBin = (ProcessoDocumentoBin) EntityUtil.cloneObject(pdBinBase, false);
			pd.setIdProcessoDocumento(0);
			pdBin.setIdProcessoDocumentoBin(0);

			em.persist(pdBin);

			pd.setProcessoDocumentoBin(pdBin);
			pd.setProcesso(processoTrf.getProcesso());
			em.persist(pd);

			if (pdBinBase.getSignature() != null) {
				ProcessoDocumentoBinPessoaAssinatura assinatura = new ProcessoDocumentoBinPessoaAssinatura();
				assinatura.setAssinatura(pdBinBase.getSignature());
				assinatura.setCertChain(pdBinBase.getCertChain());
				assinatura.setDataAssinatura(new Date());
				Pessoa pessoaLogada = Authenticator.getPessoaLogada();
				assinatura.setNomePessoa(pessoaLogada.getNome());
				assinatura.setPessoa(pessoaLogada);
				assinatura.setProcessoDocumentoBin(pdBin);
				em.persist(assinatura);
				// define o papel do documento como o do responssavel pela
				// assinatura
				for (ProcessoDocumento pdb : pdBin.getProcessoDocumentoList()) {
					pdb.setPapel(Authenticator.getPapelAtual());
					EntityUtil.getEntityManager().merge(pdb);
					EntityUtil.getEntityManager().flush();
				}
			}

			em.flush();

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

	}

}