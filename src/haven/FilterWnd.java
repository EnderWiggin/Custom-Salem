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
		chectInput();
	    }
	};
	pack();
	hide();
    }

    private void setFilter(String text) {
	if(text == null){
	    GItem.setFilter(null);
	} else {
	    GItem.setFilter(ItemFilter.create(text));
	}
    }

    private void chectInput() {
	if(input.text.length() >= 2){
	    setFilter(input.text);
	} else {
	    setFilter(null);
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
	chectInput();
	raise();
    }
}
