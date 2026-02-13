package br.com.jt.pje.manager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.exceptions.NegocioException;
import br.com.jt.pje.dao.LimiteDepositoRecursalDAO;
import br.jus.pje.jt.entidades.LimiteDepositoRecursal;
import br.jus.pje.nucleo.util.DateUtil;

@Name(LimiteDepositoRecursalManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class LimiteDepositoRecursalManager extends GenericManager {

	private static final long serialVersionUID = 1L;  

	public static final String NAME = "limiteDepositoRecursalManager";

	@In
	private LimiteDepositoRecursalDAO limiteDepositoRecursalDAO;

	public void persist(LimiteDepositoRecursal limiteDepositoRecursal) throws NegocioException {
		validar(limiteDepositoRecursal);
		limiteDepositoRecursalDAO.persist(limiteDepositoRecursal);
	}

	public void update(LimiteDepositoRecursal limiteDepositoRecursal) throws NegocioException {
		validar(limiteDepositoRecursal);
		limiteDepositoRecursalDAO.update(limiteDepositoRecursal);
	}
	
	public void remove(LimiteDepositoRecursal limiteDepositoRecursal) throws NegocioException{
		LimiteDepositoRecursal vigente = limiteDepositoRecursalDAO.getLimiteDepositoRecursalEmVigencia();
		if(limiteDepositoRecursal.getIdLimiteDepositoRecursal() == vigente.getIdLimiteDepositoRecursal()){
			throw new NegocioException("Não é possível excluir o limite de depósito recursal vigente.");
		}
		limiteDepositoRecursalDAO.remove(limiteDepositoRecursal);
	}
	
	public void fecharDepositoRecursalVigente(Date dataInicio){
		LimiteDepositoRecursal vigente = limiteDepositoRecursalDAO.getLimiteDepositoRecursalEmVigencia();
		
		Date dataAnterior = DateUtil.dataMenosDias(dataInicio, 1);
		vigente.setDataFimVigencia(dataAnterior);
		
		if(vigente.getDataFimVigencia().before(vigente.getDataInicioVigencia())){
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			throw new NegocioException("A data de fechamento ("+df.format(vigente.getDataFimVigencia())+") não pode ser menor que a data de inicio ("+df.format(vigente.getDataInicioVigencia())+") da vigência anterior");
		}
		limiteDepositoRecursalDAO.update(vigente);
	}

	private void validar(LimiteDepositoRecursal limiteDepositoRecursal) {
		LimiteDepositoRecursal vigente = limiteDepositoRecursalDAO.getLimiteDepositoRecursalEmVigencia();
		boolean existeVigente = vigente != null;
		boolean ehVigente = existeVigente && vigente.getIdLimiteDepositoRecursal() == limiteDepositoRecursal.getIdLimiteDepositoRecursal();
		Date inicioVigencia = limiteDepositoRecursal.getDataInicioVigencia();
		Date fimVigencia = limiteDepositoRecursal.getDataFimVigencia();
		boolean estaFechando = fimVigencia != null;
		boolean ehAposVigente = existeVigente && (inicioVigencia.after(vigente.getDataInicioVigencia()) || (estaFechando && fimVigencia.after(vigente.getDataInicioVigencia())));
		
		if(!estaFechando && existeVigente && !ehVigente){
			throw new NegocioException("vigente");
		}else if(ehAposVigente){
			throw new NegocioException("Para cadastrar um período posterior ao vigente, a data fim deve estar em aberto.");
		}else if(!existeVigente && estaFechando){
			throw new NegocioException("Não existe limite de depósito recursal em vigência.");
		}else if(ehVigente && estaFechando){
			throw new NegocioException("Não é possível finalizar esse limite de depósito recursal, pois o mesmo se encontra em vigência.");
		}
		
		if(ehVigente && existeLimiteFechadoApos(inicioVigencia)){
			throw new NegocioException("Esse período é inválido, pois existe períodos fechados após a data informada.");
		}
		
		List<LimiteDepositoRecursal> list = limiteDepositoRecursalDAO.getLimiteDepositoRecursalEntre(inicioVigencia , fimVigencia, limiteDepositoRecursal.getIdLimiteDepositoRecursal());
		if(list != null && !list.isEmpty()){
			throw new NegocioException("Já existe limite de depósito recursal cadastrado para o período informado.");
		}
	}
	
	public LimiteDepositoRecursal getLimiteDepositoRecursalEm(Date data){
		return limiteDepositoRecursalDAO.getLimiteDepositoRecursalEm(data);
	}
	
	public boolean existeLimiteFechadoApos(Date data){
		return limiteDepositoRecursalDAO.getQtdLimitesFechadosApos(data) > 0;
	}
}