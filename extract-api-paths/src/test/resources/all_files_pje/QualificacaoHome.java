package br.com.infox.cliente.home;

import static org.jboss.seam.faces.FacesMessages.instance;

import java.io.File;
import java.io.FilenameFilter;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ComplementoQualificacao;
import br.jus.pje.nucleo.entidades.Qualificacao;

@Name("qualificacaoHome")
@BypassInterceptors
public class QualificacaoHome extends AbstractQualificacaoHome<Qualificacao> {

	private static final long serialVersionUID = 1L;

	private ComplementoQualificacao complemento = new ComplementoQualificacao();

	@Override
	public void newInstance() {
		super.newInstance();
		getInstance().setComponenteValidacao("default");
		complemento.setComponenteValidacao("default");
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		complemento = new ComplementoQualificacao();
		return super.beforePersistOrUpdate();
	}

	@Override
	public String remove(Qualificacao obj) {
		obj.setAtivo(Boolean.FALSE);
		setInstance(obj);
		String ret = super.update();
		newInstance();
		return ret;
	}

	public ComplementoQualificacao getNovoComplemento() {
		return complemento;
	}

	public void setNovoComplemento(ComplementoQualificacao novoComplemento) {
		this.complemento = novoComplemento;
	}

	public void insereComplemento() {
		complemento.setQualificacao(getInstance());

		Boolean achou = false;
		int i = 0, size = getInstance().getComplementoQualificacaoList().size();
		while ((!achou) & (i < size)) {
			achou = getInstance().getComplementoQualificacaoList().get(i).getComplementoQualificacao()
					.equals(complemento.toString());
			i++;
		}

		if (!achou) {
			getInstance().getComplementoQualificacaoList().add(complemento);
			complemento = new ComplementoQualificacao();
			complemento.setComponenteValidacao("default");
		} else {
			instance().add(StatusMessage.Severity.ERROR, "Este complemento já existe.");
		}
	}

	public void removeComplemento(ComplementoQualificacao c) {
		getInstance().getComplementoQualificacaoList().remove(c);
		getEntityManager().remove(c);
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		for (ComplementoQualificacao cq : getInstance().getComplementoQualificacaoList()) {
			getEntityManager().merge(cq);
			EntityUtil.flush();
		}
		return super.afterPersistOrUpdate(ret);
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
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isFile();
			}
		});
		for (int i = 0; i < fileStr.length; i++) {
			fileStr[i] = fileStr[i].split(".xhtml")[0];
		}
		return fileStr;
	}
}