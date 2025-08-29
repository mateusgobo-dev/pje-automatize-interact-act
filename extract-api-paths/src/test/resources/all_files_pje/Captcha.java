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

package br.com.itx.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.captcha.CaptchaResponse;

@Name("org.jboss.seam.captcha.captcha")
@BypassInterceptors
public class Captcha extends org.jboss.seam.captcha.Captcha {

	private static final long serialVersionUID = 1L;
	
	private String correctResponse;
	private SecureRandom secureRandom = new SecureRandom();	
	private Color backgroundColor = new Color(0xf5,0xf5, 0xf5);
	// PJEII-14690 [TJDFT] Vanessa Schriver: aumentou-se o tamanho da fonte e a altura do campo para melhorar a legibilidade do captcha.
	private Font textFont = new Font("Arial", Font.PLAIN, 27);
	private int charsToPrint = 6;
	private int width = 120;
	private int height = 40;
	private int rectsToDraw = 4;
	private float horizMargin = 20.0f;
	private double rotationRange = 0.2;
	
    @Override
    @Create
    public void init() {
        super.init();
        inicializaDesafioResposta();
    }

	/**
     * Inicializa o desafio e a resposta correta.
     */
    public void inicializaDesafioResposta() {
    	String str = "123456789";
    	char[] caracteresParaCaptcha = str.toCharArray();
		String challenge = RandomStringUtils.random(6, 0, caracteresParaCaptcha.length, true, true, caracteresParaCaptcha, new SecureRandom());
        super.setChallenge(challenge);
        setCorrectResponse(challenge);    	
    }
    
    /**
     * Obtém a resposta correta
     */
    private String getCorrectResponse() {
    	return this.correctResponse;
    }
    
    /**
     * Seta a resposta correta
     */
    protected void setCorrectResponse(String correctResponse) {
    	this.correctResponse = correctResponse;
    }
    
    /**
     * Valida o texto digitado com a resposta correta ao desafio.
     * 
     * @param response
     */
    @Override
    public boolean validateResponse(String response) {
    	boolean valid = ((response != null)
    			&& (getCorrectResponse() != null)
    			&& (response.toLowerCase().equals(getCorrectResponse().toLowerCase())));
       if (!valid) {
          init();
       }
       return valid;
    }

	@Override
	@CaptchaResponse(message = "#{messages['captcha.invalidCaptcha']}")
	public String getResponse() {
		return super.getResponse();
	}
	
	/**
    * Renderiza a questão do desafio como uma imagem.
    */
    @Override
    public BufferedImage renderChallenge() {    	
    	return getAlgorithmRenderChallenge();
    }
    
    private BufferedImage getAlgorithmRenderChallenge(){
        inicializaDesafioResposta();
        
    	BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);

        for (int i = 0; i < rectsToDraw; i++) {
            int rectColor = 80 + (int)(secureRandom.nextDouble() * 70);
            float rectLinewidth = 0.3f + (float)(secureRandom.nextDouble());
            g.setColor(new Color(rectColor, rectColor, rectColor));
            g.setStroke(new BasicStroke(rectLinewidth));
            int halfRect = (int) (secureRandom.nextDouble() * height / 2.0);
            int rectX = (int) (secureRandom.nextDouble() * width - halfRect);
            int rectY = (int) (secureRandom.nextDouble() * height - halfRect);
            g.drawRect(rectX, rectY, halfRect * 2, halfRect * 2);
        }

        g.setFont(textFont);
        FontMetrics fontMetrics = g.getFontMetrics();
        int maxAdvance = fontMetrics.getMaxAdvance();
        int fontHeight = fontMetrics.getHeight();
        float spaceForLetters = -horizMargin * 2 + width;
        float spacePerChar = spaceForLetters / (charsToPrint - 1.0f);

        char[] allChars = getChallenge().toCharArray();
        for (int i = 0; i < allChars.length; i++ ) {
            char charToPrint = allChars[i];
            int charWidth = fontMetrics.charWidth(charToPrint);
            int charDim = Math.max(maxAdvance, fontHeight);
            int halfCharDim = (charDim / 2);
            BufferedImage charImage = new BufferedImage(charDim, charDim, BufferedImage.TYPE_INT_ARGB);
            Graphics2D charGraphics = charImage.createGraphics();
            charGraphics.translate(halfCharDim, halfCharDim);
            double angle = (secureRandom.nextDouble() - 0.5) * rotationRange;
            charGraphics.transform(AffineTransform.getRotateInstance(angle));
            charGraphics.translate(-halfCharDim, -halfCharDim);
            int charColor = 60 + (int)(secureRandom.nextDouble() * 90);
            charGraphics.setColor(new Color(charColor, charColor, charColor));
            charGraphics.setFont(textFont);
            int charX = (int) (0.5 * charDim - 0.5 * charWidth);
            charGraphics.drawString("" + charToPrint, charX, ((charDim - fontMetrics.getAscent())/2 + fontMetrics.getAscent()));
            float x = horizMargin + spacePerChar * (i) - charDim / 2.0f;
            int y = ((height - charDim) / 2);
            g.drawImage(charImage, (int) x, y, charDim, charDim, null, null);

            charGraphics.dispose();
        }
        g.dispose();
        return bufferedImage;
    }
    
    public int getCode() {
    	return (int) (secureRandom.nextDouble() * 10000);
    }

}