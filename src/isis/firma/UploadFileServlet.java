package isis.firma;

import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.InputStream;  
import java.io.PrintWriter;  
import javax.servlet.ServletException;  
import javax.servlet.annotation.MultipartConfig;  
import javax.servlet.annotation.WebServlet;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  
import javax.servlet.http.Part;  


@WebServlet("/UploadFileServlet")
@MultipartConfig 
public class UploadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	int BUFFER_LENGTH = 4096; 
       
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         for (Part part : req.getParts()) { 
              InputStream is = req.getPart(part.getName()).getInputStream();  
              String fileName = getFileName(part);  
              FileOutputStream os = new FileOutputStream(System.getenv("OPENSHIFT_DATA_DIR") + fileName);  
              byte[] bytes = new byte[BUFFER_LENGTH];  
              int read = 0;  
              while ((read = is.read(bytes, 0, BUFFER_LENGTH)) != -1) {  
                   os.write(bytes, 0, read);  
              }  
              os.flush();  
              is.close();  
              os.close();  
              resp.sendRedirect("http://firma-isisconsultores.rhcloud.com/visor.html");
         }  

	}
	
	
    private String getFileName(Part part) {  
        for (String cd : part.getHeader("content-disposition").split(";")) {  
             if (cd.trim().startsWith("filename")) {  
                  String filename = cd.substring(cd.indexOf('=') + 1);  
                  //remove extra file path in windows local machine  
                  return filename.substring(filename.lastIndexOf("\\") + 1)  
                            .trim().replace("\"", "");  
             }  
        }  
        return null;  
   }  
	
}