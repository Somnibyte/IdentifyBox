/*
*	File: ImageProcessGUI.java
* 
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.JCheckBox;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageProcessGUI extends JFrame{

	private LoadImagePanel loadPanel;
	private ImageTransformPanel transformPanel;

	public ImageProcessGUI(){
		super("Image Processing Application");
		getContentPane().setLayout(new BorderLayout());

		transformPanel = new ImageTransformPanel("Steps to generate output images");
		loadPanel = new LoadImagePanel("Original Image", transformPanel);
		
		add(transformPanel, BorderLayout.LINE_END);
		add(loadPanel, BorderLayout.CENTER);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setVisible(true);
	}

	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ImageProcessGUI();
            }
        });
	} 
}

class LoadImagePanel extends JPanel{

	protected JLabel labelImage;
	protected JLabel imageLabel;
	protected JButton loadImage;
	protected String imagePath;
	protected ImageUtils imageUtils = new ImageUtils();

	public LoadImagePanel(String title, ImageTransformPanel transformPanel){
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(new EtchedBorder(), title));

		labelImage = new JLabel("Image:");
		imageLabel = new JLabel();
		loadImage = new JButton("Load Image to be Compared Against");
		loadImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("input/"));
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Bitmap Images", "bmp");
    			fc.setFileFilter(filter);
				int result = fc.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION){
					File file = fc.getSelectedFile();
					imagePath = file.getName();
					try{
						BufferedImage img = ImageIO.read(file);
						ArrayList<Integer> scaledDimens = imageUtils.getScaledDimensions(img, new Dimension(500, 500));
						imageLabel.setIcon(new ImageIcon(imageUtils.resize(img, scaledDimens.get(0), scaledDimens.get(1))));
						transformPanel.setImagePath(imagePath); 
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


}


class ImageTransformPanel extends JPanel{

	private String imagePath = " ";
	private JButton loadImage;
	private JCheckBox smoothBox, sharpenBox, histogramBox, edgeDetectBox, finalBox;
	// private HashMap<String, Boolean> actions = new HashMap<>(); for automatic mode only
	private BitImage bitimage = new BitImage();
	private BufferedImage img;
	private JButton applyButton;
	private JPanel extras;

	public ImageTransformPanel(String titled){
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(new EtchedBorder(), titled));

		//JLabel instructions = new JLabel("You have to click \"Load Image to be Processed\" before you run any of the operations below.");

		loadImage = new JButton("Load Image to be Processed");
		loadImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent et){
				int option = JOptionPane.showConfirmDialog(null, "Do you want to use a new image or keep using the old one?",
								"Select Image for Processing", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION){
					JFileChooser fc = new JFileChooser();
					int result = fc.showOpenDialog(null);
					if (result == JFileChooser.APPROVE_OPTION){
						File file = fc.getSelectedFile();
						imagePath = file.getName();
						img = null;
					}
				}
			}
		});

		smoothBox = new JCheckBox("Apply smoothing");
		histogramBox = new JCheckBox("Apply histogram equalization");

		//setupActions();

		smoothBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent st){
				JCheckBox cb = (JCheckBox) st.getSource();
        		if (cb.isSelected()) {
        			try{
        				// Will change the values of 4 to a more generic version.
            			img = bitimage.applySmoothing((imagePath), 4, 4);
            		} catch (IOException e){
            			e.printStackTrace();
            		}imageLabel.setIcon(new ImageIcon
        		} 
			}
		});

		histogramBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ht){
				JCheckBox cb = (JCheckBox) ht.getSource();
        		if (cb.isSelected()) {
        			try{
            			img = bitimage.applyHistogramEqualization((imagePath));
        			}catch (IOException e){
        				e.printStackTrace();
        			}
        		} 
			}
		});

		applyButton = new JButton("Apply operation to image");
		applyButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent at){
				new OutputImageModal(img, bitimage);
			}
		});


		extras = new JPanel();
		extras.setBorder(new TitledBorder(new EtchedBorder(), "Extra configs for option selected"));

		add(loadImage);
		add(smoothBox);
		add(histogramBox);
		add(extras);
		add(applyButton);

	}

	/* For automatic mode only
	private void setupActions(){
		actions.put("Apply smoothing", false);
		actions.put("Apply histogram equalization", false);
	}*/

	public void setImagePath(String imagePath){
		this.imagePath = imagePath;
	}
}


class OutputImageModal extends JDialog{

	private JLabel outImgLabel;
	private JButton saveButton, noSaveButton;
	private ImageUtils imageUtils = new ImageUtils();

	public OutputImageModal(BufferedImage img, BitImage bitimage){
		outImgLabel = new JLable();
		outImgLabel.setBorder(new TitledBorder(new EtchedBorder(), "Output Image"));

		ArrayList<Integer> scaledDimens = imageUtils.getScaledDimensions(img, new Dimension(500, 500));
		outImgLabel.setIcon(new ImageIcon(imageUtils.resize(img, scaledDimens.get(0), scaledDimens.get(1))));

		saveButton = new JButton("Save");
		ActionListener lst = new ActionListener(){
			public void actionPerformed(ActionEvent avt){
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Specify a file to save");
				fileChooser.setCurrentDirectory(new File("output/"));
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Bitmap Images", "bmp");
    			fileChooser.setFileFilter(filter);
				int userSelection = fileChooser.showSaveDialog(null);
				if (userSelection == JFileChooser.APPROVE_OPTION) {
 				    File file = fileChooser.getSelectedFile();
 				    try{
     					bitimage.save(file.getName());
     				} catch (IOException | RuntimeException ex){
     					ex.printStackTrace();
     				}
				}
				dispose();
			}
		};
		saveButton.addActionListener(lst);
		getRootPane().setDefaultButton(saveButton);
		getRootPane().registerKeyboardAction(lst,
			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);

		noSaveButton = new JButton("Don't Save");
		ActionListener ast = new ActionListener(){
			public void actionPerformed(ActionEvent et){
				dispose();
			}
		};
		getRootPane().registerKeyboardAction(ast,
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);


	}

}
