package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.ComplementoPessoaQualificacao;
import br.jus.pje.nucleo.entidades.ComplementoQualificacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaQualificacao;
import br.jus.pje.nucleo.entidades.Qualificacao;

@Name("pessoaQualificacaoHome")
@BypassInterceptors
public class PessoaQualificacaoHome extends AbstractPessoaQualificacaoHome<PessoaQualificacao> {

	private static final long serialVersionUID = 1L;
	private ArrayList<ComplementoPessoaQualificacao> compPessoaQualificacaoList;

	@Override
	public void setId(Object id) {
		compPessoaQualificacaoList = null;
		super.setId(id);
	}

	public List<ComplementoPessoaQualificacao> getComplementoPessoaQualificacaoList() {
		if (compPessoaQualificacaoList != null) {
			return compPessoaQualificacaoList;
		}
		if (getInstance() == null) {
			return null;
		}
		List<ComplementoPessoaQualificacao> pessoaQualificacaoList = null;
		Qualificacao qual = getInstance().getQualificacao();
		if (qual != null) {
			compPessoaQualificacaoList = new ArrayList<ComplementoPessoaQualificacao>();
			List<ComplementoQualificacao> compDocList = new ArrayList<ComplementoQualificacao>(
					qual.getComplementoQualificacaoList());
			pessoaQualificacaoList = getInstance().getComplementoPessoaQualificacaoList();
			for (ComplementoQualificacao cd : compDocList) {
				boolean exists = false;
				for (ComplementoPessoaQualificacao cpq : pessoaQualificacaoList) {
					if (cd.equals(cpq.getComplementoQualificacao())) {
						exists = true;
						compPessoaQualificacaoList.add(cpq);
					}
				}
				if (!exists) {
					ComplementoPessoaQualificacao cpq = new ComplementoPessoaQualificacao();
					cpq.setPessoaQualificacao(getInstance());
					cpq.setComplementoQualificacao(cd);
					compPessoaQualificacaoList.add(cpq);
				}
			}
		}
		return compPessoaQualificacaoList;
	}

	public void compPessoaQualificacaoListClear() {
		compPessoaQualificacaoList = null;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		PessoaHome pessoaHome = (PessoaHome) Component.getInstance("pessoaHome");
		Pessoa pessoa = pessoaHome.getInstance();
		if (pessoa != null) {
			getInstance().setPessoa(pessoa);
		}
		if (compPessoaQualificacaoList != null) {
			for (Iterator<ComplementoPessoaQualificacao> it = compPessoaQualificacaoList.iterator(); it.hasNext();) {
				ComplementoPessoaQualificacao cpq = it.next();
				String valor = cpq.getValorComplementoPessoaQualificacao();
				if (valor == null || valor.equals("")) {
					it.remove();
					if (getEntityManager().contains(cpq)) {
						getEntityManager().remove(cpq);
					}
				}
			}
			getInstance().setComplementoPessoaQualificacaoList(compPessoaQualificacaoList);
			compPessoaQualificacaoList = null;
		}
		refreshGrid("pessoaQualificacaoGrid");
		return super.beforePersistOrUpdate();
	}

	public boolean isRequired(Object o) {
		boolean ret = false;
		if (o instanceof ComplementoPessoaQualificacao) {
			ComplementoPessoaQualificacao cpq = (ComplementoPessoaQualificacao) o;
			ret = cpq.getComplementoQualificacao().getObrigatorio();
		}
		return ret;
	}

	@Override
	public String remove(PessoaQualificacao obj) {
		for (ComplementoPessoaQualificacao cpq : obj.getComplementoPessoaQualificacaoList()) {
			getEntityManager().remove(cpq);
		}

		return super.remove(obj);
	}

	/*
	 * Retorna o nome do componente atribuido ao documento de identificação.
	 */
	public String getComponenteQualificacao() {
		if (getInstance().getQualificacao() == null) {
			return "default";
		} else {
			String comp = getInstance().getQualificacao().getComponenteValidacao();
			if (comp == null)
				return "default";
			return comp;
		}
	}
}