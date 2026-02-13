/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.itx.component;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

public class MeasureTime implements Serializable{

	private static final long serialVersionUID = 1L;

	private long start;
	
	private long stop;
	
	private boolean counting = false;
	
	private Logger logger = Logger.getLogger(MeasureTime.class);

	public MeasureTime(){
		start = 0;
	}

	public MeasureTime(boolean start){
		this();
		if (start && !counting){
			start();
		}
	}

	public MeasureTime start(){
		start = new Date().getTime();
		counting = true;
		return this;
	}

	public long stop(){
		stop = new Date().getTime();
		return stop - start;
	}

	public long getTime(){
		return stop - start;
	}

	public void print(String msg){
		logger.info(String.format("%s. Time spent: %d ms", msg, (stop - start)));
	}

	public void reset(){
		counting = false;
	}

	public void resetAndStart(){
		reset();
		start();
	}

}