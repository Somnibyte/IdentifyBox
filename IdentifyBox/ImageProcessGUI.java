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
import java.awt.image.BufferedImage;
import java.util.ArrayList;

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
		setSize(800, 600);
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
	protected ImageUtils imageUtils = new ImageUtils();

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
						BufferedImage img = ImageIO.read(file);
						ArrayList<Integer> scaledDimens = imageUtils.getScaledDimensions(img, new Dimension(500, 500));
						imageLabel.setIcon(new ImageIcon(imageUtils.resize(img, scaledDimens.get(0), scaledDimens.get(1))));
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

