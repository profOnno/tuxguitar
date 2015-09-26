package org.herac.tuxguitar.android.action.impl.view;

import org.herac.tuxguitar.action.TGActionContext;
import org.herac.tuxguitar.android.action.TGActionBase;
import org.herac.tuxguitar.android.view.keyboard.TGTabKeyboard;
import org.herac.tuxguitar.util.TGContext;

public class TGToggleTabKeyboardAction extends TGActionBase{
	
	public static final String NAME = "action.view.toogle-tab-keyboard";
	
	public TGToggleTabKeyboardAction(TGContext context) {
		super(context, NAME);
	}
	
	protected void processAction(TGActionContext context) {
		TGTabKeyboard tgTabKeyboard = TGTabKeyboard.getInstance(getContext());
		if( tgTabKeyboard != null ) {
			tgTabKeyboard.toggleVisibility();
		}
	}
}
