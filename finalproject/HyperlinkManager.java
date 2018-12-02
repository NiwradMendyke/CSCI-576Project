package finalproject;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.nio.file.Files;
import java.util.*;

public class HyperlinkManager { // secondary window used to allow user to modify start/end frames of a selected hyperlink

	Hyperlink currLink;

	JFrame frame, parentFrame;
	JTextField startFrame, endFrame;
	JButton changeName, update;

	DrawableLabel parentLabel;


	public HyperlinkManager(JFrame parent, DrawableLabel label) {
		parentFrame = parent; // reference to main parent frame
		parentLabel = label; // reference to the drawablelabel

		createFrame();
	}

	public void createFrame() { // used to create the manager gui

		JLabel startInputLabel, endInputLabel;

		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		startInputLabel = new JLabel();
		startInputLabel.setHorizontalAlignment(SwingConstants.CENTER);
		startInputLabel.setText("Edit hyperlink start frame");

		endInputLabel = new JLabel();
		endInputLabel.setHorizontalAlignment(SwingConstants.CENTER);
		endInputLabel.setText("Edit hyperlink end frame");

		startFrame = new JTextField(10);
		endFrame = new JTextField(10);

		changeName = new JButton("Edit Name");
		changeName.setHorizontalAlignment(SwingConstants.CENTER);
		changeName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newName = JOptionPane.showInputDialog(parentFrame, "Enter a new name for the selected Hyperlink");
				if (newName != null) {
					parentLabel.updateName(currLink.getName(), newName);

					frame.setTitle(newName);
				}
			}
		});

		update = new JButton("Update");
		update.setHorizontalAlignment(SwingConstants.CENTER);
		update.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	int start = Integer.parseInt(startFrame.getText());
            	int end = Integer.parseInt(endFrame.getText());

            	if (start > end) {
            		JOptionPane.showMessageDialog(parentFrame, "End frame must be after start frame");
            		return;
            	}
            	if (start < 1 || end < 1) {
            		JOptionPane.showMessageDialog(parentFrame, "Frame number must be positive");
            		return;
            	}
            	if (start > 9000 || end > 9000) {
					JOptionPane.showMessageDialog(parentFrame, "Frame number must be less than length of video");
            		return;
            	}
                currLink.setStartEndFrames(start, end);


                parentFrame.revalidate();
				parentFrame.repaint();
				parentLabel.updateFrame(); // updates the current frame with the new start/end of the current hyperlink
            }
        });

		GridBagConstraints c = new GridBagConstraints();


		// Add all components to frame
		c.weightx = 1;
        c.weighty = 1;
		c.gridx = 0;
        c.gridy = 0;
		frame.add(startInputLabel, c);

		c.gridx = 1;
		frame.add(endInputLabel, c);

		c.gridx = 0;
		c.gridy = 1;
		frame.add(startFrame, c);

		c.gridx = 1;
		frame.add(endFrame, c);

		c.gridx = 0;
		c.gridy = 2;
		frame.add(changeName, c);

		c.gridx = 1;
		frame.add(update, c);

		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(400, 100));
		frame.pack();
	}

	public void updateManager(Hyperlink selectedLink) { // used to display the manager with a given hyperlink
		currLink = selectedLink;

		frame.setTitle(currLink.getName());

		startFrame.setText(Integer.toString(currLink.getStart()));
		endFrame.setText(Integer.toString(currLink.getStart() + currLink.getLength()));

		frame.revalidate();
		frame.repaint();

		if (!frame.isVisible()) {
			frame.setVisible(true);
			frame.toFront();
		}
	}

	public void hideFrame() {
		frame.setVisible(false);
	}
}