package haven;

public class R2DWdg extends Widget {

    private PView target;

    public R2DWdg(PView target) {
	super(target.c, target.sz, target.parent);
	this.target = target;
	lower();
    }

    @Override
    public void draw(GOut g) {
	super.draw(g);
	g.chcolor();
	if(target.rls == null){return;}
	for(RenderList.Slot s : target.rls.slots()) {
	    if(s.r instanceof PView.Render2D)
		((PView.Render2D)s.r).draw2d(g);
	}
    }
}
