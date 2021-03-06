package Cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class Document extends ServerResource {
	String fileName = "";
	
	@Get
    public FileRepresentation getResource() throws IOException {
		FileRepresentation result = null;
		Request request = getRequest();
		Form form = request.getResourceRef().getQueryAsForm();		
		
		if(form.getValues("fileName") != null)
		{
			fileName += form.getValues("fileName");
		}
		for (int i = 0; i<Main.listOfCachedFiles.size(); i++)
    	{
    		if(fileName.equals(Main.listOfCachedFiles.get(i).getName()))
    		{
    			writeLog(fileName, "cached file " + fileName); 
    			String fileType = Files.probeContentType(new File(Main.filePath + "//" + fileName).toPath());
    			if(fileType==null)
    			{
    				result = new FileRepresentation(Main.filePath + "//" + fileName, MediaType.TEXT_HTML);
    			}
    			else
    			{
    				switch (fileType) {
    		         case "text/plain":
    		        	 result = new FileRepresentation(Main.filePath + "//" + fileName, MediaType.TEXT_HTML);
    		             break;
    		         case "image/jpeg":
    		        	 result = new FileRepresentation(Main.filePath + "//" + fileName, MediaType.IMAGE_JPEG);
    		             break;
    		         case "image/png":
    		        	 result = new FileRepresentation(Main.filePath + "//" + fileName, MediaType.IMAGE_PNG);
    		             break;
    		         case "application/pdf":
    		        	 result = new FileRepresentation(Main.filePath + "//" + fileName, MediaType.APPLICATION_PDF);
    		        	 break;
    		         case "application/msword":
    		        	 result = new FileRepresentation(Main.filePath + "//" + fileName, MediaType.APPLICATION_WORD);
    		             break;
    		         case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
    		        	 result = new FileRepresentation(Main.filePath + "//" + fileName, MediaType.APPLICATION_MSOFFICE_DOCX);
    		        	 break;
    		         case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
    		        	 result = new FileRepresentation(Main.filePath + "//" + fileName, MediaType.APPLICATION_MSOFFICE_XLSX);
    		        	 break;
    		         default:
    		        	 result = new FileRepresentation(Main.filePath + "//" + fileName, MediaType.TEXT_HTML);
    				}			
    			}		 
    			return result;
    		} 
    	}

		ClientResource file = new ClientResource("http://localhost:" + Main.serverPort + "/api/file?filename="+fileName);
		writeLog(fileName, "file " + fileName + " downloaded from the server");  
		Representation response = file.get();
		MediaType fileType = response.getMediaType();
		InputStream fileInput = response.getStream();
		OutputStream fileOut = new FileOutputStream(new File(Main.filePath+"/"+fileName));
		int read = 0;
		byte[] bytes = new byte[1024];
		 while ((read = fileInput.read(bytes)) != -1)
		 {
			 fileOut.write(bytes, 0, read);
		 }
		 fileInput.close();
		 fileOut.flush();
		 fileOut.close();
		 Main.listOfCachedFiles.add(new File(Main.filePath+"/"+fileName));
		 result = new FileRepresentation(Main.filePath + "//" + fileName, fileType);
		 return result;
    }
	
	public void writeLog(String fineName, String info){
        String content=info(fineName,info);
        RandomAccessFile mm = null;
        FileOutputStream o = null;
        try {
            o = new FileOutputStream(Main.logName,true);
            o.write(content.getBytes("utf-8"));
            o.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mm != null) {
                try {
                    mm.close(); 
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 
            }
        }
    }  
 
    public String info(String fileName, String info){
         StringBuffer buffer=new StringBuffer();
         Date date = new Date();   
         SimpleDateFormat sd = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");   
         String d = sd.format(date)+System.getProperty("line.separator");  
         buffer.append("<br>user request: file "+ fileName +" at "+ d+"<br>");
         buffer.append("response: ").append(info).append(System.getProperty("line.separator"));
         buffer.append("<br>");
         return buffer.toString();
    }
}
