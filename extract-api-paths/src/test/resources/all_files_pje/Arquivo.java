/**
 * 
 */
package br.com.infox.ibpm.component;

import java.util.Arrays;

/**
 * @author solimar.santos
 * [PJEII-1139] Criação da classe para trabalhar de forma mais adequada com o arquivo de download.
 */
public class Arquivo {

	private String Name;  
    private String mime;  
    private long length;  
    private byte[] data;  
    public byte[] getData() {  
        return data;  
    }  
    public void setData(byte[] data) {  
        this.data = data;  
    }  
    public String getName() {  
        return Name;  
    }  
    public void setName(String name) {  
        Name = name;  
        int extDot = name.lastIndexOf('.');  
        if(extDot > 0){  
            String extension = name.substring(extDot +1);  
            if("bmp".equals(extension)){  
                mime="image/bmp";  
            } else if("jpg".equals(extension)){  
                mime="image/jpeg";  
            } else if("gif".equals(extension)){  
                mime="image/gif";  
            } else if("png".equals(extension)){  
                mime="image/png";  
            } else {  
                mime = "image/unknown";  
            }  
        }  
    }  
    public long getLength() {  
        return length;  
    }  
    public void setLength(long length) {  
        this.length = length;  
    }  
      
    public String getMime(){  
        return mime;  
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Name == null) ? 0 : Name.hashCode());
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + (int) (length ^ (length >>> 32));
		result = prime * result + ((mime == null) ? 0 : mime.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Arquivo other = (Arquivo) obj;
		if (Name == null) {
			if (other.Name != null)
				return false;
		}
		else if (!Name.equals(other.Name))
			return false;
		if (!Arrays.equals(data, other.data))
			return false;
		if (length != other.length)
			return false;
		if (mime == null) {
			if (other.mime != null)
				return false;
		}
		else if (!mime.equals(other.mime))
			return false;
		return true;
	}
}