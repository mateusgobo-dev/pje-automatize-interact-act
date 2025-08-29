package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;

@Name("processoAssuntoHome")
@BypassInterceptors
public class ProcessoAssuntoHome extends AbstractProcessoAssuntoHome<ProcessoAssunto> {

	private static final long serialVersionUID = 1L;
	private AssuntoTrf assuntoTrf;
	private String assunto;

	public AssuntoTrf getLocalizacao() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			assuntoTrf = getInstance().getAssuntoTrf();
		}
	}

	@Override
	public void newInstance() {
		assuntoTrf = null;
		super.newInstance();
	}

	@Override
	public String persist() {
		getInstance().setAssuntoTrf(assuntoTrf);
		getInstance().getProcessoTrf().getProcessoAssuntoList().add(getInstance());
		refreshGrid("processoAssuntoGrid");
		return super.persist();
	}

	@Override
	public String update() {
		getInstance().setAssuntoTrf(assuntoTrf);
		String update = super.update();
		refreshGrid("processoAssuntoGrid");
		return update;
	}

	@Override
	public String remove(ProcessoAssunto obj) {
		setInstance(obj);
		return super.remove(obj);
	}

	public String getAssunto() {
		return assunto;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}

}