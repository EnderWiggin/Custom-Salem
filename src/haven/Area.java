package haven;

import java.util.Iterator;

public final class Area implements Iterable<Coord> {
    public final int north;
    public final int west;
    public final int south;
    public final int east;
    
    public Area(int north, int west, int south, int east) {
        this.north = north;
        this.west = west;
        this.south = south;
        this.east = east;
    }

    public Coord[] coords() {
        Coord[] result = new Coord[(Math.abs(north - south) + 1) * (Math.abs(west - east) + 1)];
        int i = 0;
        for (Coord c : this) {
            result[i++] = c;
        }
        return result;
    }

    @Override
    public Iterator<Coord> iterator() {
        return new AreaIterator();
    }

    private class AreaIterator implements Iterator<Coord> {
        private Coord next;
        private Coord offset;

        public AreaIterator() {
            next = new Coord(west, north);
            offset = new Coord(Integer.signum(east - west), Integer.signum(south - north));
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Coord next() {
            if (next == null)
                throw new IllegalStateException();

            Coord current = next;
            
            if (next.x != east) {
                next = next.add(offset.x, 0);
            } else if (next.y != south) {
                next = new Coord(west, next.y + offset.y);
            } else {
                next = null;
            }

            return current;
        }

        @Override
        public void remove() {
        }
    }
}
