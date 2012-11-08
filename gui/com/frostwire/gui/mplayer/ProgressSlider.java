package com.frostwire.gui.mplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class ProgressSlider extends JPanel {

	private static final long serialVersionUID = 8000075870624583383L;
	private JSlider progressSlider;
	private JLabel remainingTime;
	private JLabel elapsedTime;
	
	private float totalTime = 0;
	private float currentTime = 0;
	
	private LinkedList<ProgressSliderListener> listeners = new LinkedList<ProgressSliderListener>();
	
	public ProgressSlider() {
		
		setLayout(new BorderLayout());
		setOpaque(false);
        setSize(new Dimension(411, 17));
        
        elapsedTime = new JLabel("--:--");
        Font font = new Font(elapsedTime.getFont().getFontName(), Font.BOLD, 14 );
        elapsedTime.setFont(font);
        elapsedTime.setForeground(Color.white);
        add(elapsedTime, BorderLayout.WEST);
        
        progressSlider = new JSlider();
        progressSlider.setOpaque(false);
        progressSlider.setFocusable(false);
        progressSlider.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				ProgressSlider.this.onProgressSliderValueChanged(((JSlider)e.getSource()).getValue());
			}
        });
        progressSlider.addMouseListener( new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				for (ProgressSliderListener l : listeners ) {
					l.onProgressSliderMouseDown();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				for (ProgressSliderListener l : listeners ) {
					l.onProgressSliderMouseUp();
				}
			}
        	
        });
        
        add(progressSlider, BorderLayout.CENTER);
        
        remainingTime = new JLabel("--:--");
        remainingTime.setFont(font);
        remainingTime.setForeground(Color.white);
        add(remainingTime, BorderLayout.EAST);
        
	}
	
	public void addProgressSliderListener( ProgressSliderListener listener ) {
		listeners.add(listener);
	}
	
	public void removeProgressSliderListener( ProgressSliderListener listener ) {
		listeners.remove(listener);
	}
	
	public void setTotalTime(int seconds) {
		if ( seconds != totalTime ) {
			totalTime = seconds;
			currentTime = 0;
			updateUIControls();
		}
	}
	
	public void setCurrentTime(int seconds) {
		if ( seconds != currentTime) {
			currentTime = seconds;
			updateUIControls();
		}
	}
	
	public void onProgressSliderValueChanged( int value ) {
		
		int seconds = (int) (value / 100.0 * totalTime);
		for (ProgressSliderListener l : listeners ) {
			l.onProgressSliderTimeValueChange(seconds);
		}
	}
	
	private void updateUIControls() {
		elapsedTime.setText(TimeUtils.getTimeFormatedString((int) currentTime));
		remainingTime.setText(TimeUtils.getTimeFormatedString((int) (totalTime - currentTime)));
		
		int progressValue = (int) (currentTime / totalTime * 100);
		progressSlider.setValue(Math.max(0, Math.min(100,progressValue)));
	}

}
