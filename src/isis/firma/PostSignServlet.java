package isis.firma;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PdfPKCS7;

@WebServlet("/PostSignServlet")
public class PostSignServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
			resp.setContentType("application/octet-stream");
			
			System.out.println("ENTRE A POST-SIGN");
			
			// we get the objects we need for postsigning from the session
			HttpSession session = req.getSession(false);
			PdfPKCS7 sgn = (PdfPKCS7) session.getAttribute("sgn");
			byte[] hash = (byte[]) session.getAttribute("hash");
			PdfSignatureAppearance sap = (PdfSignatureAppearance) session.getAttribute("sap");
			ByteArrayOutputStream os = (ByteArrayOutputStream) session.getAttribute("baos");
			session.invalidate();
			
			// we read the signed bytes
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ServletInputStream is = req.getInputStream();
			int read;
			byte[] data = new byte[256];
			while ((read = is.read(data, 0, data.length)) != -1) {
				baos.write(data, 0, read);
			}
			// we complete the PDF signing process
			sgn.setExternalDigest(baos.toByteArray(), null, "RSA");
			byte[] encodedSig = sgn.getEncodedPKCS7(hash,null,null, null, CryptoStandard.CMS);
			byte[] paddedSig = new byte[8192];
			System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
			PdfDictionary dic2 = new PdfDictionary();
			dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));
			try {
				sap.close(dic2);
			} 
			catch (DocumentException e) {
				throw new IOException(e);
			}
			
			// we write the signed document to the HttpResponse output stream
			byte[] pdf = os.toByteArray();
			OutputStream sos = resp.getOutputStream();
			sos.write(pdf, 0, pdf.length);
			sos.flush();
			sos.close();
		}
}