package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.plaf.synth.SynthSliderUI;

import com.frostwire.gui.theme.SkinRangeSliderUI;

public class RangeSlider2 extends JSlider {

    protected int thumbNum;

    protected BoundedRangeModel[] sliderModels;

    protected Icon[] thumbRenderers;

    protected Color[] fillColors;

    protected Color trackFillColor;

    public RangeSlider2() {
      createThumbs(2);
      updateUI();
    }

    protected void createThumbs(int n) {
      thumbNum = n;
      sliderModels = new BoundedRangeModel[n];
      thumbRenderers = new Icon[n];
      fillColors = new Color[n];
      for (int i = 0; i < n; i++) {
        sliderModels[i] = new DefaultBoundedRangeModel(50, 0, 0, 100);
        thumbRenderers[i] = null;
        fillColors[i] = null;
      }
    }

    public int getThumbNum() {
      return thumbNum;
    }

    public int getValueAt(int index) {
      return getModelAt(index).getValue();
    }

    public void setValueAt(int n, int index) {
      getModelAt(index).setValue(n);
      // should I fire?
    }

    public int getMinimum() {
      return getModelAt(0).getMinimum();
    }

    public int getMaximum() {
      return getModelAt(0).getMaximum();
    }

    public BoundedRangeModel getModelAt(int index) {
      return sliderModels[index];
    }

    public Icon getThumbRendererAt(int index) {
      return thumbRenderers[index];
    }

    public void setThumbRendererAt(Icon icon, int index) {
      thumbRenderers[index] = icon;
    }

    public Color getFillColorAt(int index) {
      return fillColors[index];
    }

    public void setFillColorAt(Color color, int index) {
      fillColors[index] = color;
    }

    public Color getTrackFillColor() {
      return trackFillColor;
    }

    public void setTrackFillColor(Color color) {
      trackFillColor = color;
    }

    @Override
    public void updateUI() {
        setUI(new SkinRangeSliderUI(this));
    }

    public static class MThumbSliderExample2 extends JFrame {
        public MThumbSliderExample2() {
            super("MThumbSlider Example");

            JSlider slider = new JSlider();
            slider.setUI((SynthSliderUI) SynthSliderUI.createUI(slider));

            RangeSlider2 mSlider = new RangeSlider2();
            mSlider.setValueAt(25, 0);
            mSlider.setValueAt(75, 1);

            getContentPane().setLayout(new FlowLayout());
            getContentPane().add(slider);
            getContentPane().add(mSlider);
        }
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception ex) {
            System.err.println("Error loading L&F: " + ex);
        }

        MThumbSliderExample2 f = new MThumbSliderExample2();
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setSize(300, 100);
        f.show();
    }
}
