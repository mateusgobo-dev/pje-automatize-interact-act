package br.com.infox.cliente.home;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.bean.ConsultaProcesso;
import br.com.infox.cliente.component.suggest.ProcessoAdvogadoProcSuggestBean;
import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.component.tree.JurisdicaoTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.ProcessoTrfInicialAdvogadoList;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcuradorAssuntoTrf;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcuradorClasseJudicial;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name(CaixaAdvogadoProcuradorHome.NAME)
@BypassInterceptors
public class CaixaAdvogadoProcuradorHome extends AbstractHome<CaixaAdvogadoProcurador> {

	public static final String NAME = "caixaAdvogadoProcuradorHome";
	private static final long serialVersionUID = 1L;
	private boolean cpf = false;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private ProcessoTrf numeroProcesso;
	private String exibirInformacao;

	public static CaixaAdvogadoProcuradorHome instance() {
		return ComponentUtil.getComponent(CaixaAdvogadoProcuradorHome.NAME);
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	@Override
	public void setInstance(CaixaAdvogadoProcurador instance) {
		super.setInstance(instance);
		if (instance != null && !Strings.isEmpty(instance.getNumeroCpfCnpjParte())) {
			if (!instance.getNumeroCpfCnpjParte().contains("/")) {
				cpf = true;
			}
		}
	}

	public void clearCpfCnpj() {
		getInstance().setNumeroCpfCnpjParte(null);
	}

	private void clearTree() {
		JurisdicaoTreeHandler tree = getComponent(JurisdicaoTreeHandler.NAME);
		tree.clearTree();
	}

	public void removeCaixa(int id) {
		CaixaAdvogadoProcurador caixa = EntityUtil.find(CaixaAdvogadoProcurador.class, id);
		if (caixa != null) {
			removerProcessosCaixa(caixa);
			EntityUtil.getEntityManager().remove(caixa);
			EntityUtil.flush();
			clearTree();
		} else {
			FacesMessages.instance().add(Severity.ERROR, "Clique em uma caixa para excluir.");
		}
	}

	private void removerProcessosCaixa(CaixaAdvogadoProcurador caixa) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from tb_processo_caixa_adv_proc pcap ");
		sb.append("where pcap.id_caixa_adv_proc = :idCaixa");
		EntityUtil.createNativeQuery(getEntityManager(), sb)
				.setParameter("idCaixa", caixa.getIdCaixaAdvogadoProcurador()).executeUpdate();
	}

	public void addCaixa() {
		
		getInstance().setJurisdicao(PainelUsuarioAdvogadoHome.instance().getJurisdicao());
		getInstance().setLocalizacao(Authenticator.getLocalizacaoAtual());
		ConsultaProcesso cp = ConsultaProcessoHome.instance().getInstance();
		if (cp.getInPesquisa()) {
			getInstance().setNumeroDigitoVerificador(cp.getNumeroDigitoVerificador());
			getInstance().setAno(cp.getAno());
			getInstance().setNumeroOrigemProcesso(cp.getNumeroOrigemProcesso());
			getInstance().setNumeroSequencia(cp.getNumeroSequencia());
			getInstance().setNomeParte(cp.getNomeParte());
			if (cp.getNumeroCNPJ() != null && !Strings.isEmpty(cp.getNumeroCNPJ())) {
				getInstance().setNumeroCpfCnpjParte(cp.getNumeroCNPJ());
			} else if (cp.getNumeroCPF() != null && Strings.isEmpty(cp.getNumeroCPF())) {
				getInstance().setNumeroCpfCnpjParte(cp.getNumeroCPF());
			}
			getInstance().setNascimentoInicialParte(cp.getNascimentoInicialParte());
			getInstance().setNascimentoFinalParte(cp.getNascimentoFinalParte());
			getInstance().setUfOABParte(cp.getUfOABParte());
			getInstance().setLetraOABParte(cp.getLetraOABParte());
			getInstance().setNumeroOABParte(cp.getNumeroOABParte());
			getInstance().getClasseJudicialList().add(cp.getClasseJudicial());
			getInstance().getAssuntoTrfList().add(cp.getAssuntoTrf());
			persist();
			PainelUsuarioAdvogadoHome.instance().addProcessosGridEmCaixa(getInstance());
		} else {
			persist();
		}
		newInstance();

		clearTree();
	}

