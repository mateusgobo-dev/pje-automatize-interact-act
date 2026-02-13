package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ConsultaProcessoAdiadoVista;

@Name("consultaProcessoAdiadoVistaHome")
@BypassInterceptors
public class ConsultaProcessoAdiadoVistaHome extends AbstractHome<ConsultaProcessoAdiadoVista> {

	private static final long serialVersionUID = 1L;

	public void setConsultaProcessoAdiadoVistaIdConsultaProcessoAdiadoVista(Integer id) {
		setId(id);
	}

	public Integer getConsultaProcessoAdiadoVistaIdConsultaProcessoAdiadoVista() {
		return (Integer) getId();
	}

}