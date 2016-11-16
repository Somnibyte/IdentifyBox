/*
*	File: ImageProcessGUI.java
*
*	Naming conventions:
*	Buttons - (name)Btn
*	Panels - (name)Panel
*	Radio Button - (name)RdioBtn
*	Labels - (name)Label
* 	ButtonGroup - (name)BtnGrp
*   Dialog - (name)diag
*   ActionListener - (name)listner
*
*
*	Components are added in the order they appear.
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


/**
 * 	This is the main GUI class
 */
public class ImageProcessGUI extends JFrame{

	private LoadInitialImagePanel loadPanel;
	private ImageTransformPanel transformPanel;

	public ImageProcessGUI(){
		super("Image Processing Application");
		getContentPane().setLayout(new BorderLayout());

		loadPanel = new LoadInitialImagePanel("Original Image");
		transformPanel = new ImageTransformPanel("Steps to generate output images", loadPanel);

		add(loadPanel, BorderLayout.CENTER);
		add(transformPanel, BorderLayout.LINE_END);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(950, 750);
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


/**
 *  This acts the display for the initial image to be processed
 */
class LoadInitialImagePanel extends JPanel{

	private JLabel imgTitleLabel, emptyLabel;
	private JButton loadImageBtn;

	private ImageUtils imageUtils = new ImageUtils();
	private String path = " ";

	public LoadInitialImagePanel(String title){
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(new EtchedBorder(), title));

		imgTitleLabel = new JLabel("Image:");
		emptyLabel = new JLabel();
		loadImageBtn = new JButton("Load Image to be Compared Against");
		loadImageBtn.addActionListener(new ActionListener(){
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
						emptyLabel.setIcon(new ImageIcon(imageUtils.resize(img, scaledDimens.get(0), scaledDimens.get(1))));
					} catch (IOException evt){
						evt.printStackTrace();
					}
				}
			}
		});

		add(loadImageBtn);
		add(imgTitleLabel);
		add(emptyLabel);

	}


	public String getPath(){
		return path;
	}

}


class ImageTransformPanel extends JPanel{

	private JButton loadImageBtn, applyOperationBtn;
	private JRadioButton smoothRdioBtn, contrastRdioBtn, histogramRdioBtn, kirschRdioBtn, lapicianRdioBtn, thinningRdioBtn;
	private ButtonGroup imgProcessOperations;
	private JLabel kernelLabel;
	private JSpinner matOrderJSpinner;		// Input for order
	private JPanel extrasPanel;

	private String imagePath = " ";
	private BufferedImage img;
	private int order = 3;				// Since kernel is a order by order matrix

	private BitImage bitImage = new BitImage();
	private KernelGrid kernelVals_GUI;

	public ImageTransformPanel(String titled, LoadInitialImagePanel loadPanel){
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(new EtchedBorder(), titled));


		loadImageBtn = new JButton("Load Image to be Processed");
		smoothRdioBtn = new JRadioButton("Apply smoothing");
		histogramRdioBtn = new JRadioButton("Apply histogram equalization");
		kirschRdioBtn = new JRadioButton("Apply kirsch edge detection");
		lapicianRdioBtn = new JRadioButton("Apply lapician edge detection");
		contrastRdioBtn = new JRadioButton("Apply constrast");
		thinningRdioBtn = new JRadioButton("Apply thinning");
		applyOperationBtn = new JButton("Apply operation to image");

		extrasPanel = new JPanel();			// holds all kernel related stuff
		JPanel inputToKernelPanel = new JPanel();
		kernelLabel = new JLabel("Enter n for an n by n kernel");


		imgProcessOperations = new ButtonGroup ();
		imgProcessOperations.add(smoothRdioBtn);
		imgProcessOperations.add(histogramRdioBtn);
		imgProcessOperations.add(lapicianRdioBtn);
		imgProcessOperations.add(contrastRdioBtn);
		imgProcessOperations.add(thinningRdioBtn);

		smoothRdioBtn.setEnabled(false);
		histogramRdioBtn.setEnabled(false);
		kirschRdioBtn.setEnabled(false);
		lapicianRdioBtn.setEnabled(false);
		contrastRdioBtn.setEnabled(false);
		thinningRdioBtn.setEnabled(false);


