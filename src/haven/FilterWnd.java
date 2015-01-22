package haven;

public class FilterWnd extends GameUI.Hidewnd{
    TextEntry input;

    FilterWnd(Widget parent) {
	super(Coord.z, Coord.z, parent, "Filter");
	cap = null;

	input = new TextEntry(Coord.z, 200, this, ""){
	    @Override
	    protected void changed() {
		if(text.length() >= 3){
		    setFilter(text);
		} else {
		    setFilter(null);
		}
	    }
	};
	pack();
	hide();
    }

    private void setFilter(String text) {
	if(text == null){
	    WItem.filter = null;
	} else {
	    WItem.filter = ItemFilter.create(text);
	}
    }

    @Override
    public void hide() {
	super.hide();
	setFilter(null);
    }

    @Override
    public void show() {
	super.show();
	setfocus(input);
    }
}
