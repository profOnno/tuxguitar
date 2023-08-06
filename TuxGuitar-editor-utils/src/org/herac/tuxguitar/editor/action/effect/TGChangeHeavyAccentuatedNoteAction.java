package org.herac.tuxguitar.editor.action.effect;

import org.herac.tuxguitar.action.TGActionContext;
import org.herac.tuxguitar.document.TGDocumentContextAttributes;
import org.herac.tuxguitar.editor.action.TGActionBase;
import org.herac.tuxguitar.song.models.TGBeat;
import org.herac.tuxguitar.song.models.TGMeasure;
import org.herac.tuxguitar.song.models.TGNote;
import org.herac.tuxguitar.song.models.TGString;
import org.herac.tuxguitar.util.TGContext;
import org.herac.tuxguitar.util.TGNoteRange;

public class TGChangeHeavyAccentuatedNoteAction extends TGActionBase {
	
	public static final String NAME = "action.note.effect.change-heavy-accentuated";
	
	public TGChangeHeavyAccentuatedNoteAction(TGContext context) {
		super(context, NAME);
	}
	
	protected void processAction(TGActionContext context){
		TGNoteRange noteRange = context.getAttribute(TGDocumentContextAttributes.ATTRIBUTE_NOTE_RANGE);

		if (noteRange!=null && !noteRange.isEmpty()) {
			boolean newValue = true;
			if (noteRange.getNotes().stream().allMatch(n -> n.getEffect().isHeavyAccentuatedNote())) {
				newValue = false;
			}
			for (TGNote note : noteRange.getNotes()) {
				getSongManager(context).getMeasureManager().setHeavyAccentuatedNote(note, newValue);
			}
		} else {
			TGMeasure measure = ((TGMeasure) context.getAttribute(TGDocumentContextAttributes.ATTRIBUTE_MEASURE));
			TGBeat beat = ((TGBeat) context.getAttribute(TGDocumentContextAttributes.ATTRIBUTE_BEAT));
			TGString string = ((TGString) context.getAttribute(TGDocumentContextAttributes.ATTRIBUTE_STRING));
			
			getSongManager(context).getMeasureManager().changeHeavyAccentuatedNote(measure, beat.getStart(), string.getNumber());
		}
	}
}