		loadImageBtn.addActionListener(new ActionListener(){
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

				smoothRdioBtn.setEnabled(true);
				histogramRdioBtn.setEnabled(true);
				kirschRdioBtn.setEnabled(true);
				lapicianRdioBtn.setEnabled(true);
				contrastRdioBtn.setEnabled(true);
				thinningRdioBtn.setEnabled(true);
			}
		});

		// Will remove hardcoded values
		ActionListener actListner = new ActionListener(){
			@Override
   			public void actionPerformed(ActionEvent e) {
            	 if(e.getSource() == smoothRdioBtn){
            	 	try{
            	 		String options = JOptionPane.showInputDialog(null, "Enter your frameX and frameY values respectively.Example :4,5");
            	 		if (options != null){
	            	 		String[] values = options.split(",");
	            	 		int frameX = Integer.parseInt(values[0]);
	            	 		int frameY = Integer.parseInt(values[1]);
	        				img = bitImage.applySmoothing((imagePath), frameX, frameY);
	        			}
        			} catch(IOException ie){
        				ie.printStackTrace();
        			}
            	 }else if(e.getSource() == histogramRdioBtn){
            	 	try{
            	 		img = bitImage.applyHistogramEqualization(imagePath);
        			} catch(IOException ie){
        				ie.printStackTrace();
   					}
            	 } else if(e.getSource() == contrastRdioBtn){
            	 	try{
            	 		String options = JOptionPane.showInputDialog(null, "Enter your contrast and brightness values respectively.Example :4,5");
            	 		if (options != null){
	            	 		String[] values = options.split(",");
	            	 		int contrast = Integer.parseInt(values[0]);
	            	 		int brightness = Integer.parseInt(values[1]);
            	 			img = bitImage.applyContrast((imagePath), contrast, brightness);
            	 		}
        			} catch(IOException ie){
        				ie.printStackTrace();
   					}
   				 } else if(e.getSource() == kirschRdioBtn){
            	 	try{
            	 		img = bitImage.applyKirschEdgeDetection(imagePath);
        			} catch(IOException ie){
        				ie.printStackTrace();
   					}
            	 } else if(e.getSource() == lapicianRdioBtn){
            	 	try{
            	 		img = bitImage.applyLapicianEdgeDetection(imagePath);
        			} catch(IOException ie){
        				ie.printStackTrace();
   					}
					} else if(e.getSource() == thinningRdioBtn){
            	 	try{
            	 		img = bitImage.applyThinning(imagePath);
        			} catch(IOException ie){
        				ie.printStackTrace();
   					}
            	 }
            }
        };

		smoothRdioBtn.addActionListener(actListner);
		histogramRdioBtn.addActionListener(actListner);
		kirschRdioBtn.addActionListener(actListner);
		lapicianRdioBtn.addActionListener(actListner);
		contrastRdioBtn.addActionListener(actListner);
		thinningRdioBtn.addActionListener(actListner);


		extrasPanel.setLayout(new BoxLayout(extrasPanel, BoxLayout.PAGE_AXIS));
		extrasPanel.setBorder(new TitledBorder(new EtchedBorder(), "Extra configs for option selected"));


		matOrderJSpinner = new JSpinner (new SpinnerNumberModel(
			3,			// initial value
			3,			// minimum value
			12,			// maximum value
			1			// step
		));

		kernelVals_GUI = new KernelGrid(order);

		matOrderJSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				JSpinner gf = (JSpinner)e.getSource();
				order = (Integer)gf.getValue();
				updateKernel(order);
			}
		});

		applyOperationBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent at){
				new OutputImageModal(img, ImageTransformPanel.this);
			}
		});


		inputToKernelPanel.setLayout(new BoxLayout(inputToKernelPanel, BoxLayout.LINE_AXIS));
		inputToKernelPanel.add(kernelLabel);
		inputToKernelPanel.add(matOrderJSpinner);

		extrasPanel.add(inputToKernelPanel);
		extrasPanel.add(kernelVals_GUI);

		add(loadImageBtn);
		add(smoothRdioBtn);
		add(histogramRdioBtn);
		add(lapicianRdioBtn);
		add(thinningRdioBtn);
		add(kirschRdioBtn);
		add(contrastRdioBtn);
		add(extrasPanel);
		add(applyOperationBtn);


	}

	public void setImagePath(String imagePath){
		this.imagePath = imagePath;
	}

	public void updateKernel(int order){
		extrasPanel.remove(kernelVals_GUI);
		extrasPanel.revalidate();
		extrasPanel.repaint();
		kernelVals_GUI = new KernelGrid(order);
		extrasPanel.add(kernelVals_GUI);
		extrasPanel.revalidate();
		extrasPanel.repaint();
	}

	public JPanel getKernel(){
		return kernelVals_GUI;
	}
}


