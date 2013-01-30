package com.novelbio.base.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.jdesktop.application.Application;

import com.novelbio.base.multithread.RunProcess;
/**
 * 显示进度条和读取信息的框
 * */
public class GUIprocess extends javax.swing.JPanel implements Runnable {
	private JButton btnClose;
	JDialog jDailog;
	JProgressBar progressBar;

	public GUIprocess() {
		super();
		initGUI();
	}
	public void run() {
		jDailog = new JDialog(new Frame(), true);
		jDailog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stopRunning();
			}
		});
		jDailog.getContentPane().add(this);
		jDailog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		jDailog.pack();
		jDailog.setTitle("System Infomation");
		jDailog.setVisible(true);
	}
	private void initGUI() {
		try {
			this.setPreferredSize(new Dimension(643, 81));
			{
				btnClose = new JButton();
				btnClose.setText("Stop");
				btnClose.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						 stopRunning();
					}
				});
				btnClose.setBounds(269, 38, 94, 24);
			}
			setLayout(null);
			add(btnClose);
			
			JProgressBar progressBar = new JProgressBar();
			progressBar.setBounds(12, 12, 619, 14);
			add(progressBar);
			Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void closeWindow() {
		jDailog.dispose();
	}
	private void stopRunning() {
		int result = JOptionPane.showConfirmDialog(jDailog, "Do you want to stop the running?", "message", JOptionPane.YES_NO_OPTION);
		if(result == JOptionPane.OK_OPTION) {
			jDailog.dispose();
		}
		else {
			jDailog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			return;
		}
	}
	public JProgressBar getProgressBar() {
		return progressBar;
	}
}
