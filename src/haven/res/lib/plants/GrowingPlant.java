package haven.res.lib.plants;

import haven.Coord3f;
import haven.FastMesh;
import haven.FastMesh.MeshRes;
import haven.Gob;
import haven.Material;
import haven.MeshBuf;
import haven.Message;
import haven.RenderList;
import haven.Rendered;
import haven.Resource;
import haven.Sprite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GrowingPlant extends Sprite
{
    private Rendered[] parts;
    private FastMesh[] meshes;

    private void cons(Gob gob, Resource res, int paramInt1, int paramInt2)
    {
	ArrayList<Integer> meshes = new ArrayList<Integer>();
	Collection<MeshRes> allMeshes = res.layers(FastMesh.MeshRes.class);
	for (MeshRes mesh : allMeshes ) { 
	    if ((mesh.id == paramInt2) && (mesh.mat != null) && (!meshes.contains(Integer.valueOf(mesh.ref))))
		meshes.add(Integer.valueOf(((FastMesh.MeshRes)mesh).ref));
	}
	HashMap<Material, MeshBuf> mats = new HashMap<Material, MeshBuf>();
	Object rand = gob.mkrandoom();
	float f1 = gob.glob.map.getcz(gob.rc);
	Coord3f localCoord3f;
	float f2;
	float f3;
	int j;
	for (int i = 0; i < paramInt1; i++)
	{
	    float f4 = ((Random)rand).nextFloat() * 44.0F - 22.0F;
	    float f5 = ((Random)rand).nextFloat() * 44.0F - 22.0F;
	    localCoord3f = new Coord3f(f4, f5, gob.glob.map.getcz(gob.rc.x + f4, gob.rc.y + f5) - f1);
	    double d = ((Random)rand).nextDouble() * 3.141592653589793D * 2.0D;
	    f2 = (float)Math.sin(d);
	    f3 = (float)Math.cos(d);
	    if(meshes.size() != 0){
		j = ((Integer)meshes.get(((Random)rand).nextInt(meshes.size()))).intValue();
		for (FastMesh.MeshRes localMeshRes : allMeshes) {
		    if (localMeshRes.ref == j) {
			MeshBuf localMeshBuf = (MeshBuf)mats.get(localMeshRes.mat.get());
			if (localMeshBuf == null)
			    mats.put(localMeshRes.mat.get(), localMeshBuf = new MeshBuf());
			MeshBuf.Vertex[] arrayOfVertex1 = localMeshBuf.copy(localMeshRes.m);
			for (MeshBuf.Vertex localVertex : arrayOfVertex1) {
			    float f6 = localVertex.pos.x; float f7 = localVertex.pos.y;
			    localVertex.pos.x = (f6 * f3 - f7 * f2 + localCoord3f.x);
			    localVertex.pos.y = (f7 * f3 + f6 * f2 - localCoord3f.y);
			    localVertex.pos.z += localCoord3f.z;
			    float f8 = localVertex.nrm.x; float f9 = localVertex.nrm.y;
			    localVertex.nrm.x = (f8 * f3 - f9 * f2);
			    localVertex.nrm.y = (f9 * f3 + f8 * f2);
			}
		    }
		}
	    }
	}
	this.meshes = new FastMesh[mats.size()];
	this.parts = new Rendered[mats.size()];
	int i = 0;
	for (Map.Entry<Material, MeshBuf> localEntry : mats.entrySet()) {
	    this.meshes[i] = localEntry.getValue().mkmesh();
	    this.parts[i] = localEntry.getKey().apply(this.meshes[i]);
	    i++;
	}
    }

    public GrowingPlant(Gob paramGob, Resource paramResource, int paramInt1, int paramInt2) {
	super(paramGob, paramResource);
	cons(paramGob, paramResource, paramInt1, paramInt2);
    }

    public boolean setup(RenderList paramRenderList) {
	for (Rendered localRendered : this.parts)
	    paramRenderList.add(localRendered, null);
	return false;
    }

    public void dispose() {
	for (FastMesh localFastMesh : this.meshes)
	    localFastMesh.dispose();
    }

    public static class Factory
    implements Sprite.Factory
    {
	private final int sn;

	public Factory(int paramInt)
	{
	    this.sn = paramInt;
	}

	public Sprite create(Sprite.Owner paramOwner, Resource paramResource, Message paramMessage) {
	    return new GrowingPlant((Gob)paramOwner, paramResource, this.sn, paramMessage.uint8());
	}
    }
}