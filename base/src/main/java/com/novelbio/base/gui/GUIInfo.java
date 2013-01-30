package com.novelbio.base.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.jdesktop.application.Application;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.multithread.RunProcess;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class GUIInfo extends javax.swing.JPanel  implements Runnable {
	private JButton btnClose;
	JTextArea textArea;
	RunProcess runProcess;
	JDialog jDialog;
	
	public static void main(String[] args) {
		GUIInfo guiInfo = new GUIInfo(null);
		guiInfo.run();
		
	}
	public GUIInfo(RunProcess runProcess) {
		super();
		initGUI();
		this.runProcess = runProcess;
	}
	public void run() {
		jDialog = new JDialog(new Frame(), true);
		jDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stopRunning();
			}
		});
		
		Image im = Toolkit.getDefaultToolkit().getImage("/media/winE/NBC/advertise/宣传/LOGO/favicon.png");
		jDialog.setIconImage(im);
		jDialog.setResizable(false);
		jDialog.getContentPane().add(this);
		jDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		jDialog.pack();
		jDialog.setTitle("System Infomation");
		jDialog.setVisible(true);
	}
	private void initGUI() {
		try {
			this.setPreferredSize(new Dimension(643, 557));
			{
				btnClose = new JButton();
				btnClose.setText("Stop");
				btnClose.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
//						 stopRunning();
						String cmd = "Rscript /media/winD/fedora/gitNovelbio/Novelbio-Bioinformatics-Analysis-Platform/target/rscript/tmp/TopGO_2012-09-09040524123.R";
						CmdOperate cmdOperate = new CmdOperate(cmd);
						Thread thread = new Thread(cmdOperate);
						thread.start();
						while (true) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				});
				btnClose.setBounds(272, 516, 94, 24);
			}
			setLayout(null);
			add(btnClose);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(12, 12, 619, 490);
			add(scrollPane);
			
			textArea = new JTextArea();
			scrollPane.setViewportView(textArea);
			textArea.setWrapStyleWord(true);
			Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void closeWindow() {
		jDialog.dispose();
	}
	private void stopRunning() {
		int result = JOptionPane.showConfirmDialog(null, "Do you want to stop the running?", "message", JOptionPane.YES_NO_OPTION);
		if(result == JOptionPane.OK_OPTION) {
			if (runProcess != null) {
				runProcess.threadStop();
			}
			jDialog.dispose();
		}
		else {
			jDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			return;
		}
	}
	/** 自动在textInfo尾巴加上 “\n” */
	public void appendTxtInfo(String textInfo) {
		synchronized (this) {
			textArea.append(textInfo + "\n");
		}
	}
	/** 不加"\n" */
	public void addTxtInfo(String textInfo) {
		synchronized (this) {
			textArea.append(textInfo);
		}
	}
	public void setTxtInfo(String textInfo) {
		synchronized (this) {
			textArea.setText(textInfo);
		}
	}
	
	
}

