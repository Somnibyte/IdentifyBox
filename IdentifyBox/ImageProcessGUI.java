/*
*	File: ImageProcessGUI.java
* 
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
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
	private JRadioButton smoothBox, sharpenBox, histogramBox, edgeDetectBox, finalBox;
	private ButtonGroup actions;	
	private BitImage bitimage = new BitImage();
	private BufferedImage img;
	private JButton applyButton;

	private int order;
	private JLabel kernelLabel;
	private JSpinner matrixOrder;
	private JPanel extras;

	public ImageTransformPanel(String titled, LoadImagePanel loadPanel){
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(new EtchedBorder(), titled));

		loadImage = new JButton("Load Image to be Processed");

		smoothBox = new JRadioButton("Apply smoothing");
		histogramBox = new JRadioButton("Apply histogram equalization");
		applyButton = new JButton("Apply operation to image");
		actions = new ButtonGroup ();

		actions.add(smoothBox);
		actions.add(histogramBox);

		smoothBox.setEnabled(false);
		histogramBox.setEnabled(false);


		loadImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent et){
				int option = JOptionPane.showConfirmDialog(null, "Do you want to use a new image(Yes to use a new image, No to use the last image loaded)?",
								"Select Image for Processing", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION){
					JFileChooser fc = new JFileChooser();
					fc.setCurrentDirectory(new File(new File("").getAbsolutePath()));
					int result = fc.showOpenDialog(null);
					if (result == JFileChooser.APPROVE_OPTION){
						File file = fc.getSelectedFile();
						setImagePath(file.getAbsolutePath());
					}
					// Loading the image from the LoadPanel
				} else if (option == JOptionPane.NO_OPTION && " ".equals(imagePath)){ 
					setImagePath(loadPanel.getPath());
					System.out.println(imagePath);
				}

				smoothBox.setEnabled(true);
				histogramBox.setEnabled(true);

			}
		});

		ActionListener act = new ActionListener(){
			@Override
   			public void actionPerformed(ActionEvent e) {
            	 if(e.getSource() == smoothBox){
            	 	try{
        				img = bitimage.applySmoothing((imagePath), 4, 4);
        			} catch(IOException ie){
        				ie.printStackTrace();
        			}
            	 }else if(e.getSource() == histogramBox){
            	 	try{
            	 		img = bitimage.applyHistogramEqualization((imagePath));
        			} catch(IOException ie){
        				ie.printStackTrace();
   					}
            	 }
            }
        };

		smoothBox.addActionListener(act);
		histogramBox.addActionListener(act);

		
		applyButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent at){	
				new OutputImageModal(img, ImageTransformPanel.this);
			}
		});


		extras = new JPanel();
		extras.setLayout(new BoxLayout(extras, BoxLayout.PAGE_AXIS));
		extras.setBorder(new TitledBorder(new EtchedBorder(), "Extra configs for option selected"));

		kernelLabel = new JLabel("Enter n for an n by n kernel");
		
		
		matrixOrder = new JSpinner (new SpinnerNumberModel(
			3,			// initial value
			3,			// minimum value
			12,			// maximum value
			1			// step
		));
		matrixOrder.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				JSpinner gf = (JSpinner)e.getSource();
				order = (Integer)gf.getValue();
			}	
		});


		JPanel inputToKernelPanel = new JPanel();
		inputToKernelPanel.setLayout(new BoxLayout(inputToKernelPanel, BoxLayout.LINE_AXIS));
		inputToKernelPanel.add(kernelLabel);
		inputToKernelPanel.add(matrixOrder);

		extras.add(inputToKernelPanel);
		//extras.add();

		add(loadImage);
		add(smoothBox);
		add(histogramBox);
		add(extras);
		add(applyButton);

	}

	public void setImagePath(String imagePath){
		this.imagePath = imagePath;
	}
}


/*
class KernelModel extends AbstractTableMode{
	private int [][] data;

	public int getRowCount() {
   		return data.length;
  	}

  	public int getColumnCount() {
   		return 0;
  	}

	public int [][] getData(){
		return data;
	}

	public void setMatrix(int order){
		data = new int[order][order];
	}

	public Object getValueAt(int row, int col) {
      return data[row][col];
    }

    public void setValueAt(Object value, int row, int col){
      data[row][col] = value;
      fireTableCellUpdated(row, col);
    }


    public boolean isCellEditable(int row, int column) {
        return true;
    }
}
*/

class OutputImageModal extends JDialog{

	private JLabel outImgLabel;
	private JButton saveButton, noSaveButton;
	private ImageUtils imageUtils = new ImageUtils();
	private JPanel imagePanel, commandPanel;

	public OutputImageModal(BufferedImage img, ImageTransformPanel transformPanel){

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
 				    transformPanel.setImagePath(file.getAbsolutePath());
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
