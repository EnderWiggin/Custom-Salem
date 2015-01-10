package haven;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ArtificeData implements ItemData.ITipData {
    public int pmax;
    public int pmin;
    public String[] profs;
    public String[] attrs;
    public int[] vals;

    public ArtificeData() {

    }

    public ArtificeData(ItemInfo info) {
	pmin = (int) (100*Reflect.getFieldValueDouble(info,"pmin"));
	pmax = (int) (100*Reflect.getFieldValueDouble(info,"pmax"));
	profs = (String[]) Reflect.getFieldValue(info, "attrs");
	//noinspection unchecked
	List<ItemInfo> sub = (List<ItemInfo>) Reflect.getFieldValue(info, "sub");
	info = sub.get(0);
	attrs = (String[]) Reflect.getFieldValue(info, "attrs");
	vals = (int[]) Reflect.getFieldValue(info, "vals");
    }

    @Override
    public ItemInfo.Tip create() {
	Resource res = Resource.load("ui/tt/slot");
	if(res == null){return null;}
	ItemInfo.InfoFactory f = res.layer(Resource.CodeEntry.class).get(ItemInfo.InfoFactory.class);
	Session sess = UI.instance.sess;
	int rid = sess.getresid("ui/tt/dattr");
	if(rid == 0){return null;}
	Object[] bonuses = new Object[1 + attrs.length + vals.length];
	bonuses[0] = rid;
	for(int k = 0; k< attrs.length; k++){
	    bonuses[1 + 2*k] = attrs[k];
	    bonuses[2 + 2*k] = vals[k];
	}
	Object[] args = new Object[4 + profs.length];
	int i=0;
	args[i++] = 0;
	args[i++] = pmin;
	args[i++] = pmax;
	for(String prof : profs){
	    args[i++] = prof;
	}
	args[i] = new Object[]{bonuses};

	return (ItemInfo.Tip) f.build(sess, args);
    }

    public static class DataAdapter extends TypeAdapter<ArtificeData> {

	@Override
	public void write(JsonWriter writer, ArtificeData data) throws IOException {
	    writer.beginObject();
	    writer.name("pmin").value(data.pmin);
	    writer.name("pmax").value(data.pmax);

	    writer.name("profs");
	    writer.beginArray();
	    for(String prof : data.profs){
		writer.value(prof);
	    }
	    writer.endArray();

	    writer.name("bonuses");
	    writer.beginObject();
	    int n = data.attrs.length;
	    for(int i=0; i < n; i++){
		writer.name(data.attrs[i]).value(data.vals[i]);
	    }
	    writer.endObject();

	    writer.endObject();
	}

	@Override
	public ArtificeData read(JsonReader reader) throws IOException {
	    ArtificeData data = new ArtificeData();
	    reader.beginObject();
	    while(reader.hasNext()){
		String name = reader.nextName();
		if(name.equals("pmin")){
		    data.pmin = reader.nextInt();
		} else if(name.equals("pmax")) {
		    data.pmax = reader.nextInt();
		} else if(name.equals("profs")) {
		    data.profs = parseArray(reader);
		} else if(name.equals("bonuses")) {
		    parseObject(reader, data);
		}
	    }
	    reader.endObject();
	    return data;
	}

	private void parseObject(JsonReader reader, ArtificeData data) throws IOException {
	    List<String> names = new LinkedList<String>();
	    List<Integer> vals = new LinkedList<Integer>();

	    reader.beginObject();
	    while(reader.hasNext()){
		names.add(reader.nextName());
		vals.add(reader.nextInt());
	    }
	    reader.endObject();

	    data.attrs = names.toArray(new String[names.size()]);

	    data.vals = new int[vals.size()];
	    for(int i = 0; i < data.vals.length; i++) {
		data.vals[i] = vals.get(i);
	    }
	}

	private String[] parseArray(JsonReader reader) throws IOException {
	    List<String> values = new LinkedList<String>();
	    reader.beginArray();
	    while(reader.hasNext()){
		values.add(reader.nextString());
	    }
	    reader.endArray();
	    return values.toArray(new String[values.size()]);
	}
    }
}
