package isis.firma;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;

@WebServlet("/PostSignServlet")
public class PostSignServlet extends HttpServlet {
	
	
	class MyExternalSignatureContainer implements ExternalSignatureContainer {
		
	    protected byte[] sig;
	    
	    public MyExternalSignatureContainer(byte[] sig) {
	        this.sig = sig;
	    }
	    
	    public byte[] sign(InputStream is) {
	        return sig;
	    }
	    
	    public void modifySigningDictionary(PdfDictionary signDic) {
	    }
	}
	
	
	private static final long serialVersionUID = 1L;
       
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setContentType("application/octet-stream");
			
			System.out.println("ENTRE A POST-SIGN");
			
			// we get the objects we need for postsigning from the session
			HttpSession session = req.getSession(false);
            PdfReader reader = new PdfReader(System.getenv("OPENSHIFT_DATA_DIR")+"/D0002_t.pdf");
            FileOutputStream fos = new FileOutputStream(System.getenv("OPENSHIFT_DATA_DIR")+"/D0002_f.pdf");
 
			session.invalidate();
			
			// we read the signed bytes
			ObjectInputStream ois = new ObjectInputStream(req.getInputStream());
						
			byte [] sig = new byte [256];
			ois.read(sig);
	
			ExternalSignatureContainer external = new MyExternalSignatureContainer(sig);
		    try {
				MakeSignature.signDeferred(reader, "sig", fos, external);
			} 
		    catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		    catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
   			
			// we write the signed document to the HttpResponse output stream
			byte [] pdf = new byte [20];
			OutputStream sos = resp.getOutputStream();
			sos.write(pdf, 0, pdf.length);
			sos.flush();
			sos.close();
			
			fos.flush();
			fos.close();
			reader.close();
		}
}