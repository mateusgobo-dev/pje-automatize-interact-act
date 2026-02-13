package br.com.infox.cliente.home;

import java.io.File;
import java.io.FilenameFilter;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ClasseAplicacao;
import br.jus.pje.nucleo.entidades.ComplementoClasse;

@Name(ClasseAplicacaoHome.NAME)
@BypassInterceptors
public class ClasseAplicacaoHome extends AbstractClasseAplicacaoHome<ClasseAplicacao> {

	private static final long serialVersionUID = 1L;
	private ComplementoClasse complemento = new ComplementoClasse();
	public static final String NAME = "classeAplicacaoHome";

	public static ClasseAplicacaoHome instance() {
		return ComponentUtil.getComponent(ClasseAplicacaoHome.NAME);
	}

	@Override
	public void newInstance() {
		super.newInstance();
		complemento.setComponenteValidacao("default");
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		complemento = new ComplementoClasse();
		return super.beforePersistOrUpdate();
	}

	@Override
	public String remove(ClasseAplicacao obj) {
		obj.setAtivo(Boolean.FALSE);
		setInstance(obj);
		String ret = super.update();
		newInstance();
		refreshGrid("classeAplicacaoGrid");
		return ret;
	}

	public ComplementoClasse getNovoComplemento() {
		return complemento;
	}

	public void setNovoComplemento(ComplementoClasse novoComplemento) {
		this.complemento = novoComplemento;
	}

	public void insereComplemento() {
		complemento.setClasseAplicacao(getInstance());

		if (complemento.getComplementoClasse() == null) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Informe a descrição.");
			return;
		}

		Boolean achou = false;
		int i = 0, size = getInstance().getComplementoClasseList().size();
		while ((!achou) & (i < size)) {
			achou = getInstance().getComplementoClasseList().get(i).getComplementoClasse()
					.equals(complemento.toString());
			i++;
		}
		if (complemento.getComplementoClasse() == null) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Digite uma descrição.");
			return;
		}
		if (!achou) {
			getInstance().getComplementoClasseList().add(complemento);
			complemento = new ComplementoClasse();
			complemento.setComponenteValidacao("default");
			complemento.setClasseAplicacao(getInstance());
			// super.persist();
			try {
				getEntityManager().persist(getInstance());
				EntityUtil.flush();
			} catch (Exception e) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						"Esta Aplicação já existe para esta Classe Judicial.");
			}
		} else {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Este complemento já existe.");
		}

		complemento = new ComplementoClasse();

	}

	public void removeComplemento(ComplementoClasse c) {
		getInstance().getComplementoClasseList().remove(c);
		getEntityManager().remove(c);
	}

	/*
	 * Retorna o nome dos componentes dentro da pasta "/WEB-INF/xhtml/components
	 * /form/documentosIdentificacao"
	 * 
	 * @Author: João Paulo lacerda
	 */

	public String[] getComponentList() {
		String dir = new Util().getContextRealPath();
		File file = new File(dir, "/WEB-INF/xhtml/components/form/docsIdentificacao");
		String[] fileStr = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return new File(dir, name).isFile();
			}
		});
		for (int i = 0; i < fileStr.length; i++) {
			fileStr[i] = fileStr[i].split(".xhtml")[0];
		}
		return fileStr;
	}

	public boolean verificaAplicacaoClasseJaCadastrada() {
		if (!isManaged()) {
			String queryString = "select count(a) from ClasseAplicacao a where a.classeJudicial = ? and a.aplicacaoClasse = ? and a.orgaoJustica = ?";

			Query query = getEntityManager().createQuery(queryString);

			query.setParameter(1, instance.getClasseJudicial());
			query.setParameter(2, instance.getAplicacaoClasse());
			query.setParameter(3, instance.getOrgaoJustica());

			Long retorno = 0L;
			try {
				retorno = (Long) query.getSingleResult();
			} catch (NoResultException no) {
				return Boolean.FALSE;
			}
			if (retorno > 0) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Aplicacao já cadastrada.");
				return true;
			}
		}
		return false;
	}

	@Override
	public String persist() {
		String ret = "";
		if (!verificaAplicacaoClasseJaCadastrada()) {
			ret = super.persist();

			getEntityManager().flush();
		}
		refreshGrid("classeAplicacaoGrid");
		return ret;
	}
}