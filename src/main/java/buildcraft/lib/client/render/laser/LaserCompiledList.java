package buildcraft.lib.client.render.laser;
public abstract class LaserCompiledList {
    public abstract void render();
    public abstract void delete();
    public static class Builder implements ILaserRenderer, AutoCloseable {
        public Builder(boolean useNormalColour) {}
        @Override public void vertex(double x, double y, double z, double u, double v, int lmap, float nx, float ny, float nz, float diffuse) {}
        public LaserCompiledList build() { return null; }
        @Override public void close() {}
    }
}
