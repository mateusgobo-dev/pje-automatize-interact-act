package br.com.infox.cliente.home;

import java.util.Iterator;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoPapel;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("tipoProcessoDocumentoTrfPapelHome")
@BypassInterceptors
public class TipoProcessoDocumentoTrfPapelHome extends
		AbstractTipoProcessoDocumentoTrfPapelHome<TipoProcessoDocumentoPapel> {
	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		getInstance().setTipoProcessoDocumento(TipoProcessoDocumentoTrfHome.instance().getInstance());
		String ret = super.persist();
		refreshGrid("tipoProcessoDocumentoTrfPapelGrid");
		return ret;
	}

	@Override
	public String update() {
		getInstance().setTipoProcessoDocumento(TipoProcessoDocumentoTrfHome.instance().getInstance());
		String ret = super.update();
		refreshGrid("tipoProcessoDocumentoTrfPapelGrid");
		return ret;
	}

	@Override
	public String remove() {
		getInstance().setTipoProcessoDocumento(TipoProcessoDocumentoTrfHome.instance().getInstance());
		newInstance();
		return super.remove();
	}

	@SuppressWarnings("unchecked")
	public List<Papel> papelItems() {
		String ejbql = "select o from Papel o order by LOWER(COALESCE(o.nome, o.identificador))";
		List<Papel> papeis = getEntityManager().createQuery(ejbql).getResultList();
		for (Iterator<Papel> iterator = papeis.iterator(); iterator.hasNext();) {
			Papel papel = iterator.next();
			if (papel.getIdentificador().startsWith("/")) {
				iterator.remove();
			}
		}
		return papeis;
	}
}