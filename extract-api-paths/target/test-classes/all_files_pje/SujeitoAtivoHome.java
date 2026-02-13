package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.lancadormovimento.SujeitoAtivo;

/**
 * Classe para operações com "Assunto(TUA)"
 * 
 */
@Name(SujeitoAtivoHome.NAME)
@BypassInterceptors
public class SujeitoAtivoHome extends AbstractHome<SujeitoAtivo>{

	public static final String NAME = "sujeitoAtivoHome";
	private static final long serialVersionUID = 1L;

	public void setSujeitoAtivoId(Long id){
		setId(id);
	}

	public Long getSujeitoAtivoId(){
		return (Long) getId();
	}

}
