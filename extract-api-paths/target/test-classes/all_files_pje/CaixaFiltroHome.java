package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.bean.ConsultaProcesso;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.TarefaTree;
import br.com.infox.ibpm.component.tree.TarefasTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.CaixaHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.CaixaFiltro;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Tarefa;

@Name(CaixaFiltroHome.NAME)
@BypassInterceptors
public class CaixaFiltroHome extends AbstractCaixaFiltroHome<CaixaFiltro> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "caixaFiltroHome";
	private String nomeNovaCaixa;
	private boolean cpf;
	
	public static final String REGEX_PADRAO_INTERVALO = "((([0-9]{1,7}-[0-9]{1,7};)|([0-9]{1,7};))*(([0-9]{1,7}-[0-9]{1,7})|([0-9]{1,7}));?)";

	public static CaixaFiltroHome instance() {
		return ComponentUtil.getComponent(CaixaFiltroHome.NAME);
	}

	public void verificarCaixa() {
		String s = "select o from CaixaFiltro o where o.idCaixa = :caixa";
		Query q = getEntityManager().createQuery(s);
		q.setParameter("caixa", CaixaHome.instance().getInstance().getIdCaixa());
		instance = (CaixaFiltro) EntityUtil.getSingleResult(q);
	}

	public boolean verificaRegistroDuplicado() {
		String s = "select o from CaixaFiltro o where o.orgaoJulgador = :orgaoJulgador and lower(to_ascii(o.nomeCaixa)) = lower(to_ascii(:nomeCaixa)) and o.tarefa = :tarefa";
		Query q = getEntityManager().createQuery(s);
		q.setParameter("orgaoJulgador", getInstance().getOrgaoJulgador());
		q.setParameter("nomeCaixa", getInstance().getNomeCaixa());
		q.setParameter("tarefa", getInstance().getTarefa());

		CaixaFiltro c1 = (CaixaFiltro) EntityUtil.getSingleResult(q);

		return c1 != null;
	}

	@Override
	public void newInstance() {
		nomeNovaCaixa = null;
		super.newInstance();
	}
	
	@Override
	public CaixaFiltro getInstance() {
		CaixaFiltro caixaFiltro = super.getInstance();
		
		Integer numeroIdentificacaoOrgaoJustica = caixaFiltro.getNumeroIdentificacaoOrgaoJustica();
		if (numeroIdentificacaoOrgaoJustica == null) {
			String numeroOrgaoJustica = ParametroUtil.getParametro("numeroOrgaoJustica");
			if (numeroOrgaoJustica != null) {
				try {
					caixaFiltro.setNumeroIdentificacaoOrgaoJustica(Integer.parseInt(numeroOrgaoJustica));
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		return caixaFiltro;
	}

	/**
	 * Cria uma caixa com o nome informado na tarefa informada
	 * @param idTarefa
	 * @param nomeNovaCaixa
	 */
	public void addCaixa(int idTarefa, String nomeNovaCaixa) {
		this.setNomeNovaCaixa(nomeNovaCaixa);
		addCaixa(idTarefa);
	}
	
	public CaixaFiltro addCaixaComRetorno(int idTarefa, String nomeNovaCaixa) {
		this.setNomeNovaCaixa(nomeNovaCaixa);
		return addCaixaComRetorno(idTarefa);
	}
	
	public void addCaixa(int idTarefa) {
		if(idTarefa > 0){
			getInstance().setNomeCaixa(nomeNovaCaixa);
			getInstance().setTarefa(getEntityManager().find(Tarefa.class, idTarefa));
			getInstance().setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
			getInstance().setOrgaoJulgadorColegiado(Authenticator.getOrgaoJulgadorColegiadoAtual());
			if (verificaRegistroDuplicado()) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado");
				newInstance();
				return;
			}
			persist();
			newInstance();
			TarefasTreeHandler tree = getComponent("tarefasTree");
			tree.clearTree();
			//Recarregar o no da tarefa(pai) e seus filhos(caixas)
			TarefaTree.adicionarIdTarefa(idTarefa);
			getEntityManager().clear();
		}
	}
	
	public CaixaFiltro addCaixaComRetorno(Integer idTask, String nomeCaixa,
			OrgaoJulgadorColegiado ojc) {
		this.setNomeNovaCaixa(nomeCaixa);
		return addCaixaComRetorno(idTask, getEntityManager().find(OrgaoJulgadorColegiado.class, ojc.getIdOrgaoJulgadorColegiado()), null);
	}

	public CaixaFiltro addCaixaComRetorno(int idTarefa) {
		return addCaixaComRetorno(idTarefa, Authenticator.getOrgaoJulgadorColegiadoAtual(), Authenticator.getOrgaoJulgadorAtual());
	}
	
	public CaixaFiltro addCaixaComRetorno(int idTarefa, OrgaoJulgadorColegiado ojc, OrgaoJulgador oj) {
		CaixaFiltro retorno = null;
		if(idTarefa > 0){
			getInstance().setNomeCaixa(nomeNovaCaixa);
			getInstance().setTarefa(getEntityManager().find(Tarefa.class, idTarefa));
			getInstance().setOrgaoJulgador(oj);
			getInstance().setOrgaoJulgadorColegiado(ojc);
			if (verificaRegistroDuplicado()) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado");
				newInstance();
				return null;
			}
			ConsultaProcesso cp = ConsultaProcessoHome.instance().getInstance();
			if (cp.getInPesquisa()) {
				getInstance().setClasseJudicial(cp.getClasseJudicial());
				getInstance().setAssuntoTrf(cp.getAssuntoTrf());
				getInstance().setNumeroDigitoVerificador(cp.getNumeroDigitoVerificador());
				getInstance().setAno(cp.getAno());
				getInstance().setNumeroOrigemProcesso(cp.getNumeroOrigemProcesso());
				getInstance().setNumeroSequencia(cp.getNumeroSequencia());
				getInstance().setNomeParte(cp.getNomeParte());
				if (cp.getNumeroCNPJ() != null && !"".equals(cp.getNumeroCNPJ())) {
					getInstance().setNumeroCpfCnpjParte(cp.getNumeroCNPJ());
				} else if (cp.getNumeroCPF() != null && !"".equals(cp.getNumeroCPF())) {
					getInstance().setNumeroCpfCnpjParte(cp.getNumeroCPF());
				}
				getInstance().setNascimentoInicialParte(cp.getNascimentoInicialParte());
				getInstance().setNascimentoFinalParte(cp.getNascimentoFinalParte());
				getInstance().setUfOABParte(cp.getUfOABParte());
				getInstance().setLetraOABParte(cp.getLetraOABParte());
				getInstance().setNumeroOABParte(cp.getNumeroOABParte());
				if (cp.getIdTaskAnterior() != null) {
					getInstance().setTarefaAnterior(getEntityManager().find(Tarefa.class, cp.getIdTaskAnterior()));
				}
				getInstance().setCargo(cp.getCargo());
				persist();
				PainelUsuarioHome.instance().setProcessoCaixa(getInstance());
				retorno = getInstance();
			} else {
				persist();
				retorno = getInstance();
			}
			newInstance();
			TarefasTreeHandler tree = getComponent("tarefasTree");
			tree.clearTree();
			//Recarregar o no da tarefa(pai) e seus filhos(caixas)
			TarefaTree.adicionarIdTarefa(idTarefa);
			getEntityManager().clear();
		}
		return retorno;
	}

	public boolean getCpf() {
		return cpf;
	}

	public void setCpf(boolean cpf) {
		this.cpf = cpf;
	}

	public void clearCpfCnpj() {
		instance.setNumeroCpfCnpjParte(null);
	}

	public String getNomeNovaCaixa() {
		return nomeNovaCaixa;
	}

	public void setNomeNovaCaixa(String nomeNovaCaixa) {
		this.nomeNovaCaixa = nomeNovaCaixa;
	}
	
	@Override
	public String update(){
		if(ParametroUtil.instance().isTerceiroGrau()){
			try {
				getInstance().setNumeroIdentificacaoOrgaoJustica(
						Integer.parseInt(getInstance().getRamoJustica() + getInstance().getRespectivoTribunal()));
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
		if(getInstance().getValorNumeroProcesso() != null && 
				!getInstance().getValorNumeroProcesso().isEmpty() &&
				!getInstance().getValorNumeroProcesso().matches(REGEX_PADRAO_INTERVALO)){
			FacesMessages.instance().add(Severity.ERROR, "Formato inválido no número do processo!");
			return "";
		} else {
			return super.update();
		}
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
		return q.getResultList();

	}
	
	public boolean getExisteOrgaoJulgadorAtual(){
		return Authenticator.getOrgaoJulgadorAtual() != null;
	}

}
