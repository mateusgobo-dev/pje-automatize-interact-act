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
import br.com.jt.pje.dao.SalarioMinimoDAO;
import br.jus.pje.jt.entidades.SalarioMinimo;
import br.jus.pje.nucleo.util.DateUtil;

@Name(SalarioMinimoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class SalarioMinimoManager extends GenericManager {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "salarioMinimoManager";

	@In
	private SalarioMinimoDAO salarioMinimoDAO;

	public void persist(SalarioMinimo salarioMinimo) throws NegocioException {
		validar(salarioMinimo);
		salarioMinimoDAO.persist(salarioMinimo);
	}

	public void update(SalarioMinimo salarioMinimo) throws NegocioException {
		validar(salarioMinimo);
		salarioMinimoDAO.update(salarioMinimo);
	}
	
	public void remove(SalarioMinimo salarioMinimo) throws NegocioException {
		SalarioMinimo vigente = salarioMinimoDAO.getSalarioMinimoEmVigencia();
		if(salarioMinimo.getIdSalarioMinimo() == vigente.getIdSalarioMinimo()){
			throw new NegocioException("Não é possível excluir o salário mínimo vigente.");
		}
		salarioMinimoDAO.remove(salarioMinimo);
	}
	
	public void fecharSalarioVigente(Date dataInicio){
		SalarioMinimo vigente = salarioMinimoDAO.getSalarioMinimoEmVigencia();
		
		Date dataAnterior = DateUtil.dataMenosDias(dataInicio, 1);
		vigente.setDataFimVigencia(dataAnterior);
		
		if(vigente.getDataFimVigencia().before(vigente.getDataInicioVigencia())){
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			throw new NegocioException("A data de fechamento ("+df.format(vigente.getDataFimVigencia())+") não pode ser menor que a data de inicio ("+df.format(vigente.getDataInicioVigencia())+") da vigência anterior");
		}
		salarioMinimoDAO.update(vigente);
	}

	private void validar(SalarioMinimo salarioMinimo) {
		SalarioMinimo vigente = salarioMinimoDAO.getSalarioMinimoEmVigencia();
		boolean existeVigente = vigente != null;
		boolean ehVigente = existeVigente && vigente.getIdSalarioMinimo() == salarioMinimo.getIdSalarioMinimo();
		Date inicioVigencia = salarioMinimo.getDataInicioVigencia();
		Date fimVigencia = salarioMinimo.getDataFimVigencia();
		boolean estaFechando = fimVigencia != null;
		boolean ehAposVigente = existeVigente && (inicioVigencia.after(vigente.getDataInicioVigencia()) || (estaFechando && fimVigencia.after(vigente.getDataInicioVigencia())));
		
		if(!estaFechando && existeVigente && !ehVigente){
			throw new NegocioException("vigente");
		}else if(ehAposVigente){
			throw new NegocioException("Para cadastrar um período posterior ao vigente, a data fim deve estar em aberto.");
		}else if(!existeVigente && estaFechando){
			throw new NegocioException("Não existe salário mínimo em vigência.");
		}else if(ehVigente && estaFechando){
			throw new NegocioException("Não é possível finalizar esse salário mínimo, pois o mesmo se encontra em vigência.");
		}
		
		if(ehVigente && existeSalarioFechadoApos(inicioVigencia)){
			throw new NegocioException("Esse período é inválido, pois existe períodos fechados após a data informada.");
		}
		
		List<SalarioMinimo> list = salarioMinimoDAO.getSalarioMinimoEntre(inicioVigencia, fimVigencia, salarioMinimo.getIdSalarioMinimo());
		if (list != null && !list.isEmpty()) {
			throw new NegocioException("Já existe Salário Mínimo cadastrado para o período informado.");
		}
	}
	
	public SalarioMinimo getSalarioMinimoEm(Date data){
		return salarioMinimoDAO.getSalarioMinimoEm(data);
	}
	
	public boolean existeSalarioFechadoApos(Date data){
		return salarioMinimoDAO.getQtdSalariosFechadosApos(data) > 0;
	}
}