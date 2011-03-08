/*
 * Copyright (c) 2005-2010 Substance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Substance Kirill Grouchnikov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package test;

import java.awt.*;

import javax.swing.*;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.shaper.SubstanceButtonShaper;
import org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel;
import org.pushingpixels.substance.shaperpack.*;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class TestButtons extends JFrame {
	private JButton getButton(String text, SubstanceButtonShaper shaper) {
		JButton button = new JButton(text);
		button.putClientProperty(SubstanceLookAndFeel.BUTTON_SHAPER_PROPERTY,
				shaper);
		return button;
	}

	public TestButtons() {
		super("Button shapes");

		FormLayout layout = new FormLayout("center:pref, 4dlu, center:pref");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.append(this.getButton("rhino", new RhinoButtonShaper()), this
				.getButton("big rhino", new RhinoButtonShaper()));

		builder.append(this.getButton("fish", new FishButtonShaper()), this
				.getButton("big fish", new FishButtonShaper()));

		builder.append(this.getButton("racecar", new RaceCarButtonShaper()),
				this.getButton("big racecar", new RaceCarButtonShaper()));

		builder.append(
				this.getButton("butterfly", new ButterflyButtonShaper()),
				this.getButton("big butterfly", new ButterflyButtonShaper()));

		builder.append(this.getButton("foot", new FootButtonShaper()), this
				.getButton("big foot", new FootButtonShaper()));

		builder.append(this.getButton("icecream", new IceCreamButtonShaper()),
				this.getButton("big icecream", new IceCreamButtonShaper()));

		builder.append(this.getButton("stegosaurus",
				new StegosaurusButtonShaper()), this.getButton(
				"big stegosaurus", new StegosaurusButtonShaper()));

		builder.append(this.getButton("dolphin", new DolphinButtonShaper()),
				this.getButton("big dolphin", new DolphinButtonShaper()));
		
		this.add(builder.getPanel(), BorderLayout.CENTER);
	}

	public static void main(String... args) {
		try {
			UIManager
					.setLookAndFeel(new SubstanceBusinessBlackSteelLookAndFeel());
		} catch (Exception exc) {
		}
		JFrame.setDefaultLookAndFeelDecorated(true);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TestButtons tb = new TestButtons();
				tb.invalidate();
				System.out.println(tb.getPreferredSize());
				tb.pack();
				tb.setLocationRelativeTo(null);
				tb.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				tb.setVisible(true);
			}
		});
	}
}
