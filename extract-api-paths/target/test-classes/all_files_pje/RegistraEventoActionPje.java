/*
  IBPM - Ferramenta de produtividade Java
  Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.
 
  Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
  sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
  Free Software Foundation; versão 2 da Licença.
  Este programa é distribuído na expectativa de que seja útil, porém, SEM 
  NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
  ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
  
  Consulte a GNU GPL para mais detalhes.
  Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
  veja em http://www.gnu.org/licenses/  
 */
package br.com.infox.ibpm.jbpm.actions;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.Usuario;

public class RegistraEventoActionPje {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public static void registraEventoProcessual(Date data, String... codigos) throws Exception {
		RegistraEventoAction registraEventoAction = RegistraEventoAction.instance();

		Processo processo = ProcessoHome.instance().getInstance();
		Usuario usuario = Authenticator.getUsuarioLogado();

		String hql = "select o from Evento o where o.codEvento in (:codigos)";
		Query query = EntityUtil.createQuery(hql);
		List<String> listaCodigos = Arrays.asList(codigos);
		query.setParameter("codigos", Util.isEmpty(listaCodigos)?null:listaCodigos);
		List<Evento> list = query.getResultList();
		if (list.size() == 0) {
			throw new Exception("Nenhum evento encontrado: " + Arrays.toString(codigos));
		}
		for (Evento eventoProcessual : list) {
			registraEventoAction.registrarEvento(processo, eventoProcessual, usuario, data);
		}
	}

}