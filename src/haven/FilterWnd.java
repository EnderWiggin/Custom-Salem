package haven;

public class FilterWnd extends GameUI.Hidewnd{
    TextEntry input;

    FilterWnd(Widget parent) {
	super(new Coord(120, 200), Coord.z, parent, "Filter");
	cbtn.visible = false;
	cap = null;

	input = new TextEntry(Coord.z, 200, this, ""){
	    @Override
	    protected void changed() {
		if(text.length() >= 2){
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
	    WItem.setFilter(null);
	} else {
	    WItem.setFilter(ItemFilter.create(text));
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
