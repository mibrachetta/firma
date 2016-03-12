package isis.firma;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
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
				// We get the self-signed certificate from the client
				ObjectInputStream ois = new ObjectInputStream(req.getInputStream());
				X509Certificate cert = (X509Certificate) ois.readObject();
				Certificate chain [] = new Certificate [1];
				chain[0]=cert;
								
				//we create a reader and a stamper
				File archivo_original = new File(System.getenv("OPENSHIFT_DATA_DIR")+"/D0002.pdf");
	            byte [] bytearchivo = new byte[(int) archivo_original.length()];
	            FileInputStream fis = new FileInputStream(archivo_original);
	            fis.read(bytearchivo);
	            PdfReader reader = new PdfReader(bytearchivo);

	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PdfStamper stamper = PdfStamper.createSignature(reader, baos, '\0');
				
				//we create the signature appearance
				PdfSignatureAppearance sap = stamper.getSignatureAppearance();
				sap.setReason("Prueba");
				sap.setLocation("En servidor");
				sap.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
				sap.setCertificate(chain[0]);
				
				// we create the signature infrastructure
				PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
				dic.setReason(sap.getReason());
				dic.setLocation(sap.getLocation());
				dic.setContact(sap.getContact());
				dic.setDate(new PdfDate(sap.getSignDate()));
				sap.setCryptoDictionary(dic);
				HashMap<PdfName, Integer> exc = new HashMap<PdfName, Integer>();
				exc.put(PdfName.CONTENTS, new Integer(8192 * 2 + 2));
				sap.preClose(exc);
				
				ExternalDigest externalDigest = new ExternalDigest() {
					public MessageDigest getMessageDigest(String hashAlgorithm) throws GeneralSecurityException {
						return DigestAlgorithms.getMessageDigest(hashAlgorithm, null);
					}
				};
				
				PdfPKCS7 sgn = new PdfPKCS7(null, chain, "SHA256",null, externalDigest, false);
				InputStream data = sap.getRangeStream();
				byte hash[] = DigestAlgorithms.digest(data,externalDigest.getMessageDigest("SHA256"));
				System.out.println("HASH");
				System.out.println(new String(Base64.encodeBytes(hash)));
				
				Calendar cal = Calendar.getInstance();
				byte[] sh = sgn.getAuthenticatedAttributeBytes(hash,cal,null, null, CryptoStandard.CMS);
				System.out.println("Y ESTO ES:");
				System.out.println(new String(Base64.encodeBytes(sh)));

				
				// We store the objects we'll need for post signing in a session
				HttpSession session = req.getSession(true);
				session.setAttribute("sgn", sgn);
				session.setAttribute("hash", hash);
				session.setAttribute("cal", cal);
				session.setAttribute("sap", sap);
				session.setAttribute("baos", baos);

			
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