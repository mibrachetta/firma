package isis.firma;

import javax.swing.*;
public class appletFirma extends JApplet
{
	private JLabel etiqueta = new JLabel("Hola");
	public void init()
	{
		add(etiqueta);
	}

	public void cambia()
	{
		etiqueta.setText("Adios");
	}
}