class KernelGrid extends JPanel{

	private final ArrayList<JButton> list = new ArrayList<JButton>();
	private int [][] data;
	private int order;

    private JButton getGridButton(final int row, final int column) {
        int index = row * order + column;
        return list.get(index);
    }

	private JButton createGridButton(final int row, final int col, int value) {
        JButton b = new JButton(String.valueOf(value));
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	JButton gb = KernelGrid.this.getGridButton(row, col);
                String val = JOptionPane.showInputDialog(null, "Enter your value for this cell");
                if (val != null){
                	gb.setText(val);
                	data[row][col] = Integer.parseInt(val);
                }
                printKernelCells();
            }
        });
        return b;
    }

    private void resetData(int [][] data){
    	for (int i =0; i < data.length; i++){
    		for (int j = 0; j < data[0].length; j++){
    			data[i][j] = 0;
    		}
    	}
    }

    public KernelGrid(int order){
    	this.order = order;
    	data = new int[order][order];
    	resetData(data);
    	setLayout(new GridLayout(order, order));
        for (int i = 0; i < order * order; i++) {
            int row = i / order;
            int col = i % order;
            JButton gb = createGridButton(row, col, 0);
            list.add(gb);
            add(gb);
        }
	}

	public int [][] getValueData(){
		return data;
	}

	public void printKernelCells(){
		StringBuilder sb = new StringBuilder();
		for (int i =0; i < data.length; i++){
    		for (int j = 0; j < data[0].length; j++){
    			sb.append(data[i][j] + " ");
    		}
    		sb.append("\n");
    	}
    	System.out.println(sb.toString());
	}

}


class OutputImageModal extends JDialog{

	private JLabel outImgLabel;
	private JButton saveBtn, noSaveBtn;
	private ImageUtils imageUtils = new ImageUtils();
	private JPanel imagePanel, commandPanel;

	public OutputImageModal(BufferedImage img, ImageTransformPanel transformPanel){

		imagePanel = new JPanel(new BorderLayout());
		commandPanel = new JPanel(new FlowLayout());
		saveBtn = new JButton("Save");
		noSaveBtn = new JButton("Don't Save");

		outImgLabel = new JLabel();
		outImgLabel.setBorder(new TitledBorder(new EtchedBorder(), "Output Image"));

		ArrayList<Integer> scaledDimens = imageUtils.getScaledDimensions(img, new Dimension(500, 500));
		outImgLabel.setIcon(new ImageIcon(imageUtils.resize(img, scaledDimens.get(0), scaledDimens.get(1))));


		ActionListener saveListner = new ActionListener(){
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

		saveBtn.addActionListener(saveListner);
		getRootPane().setDefaultButton(saveBtn);
		getRootPane().registerKeyboardAction(saveListner,
			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);


		ActionListener exitListner = new ActionListener(){
			public void actionPerformed(ActionEvent et){
				dispose();
			}
		};
		noSaveBtn.addActionListener(exitListner );
		getRootPane().registerKeyboardAction(exitListner ,
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);

		imagePanel.add(outImgLabel, BorderLayout.PAGE_START);

		commandPanel.add(saveBtn);
		commandPanel.add(noSaveBtn);
		imagePanel.add(commandPanel, BorderLayout.PAGE_END);

		add(imagePanel);
		pack();
		setVisible(true);
	}

}
