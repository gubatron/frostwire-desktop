package com.limegroup.gnutella.gui.themes.fueled;

import java.awt.Color;

import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ColorSchemeSingleColorQuery;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceColorSchemeBundle;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.painter.border.ClassicBorderPainter;
import org.pushingpixels.substance.api.painter.border.FractionBasedBorderPainter;
import org.pushingpixels.substance.api.painter.decoration.MatteDecorationPainter;
import org.pushingpixels.substance.api.painter.fill.FractionBasedFillPainter;
import org.pushingpixels.substance.api.painter.highlight.ClassicHighlightPainter;
import org.pushingpixels.substance.api.painter.overlay.BottomLineOverlayPainter;
import org.pushingpixels.substance.api.shaper.ClassicButtonShaper;

public class FueledSkin extends SubstanceSkin {
	
    public static final String NAME = "Fueled";

	public FueledSkin() {
		SubstanceSkin.ColorSchemes schemes = SubstanceSkin
				.getColorSchemes("org/limewire/gui/resources/fueled.colorschemes");

		SubstanceColorScheme activeScheme = schemes.get("Fueled Active");
		SubstanceColorScheme enabledScheme = schemes.get("Fueled Enabled");
		SubstanceColorScheme disabledScheme = schemes.get("Fueled Disabled");

		SubstanceColorSchemeBundle defaultSchemeBundle = new SubstanceColorSchemeBundle(
				activeScheme, enabledScheme, disabledScheme);

		// borders
		SubstanceColorScheme activeBorderScheme = schemes
				.get("Fueled Active Border");
		SubstanceColorScheme enabledBorderScheme = schemes
				.get("Fueled Enabled Border");
		SubstanceColorScheme disabledBorderScheme = schemes
				.get("Fueled Enabled Border");
		defaultSchemeBundle.registerColorScheme(enabledBorderScheme,
				ColorSchemeAssociationKind.BORDER, ComponentState.ENABLED);
		defaultSchemeBundle.registerColorScheme(activeBorderScheme,
				ColorSchemeAssociationKind.BORDER, ComponentState.DEFAULT);
		defaultSchemeBundle.registerColorScheme(disabledBorderScheme,
				ColorSchemeAssociationKind.BORDER,
				ComponentState.DISABLED_DEFAULT,
				ComponentState.DISABLED_SELECTED,
				ComponentState.DISABLED_UNSELECTED);

		// states
		SubstanceColorScheme defaultScheme = schemes.get("Fueled Default");
		SubstanceColorScheme defaultBorderScheme = schemes
				.get("Fueled Default Border");
		defaultSchemeBundle.registerColorScheme(defaultScheme,
				ComponentState.DEFAULT);
		defaultSchemeBundle.registerColorScheme(defaultBorderScheme,
				ColorSchemeAssociationKind.BORDER, ComponentState.DEFAULT);

		SubstanceColorScheme pressedScheme = schemes.get("Fueled Pressed");
		SubstanceColorScheme pressedBorderScheme = schemes
				.get("Fueled Pressed Border");
		defaultSchemeBundle.registerColorScheme(pressedScheme,
				ComponentState.PRESSED_SELECTED,
				ComponentState.PRESSED_UNSELECTED);
		defaultSchemeBundle.registerColorScheme(pressedBorderScheme,
				ColorSchemeAssociationKind.BORDER,
				ComponentState.PRESSED_SELECTED,
				ComponentState.PRESSED_UNSELECTED);

		SubstanceColorScheme selectedScheme = schemes.get("Fueled Selected");
		SubstanceColorScheme selectedBorderScheme = schemes
				.get("Fueled Selected Border");
		defaultSchemeBundle.registerColorScheme(selectedScheme,
				ComponentState.SELECTED, ComponentState.ROLLOVER_SELECTED);
		defaultSchemeBundle.registerColorScheme(selectedBorderScheme,
				ColorSchemeAssociationKind.BORDER, ComponentState.SELECTED,
				ComponentState.ROLLOVER_SELECTED);

		SubstanceColorScheme backgroundScheme = schemes
				.get("Fueled Background");

		this.registerDecorationAreaSchemeBundle(defaultSchemeBundle,
				backgroundScheme, DecorationAreaType.NONE);

		this.registerAsDecorationArea(activeScheme,
				DecorationAreaType.PRIMARY_TITLE_PANE);

		this.addOverlayPainter(new BottomLineOverlayPainter(
				new ColorSchemeSingleColorQuery() {
					@Override
					public Color query(SubstanceColorScheme scheme) {
						return scheme.getDarkColor().darker();
					}
				}), DecorationAreaType.PRIMARY_TITLE_PANE);

		this.buttonShaper = new ClassicButtonShaper();
		this.watermark = new FueledSkinWatermark();
		this.fillPainter = new FractionBasedFillPainter("Fueled",
				new float[] { 0.0f, 0.49999f, 0.5f, 0.65f, 1.0f },
				new ColorSchemeSingleColorQuery[] {
						ColorSchemeSingleColorQuery.EXTRALIGHT,
						ColorSchemeSingleColorQuery.LIGHT,
						ColorSchemeSingleColorQuery.MID,
						ColorSchemeSingleColorQuery.LIGHT,
						ColorSchemeSingleColorQuery.ULTRALIGHT });

		this.decorationPainter = new MatteDecorationPainter();
		this.highlightPainter = new ClassicHighlightPainter();

		this.borderPainter = new FractionBasedBorderPainter("Fueled",
				new float[] { 0.0f, 0.5f, 1.0f },
				new ColorSchemeSingleColorQuery[] {
						ColorSchemeSingleColorQuery.MID,
						ColorSchemeSingleColorQuery.DARK,
						ColorSchemeSingleColorQuery.ULTRADARK });
		this.highlightBorderPainter = new ClassicBorderPainter();
	}

	public String getDisplayName() {
		return NAME;
	}
}
