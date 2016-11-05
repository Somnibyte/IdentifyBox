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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageProcessGUI extends JFrame{

	private LoadImagePanel loadPanel;
	private ImageTransformPanel transformPanel;
	public String originalLoadedImageName = "";

	public ImageProcessGUI(){
		super("Image Processing Application");
		getContentPane().setLayout(new BorderLayout());

		loadPanel = new LoadImagePanel("Original Image");
		transformPanel = new ImageTransformPanel("Steps to generate output images", loadPanel);
		
		add(loadPanel, BorderLayout.CENTER);
		add(transformPanel, BorderLayout.LINE_END);
		
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

	private JLabel labelImage;
	private JLabel imageLabel;
	private JButton loadImage;
	private ImageUtils imageUtils = new ImageUtils();
	private String path = " ";

	public LoadImagePanel(String title){
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(new EtchedBorder(), title));

		labelImage = new JLabel("Image:");
		imageLabel = new JLabel();
		loadImage = new JButton("Load Image to be Compared Against");
		loadImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(new File("").getAbsolutePath(), "input"));
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Bitmap Images", "bmp");
    			fc.setFileFilter(filter);
				int result = fc.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION){
					File file = fc.getSelectedFile();
					path = file.getAbsolutePath();
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

	
	public String getPath(){
		return path;
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

	public ImageTransformPanel(String titled, LoadImagePanel loadPanel){
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(new EtchedBorder(), titled));

		//JLabel instructions = new JLabel("You have to click \"Load Image to be Processed\" before you run any of the operations below.");

		loadImage = new JButton("Load Image to be Processed");
		loadImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent et){
				int option = JOptionPane.showConfirmDialog(null, "Do you want to use a new image(Yes to use a new image, No to use the loaded image)?",
								"Select Image for Processing", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION){
					JFileChooser fc = new JFileChooser();
					fc.setCurrentDirectory(new File(new File("").getAbsolutePath()));
					int result = fc.showOpenDialog(null);
					if (result == JFileChooser.APPROVE_OPTION){
						File file = fc.getSelectedFile();
						setImagePath(file.getAbsolutePath());
						//img = null;
					}
				} else if (option == JOptionPane.NO_OPTION && !imagePath.isEmpty()){
					setImagePath(loadPanel.getPath());
				}
			}
		});

		smoothBox = new JCheckBox("Apply smoothing");
		histogramBox = new JCheckBox("Apply histogram equalization");
		applyButton = new JButton("Apply operation to image");

		//setupActions();

		smoothBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent st){
				JCheckBox bc = (JCheckBox) st.getSource();
        		if (bc.isSelected()) {
        			try{
        				// Will change the values of 4 to a more generic version.
        				if (imagePath.isEmpty()) System.out.println("Ficked");
            			img = bitimage.applySmoothing((imagePath), 4, 4);
            		} catch (IOException e){
            			e.printStackTrace();
            		}
        		} 
			}
		});

		histogramBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ht){
				JCheckBox cb = (JCheckBox) ht.getSource();
        		if (cb.isSelected()) {
        			try{
        				if (imagePath.isEmpty()) System.out.println("Ficked up");
            			img = bitimage.applyHistogramEqualization((imagePath));
        			}catch (IOException e){
        				e.printStackTrace();
        			}
        		} 
			}
		});

		
		applyButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent at){
				new OutputImageModal(img);
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
	private JPanel imagePanel, commandPanel;

	public OutputImageModal(BufferedImage img){

		imagePanel = new JPanel(new BorderLayout());
		commandPanel = new JPanel(new FlowLayout());

		outImgLabel = new JLabel();
		outImgLabel.setBorder(new TitledBorder(new EtchedBorder(), "Output Image"));

		ArrayList<Integer> scaledDimens = imageUtils.getScaledDimensions(img, new Dimension(500, 500));
		outImgLabel.setIcon(new ImageIcon(imageUtils.resize(img, scaledDimens.get(0), scaledDimens.get(1))));

		saveButton = new JButton("Save");
		noSaveButton = new JButton("Don't Save");

		ActionListener lst = new ActionListener(){
			public void actionPerformed(ActionEvent avt){
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Specify a file to save");
				fileChooser.setCurrentDirectory(new File(new File("").getAbsolutePath(), "output"));
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Bitmap Images", "bmp");
    			fileChooser.setFileFilter(filter);
				int userSelection = fileChooser.showSaveDialog(null);
				if (userSelection == JFileChooser.APPROVE_OPTION) {
 				    File file = fileChooser.getSelectedFile();
 				    //formatName = formatName + String
 				    try{
     					ImageIO.write(img, "bmp", file);
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

		
		ActionListener ast = new ActionListener(){
			public void actionPerformed(ActionEvent et){
				dispose();
			}
		};
		noSaveButton.addActionListener(ast);
		getRootPane().registerKeyboardAction(ast,
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);

		imagePanel.add(outImgLabel, BorderLayout.PAGE_START);

		commandPanel.add(saveButton);
		commandPanel.add(noSaveButton);
		imagePanel.add(commandPanel, BorderLayout.PAGE_END);

		add(imagePanel);
		pack();
		setVisible(true);
	}

}
