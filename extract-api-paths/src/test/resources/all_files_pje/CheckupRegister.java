package br.jus.cnj.pje.util.checkup.spi;

import br.jus.cnj.pje.util.checkup.CheckupService;

/**
 * Interface que deverá ser usado em services que desejam executar os validadores do PJE.
 * 
 * @see CheckupService#registerCheckup(CheckupWorker) 
 * 
 * @author davidhsv
 *
 */
public interface CheckupRegister {

	void registerCheckup(CheckupWorker checkupRegister);

}
