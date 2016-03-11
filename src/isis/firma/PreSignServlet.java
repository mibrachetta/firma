package isis.firma;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalBlankSignatureContainer;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PdfPKCS7;

@WebServlet("/PreSignServlet")
public class PreSignServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setContentType("application/octet-stream");
			
			try {
				System.out.println("ENTRE A PRE-SIGN");
				
				// Obtenemos el Certificado del Cliente - OJO! No hay cadena de Certificación lo trata como un autofirmado
				ObjectInputStream ois = new ObjectInputStream(req.getInputStream());
				X509Certificate cert = (X509Certificate) ois.readObject();
				Certificate chain [] = new Certificate [1];
				chain[0]=cert;
								
				
				//Creamos un campo de firma vacío en el documento - Creando un nuevo documento temporal
				
	            PdfReader reader = new PdfReader(System.getenv("OPENSHIFT_DATA_DIR")+"/D0002.pdf");
	            FileOutputStream fos = new FileOutputStream(System.getenv("OPENSHIFT_DATA_DIR")+"/D0002_t.pdf");
	            PdfStamper stamper = PdfStamper.createSignature(reader, fos, '\0');
	            PdfSignatureAppearance sap = stamper.getSignatureAppearance();
				sap.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
	            sap.setCertificate(chain[0]);

	            ExternalSignatureContainer external = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
	            MakeSignature.signExternalContainer(sap, external, 8192);

	            fos.close();
	            reader.close();
	            
	            BouncyCastleDigest digest = new BouncyCastleDigest();
	            PdfPKCS7 sgn = new PdfPKCS7(null, chain, "SHA1", null, digest, false);
	            InputStream data = sap.getRangeStream();
	            byte[] hash = DigestAlgorithms.digest(data, digest.getMessageDigest("SHA1"));
	            Calendar cal = Calendar.getInstance();
	            byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, cal, null, null, CryptoStandard.CMS);
				
				// We store the objects we'll need for post signing in a session
				HttpSession session = req.getSession(true);
				session.setAttribute("sgn", sgn);
				session.setAttribute("hash", hash);
				session.setAttribute("cal", cal);
				session.setAttribute("sap", sap);
				session.setAttribute("fos", fos);

				// we write the hash that needs to be signed to the HttpResponse output
				OutputStream os = resp.getOutputStream();
				os.write(sh, 0, sh.length);
				os.flush();
				os.close();
			} 
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("Y NO ANDUVO");
				e.printStackTrace();
			}
			catch (DocumentException e) {
				throw new IOException(e);
			} 
			
			catch (GeneralSecurityException e) {
				throw new IOException(e);
			}
		}
}