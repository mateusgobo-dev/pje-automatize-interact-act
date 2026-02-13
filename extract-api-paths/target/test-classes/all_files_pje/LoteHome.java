package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jbpm.taskmgmt.def.Task;

import br.com.infox.cliente.component.tree.LotesTreeHandler;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Lote;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.enums.TitularidadeOrgaoEnum;

@Name("loteHome")
@BypassInterceptors
public class LoteHome extends AbstractLoteHome<Lote> {

	private static final long serialVersionUID = 1L;
	private TitularidadeOrgaoEnum titularidade;
	private String nomeNovoLote;
	private String idTarefa;

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			Date dtInclusaoProcesso = new Date();
			setLoteValues(dtInclusaoProcesso);
			if (Integer.valueOf(idTarefa) != 0) {
				getInstance().setTarefa(JbpmUtil.getTarefa(Long.parseLong(idTarefa)));
			}
			ret = super.persist();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	// Verifica se a descrição do lote não está nulo.
	private void setLoteValues(Date data) {
		if (!getInstance().getLote().equals("")) {
			getInstance().setDtCriacao(data);
			Pessoa pessoaLogada = (Pessoa) Contexts.getSessionContext().get("usuarioLogado");
			getInstance().setUsuario(pessoaLogada);
			getInstance().setAtivo(Boolean.TRUE);
		}
	}

	@Override
	public String remove(Lote obj) {
		String ret = null;
		try {
			ret = super.update();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	public static LoteHome instance() {
		return ComponentUtil.getComponent("loteHome");
	}

	public TitularidadeOrgaoEnum getTitularidade() {
		return titularidade;
	}

	public void setTitularidade(TitularidadeOrgaoEnum titularidade) {
		this.titularidade = titularidade;
	}

	public TitularidadeOrgaoEnum[] getTitularidadeItems() {
		return TitularidadeOrgaoEnum.values();
	}

	public List<SelectItem> getActorTasks() {
		List<SelectItem> listTasksItems = new ArrayList<SelectItem>();
		if (!isManaged()) {
			listTasksItems.add(new SelectItem(String.valueOf(0), "Selecione uma Tarefa"));
		}
		for (Task task : JbpmUtil.getTasksForLocalizacaoAtual()) {
			listTasksItems.add(new SelectItem(String.valueOf(task.getId()), task.getName()));
		}
		return listTasksItems;
	}

	public void setNomeNovoLote(String nomeNovoLote) {
		this.nomeNovoLote = nomeNovoLote;
	}

	public String getNomeNovoLote() {
		return nomeNovoLote;
	}

	public void addLote(int idTarefa) {
		instance = new Lote();
		instance.setTarefa(getEntityManager().find(Tarefa.class, idTarefa));
		instance.setLote(nomeNovoLote);
		persist();
		LotesTreeHandler tree = ComponentUtil.getComponent("lotesTree");
		tree.clearTree();
	}

	public void removeLote(int idLote) {
		instance = EntityUtil.find(Lote.class, idLote);
		remove();
		LotesTreeHandler tree = ComponentUtil.getComponent("lotesTree");
		tree.clearTree();
	}

	public void setIdTarefa(String idTarefa) {
		this.idTarefa = idTarefa;
	}

	public String getIdTarefa() {
		if (getInstance().getTarefa() != null) {
			return getInstance().getTarefa().getTarefa();
		}
		return null;
	}

	public void clearSearchLote() {
		Contexts.removeFromAllContexts("usuariosLoteSuggest");
	}
}