	public void setCpf(boolean cpf) {
		this.cpf = cpf;
	}

	public boolean getCpf() {
		return cpf;
	}

	public boolean verificarCaixaClasse(ClasseJudicial classe, CaixaAdvogadoProcurador caixa) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from CaixaAdvogadoProcuradorClasseJudicial o ");
		sb.append("where o.classeJudicial = :classe ");
		sb.append("and o.caixaAdvogadoProcurador = :caixa");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("classe", classe);
		q.setParameter("caixa", caixa);
		long quantidade = (Long) q.getSingleResult();
		return quantidade > 0;
	}

	public void gravarClasses() {
		ClasseJudicialTreeHandler tree = ComponentUtil.getComponent("classeJudicialTree");
		for (ClasseJudicial classe : tree.getSelectedTree()) {
			if (!verificarCaixaClasse(classe, getInstance())) {
				CaixaAdvogadoProcuradorClasseJudicial caixaClasse = new CaixaAdvogadoProcuradorClasseJudicial();
				caixaClasse.setClasseJudicial(classe);
				caixaClasse.setCaixaAdvogadoProcurador(getInstance());
				getEntityManager().persist(caixaClasse);
				EntityUtil.flush();
			}
		}
		tree.clearTree();
	}

	public boolean verificarCaixaAssunto(AssuntoTrf assuntoTrf, CaixaAdvogadoProcurador caixa) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from CaixaAdvogadoProcuradorAssuntoTrf o ");
		sb.append("where o.assuntoTrf = :assuntoTrf ");
		sb.append("and o.caixaAdvogadoProcurador = :caixa");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("assuntoTrf", assuntoTrf);
		q.setParameter("caixa", caixa);
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public void gravarAssuntos() {
		AssuntoTrfTreeHandler tree = ComponentUtil.getComponent("assuntoTrfTree");
		for (AssuntoTrf assuntoTrf : tree.getSelectedTree()) {
			if (!verificarCaixaAssunto(assuntoTrf, getInstance())) {
				CaixaAdvogadoProcuradorAssuntoTrf caixaAssunto = new CaixaAdvogadoProcuradorAssuntoTrf();
				caixaAssunto.setAssuntoTrf(assuntoTrf);
				caixaAssunto.setCaixaAdvogadoProcurador(getInstance());
				getEntityManager().persist(caixaAssunto);
				EntityUtil.flush();
			}
		}
		tree.clearTree();
	}

	
	@Override
	public String update() {
		if (!validarIntervaloProcesso(getInstance().getIntervaloNumeroProcesso())){
			return "";
		}
		
		anularCamposVazios();
		atualizarIntervalosDataFinalInclusivo();
		
		String update = super.update();
		gravarAssuntos();
		gravarClasses();
		return update;
	}
	
	
	
	/**
	 * Verifica a existência de campos vazios ("") 
	 * e troca por <code>null<code>
	 */
	private void anularCamposVazios(){
		CaixaAdvogadoProcurador caixa = getInstance(); 
		
		if (caixa.getNumeroCpfCnpjParte() != null){
			/* remove a máscara do cpf/cnpj */
			String cpfCnpjSemMascara = StringUtil.removeNaoAlphaNumericos(caixa.getNumeroCpfCnpjParte().trim());
			
			if (cpfCnpjSemMascara.equals("")){
				caixa.setNumeroCpfCnpjParte(null);
			}	
		}
		
	}
	

	/**
	 * Atualiza campos compostos de intervalos de data
	 * para que a data final compreenda até as 23:59
	 */
	private void atualizarIntervalosDataFinalInclusivo(){
		CaixaAdvogadoProcurador caixa = getInstance();
		if (caixa.getDataDistribuicaoFinal() != null){
			Date novaData = DateUtil.getEndOfDay(caixa.getDataDistribuicaoFinal());
			caixa.setDataDistribuicaoFinal(novaData);
		}
		
		if (caixa.getNascimentoFinalParte() != null){
			Date novaData = DateUtil.getEndOfDay(caixa.getNascimentoFinalParte());
			caixa.setNascimentoFinalParte(novaData);
		}
	}
	
	
	private Boolean validarIntervaloProcesso(String intervaloProcesso){
		if(intervaloProcesso != null && !intervaloProcesso.trim().equals("")){
			String mensagemErro = "Padrão inválido para o intervalo de número do processo!";
			if(intervaloProcesso.startsWith(";") || intervaloProcesso.endsWith(";")){
				FacesMessages.instance().add(Severity.ERROR, mensagemErro);
				return false;
			}
			String[] strList = intervaloProcesso.split(";");
			for (String string : strList) {
				if(string.length() > 1 && !string.contains("-")){
					FacesMessages.instance().add(Severity.ERROR, mensagemErro);
					return false;
				}else if(string.contains("-")){
					String[] strList2 = string.split("-");
					if(strList2.length != 2){
						FacesMessages.instance().add(Severity.ERROR, mensagemErro);
						return false;
					}
					for (String string2 : strList2) {
						if(string2.length() != 7){
							FacesMessages.instance().add(Severity.ERROR, mensagemErro);
							return false;
						}
					}
				}
			}

			for (String string : strList) {
				String [] strList2 = string.split("-");
				for (String string2 : strList2) {
					try{
						Long.parseLong(string2);
					}catch (Exception e) {
						FacesMessages.instance().add(Severity.ERROR, mensagemErro);
						return false;
					}
				}
			}
		}
		
		return true;
	}
	

	@Override
	public String persist() {
		String persist = super.persist();
		gravarAssuntos();
		gravarClasses();
		return persist;
	}

	public void removeCaixaAssunto(CaixaAdvogadoProcuradorAssuntoTrf obj) {
		getEntityManager().remove(obj);
		EntityUtil.flush();
	}

	public void removeCaixaClasse(CaixaAdvogadoProcuradorClasseJudicial obj) {
		getEntityManager().remove(obj);
		EntityUtil.flush();
	}

	public void localizarCaixa() {
		if (getNumeroProcesso() == null) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o ");
		sb.append("where o.processo.numeroProcesso = :numeroProcesso ");
		if (ProcessoTrfInicialAdvogadoList.instance().getCaixaPedente()) {
			sb.append(getFiltroPendentes());
		}
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("numeroProcesso", getNumeroProcesso().getNumeroProcesso());

		ProcessoTrf proc = EntityUtil.getSingleResult(q);
		String informacao = "";
		if (proc != null) {
			if (proc.getJurisdicao() != null) {
				informacao = proc.getJurisdicao().getJurisdicao();
			}
			
			sb = new StringBuilder();
			sb.append("Select o from ProcessoCaixaAdvogadoProcurador o ");
			sb.append("where o.processoTrf.idProcessoTrf = :processoTrf ");
			sb.append("and o.caixaAdvogadoProcurador.localizacao = :localizacao ");
			q = getEntityManager().createQuery(sb.toString());
			q.setParameter("processoTrf", proc.getIdProcessoTrf());
			q.setParameter("localizacao", Authenticator.getLocalizacaoAtual());
			
			q.setFirstResult(0);
			q.setMaxResults(1);
			ProcessoCaixaAdvogadoProcurador caixa = null;
			try {
				caixa = (ProcessoCaixaAdvogadoProcurador) q.getSingleResult();
			} catch (NoResultException e) {
			}
			
			if (caixa != null) {
				informacao += " / " + caixa.getCaixaAdvogadoProcurador().getNomeCaixaAdvogadoProcurador();
			}
			setExibirInformacao(informacao);
		} else {
			setExibirInformacao("Processo não encontrado!");
		}
	}

	public ProcessoTrf getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(ProcessoTrf numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public void setExibirInformacao(String exibirInformacao) {
		this.exibirInformacao = exibirInformacao;
	}

	public String getExibirInformacao() {
		return exibirInformacao;
	}

	public void setarNumeroProcesso(ProcessoTrf processo) {
		setNumeroProcesso(processo);
		getProcessoAdvogadoProcSuggestBean().setInstance(getNumeroProcesso());
	}

	private ProcessoAdvogadoProcSuggestBean getProcessoAdvogadoProcSuggestBean() {
		return getComponent("processoAdvogadoProcSuggest");
	}

	public void limparPesquisa() {
		setExibirInformacao(null);
		setNumeroProcesso(null);
		getProcessoAdvogadoProcSuggestBean().setInstance(null);
	}

	public String getFiltroPendentes() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("and o.idProcessoTrf in (select ppe.processoJudicial.idProcessoTrf from ProcessoParteExpediente ppe ");
//		sb.append("where ppe.dtCienciaParte is not null and ");
//		sb.append("ppe.pendenteManifestacao = true and ");
//		sb.append("ppe.pessoaParte in (#{pessoaAdvogadoHome.pessoaAdvogadoProcurador})) ");
//		return sb.toString();
		String SQL_NAO_RESPONDIDO = "AND o.idProcessoTrf IN (SELECT DISTINCT proc.idProcessoTrf FROM ProcessoParteExpediente AS ppe " +
				"	INNER JOIN ppe.processoJudicial proc " +
				"	INNER JOIN proc.processoParteList parte " +
				"	LEFT OUTER JOIN parte.processoParteRepresentanteList rep LEFT JOIN rep.representante pes WITH pes.idUsuario = #{usuarioLogado.idUsuario} " +
				"	WHERE proc.processoStatus = 'D' AND ppe.tipoPrazo NOT IN ('S', 'C') " +
				"	AND ppe.resposta IS NULL AND (ppe.fechado = false AND ppe.dtCienciaParte IS NOT NULL) AND ppe.dtPrazoLegal >= current_date " +
				"	AND (" +
				"		(parte.processoParteRepresentanteList IS NOT EMPTY " +
				"			AND ppe.pessoaParte = parte.pessoa " +
				"			AND rep.representante.idUsuario = #{usuarioLogado.idUsuario}) " +
				"		OR " +
				"			ppe.pessoaParte IN (#{pessoaService.getRepresentados(usuarioLogado)})" +
				"	)) ";
		return SQL_NAO_RESPONDIDO;
	}

	public void mudarCaixa(boolean pendente) {
		limparPesquisa();
		ProcessoTrfInicialAdvogadoList.instance().setCaixaPedente(pendente);
	}

	/**
	 * Método que retorna a quantidade de processos em caixa de uma jurisdição.
	 * 
	 * @param idJurisdicao
	 * @return
	 */
	public Integer getQtdEmCaixa(int idJurisdicao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoCaixaAdvogadoProcurador o ");
		sb.append("where o.caixaAdvogadoProcurador.jurisdicao.idJurisdicao = :jurisdicao ");
		sb.append("and o.caixaAdvogadoProcurador.localizacao = :localizacao ");
		sb.append("and (o.processoTrf.segredoJustica = false or ");
		sb.append("		o.processoTrf.segredoJustica is null or ");
		sb.append("		o.processoTrf in (select p.processo from ProcessoVisibilidadeSegredo p ");
		sb.append("						  where p.pessoa = :pessoaLogada))");
		if (ProcessoTrfInicialAdvogadoList.instance().getCaixaPedente()) {
			sb.append("and o.processoTrf.idProcessoTrf in (select ppe.processoJudicial.idProcessoTrf from ProcessoParteExpediente ppe ");
			sb.append("where ppe.dtCienciaParte is not null and ");
			sb.append("ppe.pendenteManifestacao = true and ");
			sb.append("ppe.pessoaParte in (#{pessoaAdvogadoHome.pessoaAdvogadoProcurador})) ");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("jurisdicao", idJurisdicao);
		q.setParameter("localizacao", Authenticator.getLocalizacaoAtual());
		q.setParameter("pessoaLogada", Authenticator.getPessoaLogada());
		return q.getResultList().size();

	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorList() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador o ");
		sb.append("where o.orgaoJulgador.ativo = true ");
		if(getInstance().getOrgaoJulgadorColegiado() != null){
			sb.append("and o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		if(getInstance().getOrgaoJulgadorColegiado() != null){
			q.setParameter("orgaoJulgadorColegiado", getInstance().getOrgaoJulgadorColegiado());
		}
		return (List<OrgaoJulgador>) q.getResultList();

	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoList() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorColegiado o ");
		sb.append("where o.ativo = true ");
		Query q = getEntityManager().createQuery(sb.toString());
		return (List<OrgaoJulgadorColegiado>) q.getResultList();

	}	
}
