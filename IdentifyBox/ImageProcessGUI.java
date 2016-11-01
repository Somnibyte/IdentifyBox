/*
*	File: ImageProcessGUI.java
* 
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.imageio.ImageIO;
import java.io.*;

public class ImageProcessGUI extends JFrame{

	public BitImage bitimage = new BitImage();
	//public HashMap<String, String> actions = new HashMap<>();
	private LoadImagePanel loadPanel;

	public ImageProcessGUI(){
		super("Image Processing Application");
		getContentPane().setLayout(new BorderLayout());

		loadPanel = new LoadImagePanel("Original Image");
		add(loadPanel, BorderLayout.LINE_START);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 800);
		pack();
		setVisible(true);
	}

	public static void main(String[] args){
		new ImageProcessGUI();
	} 
}

class LoadImagePanel extends JPanel{

	protected JLabel labelImage;
	protected JLabel imageLabel;
	protected JButton loadImage;
	protected String imagePath;

	public LoadImagePanel(String title){
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(new EtchedBorder(), title));

		labelImage = new JLabel("Image:");
		imageLabel = new JLabel();
		loadImage = new JButton("Load Image");
		loadImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JFileChooser fc = new JFileChooser();
				int result = fc.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION){
					File file = fc.getSelectedFile();
					imagePath = file.getName();
					try{
						imageLabel.setIcon(new ImageIcon(ImageIO.read(file)));
					} catch (IOException evt){
						evt.printStackTrace();
					}
				}
			}
		});


		add(loadImage);
		add(labelImage);
		add(imageLabel);
	}

	public String getImageFileName(){
		return imagePath;
	}
}


class ImageTransformPanel extends JPanel{

	public ImageTransformPanel(String title){

	}
}

