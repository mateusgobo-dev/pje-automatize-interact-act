package br.jus.cnj.pje.servicos;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.DateDAO;

/**
 * Oferece serviços de data/hora. Atualmente, utiliza o <code>DateDAO</code>
 * para recuperar a data/hora do servidor de banco de dados. Futuramente,
 * deve ser substituído por uma protocolizadora. 
 * @author Ricardo Scholz / David Vieira
 */
@Name(DateService.NAME)
@Scope(ScopeType.EVENT)
public class DateService {
	
	/*
	 * PJE-JT: Ricardo Scholz / David Vieira : PJEII-6850 - 2013-05-21
	 * Criação de método que recupera a data atual do sistema. As várias 
	 * instâncias do JBoss não apresentam precisão de sincronização suficiente, 
	 * causando diversos problemas na ordenação de documentos, que é realizada 
	 * com base na data de assinatura. Abordagem ideal deve utilizar uma 
	 * protocolizadora. 
	 */
	
	public final static String NAME = "dateService";
	
	@In(create = true, required = true)
	private DateDAO dateDAO;
	
	public Date getDataHoraAtual() {
		return dateDAO.getDataHoraAtual();
	}
	
	/*
	 * PJE-JT: Fim.
	 */
	
	public static DateService instance() {
		return (ComponentUtil.getComponent(DateService.NAME));
	}	
}
