package br.com.infox.pje.service;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Base64;

import br.com.itx.util.ComponentUtil;

/**
 * Service que salva as imagens Base64 do HTML em um arquivo temporário para o iText conseguir colocar as imagens no PDF. 
 */
@Name(ItextHtmlConverterService.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class ItextHtmlConverterService implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Logger
	private Log logger;

	public static final String NAME = "itextHtmlConverterService";
	private static final int MAX_WIDTH = 700;
	
	private List<File> tempFiles = new ArrayList<>();

	public String converteImagensHtml(String html) {
		Pattern pattern = Pattern.compile("<img .*src=\"(.*?)(data:image/.*?;base64,)(.*?)\"(.*?)(>|/>|>(.*?)</img>)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(html);
		
		ByteArrayInputStream byteArrayInputStream = null;
		while (matcher.find()) {
			try {
				byteArrayInputStream = new ByteArrayInputStream(Base64.decode(matcher.group(3)));
				BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
				
				File tempFile = File.createTempFile("itext", null);
				this.tempFiles.add(tempFile);
				ImageIO.write(resize(bufferedImage, MAX_WIDTH), "png", tempFile);
				
				html = html.replace(matcher.group(1) + matcher.group(2) + matcher.group(3), tempFile.toURI().toString());
			} catch (Exception ex) {
				logger.error(ex.getLocalizedMessage());
			} finally {
				try {
					byteArrayInputStream.close();
				} catch (IOException e) {
					// Nothing to do...
				}
			}
		}
		return html;
	}
	
    private BufferedImage resize(BufferedImage bufferedImage, int width) {
    	if (bufferedImage.getWidth() > width) {
    		int adjustedHeight = Double.valueOf(width * ((double)bufferedImage.getHeight() / bufferedImage.getWidth())).intValue();
    		
            Image image = bufferedImage.getScaledInstance(width, adjustedHeight, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(width, adjustedHeight, BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D graphics2d = resized.createGraphics();
            graphics2d.drawImage(image, 0, 0, null);
            graphics2d.dispose();
            
            return resized;    		
    	}
    	return bufferedImage;
    }
	
	@Destroy
	public void destroy() {
		for (File tempFile : this.tempFiles) {
			tempFile.delete();
		}
	}
	
	public static final ItextHtmlConverterService instance() {
		return ComponentUtil.getComponent(NAME);
	}
	
}